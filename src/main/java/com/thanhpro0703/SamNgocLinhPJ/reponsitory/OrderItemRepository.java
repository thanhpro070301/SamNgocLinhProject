package com.thanhpro0703.SamNgocLinhPJ.reponsitory;

import com.thanhpro0703.SamNgocLinhPJ.entity.OrderEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.OrderItemEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Integer> {
    
    List<OrderItemEntity> findByOrder(OrderEntity order);
    
    List<OrderItemEntity> findByProduct(ProductEntity product);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItemEntity oi WHERE oi.product.id = :productId")
    long countTotalSoldForProduct(Integer productId);
} 