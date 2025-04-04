package com.thanhpro0703.SamNgocLinhPJ.dto;
import com.thanhpro0703.SamNgocLinhPJ.entity.NewsEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class NewsDTO {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @URL(message = "URL hình ảnh không hợp lệ")
    private String image;

    public NewsEntity toEntity() {
        return NewsEntity.builder()
                .title(this.title)
                .content(this.content)
                .image(this.image)
                .build();
    }
}