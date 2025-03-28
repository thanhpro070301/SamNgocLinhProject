package com.thanhpro0703.SamNgocLinhPJ.dto;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    /**
     * Chuyển từ DTO sang Entity
     */
    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .password(this.password) // Mật khẩu sẽ được mã hóa trong service
                .build();
    }

    /**
     * Chuyển từ Entity sang DTO
     */
    public static UserDTO fromEntity(UserEntity user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
