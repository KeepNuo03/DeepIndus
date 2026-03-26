package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.model.Product;
import com.induscore.service.ProductionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生产线管理接口控制器。
 */
@RestController
@RequestMapping("/v1/production")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    /**
     * 获取生产线列表与统计。
     */
    @GetMapping("/lines")
    public ApiResponse<Map<String, Object>> getProductionLines() {
        return ApiResponse.success(productionService.getProductionLines());
    }

    /**
     * 获取生产线可生产的产品列表。
     */
    @GetMapping("/lines/{lineId}/products")
    public ApiResponse<List<Map<String, Object>>> getProductsByProductionLine(@PathVariable Long lineId) {
        List<Product> products = productionService.getProductsByProductionLine(lineId);

        List<Map<String, Object>> productList = products.stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "name", p.getName(),
                        "model", p.getModel(),
                        "category", p.getCategory(),
                        "status", p.getStatus()
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(productList);
    }

    /**
     * 为生产线添加可生产的产品。
     */
    @PostMapping("/lines/{lineId}/products/{productId}")
    public ApiResponse<Void> addProductToProductionLine(
            @PathVariable Long lineId,
            @PathVariable Long productId
    ) {
        productionService.addProductToProductionLine(lineId, productId);
        return ApiResponse.success(null);
    }

    /**
     * 从生产线移除可生产的产品。
     */
    @DeleteMapping("/lines/{lineId}/products/{productId}")
    public ApiResponse<Void> removeProductFromProductionLine(
            @PathVariable Long lineId,
            @PathVariable Long productId
    ) {
        productionService.removeProductFromProductionLine(lineId, productId);
        return ApiResponse.success(null);
    }
}
