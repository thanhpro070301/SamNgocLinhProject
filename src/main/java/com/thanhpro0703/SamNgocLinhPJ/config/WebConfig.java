package com.thanhpro0703.SamNgocLinhPJ.config;

import com.thanhpro0703.SamNgocLinhPJ.security.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    // Disable this CORS config since we're using CorsFilter
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOrigins(
    //                 "http://localhost:5173", 
    //                 "http://localhost:8080",
    //                 "http://localhost:3000",
    //                 "https://regal-piroshki-919004.netlify.app",
    //                 "https://samngoclinhproject.onrender.com"
    //             )
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true)
    //             .maxAge(3600);
    // }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")  // Áp dụng interceptor cho tất cả API
                .excludePathPatterns(
                    "/api/auth/**",          // Không chặn API đăng nhập/đăng ký
                    "/api/products/**",      // Cho phép xem sản phẩm không cần đăng nhập
                    "/api/categories/**",    // Cho phép xem danh mục không cần đăng nhập
                    "/api/news/**",          // Cho phép xem tin tức không cần đăng nhập
                    "/api/users/**",         // Cho phép xem người dùng không cần đăng nhập
                    "/api/orders/public/**", // Cho phép xem đơn hàng công khai không cần đăng nhập
                    "/api/test/**",          // Cho phép sử dụng API test không cần đăng nhập
                    "/api/status",           // Cho phép truy cập API status không cần đăng nhập
                    "/api/health",           // Cho phép truy cập API health không cần đăng nhập
                    "/api/api/health"        // Cho phép truy cập API api/health không cần đăng nhập
                );
    }
} 