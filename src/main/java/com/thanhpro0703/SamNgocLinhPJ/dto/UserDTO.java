package com.thanhpro0703.SamNgocLinhPJ.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String status;
    private String avatar;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    public static UserDTO fromEntity(UserEntity entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .role(entity.getRole().name())
                .status(entity.getStatus().name())
                .avatar(entity.getAvatar())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    public static UserDTO fromEntityWithoutSensitiveInfo(UserEntity entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .role(entity.getRole().name())
                .status(entity.getStatus().name())
                .avatar(entity.getAvatar())
                .build();
    }
}
