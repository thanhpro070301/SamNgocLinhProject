package com.thanhpro0703.SamNgocLinhPJ.repository;

import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    
    Optional<CategoryEntity> findBySlug(String slug);
    
    List<CategoryEntity> findByParentIsNullAndStatus(CategoryEntity.Status status);
    
    List<CategoryEntity> findByStatus(CategoryEntity.Status status);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
} 