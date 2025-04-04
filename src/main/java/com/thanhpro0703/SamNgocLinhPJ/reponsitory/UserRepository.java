package com.thanhpro0703.SamNgocLinhPJ.reponsitory;

import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<UserEntity> findByEmailAndStatus(String email, UserEntity.Status status);
}

