package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserSessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;

    /**
     * Đăng ký người dùng mới
     */
    @Transactional
    public UserEntity registerUser(String name, String email, String password, String phone) {
        try {
            if (userRepository.existsByEmail(email)) {
                log.warn("Email '{}' đã được sử dụng!", email);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại!");
            }

            // Kiểm tra xem email đã xác thực chưa
            if (!otpService.isVerified(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email chưa được xác thực!");
            }

            // Lưu thông tin người dùng sau khi xác thực OTP
            UserEntity newUser = UserEntity.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .phone(phone)
                    .role(UserEntity.Role.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            UserEntity savedUser = userRepository.save(newUser);
            log.info("Người dùng '{}' đã đăng ký thành công.", email);
            return savedUser;
        } catch (ResponseStatusException e) {
            // Ném lại các lỗi cụ thể để controller xử lý
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không xác định khi đăng ký người dùng '{}': {}", email, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại sau.");
        }
    }

    /**
     * Xác thực đăng nhập và tạo session
     */
    @Transactional
    public String loginUser(String email, String password, HttpServletRequest request, boolean rememberMe) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy email '{}' ", email);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy email!");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Mật khẩu không đúng cho email '{}'", email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu không chính xác!");
        }

        // Tạo phiên đăng nhập mới và lấy token
        UserSessionEntity session = userSessionService.createSession(user, request, rememberMe);
        String token = session.getTokenId();

        log.info("Email '{}' đã đăng nhập, token được tạo.", email);
        return token;
    }

    /**
     * Xác thực đăng nhập không có request (dùng cho API)
     */
    @Transactional
    public String loginUser(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy email '{}' ", email);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy email!");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Mật khẩu không đúng cho email '{}'", email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu không chính xác!");
        }

        // Tạo phiên đăng nhập mới (API version)
        UserSessionEntity session = userSessionService.createSession(user, false);
        String token = session.getTokenId();
        
        // Log chi tiết thông tin người dùng và token
        log.info("=================================================");
        log.info("ĐĂNG NHẬP THÀNH CÔNG - THÔNG TIN CHI TIẾT:");
        log.info("ID: {}", user.getId());
        log.info("Email: {}", user.getEmail());
        log.info("Tên: {}", user.getName());
        log.info("Role: {}", user.getRole());
        log.info("Token: {}", token);
        log.info("Token hết hạn: {}", session.getExpiresAt());
        log.info("=================================================");
        
        return token;
    }

    /**
     * Lấy thông tin người dùng từ token
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> getUserByToken(String token) {
        return userSessionService.getSessionByToken(token)
                .map(UserSessionEntity::getUser);
    }

    /**
     * Đăng xuất: Xóa session
     */
    @Transactional
    public void logoutUser(String token) {
        try {
            userSessionService.deleteSession(token);
            log.info("Đã đăng xuất, token bị xóa: {}", token);
        } catch (ResponseStatusException e) {
            // Xử lý trường hợp token không tồn tại (có thể đã hết hạn hoặc đã bị xóa)
            log.warn("Không thể đăng xuất - Token không tồn tại hoặc đã hết hạn: {}", token);
        }
    }

    /**
     * Đăng xuất khỏi tất cả thiết bị
     */
    @Transactional
    public void logoutAllDevices(Long userId) {
        userSessionService.deleteAllUserSessions(userId);
        log.info("Đã đăng xuất khỏi tất cả thiết bị cho người dùng ID: {}", userId);
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    @Transactional(readOnly = true)
    public boolean isValidToken(String token) {
        return userSessionService.isValidSession(token);
    }
}
