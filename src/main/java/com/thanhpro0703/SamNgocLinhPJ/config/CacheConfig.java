package com.thanhpro0703.SamNgocLinhPJ.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cấu hình cache cho ứng dụng sử dụng Caffeine Cache
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Tên các cache
    public static final String PRODUCTS_CACHE = "products";
    public static final String CATEGORIES_CACHE = "categories";
    public static final String NEWS_CACHE = "news";
    public static final String PRODUCT_BY_SLUG_CACHE = "productBySlug";
    public static final String CATEGORY_BY_SLUG_CACHE = "categoryBySlug";
    public static final String PRODUCT_COUNT_CACHE = "productCount";
    
    /**
     * Cache manager chính với cấu hình mặc định
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
                PRODUCTS_CACHE, 
                CATEGORIES_CACHE, 
                NEWS_CACHE, 
                PRODUCT_BY_SLUG_CACHE,
                CATEGORY_BY_SLUG_CACHE,
                PRODUCT_COUNT_CACHE
        ));
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats());
        
        return cacheManager;
    }
    
    /**
     * Cache manager cho dữ liệu ngắn hạn (5 phút)
     */
    @Bean
    public CacheManager shortLivedCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        
        return cacheManager;
    }
    
    /**
     * Cache manager cho dữ liệu dài hạn (1 giờ)
     */
    @Bean
    public CacheManager longLivedCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats());
        
        return cacheManager;
    }
} 