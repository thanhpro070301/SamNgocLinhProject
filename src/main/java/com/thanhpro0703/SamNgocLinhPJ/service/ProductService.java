package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.dto.ProductDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductImageEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.CategoryRepository;
import com.thanhpro0703.SamNgocLinhPJ.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Lấy danh sách tất cả sản phẩm (Chuyển đổi sang DTO)
    @Transactional(readOnly = true)
    @Cacheable(value = "products")
    public List<ProductDTO> getAllProducts() {
        log.info("Lấy danh sách tất cả sản phẩm");
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntity) // Convert Entity → DTO
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID (Chuyển đổi sang DTO)
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(Integer id) {
        log.info("Tìm sản phẩm với ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm với ID: " + id));
        return ProductDTO.fromEntity(product);
    }

    // Tạo sản phẩm mới (Dùng DTO)
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "bestSellingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true)
    })
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Tạo sản phẩm mới: {}", productDTO.getName());

        // Kiểm tra trùng tên sản phẩm
        if (productRepository.existsByName(productDTO.getName())) {
            log.warn("Sản phẩm '{}' đã tồn tại!", productDTO.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm đã tồn tại!");
        }

        // Kiểm tra tên không được null hoặc rỗng
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên sản phẩm không được để trống");
        }

        // Tạo slug tự động nếu không có
        if (productDTO.getSlug() == null || productDTO.getSlug().trim().isEmpty()) {
            String slug = productDTO.getName().toLowerCase()
                    .replaceAll("\\s+", "-")
                    .replaceAll("[^a-z0-9-]", "");
            productDTO.setSlug(slug);
        }

        // Lấy danh mục từ DB
        CategoryEntity category = findCategoryById(productDTO.getCategoryId());

        // Xử lý trạng thái mặc định là ACTIVE
        ProductEntity.Status status = ProductEntity.Status.ACTIVE;
        
        // Nếu số lượng = 0, tự động chuyển trạng thái sang INACTIVE
        if (productDTO.getStock() != null && productDTO.getStock() == 0) {
            status = ProductEntity.Status.INACTIVE;
            productDTO.setStatus("INACTIVE"); // Đảm bảo DTO cũng có giá trị đúng
            log.info("Số lượng sản phẩm = 0, tự động chuyển trạng thái sang INACTIVE");
        }
        
        // Xử lý status khi được chỉ định trong DTO
        if (productDTO.getStatus() != null && !productDTO.getStatus().trim().isEmpty()) {
            try {
                String statusValue = productDTO.getStatus().trim().toUpperCase();
                switch (statusValue) {
                    case "ACTIVE":
                        status = ProductEntity.Status.ACTIVE;
                        break;
                    case "INACTIVE":
                        status = ProductEntity.Status.INACTIVE;
                        break;
                    default:
                        log.warn("Trạng thái không hợp lệ: {}. Sử dụng ACTIVE làm mặc định.", productDTO.getStatus());
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Trạng thái không hợp lệ: " + productDTO.getStatus() + 
                            ". Giá trị hợp lệ: ACTIVE, INACTIVE");
                }
            } catch (Exception e) {
                if (e instanceof ResponseStatusException) {
                    throw e;
                }
                log.warn("Lỗi xử lý trạng thái: {}. Sử dụng ACTIVE làm mặc định.", e.getMessage());
                status = ProductEntity.Status.ACTIVE;
            }
        }

        // Kiểm tra giá không được null
        if (productDTO.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá sản phẩm không được để trống");
        }

        // Chuyển DTO → Entity rồi lưu vào DB
        ProductEntity newProduct = ProductEntity.builder()
                .name(productDTO.getName())
                .slug(productDTO.getSlug())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .originalPrice(productDTO.getOriginalPrice())
                .image(productDTO.getImage())
                .category(category)
                .stock(productDTO.getStock() != null ? productDTO.getStock() : 0)
                .status(status)
                .build();
        
        try {
            ProductEntity savedProduct = productRepository.save(newProduct);
            
            // Tạo ProductImageEntity cho trường image chính
            if (savedProduct.getImage() != null && !savedProduct.getImage().trim().isEmpty()) {
                ProductImageEntity primaryImage = ProductImageEntity.builder()
                        .product(savedProduct)
                        .imagePath(savedProduct.getImage())
                        .isPrimary(true)
                        .build();
                
                // Thêm vào danh sách images của sản phẩm
                if (savedProduct.getImages() == null) {
                    savedProduct.setImages(new ArrayList<>());
                }
                savedProduct.getImages().add(primaryImage);
                productRepository.save(savedProduct);
            }
            
            return ProductDTO.fromEntity(savedProduct);
        } catch (Exception e) {
            log.error("Lỗi khi lưu sản phẩm: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Lỗi khi lưu sản phẩm: " + e.getMessage());
        }
    }

    // Cập nhật sản phẩm
    @Transactional
    @Caching(
        put = { @CachePut(value = "product", key = "#id") },
        evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "bestSellingProducts", allEntries = true),
            @CacheEvict(value = "newArrivals", allEntries = true)
        }
    )
    public ProductDTO updateProduct(Integer id, ProductDTO productDTO) {
        log.info("Cập nhật sản phẩm với ID: {}", id);

        // Lấy sản phẩm cần cập nhật
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new  ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm với ID: " + id));

        // Kiểm tra trùng tên sản phẩm (nếu thay đổi tên)
        if (productDTO.getName() != null && !product.getName().equals(productDTO.getName()) && productRepository.existsByName(productDTO.getName())) {
            log.warn("Tên sản phẩm '{}' đã tồn tại!", productDTO.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tên sản phẩm đã tồn tại!");
        }

        // Kiểm tra tên không được null hoặc rỗng (do có ràng buộc @NotBlank trong entity)
        if (productDTO.getName() != null) {
            if (productDTO.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên sản phẩm không được để trống");
            }
            product.setName(productDTO.getName());
        }
        
        if (productDTO.getSlug() != null) {
            product.setSlug(productDTO.getSlug());
        } else if (productDTO.getName() != null) {
            // Nếu slug không được cung cấp nhưng tên được cập nhật, tự động tạo slug từ tên
            String slug = productDTO.getName().toLowerCase()
                    .replaceAll("\\s+", "-")
                    .replaceAll("[^a-z0-9-]", "");
            product.setSlug(slug);
        }
        
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        
        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }
        
        if (productDTO.getOriginalPrice() != null) {
            product.setOriginalPrice(productDTO.getOriginalPrice());
        }
        
        if (productDTO.getImage() != null) {
            product.setImage(productDTO.getImage());
            
            // Kiểm tra và cập nhật ProductImageEntity tương ứng
            boolean hasPrimaryImage = false;
            if (product.getImages() != null) {
                for (ProductImageEntity img : product.getImages()) {
                    if (img.getIsPrimary()) {
                        // Cập nhật đường dẫn ảnh chính
                        img.setImagePath(productDTO.getImage());
                        hasPrimaryImage = true;
                        break;
                    }
                }
            }
            
            // Nếu chưa có ảnh chính, tạo mới
            if (!hasPrimaryImage) {
                ProductImageEntity primaryImage = ProductImageEntity.builder()
                        .product(product)
                        .imagePath(productDTO.getImage())
                        .isPrimary(true)
                        .build();
                
                // Thêm vào danh sách images của sản phẩm
                if (product.getImages() == null) {
                    product.setImages(new ArrayList<>());
                }
                product.getImages().add(primaryImage);
            }
        }
        
        if (productDTO.getStock() != null) {
            product.setStock(productDTO.getStock());
            
            // Nếu cập nhật số lượng = 0, tự động chuyển trạng thái sang INACTIVE
            if (productDTO.getStock() == 0) {
                product.setStatus(ProductEntity.Status.INACTIVE);
                log.info("Số lượng sản phẩm = 0, tự động chuyển trạng thái sang INACTIVE");
            } else if (product.getStatus() == ProductEntity.Status.INACTIVE && productDTO.getStock() > 0) {
                // Nếu trạng thái hiện tại là INACTIVE và số lượng > 0, chuyển sang ACTIVE
                product.setStatus(ProductEntity.Status.ACTIVE);
                log.info("Sản phẩm có số lượng > 0, chuyển từ INACTIVE sang ACTIVE");
            }
        }
        
        // Cập nhật trạng thái nếu được chỉ định
        if (productDTO.getStatus() != null && !productDTO.getStatus().trim().isEmpty()) {
            try {
                String statusValue = productDTO.getStatus().trim().toUpperCase();
                switch (statusValue) {
                    case "ACTIVE":
                        product.setStatus(ProductEntity.Status.ACTIVE);
                        break;
                    case "INACTIVE":
                        product.setStatus(ProductEntity.Status.INACTIVE);
                        break;
                    default:
                        log.warn("Trạng thái không hợp lệ: {}", productDTO.getStatus());
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Trạng thái không hợp lệ: " + productDTO.getStatus() + 
                            ". Giá trị hợp lệ: ACTIVE, INACTIVE");
                }
            } catch (Exception e) {
                if (e instanceof ResponseStatusException) {
                    throw e;
                }
                log.warn("Lỗi xử lý trạng thái: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Trạng thái không hợp lệ: " + productDTO.getStatus());
            }
        }

        // Cập nhật danh mục nếu có
        if (productDTO.getCategoryId() != null) {
            CategoryEntity category = findCategoryById(productDTO.getCategoryId());
            product.setCategory(category);
        }

        try {
            ProductEntity updatedProduct = productRepository.save(product);
            return ProductDTO.fromEntity(updatedProduct);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    // Xóa sản phẩm
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#id"),
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "bestSellingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true)
    })
    public void deleteProduct(Integer id) {
        log.info("Xóa sản phẩm với ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm với ID: " + id));

        productRepository.delete(product);
        log.info("Đã xóa sản phẩm với ID: {}", id);
    }

    // Tìm danh mục theo ID
    private CategoryEntity findCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy danh mục với ID: " + categoryId));
    }

    // Lấy sản phẩm bán chạy nhất
    @Transactional(readOnly = true)
    @Cacheable(value = "bestSellingProducts")
    public List<ProductDTO> getBestSellingProducts() {
        log.info("Lấy danh sách sản phẩm bán chạy nhất");
        return productRepository.findTop8ByStatusOrderBySoldDesc(ProductEntity.Status.ACTIVE)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm mới nhất
    @Transactional(readOnly = true)
    @Cacheable(value = "newArrivals")
    public List<ProductDTO> getNewArrivals() {
        log.info("Lấy danh sách sản phẩm mới nhất");
        return productRepository.findTop4ByStatusOrderByCreatedAtDesc(ProductEntity.Status.ACTIVE)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Lấy sản phẩm theo slug
    @Transactional(readOnly = true)
    @Cacheable(value = "productBySlug", key = "#slug")
    public Optional<ProductDTO> getProductBySlug(String slug) {
        log.info("Tìm sản phẩm với slug: {}", slug);
        return productRepository.findBySlug(slug)
                .map(ProductDTO::fromEntity);
    }
    
    // Lấy danh sách sản phẩm theo danh mục
    @Transactional(readOnly = true)
    @Cacheable(value = "productsByCategory", key = "#categorySlug + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductDTO> getProductsByCategory(String categorySlug, Pageable pageable) {
        log.info("Lấy danh sách sản phẩm theo danh mục: {}", categorySlug);
        CategoryEntity category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + categorySlug));
                
        Page<ProductEntity> productPage = productRepository.findByCategoryAndStatus(
                category, ProductEntity.Status.ACTIVE, pageable);
                
        return productPage.map(ProductDTO::fromEntity);
    }
}
