package com.example.dzl;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    /** access 过期时间（秒），例如 7200 */
    @Value("${jwt.access-token.expire}")
    private Long accessExpireSeconds;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("jwt.secret 长度必须至少 32 字符（256bit+），否则不安全/可能报错");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 AccessToken：sub=userId，附带 roleId */
    public String generateAccessToken(Long userId, int roleId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpireSeconds * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))       // sub
                .setId(UUID.randomUUID().toString())      // jti
                .claim("roleId", roleId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验签+校验exp，成功返回Claims；
     * 过期抛 ExpiredJwtException；非法抛 JwtException
     **/
    public Claims verifyAccessToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public Long getRoleId(Claims claims) {
        return claims.get("roleId", Long.class);
    }

    /** 剩余时间（秒），可能为负数 */
    public long remainingSeconds(Claims claims) {
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
    }

}

