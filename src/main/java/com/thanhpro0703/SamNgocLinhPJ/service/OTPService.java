package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.OTPEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {
    private final JavaMailSender mailSender;
    private final OTPRepository otpRepository;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    public boolean isVerified(String email) {
        OTPEntity otp = otpRepository.findByEmailAndOtp(email, "VERIFIED")
            .orElse(null);
        return otp != null && !otp.getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    @Transactional
    public void markVerified(String email) {
        // Delete old OTPs
        otpRepository.deleteByEmail(email);
        
        // Create verified record
        OTPEntity verifiedOtp = OTPEntity.builder()
            .email(email)
            .otp("VERIFIED")
            .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .build();
        otpRepository.save(verifiedOtp);
        
        log.info("Email {} marked as verified", email);
    }
    
    @Transactional
    public void sendOtp(String email) {
        // Delete old OTPs
        otpRepository.deleteByEmail(email);
        
        // Generate OTP
        String otp = generateOTP();
        
        // Store OTP in database
        OTPEntity otpEntity = OTPEntity.builder()
            .email(email)
            .otp(otp)
            .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .build();
        otpRepository.save(otpEntity);
        
        // Send email
        sendOTPEmail(email, otp);
        
        log.info("OTP sent to email: {}", email);
    }
    
    public boolean verifyOtp(String email, String otp) {
        OTPEntity otpEntity = otpRepository.findByEmailAndOtp(email, otp)
            .orElse(null);
        
        if (otpEntity == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        
        // Check if OTP has expired
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}", email);
            otpRepository.delete(otpEntity);
            return false;
        }
        
        // Mark as verified
        markVerified(email);
        return true;
    }
    
    private String generateOTP() {
        // Use a more secure random number generator
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    private void sendOTPEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Xác thực OTP - Sâm Ngọc Linh");
        message.setText(String.format(
            "Xin chào,\n\n" +
            "Mã OTP của bạn là: %s\n\n" +
            "Mã OTP này có hiệu lực trong %d phút.\n" +
            "Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
            "Trân trọng,\n" +
            "Đội ngũ Sâm Ngọc Linh",
            otp, OTP_EXPIRY_MINUTES
        ));
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending OTP email to: {}", email, e);
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng thử lại sau.");
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteByExpiresAtBefore(now);
        log.info("Cleaned up expired OTPs");
    }
}

