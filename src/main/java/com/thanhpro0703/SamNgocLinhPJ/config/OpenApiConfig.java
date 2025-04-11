package com.thanhpro0703.SamNgocLinhPJ.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("Sâm Ngọc Linh REST API")
                        .description("API cho ứng dụng Sâm Ngọc Linh eCommerce")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Thanh Pro")
                                .email("tback0703@gmail.com")
                                .url("https://samngoclinhproject.onrender.com"))
                        .license(new License()
                                .name("License of API")
                                .url("API license URL")));
    }
} 