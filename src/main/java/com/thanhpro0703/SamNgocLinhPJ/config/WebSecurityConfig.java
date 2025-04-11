package com.thanhpro0703.SamNgocLinhPJ.config;

import com.thanhpro0703.SamNgocLinhPJ.filters.RateLimitFilter;
import com.thanhpro0703.SamNgocLinhPJ.filters.RequestLoggingFilter;
import com.thanhpro0703.SamNgocLinhPJ.security.jwt.JwtAuthenticationEntryPoint;
import com.thanhpro0703.SamNgocLinhPJ.security.jwt.JwtAuthenticationFilter;
import com.thanhpro0703.SamNgocLinhPJ.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private RateLimitFilter rateLimitFilter;
    
    @Autowired
    private RequestLoggingFilter requestLoggingFilter;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/", "/static/**", "/favicon.ico", "/index.html").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/news/**").permitAll()
                .requestMatchers("/api/health", "/api/status", "/api/api/health", "/api").permitAll()
                // Cho phép truy cập Swagger UI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Endpoints kiểm tra xác thực
                .requestMatchers("/api/test/public").permitAll()
                .requestMatchers("/api/test/user").authenticated()
                .requestMatchers("/api/test/admin").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/test/auth-info").authenticated()
                // API quản trị yêu cầu xác thực ADMIN
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // Tất cả các request khác đều yêu cầu xác thực
                .anyRequest().authenticated()
            )
            // Thêm bộ lọc truy cập debug
            .httpBasic(basic -> {});
        
        // Thêm Request Logging filter (sẽ chạy đầu tiên)
        http.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class);

        // Thêm Rate Limit filter
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080",
            "https://regal-piroshki-919004.netlify.app",
            "https://samngoclinhproject.onrender.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "x-environment"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 