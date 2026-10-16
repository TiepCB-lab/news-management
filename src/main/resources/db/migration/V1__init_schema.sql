-- ============================================================
-- V1: Khởi tạo Schema Ban Đầu (Initial Schema)
-- Tác giả: Kỹ sư Dữ liệu - UTH Enterprise Semester
-- Mô tả: Tạo toàn bộ cấu trúc bảng và chỉ mục nền tảng
-- ============================================================

-- Bảng: users
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

-- Index tối ưu tra cứu người dùng
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);

-- --------------------------------------------------------

-- Bảng: categories
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

-- Index tối ưu tra cứu chuyên mục theo slug
CREATE INDEX IF NOT EXISTS idx_category_slug ON categories (slug);

-- --------------------------------------------------------

-- Bảng: news
CREATE TABLE IF NOT EXISTS news (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    slug          VARCHAR(280) NOT NULL UNIQUE,
    summary       VARCHAR(500),
    content       TEXT         NOT NULL,
    thumbnail_url VARCHAR(255),
    view_count    BIGINT       NOT NULL DEFAULT 0,
    is_published  BOOLEAN      NOT NULL DEFAULT TRUE,
    category_id   BIGINT REFERENCES categories (id) ON DELETE SET NULL,
    author_id     BIGINT REFERENCES users (id)       ON DELETE SET NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP
);

-- Index cơ bản (slug, created_at, view_count, is_published)
CREATE INDEX IF NOT EXISTS idx_news_slug        ON news (slug);
CREATE INDEX IF NOT EXISTS idx_news_created_at  ON news (created_at);
CREATE INDEX IF NOT EXISTS idx_news_view_count  ON news (view_count);
CREATE INDEX IF NOT EXISTS idx_news_published   ON news (is_published);
