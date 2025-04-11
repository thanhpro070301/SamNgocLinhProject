package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhpro0703.SamNgocLinhPJ.config.TestConfig;
import com.thanhpro0703.SamNgocLinhPJ.dto.ProductDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.security.jwt.JwtTokenProvider;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.ProductService;
import com.thanhpro0703.SamNgocLinhPJ.service.UserDetailsServiceImpl;
import com.thanhpro0703.SamNgocLinhPJ.service.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private AuthService authService;
    
    @MockBean
    private UserSessionService userSessionService;
    
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @MockBean
    private AuthenticationManager authenticationManager;

    private ProductDTO product1;
    private ProductDTO product2;

    @BeforeEach
    void setUp() {
        product1 = new ProductDTO();
        product1.setId(1);
        product1.setName("Test Product 1");
        product1.setSlug("test-product-1");
        product1.setDescription("Test Description 1");
        product1.setPrice(BigDecimal.valueOf(100.0));
        product1.setStock(50);

        product2 = new ProductDTO();
        product2.setId(2);
        product2.setName("Test Product 2");
        product2.setSlug("test-product-2");
        product2.setDescription("Test Description 2");
        product2.setPrice(BigDecimal.valueOf(200.0));
        product2.setStock(100);
    }

    @Test
    void testGetBestSellingProducts() throws Exception {
        List<ProductDTO> products = Arrays.asList(product1, product2);
        when(productService.getBestSellingProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products/best-selling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Test Product 1"))
                .andExpect(jsonPath("$[1].name").value("Test Product 2"));
        
        verify(productService, times(1)).getBestSellingProducts();
    }

    @Test
    void testGetNewArrivals() throws Exception {
        List<ProductDTO> products = Arrays.asList(product1, product2);
        when(productService.getNewArrivals()).thenReturn(products);

        mockMvc.perform(get("/api/products/new-arrivals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Test Product 1"))
                .andExpect(jsonPath("$[1].name").value("Test Product 2"));
        
        verify(productService, times(1)).getNewArrivals();
    }

    @Test
    void testGetProductBySlug() throws Exception {
        when(productService.getProductBySlug("test-product-1")).thenReturn(Optional.of(product1));

        mockMvc.perform(get("/api/products/slug/test-product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product 1"))
                .andExpect(jsonPath("$.price").value(100.0));
        
        verify(productService, times(1)).getProductBySlug("test-product-1");
    }

    @Test
    void testGetProductBySlug_NotFound() throws Exception {
        when(productService.getProductBySlug("non-existent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/slug/non-existent"))
                .andExpect(status().isNotFound());
        
        verify(productService, times(1)).getProductBySlug("non-existent");
    }

    @Test
    void testCreateProduct_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isUnauthorized());
        
        verify(productService, never()).createProduct(any());
    }

    @Test
    void testCreateProduct_WithAdminRole() throws Exception {
        // Mock the user with ADMIN role
        UserEntity adminUser = new UserEntity();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserEntity.Role.ADMIN);
        
        when(authService.getUserByToken(anyString())).thenReturn(Optional.of(adminUser));
        when(productService.createProduct(any())).thenReturn(product1);

        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product 1"));
        
        verify(authService, times(1)).getUserByToken(anyString());
        verify(productService, times(1)).createProduct(any());
    }
} 