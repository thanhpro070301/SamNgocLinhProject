package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.OTPEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {
    private final JavaMailSender mailSender;
    private final OTPRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int VERIFIED_EXPIRY_MINUTES = 30; // 30 phút cho trạng thái đã xác thực
    private static final int MAX_ATTEMPTS = 3;
    private static final String VERIFIED_FLAG = "VERIFIED";
    
    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        log.info("Checking if email is verified: {}", maskEmail(email));
        OTPEntity otp = otpRepository.findByEmail(email)
            .stream()
            .filter(o -> VERIFIED_FLAG.equals(o.getOtp()))
            .findFirst()
            .orElse(null);
        return otp != null && !otp.getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void markVerified(String email) {
        log.info("Marking email as verified: {}", maskEmail(email));
        try {
            // Delete old OTPs
            otpRepository.deleteByEmail(email);
            
            // Create verified record
            OTPEntity verifiedOtp = OTPEntity.builder()
                .email(email)
                .otp(VERIFIED_FLAG)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFIED_EXPIRY_MINUTES))
                .build();
            otpRepository.save(verifiedOtp);
            
            log.info("Email {} marked as verified", maskEmail(email));
        } catch (Exception e) {
            log.error("Error marking email as verified: {}", maskEmail(email), e);
            throw e;
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void sendOtp(String email) {
        log.info("Sending OTP to email: {}", maskEmail(email));
        try {
            // Delete old OTPs
            otpRepository.deleteByEmail(email);
            
            // Generate OTP
            String otp = generateOTP();
            
            // Store OTP in database (hashed)
            OTPEntity otpEntity = OTPEntity.builder()
                .email(email)
                .otp(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
            otpRepository.save(otpEntity);
            
            // Send email
            sendOTPEmail(email, otp);
            
            log.info("OTP sent to email: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Error sending OTP to email: {}", maskEmail(email), e);
            throw e;
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean verifyOtp(String email, String otp) {
        if (email == null || email.isEmpty() || otp == null || otp.isEmpty()) {
            log.warn("Invalid email or OTP: email={}, otp={}", maskEmail(email), otp != null);
            return false;
        }
        
        log.info("Verifying OTP for email: {}", maskEmail(email));
        try {
            // Tìm tất cả các OTP cho email này
            var otpEntities = otpRepository.findByEmail(email);
            
            if (otpEntities.isEmpty()) {
                log.warn("No OTP found for email: {}", maskEmail(email));
                return false;
            }
            
            // Tìm OTP hợp lệ và chưa hết hạn
            boolean verified = false;
            for (OTPEntity otpEntity : otpEntities) {
                // Bỏ qua nếu đã hết hạn
                if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
                    continue;
                }
                
                // Bỏ qua nếu là trạng thái đã xác thực
                if (VERIFIED_FLAG.equals(otpEntity.getOtp())) {
                    continue;
                }
                
                // Kiểm tra mã OTP
                if (passwordEncoder.matches(otp, otpEntity.getOtp())) {
                    verified = true;
                    break;
                }
            }
            
            if (!verified) {
                log.warn("Invalid OTP for email: {}", maskEmail(email));
                return false;
            }
            
            // Delete all OTPs for this email
            otpRepository.deleteByEmail(email);
            
            // Mark as verified
            markVerified(email);
            return true;
        } catch (Exception e) {
            log.error("Error verifying OTP for email: {}: {}", maskEmail(email), e.getMessage(), e);
            return false;
        }
    }
    
    private String generateOTP() {
        // Sử dụng SecureRandom để tăng tính bảo mật
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    private void sendOTPEmail(String email, String otp) {
        log.info("Sending OTP email to: {}", maskEmail(email));
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
            log.info("OTP email sent successfully to: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Error sending OTP email: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng thử lại sau.");
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional(propagation = Propagation.REQUIRED)
    public void cleanupExpiredOtps() {
        log.info("Cleaning up expired OTPs");
        try {
            LocalDateTime now = LocalDateTime.now();
            int deleted = otpRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up {} expired OTPs", deleted);
        } catch (Exception e) {
            log.error("Error cleaning up expired OTPs", e);
        }
    }
    
    /**
     * Che dấu email trong log để bảo vệ thông tin
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return "invalid-email";
        }
        
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        
        if (name.length() <= 2) {
            return name + "@" + domain;
        }
        
        String maskedName = name.substring(0, 1) + 
                            "*".repeat(name.length() - 2) + 
                            name.substring(name.length() - 1);
        
        return maskedName + "@" + domain;
    }
}

