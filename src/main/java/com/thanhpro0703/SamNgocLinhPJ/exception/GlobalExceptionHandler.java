package com.thanhpro0703.SamNgocLinhPJ.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi khi tham số không đúng kiểu dữ liệu
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        log.error("Lỗi chuyển đổi tham số: " + ex.getMessage(), ex);
        
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        
        String message = String.format("Tham số '%s' phải có kiểu dữ liệu là '%s', nhưng giá trị nhận được là '%s'", 
                name, type, value);
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "BAD_REQUEST");
        errors.put("message", message);
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Xử lý ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException ex) {
        
        log.error("Lỗi ResponseStatusException: " + ex.getMessage(), ex);
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getStatusCode().toString());
        errors.put("message", ex.getReason());
        
        return new ResponseEntity<>(errors, ex.getStatusCode());
    }
    
    /**
     * Xử lý các lỗi không lường trước được
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        log.error("Lỗi không xác định: " + ex.getMessage(), ex);
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "INTERNAL_SERVER_ERROR");
        errors.put("message", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.");
        
        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
