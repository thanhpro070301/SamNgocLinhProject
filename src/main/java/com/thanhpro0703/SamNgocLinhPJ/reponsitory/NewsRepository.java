package com.thanhpro0703.SamNgocLinhPJ.reponsitory;

import com.thanhpro0703.SamNgocLinhPJ.entity.NewsEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Integer> {
    
    Optional<NewsEntity> findBySlug(String slug);
    
    Page<NewsEntity> findByPublishedTrue(Pageable pageable);
    
    Page<NewsEntity> findByCategory(String category, Pageable pageable);
    
    Page<NewsEntity> findByAuthor(UserEntity author, Pageable pageable);
    
    List<NewsEntity> findTop5ByPublishedTrueOrderByPublishDateDesc();
}