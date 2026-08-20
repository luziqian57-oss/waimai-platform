package com.waimai.platform.mapper;

import com.waimai.platform.model.CommerceRows.OrderItemRow;
import com.waimai.platform.model.CommerceRows.OrderRow;
import com.waimai.platform.model.OrderEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("""
            INSERT INTO wm_order
            (order_no, user_id, shop_id, address_id, total_amount, delivery_fee, payable_amount,
             order_status, receiver_name, receiver_phone, receiver_address, remark)
            VALUES (#{orderNo}, #{userId}, #{shopId}, #{addressId}, #{totalAmount}, #{deliveryFee},
                    #{payableAmount}, #{orderStatus}, #{receiverName}, #{receiverPhone},
                    #{receiverAddress}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(OrderEntity order);

    @Insert("""
            INSERT INTO wm_order_item
            (order_id, product_id, sku_id, product_name, sku_name, unit_price, quantity, subtotal)
            VALUES (#{orderId}, #{productId}, #{skuId}, #{productName}, #{skuName},
                    #{unitPrice}, #{quantity}, #{subtotal})
            """)
    int insertItem(
            Long orderId, Long productId, Long skuId, String productName, String skuName,
            BigDecimal unitPrice, Integer quantity, BigDecimal subtotal);

    @Select("""
            SELECT o.id, o.order_no, o.user_id, o.shop_id, s.shop_name, o.address_id,
                   o.total_amount, o.delivery_fee, o.payable_amount, o.order_status,
                   o.receiver_name, o.receiver_phone, o.receiver_address, o.remark,
                   o.created_at, o.updated_at
            FROM wm_order o JOIN wm_shop s ON s.id = o.shop_id
            WHERE o.order_no = #{orderNo} AND o.user_id = #{userId}
            """)
    OrderRow findOwned(String orderNo, Long userId);

    @Select("""
            SELECT o.id, o.order_no, o.user_id, o.shop_id, s.shop_name, o.address_id,
                   o.total_amount, o.delivery_fee, o.payable_amount, o.order_status,
                   o.receiver_name, o.receiver_phone, o.receiver_address, o.remark,
                   o.created_at, o.updated_at
            FROM wm_order o JOIN wm_shop s ON s.id = o.shop_id
            WHERE o.order_no = #{orderNo} AND o.user_id = #{userId}
            FOR UPDATE
            """)
    OrderRow findOwnedForUpdate(String orderNo, Long userId);

    @Select("""
            SELECT o.id, o.order_no, o.user_id, o.shop_id, s.shop_name, o.address_id,
                   o.total_amount, o.delivery_fee, o.payable_amount, o.order_status,
                   o.receiver_name, o.receiver_phone, o.receiver_address, o.remark,
                   o.created_at, o.updated_at
            FROM wm_order o JOIN wm_shop s ON s.id = o.shop_id
            WHERE o.order_no = #{orderNo}
            FOR UPDATE
            """)
    OrderRow findForUpdate(String orderNo);

    @Select("""
            SELECT id, order_id, product_id, sku_id, product_name, sku_name,
                   unit_price, quantity, subtotal
            FROM wm_order_item WHERE order_id = #{orderId} ORDER BY id
            """)
    List<OrderItemRow> findItems(Long orderId);

    @Select("""
            SELECT o.id, o.order_no, o.user_id, o.shop_id, s.shop_name, o.address_id,
                   o.total_amount, o.delivery_fee, o.payable_amount, o.order_status,
                   o.receiver_name, o.receiver_phone, o.receiver_address, o.remark,
                   o.created_at, o.updated_at
            FROM wm_order o JOIN wm_shop s ON s.id = o.shop_id
            WHERE o.user_id = #{userId}
            ORDER BY o.id DESC LIMIT #{size} OFFSET #{offset}
            """)
    List<OrderRow> findPage(Long userId, Integer offset, Integer size);

    @Select("SELECT COUNT(*) FROM wm_order WHERE user_id = #{userId}")
    long countByUser(Long userId);

    @Update("""
            UPDATE wm_order SET order_status = #{newStatus}
            WHERE id = #{orderId} AND order_status = #{expectedStatus}
            """)
    int transitionStatus(Long orderId, String expectedStatus, String newStatus);
}
