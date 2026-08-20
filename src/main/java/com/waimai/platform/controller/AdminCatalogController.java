package com.waimai.platform.controller;

import com.waimai.platform.dto.AdminCatalogDtos.AdminEntityResponse;
import com.waimai.platform.dto.AdminCatalogDtos.CategoryRequest;
import com.waimai.platform.dto.AdminCatalogDtos.ProductRequest;
import com.waimai.platform.dto.AdminCatalogDtos.ShopRequest;
import com.waimai.platform.dto.AdminCatalogDtos.SkuRequest;
import com.waimai.platform.service.AdminCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/catalog")
public class AdminCatalogController {

    private final AdminCatalogService service;

    public AdminCatalogController(AdminCatalogService service) { this.service = service; }

    @PostMapping("/shops") @ResponseStatus(HttpStatus.CREATED)
    public AdminEntityResponse createShop(@Valid @RequestBody ShopRequest request) { return service.createShop(request); }
    @PutMapping("/shops/{id}")
    public AdminEntityResponse updateShop(@PathVariable Long id, @Valid @RequestBody ShopRequest request) { return service.updateShop(id, request); }
    @DeleteMapping("/shops/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableShop(@PathVariable Long id) { service.disableShop(id); }

    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    public AdminEntityResponse createCategory(@Valid @RequestBody CategoryRequest request) { return service.createCategory(request); }
    @PutMapping("/categories/{id}")
    public AdminEntityResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return service.updateCategory(id, request); }
    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableCategory(@PathVariable Long id) { service.disableCategory(id); }

    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
    public AdminEntityResponse createProduct(@Valid @RequestBody ProductRequest request) { return service.createProduct(request); }
    @PutMapping("/products/{id}")
    public AdminEntityResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) { return service.updateProduct(id, request); }
    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableProduct(@PathVariable Long id) { service.disableProduct(id); }

    @PostMapping("/skus") @ResponseStatus(HttpStatus.CREATED)
    public AdminEntityResponse createSku(@Valid @RequestBody SkuRequest request) { return service.createSku(request); }
    @PutMapping("/skus/{id}")
    public AdminEntityResponse updateSku(@PathVariable Long id, @Valid @RequestBody SkuRequest request) { return service.updateSku(id, request); }
    @DeleteMapping("/skus/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableSku(@PathVariable Long id) { service.disableSku(id); }
}
