package com.uth.newsmanagement.dto.news;

import com.uth.newsmanagement.entity.News;
import java.time.LocalDateTime;

public class NewsResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String thumbnailUrl;
    private Long viewCount;
    private Boolean isPublished;
    private Long categoryId;
    private String categoryName;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NewsResponse() {
    }

    public static NewsResponse fromEntity(News news) {
        NewsResponse res = new NewsResponse();
        res.setId(news.getId());
        res.setTitle(news.getTitle());
        res.setSlug(news.getSlug());
        res.setSummary(news.getSummary());
        res.setContent(news.getContent());
        res.setThumbnailUrl(news.getThumbnailUrl());
        res.setViewCount(news.getViewCount());
        res.setIsPublished(news.getIsPublished());
        res.setCreatedAt(news.getCreatedAt());
        res.setUpdatedAt(news.getUpdatedAt());

        if (news.getCategory() != null) {
            res.setCategoryId(news.getCategory().getId());
            res.setCategoryName(news.getCategory().getName());
        }

        if (news.getAuthor() != null) {
            res.setAuthorUsername(news.getAuthor().getUsername());
        }

        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
