package com.waimai.platform.controller;

import com.waimai.platform.dto.CartDtos.AddItemRequest;
import com.waimai.platform.dto.CartDtos.CartView;
import com.waimai.platform.dto.CartDtos.UpdateQuantityRequest;
import com.waimai.platform.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartView get(Authentication authentication, @RequestParam Long shopId) {
        return cartService.get(authentication.getName(), shopId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartView add(Authentication authentication, @Valid @RequestBody AddItemRequest request) {
        return cartService.add(authentication.getName(), request);
    }

    @PutMapping("/items/{itemId}")
    public CartView update(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        return cartService.update(authentication.getName(), itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long itemId) {
        cartService.delete(authentication.getName(), itemId);
    }
}
