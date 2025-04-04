package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserSessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.UserService;
import com.thanhpro0703.SamNgocLinhPJ.service.UserSessionService;
import com.thanhpro0703.SamNgocLinhPJ.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account/sessions")
@RequiredArgsConstructor
public class UserSessionController {
    
    private final UserSessionService userSessionService;
    private final AuthService authService;
    private final UserService userService;
    
    /**
     * Lấy danh sách phiên đăng nhập của người dùng hiện tại
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCurrentUserSessions(
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        
        UserEntity currentUser = authService.getUserByToken(token)
                .orElseThrow(() -> new RuntimeException("Phiên đăng nhập không hợp lệ"));
        
        List<UserSessionEntity> sessions = userSessionService.getUserSessions(currentUser.getId());
        
        List<Map<String, Object>> sessionDTOs = sessions.stream()
                .map(session -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", session.getId());
                    dto.put("deviceType", session.getDeviceType());
                    dto.put("platform", session.getPlatform());
                    dto.put("browser", session.getBrowser());
                    dto.put("ipAddress", session.getIpAddress());
                    dto.put("lastActivity", session.getLastActivity());
                    dto.put("isCurrentSession", session.getTokenId().equals(token));
                    dto.put("isRememberMe", session.getIsRememberMe());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(sessionDTOs);
    }
    
    /**
     * Đăng xuất khỏi một phiên đăng nhập cụ thể
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> logoutFromSession(
            @PathVariable Integer sessionId,
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        
        UserEntity currentUser = authService.getUserByToken(token)
                .orElseThrow(() -> new RuntimeException("Phiên đăng nhập không hợp lệ"));
        
        UserSessionEntity session = userSessionService.getSessionById(sessionId);
        
        // Kiểm tra xem phiên này có thuộc về người dùng hiện tại không
        if (!session.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        // Đăng xuất khỏi phiên này
        userSessionService.deleteSessionById(sessionId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Đăng xuất khỏi tất cả các phiên đăng nhập trừ phiên hiện tại
     */
    @DeleteMapping("/other-devices")
    public ResponseEntity<Void> logoutFromOtherDevices(
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        
        UserEntity currentUser = authService.getUserByToken(token)
                .orElseThrow(() -> new RuntimeException("Phiên đăng nhập không hợp lệ"));
        
        userSessionService.deleteOtherSessions(currentUser.getId(), token);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Đăng xuất khỏi tất cả các thiết bị (bao gồm cả thiết bị hiện tại)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> logoutFromAllDevices(
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        
        UserEntity currentUser = authService.getUserByToken(token)
                .orElseThrow(() -> new RuntimeException("Phiên đăng nhập không hợp lệ"));
        
        authService.logoutAllDevices(currentUser.getId());
        
        return ResponseEntity.noContent().build();
    }
} 