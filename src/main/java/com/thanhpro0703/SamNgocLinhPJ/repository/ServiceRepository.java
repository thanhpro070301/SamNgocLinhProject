package com.thanhpro0703.SamNgocLinhPJ.repository;

import com.thanhpro0703.SamNgocLinhPJ.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {
    
    List<ServiceEntity> findByStatus(ServiceEntity.Status status);
    
    boolean existsByName(String name);
} 