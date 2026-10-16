package com.uth.newsmanagement.service;

import com.uth.newsmanagement.common.exception.BadRequestException;
import com.uth.newsmanagement.common.exception.ResourceNotFoundException;
import com.uth.newsmanagement.dto.news.NewsCreateRequest;
import com.uth.newsmanagement.dto.news.NewsResponse;
import com.uth.newsmanagement.dto.news.NewsUpdateRequest;
import com.uth.newsmanagement.entity.Category;
import com.uth.newsmanagement.entity.News;
import com.uth.newsmanagement.entity.User;
import com.uth.newsmanagement.repository.CategoryRepository;
import com.uth.newsmanagement.repository.NewsRepository;
import com.uth.newsmanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Xử lý nghiệp vụ Tin tức (Nhiệm vụ trọng tâm cho cả Backend và Data Specialist).
 */
@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public NewsService(NewsRepository newsRepository,
                       CategoryRepository categoryRepository,
                       UserRepository userRepository) {
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // PUBLIC APIS (Đọc tin tức cho người dùng)
    // ==========================================

    /**
     * Lấy danh sách tin tức đã xuất bản, hỗ trợ tìm kiếm và lọc theo chuyên mục.
     *
     * <p>Chiến lược tìm kiếm theo từ khóa:
     * <ul>
     *   <li>Keyword ≥ 2 ký tự → dùng {@code PostgreSQL Full-Text Search} (GIN index, O(log N))</li>
     *   <li>Keyword = 1 ký tự  → fallback {@code LIKE} (tránh lỗi tsquery với chuỗi đơn)</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public Page<NewsResponse> getPublicNews(Pageable pageable, String keyword, Long categoryId) {
        Page<News> newsPage;
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            // Sử dụng FTS khi keyword đủ dài (>=2 ký tự) để tận dụng GIN index
            if (trimmed.length() >= 2) {
                newsPage = newsRepository.fullTextSearchPublicNews(trimmed, pageable);
            } else {
                // Fallback LIKE cho keyword 1 ký tự
                newsPage = newsRepository.searchPublicNewsLike(trimmed, pageable);
            }
        } else if (categoryId != null) {
            newsPage = newsRepository.findByCategoryIdAndIsPublishedTrue(categoryId, pageable);
        } else {
            newsPage = newsRepository.findByIsPublishedTrue(pageable);
        }
        return newsPage.map(NewsResponse::fromEntity);
    }

    /**
     * Xem chi tiết tin tức theo slug hoặc ID & Tự động tăng lượt xem
     */
    @Transactional
    public NewsResponse getPublicNewsDetail(String slugOrId) {
        News news;
        try {
            Long id = Long.parseLong(slugOrId);
            news = newsRepository.findByIdAndIsPublishedTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Tin tức", "id", id));
        } catch (NumberFormatException e) {
            news = newsRepository.findBySlugAndIsPublishedTrue(slugOrId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tin tức", "slug", slugOrId));
        }

        // Tăng lượt xem
        newsRepository.incrementViewCount(news.getId());
        news.setViewCount(news.getViewCount() + 1);

        return NewsResponse.fromEntity(news);
    }

    // ==========================================
    // PRIVATE APIS (Quản trị tin tức: Thêm, sửa, xóa, tìm kiếm nâng cao)
    // ==========================================

    /**
     * Lấy danh sách tin tức quản trị (bao gồm cả tin chưa xuất bản)
     * TODO: [DATA] Bổ sung thêm Specification lọc theo khoảng ngày tạo (date range)
     */
    @Transactional(readOnly = true)
    public Page<NewsResponse> getAdminNews(Pageable pageable, String keyword, Long categoryId) {
        Page<News> newsPage = newsRepository.searchAdminNews(
                StringUtils.hasText(keyword) ? keyword.trim() : null,
                categoryId,
                pageable
        );
        return newsPage.map(NewsResponse::fromEntity);
    }

    /**
     * Thêm tin tức mới
     */
    @Transactional
    public NewsResponse createNews(NewsCreateRequest request, String authorUsername) {
        User author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Tác giả", "username", authorUsername));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Chuyên mục", "id", request.getCategoryId()));

        News news = new News();
        news.setTitle(request.getTitle().trim());
        news.setSlug(generateUniqueSlug(request.getTitle()));
        news.setSummary(request.getSummary());
        news.setContent(request.getContent());
        news.setThumbnailUrl(request.getThumbnailUrl());
        news.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : true);
        news.setViewCount(0L);
        news.setCategory(category);
        news.setAuthor(author);

        News saved = newsRepository.save(news);
        return NewsResponse.fromEntity(saved);
    }

    /**
     * Cập nhật tin tức
     */
    @Transactional
    public NewsResponse updateNews(Long id, NewsUpdateRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tức", "id", id));

        if (StringUtils.hasText(request.getTitle()) && !request.getTitle().trim().equalsIgnoreCase(news.getTitle())) {
            news.setTitle(request.getTitle().trim());
            news.setSlug(generateUniqueSlug(request.getTitle()));
        }

        if (request.getSummary() != null) {
            news.setSummary(request.getSummary());
        }

        if (StringUtils.hasText(request.getContent())) {
            news.setContent(request.getContent());
        }

        if (request.getThumbnailUrl() != null) {
            news.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getIsPublished() != null) {
            news.setIsPublished(request.getIsPublished());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chuyên mục", "id", request.getCategoryId()));
            news.setCategory(category);
        }

        News updated = newsRepository.save(news);
        return NewsResponse.fromEntity(updated);
    }

    /**
     * Xóa tin tức
     */
    @Transactional
    public void deleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tin tức", "id", id);
        }
        newsRepository.deleteById(id);
    }

    /**
     * Tạo slug tiếng Việt thân thiện chuẩn SEO từ tiêu đề bài viết
     */
    private String generateUniqueSlug(String input) {
        String baseSlug = toSlug(input);
        String uniqueSlug = baseSlug;
        int count = 1;

        while (newsRepository.findBySlug(uniqueSlug).isPresent()) {
            uniqueSlug = baseSlug + "-" + count;
            count++;
        }
        return uniqueSlug;
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null) return UUID.randomUUID().toString();
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        slug = slug.replace("đ", "d").replace("Đ", "d");
        slug = NONLATIN.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
