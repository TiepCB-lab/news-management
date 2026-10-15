package com.uth.newsmanagement.repository;

import com.uth.newsmanagement.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

    Optional<News> findBySlug(String slug);

    Optional<News> findBySlugAndIsPublishedTrue(String slug);

    Optional<News> findByIdAndIsPublishedTrue(Long id);

    Page<News> findByIsPublishedTrue(Pageable pageable);

    Page<News> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);

    /**
     * Tăng số lượt xem của bài viết
     * TODO: [DATA] Có thể tối ưu bằng Redis cache hoặc batch update nếu lượt xem cao.
     */
    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * Tìm kiếm tin tức theo từ khóa trong tiêu đề hoặc tóm tắt (Public)
     * TODO: [DATA] Tối ưu hóa câu query này bằng Full-text search hoặc native query khi cần.
     */
    @Query("SELECT n FROM News n WHERE n.isPublished = true AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<News> searchPublicNews(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm kiếm tin tức quản trị (Private) hỗ trợ lọc theo keyword và categoryId
     * TODO: [DATA] Sử dụng JpaSpecificationExecutor để viết Dynamic Query sạch hơn.
     */
    @Query("SELECT n FROM News n WHERE " +
           "(:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR n.category.id = :categoryId)")
    Page<News> searchAdminNews(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);
}
