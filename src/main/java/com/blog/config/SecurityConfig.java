package com.blog.config;

import com.blog.security.JwtAuthenticationFilter;
import com.blog.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Class Name: SecurityConfig
 * Package: com.blog.config
 * Description:
 * author:
 * Create: 2025/3/10
 * Version: 1.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    // 通過構造函數注入 JwtAuthenticationFilter 和 CustomUserDetailsService
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // 配置 Spring Security 的過濾器鏈
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 關閉 CSRF（適用於 REST API）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 禁用
                                                                                                              // Session，使用
                                                                                                              // JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/users/login", "/api/users/register").permitAll() // 登入、註冊
                                                                                                                // API
                                                                                                                // 允許訪問
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/**", "/api/tags/**").permitAll() // 餐廳列表和標籤可公開訪問
                        .requestMatchers(HttpMethod.POST, "/api/restaurants/**").hasRole("ADMIN") // 只有管理員可以新增餐廳
                        .requestMatchers("/api/restaurants/*/favorite").authenticated() // 收藏功能需要登入
                        .anyRequest().authenticated() // 其他請求需驗證
                )
                .authenticationProvider(authenticationProvider()) // 使用手動創建的 AuthenticationProvider
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 將 JWT
                                                                                                       // 過濾器放在合適的位置

        return http.build();
    }

    // 手動創建 AuthenticationProvider Bean
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService); // 注入自定義 UserDetailsService
        provider.setPasswordEncoder(passwordEncoder); // 使用密碼編碼器
        return provider;
    }

    // 配置 AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}