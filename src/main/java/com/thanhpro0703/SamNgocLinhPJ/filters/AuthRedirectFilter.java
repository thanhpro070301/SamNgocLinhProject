package com.thanhpro0703.SamNgocLinhPJ.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter để chuyển hướng người dùng chưa đăng nhập sang trang đăng nhập khi họ truy cập vào trang checkout
 */
@Component
@Order(1)
public class AuthRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        String requestURI = request.getRequestURI();
        String authorization = request.getHeader("Authorization");
        
        // Kiểm tra xem người dùng có đang cố gắng truy cập API đặt hàng mà không có token không
        if (requestURI.startsWith("/api/orders") 
                && !requestURI.startsWith("/api/orders/public") 
                && (authorization == null || !authorization.startsWith("Bearer "))) {
            
            if (request.getHeader("Accept") != null && 
                    request.getHeader("Accept").contains("application/json")) {
                // Nếu là API request, trả về 401 Unauthorized
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Bạn cần đăng nhập để thực hiện thanh toán\",\"redirectUrl\":\"/login\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
} 