package com.thanhpro0703.SamNgocLinhPJ.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Component
@Order(1) // Thực thi trước các filter khác
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // Chỉ ghi log cho API
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin") || path.startsWith("/api/test")) {
            logRequest(request);
        }
        
        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
        
        // Log thông tin xác thực sau khi các filter khác đã xử lý
        if (path.startsWith("/api/admin") || path.startsWith("/api/test")) {
            logAuthenticationStatus();
        }
    }
    
    private void logRequest(HttpServletRequest request) {
        StringBuilder logMessage = new StringBuilder("\n--- REQUEST DETAILS ---\n");
        
        // Thông tin cơ bản
        logMessage.append("URI: ").append(request.getRequestURI()).append("\n");
        logMessage.append("Method: ").append(request.getMethod()).append("\n");
        
        // Ghi log headers
        logMessage.append("Headers:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Che giấu token đầy đủ trong Authorization header
            if ("authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                if (headerValue.startsWith("Bearer ")) {
                    String tokenPrefix = headerValue.substring(0, 10); // "Bearer " + 3 first chars
                    headerValue = tokenPrefix + "..." + headerValue.substring(headerValue.length() - 5);
                }
            }
            
            logMessage.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        }
        
        logger.info(logMessage.toString());
    }
    
    private void logAuthenticationStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            String authorities = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.joining(", "));
            
            logger.info("\n--- AUTHENTICATION RESULT ---\n" +
                    "Authenticated: {}\n" +
                    "Principal: {}\n" +
                    "Name: {}\n" +
                    "Authorities: {}\n",
                    auth.isAuthenticated(),
                    auth.getPrincipal().getClass().getSimpleName(),
                    auth.getName(),
                    authorities);
        } else {
            logger.warn("No authentication found in SecurityContext after filter chain");
        }
    }
} 