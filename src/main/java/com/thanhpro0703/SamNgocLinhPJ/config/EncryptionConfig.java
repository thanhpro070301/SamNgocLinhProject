package com.thanhpro0703.SamNgocLinhPJ.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cấu hình mã hóa/giải mã các thông tin nhạy cảm như mật khẩu email, mật khẩu database
 */
@Configuration
public class EncryptionConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionConfig.class);

    @Autowired
    private Environment environment;

    /**
     * Tạo bean TextEncryptor để mã hóa/giải mã văn bản
     * Khóa và salt được lấy từ biến môi trường hoặc application.properties
     * Nếu không có, sẽ sử dụng giá trị mặc định (không an toàn cho môi trường production)
     */
    @Bean
    public TextEncryptor textEncryptor() {
        String encryptionKey = environment.getProperty("app.encryption.key", "samngoclinhsecuritykey12345678");
        String encryptionSalt = environment.getProperty("app.encryption.salt", "9c4e6ac3741bfd90");
        
        logger.debug("Creating TextEncryptor with key: [protected] and salt: {}", encryptionSalt);
        
        return Encryptors.text(encryptionKey, encryptionSalt);
    }

    /**
     * Phương thức tiện ích để mã hóa một chuỗi văn bản
     * @param text Văn bản cần mã hóa
     * @return Văn bản đã mã hóa
     */
    public String encrypt(String text) {
        return textEncryptor().encrypt(text);
    }

    /**
     * Phương thức tiện ích để giải mã một chuỗi văn bản
     * @param encryptedText Văn bản đã mã hóa
     * @return Văn bản đã giải mã
     */
    public String decrypt(String encryptedText) {
        return textEncryptor().decrypt(encryptedText);
    }
} 