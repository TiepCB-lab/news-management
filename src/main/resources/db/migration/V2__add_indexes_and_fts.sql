-- ============================================================
-- V2: Tối Ưu Hóa Chiến Lược Đánh Chỉ Mục (Indexing Strategy)
-- Tác giả: Kỹ sư Dữ liệu - UTH Enterprise Semester
-- Mô tả:
--   1. Composite index cho truy vấn lọc & sắp xếp đa tiêu chí
--   2. PostgreSQL Full-Text Search (tsvector) cho tìm kiếm nhanh
-- ============================================================

-- --------------------------------------------------------
-- PHẦN 1: COMPOSITE INDEXES - Tối ưu truy vấn đa tiêu chí
-- --------------------------------------------------------

-- Index phổ biến nhất: lọc published + sắp xếp theo ngày
-- Phục vụ: GET /api/v1/public/news (mặc định)
CREATE INDEX IF NOT EXISTS idx_news_published_created
    ON news (is_published, created_at DESC);

-- Index phục vụ lọc theo chuyên mục (published + category)
-- Phục vụ: GET /api/v1/public/news?categoryId=X
CREATE INDEX IF NOT EXISTS idx_news_published_category
    ON news (is_published, category_id, created_at DESC);

-- Index phục vụ bảng xếp hạng nội dung nổi bật (top views)
-- Phục vụ: GET /api/v1/public/news?sortBy=viewCount&sortDir=desc
CREATE INDEX IF NOT EXISTS idx_news_published_views
    ON news (is_published, view_count DESC);

-- Index phục vụ tra cứu admin: lọc theo category_id (không giới hạn is_published)
-- Phục vụ: GET /api/v1/admin/news?categoryId=X
CREATE INDEX IF NOT EXISTS idx_news_category_created
    ON news (category_id, created_at DESC);

-- --------------------------------------------------------
-- PHẦN 2: FULL-TEXT SEARCH INDEX (PostgreSQL GIN + tsvector)
-- --------------------------------------------------------

-- Thêm cột tsvector được tính toán sẵn (stored generated column)
-- Kết hợp title (weight A - quan trọng nhất) và summary (weight B)
-- Sử dụng cấu hình tiếng Anh/Unaccent cho tính tương thích tốt nhất
ALTER TABLE news
    ADD COLUMN IF NOT EXISTS fts_vector TSVECTOR
        GENERATED ALWAYS AS (
            setweight(to_tsvector('simple', coalesce(title, '')), 'A') ||
            setweight(to_tsvector('simple', coalesce(summary, '')), 'B') ||
            setweight(to_tsvector('simple', coalesce(content, '')), 'C')
        ) STORED;

-- GIN index trên cột tsvector - tối ưu cho Full-Text Search
-- GIN (Generalized Inverted Index): phù hợp với dữ liệu tĩnh, query nhanh
CREATE INDEX IF NOT EXISTS idx_news_fts_vector
    ON news USING GIN (fts_vector);

-- --------------------------------------------------------
-- PHẦN 3: INDEX HỖ TRỢ THAM CHIẾU KHÓA NGOẠI
-- --------------------------------------------------------

-- PostgreSQL không tự tạo index cho FK — cần tạo thủ công
CREATE INDEX IF NOT EXISTS idx_news_author_id
    ON news (author_id);
