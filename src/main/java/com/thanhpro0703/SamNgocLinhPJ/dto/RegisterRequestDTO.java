package com.thanhpro0703.SamNgocLinhPJ.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String email;
    private String otp;
}