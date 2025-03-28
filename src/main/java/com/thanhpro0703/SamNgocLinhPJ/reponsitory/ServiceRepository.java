package com.thanhpro0703.SamNgocLinhPJ.reponsitory;
import com.thanhpro0703.SamNgocLinhPJ.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    boolean existsByName(String name);
}