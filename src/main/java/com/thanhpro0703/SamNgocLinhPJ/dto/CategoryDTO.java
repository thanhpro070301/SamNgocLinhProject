package com.thanhpro0703.SamNgocLinhPJ.dto;

import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class CategoryDTO {
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự")
    private String name;


    public CategoryEntity toEntity() {
        return CategoryEntity.builder()
                .id(Optional.ofNullable(this.id).orElse(null)) // Tránh lỗi null không cần thiết
                .name(this.name)
                .build();
    }

    public static CategoryDTO fromEntity(CategoryEntity category) {
        return Optional.ofNullable(category)
                .map(cat -> CategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .orElse(null); // Nếu null thì trả về null, tránh lỗi
    }
}
