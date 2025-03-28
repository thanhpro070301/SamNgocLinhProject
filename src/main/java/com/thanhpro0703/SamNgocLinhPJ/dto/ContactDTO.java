package com.thanhpro0703.SamNgocLinhPJ.dto;
import com.thanhpro0703.SamNgocLinhPJ.entity.ContactEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactDTO {
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phone;

    @Size(max = 500, message = "Tin nhắn không được vượt quá 500 ký tự")
    private String message;

    public ContactEntity toEntity() {
        return ContactEntity.builder()
                .name(this.name)
                .phone(this.phone)
                .message(this.message)
                .build();
    }
}

