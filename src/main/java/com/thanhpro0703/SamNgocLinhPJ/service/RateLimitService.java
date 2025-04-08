package com.thanhpro0703.SamNgocLinhPJ.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
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
} 