package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();

    // 允許跨域的來源
    config.addAllowedOrigin("http://localhost:8081");

    // 允許跨域的 HTTP 方法
    config.addAllowedMethod("*");

    // 允許跨域的請求頭
    config.addAllowedHeader("*");

    // 允許攜帶認證信息（cookies 等）
    config.setAllowCredentials(true);

    // 設置緩存時間
    config.setMaxAge(3600L);

    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}