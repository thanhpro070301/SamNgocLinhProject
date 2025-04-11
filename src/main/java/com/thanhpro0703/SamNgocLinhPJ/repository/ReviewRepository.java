package com.thanhpro0703.SamNgocLinhPJ.repository;

import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ReviewEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    
    List<ReviewEntity> findByProductAndStatus(ProductEntity product, ReviewEntity.Status status);
    
    Page<ReviewEntity> findByProduct(ProductEntity product, Pageable pageable);
    
    List<ReviewEntity> findByUser(UserEntity user);
    
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.product = :product AND r.status = 'APPROVED'")
    Double getAverageRatingForProduct(ProductEntity product);
} 