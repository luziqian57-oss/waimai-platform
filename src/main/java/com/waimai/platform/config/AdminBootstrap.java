package com.waimai.platform.config;

import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.bootstrap.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminBootstrap(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            @Value("${admin.bootstrap.username:admin}") String username,
            @Value("${admin.bootstrap.password:}") String password) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (password == null || password.length() < 12) {
            throw new IllegalStateException("启用管理员初始化时，ADMIN_BOOTSTRAP_PASSWORD 至少需要12个字符");
        }
        if (userMapper.findByUsername(username) != null) {
            return;
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setNickname("平台管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        userMapper.insert(admin);
    }
}
