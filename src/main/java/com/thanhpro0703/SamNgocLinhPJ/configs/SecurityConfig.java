package com.thanhpro0703.SamNgocLinhPJ.configs;
import com.thanhpro0703.SamNgocLinhPJ.filters.AuthRedirectFilter;
import com.thanhpro0703.SamNgocLinhPJ.filters.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// @EnableWebSecurity
@Deprecated // Đánh dấu là cũ, sử dụng WebSecurityConfig thay thế
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final AuthRedirectFilter authRedirectFilter;

    public SecurityConfig(TokenAuthenticationFilter tokenAuthenticationFilter,
                          AuthRedirectFilter authRedirectFilter) {
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.authRedirectFilter = authRedirectFilter;
    }

    // @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS instead of disabling it
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Cấu hình CSRF - chỉ tắt cho API endpoint, nên bật cho ứng dụng web thông thường
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**"))
                
            // Cấu hình bảo mật HTTP
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; font-src 'self' data:; connect-src 'self'"))
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
            
            // Không sử dụng session - vì chúng ta đang dùng token
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Cấu hình authorization
            .authorizeHttpRequests(auth -> auth
                // Ưu tiên cho phép các health check endpoints trước
                .requestMatchers(
                    "/", "/health", "/status", 
                    "/api", "/api/health", "/api/status", 
                    "/api/api/health" // Bao gồm cả đường dẫn /api/api/health từ log frontend
                ).permitAll()
                // Sau đó cho phép các API công khai khác
                .requestMatchers(
                    "/api/auth/**", "/api/products/**", "/api/categories/**", 
                    "/api/news/**", "/api/test/**", "/api/orders/public/**"
                ).permitAll()
                // Yêu cầu quyền ADMIN cho /api/admin/**
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // Đảm bảo endpoint đặt hàng yêu cầu xác thực
                .requestMatchers("/api/orders/**").authenticated()
                // Tất cả các request khác yêu cầu xác thực
                .anyRequest().authenticated())
            
            // Thêm các filter xác thực
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // @Bean
    public HttpFirewall allowUrlEncodedPercentHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        
        // Cấu hình cho phép các ký tự đặc biệt trong giá trị header/cookie
        firewall.setAllowedHeaderValues(header -> true);
        firewall.setAllowedHeaderNames(header -> true);
        firewall.setAllowedParameterNames(parameter -> true);
        
        return firewall;
    }

    // @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("http://localhost:5173"); 
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("https://regal-piroshki-919004.netlify.app");
        configuration.addAllowedOrigin("https://samngoclinhproject.onrender.com");
        configuration.addAllowedHeader("*");
        configuration.addAllowedHeader("Cache-Control");
        configuration.addAllowedHeader("x-environment");
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
