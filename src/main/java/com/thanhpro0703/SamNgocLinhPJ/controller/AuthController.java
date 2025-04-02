package com.thanhpro0703.SamNgocLinhPJ.controller;
import com.thanhpro0703.SamNgocLinhPJ.dto.LoginRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.dto.RegisterRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.OTPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OTPService otpService;

    /**
     * Gửi OTP
     */
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody String email) {
        otpService.sendOtp(email);
        return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn!");
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (otpService.verifyOtp(email, otp)) {
            otpService.markVerified(email); // Lưu trạng thái xác thực thành công
            return ResponseEntity.ok("Xác thực OTP thành công!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP không hợp lệ hoặc đã hết hạn!");
    }

    /**
     * Đăng ký người dùng mới
     */


    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (!otpService.isVerified(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email chưa được xác thực!");
        }
        UserEntity newUser = authService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(newUser);
    }



    /**
     * Đăng nhập và nhận token
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.loginUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    /**
     * Đăng xuất (Xóa session)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logoutUser(token);
        return ResponseEntity.noContent().build();
    }
}
