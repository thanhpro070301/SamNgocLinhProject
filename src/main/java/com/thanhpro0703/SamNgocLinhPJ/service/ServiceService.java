package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.ServiceEntity;
import com.thanhpro0703.SamNgocLinhPJ.exception.DuplicateServiceException;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // Thêm logging
@Validated // Đảm bảo dữ liệu hợp lệ khi nhận vào
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public List<ServiceEntity> getAllServices() {
        log.info("Lấy danh sách tất cả dịch vụ");
        return serviceRepository.findAll();
    }

    public ServiceEntity getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy dịch vụ với ID: {}", id);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy dịch vụ với ID: " + id);
                });
    }

    @Transactional
    public ServiceEntity createService(ServiceEntity service) {
        if (serviceRepository.existsByName(service.getName())) {
            log.warn("Dịch vụ '{}' đã tồn tại!", service.getName());
            throw new DuplicateServiceException("Dịch vụ đã tồn tại!");
        }
        log.info("Tạo dịch vụ mới: {}", service.getName());
        return serviceRepository.save(service);
    }

    @Transactional
    public ServiceEntity updateService(Long id, ServiceEntity serviceDetails) {
        ServiceEntity service = getServiceById(id);

        if (serviceDetails.getName() != null) {
            service.setName(serviceDetails.getName());
        }
        if (serviceDetails.getDescription() != null) {
            service.setDescription(serviceDetails.getDescription());
        }

        log.info("Cập nhật dịch vụ ID: {}", id);
        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(Long id) {
        ServiceEntity service = getServiceById(id);
        serviceRepository.delete(service);
        log.info("Đã xóa dịch vụ với ID: {}", id);
    }
}
