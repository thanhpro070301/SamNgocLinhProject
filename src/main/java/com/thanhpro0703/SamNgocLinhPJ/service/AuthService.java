package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserSessionEntity;
import com.thanhpro0703.SamNgocLinhPJ.exception.BadRequestException;
import com.thanhpro0703.SamNgocLinhPJ.exception.ResourceNotFoundException;
import com.thanhpro0703.SamNgocLinhPJ.exception.UnauthorizedException;
import com.thanhpro0703.SamNgocLinhPJ.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
        if (userRepository.existsByEmail(email)) {
            log.warn("Email '{}' đã được sử dụng!", email);
            throw new BadRequestException("Email đã tồn tại!");
        }

        if (!otpService.isVerified(email)) {
            throw new BadRequestException("Email chưa được xác thực!");
        }

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
    }

    /**
     * Xác thực đăng nhập và tạo session
     */
    @Transactional
    public Map<String, Object> loginUser(String email, String password, HttpServletRequest request, boolean rememberMe) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy email '{}' ", email);
                    return new ResourceNotFoundException("Email hoặc mật khẩu không đúng");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Mật khẩu không đúng cho email '{}'", email);
            throw new UnauthorizedException("Email hoặc mật khẩu không đúng");
        }

        UserSessionEntity session = userSessionService.createSession(user, request, rememberMe);
        String token = session.getTokenId();
        String refreshToken = session.getRefreshTokenId();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("refreshToken", refreshToken);
        responseData.put("expiresAt", session.getExpiresAt());
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("role", user.getRole());
        userData.put("phone", user.getPhone());
        responseData.put("user", userData);

        log.info("=================================================");
        log.info("ĐĂNG NHẬP THÀNH CÔNG - THÔNG TIN CHI TIẾT:");
        log.info("ID: {}", user.getId());
        log.info("Email: {}", user.getEmail());
        log.info("Tên: {}", user.getName());
        log.info("Role: {}", user.getRole());
        log.info("Token: {}", token);
        log.info("Token hết hạn: {}", session.getExpiresAt());
        log.info("=================================================");

        return responseData;
    }

    /**
     * Xác thực đăng nhập không có request (dùng cho API)
     */
    @Transactional
    public Map<String, Object> loginUser(String email, String password) {
        return loginUser(email, password, null, false);
    }

    /**
     * Lấy thông tin người dùng từ token
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> getUserByToken(String token) {
        return userSessionService.getSessionByToken(token)
                .map(session -> {
                    UserEntity user = session.getUser();
                    user.getEmail(); 
                    return user;
                });
    }

    /**
     * Đăng xuất: Xóa session
     */
    @Transactional
    public void logoutUser(String token) {
        Optional<UserSessionEntity> sessionOpt = userSessionService.getSessionByToken(token);
            
        userSessionService.deleteSession(token);
            
        String userIdInfo = sessionOpt.map(s -> "User ID: " + s.getUser().getId()).orElse("Unknown User (token might have been invalid)");
        log.info("Logout successful. Session token invalidated: {} for {}", token, userIdInfo);
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

    /**
     * Xác thực token có hợp lệ không
     */
    public boolean verifyToken(String token) {
        UserSessionEntity session = userSessionService.getSessionByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token không tồn tại trong hệ thống: {}", token);
                    return new UnauthorizedException("Token không hợp lệ hoặc đã hết hạn");
                });
            
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Token đã hết hạn: {}", token);
            return false;
        }
            
        userSessionService.updateLastActivity(token);
            
        return true;
    }
    
    /**
     * Lấy thông tin session từ token
     */
    public Map<String, Object> getSessionInfo(String token) {
        UserSessionEntity session = userSessionService.getSessionByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin session cho token: " + token));
                
        UserEntity user = session.getUser();
        user.getEmail(); 

        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("id", session.getId());
        sessionInfo.put("createdAt", session.getCreatedAt());
        sessionInfo.put("lastActivity", session.getLastActivity());
        sessionInfo.put("expiresAt", session.getExpiresAt());
        sessionInfo.put("isRememberMe", session.getIsRememberMe());
        sessionInfo.put("deviceType", session.getDeviceType());
        sessionInfo.put("browser", session.getBrowser());
        sessionInfo.put("platform", session.getPlatform());
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("role", user.getRole());
        
        result.put("session", sessionInfo);
        result.put("user", userInfo);
        
        return result;
    }
}
