package com.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import com.blog.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
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
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key cannot be null or empty");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT Token Provider initialized with expiration: {} ms", jwtExpiration);
    }

    // 生成 JWT
    public String generateToken(UserDetails userDetails) {
        logger.info("Generating token for user: {}", userDetails.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("USER");
        claims.put("role", role);
        logger.info("User role: {}", role);

        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("userId", user.getId());
            logger.info("User ID: {}", user.getId());
        }

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();

        logger.info("Generated token: {}", token);
        return token;
    }

    // 從 JWT 獲取用戶名稱
    public String getUsernameFromToken(String token) {
        try {
            logger.info("Getting username from token");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            logger.info("Token claims - username: {}, role: {}", username, role);

            return username;
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting username from token: {}", e.getMessage());
            throw e;
        }
    }

    // 驗證 JWT 是否有效
    public boolean validateToken(String token) {
        try {
            logger.info("Validating token");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            logger.info("Token validation successful - username: {}, role: {}", username, role);

            return true;
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // 從 token 獲取角色
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);
            logger.info("Role from token: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("Error getting role from token: {}", e.getMessage());
            return null;
        }
    }
}
