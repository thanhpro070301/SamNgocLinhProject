package com.thanhpro0703.SamNgocLinhPJ.reponsitory;
import com.thanhpro0703.SamNgocLinhPJ.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Long> {
    boolean existsByTitle(String title);
}