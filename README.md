# 📰 Enterprise News Management Platform - RESTful API Specification

> **Học Kỳ Doanh Nghiệp - Trường Đại Học Giao Thông Vận Tải TP.HCM (UTH)**  
> **Tài Liệu Đặc Tả Kỹ Thuật, Kiến Trúc Hệ Thống & Hướng Dẫn Vận Hành**  
> *Phiên bản: 1.0.0 (Production-Ready) | Kiến trúc: Clean Layered Architecture*

---

## 📌 Mục Lục
1. [Tổng Quan Kiến Trúc & Công Nghệ](#-tổng-quan-kiến-trúc--công-nghệ)
2. [Cấu Trúc Thư Mục & Phân Tầng Hệ Thống](#-cấu-trúc-thư-mục--phân-tầng-hệ-thống)
3. [Phân Định Trách Nhiệm Kỹ Thuật (WBS / Task Delegation)](#-phân-định-trách-nhiệm-kỹ-thuật-wbs--task-delegation)
4. [Quy Trình Khởi Chạy & Vận Hành Với Docker](#-quy-trình-khởi-chạy--vận-hành-với-docker)
5. [Cấu Hình Môi Trường Thực Thi (.env)](#-cấu-hình-môi-trường-thực-thi-env)
6. [Chuẩn Hóa Giao Thức Request / Response](#-chuẩn-hóa-giao-thức-request--response)
7. [Đặc Tả Chi Tiết API Endpoints](#-đặc-tả-chi-tiết-api-endpoints)
8. [Dữ Liệu Khởi Tạo Mặc Định (System Seeding)](#-dữ-liệu-khởi-tạo-mặc-định-system-seeding)

---

## 🛠 Tổng Quan Kiến Trúc & Công Nghệ

Hệ thống được thiết kế theo tiêu chuẩn công nghiệp nhằm đảm bảo tính toàn vẹn dữ liệu, hiệu năng truy vấn cao và khả năng mở rộng (scalability):

| Phân Hệ | Công Nghệ Áp Dụng | Mô Tả Kỹ Thuật |
| :--- | :--- | :--- |
| **Nền Tảng Cốt Lõi** | Java 17 LTS + Spring Boot 3.3.4 | Nền tảng hướng đối tượng strongly-typed, tối ưu hóa bộ nhớ và luồng thực thi. |
| **Bảo Mật & Phân Quyền** | Spring Security 6 + JJWT 0.12.6 | Cơ chế xác thực phi trạng thái (Stateless Session) sử dụng thuật toán HMAC-SHA256. |
| **Lưu Trữ Dữ Liệu** | PostgreSQL 16 Alpine | Cơ sở dữ liệu quan hệ mã nguồn mở mạnh mẽ, hỗ trợ đánh chỉ mục B-tree và Full-Text Search. |
| **Tầng Ánh Xạ Đối Tượng** | Spring Data JPA + Hibernate 6 | Quản trị giao dịch tự động (Declarative Transactions), Auditing thời gian thực. |
| **Chuẩn Hóa Đầu Vào** | Jakarta Bean Validation | Kiểm định nghiêm ngặt tính hợp lệ của dữ liệu đầu vào ngay tại tầng Controller. |
| **Container Hóa** | Docker & Docker Compose | Multi-stage build nhằm cô lập môi trường thực thi, dung lượng image tối ưu (~180MB). |
| **Quản Trị Cơ Sở Dữ Liệu** | pgAdmin 4 Web Interface | Cung cấp giao diện web trực quan để giám sát, truy vấn và quản trị schema CSDL. |

---

## 📂 Cấu Trúc Thư Mục & Phân Tầng Hệ Thống

Dự án áp dụng mô hình **Clean Layered Architecture**, phân tách độc lập các mối quan tâm (Separation of Concerns):

```text
news-management/
├── docker-compose.yml                          # Điều phối dịch vụ (App + PostgreSQL 16 + pgAdmin 4)
├── Dockerfile                                  # Multi-stage container build (Maven compile -> JRE 17 runtime)
├── .dockerignore                               # Loại bỏ artifacts không cần thiết khi đóng gói container
├── .env.example                                # Bản mẫu khai báo biến môi trường chuẩn
├── .env                                        # Tệp biến môi trường cục bộ (được cô lập khỏi VCS)
├── pom.xml                                     # Quản trị vòng đời ứng dụng và thư viện phụ thuộc (Maven)
├── README.md                                   # Tài liệu kỹ thuật chính thức
│
├── uploads/                                    # Phân vùng lưu trữ tệp tin tĩnh do người dùng tải lên
│
└── src/
    └── main/
        ├── java/com/uth/newsmanagement/
        │   ├── NewsManagementApplication.java  # Điểm khởi động ứng dụng (Spring Boot Application Entrypoint)
        │   │
        │   ├── common/                         # TẦNG DÙNG CHUNG TOÀN HỆ THỐNG
        │   │   ├── constant/                   # Định nghĩa các hằng số nghiệp vụ (AppConstants)
        │   │   ├── dto/                        # Đối tượng bao bọc dữ liệu chuẩn (ApiResponse<T>, PaginationMeta)
        │   │   └── exception/                  # Bộ xử lý ngoại lệ trung tâm (@RestControllerAdvice)
        │   │
        │   ├── config/                         # TẦNG CẤU HÌNH HẠ TẦNG
        │   │   ├── SecurityConfig.java         # Thiết lập Security Filter Chain, phân luồng Public/Private
        │   │   └── WebMvcConfig.java           # Định tuyến tài nguyên tĩnh (/uploads/**) và thiết lập CORS
        │   │
        │   ├── security/                       # TẦNG BẢO MẬT VÀ XÁC THỰC
        │   │   ├── JwtTokenProvider.java       # Phát hành, phân tích cú pháp và kiểm định JWT Token
        │   │   ├── JwtAuthenticationFilter.java# Interceptor trích xuất Bearer Token từ HTTP Header
        │   │   ├── CustomUserDetailsService.java
        │   │   └── UserPrincipal.java          # Cài đặt chi tiết đối tượng UserDetails cho Spring Security
        │   │
        │   ├── entity/                         # TẦNG THỰC THỂ DỮ LIỆU (ORM Entities)
        │   │   ├── BaseEntity.java             # Lớp cơ sở tự động ghi nhận createdAt, updatedAt
        │   │   ├── Role.java                   # Danh mục quyền hạn (ROLE_USER, ROLE_ADMIN)
        │   │   ├── User.java                   # Thực thể người dùng (được tối ưu Index trên username/email)
        │   │   ├── Category.java               # Thực thể chuyên mục (được tối ưu Index trên slug)
        │   │   └── News.java                   # Thực thể tin tức (được tối ưu Index trên slug, createdAt, viewCount)
        │   │
        │   ├── repository/                     # TẦNG TRUY XUẤT DỮ LIỆU (Data Access Layer)
        │   │   ├── UserRepository.java
        │   │   ├── CategoryRepository.java
        │   │   └── NewsRepository.java         # Triển khai truy vấn động, phân trang, lọc và thống kê
        │   │
        │   ├── dto/                            # ĐỐI TƯỢNG VẬN CHUYỂN DỮ LIỆU (Data Transfer Objects)
        │   │   ├── auth/                       # RegisterRequest, LoginRequest, AuthResponse
        │   │   ├── news/                       # NewsCreateRequest, NewsUpdateRequest, NewsResponse
        │   │   └── file/                       # FileUploadResponse
        │   │
        │   ├── service/                        # TẦNG XỬ LÝ NGHIỆP VỤ (Business Logic Layer)
        │   │   ├── AuthService.java            # Quy trình đăng ký tài khoản, xác thực và cấp phát token
        │   │   ├── FileStorageService.java     # Kiểm định MIME type, tạo định danh duy nhất và lưu trữ tệp
        │   │   └── NewsService.java            # Nghiệp vụ tin tức, thuật toán tạo slug và tăng lượt xem
        │   │
        │   ├── controller/                     # TẦNG ĐIỀU KHIỂN & ĐỊNH TUYẾN (API Gateway / Endpoints)
        │   │   ├── AuthController.java         # Tuyến /api/v1/auth/** (Public)
        │   │   ├── FileController.java         # Tuyến /api/v1/files/** (Private - Yêu cầu xác thực)
        │   │   ├── PublicNewsController.java   # Tuyến /api/v1/public/news/** (Public)
        │   │   └── AdminNewsController.java    # Tuyến /api/v1/admin/news/** (Private - Yêu cầu xác thực)
        │   │
        │   └── database/                       # TẦNG KHỞI TẠO VÀ DI TRÚ DỮ LIỆU
        │       └── DataSeeder.java             # Khởi tạo dữ liệu nền tảng tự động khi triển khai lần đầu
        │
        └── resources/
            └── application.yml                 # Cấu hình tập trung nạp tham số từ biến môi trường
```

---

## 👥 Phân Định Trách Nhiệm Kỹ Thuật (WBS / Task Delegation)

Hệ thống được xây dựng dựa trên sự phối hợp chặt chẽ giữa 2 kỹ sư chuyên môn hóa:

### 1. Kỹ sư Backend (Lead Backend Engineer)
*Chịu trách nhiệm về Kiến trúc hệ thống, Chuẩn hóa API, Bảo mật tầng mạng và Hạ tầng triển khai.*
- [x] Thiết kế khung kiến trúc tổng thể dự án trên nền tảng Spring Boot 3.3.4 và Java 17.
- [x] Đóng gói và thiết lập quy trình Container hóa với Dockerfile (Multi-stage) và Docker Compose.
- [x] Thiết lập cơ chế cấu hình động qua biến môi trường (`.env`, `application.yml`).
- [x] Chuẩn hóa định dạng trao đổi dữ liệu toàn cục (`ApiResponse<T>`, `PaginationMeta`).
- [x] Xây dựng bộ chặn và xử lý ngoại lệ tập trung (`GlobalExceptionHandler` với `@RestControllerAdvice`).
- [x] Thiết lập bộ lọc bảo mật **Spring Security 6** (Stateless Session, CORS policy, vô hiệu hóa CSRF).
- [x] Triển khai hệ thống xác thực **JWT Authentication** (`JwtTokenProvider`, `JwtAuthenticationFilter`).
- [x] Xây dựng API Đăng ký tài khoản kèm cơ chế băm mật khẩu chuẩn công nghiệp (`BCryptPasswordEncoder`).
- [x] Xây dựng API Đăng nhập và quản trị vòng đời của Access Token.
- [x] Xây dựng phân hệ Upload và Xóa tệp tin tĩnh với cơ chế kiểm tra định dạng và chống tấn công Path Traversal.
- [ ] **TODO: [BACKEND]** Xuất bản tài liệu kỹ thuật tương tác qua OpenAPI/Swagger và Postman Collection phục vụ kiểm thử.
- [ ] **TODO: [BACKEND]** Cấu hình cơ chế phân quyền chi tiết dựa trên Role (RBAC) cho các thao tác nhạy cảm.

---

### 2. Kỹ sư Dữ liệu (Data & Persistence Specialist)
*Chịu trách nhiệm về Thiết kế CSDL, Tối ưu hóa truy vấn, Đánh chỉ mục và Nghiệp vụ tin tức.*
- [x] Thiết kế mô hình dữ liệu quan hệ (ERD) cho các thực thể: `users`, `categories`, `news`.
- [x] Cấu hình môi trường quản trị CSDL trực quan pgAdmin 4 trên nền tảng Docker.
- [x] Định nghĩa và cấu hình ánh xạ JPA Entities (`@Entity`, `@Table`, `@Index`, `@ManyToOne`).
- [x] Triển khai lớp `DataSeeder` tự động nạp dữ liệu danh mục và tài khoản mẫu khi khởi động hệ thống.
- [x] Xây dựng `NewsRepository` tích hợp phân trang (`Pageable`), sắp xếp đa tiêu chí và đếm lượt xem nguyên tử.
- [x] Xây dựng các dịch vụ nghiệp vụ tin tức tại `NewsService` và điều phối tại Controllers.
- [x] **[DATA]** Tối ưu hóa câu truy vấn tìm kiếm toàn văn: Ứng dụng **PostgreSQL Full-Text Search** (`to_tsvector`, `to_tsquery`) hoặc triển khai dynamic query với `JpaSpecificationExecutor`.
- [x] **[DATA]** Tối ưu hóa chiến lược đánh chỉ mục (Indexing Strategy) trên các trường: `created_at`, `view_count`, `category_id`, `is_published` để đảm bảo độ trễ truy vấn thấp khi khối lượng bản ghi tăng trưởng lớn.
- [x] **[DATA]** Mở rộng bộ dữ liệu mẫu (Seeder): Sinh tập dữ liệu giả lập từ 30 - 50 bài viết thực tế với đa dạng chủ đề phục vụ đo kiểm hiệu năng sắp xếp và phân trang.
- [x] **[DATA]** (Mở rộng nâng cao): Thay thế cơ chế `ddl-auto: update` bằng công cụ quản trị phiên bản schema chuyên nghiệp (**Flyway Migration**).

---

## 🐳 Quy Trình Khởi Chạy & Vận Hành Với Docker

Dự án cung cấp 2 quy trình vận hành tiêu chuẩn, đáp ứng linh hoạt cho giai đoạn phát triển cũng như đánh giá chất lượng:

### Quy trình 1: Môi Trường Phát Triển Cục Bộ (Local Development Workflow)
> **Mục tiêu:** Tách biệt CSDL vào container để đảm bảo tính cô lập, đồng thời cho phép Kỹ sư Backend tận dụng tính năng Debug, Hot-reload và thực thi nhanh chóng trên IDE máy trạm.

1. **Khởi chạy CSDL PostgreSQL 16 và giao diện pgAdmin 4:**
   ```powershell
   docker compose up -d postgres pgadmin
   ```
2. **Khởi chạy ứng dụng Backend trên máy trạm:**
   - Thực thi trực tiếp từ IDE (IntelliJ IDEA / VS Code) bằng cách chạy class `NewsManagementApplication.java`.
   - Hoặc thực thi qua giao diện dòng lệnh Maven:
     ```powershell
     mvn spring-boot:run
     ```
3. **Truy cập Giao diện Quản trị CSDL (pgAdmin 4):**
   - URL: `http://localhost:5050`
   - Tài khoản đăng nhập: `admin@uth.edu.vn` | Mật khẩu: `admin123`
   - Cấu hình kết nối tới CSDL:
     - **Name:** `News Production DB`
     - **Host name/address:** `localhost` (hoặc `postgres` nếu kết nối nội bộ giữa các container)
     - **Port:** `5432`
     - **Maintenance database:** `news_management_db`
     - **Username:** `postgres`
     - **Password:** `postgres123`

---

### Quy trình 2: Môi Trường Triển Khai Toàn Diện (Fullstack Containerized Deployment)
> **Mục tiêu:** Triển khai khép kín toàn bộ hệ sinh thái (Ứng dụng + CSDL + Giao diện quản trị) chỉ với một câu lệnh duy nhất. Phục vụ công tác đánh giá đồ án, kiểm thử chấp nhận (UAT) mà không cần cài đặt JDK hay CSDL thủ công trên máy chủ.

```powershell
# 1. Tự động biên dịch mã nguồn đa tầng và khởi chạy cụm containers:
docker compose up --build -d

# 2. Giám sát nhật ký hoạt động (Logs) của ứng dụng theo thời gian thực:
docker compose logs -f app

# 3. Dừng và giải phóng tài nguyên toàn hệ thống:
docker compose down

# 4. (Tùy chọn) Khởi tạo lại trạng thái trắng (Xóa bỏ hoàn toàn volume dữ liệu):
docker compose down -v
```

Sau khi cụm dịch vụ khởi động hoàn tất, các cổng giao tiếp được phân bổ như sau:
*   **Cổng RESTful API Backend:** `http://localhost:8080`
*   **Cổng Quản trị CSDL pgAdmin 4:** `http://localhost:5050`
*   **Cổng Dịch vụ PostgreSQL:** `localhost:5432`

---

## ⚙️ Cấu Hình Môi Trường Thực Thi (.env)

Hệ thống tuân thủ nguyên tắc **The Twelve-Factor App**, tách biệt hoàn toàn cấu hình khỏi mã nguồn:

```env
# ==========================================
# CẤU HÌNH CỔNG MÁY CHỦ
# ==========================================
SERVER_PORT=8080

# ==========================================
# CẤU HÌNH DỊCH VỤ CSDL POSTGRESQL 16
# ==========================================
POSTGRES_DB=news_management_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123

# Chuỗi kết nối JDBC cho máy trạm (Host)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/news_management_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres123
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# Đồng bộ schema thực thể
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true

# ==========================================
# CẤU HÌNH GIAO DIỆN PGADMIN 4
# ==========================================
PGADMIN_EMAIL=admin@uth.edu.vn
PGADMIN_PASSWORD=admin123

# ==========================================
# CẤU HÌNH BẢO MẬT JSON WEB TOKEN (JWT)
# ==========================================
# Khóa bí mật tối thiểu 256-bit theo chuẩn RFC 7518
JWT_SECRET=NewsManagementSecretKeyForJwtAuthenticationMustBeAtLeast256BitsLong2026!
# Thời hạn hiệu lực: 86.400.000 ms (24 giờ)
JWT_EXPIRATION_MS=86400000

# ==========================================
# CẤU HÌNH TẢI LÊN TỆP TIN
# ==========================================
APP_UPLOAD_DIR=./uploads
APP_UPLOAD_MAX_FILE_SIZE=5MB
```

---

## 📦 Chuẩn Hóa Giao Thức Request / Response

Tất cả các phản hồi từ hệ thống đều được bọc trong cấu trúc chuẩn thống nhất `ApiResponse<T>`, tạo sự thuận lợi tối đa cho việc tích hợp phía Client/Frontend:

### 1. Cấu Trúc Phản Hồi Thành Công (HTTP 200 OK / 201 Created)
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Lấy danh sách tin tức thành công",
  "data": [
    {
      "id": 1,
      "title": "Chào mừng sinh viên tham gia Học Kỳ Doanh Nghiệp UTH",
      "slug": "chao-mung-sinh-vien-tham-gia-hoc-ky-doanh-nghiep-uth",
      "summary": "Chương trình hợp tác đào tạo giữa nhà trường và doanh nghiệp công nghệ...",
      "content": "Nội dung chi tiết của bài viết...",
      "thumbnailUrl": "/uploads/example-banner.png",
      "viewCount": 126,
      "isPublished": true,
      "categoryId": 3,
      "categoryName": "Giáo dục UTH",
      "authorUsername": "admin",
      "createdAt": "2026-10-15T16:30:00",
      "updatedAt": "2026-10-15T16:35:00"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 10,
    "totalElements": 25,
    "totalPages": 3
  },
  "timestamp": "2026-10-15T17:00:00"
}
```

### 2. Cấu Trúc Phản Hồi Khi Vi Phạm Dữ Liệu Đầu Vào (HTTP 400 Bad Request)
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Dữ liệu gửi lên không hợp lệ",
  "data": {
    "email": "Email không đúng định dạng",
    "password": "Mật khẩu phải có ít nhất 6 ký tự",
    "username": "Tên đăng nhập không được để trống"
  },
  "timestamp": "2026-10-15T17:01:25"
}
```

### 3. Cấu Trúc Phản Hồi Khi Lỗi Xác Thực (HTTP 401 Unauthorized)
```json
{
  "success": false,
  "statusCode": 401,
  "message": "Bạn cần đăng nhập (hoặc cung cấp Bearer token hợp lệ) để truy cập tài nguyên này",
  "timestamp": "2026-10-15T17:02:10"
}
```

---

## 📡 Đặc Tả Chi Tiết API Endpoints

### 1. Phân Hệ Xác Thực & Phân Quyền (Authentication - Public)
| Phương Thức | Điểm Cuối (Endpoint) | Chức Năng | Định Dạng Dữ Liệu Đầu Vào |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản người dùng mới | `{"username": "...", "email": "...", "password": "..."}` |
| `POST` | `/api/v1/auth/login` | Xác thực đăng nhập và nhận JWT Access Token | `{"username": "...", "password": "..."}` |

### 2. Phân Hệ Đọc Tin Tức (Public News - Độc Giả)
*Không yêu cầu đính kèm Token trong Header.*

| Phương Thức | Điểm Cuối (Endpoint) | Chức Năng | Tham Số Truy Vấn (Query Parameters) |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/public/news` | Truy xuất danh sách tin tức đã xuất bản | `page` (default 1), `limit` (default 10), `sortBy` (default `createdAt`), `sortDir` (`asc`/`desc`), `keyword`, `categoryId` |
| `GET` | `/api/v1/public/news/{slugOrId}` | Xem chi tiết bài viết và **tự động tăng nguyên tử lượt xem** | Path variable: `slug` (ví dụ: `cong-nghe-ai-2026`) hoặc `id` |

### 3. Phân Hệ Quản Trị Tin Tức (Private News - Quản Trị & Biên Tập)
> **Yêu cầu bảo mật:** Bắt buộc gửi kèm HTTP Header:  
> `Authorization: Bearer <access_token>`

| Phương Thức | Điểm Cuối (Endpoint) | Chức Năng | Dữ Liệu Yêu Cầu |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/news` | Truy vấn danh sách tin quản trị (bao gồm tin ẩn) | Hỗ trợ phân trang, lọc và sắp xếp tương tự Public |
| `POST` | `/api/v1/admin/news` | Khởi tạo bài viết mới vào hệ thống | `{"title": "...", "summary": "...", "content": "...", "categoryId": 1, "thumbnailUrl": "..."}` |
| `PUT` | `/api/v1/admin/news/{id}` | Cập nhật thông tin và trạng thái bài viết | `{"title": "...", "content": "...", "isPublished": true, ...}` |
| `DELETE` | `/api/v1/admin/news/{id}` | Xóa bài viết khỏi cơ sở dữ liệu | Path variable: `id` |

### 4. Phân Hệ Quản Trị Tệp Tin (File Management - Private)
> **Yêu cầu bảo mật:** Bắt buộc gửi kèm HTTP Header:  
> `Authorization: Bearer <access_token>`

| Phương Thức | Điểm Cuối (Endpoint) | Chức Năng | Kiểu Nội Dung (Content-Type) |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/files/upload` | Tải lên hình ảnh bài viết (Hỗ trợ: `.jpg`, `.png`, `.webp`; Max: 5MB) | `multipart/form-data` (form key: `file`) |
| `DELETE` | `/api/v1/files/{fileName}` | Xóa tệp tin vật lý khỏi phân vùng lưu trữ | Path variable: `fileName` |
| `GET` | `/uploads/{fileName}` | Truy cập hoặc tải tệp trực tiếp | *(Tuyến công khai phục vụ hiển thị)* |

---

## 🔑 Dữ Liệu Khởi Tạo Mặc Định (System Seeding)

Khi hệ thống được khởi chạy lần đầu tiên trên một cơ sở dữ liệu trống, cơ chế `DataSeeder` sẽ tự động khởi tạo dữ liệu mẫu nhằm phục vụ công tác kiểm thử tức thì:

### 1. Tài Khoản Quản Trị & Tác Giả
| Tên Đăng Nhập | Mật Khẩu Khởi Tạo | Vai Trò Hệ Thống | Phạm Vi Quyền Hạn |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Toàn quyền quản trị nội dung và hệ thống |
| `author` | `author123` | `ROLE_USER` | Tác giả biên soạn và xuất bản tin tức |

### 2. Danh Mục Khởi Tạo Sẵn
*   **Công nghệ** (Slug: `cong-nghe`)
*   **Thời sự** (Slug: `thoi-su`)
*   **Giáo dục UTH** (Slug: `giao-duc-uth`)
