package com.waimai.platform.service;

import com.waimai.platform.dto.AuthResponse;
import com.waimai.platform.dto.LoginRequest;
import com.waimai.platform.dto.RegisterRequest;
import com.waimai.platform.dto.UserResponse;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.model.User;
import com.waimai.platform.security.JwtService;
import com.waimai.platform.security.LoginRateLimiter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final String dummyPasswordHash;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginRateLimiter loginRateLimiter
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
        this.dummyPasswordHash = passwordEncoder.encode("timing-check-only-password");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.findByUsername(request.username()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setPhone(normalizePhone(request.phone()));
        user.setRole("USER");
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名或手机号已存在");
        }
        return createAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request, String clientAddress) {
        loginRateLimiter.checkAllowed(request.username(), clientAddress);
        User user = userMapper.findByUsername(request.username());
        String passwordHash = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);
        if (user == null || !passwordMatches) {
            loginRateLimiter.recordFailure(request.username(), clientAddress);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号已被禁用");
        }
        loginRateLimiter.clear(request.username(), clientAddress);
        return createAuthResponse(user);
    }

    public UserResponse currentUser(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户不存在或已被禁用");
        }
        return UserResponse.from(user);
    }

    private AuthResponse createAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse("Bearer", token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone;
    }
}
