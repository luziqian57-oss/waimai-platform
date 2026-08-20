package com.waimai.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class AdminCatalogDtos {

    private AdminCatalogDtos() {
    }

    public record ShopRequest(
            @NotBlank @Size(max = 100) String shopName,
            @Size(max = 500) String logoUrl,
            @Size(max = 20) String contactPhone,
            @Size(max = 255) String address,
            Boolean active) {
    }

    public record CategoryRequest(
            @NotNull Long shopId,
            @NotBlank @Size(max = 50) String categoryName,
            @NotNull @Min(0) Integer sortNo,
            Boolean active) {
    }

    public record ProductRequest(
            @NotNull Long shopId,
            @NotNull Long categoryId,
            @NotBlank @Size(max = 100) String productName,
            @Size(max = 500) String description,
            @Size(max = 500) String imageUrl,
            Boolean active) {
    }

    public record SkuRequest(
            @NotNull Long productId,
            @NotBlank @Size(max = 100) String skuName,
            @NotNull @DecimalMin(value = "0.01") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            Boolean active) {
    }

    public record AdminEntityResponse(Long id, String entityType, boolean active) {
    }
}
