package com.waimai.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotNull Long shopId,
            @NotNull Long addressId,
            @Size(max = 255) String remark) {
    }

    public record UpdateStatusRequest(@NotBlank String status) {
    }

    public record OrderItemView(
            Long id, Long productId, Long skuId, String productName, String skuName,
            BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {
    }

    public record OrderView(
            String orderNo, Long shopId, String shopName, BigDecimal totalAmount,
            BigDecimal deliveryFee, BigDecimal payableAmount, String orderStatus,
            String receiverName, String receiverPhone, String receiverAddress, String remark,
            LocalDateTime createdAt, LocalDateTime updatedAt, List<OrderItemView> items) {
    }
}
