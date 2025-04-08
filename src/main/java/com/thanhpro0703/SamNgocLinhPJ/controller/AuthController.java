package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.LoginRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.dto.RegisterRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.OTPService;
import com.thanhpro0703.SamNgocLinhPJ.utils.TokenUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OTPService otpService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Rate limiting configuration
    private final Bucket loginBucket = Bucket4j.builder()
        .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
        .build();

    private final Bucket registerBucket = Bucket4j.builder()
        .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))))
        .build();

    private final Bucket otpBucket = Bucket4j.builder()
        .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1))))
        .build();

    /**
     * Gửi OTP
     */
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody String email, HttpServletRequest request) {
        if (!otpBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for OTP request from IP: {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.");
        }

        try {
            otpService.sendOtp(email);
            log.info("OTP sent successfully to email: {}", email);
            return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn!");
        } catch (Exception e) {
            log.error("Error sending OTP to email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Có lỗi xảy ra khi gửi OTP. Vui lòng thử lại sau.");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
        @RequestParam String email,
        @RequestParam String otp,
        HttpServletRequest request
    ) {
        if (!otpBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for OTP verification from IP: {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.");
        }

        try {
            if (otpService.verifyOtp(email, otp)) {
                otpService.markVerified(email);
                log.info("OTP verified successfully for email: {}", email);
                return ResponseEntity.ok("Xác thực OTP thành công!");
            }
            log.warn("Invalid OTP attempt for email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("OTP không hợp lệ hoặc đã hết hạn!");
        } catch (Exception e) {
            log.error("Error verifying OTP for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Có lỗi xảy ra khi xác thực OTP. Vui lòng thử lại sau.");
        }
    }

    /**
     * Đăng ký người dùng mới
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
        @Valid @RequestBody RegisterRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        if (!registerBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for registration from IP: {}", httpRequest.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Quá nhiều yêu cầu đăng ký. Vui lòng thử lại sau 1 phút.");
        }

        try {
            if (!otpService.isVerified(request.getEmail())) {
                log.warn("Registration attempt with unverified email: {}", request.getEmail());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email chưa được xác thực!");
            }

            UserEntity newUser = authService.registerUser(
                request.getEmail(),
                request.getPassword()
            );

            log.info("New user registered successfully: {}", request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký thành công!");
            response.put("user", newUser);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during registration for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Có lỗi xảy ra khi đăng ký. Vui lòng thử lại sau.");
        }
    }

    /**
     * Đăng nhập và nhận token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @Valid @RequestBody LoginRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        if (!loginBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for login from IP: {}", httpRequest.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Quá nhiều yêu cầu đăng nhập. Vui lòng thử lại sau 1 phút.");
        }

        try {
            String token = authService.loginUser(request.getEmail(), request.getPassword());
            
            if (token == null) {
                log.warn("Failed login attempt for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email hoặc mật khẩu không đúng");
            }

            log.info("User logged in successfully: {}", request.getEmail());
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Đăng nhập thành công!");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during login for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại sau.");
        }
    }

    /**
     * Đăng xuất (Xóa session)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logoutUser(token);
                log.info("User logged out successfully");
            }
            return ResponseEntity.ok("Đăng xuất thành công!");
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Có lỗi xảy ra khi đăng xuất. Vui lòng thử lại sau.");
        }
    }
}
