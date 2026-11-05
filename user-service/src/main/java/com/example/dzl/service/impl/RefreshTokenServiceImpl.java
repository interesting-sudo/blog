package com.example.dzl.service.impl;

import com.example.dzl.enums.CommonErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.service.RefreshTokenService;
import com.example.dzl.utils.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import javax.servlet.http.Cookie;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RedisCache redisCache;

    @Value("${jwt.refresh-token.cookie-name:refresh_token}")
    private String cookieName;

    @Value("${jwt.refresh-token.expire:604800}")
    private long refreshExpireSeconds;

    @Value("${jwt.refresh-token.sliding:true}")
    private boolean sliding;

    @Value("${jwt.refresh-token.rotation:false}")
    private boolean rotation;


    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ---------------- Redis Keys ----------------
    private String keyUserRt(Long userId) {
        return "auth:rt:" + userId;
    }

    private String keyRtid(String rtid) {
        return "auth:rtid:" + rtid;
    }

    // ---------------- Public APIs ----------------

    @Override
    public String generateRefreshToken(Long userId, HttpServletResponse response) {
        // 1) 生成 rtid（refreshToken）
        String rtid = generateRtid();

        // 2) 写 Redis（单设备：覆盖旧的）
        // auth:rtid:<rtid> -> userId
        redisCache.setCacheString(keyRtid(rtid),String.valueOf(userId),refreshExpireSeconds);
        // auth:rt:<userId> -> rtid
        redisCache.setCacheString(keyUserRt(userId), rtid, refreshExpireSeconds);

        // 3) 写 HttpOnly Cookie
        writeRefreshCookie(response, rtid);

        return rtid;
    }

    @Override
    public Long verifyRefreshToken(HttpServletRequest request) {
        String rtid = readCookie(request, cookieName)
                .orElseThrow(() -> new BusinessException(CommonErrorEnum.UNAUTHORIZED));

        // 1) rtid -> userId
        String userIdStr = redisCache.getCacheString(keyRtid(rtid));
        if (userIdStr == null) {
            // 数据被污染/异常
            throw new BusinessException(CommonErrorEnum.UNAUTHORIZED);
        }

        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            // 数据被污染/异常
            throw new BusinessException(CommonErrorEnum.UNAUTHORIZED);
        }

        // 2) 防旧 refresh 重放：auth:rt:<userId> 必须等于当前 rtid
        String current = redisCache.getCacheString(keyUserRt(userId));
        if (current == null || !current.equals(rtid)) {
            throw new BusinessException(CommonErrorEnum.UNAUTHORIZED);
        }

        return userId;
    }

    @Override
    public String onRefreshSuccess(Long userId, HttpServletRequest request, HttpServletResponse response) {
        // refresh 成功后，你可以选择：
        // A) 滑动续期（默认 true）
        // B) 轮换 refresh（rotation，加分项，默认 false）
        // C) 什么也不做（固定7天）

        String oldRtid = readCookie(request, cookieName).orElse(null);
        if (oldRtid == null) {
            throw new BusinessException(CommonErrorEnum.UNAUTHORIZED);
        }

        if (rotation) {
            // 轮换：旧 rtid 立即作废，生成新 rtid

            revokeByRtid(oldRtid, userId);
            return generateRefreshToken(userId, response);
        }

        if (sliding) {
            // 续期：重置两个 key 的 TTL
            redisCache.expireString(keyRtid(oldRtid), refreshExpireSeconds);
            redisCache.expireString(keyUserRt(userId), refreshExpireSeconds);
        }

        return oldRtid;
    }

    @Override
    public void revokeRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String rtid = readCookie(request, cookieName).orElse(null);
        if (rtid != null) {
            // 尽力删除
            String userIdStr = redisCache.getCacheString(keyRtid(rtid));
            if (userIdStr != null) {
                try {
                    Long userId = Long.valueOf(userIdStr);
                    revokeByRtid(rtid, userId);
                } catch (NumberFormatException ignored) {
                    redisCache.deleteString(keyRtid(rtid));
                }
            } else {
                redisCache.deleteString(keyRtid(rtid));
            }
        }
        // 清 Cookie
        clearRefreshCookie(response);
    }

    // ---------------- Internal helpers ----------------

    private void revokeByRtid(String rtid, Long userId) {
        // 删除 rtid 映射
        redisCache.deleteString(keyRtid(rtid));
        // 删除 user 当前 rtid（只在匹配时删，避免误删）
        String current = redisCache.getCacheString(keyUserRt(userId));
        if (rtid.equals(current)) {
            redisCache.deleteString(keyUserRt(userId));
        }
    }

    /** 生成高强度 rtid（Base64URL，无'='，长度约 43） */
    private String generateRtid() {
        byte[] bytes = new byte[32]; // 256bit
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return Optional.ofNullable(c.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * 写入 Refresh Token Cookie
     * @param response HttpServletResponse 响应对象
     */
    private void writeRefreshCookie(HttpServletResponse response, String rtid) {
        // 使用 Set-Cookie 头，写 HttpOnly
        Cookie cookie = new Cookie("refresh_token", rtid);
        cookie.setHttpOnly(true);  // 防止 JS 访问
        cookie.setSecure(false);    // 如果使用 HTTPS，建议设置为 true
        cookie.setPath("/");       // 使其对整个站点有效
        response.addCookie(cookie);
    }

    /**
     * 清除 Refresh Token Cookie
     * @param response HttpServletResponse 响应对象
     */
    private void clearRefreshCookie(HttpServletResponse response) {
        // 创建同名Cookie（必须与写入时的name一致）
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);  // 与写入时保持一致，防止JS访问
         cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);       // 设置过期时间为0，触发浏览器删除Cookie
        response.addCookie(cookie);
    }
}
