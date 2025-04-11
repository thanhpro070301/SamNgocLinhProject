package com.thanhpro0703.SamNgocLinhPJ.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cấu hình thêm các HTTP Security Headers để tăng cường bảo mật
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                
                // Bảo vệ chống clickjacking
                response.setHeader("X-Frame-Options", "DENY");
                
                // Bảo vệ chống XSS
                response.setHeader("X-XSS-Protection", "1; mode=block");
                
                // Ngăn chặn MIME-sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");
                
                // Kiểm soát Referer policy
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                
                // Content Security Policy mạnh
                response.setHeader("Content-Security-Policy", 
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "img-src 'self' data:; " +
                        "style-src 'self' 'unsafe-inline'; " + 
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'");
                
                // Permissions policy để hạn chế các tính năng trình duyệt
                response.setHeader("Permissions-Policy", 
                        "camera=(), " +
                        "microphone=(), " +
                        "geolocation=()");
                
                // Chỉ định HSTS nếu sử dụng HTTPS
                if (request.isSecure()) {
                    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
} 