package com.waimai.platform.service;

import com.waimai.platform.dto.AuthResponse;
import com.waimai.platform.dto.UserResponse;
import com.waimai.platform.dto.WechatLoginRequest;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.mapper.WechatIdentityMapper;
import com.waimai.platform.model.User;
import com.waimai.platform.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatAuthService {

    private final WechatIdentityMapper identityMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestClient restClient;
    private final String appId;
    private final String appSecret;
    private final boolean mockEnabled;

    public WechatAuthService(
            WechatIdentityMapper identityMapper, UserMapper userMapper, PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${wechat.app-id:}") String appId,
            @Value("${wechat.app-secret:}") String appSecret,
            @Value("${wechat.mock-enabled:false}") boolean mockEnabled) {
        this.identityMapper = identityMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.restClient = RestClient.create();
        this.appId = appId;
        this.appSecret = appSecret;
        this.mockEnabled = mockEnabled;
    }

    @Transactional
    public AuthResponse login(WechatLoginRequest request) {
        String openid = exchangeOpenid(request.code());
        User user = identityMapper.findUserByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setUsername("wx_" + sha256(openid).substring(0, 20));
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setNickname(request.nickname() == null || request.nickname().isBlank() ? "微信用户" : request.nickname().trim());
            user.setRole("USER");
            user.setStatus(1);
            userMapper.insert(user);
            identityMapper.insert(user.getId(), openid);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号已被禁用");
        }
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse("Bearer", token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    @SuppressWarnings("unchecked")
    private String exchangeOpenid(String code) {
        if (mockEnabled && code.startsWith("mock-")) {
            return "mock_" + sha256(code).substring(0, 59);
        }
        if (appId.isBlank() || appSecret.isBlank()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "微信登录尚未配置");
        }
        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https").host("api.weixin.qq.com").path("/sns/jscode2session")
                            .queryParam("appid", appId).queryParam("secret", appSecret)
                            .queryParam("js_code", code).queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve().body(Map.class);
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "微信登录服务暂时不可用");
        }
        if (body == null || body.get("openid") == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "微信登录凭证无效");
        }
        return body.get("openid").toString();
    }

    private String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-256", exception);
        }
    }
}
