package com.thanhpro0703.SamNgocLinhPJ.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Mặc định trả về 400
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
} 