package com.waimai.platform.controller;

import com.waimai.platform.dto.CatalogResponse.ShopMenu;
import com.waimai.platform.dto.CatalogResponse.ShopSummary;
import com.waimai.platform.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ShopSummary> shops() {
        return catalogService.shops();
    }

    @GetMapping("/{shopId}/menu")
    public ShopMenu menu(@PathVariable Long shopId) {
        return catalogService.menu(shopId);
    }
}
