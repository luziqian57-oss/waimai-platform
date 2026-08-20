package com.waimai.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 50) String contactName,
        @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String contactPhone,
        @NotBlank @Size(max = 50) String province,
        @NotBlank @Size(max = 50) String city,
        @NotBlank @Size(max = 50) String district,
        @NotBlank @Size(max = 255) String detailAddress,
        Boolean isDefault
) {
}
