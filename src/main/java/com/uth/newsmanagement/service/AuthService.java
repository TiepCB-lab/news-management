package com.uth.newsmanagement.service;

import com.uth.newsmanagement.common.exception.BadRequestException;
import com.uth.newsmanagement.common.exception.AppException;
import com.uth.newsmanagement.dto.auth.AuthResponse;
import com.uth.newsmanagement.dto.auth.LoginRequest;
import com.uth.newsmanagement.dto.auth.RegisterRequest;
import com.uth.newsmanagement.entity.Role;
import com.uth.newsmanagement.entity.User;
import com.uth.newsmanagement.repository.UserRepository;
import com.uth.newsmanagement.security.JwtTokenProvider;
import com.uth.newsmanagement.security.UserPrincipal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

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
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Mật khẩu không được vượt quá 72 byte UTF-8 (giới hạn BCrypt)");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Tên đăng nhập đã được sử dụng!", HttpStatus.CONFLICT);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email đã được sử dụng!", HttpStatus.CONFLICT);
        }

        // Tạo tài khoản mới, mã hóa mật khẩu bằng BCrypt
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);

        try {
            // Flush để bắt cả trường hợp hai request cùng vượt qua bước kiểm tra trùng.
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException("Tên đăng nhập hoặc email đã được sử dụng", HttpStatus.CONFLICT);
        }
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Thông tin đăng nhập không hợp lệ");
        }
        // Xác thực thông tin qua Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Sinh JWT Token
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        return new AuthResponse(jwt, user.getUsername(), user.getEmail(),
                user.getAuthorities().iterator().next().getAuthority());
    }
}
