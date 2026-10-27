package com.uth.newsmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uth.newsmanagement.common.dto.ApiResponse;
import com.uth.newsmanagement.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật hệ thống với Spring Security 6.
 * Áp dụng mô hình RESTful API Stateless Session kết hợp Bearer JWT Token.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF vì REST API dùng Stateless JWT
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())

            // 2. Cấu hình Session không lưu trạng thái trên server (Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Xử lý lỗi trả về khi không xác thực (401 Unauthorized)
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ApiResponse<Void> apiResponse = ApiResponse.error(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Bạn cần đăng nhập (hoặc cung cấp Bearer token hợp lệ) để truy cập tài nguyên này"
                );
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            }).accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                ApiResponse<Void> apiResponse = ApiResponse.error(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Bạn không có quyền thực hiện thao tác này"
                );
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            }))

            // 4. Phân chia quyền hạn Public / Private APIs theo yêu cầu đề bài
            .authorizeHttpRequests(auth -> auth
                // Public APIs: Đăng ký, đăng nhập
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Public APIs: Đọc danh sách tin tức, chi tiết tin tức
                .requestMatchers("/api/v1/public/**").permitAll()
                // Public APIs: Xem file/ảnh upload tĩnh
                .requestMatchers("/uploads/**").permitAll()
                // Cho phép trang lỗi mặc định của Spring
                .requestMatchers("/error").permitAll()
                // Tất cả các API còn lại (CRUD tin tức, upload/xóa file) bắt buộc phải có Token
                .anyRequest().authenticated()
            );

        // 5. Thêm filter xác thực JWT trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
