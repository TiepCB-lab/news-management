package com.uth.newsmanagement.entity;

import jakarta.persistence.*;

/**
 * Entity Chuyên mục / Thể loại tin tức (Category).
 * 
 * TODO: [DATA] Bổ sung thêm các index hoặc ràng buộc nếu cần (ví dụ: slug unique).
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_slug", columnList = "slug")
})
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 255)
    private String description;

    public Category() {
    }

    public Category(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
