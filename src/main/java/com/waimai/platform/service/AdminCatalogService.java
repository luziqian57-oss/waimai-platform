package com.waimai.platform.service;

import com.waimai.platform.dto.AdminCatalogDtos.AdminEntityResponse;
import com.waimai.platform.dto.AdminCatalogDtos.CategoryRequest;
import com.waimai.platform.dto.AdminCatalogDtos.ProductRequest;
import com.waimai.platform.dto.AdminCatalogDtos.ShopRequest;
import com.waimai.platform.dto.AdminCatalogDtos.SkuRequest;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.CatalogMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogService {

    private final CatalogMapper catalogMapper;

    public AdminCatalogService(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    @Transactional
    public AdminEntityResponse createShop(ShopRequest request) {
        catalogMapper.insertShop(request.shopName(), request.logoUrl(), request.contactPhone(), request.address(), status(request.active()));
        return response(catalogMapper.lastInsertId(), "SHOP", request.active());
    }

    public AdminEntityResponse updateShop(Long id, ShopRequest request) {
        require(catalogMapper.updateShop(id, request.shopName(), request.logoUrl(), request.contactPhone(),
                request.address(), status(request.active())), "店铺不存在");
        return response(id, "SHOP", request.active());
    }

    public void disableShop(Long id) { require(catalogMapper.disableShop(id), "店铺不存在"); }

    @Transactional
    public AdminEntityResponse createCategory(CategoryRequest request) {
        requireShop(request.shopId());
        catalogMapper.insertCategory(request.shopId(), request.categoryName(), request.sortNo(), status(request.active()));
        return response(catalogMapper.lastInsertId(), "CATEGORY", request.active());
    }

    public AdminEntityResponse updateCategory(Long id, CategoryRequest request) {
        requireShop(request.shopId());
        require(catalogMapper.updateCategory(id, request.shopId(), request.categoryName(), request.sortNo(),
                status(request.active())), "分类不存在");
        return response(id, "CATEGORY", request.active());
    }

    public void disableCategory(Long id) { require(catalogMapper.disableCategory(id), "分类不存在"); }

    @Transactional
    public AdminEntityResponse createProduct(ProductRequest request) {
        requireCategory(request.categoryId(), request.shopId());
        catalogMapper.insertProduct(request.shopId(), request.categoryId(), request.productName(),
                request.description(), request.imageUrl(), status(request.active()));
        return response(catalogMapper.lastInsertId(), "PRODUCT", request.active());
    }

    public AdminEntityResponse updateProduct(Long id, ProductRequest request) {
        requireCategory(request.categoryId(), request.shopId());
        require(catalogMapper.updateProduct(id, request.shopId(), request.categoryId(), request.productName(),
                request.description(), request.imageUrl(), status(request.active())), "商品不存在");
        return response(id, "PRODUCT", request.active());
    }

    public void disableProduct(Long id) { require(catalogMapper.disableProduct(id), "商品不存在"); }

    @Transactional
    public AdminEntityResponse createSku(SkuRequest request) {
        if (catalogMapper.productExists(request.productId()) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        catalogMapper.insertSku(request.productId(), request.skuName(), request.price(), request.stock(), status(request.active()));
        return response(catalogMapper.lastInsertId(), "SKU", request.active());
    }

    public AdminEntityResponse updateSku(Long id, SkuRequest request) {
        if (catalogMapper.productExists(request.productId()) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        require(catalogMapper.updateSku(id, request.productId(), request.skuName(), request.price(),
                request.stock(), status(request.active())), "SKU不存在");
        return response(id, "SKU", request.active());
    }

    public void disableSku(Long id) { require(catalogMapper.disableSku(id), "SKU不存在"); }

    private void requireShop(Long shopId) {
        if (catalogMapper.shopExists(shopId) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "店铺不存在");
        }
    }

    private void requireCategory(Long categoryId, Long shopId) {
        requireShop(shopId);
        if (catalogMapper.categoryBelongsToShop(categoryId, shopId) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不属于当前店铺");
        }
    }

    private int status(Boolean active) { return Boolean.FALSE.equals(active) ? 0 : 1; }

    private AdminEntityResponse response(Long id, String type, Boolean active) {
        return new AdminEntityResponse(id, type, !Boolean.FALSE.equals(active));
    }

    private void require(int affectedRows, String message) {
        if (affectedRows == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, message);
        }
    }
}
