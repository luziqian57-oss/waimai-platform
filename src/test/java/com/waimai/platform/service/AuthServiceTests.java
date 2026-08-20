package com.waimai.platform.service;

import com.waimai.platform.dto.LoginRequest;
import com.waimai.platform.dto.RegisterRequest;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.model.User;
import com.waimai.platform.security.JwtService;
import com.waimai.platform.security.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
class AuthServiceTests {

    private FakeUserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private FakeLoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() {
        userMapper = new FakeUserMapper();
        passwordEncoder = new BCryptPasswordEncoder();
        JwtService jwtService = new JwtService(
                "test-only-jwt-secret-with-at-least-thirty-two-bytes",
                120
        );
        loginRateLimiter = new FakeLoginRateLimiter();
        authService = new AuthService(userMapper, passwordEncoder, jwtService, loginRateLimiter);
    }

    @Test
    void registerHashesPasswordAndReturnsToken() {
        var response = authService.register(new RegisterRequest(
                "new_user", "TestPass2026!", "新用户", "13800138000"
        ));

        User savedUser = userMapper.insertedUser;
        assertNotEquals("TestPass2026!", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("TestPass2026!", savedUser.getPasswordHash()));
        assertEquals("Bearer", response.tokenType());
        assertEquals("new_user", response.user().username());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        userMapper.existingUser = activeUser("existing_user");

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(
                new RegisterRequest("existing_user", "TestPass2026!", "用户", "")
        ));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = activeUser("login_user");
        user.setPasswordHash(passwordEncoder.encode("CorrectPass2026!"));
        userMapper.existingUser = user;

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(
                new LoginRequest("login_user", "WrongPass2026!"), "127.0.0.1"
        ));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(1, loginRateLimiter.failureCount);
    }

    @Test
    void loginRejectsDisabledUser() {
        User user = activeUser("disabled_user");
        user.setPasswordHash(passwordEncoder.encode("CorrectPass2026!"));
        user.setStatus(0);
        userMapper.existingUser = user;

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(
                new LoginRequest("disabled_user", "CorrectPass2026!"), "127.0.0.1"
        ));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void successfulLoginClearsFailures() {
        User user = activeUser("login_user");
        user.setPasswordHash(passwordEncoder.encode("CorrectPass2026!"));
        userMapper.existingUser = user;

        var response = authService.login(
                new LoginRequest("login_user", "CorrectPass2026!"), "127.0.0.1"
        );

        assertEquals("login_user", response.user().username());
        assertEquals(1, loginRateLimiter.clearCount);
    }

    @Test
    void blockedLoginDoesNotQueryUserStore() {
        loginRateLimiter.blocked = true;

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(
                new LoginRequest("blocked_user", "AnyPass2026!"), "127.0.0.1"
        ));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatus());
        assertEquals(0, userMapper.findByUsernameCount);
    }

    private User activeUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setNickname("测试用户");
        user.setRole("USER");
        user.setStatus(1);
        return user;
    }

    private static class FakeUserMapper implements UserMapper {

        private User existingUser;
        private User insertedUser;
        private int findByUsernameCount;

        @Override
        public User findByUsername(String username) {
            findByUsernameCount++;
            if (existingUser != null && existingUser.getUsername().equals(username)) {
                return existingUser;
            }
            return null;
        }

        @Override
        public User findById(Long id) {
            if (existingUser != null && existingUser.getId().equals(id)) {
                return existingUser;
            }
            return null;
        }

        @Override
        public int insert(User user) {
            user.setId(10L);
            insertedUser = user;
            return 1;
        }
    }

    private static class FakeLoginRateLimiter implements LoginRateLimiter {

        private boolean blocked;
        private int failureCount;
        private int clearCount;

        @Override
        public void checkAllowed(String username, String clientAddress) {
            if (blocked) {
                throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试");
            }
        }

        @Override
        public void recordFailure(String username, String clientAddress) {
            failureCount++;
        }

        @Override
        public void clear(String username, String clientAddress) {
            clearCount++;
        }
    }
}
