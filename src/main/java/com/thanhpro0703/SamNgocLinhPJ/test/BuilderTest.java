package com.thanhpro0703.SamNgocLinhPJ.test;

import com.thanhpro0703.SamNgocLinhPJ.entity.*;
import com.thanhpro0703.SamNgocLinhPJ.dto.*;

public class BuilderTest {
    
    public static void main(String[] args) {
        // Test ProductEntity builder
        ProductEntity product = ProductEntity.builder()
                .id(1)
                .name("Test Product")
                .slug("test-product")
                .description("Test description")
                .build();
        
        System.out.println("Product created: " + product.getName());
        
        // Test CategoryEntity builder
        CategoryEntity category = CategoryEntity.builder()
                .id(1)
                .name("Test Category")
                .slug("test-category")
                .build();
        
        System.out.println("Category created: " + category.getName());
        
        // Test UserEntity builder
        UserEntity user = UserEntity.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();
        
        System.out.println("User created: " + user.getName());
        
        // Test ProductDTO builder
        ProductDTO productDTO = ProductDTO.builder()
                .id(1)
                .name("Test Product DTO")
                .build();
                
        System.out.println("ProductDTO created: " + productDTO.getName());
        
        // Test UserDTO builder
        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .name("Test User DTO")
                .email("test@example.com")
                .build();
                
        System.out.println("UserDTO created: " + userDTO.getName());
        
        // Test CategoryDTO builder
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .id(1)
                .name("Test Category DTO")
                .build();
                
        System.out.println("CategoryDTO created: " + categoryDTO.getName());
        
        System.out.println("All builders working correctly!");
    }
} 