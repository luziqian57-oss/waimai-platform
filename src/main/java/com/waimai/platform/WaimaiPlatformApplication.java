package com.waimai.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class WaimaiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaimaiPlatformApplication.class, args);
    }
}
