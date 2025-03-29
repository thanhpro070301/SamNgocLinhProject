package com.thanhpro0703.SamNgocLinhPJ.configs;
import com.thanhpro0703.SamNgocLinhPJ.security.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

//    private final AuthenticationInterceptor authenticationInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authenticationInterceptor)
//                .addPathPatterns("/api/**")  // Áp dụng interceptor cho tất cả API
//                .excludePathPatterns("/api/auth/**"); // Không chặn API đăng nhập/đăng ký
//    }
}