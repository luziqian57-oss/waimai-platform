package com.waimai.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WechatLoginRequest(
        @NotBlank @Size(max = 128) String code,
        @Size(max = 50) String nickname
) {
}
