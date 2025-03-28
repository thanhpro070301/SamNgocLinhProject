package com.thanhpro0703.SamNgocLinhPJ.entity;

import com.thanhpro0703.SamNgocLinhPJ.entity.base.BaseEntity;
import com.thanhpro0703.SamNgocLinhPJ.utils.StringFormatterUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String message;


    @PrePersist
    @PreUpdate
    private void formatData() {
        if (this.name != null) {
                this.name = StringFormatterUtil.formatName(this.name);
        }
        if (this.phone != null) {
            this.phone = this.phone.trim();
        }
        if (this.message != null) {
            this.message = this.message.trim();
        }
    }
}
