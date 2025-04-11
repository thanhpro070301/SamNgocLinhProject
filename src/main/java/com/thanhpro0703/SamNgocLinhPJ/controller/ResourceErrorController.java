package com.thanhpro0703.SamNgocLinhPJ.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ResourceErrorController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        
        // Kiểm tra nếu là lỗi liên quan đến API
        if (requestUri != null && (requestUri.startsWith("/api") || requestUri.equals("/api"))) {
            errorDetails.put("error", "API_ERROR");
            errorDetails.put("status", statusCode != null ? statusCode : 500);
            errorDetails.put("path", requestUri);
            
            // Lỗi NoResourceFoundException đặc biệt cho API
            if (exception instanceof NoResourceFoundException) {
                errorDetails.put("message", "API endpoint không tồn tại: " + requestUri);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
            }
            
            // Các lỗi khác
            errorDetails.put("message", exception != null ? exception.getMessage() : "Lỗi không xác định");
            return ResponseEntity.status(statusCode != null ? statusCode : 500).body(errorDetails);
        }
        
        // Các lỗi không phải API
        errorDetails.put("error", "ERROR");
        errorDetails.put("status", statusCode != null ? statusCode : 500);
        errorDetails.put("message", "Lỗi hệ thống");
        return ResponseEntity.status(statusCode != null ? statusCode : 500).body(errorDetails);
    }
} 