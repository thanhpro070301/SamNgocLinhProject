package com.thanhpro0703.SamNgocLinhPJ.repository;

import com.thanhpro0703.SamNgocLinhPJ.entity.ContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Integer> {
    
    Page<ContactEntity> findByStatus(ContactEntity.Status status, Pageable pageable);
    
    long countByStatus(ContactEntity.Status status);
} 