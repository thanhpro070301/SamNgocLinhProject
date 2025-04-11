package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.ProductDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.ProductRepository;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.ProductService;
import com.thanhpro0703.SamNgocLinhPJ.utils.TokenUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ProductEntity> productPage;
        
        // Lọc theo trạng thái nếu được chỉ định
        if (status != null && !status.isEmpty()) {
            try {
                ProductEntity.Status statusEnum = ProductEntity.Status.valueOf(status.toUpperCase());
                productPage = productRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                // Nếu status không hợp lệ, lấy tất cả sản phẩm
                log.warn("Trạng thái không hợp lệ: {}. Lấy tất cả sản phẩm.", status);
                productPage = productRepository.findAll(pageable);
            }
        } else {
            // Mặc định lấy tất cả sản phẩm
            productPage = productRepository.findAll(pageable);
        }

        List<ProductDTO> products = productPage.getContent().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        // Kiểm tra và xử lý giá trị "undefined"
        if (id == null || id.equals("undefined") || id.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID sản phẩm không hợp lệ");
            return ResponseEntity.badRequest().body(null);
        }
        
        try {
            Integer productId = Integer.parseInt(id);
            return productRepository.findById(productId)
                    .map(ProductDTO::fromEntity)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID sản phẩm phải là số nguyên");
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        // Kiểm tra và xử lý giá trị null, undefined hoặc rỗng
        if (slug == null || slug.equals("undefined") || slug.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "Slug sản phẩm không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        
        return productRepository.findBySlug(slug)
                .map(ProductDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<ProductDTO>> getBestSellingProducts() {
        List<ProductDTO> products = productService.getBestSellingProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductDTO>> getNewArrivals() {
        List<ProductDTO> products = productService.getNewArrivals();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> productPage = productRepository.searchProducts(keyword, pageable);

        List<ProductDTO> products = productPage.getContent().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());

        return ResponseEntity.ok(response);
    }
    
    /**
     * Thêm sản phẩm mới (chỉ dành cho admin)
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @RequestHeader("Authorization") String bearerToken) {
        
        // Xác thực người dùng là admin
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        
        // Log chi tiết thông tin người dùng và token
        log.info("=================================================");
        log.info("THAO TÁC TẠO SẢN PHẨM - THÔNG TIN XÁC THỰC:");
        log.info("Người dùng ID: {}", user.getId());
        log.info("Email: {}", user.getEmail());
        log.info("Tên: {}", user.getName());
        log.info("Role: {}", user.getRole());
        log.info("Token sử dụng: {}", token);
        log.info("=================================================");
        
        // Kiểm tra quyền admin
        if (!user.getRole().equals(UserEntity.Role.ADMIN)) {
            log.warn("Từ chối quyền: Người dùng {} không phải ADMIN", user.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền thêm sản phẩm");
        }
        
        // Tạo sản phẩm mới
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    /**
     * Cập nhật sản phẩm (chỉ dành cho admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDTO productDTO,
            @RequestHeader("Authorization") String bearerToken) {
        
        // Kiểm tra và xử lý giá trị "undefined"
        if (id == null || id.equals("undefined") || id.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID sản phẩm không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Integer productId = Integer.parseInt(id);
            
            // Xác thực người dùng là admin
            String token = TokenUtils.extractToken(bearerToken);
            UserEntity user = authService.getUserByToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
            
            // Kiểm tra quyền admin
            if (!user.getRole().equals(UserEntity.Role.ADMIN)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền cập nhật sản phẩm");
            }
            
            // Cập nhật sản phẩm
            ProductDTO updatedProduct = productService.updateProduct(productId, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID sản phẩm phải là số nguyên");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Xóa sản phẩm (chỉ dành cho admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String id,
            @RequestHeader("Authorization") String bearerToken) {
        
        // Kiểm tra và xử lý giá trị "undefined"
        if (id == null || id.equals("undefined") || id.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID sản phẩm không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Integer productId = Integer.parseInt(id);
            
            // Xác thực người dùng là admin
            String token = TokenUtils.extractToken(bearerToken);
            UserEntity user = authService.getUserByToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
            
            // Kiểm tra quyền admin
            if (!user.getRole().equals(UserEntity.Role.ADMIN)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xóa sản phẩm");
            }
            
            // Xóa sản phẩm
            productService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID sản phẩm phải là số nguyên");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
