package com.thanhpro0703.SamNgocLinhPJ.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.thanhpro0703.SamNgocLinhPJ.exception.RateLimitExceededException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5;
    private static final Duration DURATION = Duration.ofMinutes(1);

    public boolean isRateLimited(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket());
        return !bucket.tryConsume(1);
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS, Refill.intervally(MAX_REQUESTS, DURATION));
        return Bucket4j.builder().addLimit(limit).build();
    }

    public void checkRateLimit(String action, HttpServletRequest request) {
        Bucket bucket = getBucket(action);
        String clientIp = getClientIp(request);
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for action '{}' from IP: {}", action, clientIp);
            throw new RateLimitExceededException("Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.");
        }
        log.trace("Rate limit check passed for action '{}' from IP: {}", action, clientIp);
    }

    private Bucket getBucket(String action) {
        return buckets.computeIfAbsent(action, key -> {
            return switch (key) {
                case "login" -> createLoginBucket();
                case "register" -> createRegisterBucket();
                case "otp" -> createOtpBucket();
                default -> throw new IllegalArgumentException("Unknown rate limit action: " + key);
            };
        });
    }

    private Bucket createLoginBucket() {
        return Bucket4j.builder()
               .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
               .build();
    }

    private Bucket createRegisterBucket() {
        return Bucket4j.builder()
               .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))))
               .build();
    }

    private Bucket createOtpBucket() {
        return Bucket4j.builder()
               .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1))))
               .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
} 