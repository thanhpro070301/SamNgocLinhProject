package com.thanhpro0703.SamNgocLinhPJ.reponsitory;

import com.thanhpro0703.SamNgocLinhPJ.entity.OrderEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {
    
    Page<OrderEntity> findByUser(UserEntity user, Pageable pageable);
    
    List<OrderEntity> findByUserAndStatus(UserEntity user, OrderEntity.Status status);
    
    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.status = 'PENDING'")
    long countPendingOrders();
    
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.status = 'COMPLETED'")
    long countCompletedOrders();

    @Query("SELECT o FROM OrderEntity o WHERE o.user.id = :userId")
    List<OrderEntity> findByUserId(Long userId);
} 