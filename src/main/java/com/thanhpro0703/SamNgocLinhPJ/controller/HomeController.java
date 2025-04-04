package com.thanhpro0703.SamNgocLinhPJ.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Sâm Ngọc Linh API");
        response.put("version", "1.0.0");
        response.put("description", "RESTful API for Sâm Ngọc Linh E-commerce website");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("products", "/api/products");
        endpoints.put("product_details", "/api/products/{id}");
        endpoints.put("product_by_slug", "/api/products/slug/{slug}");
        endpoints.put("best_selling_products", "/api/products/best-selling");
        endpoints.put("new_arrivals", "/api/products/new-arrivals");
        endpoints.put("search_products", "/api/products/search?keyword={keyword}");
        
        endpoints.put("categories", "/api/categories");
        endpoints.put("parent_categories", "/api/categories/parent");
        endpoints.put("category_details", "/api/categories/{id}");
        endpoints.put("category_by_slug", "/api/categories/slug/{slug}");
        endpoints.put("products_by_category", "/api/categories/{id}/products");
        
        endpoints.put("news", "/api/news");
        endpoints.put("news_details", "/api/news/{id}");
        endpoints.put("news_by_slug", "/api/news/slug/{slug}");
        endpoints.put("news_by_category", "/api/news/category/{category}");
        endpoints.put("latest_news", "/api/news/latest");
        
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }
} 