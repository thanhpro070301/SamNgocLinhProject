package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.dto.CategoryDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("Lấy danh sách tất cả danh mục");
        try {
            return categoryRepository.findAll().stream()
                    .map(entity -> {
                        try {
                            return CategoryDTO.fromEntity(entity);
                        } catch (Exception e) {
                            log.error("Lỗi khi chuyển đổi CategoryEntity sang DTO: {}", e.getMessage());
                            CategoryDTO dto = new CategoryDTO();
                            dto.setId(entity.getId());
                            dto.setName(entity.getName());
                            dto.setSlug(entity.getSlug());
                            dto.setDescription(entity.getDescription());
                            dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
                            dto.setCreatedAt(entity.getCreatedAt());
                            return dto;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách danh mục: {}", e.getMessage(), e);
            return List.of(); // Trả về danh sách rỗng thay vì lỗi
        }
    }


    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Integer id) {
        log.info("Tìm danh mục có ID: {}", id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục có ID: {}", id);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy danh mục có ID: " + id);
                });
        return CategoryDTO.fromEntity(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Tạo danh mục mới: {}", categoryDTO.getName());

        // Check trùng tên
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            log.warn("Danh mục '{}' đã tồn tại!", categoryDTO.getName());
            throw new IllegalArgumentException("Danh mục đã tồn tại!");
        }

        CategoryEntity category = categoryRepository.save(categoryDTO.toEntity());
        return CategoryDTO.fromEntity(category);
    }


    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO) {
        log.info("Cập nhật danh mục có ID: {}", id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục có ID: {}", id);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy danh mục có ID: " + id);
                });

        if (!category.getName().equals(categoryDTO.getName()) && categoryRepository.existsByName(categoryDTO.getName())) {
            log.warn("Tên danh mục '{}' đã tồn tại!", categoryDTO.getName());
            throw new IllegalArgumentException("Tên danh mục đã tồn tại!");
        }

        category.setName(categoryDTO.getName());
        return CategoryDTO.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Integer id) {
        log.info("Xóa danh mục có ID: {}", id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy danh mục có ID: {}", id);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy danh mục có ID: " + id);
                });

        if (!category.getProducts().isEmpty()) {
            log.warn("Không thể xóa danh mục '{}', vì có sản phẩm liên quan!", category.getName());
            throw new IllegalStateException("Không thể xóa danh mục đang chứa sản phẩm!");
        }

        categoryRepository.delete(category);
        log.info("Đã xóa danh mục có ID: {}", id);
    }
}
