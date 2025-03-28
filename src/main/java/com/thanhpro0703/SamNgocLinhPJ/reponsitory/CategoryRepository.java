package com.thanhpro0703.SamNgocLinhPJ.reponsitory;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String title);
}
