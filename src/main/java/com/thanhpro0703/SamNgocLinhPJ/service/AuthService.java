package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.Role;
import com.thanhpro0703.SamNgocLinhPJ.entity.SessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.SessionRepository;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Đăng ký người dùng mới
     */

    public UserEntity registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Tên người dùng '{}' đã tồn tại!", username);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên người dùng đã tồn tại!");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Email '{}' đã được sử dụng!", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại!");
        }

        UserEntity newUser = UserEntity.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
        log.info("Người dùng '{}' đã đăng ký thành công với vai trò '{}'.", username, newUser.getRole());
        return newUser;
    }


    /**
     * Xác thực đăng nhập và tạo session
     */
    public String loginUser(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy người dùng '{}'", username);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy người dùng!");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Mật khẩu không đúng cho người dùng '{}'", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu không chính xác!");
        }

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        SessionEntity session = SessionEntity.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(2)) // Token hết hạn sau 2 giờ
                .build();

        sessionRepository.save(session);
        log.info("Người dùng '{}' đã đăng nhập, token được tạo.", username);
        return token;
    }

    /**
     * Lấy thông tin người dùng từ token
     */
    public Optional<UserEntity> getUserByToken(String token) {
        return sessionRepository.findByToken(token).map(SessionEntity::getUser);
    }

    /**
     * Đăng xuất: Xóa session
     */
    public void logoutUser(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            sessionRepository.delete(session);
            log.info("Người dùng '{}' đã đăng xuất, token bị xóa.", session.getUser().getUsername());
        });
    }


    public Optional<UserEntity> authenticate(String username, String password) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        } else {
            return Optional.empty();
        }
    }
}
