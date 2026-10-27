# Tiến độ hoàn thiện Nhiệm vụ 17

Mốc hoàn thành là backend đáp ứng các yêu cầu của đề, có hướng dẫn chạy và bằng chứng kiểm tra. Các mục mở rộng như Full-Text Search, Redis, đo hiệu năng dữ liệu lớn và Flyway không phải điều kiện bàn giao.

Chia phần còn lại thành 3 đợt; mỗi đợt hoàn thiện một nhóm việc có thể kiểm tra. Không quy đổi số checkbox trong README thành phần trăm chất lượng.

| Đợt | Phạm vi | Trạng thái |
| --- | --- | --- |
| 1 — 26/10/2026 | Tìm kiếm, lọc, phân trang, sắp xếp, đọc tin và kiểm tra hồi quy | Hoàn thành |
| 2 — 27/10/2026 | Hoàn thiện đăng ký/đăng nhập, quyền truy cập, cấu hình môi trường và upload/xóa file | Hoàn thành |
| 3 | Hoàn thiện tài liệu API, dữ liệu demo, kiểm tra luồng theo đề và hướng dẫn bàn giao | Chưa thực hiện |

## Đợt 1

- Dùng chung `NewsQueryRequest` để kiểm tra đầu vào của danh sách public/private.
- Trả HTTP 400 khi số trang, giới hạn, trường sắp xếp hoặc danh mục không hợp lệ.
- Kết hợp từ khóa và chuyên mục; tìm trong cả tiêu đề và tóm tắt. Public luôn lọc tin đã xuất bản.
- Sắp xếp có danh sách trường cho phép và `id` làm tiêu chí phụ.
- Tăng lượt xem bằng câu lệnh cập nhật bộ đếm; tránh sửa entity rồi ghi đè kết quả cập nhật đó khi Hibernate flush.
- Kiểm tra API qua MockMvc với Spring Security/JWT thật và H2 riêng trong bộ nhớ. Upload của test dùng thư mục tạm.

Chạy kiểm tra: `mvn test` (cấu hình hiện tại cần JDK 25 và Maven; lần đầu cần tải dependency). Các kiểm tra H2 chưa thay thế việc chạy luồng demo trên PostgreSQL ở đợt bàn giao.

Kết quả ngày 26/10/2026: `mvn test` thành công, 39 ca kiểm tra qua, 0 lỗi, 0 bỏ qua. Chạy bằng JDK 25 có trên máy, biên dịch với `release 17`. Các ca bao gồm lọc kết hợp, tin nháp, wildcard, sắp xếp/phân trang, tham số sai, thiếu token, JSON sai định dạng và bộ đếm lượt xem. Chưa kiểm tra đồng thời nhiều lượt đọc hoặc chạy trên PostgreSQL trong đợt này.

## Đợt 2 — thực hiện với ponytail

- Chuẩn hóa username/email trước validation và kiểm tra trùng; trả 409 khi trùng kể cả hai request đăng ký đồng thời. Username phân biệt hoa thường, email chuyển chữ thường; tài khoản mới luôn là USER.
- Kiểm tra giới hạn 72 byte UTF-8 của BCrypt, đăng nhập bằng email, token không hợp lệ/hết hạn và quyền xóa USER/ADMIN.
- Upload giới hạn kích thước và kiểm tra chữ ký file; trả MIME chuẩn, dọn file dở khi lỗi ghi. USER được upload, ADMIN được xóa. Chỉ kiểm tra chữ ký, chưa giải mã/quét mã độc.
- Sửa lỗi thiếu file/Content-Type, tài nguyên tĩnh không tồn tại; lỗi nội bộ chỉ ghi vào log, không đưa chi tiết ra response.
- Spring đọc `.env` khi chạy local; đồng bộ cổng host, kích thước upload và origin CORS giữa local/Docker. Xác minh preflight private không cần token và origin lạ bị từ chối.
- Sửa hồi quy public search phát hiện ở đầu đợt: hai method repository không tồn tại và lọc/sắp xếp bị bỏ qua. Dùng lại Specification có sẵn; API hiện không dùng Full-Text Search.

Đã kiểm tra cú pháp Compose bằng `docker compose config --quiet`; chưa chạy container. Test sử dụng H2 và thư mục upload tạm; có Tomcat ở cổng ngẫu nhiên để kiểm tra multipart qua HTTP thật.

Kết quả ngày 27/10/2026: `mvn clean test` thành công, **69/69 ca qua**, 0 lỗi, 0 bỏ qua (39 kiểm tra tin tức + 30 kiểm tra auth/file/CORS). Chạy JDK 25, biên dịch `release 17`. Bản đầu đợt có 6 lỗi/39 ca; các lỗi này đã được sửa. Chưa kiểm tra migration và hành vi riêng của PostgreSQL trong đợt này.

Đánh giá skill và phạm vi rà soát được ghi tại [PONYTAIL_EVALUATION.md](PONYTAIL_EVALUATION.md).

## Tiêu chí đợt 3

- Hướng dẫn khởi chạy từ bản sao dự án mới; giải thích rõ các biến môi trường và hằng số dùng chung.
- Hoàn thiện tài liệu API/Postman và mục Swagger còn lại trong WBS; tài liệu phản ánh đúng code.
- Có dữ liệu mẫu đủ trình diễn tìm kiếm/sắp xếp/phân trang.
- Chạy luồng: đăng ký → đăng nhập → thêm tin → public đọc tin → sửa/tìm kiếm/sắp xếp → xóa tin; upload → lấy URL → xóa file.
- Ghi kết quả kiểm tra và các giới hạn còn lại trước khi đánh dấu hoàn thành.

## Chuyển sang Java 25 — 27/10/2026

- Maven biên dịch bằng `release 25`; môi trường local/IDE cần JDK 25 và reload cấu hình Maven sau khi đổi JDK.
- Nâng Spring Boot từ 3.3.4 lên 3.5.16 theo [yêu cầu hệ thống chính thức](https://docs.spring.io/spring-boot/3.5/system-requirements.html), hỗ trợ Java 25.
- Docker build dùng Maven với Temurin JDK 25, runtime dùng Temurin JRE 25.
- `mvn clean verify --batch-mode --no-transfer-progress` thành công tại thư mục dự án: 69/69 ca kiểm tra qua và tạo `target/news-management-0.0.1-SNAPSHOT.jar`. `javap` xác nhận major version 69 (Java 25).
- Lần đầu gặp class trong `target` bị thay thế bằng bytecode Java 17 khi build đang chạy. Đã kiểm tra bằng bản sao riêng, sau đó chạy lại build sạch tại gốc dự án thành công. Nếu IDE vẫn báo lỗi, chọn JDK 25, reload dự án Maven và build sạch lại.
- Chưa build/chạy image Docker vì Docker Engine chưa hoạt động; đã kiểm tra cú pháp Compose. Các kết quả `release 17` ở trên là lịch sử trước lần chuyển đổi này.
