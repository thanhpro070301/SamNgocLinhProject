package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.LoginRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.dto.RegisterRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.exception.BadRequestException;
import com.thanhpro0703.SamNgocLinhPJ.exception.RateLimitExceededException;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.OTPService;
import com.thanhpro0703.SamNgocLinhPJ.service.RateLimitService;
import com.thanhpro0703.SamNgocLinhPJ.util.ApiResponse;
import com.thanhpro0703.SamNgocLinhPJ.util.RequestUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thanhpro0703.SamNgocLinhPJ.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OTPService otpService;
    private final RateLimitService rateLimitService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * Gửi OTP - Nhận JSON request
     */
    @PostMapping(value = "/send-otp", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> sendOtpJson(@RequestBody Map<String, String> request, 
                                               HttpServletRequest httpRequest) {
        rateLimitService.checkRateLimit("otp", httpRequest);

        String email = request.get("email");
        
        if (email == null || email.isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }

        otpService.sendOtp(email);
        log.info("OTP sent successfully to email: {}", email);
        return ResponseEntity.ok(new ApiResponse(true, "Mã OTP đã được gửi đến email của bạn!"));
    }

    /**
     * Gửi OTP - Nhận text/plain request
     */
    @PostMapping(value = "/send-otp", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<ApiResponse> sendOtpPlain(@RequestBody String email, 
                                               HttpServletRequest httpRequest) {
        rateLimitService.checkRateLimit("otp", httpRequest);

        if (email == null || email.isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }

        String trimmedEmail = email.trim();
        otpService.sendOtp(trimmedEmail);
        log.info("OTP sent successfully to email: {}", trimmedEmail);
        return ResponseEntity.ok(new ApiResponse(true, "Mã OTP đã được gửi đến email của bạn!"));
    }

    @PostMapping(value = "/verify-otp", produces = "application/json")
    public ResponseEntity<ApiResponse> verifyOtp(
        @RequestBody Map<String, String> request,
        HttpServletRequest httpRequest
    ) {
        rateLimitService.checkRateLimit("otp", httpRequest);

        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || email.isEmpty() || otp == null || otp.isEmpty()) {
            throw new BadRequestException("Email và mã OTP không được để trống!");
        }

        otpService.verifyOtp(email, otp); // Nếu không hợp lệ, service sẽ ném BadRequestException
        // Không cần kiểm tra giá trị trả về nữa
        log.info("OTP verified successfully for email: {}", email);
        return ResponseEntity.ok(new ApiResponse(true, "Xác thực OTP thành công!"));
    }

    /**
     * Đăng ký người dùng mới
     */
    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequestDTO request, HttpServletRequest httpRequest) {
        rateLimitService.checkRateLimit("register", httpRequest);

        UserEntity user = authService.registerUser(
            request.getName(),
            request.getEmail(),
            request.getPassword(),
            request.getPhone()
        );
        log.info("User registered successfully: {}", user.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Đăng ký thành công", user));
    }

    /**
     * Đăng nhập và nhận token
     */
    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<ApiResponse> login(
        @Valid @RequestBody LoginRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        rateLimitService.checkRateLimit("login", httpRequest);

        Map<String, Object> authResult = authService.loginUser(request.getEmail(), request.getPassword(), httpRequest, request.getRememberMe());
        log.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Đăng nhập thành công!", authResult));
    }

    /**
     * Đăng xuất (Xóa session)
     */
    @PostMapping(value = "/logout", produces = "application/json")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        String token = RequestUtils.extractTokenFromHeader(request);
        if (token != null) {
            authService.logoutUser(token);
            log.info("Logout request processed for token (if valid).");
        } else {
            log.warn("Logout attempt without valid Authorization header.");
        }
        return ResponseEntity.ok(new ApiResponse(true, "Đăng xuất thành công!"));
    }

    /**
     * Kiểm tra token có hợp lệ không (thường dùng cho middleware hoặc kiểm tra nhanh)
     */
    @PostMapping(value = "/verify-token", produces = "application/json")
    public ResponseEntity<ApiResponse> verifyToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Token không được để trống");
        }
            
        // authService.verifyToken sẽ ném UnauthorizedException nếu không tồn tại
        // và trả về false nếu hết hạn. Cần xử lý cả hai trường hợp.
        boolean isValid = authService.verifyToken(token); 
            
        if (isValid) {
            // Nếu hợp lệ, lấy thông tin session (có thể ném ResourceNotFoundException)
            Map<String, Object> sessionInfo = authService.getSessionInfo(token);
            log.info("Token verified successfully: {}", token);
            return ResponseEntity.ok(new ApiResponse(true, "Token hợp lệ", sessionInfo));
        } else {
            // Nếu verifyToken trả về false (do hết hạn)
            // Hoặc nếu nó ném UnauthorizedException (do không tồn tại) - GlobalExceptionHandler sẽ bắt lỗi này
            // Chúng ta chỉ cần xử lý trường hợp trả về false ở đây
            throw new com.thanhpro0703.SamNgocLinhPJ.exception.UnauthorizedException("Token không hợp lệ hoặc đã hết hạn");
        }
        // Các lỗi khác (ví dụ: ResourceNotFound khi lấy sessionInfo) sẽ do GlobalExceptionHandler xử lý
    }

    /**
     * Kiểm tra token hợp lệ (trả về boolean, không cần authentication)
     */
    @PostMapping(value = "/check-token", produces = "application/json")
    public ResponseEntity<ApiResponse> checkToken(@RequestBody Map<String, String> request) {
         String token = request.get("token");
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Token không được để trống");
        }
            
        // Cải thiện để trả về thông tin chi tiết hơn về token
        Map<String, Object> data = new HashMap<>();
        data.put("valid", false);
        data.put("timestamp", System.currentTimeMillis());
        
        try {
             boolean isValid = authService.verifyToken(token);
             data.put("valid", isValid);
             
             if (isValid) {
                 // Bổ sung thêm thông tin về thời hạn của token
                 Map<String, Object> sessionInfo = authService.getSessionInfo(token);
                 Map<String, Object> sessionDetails = (Map<String, Object>) sessionInfo.get("session");
                 
                 // Thêm thông tin chi tiết về session
                 data.put("expiresAt", sessionDetails.get("expiresAt"));
                 data.put("lastActivity", sessionDetails.get("lastActivity"));
                 
                 // Không gửi thông tin người dùng chi tiết để bảo mật
                 Map<String, Object> userInfo = (Map<String, Object>) sessionInfo.get("user");
                 data.put("userId", userInfo.get("id"));
             }
        } catch (com.thanhpro0703.SamNgocLinhPJ.exception.UnauthorizedException e) {
             // Token không tồn tại trong DB -> không hợp lệ
             data.put("valid", false);
             data.put("error", "token_not_found");
             log.debug("Token not found during check: {}", token);
        } catch (Exception e) {
             // Lỗi khác không mong muốn, log và coi như không hợp lệ
             log.error("Unexpected error checking token: {}", e.getMessage());
             data.put("valid", false);
             data.put("error", "server_error");
        }
            
        // Luôn trả về OK, trạng thái hợp lệ nằm trong payload
        return ResponseEntity.ok(new ApiResponse(data.get("valid").equals(true), 
                                               data.get("valid").equals(true) ? "Token hợp lệ" : "Token không hợp lệ", 
                                               data));
    }
    
    /**
     * Lấy thông tin người dùng hiện tại từ token
     */
    @GetMapping(value = "/profile", produces = "application/json")
    public ResponseEntity<ApiResponse> getUserProfile(HttpServletRequest request) {
        String token = RequestUtils.extractTokenFromHeader(request);
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Token không được cung cấp");
        }
        
        try {
            // Xác minh token và lấy thông tin người dùng
            boolean isValid = authService.verifyToken(token);
            if (!isValid) {
                throw new com.thanhpro0703.SamNgocLinhPJ.exception.UnauthorizedException("Token không hợp lệ hoặc đã hết hạn");
            }
            
            Map<String, Object> sessionInfo = authService.getSessionInfo(token);
            Map<String, Object> userInfo = (Map<String, Object>) sessionInfo.get("user");
            
            log.info("Profile retrieved successfully for user ID: {}", userInfo.get("id"));
            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin người dùng thành công", userInfo));
        } catch (Exception e) {
            log.error("Error retrieving user profile: {}", e.getMessage());
            throw e; // Để GlobalExceptionHandler xử lý
        }
    }

    /**
     * Kiểm tra token và quyền admin
     */
    @PostMapping(value = "/check-admin", produces = "application/json")
    public ResponseEntity<ApiResponse> checkAdmin(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Token không được để trống");
        }
            
        // Kiểm tra token có hợp lệ không
        try {
            if (authService.verifyToken(token)) {
                // Lấy thông tin user từ token
                UserEntity user = authService.getUserByToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng cho token này"));
                
                boolean isAdmin = user.getRole() == UserEntity.Role.ADMIN;
                
                Map<String, Object> data = new HashMap<>();
                data.put("valid", true);
                data.put("isAdmin", isAdmin);
                data.put("username", user.getName());
                data.put("email", user.getEmail());
                data.put("role", user.getRole().name());
                
                return ResponseEntity.ok(new ApiResponse(true, "Token hợp lệ", data));
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("valid", false);
                data.put("isAdmin", false);
                return ResponseEntity.ok(new ApiResponse(false, "Token không hợp lệ hoặc đã hết hạn", data));
            }
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra token admin: {}", e.getMessage());
            Map<String, Object> data = new HashMap<>();
            data.put("valid", false);
            data.put("isAdmin", false);
            data.put("error", e.getMessage());
            return ResponseEntity.ok(new ApiResponse(false, "Lỗi xác thực: " + e.getMessage(), data));
        }
    }
}

