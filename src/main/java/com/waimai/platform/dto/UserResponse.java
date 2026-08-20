package com.waimai.platform.dto;

import com.waimai.platform.model.User;

public record UserResponse(Long id, String username, String nickname, String phone, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(), user.getRole());
    }
}
