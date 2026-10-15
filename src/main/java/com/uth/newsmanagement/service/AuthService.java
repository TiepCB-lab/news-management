package com.uth.newsmanagement.service;

import com.uth.newsmanagement.common.exception.BadRequestException;
import com.uth.newsmanagement.dto.auth.AuthResponse;
import com.uth.newsmanagement.dto.auth.LoginRequest;
import com.uth.newsmanagement.dto.auth.RegisterRequest;
import com.uth.newsmanagement.entity.Role;
import com.uth.newsmanagement.entity.User;
import com.uth.newsmanagement.repository.UserRepository;
import com.uth.newsmanagement.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý nghiệp vụ Đăng ký & Đăng nhập (Nhiệm vụ Backend).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã được sử dụng!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng!");
        }

        // Tạo tài khoản mới, mã hóa mật khẩu bằng BCrypt
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);

        // TODO: [BACKEND] Nếu là tài khoản đầu tiên, có thể cấp quyền ROLE_ADMIN nếu muốn.

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Xác thực thông tin qua Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh JWT Token
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername().trim())
                .or(() -> userRepository.findByEmail(request.getUsername().trim()))
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thông tin người dùng"));

        return new AuthResponse(jwt, user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
