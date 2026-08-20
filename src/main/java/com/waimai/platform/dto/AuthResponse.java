package com.waimai.platform.dto;

public record AuthResponse(String tokenType, String accessToken, long expiresIn, UserResponse user) {
}
