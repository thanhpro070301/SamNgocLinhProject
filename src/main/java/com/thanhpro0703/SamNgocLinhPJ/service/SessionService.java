package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.entity.SessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public String createSession(UserEntity user) {
        String token = UUID.randomUUID().toString();

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
}

