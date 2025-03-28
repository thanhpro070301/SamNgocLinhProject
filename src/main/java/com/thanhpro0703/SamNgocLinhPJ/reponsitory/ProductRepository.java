package com.thanhpro0703.SamNgocLinhPJ.reponsitory;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsByName(String name);
}