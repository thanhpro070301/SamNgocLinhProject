package com.thanhpro0703.SamNgocLinhPJ.controller;
import com.thanhpro0703.SamNgocLinhPJ.dto.LoginRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.dto.RegisterRequestDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Đăng ký người dùng mới
     */
    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserEntity newUser = authService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
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
