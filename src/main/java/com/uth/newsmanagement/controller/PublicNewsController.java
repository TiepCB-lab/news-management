package com.uth.newsmanagement.controller;

import com.uth.newsmanagement.common.constant.AppConstants;
import com.uth.newsmanagement.common.dto.ApiResponse;
import com.uth.newsmanagement.common.dto.PaginationMeta;
import com.uth.newsmanagement.dto.news.NewsResponse;
import com.uth.newsmanagement.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Public: Dành cho độc giả đọc tin tức (Không cần Token).
 */
@RestController
@RequestMapping("/api/v1/public/news")
public class PublicNewsController {

    private final NewsService newsService;

    public PublicNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * API Public: Đọc danh sách tin tức có Phân trang, Sắp xếp và Tìm kiếm
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getPublicNews(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        // Đảm bảo page >= 1 từ phía client
        int pageIndex = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageIndex, limit, sort);

        Page<NewsResponse> resultPage = newsService.getPublicNews(pageable, keyword, categoryId);

        PaginationMeta meta = new PaginationMeta(
                page,
                limit,
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );

        ApiResponse<List<NewsResponse>> response = ApiResponse.success(
                resultPage.getContent(),
                "Lấy danh sách tin tức thành công",
                meta
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API Public: Xem chi tiết tin tức (theo slug hoặc id) & tự động tăng lượt xem
     */
    @GetMapping("/{slugOrId}")
    public ResponseEntity<ApiResponse<NewsResponse>> getNewsDetail(@PathVariable String slugOrId) {
        NewsResponse news = newsService.getPublicNewsDetail(slugOrId);
        ApiResponse<NewsResponse> response = ApiResponse.success(news, "Lấy chi tiết tin tức thành công");
        return ResponseEntity.ok(response);
    }
}
