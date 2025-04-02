package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.entity.OTPEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OTPService {
    private final OTPRepository otpRepository;
    private final JavaMailSender mailSender;
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();

    public void sendOtp(String email) {
        otpRepository.deleteByEmail(email);
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        OTPEntity otpEntity = OTPEntity.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        otpRepository.save(otpEntity);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Xác thực tài khoản - OTP");
        message.setText("Mã OTP của bạn là: " + otp);
        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<OTPEntity> otpOpt = otpRepository.findByEmailAndOtp(email, otp);
        if (otpOpt.isPresent() && otpOpt.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    public void markVerified(String email) {
        verifiedEmails.put(email, true);
    }

    public boolean isVerified(String email) {
        return verifiedEmails.getOrDefault(email, false);
    }
}

