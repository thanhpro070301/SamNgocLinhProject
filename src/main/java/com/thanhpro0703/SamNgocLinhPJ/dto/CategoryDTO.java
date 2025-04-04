package com.thanhpro0703.SamNgocLinhPJ.dto;

import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private Integer parentId;
    private String parentName;
    private String status;
    private LocalDateTime createdAt;
    private List<CategoryDTO> subCategories;

    public CategoryEntity toEntity() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(this.id)
                .name(this.name)
                .slug(this.slug != null ? this.slug : generateSlug(this.name))
                .description(this.description)
                .build();
        
        if (this.status != null) {
            entity.setStatus(CategoryEntity.Status.valueOf(this.status));
        }
        
        return entity;
    }
    
    private String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
    }

    public static CategoryDTO fromEntity(CategoryEntity entity) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
        
        if (entity.getParent() != null) {
            dto.setParentId(entity.getParent().getId());
            dto.setParentName(entity.getParent().getName());
        }
        
        if (entity.getSubCategories() != null && !entity.getSubCategories().isEmpty()) {
            dto.setSubCategories(entity.getSubCategories().stream()
                    .map(CategoryDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
