package com.thanhpro0703.SamNgocLinhPJ.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RequestUtils {

    private RequestUtils() { // Private constructor để ngăn khởi tạo
    }

    /**
     * Lấy IP của client một cách an toàn từ request.
     * Ưu tiên header X-Forwarded-For.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // Lấy địa chỉ IP đầu tiên nếu có nhiều proxy
            return xForwardedForHeader.split(",")[0].trim();
        }
        // Nếu không có X-Forwarded-For, lấy từ remote address
        String remoteAddr = request.getRemoteAddr();
        // Xử lý trường hợp IPv6 loopback
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }

    /**
     * Trích xuất token từ header Authorization.
     * Hỗ trợ cả "Bearer [token]" và chỉ "[token]".
     */
    public static String extractTokenFromHeader(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else if (bearerToken != null && !bearerToken.trim().isEmpty()) {
            // Cho phép gửi token trực tiếp không có "Bearer "
            log.debug("Authorization header does not start with Bearer, using token directly.");
            return bearerToken.trim();
        }
        return null;
    }
} 