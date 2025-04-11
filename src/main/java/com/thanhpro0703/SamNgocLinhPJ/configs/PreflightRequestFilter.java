package com.thanhpro0703.SamNgocLinhPJ.configs;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter xử lý yêu cầu OPTIONS (CORS preflight) để tránh xung đột với các filter khác
 */
@Component
@Order(0) // Order thấp nhất để chạy đầu tiên, trước cả AuthRedirectFilter
public class PreflightRequestFilter extends OncePerRequestFilter {

    // Danh sách các origin được phép truy cập
    private static final List<String> ALLOWED_ORIGINS = List.of(
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8080",
        "https://regal-piroshki-919004.netlify.app",
        "https://samngoclinhproject.onrender.com"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Trường hợp đặc biệt cho preflight request (OPTIONS)
        if (request.getMethod().equals("OPTIONS")) {
            // Kiểm tra origin từ request
            String origin = request.getHeader("Origin");
            
            // Nếu origin nằm trong danh sách cho phép, thêm vào response header
            if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            } else {
                // Nếu không có origin hoặc không được phép, sử dụng localhost mặc định
                response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            }
            
            // Thiết lập các header CORS khác
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, X-Requested-With, x-environment, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Không chuyển tiếp request
        }
        
        filterChain.doFilter(request, response);
    }
} 