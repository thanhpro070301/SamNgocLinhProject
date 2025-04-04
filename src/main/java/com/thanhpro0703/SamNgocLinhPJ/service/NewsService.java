package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.NewsEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final NewsRepository newsRepository;

    public List<NewsEntity> getAllNews() {
        log.info("Lấy danh sách tất cả tin tức");
        return newsRepository.findAll();
    }

    public NewsEntity getNewsById(Integer id) {
        log.info("Tìm tin tức với ID: {}", id);
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy tin tức với ID: " + id));
    }

    public NewsEntity createNews(NewsEntity news) {
        log.info("Tạo tin tức mới: {}", news.getTitle());

        // Kiểm tra trùng tiêu đề tin tức trước khi tạo
        if (newsRepository.findAll().stream().anyMatch(n -> n.getTitle().equals(news.getTitle()))) {
            log.warn("Tin tức với tiêu đề '{}' đã tồn tại!", news.getTitle());
            throw new RuntimeException("Tin tức đã tồn tại với tiêu đề này!");
        }

        return newsRepository.save(news);
    }

    public NewsEntity updateNews(Integer id, NewsEntity newsDetails) {
        log.info("Cập nhật tin tức với ID: {}", id);

        NewsEntity news = getNewsById(id); // Tận dụng lại method getNewsById()

        news.setTitle(newsDetails.getTitle());
        news.setContent(newsDetails.getContent());
        news.setImage(newsDetails.getImage());

        return newsRepository.save(news);
    }

    public void deleteNews(Integer id) {
        log.info("Xóa tin tức với ID: {}", id);
        NewsEntity news = getNewsById(id);
        newsRepository.delete(news);
        log.info("Đã xóa tin tức với ID: {}", id);
    }
}
