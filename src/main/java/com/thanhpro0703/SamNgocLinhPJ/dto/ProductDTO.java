package com.thanhpro0703.SamNgocLinhPJ.dto;

import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private Integer categoryId;
    private String categoryName;
    private Integer stock;
    private Integer sold;
    private BigDecimal rating;
    private String status;
    private LocalDateTime createdAt;
    private List<String> images;

    public static ProductDTO fromEntity(ProductEntity entity) {
        ProductDTO dto = ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .originalPrice(entity.getOriginalPrice())
                .image(entity.getImage())
                .stock(entity.getStock())
                .sold(entity.getSold())
                .rating(entity.getRating())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
        
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }
        
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            dto.setImages(entity.getImages().stream()
                    .map(ProductImageEntity::getImagePath)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 