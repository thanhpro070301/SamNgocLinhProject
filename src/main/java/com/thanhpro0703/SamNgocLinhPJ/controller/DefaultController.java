package com.thanhpro0703.SamNgocLinhPJ.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DefaultController {

    @GetMapping("/")
    public String home() {
        return "Backend is running!";
    }
    
    @GetMapping("/api/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");
        return response;
    }
    
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is healthy");
        return response;
    }
    
    @GetMapping("/api/api/health")
    public Map<String, Object> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "API is healthy");
        return response;
    }
}
