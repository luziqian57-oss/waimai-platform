package com.waimai.platform.controller;

import com.waimai.platform.dto.OrderDtos.CreateOrderRequest;
import com.waimai.platform.dto.OrderDtos.OrderView;
import com.waimai.platform.dto.PageResponse;
import com.waimai.platform.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView create(Authentication authentication, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(authentication.getName(), request);
    }

    @GetMapping
    public PageResponse<OrderView> list(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.list(authentication.getName(), page, size);
    }

    @GetMapping("/{orderNo}")
    public OrderView detail(Authentication authentication, @PathVariable String orderNo) {
        return orderService.detail(authentication.getName(), orderNo);
    }

    @PostMapping("/{orderNo}/pay")
    public OrderView pay(Authentication authentication, @PathVariable String orderNo) {
        return orderService.pay(authentication.getName(), orderNo);
    }

    @PostMapping("/{orderNo}/cancel")
    public OrderView cancel(Authentication authentication, @PathVariable String orderNo) {
        return orderService.cancel(authentication.getName(), orderNo);
    }
}
