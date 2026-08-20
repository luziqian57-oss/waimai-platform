package com.waimai.platform.service;

import com.waimai.platform.dto.WechatLoginRequest;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.mapper.WechatIdentityMapper;
import com.waimai.platform.model.User;
import com.waimai.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WechatAuthServiceTests {

    private FakeIdentityMapper identityMapper;
    private WechatAuthService service;

    @BeforeEach
    void setUp() {
        identityMapper = new FakeIdentityMapper();
        service = new WechatAuthService(
                identityMapper,
                new FakeUserMapper(),
                new BCryptPasswordEncoder(),
                new JwtService("test-only-jwt-secret-with-at-least-thirty-two-bytes", 120),
                "",
                "",
                true
        );
    }

    @Test
    void mockWechatLoginCreatesThenReusesIdentity() {
        var first = service.login(new WechatLoginRequest("mock-stable-code", "微信用户甲"));
        var second = service.login(new WechatLoginRequest("mock-stable-code", "微信用户乙"));

        assertNotNull(first.accessToken());
        assertEquals(first.user().id(), second.user().id());
        assertEquals("微信用户甲", second.user().nickname());
        assertEquals(1, identityMapper.identities.size());
    }

    @Test
    void realWechatLoginRequiresConfiguration() {
        WechatAuthService unconfigured = new WechatAuthService(
                identityMapper,
                new FakeUserMapper(),
                new BCryptPasswordEncoder(),
                new JwtService("test-only-jwt-secret-with-at-least-thirty-two-bytes", 120),
                "",
                "",
                false
        );

        BusinessException exception = assertThrows(BusinessException.class,
                () -> unconfigured.login(new WechatLoginRequest("real-code", "用户")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    private static class FakeIdentityMapper implements WechatIdentityMapper {
        private final Map<String, User> identities = new HashMap<>();

        @Override
        public User findUserByOpenid(String openid) { return identities.get(openid); }

        @Override
        public int insert(Long userId, String openid) {
            User user = FakeUserMapper.USERS.values().stream()
                    .filter(value -> value.getId().equals(userId)).findFirst().orElseThrow();
            identities.put(openid, user);
            return 1;
        }
    }

    private static class FakeUserMapper implements UserMapper {
        private static final Map<String, User> USERS = new HashMap<>();
        private static long nextId = 1;

        FakeUserMapper() { USERS.clear(); }

        @Override
        public User findByUsername(String username) { return USERS.get(username); }

        @Override
        public User findById(Long id) {
            return USERS.values().stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public int insert(User user) {
            user.setId(nextId++);
            USERS.put(user.getUsername(), user);
            return 1;
        }
    }
}
