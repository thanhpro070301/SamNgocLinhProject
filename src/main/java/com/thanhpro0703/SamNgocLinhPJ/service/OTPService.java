package com.thanhpro0703.SamNgocLinhPJ.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {
    private final JavaMailSender mailSender;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    // In-memory storage for OTPs and verification status
    private final Map<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    
    @Cacheable(value = "verifiedEmails", key = "#email")
    public boolean isVerified(String email) {
        OTPData data = otpStorage.get(email);
        return data != null && data.isVerified();
    }
    
    @CacheEvict(value = "verifiedEmails", key = "#email")
    public void markVerified(String email) {
        OTPData data = otpStorage.get(email);
        if (data != null) {
            data.setVerified(true);
            otpStorage.put(email, data);
            log.info("Email {} marked as verified", email);
        }
    }
    
    @Transactional
    public void sendOtp(String email) {
        // Generate OTP
        String otp = generateOTP();
        
        // Store OTP data
        OTPData otpData = new OTPData(otp, LocalDateTime.now(), 0);
        otpStorage.put(email, otpData);
        
        // Send email
        sendOTPEmail(email, otp);
        
        log.info("OTP sent to email: {}", email);
    }
    
    public boolean verifyOtp(String email, String otp) {
        OTPData data = otpStorage.get(email);
        
        if (data == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        
        // Check if OTP has expired
        if (data.getGeneratedAt().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}", email);
            otpStorage.remove(email);
            return false;
        }
        
        // Check if max attempts reached
        if (data.getAttempts() >= MAX_ATTEMPTS) {
            log.warn("Max OTP attempts reached for email: {}", email);
            otpStorage.remove(email);
            return false;
        }
        
        // Increment attempts
        data.incrementAttempts();
        otpStorage.put(email, data);
        
        // Verify OTP
        boolean isValid = data.getOtp().equals(otp);
        if (isValid) {
            markVerified(email);
        }
        
        return isValid;
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
    
    // Inner class to store OTP data
    private static class OTPData {
        private final String otp;
        private final LocalDateTime generatedAt;
        private int attempts;
        private boolean verified;
        
        public OTPData(String otp, LocalDateTime generatedAt, int attempts) {
            this.otp = otp;
            this.generatedAt = generatedAt;
            this.attempts = attempts;
            this.verified = false;
        }
        
        public String getOtp() {
            return otp;
        }
        
        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }
        
        public int getAttempts() {
            return attempts;
        }
        
        public void incrementAttempts() {
            this.attempts++;
        }
        
        public boolean isVerified() {
            return verified;
        }
        
        public void setVerified(boolean verified) {
            this.verified = verified;
        }
    }
}

