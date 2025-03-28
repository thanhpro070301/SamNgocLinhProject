package com.thanhpro0703.SamNgocLinhPJ.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Liên kết với UserEntity
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(unique = true, nullable = false, length = 255)
    private String token;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusDays(7); // Phiên có hạn trong 7 ngày
    }
}
