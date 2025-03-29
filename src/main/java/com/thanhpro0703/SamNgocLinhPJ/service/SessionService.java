package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.entity.SessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public String createSession(UserEntity user) {
        String token = UUID.randomUUID().toString();
        sessionRepository.deleteByUserId(user.getId());
        // Tạo session mới
        SessionEntity session = SessionEntity.builder()
                .user(user)
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(2)) // Hết hạn sau 2 giờ
                .build();

        sessionRepository.save(session);
        return token;
    }

    public Optional<SessionEntity> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public void deleteSession(Long userId) {
        sessionRepository.deleteByUserId(userId);
    }

    public boolean isSessionValid(String token) {
        Optional<SessionEntity> sessionOpt = sessionRepository.findByToken(token);
        return sessionOpt.isPresent() && sessionOpt.get().getExpiresAt().isAfter(LocalDateTime.now());
    }

    public Optional<UserEntity> getUserFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("❌ Authorization Header không hợp lệ hoặc không tồn tại!");
            return Optional.empty();
        }

        String token = authorizationHeader.substring(7);
        log.info("🔍 Kiểm tra token: {}", token);

        Optional<SessionEntity> sessionOpt = sessionRepository.findByToken(token);

        if (sessionOpt.isEmpty()) {
            log.warn("⚠️ Không tìm thấy token trong DB!");
            return Optional.empty();
        }

        SessionEntity session = sessionOpt.get();
        log.info("📌 Token tìm thấy: ID={}, ExpiresAt={}", session.getId(), session.getExpiresAt());

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("⏳ Token đã hết hạn! Hết hạn lúc: {}", session.getExpiresAt());
            return Optional.empty();
        }

        UserEntity user = session.getUser();
        log.info("✅ Token hợp lệ. Người dùng: {}", user.getUsername());
        return Optional.of(user);
    }





}

