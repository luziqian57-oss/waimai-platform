package com.waimai.platform.model;

import java.math.BigDecimal;

public final class CatalogRows {

    private CatalogRows() {
    }

    public record ShopRow(Long id, String shopName, String logoUrl, String contactPhone, String address) {
    }

    public record CategoryRow(Long id, Long shopId, String categoryName, Integer sortNo) {
    }

    public record ProductRow(
            Long id, Long shopId, Long categoryId, String productName, String description, String imageUrl) {
    }

    public record SkuRow(Long id, Long productId, String skuName, BigDecimal price, Integer stock) {
    }

    public record SkuDetailRow(
            Long skuId, Long productId, Long shopId, String productName, String skuName,
            BigDecimal price, Integer stock, Integer skuStatus, Integer productStatus, Integer shopStatus) {
    }
}
