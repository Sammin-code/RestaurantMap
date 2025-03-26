package com.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Class Name: JwtTokenProvider
 * Package: com.blog.config
 * Description:
 * author:
 * Create: 2025/3/14
 * Version: 1.0
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long JWT_EXPIRATION = 3600000; // 1 hour

    public JwtTokenProvider() {
        // 使用 Keys.secretKeyFor 生成安全的密钥
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    // 生成 JWT
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(key)
                .compact();
    }

    // 從 JWT 獲取用戶名稱
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 驗證 JWT 是否有效
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
