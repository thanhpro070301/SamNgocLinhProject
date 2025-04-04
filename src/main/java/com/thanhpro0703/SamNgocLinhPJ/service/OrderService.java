package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.OrderEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.OrderItemEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ProductEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.OrderItemRepository;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.OrderRepository;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy tất cả đơn hàng
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Lấy đơn hàng theo ID
     */
    @Transactional(readOnly = true)
    public OrderEntity getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng!"));
    }

    /**
     * Lấy đơn hàng của người dùng
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Tạo đơn hàng mới
     */
    @Transactional
    public OrderEntity createOrder(OrderEntity order, Map<Integer, Integer> productQuantities) {
        // Tính tổng tiền dựa trên sản phẩm
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Lưu đơn hàng trước để có ID
        OrderEntity savedOrder = orderRepository.save(order);
        
        // Thêm các sản phẩm vào đơn hàng
        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Không tìm thấy sản phẩm ID: " + productId));
            
            // Kiểm tra số lượng còn
            if (product.getStock() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Sản phẩm " + product.getName() + " chỉ còn " + product.getStock() + " sản phẩm!");
            }
            
            // Tính tiền cho sản phẩm này
            BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemPrice);
            
            // Tạo item cho đơn hàng
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();
            
            // Lưu item
            orderItemRepository.save(orderItem);
            
            // Cập nhật số lượng sản phẩm
            product.setStock(product.getStock() - quantity);
            product.setSold(product.getSold() + quantity);
            productRepository.save(product);
        }
        
        // Cập nhật tổng tiền đơn hàng
        savedOrder.setTotalAmount(totalAmount.add(savedOrder.getShippingFee()));
        return orderRepository.save(savedOrder);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Transactional
    public OrderEntity updateOrderStatus(Integer orderId, OrderEntity.Status status) {
        OrderEntity order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /**
     * Cập nhật trạng thái thanh toán
     */
    @Transactional
    public OrderEntity updatePaymentStatus(Integer orderId, OrderEntity.PaymentStatus paymentStatus) {
        OrderEntity order = getOrderById(orderId);
        order.setPaymentStatus(paymentStatus);
        return orderRepository.save(order);
    }

    /**
     * Hủy đơn hàng
     */
    @Transactional
    public OrderEntity cancelOrder(Integer orderId) {
        OrderEntity order = getOrderById(orderId);
        
        // Chỉ hủy được đơn hàng ở trạng thái PENDING hoặc PROCESSING
        if (order.getStatus() == OrderEntity.Status.SHIPPING || 
            order.getStatus() == OrderEntity.Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Không thể hủy đơn hàng đã vận chuyển hoặc hoàn thành!");
        }
        
        // Hoàn trả số lượng sản phẩm
        for (OrderItemEntity item : order.getOrderItems()) {
            ProductEntity product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            product.setSold(product.getSold() - item.getQuantity());
            productRepository.save(product);
        }
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderEntity.Status.CANCELLED);
        return orderRepository.save(order);
    }
} 