package com.waimai.platform.service;

import com.waimai.platform.dto.CartDtos.AddItemRequest;
import com.waimai.platform.dto.CartDtos.CartItem;
import com.waimai.platform.dto.CartDtos.CartView;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.CartMapper;
import com.waimai.platform.mapper.CatalogMapper;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.model.CommerceRows.CartItemRow;
import com.waimai.platform.model.CommerceRows.CartRow;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartMapper cartMapper;
    private final CatalogMapper catalogMapper;
    private final UserMapper userMapper;

    public CartService(CartMapper cartMapper, CatalogMapper catalogMapper, UserMapper userMapper) {
        this.cartMapper = cartMapper;
        this.catalogMapper = catalogMapper;
        this.userMapper = userMapper;
    }

    public CartView get(String username, Long shopId) {
        Long userId = userId(username);
        CartRow cart = cartMapper.findCart(userId, shopId);
        if (cart == null) {
            var shop = catalogMapper.findActiveShop(shopId);
            if (shop == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "店铺不存在或未营业");
            }
            return new CartView(null, shopId, shop.shopName(), List.of(), BigDecimal.ZERO.setScale(2));
        }
        return view(cart, cartMapper.findItems(cart.id()));
    }

    @Transactional
    public CartView add(String username, AddItemRequest request) {
        Long userId = userId(username);
        var sku = catalogMapper.findSkuDetail(request.skuId());
        if (sku == null || sku.shopId() == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品规格不存在");
        }
        if (!sku.shopId().equals(request.shopId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "商品不属于当前店铺");
        }
        if (sku.shopStatus() != 1 || sku.productStatus() != 1 || sku.skuStatus() != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "商品已下架");
        }

        CartRow cart = cartMapper.findCart(userId, request.shopId());
        if (cart == null) {
            cartMapper.createCart(userId, request.shopId());
            cart = cartMapper.findCart(userId, request.shopId());
        }
        int existingQuantity = cartMapper.findItems(cart.id()).stream()
                .filter(item -> item.skuId().equals(request.skuId()))
                .mapToInt(CartItemRow::quantity)
                .findFirst().orElse(0);
        int newQuantity = existingQuantity + request.quantity();
        if (newQuantity > 99) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "单个规格最多购买99件");
        }
        requireStock(newQuantity, sku.stock());
        cartMapper.upsertItem(cart.id(), sku.productId(), sku.skuId(), newQuantity);
        return view(cart, cartMapper.findItems(cart.id()));
    }

    @Transactional
    public CartView update(String username, Long itemId, Integer quantity) {
        Long userId = userId(username);
        CartItemRow item = requireOwnedItem(itemId, userId);
        if (item.productStatus() != 1 || item.skuStatus() != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "商品已下架");
        }
        requireStock(quantity, item.stock());
        cartMapper.updateQuantity(itemId, quantity);
        CartRow cart = cartMapper.findCart(userId, shopIdForSku(item.skuId()));
        return view(cart, cartMapper.findItems(cart.id()));
    }

    public void delete(String username, Long itemId) {
        if (cartMapper.deleteOwnedItem(itemId, userId(username)) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "购物车商品不存在");
        }
    }

    private CartItemRow requireOwnedItem(Long itemId, Long userId) {
        CartItemRow item = cartMapper.findOwnedItem(itemId, userId);
        if (item == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "购物车商品不存在");
        }
        return item;
    }

    private Long shopIdForSku(Long skuId) {
        var sku = catalogMapper.findSkuDetail(skuId);
        if (sku == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品规格不存在");
        }
        return sku.shopId();
    }

    private void requireStock(int quantity, int stock) {
        if (quantity > stock) {
            throw new BusinessException(HttpStatus.CONFLICT, "商品库存不足");
        }
    }

    private CartView view(CartRow cart, List<CartItemRow> rows) {
        List<CartItem> items = rows.stream().map(row -> {
            BigDecimal subtotal = row.unitPrice().multiply(BigDecimal.valueOf(row.quantity()));
            return new CartItem(row.itemId(), row.productId(), row.skuId(), row.productName(), row.skuName(),
                    row.unitPrice(), row.quantity(), subtotal, row.stock());
        }).toList();
        BigDecimal total = items.stream().map(CartItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(cart.id(), cart.shopId(), cart.shopName(), items, total.setScale(2));
    }

    private Long userId(String username) {
        var user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return user.getId();
    }
}
