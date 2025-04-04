package com.thanhpro0703.SamNgocLinhPJ.entity;

import com.thanhpro0703.SamNgocLinhPJ.utils.StringFormatterUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 20, unique = true)
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        formatData();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        formatData();
    }
    
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

    public enum Status {
        NEW, READ, REPLIED
    }
}
