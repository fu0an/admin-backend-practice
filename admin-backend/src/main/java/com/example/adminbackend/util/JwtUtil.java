package com.example.adminbackend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // =====================长的密钥=====================
    private static final String SECRET_KEY = "mySecretKey123456789abcdefghijklmnopqrstuvwxyzABCDEFG";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 过期时间 7天
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成token
     */
    public static String createToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(KEY, SignatureAlgorithm.HS256) // 安全写法
                .compact();
    }

    /**
     * 解析token获取用户ID
     */
    public static Long getUserIdByToken(String token) {
        if (!StringUtils.hasText(token)) return null;

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验token是否有效
     */
    public static boolean verifyToken(String token) {
        return getUserIdByToken(token) != null;
    }
}