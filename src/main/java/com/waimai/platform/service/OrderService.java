package com.waimai.platform.service;

import com.waimai.platform.dto.OrderDtos.CreateOrderRequest;
import com.waimai.platform.dto.OrderDtos.OrderItemView;
import com.waimai.platform.dto.OrderDtos.OrderView;
import com.waimai.platform.dto.PageResponse;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.AddressMapper;
import com.waimai.platform.mapper.CartMapper;
import com.waimai.platform.mapper.CatalogMapper;
import com.waimai.platform.mapper.OrderMapper;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.model.Address;
import com.waimai.platform.model.CommerceRows.CartItemRow;
import com.waimai.platform.model.CommerceRows.OrderRow;
import com.waimai.platform.model.OrderEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("50.00");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");
    private static final Map<String, String> ADMIN_TRANSITIONS = Map.of(
            "PAID", "CONFIRMED",
            "CONFIRMED", "PREPARING",
            "PREPARING", "DELIVERING",
            "DELIVERING", "COMPLETED"
    );

    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final CatalogMapper catalogMapper;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;

    public OrderService(
            OrderMapper orderMapper, CartMapper cartMapper, CatalogMapper catalogMapper,
            AddressMapper addressMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.cartMapper = cartMapper;
        this.catalogMapper = catalogMapper;
        this.addressMapper = addressMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public OrderView create(String username, CreateOrderRequest request) {
        Long userId = userId(username);
        Address address = addressMapper.findOwned(request.addressId(), userId);
        if (address == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "收货地址不存在");
        }
        var cart = cartMapper.findCart(userId, request.shopId());
        if (cart == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "购物车为空");
        }
        List<CartItemRow> items = cartMapper.findItems(cart.id());
        if (items.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "购物车为空");
        }
        for (CartItemRow item : items) {
            if (item.productStatus() != 1 || item.skuStatus() != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, item.productName() + "已下架");
            }
            if (item.quantity() > item.stock()) {
                throw new BusinessException(HttpStatus.CONFLICT, item.productName() + "库存不足");
            }
        }

        BigDecimal total = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
        BigDecimal deliveryFee = total.compareTo(FREE_DELIVERY_THRESHOLD) >= 0
                ? BigDecimal.ZERO.setScale(2) : DELIVERY_FEE;

        OrderEntity order = new OrderEntity();
        order.setOrderNo(newOrderNo());
        order.setUserId(userId);
        order.setShopId(request.shopId());
        order.setAddressId(address.getId());
        order.setTotalAmount(total);
        order.setDeliveryFee(deliveryFee);
        order.setPayableAmount(total.add(deliveryFee));
        order.setOrderStatus("PENDING_PAYMENT");
        order.setReceiverName(address.getContactName());
        order.setReceiverPhone(address.getContactPhone());
        order.setReceiverAddress(fullAddress(address));
        order.setRemark(normalizeRemark(request.remark()));
        orderMapper.insertOrder(order);

        for (CartItemRow item : items) {
            if (catalogMapper.decreaseStock(item.skuId(), item.quantity()) == 0) {
                throw new BusinessException(HttpStatus.CONFLICT, item.productName() + "库存不足，请刷新购物车");
            }
            BigDecimal subtotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            orderMapper.insertItem(order.getId(), item.productId(), item.skuId(), item.productName(),
                    item.skuName(), item.unitPrice(), item.quantity(), subtotal);
        }
        cartMapper.clearItems(cart.id());
        return detail(username, order.getOrderNo());
    }

    public PageResponse<OrderView> list(String username, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Long userId = userId(username);
        List<OrderView> orders = orderMapper.findPage(userId, (safePage - 1) * safeSize, safeSize).stream()
                .map(this::view)
                .toList();
        return new PageResponse<>(orders, orderMapper.countByUser(userId), safePage, safeSize);
    }

    public OrderView detail(String username, String orderNo) {
        OrderRow order = orderMapper.findOwned(orderNo, userId(username));
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        return view(order);
    }

    @Transactional
    public OrderView pay(String username, String orderNo) {
        Long userId = userId(username);
        OrderRow order = orderMapper.findOwnedForUpdate(orderNo, userId);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.orderStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前订单状态不能支付");
        }
        transition(order, "PAID");
        return detail(username, orderNo);
    }

    @Transactional
    public OrderView cancel(String username, String orderNo) {
        Long userId = userId(username);
        OrderRow order = orderMapper.findOwnedForUpdate(orderNo, userId);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!List.of("PENDING_PAYMENT", "PAID").contains(order.orderStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前订单状态不能取消");
        }
        for (var item : orderMapper.findItems(order.id())) {
            catalogMapper.restoreStock(item.skuId(), item.quantity());
        }
        transition(order, "CANCELLED");
        return detail(username, orderNo);
    }

    @Transactional
    public OrderView adminTransition(String orderNo, String requestedStatus) {
        OrderRow order = orderMapper.findForUpdate(orderNo);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        String expected = ADMIN_TRANSITIONS.get(order.orderStatus());
        String normalized = requestedStatus == null ? "" : requestedStatus.trim().toUpperCase();
        if (!normalized.equals(expected)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "订单只能从 " + order.orderStatus() + " 流转到 " + (expected == null ? "终态" : expected));
        }
        transition(order, normalized);
        OrderRow updated = orderMapper.findForUpdate(orderNo);
        return view(updated);
    }

    private void transition(OrderRow order, String newStatus) {
        if (orderMapper.transitionStatus(order.id(), order.orderStatus(), newStatus) == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "订单状态已变化，请刷新后重试");
        }
    }

    private OrderView view(OrderRow order) {
        List<OrderItemView> items = orderMapper.findItems(order.id()).stream()
                .map(item -> new OrderItemView(item.id(), item.productId(), item.skuId(), item.productName(),
                        item.skuName(), item.unitPrice(), item.quantity(), item.subtotal()))
                .toList();
        return new OrderView(order.orderNo(), order.shopId(), order.shopName(), order.totalAmount(),
                order.deliveryFee(), order.payableAmount(), order.orderStatus(), order.receiverName(),
                order.receiverPhone(), order.receiverAddress(), order.remark(), order.createdAt(),
                order.updatedAt(), items);
    }

    private Long userId(String username) {
        var user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return user.getId();
    }

    private String newOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "WM" + time + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String fullAddress(Address address) {
        return address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress();
    }

    private String normalizeRemark(String remark) {
        return remark == null || remark.isBlank() ? null : remark.trim();
    }
}
