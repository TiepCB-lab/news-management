# Thử nghiệm ponytail — 27/10/2026

Áp dụng `ponytail` mức **full** để làm đợt 2 và `ponytail-review` để rà thay đổi, giả định tải của bài tập/demo local: nhóm hai người, dữ liệu mẫu nhỏ. Không dùng kết quả này để kết luận hệ thống sẵn sàng triển khai công khai.

## Kết quả quan sát

- Trước khi sửa: `mvn test` có 6 ca lỗi trên 39 ca. Response public lộ lỗi hai method repository không tồn tại; các file class cũ khiến Maven bỏ qua bước biên dịch lại.
- Dùng lại `NewsSpecifications` và bộ kiểm tra cũ để sửa tại luồng dùng chung; bỏ ba truy vấn repository không còn nơi gọi. Migration/index có sẵn không được chỉnh trong đợt này.
- Đăng nhập lấy thông tin từ principal đã xác thực, bỏ truy vấn lại tài khoản ngay sau đó.
- Dùng ràng buộc unique của DB và `saveAndFlush` để xử lý hai đăng ký trùng đồng thời; không thêm cơ chế khóa trong bộ nhớ.
- Dùng thư viện Java/Spring đang có để kiểm tra upload, xử lý lỗi và CORS. Không thêm dependency, framework hoặc lớp runtime mới.
- Giữ kiểm tra qua Spring/JWT/DB thật trong môi trường test riêng. Có ca gửi HTTP đến Tomcat cục bộ để kiểm tra giới hạn multipart; có ca mô phỏng lỗi ghi để kiểm tra dọn file dở và che lỗi nội bộ.

## Đánh giá skill

| Phần | Nhận xét qua lần thử này |
| --- | --- |
| `ponytail full` | Phù hợp dự án nhỏ. Thứ tự “tái sử dụng code → thư viện chuẩn → dependency sẵn có” giúp giữ cấu trúc hiện tại và tránh thêm tầng không cần thiết. |
| Rà nơi gọi trước khi sửa | Hữu ích nhất ở đợt này: phát hiện public search bị ngắt khỏi repository và các truy vấn cũ không còn caller. |
| Bắt buộc kiểm tra logic có rủi ro | Giữ được kiểm tra cho đăng ký đồng thời, JWT và ghi/xóa file. Code ngắn không được dùng làm lý do bỏ các kiểm tra này. |
| `ponytail-review` | Có ích khi bám trường hợp cụ thể và đọc cả caller. Không thay thế việc chạy test hay kiểm tra hệ quản trị CSDL thật. |
| Quy tắc diff ngắn | Cần cân nhắc: bổ sung kiểm tra đầu vào và dọn file khi lỗi vẫn cần thêm code. Số dòng ít hơn không tự chứng minh chất lượng tốt hơn. |
| `shortcut:` | Làm giới hạn dễ thấy, nhưng comment không chứng minh cách làm an toàn. Upload hiện chỉ kiểm tra chữ ký đầu file; triển khai công khai cần đánh giá giải mã/quét nội dung. |
| Cách trình bày | `ponytail-review` yêu cầu mẫu báo cáo tiếng Anh khá cứng; báo cáo ở đây dùng tiếng Việt theo cuộc trao đổi. |

Nhiều nguyên tắc của skill cũng là thực hành lập trình tốt thông thường. Lần thử này không có đối chứng làm cùng việc khi tắt skill, nên không khẳng định giảm bao nhiêu thời gian, token hay chi phí. Việc xóa nhiều dòng comment/truy vấn không dùng cũng không phải phép đo năng suất.

## Rà soát kết quả

Đã đọc diff cùng controller, service, repository, cấu hình và các kiểm tra liên quan. Các lỗi tìm được trong phạm vi đợt 2 đã được sửa; xem kết quả chạy cuối trong [PROGRESS.md](PROGRESS.md).

Kết quả cuối: `mvn clean test` qua **69/69 ca**, gồm 30 ca mới về auth/file/CORS và 39 ca tin tức. `docker compose config --quiet` và `git diff --check` đều thành công.

**Verdict: Ship cho phạm vi demo local của đợt 2.** Còn đợt 3 để kiểm tra bàn giao trên PostgreSQL và hoàn thiện tài liệu API.

Chưa kiểm tra PostgreSQL/Flyway, Docker chạy thực tế, tải lớn hoặc quét mã độc. Đợt 3 còn tài liệu API và kiểm tra luồng bàn giao trên PostgreSQL.
