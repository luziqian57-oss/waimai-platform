package com.waimai.platform.security;

import com.waimai.platform.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Component
public class RedisLoginRateLimiter implements LoginRateLimiter {

    private static final String KEY_PREFIX = "auth:login:failure:";
    private static final DefaultRedisScript<Long> RECORD_FAILURE_SCRIPT = new DefaultRedisScript<>(
            """
            local attempts = redis.call('INCR', KEYS[1])
            if attempts == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return attempts
            """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final long maxFailures;
    private final long windowSeconds;

    public RedisLoginRateLimiter(
            StringRedisTemplate redisTemplate,
            @Value("${security.login-rate-limit.max-failures:5}") long maxFailures,
            @Value("${security.login-rate-limit.window-seconds:300}") long windowSeconds
    ) {
        if (maxFailures <= 0) {
            throw new IllegalArgumentException("登录最大失败次数必须大于 0");
        }
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("登录限流时间窗口必须大于 0 秒");
        }
        this.redisTemplate = redisTemplate;
        this.maxFailures = maxFailures;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public void checkAllowed(String username, String clientAddress) {
        String value = redisTemplate.opsForValue().get(key(username, clientAddress));
        if (value == null) {
            return;
        }
        try {
            if (Long.parseLong(value) >= maxFailures) {
                throw tooManyAttempts();
            }
        } catch (NumberFormatException exception) {
            redisTemplate.delete(key(username, clientAddress));
        }
    }

    @Override
    public void recordFailure(String username, String clientAddress) {
        Long attempts = redisTemplate.execute(
                RECORD_FAILURE_SCRIPT,
                List.of(key(username, clientAddress)),
                Long.toString(windowSeconds)
        );
        if (attempts != null && attempts >= maxFailures) {
            throw tooManyAttempts();
        }
    }

    @Override
    public void clear(String username, String clientAddress) {
        redisTemplate.delete(key(username, clientAddress));
    }

    private String key(String username, String clientAddress) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        String normalizedAddress = clientAddress == null ? "unknown" : clientAddress;
        String input = normalizedUsername + "\n" + normalizedAddress;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-256", exception);
        }
    }

    private BusinessException tooManyAttempts() {
        return new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试");
    }
}
