package com.uth.newsmanagement.entity;

import jakarta.persistence.*;

/**
 * Entity Tin tức (News).
 * Trọng tâm của bài toán quản lý tin tức (Nhiệm vụ 17).
 * 
 * TODO: [DATA] Tối ưu hóa truy vấn:
 * - Đánh index cho các trường thường dùng để filter/sort: created_at, view_count, is_published, category_id.
 * - Có thể áp dụng Full-Text Search PostgreSQL (tsvector) hoặc MySQL FULLTEXT index cho title & content.
 */
@Entity
@Table(name = "news", indexes = {
    @Index(name = "idx_news_slug", columnList = "slug"),
    @Index(name = "idx_news_created_at", columnList = "created_at"),
    @Index(name = "idx_news_view_count", columnList = "view_count"),
    @Index(name = "idx_news_published", columnList = "is_published")
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
