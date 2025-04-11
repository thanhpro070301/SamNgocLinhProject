package com.thanhpro0703.SamNgocLinhPJ.repository;

import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    
    Optional<ProductEntity> findBySlug(String slug);
    
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    Page<ProductEntity> findByCategory(CategoryEntity category, Pageable pageable);
    
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    Page<ProductEntity> findByCategoryAndStatus(CategoryEntity category, ProductEntity.Status status, Pageable pageable);
    
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    Page<ProductEntity> findByStatus(ProductEntity.Status status, Pageable pageable);
    
    @QueryHints({
        @QueryHint(name = HINT_CACHEABLE, value = "true"),
        @QueryHint(name = HINT_FETCH_SIZE, value = "50")
    })
    @Query("SELECT p FROM ProductEntity p WHERE p.status = 'ACTIVE' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductEntity> searchProducts(String keyword, Pageable pageable);
    
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @Query("SELECT p FROM ProductEntity p WHERE p.status = :status ORDER BY p.sold DESC")
    List<ProductEntity> findTop8ByStatusOrderBySoldDesc(ProductEntity.Status status);
    
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @Query("SELECT p FROM ProductEntity p WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<ProductEntity> findTop4ByStatusOrderByCreatedAtDesc(ProductEntity.Status status);
    
    boolean existsByName(String name);
}
