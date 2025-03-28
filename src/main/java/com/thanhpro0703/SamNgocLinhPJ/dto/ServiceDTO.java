package com.thanhpro0703.SamNgocLinhPJ.dto;
import com.thanhpro0703.SamNgocLinhPJ.entity.ServiceEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServiceDTO {
    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 255, message = "Tên dịch vụ không được vượt quá 255 ký tự")
    private String name;

    private String description;

    public ServiceEntity toEntity() {
        return ServiceEntity.builder()
                .name(this.name)
                .description(this.description)
                .build();
    }
}