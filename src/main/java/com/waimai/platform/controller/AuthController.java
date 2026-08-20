package com.waimai.platform.controller;

import com.waimai.platform.dto.AuthResponse;
import com.waimai.platform.dto.LoginRequest;
import com.waimai.platform.dto.RegisterRequest;
import com.waimai.platform.dto.WechatLoginRequest;
import com.waimai.platform.service.AuthService;
import com.waimai.platform.service.WechatAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final WechatAuthService wechatAuthService;

    public AuthController(AuthService authService, WechatAuthService wechatAuthService) {
        this.authService = authService;
        this.wechatAuthService = wechatAuthService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest.getRemoteAddr());
    }

    @PostMapping("/wechat")
    public AuthResponse wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return wechatAuthService.login(request);
    }
}
