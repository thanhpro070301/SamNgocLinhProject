package com.thanhpro0703.SamNgocLinhPJ.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đây là public endpoint, ai cũng có thể truy cập");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userEndpoint() {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("message", "Đây là user endpoint, yêu cầu đăng nhập");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("message", "Đây là admin endpoint, yêu cầu quyền ADMIN");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> authInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        if (auth != null) {
            response.put("isAuthenticated", auth.isAuthenticated());
            response.put("principal", auth.getPrincipal().toString());
            response.put("name", auth.getName());
            response.put("authorities", auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            response.put("details", auth.getDetails() != null ? auth.getDetails().toString() : null);
        } else {
            response.put("isAuthenticated", false);
            response.put("error", "No authentication found in SecurityContext");
        }
        
        return ResponseEntity.ok(response);
    }
} 