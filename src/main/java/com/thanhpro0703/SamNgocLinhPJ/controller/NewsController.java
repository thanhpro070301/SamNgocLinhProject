package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.entity.NewsEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<NewsEntity> newsPage = newsRepository.findByPublishedTrue(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsPage.getContent());
        response.put("currentPage", newsPage.getNumber());
        response.put("totalItems", newsPage.getTotalElements());
        response.put("totalPages", newsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsEntity> getNewsById(@PathVariable Integer id) {
        return newsRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<NewsEntity> getNewsBySlug(@PathVariable String slug) {
        return newsRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishDate"));
        Page<NewsEntity> newsPage = newsRepository.findByCategory(category, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsPage.getContent());
        response.put("currentPage", newsPage.getNumber());
        response.put("totalItems", newsPage.getTotalElements());
        response.put("totalPages", newsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<NewsEntity>> getLatestNews() {
        List<NewsEntity> latestNews = newsRepository.findTop5ByPublishedTrueOrderByPublishDateDesc();
        return ResponseEntity.ok(latestNews);
    }
}
