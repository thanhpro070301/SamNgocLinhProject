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

    @GetMapping("/status")
    public ResponseEntity<String> checkSession(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        System.out.println("Received Authorization Header: " + authorizationHeader); // Debug log

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Session không hợp lệ hoặc đã hết hạn.");
        }

        String token = authorizationHeader.substring(7); // Cắt "Bearer "
        System.out.println("Extracted Token: " + token); // Debug log

        Optional<UserEntity> userOpt = sessionService.getUserFromToken(token);

        if (userOpt.isPresent()) {
            return ResponseEntity.ok("Session hợp lệ. Người dùng: " + userOpt.get().getUsername());
        } else {
            return ResponseEntity.status(401).body("Session không hợp lệ hoặc đã hết hạn.");
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long userId) {
        sessionService.deleteSession(userId);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }

}

