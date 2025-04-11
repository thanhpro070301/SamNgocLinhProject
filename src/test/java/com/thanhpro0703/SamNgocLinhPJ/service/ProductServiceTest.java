package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.dto.ProductDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.CategoryRepository;
import com.thanhpro0703.SamNgocLinhPJ.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBestSellingProducts() {
        // Arrange
        ProductEntity product1 = createProductEntity(1, "Product 1", "product-1", 100, ProductEntity.Status.ACTIVE);
        ProductEntity product2 = createProductEntity(2, "Product 2", "product-2", 200, ProductEntity.Status.ACTIVE);
        
        when(productRepository.findTop8ByStatusOrderBySoldDesc(ProductEntity.Status.ACTIVE))
                .thenReturn(Arrays.asList(product1, product2));

        // Act
        List<ProductDTO> result = productService.getBestSellingProducts();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
        
        verify(productRepository, times(1)).findTop8ByStatusOrderBySoldDesc(ProductEntity.Status.ACTIVE);
    }

    @Test
    void testGetNewArrivals() {
        // Arrange
        ProductEntity product1 = createProductEntity(1, "New Product 1", "new-product-1", 50, ProductEntity.Status.ACTIVE);
        ProductEntity product2 = createProductEntity(2, "New Product 2", "new-product-2", 60, ProductEntity.Status.ACTIVE);
        
        when(productRepository.findTop4ByStatusOrderByCreatedAtDesc(ProductEntity.Status.ACTIVE))
                .thenReturn(Arrays.asList(product1, product2));

        // Act
        List<ProductDTO> result = productService.getNewArrivals();

        // Assert
        assertEquals(2, result.size());
        assertEquals("New Product 1", result.get(0).getName());
        assertEquals("New Product 2", result.get(1).getName());
        
        verify(productRepository, times(1)).findTop4ByStatusOrderByCreatedAtDesc(ProductEntity.Status.ACTIVE);
    }

    @Test
    void testGetProductsByCategory() {
        // Arrange
        CategoryEntity category = new CategoryEntity();
        category.setId(1);
        category.setName("Test Category");
        category.setSlug("test-category");
        
        ProductEntity product1 = createProductEntity(1, "Category Product 1", "category-product-1", 30, ProductEntity.Status.ACTIVE);
        ProductEntity product2 = createProductEntity(2, "Category Product 2", "category-product-2", 40, ProductEntity.Status.ACTIVE);
        
        Page<ProductEntity> productPage = new PageImpl<>(Arrays.asList(product1, product2));
        
        when(categoryRepository.findBySlug("test-category")).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryAndStatus(eq(category), eq(ProductEntity.Status.ACTIVE), any(Pageable.class)))
                .thenReturn(productPage);

        // Act
        Page<ProductDTO> result = productService.getProductsByCategory("test-category", PageRequest.of(0, 10));

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("Category Product 1", result.getContent().get(0).getName());
        assertEquals("Category Product 2", result.getContent().get(1).getName());
        
        verify(categoryRepository, times(1)).findBySlug("test-category");
        verify(productRepository, times(1)).findByCategoryAndStatus(eq(category), eq(ProductEntity.Status.ACTIVE), any(Pageable.class));
    }
    
    @Test
    void testGetProductBySlug() {
        // Arrange
        ProductEntity product = createProductEntity(1, "Test Product", "test-product", 50, ProductEntity.Status.ACTIVE);
        when(productRepository.findBySlug("test-product")).thenReturn(Optional.of(product));

        // Act
        Optional<ProductDTO> result = productService.getProductBySlug("test-product");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        
        verify(productRepository, times(1)).findBySlug("test-product");
    }
    
    @Test
    void testGetProductBySlug_NotFound() {
        // Arrange
        when(productRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        // Act
        Optional<ProductDTO> result = productService.getProductBySlug("non-existent");

        // Assert
        assertFalse(result.isPresent());
        
        verify(productRepository, times(1)).findBySlug("non-existent");
    }

    // Helper method to create a ProductEntity for testing
    private ProductEntity createProductEntity(Integer id, String name, String slug, int sold, ProductEntity.Status status) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setSlug(slug);
        product.setDescription("Description for " + name);
        product.setPrice(BigDecimal.valueOf(100.00));
        // Không có setSalePrice trong ProductEntity, thay vào đó dùng setOriginalPrice
        product.setOriginalPrice(BigDecimal.valueOf(90.00));
        product.setStock(100);
        product.setSold(sold);
        product.setStatus(status);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
} 