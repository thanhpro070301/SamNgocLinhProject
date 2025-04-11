package com.thanhpro0703.SamNgocLinhPJ.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhpro0703.SamNgocLinhPJ.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private Integer id;
    
    private Long userId;
    
    private BigDecimal totalAmount;
    
    @NotNull(message = "Phí vận chuyển không được để trống")
    @Positive(message = "Phí vận chuyển phải lớn hơn 0")
    private BigDecimal shippingFee;
    
    private OrderEntity.Status status;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private OrderEntity.PaymentMethod paymentMethod;
    
    private OrderEntity.PaymentStatus paymentStatus;
    
    @NotBlank(message = "Tên người nhận không được để trống")
    private String shippingName;
    
    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String shippingPhone;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;
    
    private String shippingEmail;
    
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
    
    // Danh sách sản phẩm trong đơn hàng và số lượng tương ứng (sử dụng khi tạo đơn hàng)
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    private Map<Integer, Integer> products;
    
    // Chuyển đổi từ DTO sang Entity
    public OrderEntity toEntity() {
        return OrderEntity.builder()
                .id(id)
                .shippingFee(shippingFee)
                .paymentMethod(paymentMethod)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .shippingEmail(shippingEmail)
                .notes(notes)
                .build();
    }
    
    // Chuyển đổi từ Entity sang DTO (sử dụng khi trả về kết quả)
    public static OrderDTO fromEntity(OrderEntity order) {
        Long userId = (order.getUser() != null) ? order.getUser().getId() : null;
        return OrderDTO.builder()
                .id(order.getId())
                .userId(userId)
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingEmail(order.getShippingEmail())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
} 