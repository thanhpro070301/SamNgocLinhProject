package com.thanhpro0703.SamNgocLinhPJ.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Lớp hậu xử lý bean để tự động giải mã các thuộc tính đã được mã hóa
 * Nhận diện các trường có tiền tố ENC() và tự động giải mã khi bean được khởi tạo
 */
@Component
public class PropertyEncryptionPostProcessor implements BeanPostProcessor {

    private final TextEncryptor textEncryptor;
    
    // Danh sách các bean cần được xử lý
    private final Set<String> SENSITIVE_BEANS = new HashSet<>(Arrays.asList(
            "dataSource", 
            "mailSender", 
            "javaMailSender"));

    public PropertyEncryptionPostProcessor(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        // Chỉ xử lý các bean nhạy cảm
        if (SENSITIVE_BEANS.contains(beanName)) {
            processFields(bean);
        }
        return bean;
    }

    private void processFields(Object bean) {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.getType() == String.class) {
                String value = (String) field.get(bean);
                if (value != null && value.startsWith("ENC(") && value.endsWith(")")) {
                    String encryptedValue = value.substring(4, value.length() - 1);
                    try {
                        String decryptedValue = textEncryptor.decrypt(encryptedValue);
                        field.set(bean, decryptedValue);
                    } catch (Exception e) {
                        // Ghi log lỗi nhưng không làm gián đoạn khởi tạo ứng dụng
                        System.err.println("Error decrypting value for field: " + field.getName() + ". " + e.getMessage());
                    }
                }
            }
        });
    }
} 