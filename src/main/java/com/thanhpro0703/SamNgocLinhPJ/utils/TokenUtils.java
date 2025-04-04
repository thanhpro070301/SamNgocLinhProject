package com.thanhpro0703.SamNgocLinhPJ.utils;

/**
 * Tiện ích xử lý token
 */
public class TokenUtils {
    
    /**
     * Loại bỏ tiền tố "Bearer " khỏi token nếu có
     * @param bearerToken Token có thể có tiền tố "Bearer "
     * @return Token đã được xử lý
     */
    public static String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
} 