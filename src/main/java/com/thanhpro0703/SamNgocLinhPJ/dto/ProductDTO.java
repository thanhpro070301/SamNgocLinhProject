package com.thanhpro0703.SamNgocLinhPJ.dto;

import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id; // Bổ sung ID để hỗ trợ cập nhật

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String imageUrl;

    // Chuyển từ DTO sang Entity
    public ProductEntity toEntity(CategoryEntity category) {
        return ProductEntity.builder()
                .id(this.id) // Bổ sung ID để hỗ trợ cập nhật
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .category(category) // Gán đối tượng category thay vì chỉ ID
                .imageUrl(this.imageUrl)
                .build();
    }

    // Chuyển từ Entity sang DTO
    public static ProductDTO fromEntity(ProductEntity product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId()) // Lấy ID của category
                .build();
    }
}
