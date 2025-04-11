package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserSessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.exception.ResourceNotFoundException;
import com.thanhpro0703.SamNgocLinhPJ.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {
    
    private final UserSessionRepository userSessionRepository;
    
    /**
     * Tạo mới phiên đăng nhập
     */
    @Transactional
    public UserSessionEntity createSession(UserEntity user, HttpServletRequest request, boolean isRememberMe) {
        String tokenId = UUID.randomUUID().toString();
        
        LocalDateTime expiresAt = isRememberMe 
                ? LocalDateTime.now().plusDays(30) // Phiên lưu 30 ngày nếu "remember me"
                : LocalDateTime.now().plusHours(2); // Phiên mặc định 2 giờ
        
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        
        UserSessionEntity session = UserSessionEntity.builder()
                .user(user)
                .tokenId(tokenId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceType(parseDeviceType(userAgent))
                .platform(parsePlatform(userAgent))
                .browser(parseBrowser(userAgent))
                .lastActivity(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isRememberMe(isRememberMe)
                .build();
        
        return userSessionRepository.save(session);
    }
    
    /**
     * Tạo mới phiên đăng nhập (không cần HttpServletRequest, dùng cho API)
     */
    @Transactional
    public UserSessionEntity createSession(UserEntity user, boolean isRememberMe) {
        String tokenId = UUID.randomUUID().toString();
        
        LocalDateTime expiresAt = isRememberMe 
                ? LocalDateTime.now().plusDays(30) // Phiên lưu 30 ngày nếu "remember me"
                : LocalDateTime.now().plusHours(2); // Phiên mặc định 2 giờ
        
        UserSessionEntity session = UserSessionEntity.builder()
                .user(user)
                .tokenId(tokenId)
                .deviceType("API")
                .platform("Unknown")
                .browser("Unknown")
                .lastActivity(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isRememberMe(isRememberMe)
                .build();
        
        return userSessionRepository.save(session);
    }
    
    /**
     * Lấy thông tin phiên đăng nhập từ token
     */
    @Transactional(readOnly = true)
    public Optional<UserSessionEntity> getSessionByToken(String tokenId) {
        return userSessionRepository.findByTokenId(tokenId);
    }
    
    /**
     * Kiểm tra token có hợp lệ không
     */
    @Transactional(readOnly = true)
    public boolean isValidSession(String tokenId) {
        Optional<UserSessionEntity> sessionOpt = getSessionByToken(tokenId);
        
        return sessionOpt.map(session -> !session.getExpiresAt().isBefore(LocalDateTime.now()))
                       .orElse(false);
    }
    
    /**
     * Cập nhật thời gian hoạt động cuối cùng
     */
    @Transactional
    public void updateLastActivity(String tokenId) {
        userSessionRepository.updateLastActivity(tokenId, LocalDateTime.now());
    }
    
    /**
     * Xóa phiên đăng nhập (đăng xuất)
     */
    @Transactional
    public void deleteSession(String tokenId) {
        userSessionRepository.findByTokenId(tokenId).ifPresent(session -> {
            userSessionRepository.delete(session);
            log.info("Đã xóa phiên đăng nhập với token: {}", tokenId);
        });
    }
    
    /**
     * Xóa tất cả phiên đăng nhập của người dùng (đăng xuất khỏi tất cả thiết bị)
     */
    @Transactional
    public void deleteAllUserSessions(Long userId) {
        List<UserSessionEntity> sessions = userSessionRepository.findRecentSessionsByUserId(userId);
        userSessionRepository.deleteAll(sessions);
    }
    
    /**
     * Xóa tất cả phiên đăng nhập của người dùng trừ phiên hiện tại
     */
    @Transactional
    public void deleteOtherSessions(Long userId, String currentTokenId) {
        userSessionRepository.deleteOtherSessionsForUser(userId, currentTokenId);
    }
    
    /**
     * Job định kỳ chạy mỗi giờ để xóa các phiên hết hạn
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // Chạy mỗi giờ
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("Đang xóa các phiên đăng nhập hết hạn");
        userSessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }
    
    /**
     * Phân tích thông tin về trình duyệt, thiết bị từ User-Agent
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
    
    private String parsePlatform(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("macintosh") || userAgent.contains("mac os x")) {
            return "MacOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }
    
    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("chrome") && !userAgent.contains("edg")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("edg")) {
            return "Edge";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else {
            return "Unknown";
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Nếu IP là loopback hoặc IPv6, thì trả về
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        
        // Nếu có nhiều proxy, lấy địa chỉ IP đầu tiên
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
    
    /**
     * Lưu phiên đăng nhập
     */
    @Transactional
    public UserSessionEntity saveSession(UserSessionEntity session) {
        return userSessionRepository.save(session);
    }
    
    /**
     * Lấy danh sách phiên đăng nhập của người dùng
     */
    @Transactional(readOnly = true)
    public List<UserSessionEntity> getUserSessions(Long userId) {
        return userSessionRepository.findRecentSessionsByUserId(userId);
    }
    
    /**
     * Lấy thông tin phiên đăng nhập theo ID
     */
    @Transactional(readOnly = true)
    public UserSessionEntity getSessionById(Integer sessionId) {
        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đăng nhập với ID: " + sessionId));
    }
    
    /**
     * Xóa phiên đăng nhập theo ID
     */
    @Transactional
    public void deleteSessionById(Integer sessionId) {
        UserSessionEntity session = getSessionById(sessionId);
        userSessionRepository.delete(session);
    }
} 