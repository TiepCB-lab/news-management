package com.uth.newsmanagement.controller;

import com.uth.newsmanagement.common.dto.ApiResponse;
import com.uth.newsmanagement.common.dto.PaginationMeta;
import com.uth.newsmanagement.dto.news.NewsCreateRequest;
import com.uth.newsmanagement.dto.news.NewsResponse;
import com.uth.newsmanagement.dto.news.NewsQueryRequest;
import com.uth.newsmanagement.dto.news.NewsUpdateRequest;
import com.uth.newsmanagement.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Private: Quản lý tin tức dành cho tác giả / quản trị viên.
 * YÊU CẦU: Phải đính kèm Header 'Authorization: Bearer <token>'
 */
@RestController
@RequestMapping("/api/v1/admin/news")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class AdminNewsController {

    private final NewsService newsService;

    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * API Private: Lấy danh sách tin tức quản trị có Tìm kiếm, Phân trang và Sắp xếp
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getAdminNews(
            @Valid @ModelAttribute NewsQueryRequest query
    ) {
        Page<NewsResponse> resultPage = newsService.getAdminNews(
                query.toPageable(), query.getKeyword(), query.getCategoryId());

        PaginationMeta meta = new PaginationMeta(
                query.getPage(),
                query.getLimit(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );

        ApiResponse<List<NewsResponse>> response = ApiResponse.success(
                resultPage.getContent(),
                "Lấy danh sách tin tức quản trị thành công",
                meta
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API Private: Thêm mới tin tức
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NewsResponse>> createNews(
            @Valid @RequestBody NewsCreateRequest request,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        NewsResponse created = newsService.createNews(request, currentUsername);
        ApiResponse<NewsResponse> response = ApiResponse.created(created, "Thêm tin tức mới thành công");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API Private: Cập nhật tin tức
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsResponse>> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody NewsUpdateRequest request
    ) {
        NewsResponse updated = newsService.updateNews(id, request);
        ApiResponse<NewsResponse> response = ApiResponse.success(updated, "Cập nhật tin tức thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * API Private: Xóa tin tức
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Xóa tin tức thành công");
        return ResponseEntity.ok(response);
    }
}
