package com.thanhpro0703.SamNgocLinhPJ.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "Mật khẩu phải chứa ít nhất một chữ số, một chữ thường và một chữ hoa")
    private String password;

    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phone;
    
    private String otp;
    
    // Không trả về mật khẩu trong response
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }
    
    // Không trả về OTP trong response
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getOtp() {
        return otp;
    }
}