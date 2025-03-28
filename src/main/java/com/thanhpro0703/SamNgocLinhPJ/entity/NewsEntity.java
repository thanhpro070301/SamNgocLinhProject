package com.thanhpro0703.SamNgocLinhPJ.entity;
import com.thanhpro0703.SamNgocLinhPJ.entity.base.BaseEntity;
import com.thanhpro0703.SamNgocLinhPJ.utils.StringFormatterUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Column(name = "image_url")
    @URL(message = "URL hình ảnh không hợp lệ")
    private String imageUrl;

    @PrePersist
    @PreUpdate
    private void formatTitle() {
        this.title = StringFormatterUtil.formatName(this.title);
    }
}
