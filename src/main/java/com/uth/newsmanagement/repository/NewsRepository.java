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

/**
 * Repository tầng truy xuất dữ liệu tin tức.
 *
 * Chiến lược tìm kiếm toàn văn (Full-Text Search):
 * - Sử dụng cột {@code fts_vector} STORED GENERATED (V2 Flyway migration) kết
 * hợp GIN index.
 * - Native query gọi trực tiếp hàm {@code to_tsquery('simple', ...)} của
 * PostgreSQL.
 * - Hỗ trợ fallback LIKE khi FTS không phù hợp (ví dụ: cụm từ ngắn, ký tự đặc
 * biệt).
 *
 * Chiến lược lọc động (Dynamic Filtering):
 * - Implement {@link JpaSpecificationExecutor} cho phép xây dựng Specification
 * linh hoạt.
 * - Admin search sử dụng JPQL với điều kiện nullable để tránh viết nhiều method
 * variants.
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

     Optional<News> findBySlug(String slug);

     Optional<News> findBySlugAndIsPublishedTrue(String slug);

     Optional<News> findByIdAndIsPublishedTrue(Long id);

     /**
      * Tăng số lượt xem của bài viết theo nguyên tắc nguyên tử (Atomic Increment).
      * Lưu ý: Nếu lưu lượng lượt xem rất lớn, cân nhắc sử dụng Redis INCR + batch
      * flush.
      */
     @Modifying
     @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
     void incrementViewCount(@Param("id") Long id);

     /**
      * Tìm kiếm tin tức đã xuất bản bằng PostgreSQL Full-Text Search (tsvector /
      * tsquery).
      *
      * <p>
      * Ưu điểm so với LIKE:
      * <ul>
      * <li>Sử dụng GIN index {@code idx_news_fts_vector} → tốc độ O(log N) thay vì
      * O(N)</li>
      * <li>Hỗ trợ prefix matching: 'spring:*' tìm được "Spring Boot", "Spring
      * Security"...</li>
      * <li>Loại bỏ stop words và normalize text tự động</li>
      * </ul>
      *
      * <p>
      * Cú pháp tsquery: từ khóa được nối bằng {@code &} (AND), {@code |} (OR),
      * {@code !} (NOT).
      * Hàm {@code plainto_tsquery} tự động parse chuỗi tự nhiên thành tsquery an
      * toàn.
      *
      * @param keyword  Từ khóa tìm kiếm (chuỗi tự nhiên, ví dụ: "spring boot java")
      * @param pageable Cấu hình phân trang và sắp xếp
      * @return Trang kết quả tin tức khớp với từ khóa, sắp xếp theo độ liên quan
      */
     @Query(value = """
               SELECT n.*
               FROM news n
               WHERE n.is_published = true
                 AND n.fts_vector @@ plainto_tsquery('simple', :keyword)
               ORDER BY ts_rank(n.fts_vector, plainto_tsquery('simple', :keyword)) DESC,
                        n.created_at DESC
               """, countQuery = """
               SELECT COUNT(*)
               FROM news n
               WHERE n.is_published = true
                 AND n.fts_vector @@ plainto_tsquery('simple', :keyword)
               """, nativeQuery = true)
     Page<News> fullTextSearchPublicNews(@Param("keyword") String keyword, Pageable pageable);

     /**
      * Fallback tìm kiếm bằng LIKE — dùng khi keyword quá ngắn (&lt;2 ký tự) hoặc
      * chứa ký tự đặc biệt không phù hợp với tsquery.
      *
      * @param keyword  Từ khóa tìm kiếm (case-insensitive)
      * @param pageable Cấu hình phân trang
      * @return Trang kết quả tin tức khớp
      */
     @Query("SELECT n FROM News n WHERE n.isPublished = true AND " +
               "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
               "LOWER(n.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
     Page<News> searchPublicNewsLike(@Param("keyword") String keyword, Pageable pageable);

     /**
      * Tìm kiếm tin tức quản trị (Private) — hỗ trợ lọc động theo keyword và
      * categoryId.
      *
      * <p>
      * Sử dụng điều kiện {@code :keyword IS NULL} để một query xử lý cả 4 tổ hợp
      * (có/không keyword, có/không categoryId) mà không cần viết 4 method riêng
      * biệt.
      * Đây là pattern tương đương với {@link JpaSpecificationExecutor} nhưng nhẹ hơn
      * cho trường hợp chỉ có 2 tham số lọc.
      *
      * @param keyword    Từ khóa trong tiêu đề (nullable)
      * @param categoryId ID chuyên mục (nullable)
      * @param pageable   Cấu hình phân trang và sắp xếp
      * @return Trang kết quả tin tức (bao gồm cả tin chưa xuất bản)
      */
     @Query("SELECT n FROM News n WHERE " +
               "(:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
               "(:categoryId IS NULL OR n.category.id = :categoryId)")
     Page<News> searchAdminNews(@Param("keyword") String keyword,
               @Param("categoryId") Long categoryId,
               Pageable pageable);
}
