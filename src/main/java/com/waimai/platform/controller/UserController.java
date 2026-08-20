package com.waimai.platform.controller;

import com.waimai.platform.dto.UserResponse;
import com.waimai.platform.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
