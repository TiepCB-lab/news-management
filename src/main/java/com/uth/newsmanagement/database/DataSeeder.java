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

/**
 * Khởi tạo dữ liệu mẫu ban đầu (Data Seeder).
 * 
 * TODO: [DATA] Partner chuyên Data có thể mở rộng class này để:
 * 1. Nạp thêm hàng chục bài viết mẫu đa dạng chủ đề.
 * 2. Viết migration Flyway / Liquibase thay thế cho CommandLineRunner khi lên production.
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

            // 1. Tạo tài khoản Admin & User mặc định
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

            // 2. Tạo Categories mẫu
            Category techCat = new Category("Công nghệ", "cong-nghe", "Tin tức về AI, Blockchain, Phần mềm");
            Category newsCat = new Category("Thời sự", "thoi-su", "Tin tức sự kiện đời sống kinh tế xã hội");
            Category eduCat = new Category("Giáo dục UTH", "giao-duc-uth", "Thông tin học thuật, thực tập doanh nghiệp");
            categoryRepository.save(techCat);
            categoryRepository.save(newsCat);
            categoryRepository.save(eduCat);

            // 3. Tạo một số bài viết mẫu
            News news1 = new News();
            news1.setTitle("Chào mừng sinh viên tham gia Học Kỳ Doanh Nghiệp UTH");
            news1.setSlug(NewsService.toSlug(news1.getTitle()));
            news1.setSummary("Chương trình hợp tác đào tạo giữa Trường Đại học Giao thông vận tải TP.HCM và Doanh nghiệp công nghệ.");
            news1.setContent("Nội dung chi tiết: Sinh viên được cọ xát thực tế với các bài toán Backend RESTful API, Database Optimization và Security...");
            news1.setIsPublished(true);
            news1.setViewCount(125L);
            news1.setCategory(eduCat);
            news1.setAuthor(admin);
            newsRepository.save(news1);

            News news2 = new News();
            news2.setTitle("Xu hướng phát triển REST API với Spring Boot 3 và Java 17");
            news2.setSlug(NewsService.toSlug(news2.getTitle()));
            news2.setSummary("Tìm hiểu kiến trúc Stateless Authentication với Spring Security 6 và JWT.");
            news2.setContent("Spring Boot 3 mang lại hiệu năng vượt trội, hỗ trợ native compilation và cấu hình bảo mật trực quan hơn bao giờ hết.");
            news2.setIsPublished(true);
            news2.setViewCount(350L);
            news2.setCategory(techCat);
            news2.setAuthor(author);
            newsRepository.save(news2);

            logger.info("Đã seed xong: 2 Users, 3 Categories, 2 Bài viết mẫu!");
        }
    }
}
