package com.thanhpro0703.SamNgocLinhPJ.utils;

import lombok.Builder;
import lombok.Data;

public class TestLombok {
    
    public static void main(String[] args) {
        TestClass testClass = TestClass.builder()
                .id(1)
                .name("Test")
                .build();
                
        System.out.println("Created test class: " + testClass);
    }
    
    @Data
    @Builder
    public static class TestClass {
        private int id;
        private String name;
    }
} 