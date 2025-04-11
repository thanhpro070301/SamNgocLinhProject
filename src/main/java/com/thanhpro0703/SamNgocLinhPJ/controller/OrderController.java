package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.OrderDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.OrderEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.AuthService;
import com.thanhpro0703.SamNgocLinhPJ.service.OrderService;
import com.thanhpro0703.SamNgocLinhPJ.service.UserService;
import com.thanhpro0703.SamNgocLinhPJ.utils.TokenUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserService userService;
    private final AuthService authService;
    
    /**
     * Lấy tất cả đơn hàng (chỉ dành cho admin)
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestHeader("Authorization") String bearerToken) {
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        
        // Kiểm tra quyền admin
        if (!user.getRole().equals(UserEntity.Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập");
        }
        
        List<OrderDTO> orders = orderService.getAllOrders().stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Lấy đơn hàng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable String id,
            @RequestHeader("Authorization") String bearerToken) {
        
        // Kiểm tra và xử lý giá trị "undefined"
        if (id == null || id.equals("undefined") || id.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID đơn hàng không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Integer orderId = Integer.parseInt(id);
            
            String token = TokenUtils.extractToken(bearerToken);
            UserEntity user = authService.getUserByToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
            
            OrderEntity order = orderService.getOrderById(orderId);
            
            // Kiểm tra quyền truy cập (chỉ admin hoặc chủ đơn hàng mới có quyền xem)
            if (!user.getRole().equals(UserEntity.Role.ADMIN) && !order.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập đơn hàng này");
            }
            
            return ResponseEntity.ok(OrderDTO.fromEntity(order));
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID đơn hàng phải là số nguyên");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Lấy đơn hàng của người dùng hiện tại
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders(@RequestHeader("Authorization") String bearerToken) {
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        
        List<OrderDTO> orders = orderService.getOrdersByUserId(user.getId()).stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Tạo đơn hàng mới
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                        "Bạn cần đăng nhập để đặt hàng. Vui lòng đăng nhập hoặc tạo tài khoản trước khi thanh toán."));
        
        // Chuyển từ DTO sang Entity
        OrderEntity orderEntity = orderDTO.toEntity();
        orderEntity.setUser(user);
        
        // Tạo đơn hàng mới
        OrderEntity savedOrder = orderService.createOrder(orderEntity, orderDTO.getProducts());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderDTO.fromEntity(savedOrder));
    }
    
    /**
     * Cập nhật trạng thái đơn hàng (chỉ dành cho admin)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam OrderEntity.Status status,
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        
        // Kiểm tra quyền admin
        if (!user.getRole().equals(UserEntity.Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền cập nhật trạng thái đơn hàng");
        }
        
        OrderEntity updatedOrder = orderService.updateOrderStatus(id, status);
        
        return ResponseEntity.ok(OrderDTO.fromEntity(updatedOrder));
    }
    
    /**
     * Cập nhật trạng thái thanh toán (chỉ dành cho admin)
     */
    @PutMapping("/{id}/payment")
    public ResponseEntity<OrderDTO> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestParam OrderEntity.PaymentStatus paymentStatus,
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        
        // Kiểm tra quyền admin
        if (!user.getRole().equals(UserEntity.Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền cập nhật trạng thái thanh toán");
        }
        
        OrderEntity updatedOrder = orderService.updatePaymentStatus(id, paymentStatus);
        
        return ResponseEntity.ok(OrderDTO.fromEntity(updatedOrder));
    }
    
    /**
     * Hủy đơn hàng
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String bearerToken) {
        
        String token = TokenUtils.extractToken(bearerToken);
        UserEntity user = authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        
        OrderEntity order = orderService.getOrderById(id);
        
        // Kiểm tra quyền truy cập (chỉ admin hoặc chủ đơn hàng mới có quyền hủy)
        if (!user.getRole().equals(UserEntity.Role.ADMIN) && !order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền hủy đơn hàng này");
        }
        
        OrderEntity cancelledOrder = orderService.cancelOrder(id);
        
        return ResponseEntity.ok(OrderDTO.fromEntity(cancelledOrder));
    }
    
    /**
     * Kiểm tra trạng thái đơn hàng (endpoint công khai)
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicOrderStatus(@PathVariable String id) {
        // Kiểm tra và xử lý giá trị "undefined"
        if (id == null || id.equals("undefined") || id.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID đơn hàng không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Integer orderId = Integer.parseInt(id);
            OrderEntity order = orderService.getOrderById(orderId);
            
            // Tạo một phiên bản ẩn danh của đơn hàng chỉ với các thông tin công khai
            OrderDTO publicOrderDTO = OrderDTO.builder()
                    .id(order.getId())
                    .status(order.getStatus())
                    .paymentStatus(order.getPaymentStatus())
                    .createdAt(order.getCreatedAt())
                    .build();
            
            return ResponseEntity.ok(publicOrderDTO);
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "BAD_REQUEST");
            error.put("message", "ID đơn hàng phải là số nguyên");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 