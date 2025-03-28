package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.NewsDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.NewsEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    // Lấy danh sách tin tức
    @GetMapping
    public ResponseEntity<List<NewsEntity>> getAllNews() {
        List<NewsEntity> newsList = newsService.getAllNews();
        return ResponseEntity.ok(newsList);
    }

    // Lấy tin tức theo ID
    @GetMapping("/{id}")
    public ResponseEntity<NewsEntity> getNewsById(@PathVariable Long id) {
        NewsEntity news = newsService.getNewsById(id);
        return ResponseEntity.ok(news);
    }

    // Tạo tin tức mới
    @PostMapping
    public ResponseEntity<NewsEntity> createNews(@Valid @RequestBody NewsDTO newsDTO) {
        NewsEntity createdNews = newsService.createNews(newsDTO.toEntity());
        return ResponseEntity.created(URI.create("/api/news/" + createdNews.getId()))
                .body(createdNews);
    }

    // Cập nhật tin tức
    @PutMapping("/{id}")
    public ResponseEntity<NewsEntity> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody NewsDTO newsDTO) {
        NewsEntity updatedNews = newsService.updateNews(id, newsDTO.toEntity());
        return ResponseEntity.ok(updatedNews);
    }

    // Xóa tin tức
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}
