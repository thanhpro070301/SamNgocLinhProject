package com.thanhpro0703.SamNgocLinhPJ.configs;
import com.thanhpro0703.SamNgocLinhPJ.security.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

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
                    "/api/test/**"           // Cho phép sử dụng API test không cần đăng nhập
                );
    }
}