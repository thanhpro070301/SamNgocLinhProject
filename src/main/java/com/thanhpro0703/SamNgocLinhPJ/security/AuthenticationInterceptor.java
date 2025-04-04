package com.thanhpro0703.SamNgocLinhPJ.security;
import com.thanhpro0703.SamNgocLinhPJ.service.UserSessionService;
import com.thanhpro0703.SamNgocLinhPJ.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final UserSessionService userSessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || bearerToken.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Thiếu token xác thực!");
            return false;
        }

        // Xử lý token bằng TokenUtils
        String token = TokenUtils.extractToken(bearerToken);

        if (!userSessionService.isValidSession(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn!");
            return false;
        }

        // Cập nhật thời gian hoạt động cuối cùng của phiên
        userSessionService.updateLastActivity(token);
        return true;
    }
}