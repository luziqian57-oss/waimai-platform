package com.waimai.platform.mapper;

import com.waimai.platform.model.CatalogRows.CategoryRow;
import com.waimai.platform.model.CatalogRows.ProductRow;
import com.waimai.platform.model.CatalogRows.ShopRow;
import com.waimai.platform.model.CatalogRows.SkuDetailRow;
import com.waimai.platform.model.CatalogRows.SkuRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CatalogMapper {

    @Select("""
            SELECT id, shop_name, logo_url, contact_phone, address
            FROM wm_shop
            WHERE status = 1
            ORDER BY id
            """)
    List<ShopRow> findActiveShops();

    @Select("""
            SELECT id, shop_name, logo_url, contact_phone, address
            FROM wm_shop
            WHERE id = #{shopId} AND status = 1
            """)
    ShopRow findActiveShop(Long shopId);

    @Select("""
            SELECT id, shop_id, category_name, sort_no
            FROM wm_category
            WHERE shop_id = #{shopId} AND status = 1
            ORDER BY sort_no, id
            """)
    List<CategoryRow> findActiveCategories(Long shopId);

    @Select("""
            SELECT id, shop_id, category_id, product_name, description, image_url
            FROM wm_product
            WHERE shop_id = #{shopId} AND status = 1
            ORDER BY category_id, id
            """)
    List<ProductRow> findActiveProducts(Long shopId);

    @Select("""
            SELECT s.id, s.product_id, s.sku_name, s.price, s.stock
            FROM wm_product_sku s
            JOIN wm_product p ON p.id = s.product_id
            WHERE p.shop_id = #{shopId} AND p.status = 1 AND s.status = 1
            ORDER BY s.product_id, s.id
            """)
    List<SkuRow> findActiveSkus(Long shopId);

    @Select("""
            SELECT s.id AS sku_id, p.id AS product_id, p.shop_id, p.product_name, s.sku_name,
                   s.price, s.stock, s.status AS sku_status, p.status AS product_status,
                   sh.status AS shop_status
            FROM wm_product_sku s
            JOIN wm_product p ON p.id = s.product_id
            JOIN wm_shop sh ON sh.id = p.shop_id
            WHERE s.id = #{skuId}
            """)
    SkuDetailRow findSkuDetail(Long skuId);

    @Update("""
            UPDATE wm_product_sku
            SET stock = stock - #{quantity}
            WHERE id = #{skuId} AND status = 1 AND stock >= #{quantity}
            """)
    int decreaseStock(Long skuId, Integer quantity);

    @Update("UPDATE wm_product_sku SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int restoreStock(Long skuId, Integer quantity);

    @Select("SELECT COUNT(*) FROM wm_shop WHERE id = #{id}")
    int shopExists(Long id);

    @Select("SELECT COUNT(*) FROM wm_category WHERE id = #{categoryId} AND shop_id = #{shopId}")
    int categoryBelongsToShop(Long categoryId, Long shopId);

    @Select("SELECT COUNT(*) FROM wm_product WHERE id = #{id}")
    int productExists(Long id);

    @Select("SELECT COUNT(*) FROM wm_product_sku WHERE id = #{id}")
    int skuExists(Long id);

    @Insert("""
            INSERT INTO wm_shop (shop_name, logo_url, contact_phone, address, status)
            VALUES (#{shopName}, #{logoUrl}, #{contactPhone}, #{address}, #{status})
            """)
    int insertShop(String shopName, String logoUrl, String contactPhone, String address, Integer status);

    @Update("""
            UPDATE wm_shop SET shop_name=#{shopName}, logo_url=#{logoUrl}, contact_phone=#{contactPhone},
                address=#{address}, status=#{status} WHERE id=#{id}
            """)
    int updateShop(Long id, String shopName, String logoUrl, String contactPhone, String address, Integer status);

    @Update("UPDATE wm_shop SET status = 0 WHERE id = #{id}")
    int disableShop(Long id);

    @Insert("""
            INSERT INTO wm_category (shop_id, category_name, sort_no, status)
            VALUES (#{shopId}, #{categoryName}, #{sortNo}, #{status})
            """)
    int insertCategory(Long shopId, String categoryName, Integer sortNo, Integer status);

    @Update("""
            UPDATE wm_category SET shop_id=#{shopId}, category_name=#{categoryName}, sort_no=#{sortNo},
                status=#{status} WHERE id=#{id}
            """)
    int updateCategory(Long id, Long shopId, String categoryName, Integer sortNo, Integer status);

    @Update("UPDATE wm_category SET status = 0 WHERE id = #{id}")
    int disableCategory(Long id);

    @Insert("""
            INSERT INTO wm_product (shop_id, category_id, product_name, description, image_url, status)
            VALUES (#{shopId}, #{categoryId}, #{productName}, #{description}, #{imageUrl}, #{status})
            """)
    int insertProduct(Long shopId, Long categoryId, String productName, String description, String imageUrl, Integer status);

    @Update("""
            UPDATE wm_product SET shop_id=#{shopId}, category_id=#{categoryId}, product_name=#{productName},
                description=#{description}, image_url=#{imageUrl}, status=#{status} WHERE id=#{id}
            """)
    int updateProduct(Long id, Long shopId, Long categoryId, String productName, String description,
                      String imageUrl, Integer status);

    @Update("UPDATE wm_product SET status = 0 WHERE id = #{id}")
    int disableProduct(Long id);

    @Insert("""
            INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
            VALUES (#{productId}, #{skuName}, #{price}, #{stock}, #{status})
            """)
    int insertSku(Long productId, String skuName, java.math.BigDecimal price, Integer stock, Integer status);

    @Update("""
            UPDATE wm_product_sku SET product_id=#{productId}, sku_name=#{skuName}, price=#{price},
                stock=#{stock}, status=#{status} WHERE id=#{id}
            """)
    int updateSku(Long id, Long productId, String skuName, java.math.BigDecimal price, Integer stock, Integer status);

    @Update("UPDATE wm_product_sku SET status = 0 WHERE id = #{id}")
    int disableSku(Long id);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();
}
