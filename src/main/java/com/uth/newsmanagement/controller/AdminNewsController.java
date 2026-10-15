package com.uth.newsmanagement.controller;

import com.uth.newsmanagement.common.constant.AppConstants;
import com.uth.newsmanagement.common.dto.ApiResponse;
import com.uth.newsmanagement.common.dto.PaginationMeta;
import com.uth.newsmanagement.dto.news.NewsCreateRequest;
import com.uth.newsmanagement.dto.news.NewsResponse;
import com.uth.newsmanagement.dto.news.NewsUpdateRequest;
import com.uth.newsmanagement.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Private: Quản lý tin tức dành cho tác giả / quản trị viên.
 * YÊU CẦU: Phải đính kèm Header 'Authorization: Bearer <token>'
 */
@RestController
@RequestMapping("/api/v1/admin/news")
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
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        int pageIndex = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageIndex, limit, sort);

        Page<NewsResponse> resultPage = newsService.getAdminNews(pageable, keyword, categoryId);

        PaginationMeta meta = new PaginationMeta(
                page,
                limit,
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
    public ResponseEntity<ApiResponse<Void>> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Xóa tin tức thành công");
        return ResponseEntity.ok(response);
    }
}
