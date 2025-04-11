package com.thanhpro0703.SamNgocLinhPJ.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    // Lưu trữ bucket cho mỗi IP, sử dụng ConcurrentHashMap an toàn cho luồng
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // ObjectMapper để serialize JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Số lượng token cho mỗi bucket
    private static final int BUCKET_CAPACITY = 20;
    
    // Tốc độ nạp lại token (mỗi phút)
    private static final int REFILL_TOKENS = 20;
    
    // Thời gian nạp lại token
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    
    /**
     * Tạo mới hoặc lấy bucket cho một địa chỉ IP
     */
    private Bucket getBucketForIp(String ip) {
        return buckets.computeIfAbsent(ip, key -> {
            // Tạo giới hạn băng thông
            Bandwidth limit = Bandwidth.classic(BUCKET_CAPACITY, 
                    Refill.intervally(REFILL_TOKENS, REFILL_DURATION));
            
            // Tạo bucket với giới hạn đó
            return Bucket.builder().addLimit(limit).build();
        });
    }
    
    /**
     * Lấy IP thực từ request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Lấy địa chỉ IP của client
        String clientIp = getClientIp(request);
        
        // Lấy bucket tương ứng với IP
        Bucket tokenBucket = getBucketForIp(clientIp);
        
        // Kiểm tra có thể lấy một token không
        if (tokenBucket.tryConsume(1)) {
            // Cho phép request đi tiếp
            filterChain.doFilter(request, response);
        } else {
            // Từ chối request do vượt quá giới hạn
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            Map<String, Object> errorDetails = Map.of(
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", "Quá nhiều yêu cầu, vui lòng thử lại sau."
            );
            
            objectMapper.writeValue(response.getWriter(), errorDetails);
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Không áp dụng rate limit cho tài nguyên tĩnh và swagger
        return path.startsWith("/static/") || 
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/v3/api-docs");
    }
} 