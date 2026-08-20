package com.waimai.platform.controller;

import com.waimai.platform.dto.OrderDtos.OrderView;
import com.waimai.platform.dto.OrderDtos.UpdateStatusRequest;
import com.waimai.platform.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/{orderNo}/status")
    public OrderView updateStatus(
            @PathVariable String orderNo, @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.adminTransition(orderNo, request.status());
    }
}
