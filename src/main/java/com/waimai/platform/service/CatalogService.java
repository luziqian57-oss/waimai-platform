package com.waimai.platform.service;

import com.waimai.platform.dto.CatalogResponse.CategoryView;
import com.waimai.platform.dto.CatalogResponse.ProductView;
import com.waimai.platform.dto.CatalogResponse.ShopMenu;
import com.waimai.platform.dto.CatalogResponse.ShopSummary;
import com.waimai.platform.dto.CatalogResponse.SkuView;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.CatalogMapper;
import com.waimai.platform.model.CatalogRows.CategoryRow;
import com.waimai.platform.model.CatalogRows.ProductRow;
import com.waimai.platform.model.CatalogRows.SkuRow;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final CatalogMapper catalogMapper;

    public CatalogService(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public List<ShopSummary> shops() {
        return catalogMapper.findActiveShops().stream()
                .map(shop -> new ShopSummary(
                        shop.id(), shop.shopName(), shop.logoUrl(), shop.contactPhone(), shop.address()))
                .toList();
    }

    public ShopMenu menu(Long shopId) {
        var shop = catalogMapper.findActiveShop(shopId);
        if (shop == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "店铺不存在或未营业");
        }

        Map<Long, List<SkuRow>> skusByProduct = catalogMapper.findActiveSkus(shopId).stream()
                .collect(Collectors.groupingBy(SkuRow::productId));
        Map<Long, List<ProductRow>> productsByCategory = catalogMapper.findActiveProducts(shopId).stream()
                .collect(Collectors.groupingBy(ProductRow::categoryId));

        List<CategoryView> categories = catalogMapper.findActiveCategories(shopId).stream()
                .map(category -> categoryView(category, productsByCategory, skusByProduct))
                .toList();
        return new ShopMenu(
                new ShopSummary(shop.id(), shop.shopName(), shop.logoUrl(), shop.contactPhone(), shop.address()),
                categories
        );
    }

    private CategoryView categoryView(
            CategoryRow category,
            Map<Long, List<ProductRow>> productsByCategory,
            Map<Long, List<SkuRow>> skusByProduct) {
        List<ProductView> products = productsByCategory.getOrDefault(category.id(), List.of()).stream()
                .map(product -> new ProductView(
                        product.id(), product.productName(), product.description(), product.imageUrl(),
                        skusByProduct.getOrDefault(product.id(), List.of()).stream()
                                .map(sku -> new SkuView(sku.id(), sku.skuName(), sku.price(), sku.stock()))
                                .toList()
                ))
                .toList();
        return new CategoryView(category.id(), category.categoryName(), category.sortNo(), products);
    }
}
