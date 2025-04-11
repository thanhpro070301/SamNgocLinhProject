package com.thanhpro0703.SamNgocLinhPJ.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.http.MediaType;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Cấu hình để đảm bảo đường dẫn API được chuyển đến Controller
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Xác định rõ ràng các tài nguyên tĩnh
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // Không xử lý các API như tài nguyên tĩnh
        registry.setOrder(Integer.MAX_VALUE);
    }
} 