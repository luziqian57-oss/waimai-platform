package com.waimai.platform.mapper;

import com.waimai.platform.model.CommerceRows.CartItemRow;
import com.waimai.platform.model.CommerceRows.CartRow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("""
            SELECT c.id, c.user_id, c.shop_id, s.shop_name
            FROM wm_cart c JOIN wm_shop s ON s.id = c.shop_id
            WHERE c.user_id = #{userId} AND c.shop_id = #{shopId}
            """)
    CartRow findCart(Long userId, Long shopId);

    @Insert("INSERT IGNORE INTO wm_cart (user_id, shop_id) VALUES (#{userId}, #{shopId})")
    int createCart(Long userId, Long shopId);

    @Select("""
            SELECT ci.id AS item_id, ci.cart_id, ci.product_id, ci.sku_id, p.product_name, s.sku_name,
                   s.price AS unit_price, ci.quantity, s.stock, s.status AS sku_status,
                   p.status AS product_status
            FROM wm_cart_item ci
            JOIN wm_product p ON p.id = ci.product_id
            JOIN wm_product_sku s ON s.id = ci.sku_id
            WHERE ci.cart_id = #{cartId}
            ORDER BY ci.id
            """)
    List<CartItemRow> findItems(Long cartId);

    @Select("""
            SELECT ci.id AS item_id, ci.cart_id, ci.product_id, ci.sku_id, p.product_name, s.sku_name,
                   s.price AS unit_price, ci.quantity, s.stock, s.status AS sku_status,
                   p.status AS product_status
            FROM wm_cart_item ci
            JOIN wm_cart c ON c.id = ci.cart_id
            JOIN wm_product p ON p.id = ci.product_id
            JOIN wm_product_sku s ON s.id = ci.sku_id
            WHERE ci.id = #{itemId} AND c.user_id = #{userId}
            """)
    CartItemRow findOwnedItem(Long itemId, Long userId);

    @Insert("""
            INSERT INTO wm_cart_item (cart_id, product_id, sku_id, quantity)
            VALUES (#{cartId}, #{productId}, #{skuId}, #{quantity})
            ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)
            """)
    int upsertItem(Long cartId, Long productId, Long skuId, Integer quantity);

    @Update("UPDATE wm_cart_item SET quantity = #{quantity} WHERE id = #{itemId}")
    int updateQuantity(Long itemId, Integer quantity);

    @Delete("DELETE ci FROM wm_cart_item ci JOIN wm_cart c ON c.id = ci.cart_id WHERE ci.id = #{itemId} AND c.user_id = #{userId}")
    int deleteOwnedItem(Long itemId, Long userId);

    @Delete("DELETE FROM wm_cart_item WHERE cart_id = #{cartId}")
    int clearItems(Long cartId);
}
