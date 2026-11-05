package com.example.dzl.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RefreshTokenService {
    /** 登录成功后：签发 refresh（写Redis + Set-Cookie） */
    String generateRefreshToken(Long userId, HttpServletResponse response);

    /**
     * Refresh：校验 refresh（从Cookie取rtid -> 查Redis校验）
     * 成功返回 userId；失败抛业务异常（UNAUTHORIZED）
     */
    Long verifyRefreshToken(HttpServletRequest request);

    /**
     * 可选：refresh 成功后续期（滑动过期）
     * 或者做轮换（rotation）
     */
    String onRefreshSuccess(Long userId, HttpServletRequest request, HttpServletResponse response);

    /** 登出：删除Redis并清Cookie */
    void revokeRefreshToken(HttpServletRequest request, HttpServletResponse response);
}
