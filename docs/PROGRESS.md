# Tiến độ hoàn thiện Nhiệm vụ 17

Mốc hoàn thành là backend đáp ứng các yêu cầu của đề, có hướng dẫn chạy và bằng chứng kiểm tra. Các mục mở rộng như Full-Text Search, Redis, đo hiệu năng dữ liệu lớn và Flyway không phải điều kiện bàn giao.

Chia phần còn lại thành 3 đợt; mỗi đợt hoàn thiện một nhóm việc có thể kiểm tra. Không quy đổi số checkbox trong README thành phần trăm chất lượng.

| Đợt | Phạm vi | Trạng thái |
| --- | --- | --- |
| 1 — 26/10/2026 | Tìm kiếm, lọc, phân trang, sắp xếp, đọc tin và kiểm tra hồi quy | Hoàn thành |
| 2 | Hoàn thiện đăng ký/đăng nhập, quyền truy cập, cấu hình môi trường và upload/xóa file | Chưa thực hiện |
| 3 | Hoàn thiện tài liệu API, dữ liệu demo, kiểm tra luồng theo đề và hướng dẫn bàn giao | Chưa thực hiện |

## Đợt 1

- Dùng chung `NewsQueryRequest` để kiểm tra đầu vào của danh sách public/private.
- Trả HTTP 400 khi số trang, giới hạn, trường sắp xếp hoặc danh mục không hợp lệ.
- Kết hợp từ khóa và chuyên mục; tìm trong cả tiêu đề và tóm tắt. Public luôn lọc tin đã xuất bản.
- Sắp xếp có danh sách trường cho phép và `id` làm tiêu chí phụ.
- Tăng lượt xem bằng câu lệnh cập nhật bộ đếm; tránh sửa entity rồi ghi đè kết quả cập nhật đó khi Hibernate flush.
- Kiểm tra API qua MockMvc với Spring Security/JWT thật và H2 riêng trong bộ nhớ. Upload của test dùng thư mục tạm.

Chạy kiểm tra: `mvn test` (cần JDK 17 trở lên và Maven; lần đầu cần tải dependency). Các kiểm tra H2 chưa thay thế việc chạy luồng demo trên PostgreSQL ở đợt bàn giao.

Kết quả ngày 26/10/2026: `mvn test` thành công, 39 ca kiểm tra qua, 0 lỗi, 0 bỏ qua. Chạy bằng JDK 25 có trên máy, biên dịch với `release 17`. Các ca bao gồm lọc kết hợp, tin nháp, wildcard, sắp xếp/phân trang, tham số sai, thiếu token, JSON sai định dạng và bộ đếm lượt xem. Chưa kiểm tra đồng thời nhiều lượt đọc hoặc chạy trên PostgreSQL trong đợt này.

## Phần việc của đợt 2

- Chuẩn hóa dữ liệu đăng ký và xử lý trùng tài khoản/email.
- Kiểm tra đăng nhập, token không hợp lệ và quyền USER/ADMIN, bao gồm thao tác xóa.
- Hoàn thiện kiểm tra file, xử lý file rỗng/không đúng định dạng và lỗi request multipart.
- Đồng bộ cấu hình môi trường với hướng dẫn chạy local/Docker và cấu hình CORS.

## Tiêu chí đợt 3

- Hướng dẫn khởi chạy từ bản sao dự án mới; giải thích rõ các biến môi trường và hằng số dùng chung.
- Hoàn thiện tài liệu API/Postman và mục Swagger còn lại trong WBS; tài liệu phản ánh đúng code.
- Có dữ liệu mẫu đủ trình diễn tìm kiếm/sắp xếp/phân trang.
- Chạy luồng: đăng ký → đăng nhập → thêm tin → public đọc tin → sửa/tìm kiếm/sắp xếp → xóa tin; upload → lấy URL → xóa file.
- Ghi kết quả kiểm tra và các giới hạn còn lại trước khi đánh dấu hoàn thành.
