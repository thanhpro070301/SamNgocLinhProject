package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.CategoryDTO;
import com.thanhpro0703.SamNgocLinhPJ.dto.ProductDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.CategoryRepository;
import com.thanhpro0703.SamNgocLinhPJ.repository.ProductRepository;
import com.thanhpro0703.SamNgocLinhPJ.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            // Log lỗi
            log.error("Lỗi khi lấy danh sách danh mục: {}", e.getMessage(), e);
            
            // Tạo thông báo lỗi
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Đã xảy ra lỗi khi lấy danh mục sản phẩm");
            errorResponse.put("error", e.getMessage());
            
            // Trả về lỗi 400 Bad Request thay vì 500 Internal Server Error
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/parent")
    public ResponseEntity<List<CategoryDTO>> getParentCategories() {
        List<CategoryDTO> categories = categoryRepository.findByParentIsNullAndStatus(CategoryEntity.Status.ACTIVE)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        // Kiểm tra và xử lý giá trị "undefined"
        if (id == null || id.equals("undefined") || id.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID danh mục không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Integer categoryId = Integer.parseInt(id);
            try {
                CategoryDTO category = categoryService.getCategoryById(categoryId);
                return ResponseEntity.ok(category);
            } catch (ResponseStatusException e) {
                return ResponseEntity.notFound().build();
            }
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID danh mục phải là số nguyên");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return categoryRepository.findBySlug(slug)
                .map(CategoryDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        return categoryRepository.findById(id)
                .map(category -> {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<ProductEntity> productPage;
                    
                    if (status != null && !status.isEmpty()) {
                        if (status.equalsIgnoreCase("ALL")) {
                            // Lấy tất cả sản phẩm của danh mục
                            productPage = productRepository.findByCategory(category, pageable);
                        } else {
                            try {
                                ProductEntity.Status statusEnum = ProductEntity.Status.valueOf(status.toUpperCase());
                                productPage = productRepository.findByCategoryAndStatus(category, statusEnum, pageable);
                            } catch (IllegalArgumentException e) {
                                // Nếu status không hợp lệ, lấy tất cả sản phẩm
                                productPage = productRepository.findByCategory(category, pageable);
                            }
                        }
                    } else {
                        // Mặc định lấy tất cả sản phẩm
                        productPage = productRepository.findByCategory(category, pageable);
                    }
                    
                    List<ProductDTO> products = productPage.getContent().stream()
                            .map(ProductDTO::fromEntity)
                            .collect(Collectors.toList());
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("category", CategoryDTO.fromEntity(category));
                    response.put("products", products);
                    response.put("currentPage", productPage.getNumber());
                    response.put("totalItems", productPage.getTotalElements());
                    response.put("totalPages", productPage.getTotalPages());
                    
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
