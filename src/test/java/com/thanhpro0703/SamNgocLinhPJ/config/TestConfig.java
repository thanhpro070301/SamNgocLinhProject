package com.thanhpro0703.SamNgocLinhPJ.config;

import com.thanhpro0703.SamNgocLinhPJ.repository.UserSessionRepository;
import com.thanhpro0703.SamNgocLinhPJ.security.AuthenticationInterceptor;
import com.thanhpro0703.SamNgocLinhPJ.security.jwt.JwtAuthenticationEntryPoint;
import com.thanhpro0703.SamNgocLinhPJ.security.jwt.JwtAuthenticationFilter;
import com.thanhpro0703.SamNgocLinhPJ.service.UserSessionService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class,
    FlywayAutoConfiguration.class
})
public class TestConfig {
    
    @MockBean
    private UserSessionRepository userSessionRepository;
    
    @Bean
    @Primary
    public UserSessionService userSessionService() {
        return new UserSessionService(userSessionRepository);
    }
    
    @Bean
    @Primary
    public AuthenticationInterceptor authenticationInterceptor(UserSessionService userSessionService) {
        return new AuthenticationInterceptor(userSessionService);
    }
    
    @Bean
    @Primary
    public WebConfig webConfig(AuthenticationInterceptor authenticationInterceptor) {
        return new WebConfig(authenticationInterceptor);
    }
    
    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }
    
    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        
        return http.build();
    }
} 