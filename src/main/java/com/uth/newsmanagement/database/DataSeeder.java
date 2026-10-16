package com.uth.newsmanagement.database;

import com.uth.newsmanagement.entity.Category;
import com.uth.newsmanagement.entity.News;
import com.uth.newsmanagement.entity.Role;
import com.uth.newsmanagement.entity.User;
import com.uth.newsmanagement.repository.CategoryRepository;
import com.uth.newsmanagement.repository.NewsRepository;
import com.uth.newsmanagement.repository.UserRepository;
import com.uth.newsmanagement.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Khởi tạo dữ liệu mẫu ban đầu (Data Seeder).
 *
 * <p>Phạm vi dữ liệu khởi tạo:
 * <ul>
 *   <li>2 tài khoản: admin (ROLE_ADMIN) và author (ROLE_USER)</li>
 *   <li>3 chuyên mục: Công nghệ, Thời sự, Giáo dục UTH</li>
 *   <li>40 bài viết mẫu đa dạng chủ đề — phục vụ đo kiểm hiệu năng phân trang và Full-Text Search</li>
 * </ul>
 *
 * <p>Ghi chú kỹ thuật: Seeder chỉ chạy khi cơ sở dữ liệu hoàn toàn trống ({@code userRepository.count() == 0}).
 * Trong môi trường production, nên thay thế bằng Flyway migration data scripts (V3__seed_data.sql).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NewsRepository newsRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      NewsRepository newsRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.newsRepository = newsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Chưa có dữ liệu, bắt đầu seed dữ liệu mẫu...");

            // --------------------------------------------------------
            // BƯỚC 1: Tạo tài khoản Admin & User mặc định
            // --------------------------------------------------------
            User admin = new User(
                    "admin",
                    "admin@uth.edu.vn",
                    passwordEncoder.encode("admin123"),
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);

            User author = new User(
                    "author",
                    "author@uth.edu.vn",
                    passwordEncoder.encode("author123"),
                    Role.ROLE_USER
            );
            userRepository.save(author);

            // --------------------------------------------------------
            // BƯỚC 2: Tạo Categories mẫu
            // --------------------------------------------------------
            Category techCat = new Category("Công nghệ", "cong-nghe", "Tin tức về AI, Blockchain, Phần mềm, Lập trình");
            Category newsCat = new Category("Thời sự", "thoi-su", "Tin tức sự kiện đời sống kinh tế xã hội trong và ngoài nước");
            Category eduCat  = new Category("Giáo dục UTH", "giao-duc-uth", "Thông tin học thuật, thực tập doanh nghiệp, tuyển sinh");
            categoryRepository.save(techCat);
            categoryRepository.save(newsCat);
            categoryRepository.save(eduCat);

            // --------------------------------------------------------
            // BƯỚC 3: Tạo 40 bài viết mẫu đa dạng chủ đề
            // --------------------------------------------------------
            List<NewsData> newsDataList = List.of(

                // ===== CHUYÊN MỤC: GIÁO DỤC UTH (12 bài) =====
                new NewsData(
                    "Chào mừng sinh viên tham gia Học Kỳ Doanh Nghiệp UTH 2026",
                    "Chương trình hợp tác đào tạo giữa Trường Đại học Giao thông vận tải TP.HCM và các Doanh nghiệp công nghệ hàng đầu.",
                    "Học Kỳ Doanh Nghiệp (Enterprise Semester) là mô hình đào tạo tiên phong tại UTH, giúp sinh viên được cọ xát thực tế với các bài toán Backend RESTful API, Database Optimization và Security trong môi trường chuyên nghiệp. Sinh viên sẽ làm việc trong các nhóm dự án thực tế, áp dụng phương pháp Agile/Scrum và được mentoring trực tiếp bởi các kỹ sư cấp cao từ doanh nghiệp đối tác. Đây là cơ hội vàng để xây dựng portfolio và kết nối với nhà tuyển dụng ngay từ khi còn ngồi trên ghế nhà trường.",
                    eduCat, admin, 125L, true
                ),
                new NewsData(
                    "UTH mở rộng hợp tác doanh nghiệp: 50 vị trí thực tập mới học kỳ 2026-2",
                    "Phòng Quan hệ Doanh nghiệp UTH công bố 50 vị trí thực tập kỹ thuật tại 15 công ty công nghệ.",
                    "Trường Đại học Giao thông vận tải TP.HCM vừa ký kết thỏa thuận hợp tác với thêm 15 doanh nghiệp công nghệ trong học kỳ 2026-2, mở ra 50 vị trí thực tập mới cho sinh viên ngành Công nghệ thông tin và Kỹ thuật phần mềm. Các vị trí bao gồm: Backend Developer Intern, Data Engineer Intern, DevOps Intern và QA Engineer Intern. Sinh viên đủ điều kiện (GPA ≥ 2.5, đã hoàn thành 90 tín chỉ) có thể nộp hồ sơ qua Cổng thông tin Sinh viên từ ngày 01/11/2026.",
                    eduCat, admin, 340L, true
                ),
                new NewsData(
                    "Kết quả tuyển sinh đại học 2026: UTH vượt chỉ tiêu 15%",
                    "Tỷ lệ thí sinh trúng tuyển ngành kỹ thuật tăng mạnh, phản ánh nhu cầu nhân lực công nghệ cao.",
                    "Năm tuyển sinh 2026, Trường Đại học Giao thông vận tải TP.HCM ghi nhận số lượng hồ sơ đăng ký tăng 23% so với cùng kỳ năm ngoái. Ngành Công nghệ thông tin có điểm chuẩn cao nhất toàn trường với 25.5 điểm (thang 30), tiếp theo là Kỹ thuật Phần mềm với 24.75 điểm. Ban lãnh đạo nhà trường cho biết sẽ mở thêm 2 lớp chất lượng cao để đáp ứng nhu cầu học tập ngày càng tăng cao của xã hội.",
                    eduCat, author, 210L, true
                ),
                new NewsData(
                    "Sinh viên UTH đoạt giải Nhất cuộc thi Lập trình quốc gia 2026",
                    "Đội tuyển gồm 3 sinh viên năm 3 vừa xuất sắc giành ngôi vô địch cuộc thi Code Battle Vietnam 2026.",
                    "Sau 3 vòng thi căng thẳng kéo dài 48 giờ liên tục, đội tuyển UTH gồm Nguyễn Minh Khoa, Trần Thị Lan Anh và Phạm Quốc Tuấn đã xuất sắc vượt qua 1.200 thí sinh từ 85 trường đại học toàn quốc để giành ngôi vô địch cuộc thi Code Battle Vietnam 2026. Dự án của đội tập trung vào bài toán tối ưu hóa lộ trình vận tải bằng thuật toán đồ thị kết hợp machine learning, giúp giảm chi phí nhiên liệu lên đến 18% trong mô phỏng thực tế.",
                    eduCat, author, 892L, true
                ),
                new NewsData(
                    "Chương trình học bổng toàn phần ASEAN cho sinh viên xuất sắc UTH",
                    "Quỹ học bổng ASEAN-Tech 2026 tài trợ 100% học phí cho 10 sinh viên UTH xuất sắc.",
                    "Trong khuôn khổ hợp tác giáo dục ASEAN, Quỹ học bổng ASEAN-Tech 2026 vừa thông báo tài trợ học bổng toàn phần trị giá 80 triệu đồng/năm cho 10 sinh viên tiêu biểu của UTH. Điều kiện xét tuyển bao gồm: GPA tích lũy ≥ 3.2/4.0, đã có ít nhất một công trình nghiên cứu khoa học được công bố, và có hoạt động ngoại khóa nổi bật. Hồ sơ nộp từ ngày 15/10 đến 15/11/2026.",
                    eduCat, admin, 456L, true
                ),
                new NewsData(
                    "Ra mắt phòng Lab AI và Cloud Computing mới tại cơ sở 2 UTH",
                    "Đầu tư 5 tỷ đồng nâng cấp hạ tầng phòng thực hành với GPU clusters và môi trường cloud thực tế.",
                    "Trường UTH vừa khánh thành Phòng thực hành AI & Cloud Computing tại cơ sở 2 (Quận 12, TP.HCM) với tổng vốn đầu tư 5 tỷ đồng. Phòng Lab được trang bị 40 máy trạm cấu hình cao (GPU RTX 4080), hệ thống Kubernetes cluster 20 nodes, kết nối trực tiếp với AWS Academy và Google Cloud for Education. Sinh viên có thể thực hành các tác vụ huấn luyện mô hình Deep Learning và triển khai ứng dụng microservices trên môi trường cloud thực tế.",
                    eduCat, admin, 178L, true
                ),
                new NewsData(
                    "Hội thảo 'Xu hướng Nghề nghiệp IT 2027' — Cơ hội kết nối với 30 nhà tuyển dụng",
                    "Sự kiện thường niên lớn nhất của UTH quy tụ 30 doanh nghiệp công nghệ và hơn 500 sinh viên.",
                    "Ngày 25/10/2026, Khoa CNTT UTH sẽ tổ chức Hội thảo 'Xu hướng Nghề nghiệp IT 2027' tại Hội trường A1. Sự kiện quy tụ đại diện từ 30 doanh nghiệp hàng đầu như FPT Software, VNG Corporation, Tiki, Grab Vietnam và nhiều startup công nghệ nổi bật. Chương trình bao gồm: phiên hội thảo chuyên môn về xu hướng AI/ML, blockchain và cloud-native; gian hàng tuyển dụng trực tiếp; và buổi mock interview dành cho sinh viên năm cuối.",
                    eduCat, author, 267L, true
                ),
                new NewsData(
                    "UTH công bố chương trình đào tạo liên kết quốc tế với Đại học RMIT Úc",
                    "Sinh viên UTH có cơ hội lấy bằng kép với Đại học RMIT thông qua chương trình 2+2.",
                    "Trong khuôn khổ chuyến thăm của Đoàn giáo dục Đại học RMIT Australia, Ban giám hiệu UTH và Hiệu trưởng RMIT Vietnam đã ký kết Biên bản ghi nhớ (MOU) về chương trình đào tạo liên kết quốc tế 2+2. Theo đó, sinh viên UTH hoàn thành xuất sắc 2 năm đầu có thể chuyển tiếp sang học tập và tốt nghiệp tại RMIT với mức học phí ưu đãi 30%. Chương trình áp dụng cho các ngành: IT, Business Administration và Engineering.",
                    eduCat, admin, 534L, true
                ),
                new NewsData(
                    "Seminar 'Clean Code & SOLID Principles' với chuyên gia từ Google Việt Nam",
                    "Buổi chia sẻ kỹ thuật chuyên sâu dành cho sinh viên IT UTH yêu thích phát triển phần mềm chuyên nghiệp.",
                    "Khoa Công nghệ thông tin UTH phối hợp với Google Việt Nam tổ chức buổi Seminar chuyên đề 'Clean Code & SOLID Principles trong phát triển phần mềm hiện đại'. Diễn giả là ông Nguyễn Hữu Đức — Software Engineer tại Google với 12 năm kinh nghiệm — sẽ chia sẻ về các nguyên tắc viết code sạch, pattern thiết kế và cách review code hiệu quả trong môi trường doanh nghiệp. Tham dự: Miễn phí, đăng ký qua link trong bio fanpage UTH IT.",
                    eduCat, author, 389L, true
                ),
                new NewsData(
                    "Sinh viên UTH nghiên cứu ứng dụng IoT trong quản lý giao thông thông minh",
                    "Đề tài nghiên cứu khoa học cấp Bộ được đánh giá xuất sắc, mở ra tiềm năng ứng dụng thực tiễn tại TP.HCM.",
                    "Nhóm sinh viên Khoa Điện tử - Viễn thông UTH vừa bảo vệ thành công đề tài nghiên cứu khoa học cấp Bộ về 'Hệ thống quản lý giao thông thông minh sử dụng IoT và AI'. Hệ thống tích hợp camera thông minh, cảm biến áp lực và thuật toán xử lý ảnh để tự động điều phối tín hiệu đèn giao thông theo mật độ xe thực tế, giảm ùn tắc lên tới 32% trong các thử nghiệm mô phỏng. Sở Giao thông vận tải TP.HCM đã bày tỏ quan tâm thí điểm tại 3 nút giao thông trọng điểm.",
                    eduCat, admin, 623L, true
                ),
                new NewsData(
                    "Lịch thi học kỳ 1 năm học 2026-2027 và hướng dẫn đăng ký thi lại",
                    "Phòng Đào tạo thông báo lịch thi chính thức và quy trình đăng ký thi cải thiện điểm.",
                    "Phòng Đào tạo Trường UTH thông báo lịch thi kết thúc học phần Học kỳ 1, năm học 2026-2027 diễn ra từ ngày 10/12/2026 đến 05/01/2027. Sinh viên cần lưu ý: (1) Kiểm tra và xác nhận lịch thi trên hệ thống trước ngày 05/12; (2) Đăng ký thi lại/thi cải thiện từ 01-07/12; (3) Mang thẻ sinh viên và giấy tờ tùy thân khi dự thi. Mọi thắc mắc liên hệ Phòng Đào tạo qua email: daotao@uth.edu.vn.",
                    eduCat, author, 445L, true
                ),
                new NewsData(
                    "Cuộc thi Hackathon UTH 2026: 48 giờ xây dựng giải pháp cho giao thông đô thị",
                    "Sự kiện lập trình marathon lần đầu tiên được tổ chức tại UTH với giải thưởng tổng trị giá 50 triệu đồng.",
                    "UTH Hackathon 2026 là cuộc thi lập trình 48 giờ không ngừng nghỉ đầu tiên được tổ chức tại Trường Đại học Giao thông vận tải TP.HCM. Với chủ đề 'Smart Mobility — Giải pháp giao thông tương lai', các đội thi gồm 3-5 thành viên sẽ phát triển ứng dụng hoặc hệ thống giải quyết bài toán giao thông đô thị tại TP.HCM. Giải Nhất: 20 triệu đồng + cơ hội internship tại Grab Vietnam. Giải Nhì: 15 triệu. Giải Ba: 10 triệu. Đăng ký: 01-20/10/2026.",
                    eduCat, admin, 712L, true
                ),

                // ===== CHUYÊN MỤC: CÔNG NGHỆ (16 bài) =====
                new NewsData(
                    "Xu hướng phát triển REST API với Spring Boot 3 và Java 17",
                    "Tìm hiểu kiến trúc Stateless Authentication với Spring Security 6 và JWT trong hệ sinh thái Spring hiện đại.",
                    "Spring Boot 3 mang lại hiệu năng vượt trội với sự hỗ trợ native compilation thông qua GraalVM, giảm thời gian khởi động ứng dụng xuống còn dưới 100ms. Kết hợp với Java 17 LTS, các tính năng như Records, Sealed Classes và Pattern Matching giúp code ngắn gọn và type-safe hơn. Spring Security 6 cũng được redesign hoàn toàn với Lambda DSL, loại bỏ WebSecurityConfigurerAdapter khỏi API. JWT Authentication với JJWT 0.12 hỗ trợ đầy đủ RS256, ES256 và HS256, đáp ứng mọi yêu cầu bảo mật enterprise.",
                    techCat, author, 350L, true
                ),
                new NewsData(
                    "PostgreSQL Full-Text Search: Giải pháp tìm kiếm hiệu năng cao cho ứng dụng web",
                    "Hướng dẫn triển khai tsvector, tsquery và GIN index để thay thế LIKE trong truy vấn tìm kiếm.",
                    "Khi ứng dụng web có hàng triệu bản ghi, câu truy vấn LIKE '%keyword%' trở thành nút thắt cổ chai nghiêm trọng vì không thể sử dụng B-tree index. PostgreSQL Full-Text Search (FTS) giải quyết triệt để vấn đề này: (1) Cột tsvector STORED GENERATED tự động cập nhật khi dữ liệu thay đổi; (2) GIN index giúp tìm kiếm đạt O(log N) thay vì O(N); (3) ts_rank() cho phép sắp xếp kết quả theo độ liên quan. Trong benchmark với 1 triệu bản ghi, FTS nhanh hơn LIKE tới 200 lần.",
                    techCat, admin, 780L, true
                ),
                new NewsData(
                    "Docker & Kubernetes: Roadmap từ Container hóa đến Orchestration chuyên nghiệp",
                    "Lộ trình học Docker và Kubernetes từ cơ bản đến triển khai production với GitOps workflow.",
                    "Docker đã thay đổi hoàn toàn cách chúng ta xây dựng và triển khai ứng dụng. Với multi-stage build, một ứng dụng Spring Boot có thể được đóng gói thành image chỉ ~180MB. Kubernetes (K8s) đưa việc quản lý container lên tầm cao mới với tính năng auto-scaling, self-healing và rolling update zero-downtime. Trong bài này, chúng ta sẽ đi qua: Dockerfile tối ưu, Docker Compose cho môi trường dev, Helm charts cho K8s deployment, và GitOps workflow với ArgoCD.",
                    techCat, author, 520L, true
                ),
                new NewsData(
                    "Microservices vs Monolith: Khi nào nên và không nên chuyển đổi kiến trúc?",
                    "Phân tích thực tế các trade-offs giữa kiến trúc Monolith và Microservices dựa trên quy mô dự án.",
                    "Không phải mọi dự án đều cần Microservices. Monolith-first vẫn là lựa chọn đúng đắn cho startup và MVP nhờ độ phức tạp thấp, dễ debug và deploy. Tuy nhiên khi team vượt 8-10 người và codebase vượt 500KLOC, Microservices trở nên cần thiết: independent scaling, technology diversity và fault isolation. Netflix, Amazon, Uber đều bắt đầu từ Monolith và chuyển dần sang Microservices khi scale. Bài viết phân tích 5 dấu hiệu cho thấy đã đến lúc refactor kiến trúc.",
                    techCat, admin, 634L, true
                ),
                new NewsData(
                    "Redis Cache: Chiến lược tối ưu hiệu năng API với Cache-Aside và Write-Through",
                    "Hướng dẫn tích hợp Spring Cache + Redis để giảm latency API từ 200ms xuống còn 5ms.",
                    "Redis là giải pháp caching in-memory hàng đầu với tốc độ đọc/ghi lên tới 1 triệu operations/giây. Trong ứng dụng Spring Boot, Spring Cache abstraction cho phép thêm caching chỉ với annotation @Cacheable, @CachePut và @CacheEvict. Cache-Aside Pattern (Lazy Loading): dữ liệu chỉ được cache khi được truy vấn lần đầu — phù hợp với read-heavy workload. Write-Through Pattern: cache được cập nhật đồng thời với database — đảm bảo tính nhất quán. Benchmark thực tế cho thấy cache hit rate 85% giúp giảm tải database 6 lần.",
                    techCat, author, 445L, true
                ),
                new NewsData(
                    "ChatGPT và các mô hình LLM: Tác động đến ngành lập trình phần mềm",
                    "Phân tích cách AI coding assistant đang thay đổi workflow của lập trình viên và những kỹ năng cần thiết trong tương lai.",
                    "Sự xuất hiện của ChatGPT, GitHub Copilot và Cursor AI đang tái định nghĩa công việc của lập trình viên. Các khảo sát cho thấy 73% developer sử dụng AI tools hàng ngày, năng suất tăng trung bình 35-55% cho các tác vụ viết code, debug và viết test. Tuy nhiên, AI không thể thay thế hoàn toàn: System design, architectural decisions và business logic vẫn đòi hỏi tư duy của con người. Kỹ năng 'Prompt Engineering' và khả năng đánh giá/review code AI là những competency mới cần phát triển.",
                    techCat, admin, 1250L, true
                ),
                new NewsData(
                    "Flyway Migration: Quản trị phiên bản Database Schema trong CI/CD pipeline",
                    "Hướng dẫn tích hợp Flyway với Spring Boot để đảm bảo schema database nhất quán trên mọi môi trường.",
                    "Việc dùng Hibernate ddl-auto=update trong production là một anti-pattern nguy hiểm: không có lịch sử thay đổi, không thể rollback, dễ gây mất dữ liệu khi thay đổi cột. Flyway giải quyết triệt để bằng cách quản lý schema như source code với versioned migration scripts. Mỗi file SQL được đánh version (V1, V2, V3...), Flyway tự động track và apply các migration chưa chạy. Tích hợp với Spring Boot Boot cực đơn giản: chỉ cần thêm dependency và đặt script vào classpath:db/migration. CI/CD pipeline tự động migrate khi deploy.",
                    techCat, author, 320L, true
                ),
                new NewsData(
                    "Golang vs Java: So sánh hiệu năng và use case trong phát triển Backend",
                    "Benchmarks thực tế và phân tích khi nào nên chọn Go và khi nào Java là lựa chọn tốt hơn.",
                    "Go và Java là hai lựa chọn phổ biến cho backend development, mỗi ngôn ngữ có điểm mạnh riêng. Go: thời gian compile nhanh, memory footprint nhỏ (~10MB/service), goroutines concurrency hiệu quả — phù hợp cho high-performance microservices và CLI tools. Java: hệ sinh thái khổng lồ (Maven Central), Spring Framework mạnh mẽ, JVM optimization qua JIT compilation — phù hợp cho enterprise applications phức tạp. Benchmark: Go xử lý 50K req/s với 50MB RAM; Java Spring Boot 40K req/s với 256MB RAM. Lựa chọn phụ thuộc vào team expertise và business requirements.",
                    techCat, admin, 567L, true
                ),
                new NewsData(
                    "GraphQL vs REST API: Chọn giao thức phù hợp cho dự án của bạn",
                    "So sánh chi tiết hai phong cách thiết kế API phổ biến nhất hiện nay về hiệu năng, flexibility và độ phức tạp.",
                    "REST API với JSON và HTTP verbs là lựa chọn mặc định cho 90% dự án, dễ debug với Postman/curl, có chuẩn OpenAPI documentation. GraphQL do Facebook phát triển giải quyết vấn đề over-fetching và under-fetching của REST: client chỉ nhận đúng fields mình cần, một query có thể lấy dữ liệu từ nhiều resources. Nhược điểm GraphQL: N+1 query problem, khó cache HTTP, phức tạp hơn để setup. Khuyến nghị: REST cho public APIs và simple CRUD; GraphQL cho client-driven APIs với multiple consumers (web + mobile).",
                    techCat, author, 423L, true
                ),
                new NewsData(
                    "Tối ưu hóa JVM: Garbage Collection tuning và Memory management cho Spring Boot",
                    "Hướng dẫn cấu hình JVM flags, chọn GC algorithm phù hợp và giám sát heap memory hiệu quả.",
                    "JVM tuning là nghệ thuật cân bằng giữa throughput, latency và memory footprint. G1GC là lựa chọn mặc định từ Java 9, phù hợp cho most workloads. ZGC (từ Java 15) đạt latency dưới 1ms kể cả với heap 16GB — lý tưởng cho low-latency services. Các JVM flags quan trọng: -Xms/-Xmx (heap size), -XX:+UseZGC (GC algorithm), -XX:MaxGCPauseMillis (pause time goal). Dùng JVisualVM hoặc Grafana + Micrometer để monitor GC metrics trong production. Out-of-Memory thường do memory leak trong connection pools hoặc static collections.",
                    techCat, admin, 298L, true
                ),
                new NewsData(
                    "Blockchain trong logistics: Ứng dụng thực tế tại các doanh nghiệp vận tải Việt Nam",
                    "Các công ty logistics Việt Nam đang ứng dụng Blockchain để tăng tính minh bạch và truy xuất nguồn gốc hàng hóa.",
                    "Blockchain không chỉ là nền tảng cho cryptocurrency mà đang cách mạng hóa ngành logistics. Tại Việt Nam, Viettel Post và VNPT-Logistics đã thí điểm hệ thống truy xuất nguồn gốc hàng hóa trên Hyperledger Fabric. Smart contracts tự động thực thi điều khoản hợp đồng khi hàng đến cảng, giảm tranh chấp và thủ tục giấy tờ 60%. Với ngành xuất khẩu nông sản, blockchain giúp người mua quốc tế xác minh chứng nhận hữu cơ và truy xuất toàn bộ chuỗi cung ứng từ nông trại đến bàn ăn.",
                    techCat, author, 189L, true
                ),
                new NewsData(
                    "Cybersecurity 2026: Các vector tấn công phổ biến và cách phòng thủ hiệu quả",
                    "Báo cáo xu hướng an ninh mạng nửa cuối 2026 — từ Ransomware-as-a-Service đến tấn công Supply Chain.",
                    "Năm 2026 chứng kiến sự bùng nổ của Ransomware-as-a-Service (RaaS) với các nhóm hacker cung cấp toolkit tấn công như SaaS. Supply Chain Attack — như vụ SolarWinds — ngày càng tinh vi khi nhắm vào thư viện mã nguồn mở. Phishing với AI-generated content đạt tỷ lệ thành công cao hơn 40% so với trước. Các biện pháp phòng thủ hiệu quả: Zero Trust Architecture, MFA everywhere, SAST/DAST trong CI pipeline, và Employee Security Awareness Training. OWASP Top 10 2026 đã cập nhật với 3 mục mới liên quan đến AI-powered attacks.",
                    techCat, admin, 876L, true
                ),
                new NewsData(
                    "WebAssembly (WASM): Tương lai của lập trình web hiệu năng cao",
                    "WASM mang ngôn ngữ compiled như C++, Rust vào trình duyệt, mở ra kỷ nguyên mới cho web applications.",
                    "WebAssembly là định dạng binary có thể chạy trong trình duyệt với tốc độ gần native, giải phóng web khỏi giới hạn của JavaScript. Rust WASM cho phép viết các thư viện xử lý ảnh, mã hóa và video encoding chạy trực tiếp trên trình duyệt mà không cần server-side processing. Figma, Google Earth và AutoCAD đều sử dụng WASM cho các tác vụ đồ họa nặng. Với WASI (WebAssembly System Interface), WASM có thể chạy ngoài trình duyệt như một runtime độc lập, cạnh tranh trực tiếp với Docker container cho serverless functions.",
                    techCat, author, 234L, true
                ),
                new NewsData(
                    "Serverless Architecture với AWS Lambda: Chi phí tối ưu và Auto-scaling tự động",
                    "So sánh Serverless vs Container-based deployment và khi nào Cold Start là vấn đề cần giải quyết.",
                    "AWS Lambda cho phép chạy code mà không cần quản lý server, tính phí theo đơn vị 1ms thực thi — lý tưởng cho workload không đều (event-driven, batch processing). Ưu điểm: zero infrastructure management, auto-scaling đến hàng nghìn concurrent executions, pay-as-you-go. Nhược điểm: Cold Start latency (100ms-2s cho JVM) có thể gây vấn đề cho real-time APIs. Giải pháp: Provisioned Concurrency (tốn phí), GraalVM native image compilation (giảm cold start <50ms), hoặc chuyển sang ngôn ngữ khởi động nhanh như Go/Rust.",
                    techCat, admin, 312L, true
                ),
                new NewsData(
                    "Apache Kafka: Xây dựng hệ thống Event-Driven Architecture chịu tải cao",
                    "Hướng dẫn thiết kế messaging pipeline với Kafka Streams, Consumer Groups và exactly-once semantics.",
                    "Apache Kafka là distributed event streaming platform xử lý hàng triệu messages/giây với độ trễ dưới 10ms. Kafka được thiết kế để không bao giờ mất dữ liệu (durable log) và có thể replay messages — điều REST API không thể làm. Trong kiến trúc Event-Driven, Producer publish events vào Topics, multiple Consumer Groups có thể xử lý độc lập. Kafka Streams cho phép xử lý và transform stream data ngay trong Kafka mà không cần hệ thống riêng. Spring Kafka integration cực kỳ đơn giản với @KafkaListener và KafkaTemplate. Use case: Order processing, real-time analytics, CDC (Change Data Capture).",
                    techCat, author, 401L, true
                ),
                new NewsData(
                    "Giới thiệu Rust: Ngôn ngữ lập trình an toàn bộ nhớ đang chinh phục cộng đồng developer",
                    "Tại sao Microsoft, Google và Linux Kernel đều đang chuyển sang Rust để thay thế C/C++?",
                    "Rust là ngôn ngữ lập trình systems programming được thiết kế để an toàn bộ nhớ mà không cần Garbage Collector. Ownership model của Rust phát hiện memory leaks, dangling pointers và data races ngay tại compile time — loại bỏ 70% lỗi bảo mật phổ biến nhất. Microsoft Research phát hiện 70% CVE Windows từ 2004-2018 là memory safety issues. Google Android đã dùng Rust cho 21% new code. Linux Kernel 6.1 tích hợp Rust driver đầu tiên. Với performance ngang C/C++ nhưng an toàn hơn, Rust đang định hình lại ngành công nghiệp phần mềm.",
                    techCat, admin, 543L, true
                ),

                // ===== CHUYÊN MỤC: THỜI SỰ (12 bài) =====
                new NewsData(
                    "TP.HCM ra mắt hệ thống giao thông thông minh tích hợp AI tại 50 nút giao",
                    "Dự án Smart Traffic Management trị giá 200 tỷ đồng được triển khai thí điểm tại các quận trung tâm.",
                    "Ủy ban Nhân dân TP.HCM vừa chính thức ra mắt Hệ thống Quản lý Giao thông Thông minh (ITS) tích hợp AI tại 50 nút giao trọng điểm thuộc các quận 1, 3, 5, 10 và Bình Thạnh. Hệ thống sử dụng camera AI nhận diện phương tiện, đếm mật độ giao thông và tự động điều chỉnh thời gian đèn tín hiệu. Trong 3 tháng vận hành thử, tốc độ lưu thông trung bình tăng 18%, thời gian chờ tại các nút đèn giảm 25%. Giai đoạn 2 sẽ mở rộng ra 200 nút giao toàn thành phố vào năm 2027.",
                    newsCat, admin, 1450L, true
                ),
                new NewsData(
                    "Việt Nam ký kết Hiệp định Thương mại Tự do với 5 quốc gia ASEAN mới",
                    "FTA thế hệ mới mở ra cơ hội xuất khẩu lớn cho hàng hóa và dịch vụ công nghệ Việt Nam.",
                    "Trong khuôn khổ Hội nghị Thượng đỉnh ASEAN lần thứ 44 tại Kuala Lumpur, Việt Nam đã ký kết Hiệp định Thương mại Tự do thế hệ mới với Indonesia, Malaysia, Philippines, Brunei và Singapore. Hiệp định bao gồm các cam kết về: giảm thuế quan xuống 0% cho 85% mặt hàng, mở cửa thị trường dịch vụ tài chính-ngân hàng, và lần đầu tiên có chương riêng về Kinh tế số và Thương mại điện tử. Các chuyên gia kinh tế dự báo xuất khẩu phần mềm và dịch vụ CNTT của Việt Nam sẽ tăng 40% trong 3 năm tới.",
                    newsCat, author, 987L, true
                ),
                new NewsData(
                    "Luật An toàn Thông tin mạng sửa đổi 2026: Những thay đổi quan trọng doanh nghiệp cần biết",
                    "Các quy định mới về bảo vệ dữ liệu cá nhân và yêu cầu lưu trữ dữ liệu trong nước.",
                    "Quốc hội Việt Nam vừa thông qua Luật An toàn Thông tin mạng sửa đổi 2026 với nhiều điểm mới quan trọng. Nổi bật là yêu cầu doanh nghiệp xử lý dữ liệu cá nhân phải có Data Protection Officer (DPO), thực hiện Privacy Impact Assessment trước khi ra mắt sản phẩm mới, và báo cáo sự cố rò rỉ dữ liệu trong vòng 72 giờ. Các nền tảng mạng xã hội và thương mại điện tử có hơn 1 triệu người dùng tại Việt Nam phải lưu trữ dữ liệu người dùng Việt Nam trên server đặt tại Việt Nam hoặc tại Data Center được cấp phép.",
                    newsCat, admin, 756L, true
                ),
                new NewsData(
                    "Kinh tế số Việt Nam 2026: Quy mô đạt 45 tỷ USD, dẫn đầu Đông Nam Á",
                    "Báo cáo e-Conomy SEA 2026 ghi nhận Việt Nam là nền kinh tế số tăng trưởng nhanh nhất khu vực.",
                    "Theo báo cáo e-Conomy SEA 2026 do Google, Temasek và Bain & Company phối hợp thực hiện, quy mô kinh tế số Việt Nam đạt 45 tỷ USD Gross Merchandise Value (GMV) — tăng 31% so với năm 2025 và lần đầu tiên vượt Thái Lan để đứng thứ 2 ASEAN sau Indonesia. Thương mại điện tử chiếm 28 tỷ USD, tiếp theo là Fintech (9 tỷ), Du lịch trực tuyến (5 tỷ) và Media trực tuyến (3 tỷ). Dự báo đến 2030, kinh tế số Việt Nam sẽ đạt 100 tỷ USD.",
                    newsCat, author, 1120L, true
                ),
                new NewsData(
                    "VinFast xuất khẩu 50.000 xe điện sang thị trường Bắc Mỹ và châu Âu trong năm 2026",
                    "Thương hiệu ô tô điện Việt Nam tiếp tục mở rộng mạng lưới dealership tại Mỹ, Canada và Đức.",
                    "VinFast thông báo đã xuất khẩu thành công 50.000 xe điện trong năm 2026, vượt 25% kế hoạch đề ra. Mẫu VF9 (SUV 7 chỗ) là sản phẩm bán chạy nhất tại Mỹ với hơn 18.000 đơn hàng, trong khi VF5 Plus chiếm 60% thị phần tại các thị trường Đông Nam Á. VinFast cũng vừa khai trương nhà máy sản xuất tại North Carolina (Mỹ) với công suất 150.000 xe/năm, tạo 7.500 việc làm trực tiếp. Đây là cột mốc lịch sử đánh dấu Việt Nam trở thành nhà xuất khẩu ô tô điện đầu tiên tại ASEAN.",
                    newsCat, admin, 2340L, true
                ),
                new NewsData(
                    "Chính phủ Việt Nam phê duyệt Chiến lược Quốc gia về Trí tuệ Nhân tạo đến năm 2030",
                    "Kế hoạch đầu tư 5.000 tỷ đồng xây dựng hạ tầng AI và đào tạo 50.000 chuyên gia AI đến năm 2030.",
                    "Chính phủ vừa phê duyệt Chiến lược Quốc gia về Trí tuệ Nhân tạo đến năm 2030 với tổng ngân sách đầu tư 5.000 tỷ đồng. Mục tiêu chính: (1) Đào tạo 50.000 kỹ sư AI và Data Science; (2) Xây dựng 3 Trung tâm Đổi mới Sáng tạo AI tại Hà Nội, TP.HCM và Đà Nẵng; (3) Thúc đẩy ứng dụng AI trong 10 lĩnh vực ưu tiên gồm Y tế, Giáo dục, Nông nghiệp và Giao thông; (4) Đưa Việt Nam vào nhóm 30 quốc gia dẫn đầu về AI toàn cầu. Bộ TT&TT được giao làm cơ quan chủ trì triển khai chiến lược.",
                    newsCat, author, 1567L, true
                ),
                new NewsData(
                    "Tuyến Metro số 1 Bến Thành - Suối Tiên chính thức vận hành thương mại toàn tuyến",
                    "Sau nhiều lần trì hoãn, tuyến metro đầu tiên của TP.HCM đã sẵn sàng phục vụ hành khách 24/7.",
                    "Tuyến Metro số 1 (Bến Thành - Suối Tiên) dài 19,7km với 14 ga chính thức vận hành thương mại toàn tuyến từ ngày 01/10/2026. Trong tuần đầu tiên, hệ thống phục vụ trung bình 200.000 lượt hành khách/ngày, vượt kế hoạch 30%. Giá vé được áp dụng theo khoảng cách: từ 12.000đ đến 20.000đ/lượt. Thẻ tháng 300.000đ/tháng. Tốc độ tối đa 110km/h, thời gian chạy suốt tuyến 30 phút. Ban quản lý đường sắt đô thị TP.HCM cho biết sẽ tăng tần suất từ 10 phút/chuyến lên 5 phút/chuyến vào giờ cao điểm.",
                    newsCat, admin, 3456L, true
                ),
                new NewsData(
                    "Lũ lụt nghiêm trọng tại miền Trung: Hàng nghìn hộ dân được sơ tán khẩn cấp",
                    "Mưa lớn kéo dài gây ngập lụt diện rộng tại các tỉnh Quảng Nam, Quảng Ngãi và Bình Định.",
                    "Hoàn lưu bão số 6 gây mưa lớn liên tục trong 5 ngày tại các tỉnh miền Trung, khiến nhiều khu vực ngập sâu 1-3 mét. Tính đến sáng 16/10/2026, đã có 12.000 hộ dân tại Quảng Nam, Quảng Ngãi và Bình Định phải sơ tán. Thiệt hại về nhà cửa ước tính 500 tỷ đồng. Lực lượng quân đội, công an và Hội Chữ thập đỏ đang triển khai cứu hộ và cung cấp lương thực, nước sạch cho người dân. Chính phủ đã quyết định hỗ trợ khẩn cấp 200 tỷ đồng cho các tỉnh bị thiệt hại.",
                    newsCat, author, 4567L, true
                ),
                new NewsData(
                    "TPHCM khởi công dự án cầu Thủ Thiêm 4 — Biểu tượng kiến trúc mới của thành phố",
                    "Cây cầu dây văng trị giá 5.000 tỷ đồng kết nối Quận 7 và Khu đô thị mới Thủ Thiêm.",
                    "Thành phố Hồ Chí Minh đã khởi công Dự án Cầu Thủ Thiêm 4 với tổng mức đầu tư 5.000 tỷ đồng từ ngân sách thành phố. Cầu dây văng 6 làn xe dài 2,1km sẽ kết nối đường Nguyễn Văn Linh (Quận 7) với Khu đô thị mới Thủ Thiêm (TP. Thủ Đức), giảm áp lực giao thông cho cầu Phú Mỹ hiện đang quá tải. Thiết kế kiến trúc do liên danh tư vấn Pháp-Việt thực hiện với hình dáng cánh buồm biểu tượng cho tinh thần phát triển của TP.HCM. Dự kiến hoàn thành năm 2029.",
                    newsCat, admin, 1234L, true
                ),
                new NewsData(
                    "Giá xăng dầu thế giới biến động mạnh — Tác động đến kinh tế Việt Nam",
                    "Giá dầu Brent tăng 15% trong tháng 10/2026 do căng thẳng địa chính trị Trung Đông.",
                    "Căng thẳng leo thang tại Trung Đông đẩy giá dầu Brent tăng từ 78 USD lên 90 USD/thùng trong tháng 10/2026 — mức cao nhất trong 2 năm qua. Tại Việt Nam, theo quy định điều chỉnh giá mỗi 10 ngày, giá xăng RON 95 đã tăng 3.500 đồng/lít lên mức 25.000 đồng/lít. Các chuyên gia kinh tế cảnh báo nếu giá dầu duy trì mức 90+ USD, lạm phát Việt Nam có thể vượt ngưỡng 4% mục tiêu. Chính phủ đang xem xét điều chỉnh Quỹ Bình ổn xăng dầu để hạn chế tác động đến người tiêu dùng.",
                    newsCat, author, 876L, true
                ),
                new NewsData(
                    "Bộ Y tế phê duyệt 3 vaccine nội địa mới phòng bệnh tay chân miệng và sốt xuất huyết",
                    "Việt Nam trở thành quốc gia đầu tiên trong ASEAN sản xuất thành công vaccine sốt xuất huyết.",
                    "Hội đồng Chuyên môn Bộ Y tế vừa phê duyệt 3 vaccine nội địa mới: (1) DENGVAX-VN — vaccine sốt xuất huyết do Vabiotech sản xuất, hiệu quả bảo vệ 87%; (2) HFMV-01 — vaccine tay chân miệng phòng cả 4 chủng EV71 và CV-A16; (3) RSV-VN — vaccine bảo vệ trẻ sơ sinh và người cao tuổi khỏi RSV. Đây là bước đột phá lịch sử của ngành y tế Việt Nam sau 15 năm nghiên cứu và thử nghiệm lâm sàng. Vaccine sẽ được đưa vào Chương trình Tiêm chủng Mở rộng từ quý 1/2027.",
                    newsCat, admin, 2345L, true
                ),
                new NewsData(
                    "Thị trường bất động sản TP.HCM quý 3/2026: Phân khúc căn hộ dưới 2 tỷ khan hiếm nguồn cung",
                    "Báo cáo thị trường BĐS quý 3 cho thấy giá căn hộ tăng 8% so với cùng kỳ, thanh khoản cải thiện rõ rệt.",
                    "Theo báo cáo thị trường bất động sản TP.HCM quý 3/2026 của CBRE Việt Nam, phân khúc căn hộ dưới 2 tỷ đồng gần như không còn nguồn cung mới tại TP.HCM, khi chi phí đất và xây dựng liên tục tăng. Giá căn hộ trung bình tăng 8% so với Q3/2025, đạt 65 triệu đồng/m² tại khu vực trung tâm. Thanh khoản cải thiện với tỷ lệ hấp thụ 78%, tăng từ mức 62% quý trước. Khu vực TP. Thủ Đức và Bình Dương thu hút nhiều nhà đầu tư do quỹ đất còn rộng và hạ tầng kết nối ngày càng hoàn thiện.",
                    newsCat, author, 654L, true
                )
            );

            int count = 0;
            for (NewsData nd : newsDataList) {
                News news = new News();
                news.setTitle(nd.title());
                news.setSlug(NewsService.toSlug(nd.title()));
                news.setSummary(nd.summary());
                news.setContent(nd.content());
                news.setIsPublished(nd.isPublished());
                news.setViewCount(nd.viewCount());
                news.setCategory(nd.category());
                news.setAuthor(nd.author());
                newsRepository.save(news);
                count++;
            }

            logger.info("✅ Seed hoàn tất: 2 Users, 3 Categories, {} bài viết mẫu!", count);
        } else {
            logger.info("Database đã có dữ liệu — bỏ qua bước seed.");
        }
    }

    /**
     * Record nội bộ để truyền dữ liệu bài viết gọn hơn trong danh sách khởi tạo.
     */
    private record NewsData(
            String title,
            String summary,
            String content,
            Category category,
            User author,
            Long viewCount,
            Boolean isPublished
    ) {}
}
