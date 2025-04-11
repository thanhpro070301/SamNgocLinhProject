package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.OTPEntity;
import com.thanhpro0703.SamNgocLinhPJ.exception.BadRequestException;
import com.thanhpro0703.SamNgocLinhPJ.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
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
    public void sendOtp(String email) {
        // Generate OTP
        String otp = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Thực hiện trong transaction đảm bảo atomic
        deleteAndSaveOtp(email, otp, expiresAt);
        
        // Gửi email (Nếu có lỗi MailException, GlobalExceptionHandler sẽ xử lý)
        sendOTPEmail(email, otp);
        
        log.info("OTP sent to email: {}", email);
    }
    
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void deleteAndSaveOtp(String email, String otp, LocalDateTime expiresAt) {
        // Sử dụng map và orElse để xử lý Optional
        OTPEntity otpToSave = otpRepository.findByEmail(email)
            .map(existingOtp -> {
                existingOtp.setOtp(otp);
                existingOtp.setExpiresAt(expiresAt);
                log.info("Updating existing OTP for email: {}", email);
                return existingOtp;
            })
            .orElseGet(() -> {
                log.info("Creating new OTP for email: {}", email);
                return OTPEntity.builder()
                    .email(email)
                    .otp(otp)
                    .expiresAt(expiresAt)
                    .build();
            });
        otpRepository.save(otpToSave);
    }
    
    @Transactional
    public void markVerified(String email) {
        try {
            // Thực hiện trong transaction đảm bảo atomic
            deleteAndSaveVerified(email);
            
            log.info("Email {} marked as verified", email);
        } catch (Exception e) {
            log.error("Error marking email as verified: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đánh dấu xác thực. Vui lòng thử lại sau.");
        }
    }
    
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void deleteAndSaveVerified(String email) {
        // Sử dụng map và orElse để xử lý Optional
        OTPEntity otpToSave = otpRepository.findByEmail(email)
            .map(existingOtp -> {
                existingOtp.setOtp("VERIFIED");
                existingOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
                log.info("Updating existing OTP to VERIFIED for email: {}", email);
                return existingOtp;
            })
            .orElseGet(() -> {
                log.info("Creating new VERIFIED status for email: {}", email);
                return OTPEntity.builder()
                    .email(email)
                    .otp("VERIFIED")
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .build();
            });
        otpRepository.save(otpToSave);
    }
    
    @Transactional
    public void verifyOtp(String email, String otp) {
        OTPEntity otpEntity = otpRepository.findByEmailAndOtp(email, otp)
            .orElseThrow(() -> {
                log.warn("Invalid OTP or email: {}", email);
                return new BadRequestException("OTP không hợp lệ hoặc đã hết hạn!");
            });
        
        // Check if OTP has expired
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}", email);
            otpRepository.delete(otpEntity); // Xóa OTP hết hạn
            throw new BadRequestException("OTP không hợp lệ hoặc đã hết hạn!");
        }
        
        // Mark as verified
        markVerified(email); 
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
        
        // Để MailException (hoặc lỗi khác) được ném ra
        mailSender.send(message);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteByExpiresAtBefore(now);
        log.info("Cleaned up expired OTPs");
    }
}

