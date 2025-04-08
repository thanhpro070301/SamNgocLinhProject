package com.thanhpro0703.SamNgocLinhPJ.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, message = "Họ và tên phải có ít nhất 2 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phone;
}