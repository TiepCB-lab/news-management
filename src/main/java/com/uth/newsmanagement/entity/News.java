package com.uth.newsmanagement.entity;

import jakarta.persistence.*;

/**
 * Entity Tin tức (News).
 * Trọng tâm của bài toán quản lý tin tức (Nhiệm vụ 17).
 *
 * Chiến lược đánh chỉ mục (Indexing Strategy) — V2 Migration:
 *  - Composite index (is_published + created_at): tối ưu public listing mặc định
 *  - Composite index (is_published + category_id + created_at): tối ưu lọc theo chuyên mục
 *  - Composite index (is_published + view_count): tối ưu bảng xếp hạng
 *  - Composite index (category_id + created_at): tối ưu truy vấn admin
 *  - Index (author_id): tránh sequential scan trên FK
 *  - GIN index (fts_vector): tối ưu Full-Text Search PostgreSQL
 */
@Entity
@Table(name = "news", indexes = {
    // --- Single-column indexes ---
    @Index(name = "idx_news_slug",        columnList = "slug"),
    @Index(name = "idx_news_created_at",  columnList = "created_at"),
    @Index(name = "idx_news_view_count",  columnList = "view_count"),
    @Index(name = "idx_news_published",   columnList = "is_published"),
    @Index(name = "idx_news_author_id",   columnList = "author_id"),
    // --- Composite indexes (được bổ sung qua V2 Flyway migration) ---
    @Index(name = "idx_news_published_created",  columnList = "is_published, created_at"),
    @Index(name = "idx_news_published_category", columnList = "is_published, category_id, created_at"),
    @Index(name = "idx_news_published_views",    columnList = "is_published, view_count"),
    @Index(name = "idx_news_category_created",   columnList = "category_id, created_at")
})
public class News extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(length = 500)
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    public News() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
