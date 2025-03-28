package com.thanhpro0703.SamNgocLinhPJ.controller;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.SessionService;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        Optional<UserEntity> userOpt = authService.authenticate(username, password);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu!");
        }

        String token = sessionService.createSession(userOpt.get());
        return ResponseEntity.ok(token);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long userId) {
        sessionService.deleteSession(userId);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }
}

