package com.waimai.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddItemRequest(
            @NotNull Long shopId,
            @NotNull Long skuId,
            @NotNull @Min(1) @Max(99) Integer quantity) {
    }

    public record UpdateQuantityRequest(@NotNull @Min(1) @Max(99) Integer quantity) {
    }

    public record CartItem(
            Long itemId, Long productId, Long skuId, String productName, String skuName,
            BigDecimal unitPrice, Integer quantity, BigDecimal subtotal, Integer stock) {
    }

    public record CartView(Long cartId, Long shopId, String shopName, List<CartItem> items, BigDecimal totalAmount) {
    }
}
