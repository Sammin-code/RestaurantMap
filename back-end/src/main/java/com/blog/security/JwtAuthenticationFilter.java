package com.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class Name: JwtAuthenticationFilter
 * Package: com.blog.config
 * Description:
 * author:
 * Create: 2025/3/14
 * Version: 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // 定義不需要認證的端點
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/",
            "/api/users/login",
            "/api/users/register",
            "/api/restaurants/popular",
            "/api/restaurants/latest",
            "/api/reviews/restaurant",
            "/api/restaurants", // 添加公開的餐廳列表
            "/api/restaurants/{id}", // 添加公開的單個餐廳
            "/api/reviews/restaurant/{restaurantId}", // 添加公開的餐廳評論
            "/api/images/" // 添加公開的圖片 API
    );

    // 定義需要認證的端點
    private static final List<String> PROTECTED_PATHS = Arrays.asList(
            "/api/restaurants/favorites",
            "/api/restaurants/\\d+/favorite",
            "/api/users/\\d+", // 添加用戶資料
            "/api/users/\\d+/favorites", // 添加用戶收藏
            "/api/users/\\d+/reviews", // 添加用戶評論
            "/api/users/\\d+/restaurants", // 添加用戶創建的餐廳
            "/api/reviews/restaurant/\\d+", // 添加評論相關
            "/api/reviews/\\d+/like" // 添加按讚相關
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String method = request.getMethod();

        // 直接放行圖片 API（同時判斷 getRequestURI 與 getServletPath）
        if (path.startsWith("/api/images/") || (servletPath != null && servletPath.startsWith("/api/images/"))) {
            return true;
        }

        // 登入和註冊請求不需要認證
        if (path.equals("/api/users/login") || path.equals("/api/users/register")) {
            return true;
        }

        // 首先檢查是否是受保護的端點
        boolean isProtectedPath = PROTECTED_PATHS.stream()
                .anyMatch(pattern -> path.matches(pattern));
        if (isProtectedPath) {
            return false;
        }

        // 檢查是否是公開的 GET 請求
        if (method.equals(HttpMethod.GET.name())) { // 首先判斷是否為 GET 請求
            // 檢查是否是公開的餐廳列表或評論
            if (path.equals("/api/restaurants") || // 餐廳列表
                    (path.startsWith("/api/restaurants/") && // 餐廳相關路徑
                            !path.equals("/api/restaurants/favorites") && // 排除收藏列表
                            !path.matches("/api/restaurants/\\d+"))
                    || // 排除單個餐廳詳情
                    path.startsWith("/api/reviews/restaurant/") || // 餐廳評論
                    PUBLIC_PATHS.stream().anyMatch(path::startsWith)) { // 其他公開路徑
                return true; // 如果是公開路徑，跳過認證
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        // 圖片 API 直接放行，不做任何認證
        if (path.startsWith("/api/images/") || (servletPath != null && servletPath.startsWith("/api/images/"))) {
            filterChain.doFilter(request, response);
            return;
        }
        String method = request.getMethod();

        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Invalid token\", \"message\": \"請重新登入\"}");
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"No token\", \"message\": \"請先登入\"}");
                return;
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\", \"message\": \"請重新登入\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresAuthentication(String path, String method) {
        // 收藏相關的端點需要認證
        if (path.matches("/api/restaurants/\\d+/favorite") &&
                (method.equals(HttpMethod.POST.name()) || method.equals(HttpMethod.DELETE.name()))) {
            return true;
        }

        // 其他需要認證的端點
        return !PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private void handleAuthenticationError(HttpServletResponse response, String error, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\": \"%s\", \"message\": \"%s\"}",
                error,
                message));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return token;
        }
        return null;
    }
}
