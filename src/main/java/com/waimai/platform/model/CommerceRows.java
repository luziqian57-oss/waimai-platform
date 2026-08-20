package com.waimai.platform.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CommerceRows {

    private CommerceRows() {
    }

    public record CartRow(Long id, Long userId, Long shopId, String shopName) {
    }

    public record CartItemRow(
            Long itemId, Long cartId, Long productId, Long skuId, String productName, String skuName,
            BigDecimal unitPrice, Integer quantity, Integer stock, Integer skuStatus, Integer productStatus) {
    }

    public record OrderRow(
            Long id, String orderNo, Long userId, Long shopId, String shopName, Long addressId,
            BigDecimal totalAmount, BigDecimal deliveryFee, BigDecimal payableAmount, String orderStatus,
            String receiverName, String receiverPhone, String receiverAddress, String remark,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record OrderItemRow(
            Long id, Long orderId, Long productId, Long skuId, String productName, String skuName,
            BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {
    }
}
