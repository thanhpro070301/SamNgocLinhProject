package com.thanhpro0703.SamNgocLinhPJ.configs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Primary CORS configuration for the application.
 * This filter has a high precedence and will handle all CORS requests,
 * including OPTIONS preflight requests.
 */
@Configuration
public class CorsConfig {
    
    // Định nghĩa danh sách các origin được phép
    private final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
        "http://localhost:5173",
        "http://localhost:8080", 
        "http://localhost:3000",
        "https://regal-piroshki-919004.netlify.app",
        "https://samngoclinhproject.onrender.com"
    );
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1) // Run after PreflightRequestFilter
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Sử dụng danh sách cụ thể thay vì wildcard "*"
        DEFAULT_ALLOWED_ORIGINS.forEach(config::addAllowedOrigin);
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-CSRF-TOKEN", "Accept", "Origin", "X-Requested-With", "x-environment", "Cache-Control"));
        config.setExposedHeaders(List.of("X-CSRF-TOKEN", "Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
