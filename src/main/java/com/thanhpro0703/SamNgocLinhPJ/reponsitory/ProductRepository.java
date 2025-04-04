package com.thanhpro0703.SamNgocLinhPJ.reponsitory;

import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    
    Optional<ProductEntity> findBySlug(String slug);
    
    Page<ProductEntity> findByCategory(CategoryEntity category, Pageable pageable);
    
    Page<ProductEntity> findByCategoryAndStatus(CategoryEntity category, ProductEntity.Status status, Pageable pageable);
    
    Page<ProductEntity> findByStatus(ProductEntity.Status status, Pageable pageable);
    
    @Query("SELECT p FROM ProductEntity p WHERE p.status = 'ACTIVE' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductEntity> searchProducts(String keyword, Pageable pageable);
    
    List<ProductEntity> findTop8ByStatusOrderBySoldDesc(ProductEntity.Status status);
    
    List<ProductEntity> findTop4ByStatusOrderByCreatedAtDesc(ProductEntity.Status status);
    
    boolean existsByName(String name);
}