package com.thanhpro0703.SamNgocLinhPJ.filters;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.util.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Để Spring tự động nhận diện và quản lý bean này
@RequiredArgsConstructor // Lombok để tự tạo constructor cho các final fields
@Order(2) // Set order to 2, after AuthRedirectFilter
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final AuthService authService; // Inject AuthService
    private final UserDetailsService userDetailsService; // Inject UserDetailsService (sẽ là CustomUserDetailsService)

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = RequestUtils.extractTokenFromHeader(request);

            // Sử dụng AuthService để xác thực token
            if (StringUtils.hasText(jwt) && authService.verifyToken(jwt)) {
                logger.debug("Token verified successfully: {}", jwt);

                // Lấy thông tin UserEntity từ token
                UserEntity userEntity = authService.getUserByToken(jwt).orElse(null);

                // Nếu user tồn tại và chưa được xác thực trong context hiện tại
                if (userEntity != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userEmail = userEntity.getEmail();
                    logger.info("User found by token: {}", userEmail);

                    // Sử dụng UserDetailsService để tải UserDetails đầy đủ (bao gồm authorities)
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    
                    // Tạo Authentication với UserDetails đầy đủ
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, // Sử dụng UserDetails làm principal
                            null, 
                            userDetails.getAuthorities() // Lấy authorities từ UserDetails
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Thiết lập SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Authenticated user: {}, setting security context with authorities: {}", userEmail, userDetails.getAuthorities());
                } else {
                     logger.warn("User not found for token or user already authenticated.");
                }
            } else {
                 if (StringUtils.hasText(jwt)) {
                     logger.warn("Invalid or expired token: {}", jwt);
                 } else {
                     logger.trace("No JWT token found in request header.");
                 }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response); // Chuyển request/response cho filter tiếp theo
    }
} 