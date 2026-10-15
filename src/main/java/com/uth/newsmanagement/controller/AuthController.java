package com.uth.newsmanagement.controller;

import com.uth.newsmanagement.common.dto.ApiResponse;
import com.uth.newsmanagement.dto.auth.AuthResponse;
import com.uth.newsmanagement.dto.auth.LoginRequest;
import com.uth.newsmanagement.dto.auth.RegisterRequest;
import com.uth.newsmanagement.entity.User;
import com.uth.newsmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller Quản lý Đăng ký & Đăng nhập (Public API).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * API Đăng ký tài khoản
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        ApiResponse<String> response = ApiResponse.created(
                "Đăng ký tài khoản thành công với username: " + user.getUsername(),
                "Đăng ký thành công"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API Đăng nhập lấy JWT Bearer Token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        ApiResponse<AuthResponse> response = ApiResponse.success(
                authResponse,
                "Đăng nhập thành công"
        );
        return ResponseEntity.ok(response);
    }
}
