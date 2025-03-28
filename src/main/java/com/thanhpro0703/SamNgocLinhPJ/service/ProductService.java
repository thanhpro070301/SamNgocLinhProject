package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.dto.ProductDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.CategoryRepository;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Lấy danh sách tất cả sản phẩm (Chuyển đổi sang DTO)
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Lấy danh sách tất cả sản phẩm");
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntity) // Convert Entity → DTO
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID (Chuyển đổi sang DTO)
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Tìm sản phẩm với ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm với ID: " + id));
        return ProductDTO.fromEntity(product);
    }

    // Tạo sản phẩm mới (Dùng DTO)
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Tạo sản phẩm mới: {}", productDTO.getName());

        // Kiểm tra trùng tên sản phẩm
        if (productRepository.existsByName(productDTO.getName())) {
            log.warn("Sản phẩm '{}' đã tồn tại!", productDTO.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm đã tồn tại!");
        }

        // Lấy danh mục từ DB
        CategoryEntity category = findCategoryById(productDTO.getCategoryId());

        // Chuyển DTO → Entity rồi lưu vào DB
        ProductEntity newProduct = productDTO.toEntity(category);
        ProductEntity savedProduct = productRepository.save(newProduct);

        return ProductDTO.fromEntity(savedProduct);
    }

    // Cập nhật sản phẩm
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Cập nhật sản phẩm với ID: {}", id);

        // Lấy sản phẩm cần cập nhật
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new  ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm với ID: " + id));

        // Kiểm tra trùng tên sản phẩm (nếu thay đổi tên)
        if (!product.getName().equals(productDTO.getName()) && productRepository.existsByName(productDTO.getName())) {
            log.warn("Tên sản phẩm '{}' đã tồn tại!", productDTO.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tên sản phẩm đã tồn tại!");
        }

        // Cập nhật thông tin
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());

        // Cập nhật danh mục nếu có
        if (productDTO.getCategoryId() != null) {
            CategoryEntity category = findCategoryById(productDTO.getCategoryId());
            product.setCategory(category);
        }

        ProductEntity updatedProduct = productRepository.save(product);
        return ProductDTO.fromEntity(updatedProduct);
    }

    // Xóa sản phẩm
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Xóa sản phẩm với ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm với ID: " + id));

        productRepository.delete(product);
        log.info("Đã xóa sản phẩm với ID: {}", id);
    }

    // Tìm danh mục theo ID
    private CategoryEntity findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy danh mục với ID: " + categoryId));
    }
}
