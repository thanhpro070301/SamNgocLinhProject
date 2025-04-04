package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.ServiceDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.CategoryEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.ServiceEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService serviceService;

    // Lấy danh sách dịch vụ
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    // Lấy dịch vụ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Integer id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }


    // Tạo dịch vụ mới
    @PostMapping
    public ResponseEntity<ServiceEntity> createService(@Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceEntity createdService = serviceService.createService(serviceDTO.toEntity());
        return ResponseEntity.created(URI.create("/api/services/" + createdService.getId()))
                .body(createdService);
    }

    // Cập nhật dịch vụ
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceEntity updatedService = serviceService.updateService(id, serviceDTO.toEntity());
        return ResponseEntity.ok(updatedService);
    }

    // Xóa dịch vụ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Integer id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
