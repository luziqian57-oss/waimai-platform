package com.waimai.platform.dto;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogResponse {

    private CatalogResponse() {
    }

    public record ShopSummary(
            Long id, String shopName, String logoUrl, String contactPhone, String address) {
    }

    public record ShopMenu(ShopSummary shop, List<CategoryView> categories) {
    }

    public record CategoryView(Long id, String categoryName, Integer sortNo, List<ProductView> products) {
    }

    public record ProductView(
            Long id, String productName, String description, String imageUrl, List<SkuView> skus) {
    }

    public record SkuView(Long id, String skuName, BigDecimal price, Integer stock) {
    }
}
