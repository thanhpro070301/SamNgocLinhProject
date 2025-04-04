package com.thanhpro0703.SamNgocLinhPJ.entity;

import com.thanhpro0703.SamNgocLinhPJ.entity.base.BaseEntity;
import com.thanhpro0703.SamNgocLinhPJ.utils.StringFormatterUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 255, message = "Tên dịch vụ không được quá 255 ký tự")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    private String duration;

    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @PrePersist
    @PreUpdate
    private void formatServiceName() {
        this.name = StringFormatterUtil.formatName(this.name);
    }

    public enum Status {
        ACTIVE, INACTIVE
    }
}
