package com.thanhpro0703.SamNgocLinhPJ.utils;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * Tiện ích để mã hóa/giải mã các giá trị cấu hình
 * Sử dụng class này để mã hóa các giá trị nhạy cảm trước khi đặt vào file application.properties
 */
public class EncryptionUtil {

    // Khóa mã hóa và salt phải giống với các giá trị trong EncryptionConfig
    private static final String ENCRYPTION_KEY = "samngoclinhsecuritykey12345678";
    private static final String ENCRYPTION_SALT = "9c4e6ac3741bfd90";

    private static final TextEncryptor encryptor = Encryptors.text(ENCRYPTION_KEY, ENCRYPTION_SALT);

    /**
     * Mã hóa một chuỗi văn bản
     * @param text Văn bản cần mã hóa
     * @return Văn bản đã mã hóa
     */
    public static String encrypt(String text) {
        return encryptor.encrypt(text);
    }

    /**
     * Giải mã một chuỗi văn bản
     * @param encryptedText Văn bản đã mã hóa
     * @return Văn bản đã giải mã
     */
    public static String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }

    /**
     * Tạo chuỗi mã hóa định dạng ENC(encrypted_text) để sử dụng trong application.properties
     * @param text Văn bản cần mã hóa
     * @return Chuỗi định dạng ENC(encrypted_text)
     */
    public static String encryptForProperties(String text) {
        return "ENC(" + encrypt(text) + ")";
    }

    /**
     * Hàm main để mã hóa các giá trị từ dòng lệnh
     */
    public static void main(String[] args) {
        // Thêm các giá trị nhạy cảm cần mã hóa ở đây
        String[] sensitiveValues = {
            "xarw aiab ciix aaad", // Mật khẩu email
            "root",               // Username DB
            ""                    // Password DB (trống)
        };

        System.out.println("Các giá trị đã mã hóa để sử dụng trong application.properties:");
        for (String value : sensitiveValues) {
            System.out.println("Original: " + value);
            System.out.println("Encrypted: " + encryptForProperties(value));
            System.out.println();
        }
    }
} 