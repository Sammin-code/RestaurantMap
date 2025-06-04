package com.blog.config;

import com.blog.security.JwtAuthenticationFilter;
import com.blog.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

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

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Security Filter Chain");

        http
                .csrf(csrf -> {
                    logger.info("Disabling CSRF");
                    csrf.disable();
                })
                .cors(cors -> {
                    logger.info("Configuring CORS with custom configuration");
                    cors.configurationSource(corsConfigurationSource());
                })
                .sessionManagement(session -> {
                    logger.info("Setting session management to STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(auth -> {
                    logger.info("Configuring authorization rules");
                    auth
                            // 允許所有 OPTIONS 請求
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            // 公開端點
                            .requestMatchers("/api/auth/**", "/api/users/login", "/api/users/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/reviews/restaurant/**").permitAll()
                            .requestMatchers("/uploads/**", "/api/uploads/**").permitAll()
                            .requestMatchers("/api/debug/**").permitAll()
                            .requestMatchers("/api/images/**").permitAll()

                            // 添加新的需要 REVIEWER 角色的端點
                            .requestMatchers(HttpMethod.GET, "/api/users/{userId}").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.GET, "/api/users/{userId}/favorites").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.GET, "/api/users/{userId}/reviews").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.GET, "/api/users/{userId}/restaurants").hasRole("REVIEWER")

                            // 評論相關的端點
                            .requestMatchers(HttpMethod.POST, "/api/reviews/restaurant/{restaurantId}")
                            .hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.PUT, "/api/reviews/{reviewId}").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.POST, "/api/reviews/{reviewId}/like").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.DELETE, "/api/reviews/{reviewId}/like").hasRole("REVIEWER")

                            // 需要 REVIEWER 或 ADMIN 的端點
                            .requestMatchers(HttpMethod.DELETE, "/api/reviews/{reviewId}")
                            .hasAnyRole("REVIEWER", "ADMIN")
                            // 需要認證的端點
                            .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.PUT, "/api/restaurants/{id}").hasRole("REVIEWER")
                            .requestMatchers(HttpMethod.DELETE, "/api/restaurants/{id}").hasAnyRole("REVIEWER", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/restaurants/*/favorite").hasAnyRole("REVIEWER")
                            .requestMatchers(HttpMethod.DELETE, "/api/restaurants/*/favorite").hasAnyRole("REVIEWER")
                            .requestMatchers(HttpMethod.GET, "/api/restaurants/favorites").hasAnyRole("REVIEWER")

                            // 其他需要認證的請求
                            .anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 添加異常處理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.error("Authentication error: {} - Request URI: {}, Method: {}",
                                    authException.getMessage(),
                                    request.getRequestURI(),
                                    request.getMethod());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"請先登入\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.error("Access denied: {} - Request URI: {}, Method: {}",
                                    accessDeniedException.getMessage(),
                                    request.getRequestURI(),
                                    request.getMethod());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"權限不足\"}");
                        }));

        logger.info("Security Filter Chain configured successfully");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://restaurantmap-255668913932.asia-east1.run.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        logger.info("CORS Configuration: Allowed Origins: {}, Allowed Methods: {}, Allowed Headers: {}, Max Age: {}",
                configuration.getAllowedOrigins(),
                configuration.getAllowedMethods(),
                configuration.getAllowedHeaders(),
                configuration.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        logger.info("CORS configured successfully");
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        logger.info("Configuring authentication provider");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        logger.info("Authentication provider configured successfully");
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        logger.info("Configuring authentication manager");
        return authenticationConfiguration.getAuthenticationManager();
    }
}