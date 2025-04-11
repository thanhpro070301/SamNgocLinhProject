package com.thanhpro0703.SamNgocLinhPJ.repository;

import com.thanhpro0703.SamNgocLinhPJ.entity.OTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity, Long> {
    Optional<OTPEntity> findByEmailAndOtp(String email, String otp);
    Optional<OTPEntity> findByEmail(String email);
    void deleteByEmail(String email);
    
    @Modifying
    @Query("DELETE FROM OTPEntity o WHERE o.expiresAt < :dateTime")
    void deleteByExpiresAtBefore(@Param("dateTime") LocalDateTime dateTime);
    
    @Modifying
    @Query(value = "INSERT INTO otp_codes (email, otp, expires_at) VALUES (:email, :otp, :expiresAt) " +
            "ON DUPLICATE KEY UPDATE otp = :otp, expires_at = :expiresAt", nativeQuery = true)
    void upsertOtp(@Param("email") String email, @Param("otp") String otp, @Param("expiresAt") LocalDateTime expiresAt);
} 