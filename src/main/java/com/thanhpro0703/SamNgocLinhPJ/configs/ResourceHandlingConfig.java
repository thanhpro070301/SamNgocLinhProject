package com.thanhpro0703.SamNgocLinhPJ.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class ResourceHandlingConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Xác định các tài nguyên tĩnh và loại trừ các đường dẫn /api
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new ApiPathResourceResolver());
    }

    /**
     * ResourceResolver tùy chỉnh để ngăn xử lý các path bắt đầu bằng "/api" như tài nguyên tĩnh
     */
    private static class ApiPathResourceResolver implements ResourceResolver {
        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath, 
                                       List<? extends Resource> locations, ResourceResolverChain chain) {
            // Kiểm tra chính xác đường dẫn API
            if (requestPath.startsWith("api/") || requestPath.equals("api") || 
                requestPath.startsWith("/api/") || requestPath.equals("/api")) {
                return null;
            }
            
            // Nếu không, chuyển tiếp cho ResourceResolver tiếp theo trong chain
            return chain.resolveResource(request, requestPath, locations);
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, 
                                    ResourceResolverChain chain) {
            // Tương tự như trên nhưng cho việc giải quyết URL path
            if (resourcePath.startsWith("api/") || resourcePath.equals("api") || 
                resourcePath.startsWith("/api/") || resourcePath.equals("/api")) {
                return null;
            }
            
            return chain.resolveUrlPath(resourcePath, locations);
        }
    }
} 