package com.thanhpro0703.SamNgocLinhPJ.exception;

import com.thanhpro0703.SamNgocLinhPJ.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi khi tham số không đúng kiểu dữ liệu
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        log.error("Lỗi chuyển đổi tham số: " + ex.getMessage());
        
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        
        String message = String.format("Tham số '%s' phải có kiểu dữ liệu là '%s', nhưng giá trị nhận được là '%s'", 
                name, type, value);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, message));
    }
    
    /**
     * Xử lý ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse> handleResponseStatusException(
            ResponseStatusException ex) {
        
        log.error("Lỗi ResponseStatusException: " + ex.getMessage());
        
        return ResponseEntity
            .status(ex.getStatusCode())
            .body(new ApiResponse(false, ex.getReason()));
    }
    
    /**
     * Xử lý lỗi xác thực
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse> handleAuthenticationException(Exception ex) {
        log.error("Lỗi xác thực: " + ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse(false, "Xác thực thất bại. Vui lòng kiểm tra thông tin đăng nhập."));
    }
    
    /**
     * Xử lý lỗi phân quyền
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Lỗi phân quyền: " + ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse(false, "Bạn không có quyền thực hiện hành động này."));
    }
    
    /**
     * Xử lý lỗi validation từ @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.error("Lỗi validation: " + ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String errorMessage = String.join(", ", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, errorMessage));
    }
    
    /**
     * Xử lý lỗi ràng buộc từ Hibernate Validator
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.error("Lỗi constraint: " + ex.getMessage());
        
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        String errorMessage = String.join(", ", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, errorMessage));
    }
    
    /**
     * Xử lý lỗi bind exception
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> handleBindException(BindException ex) {
        log.error("Lỗi binding: " + ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String errorMessage = String.join(", ", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, errorMessage));
    }
    
    /**
     * Xử lý lỗi thiếu tham số yêu cầu
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex) {
        log.error("Lỗi thiếu tham số: " + ex.getMessage());
        
        String message = String.format("Thiếu tham số bắt buộc: %s", ex.getParameterName());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, message));
    }
    
    /**
     * Xử lý lỗi định dạng JSON không hợp lệ
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        log.error("Lỗi định dạng dữ liệu: " + ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, "Định dạng dữ liệu không hợp lệ. Vui lòng kiểm tra lại."));
    }
    
    /**
     * Xử lý các lỗi nghiệp vụ cụ thể
     */
    @ExceptionHandler(DuplicateServiceException.class)
    public ResponseEntity<ApiResponse> handleDuplicateServiceException(
            DuplicateServiceException ex) {
        log.error("Lỗi dịch vụ trùng lặp: " + ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiResponse(false, ex.getMessage()));
    }
    
    /**
     * Xử lý các lỗi không lường trước được
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        log.error("Lỗi không xác định: " + ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse(false, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau."));
    }
}
