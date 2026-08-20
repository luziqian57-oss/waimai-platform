package com.waimai.platform.controller;

import com.waimai.platform.mapper.EnvironmentMapper;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final EnvironmentMapper environmentMapper;
    private final StringRedisTemplate redisTemplate;

    public HealthController(EnvironmentMapper environmentMapper, StringRedisTemplate redisTemplate) {
        this.environmentMapper = environmentMapper;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("application", "UP");
        status.put("mysql", environmentMapper.ping() == 1 ? "UP" : "DOWN");
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            status.put("redis", connection.ping());
        }
        return status;
    }
}
