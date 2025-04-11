package com.thanhpro0703.SamNgocLinhPJ.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    @Column(name = "token_id", nullable = false, unique = true, length = 36)
    private String tokenId;

    @Column(name = "refresh_token_id", unique = true, length = 36)
    private String refreshTokenId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_type", length = 100)
    private String deviceType;

    @Column(name = "platform", length = 100)
    private String platform;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_remember_me")
    @Builder.Default
    private Boolean isRememberMe = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastActivity == null) {
            lastActivity = LocalDateTime.now();
        }
    }
} 