package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.*;
import com.induscore.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品管理接口控制器。
 */
@RestController
@RequestMapping("/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取产品列表（支持分类与搜索）
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        ProductQueryRequest query = new ProductQueryRequest(category, search, status, page, pageSize);
        Map<String, Object> data = productService.getProducts(query);
        return ApiResponse.success(data);
    }

    /**
     * 获取所有活跃产品（用于下拉选择）
     */
    @GetMapping("/active")
    public ApiResponse<List<ProductResponse>> getActiveProducts() {
        List<ProductResponse> products = productService.getActiveProducts();
        return ApiResponse.success(products);
    }

    /**
     * 根据ID获取产品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ApiResponse.success(product);
    }

    /**
     * 创建产品
     */
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("创建产品请求: model={}", request.model());
        ProductResponse product = productService.createProduct(request);
        return ApiResponse.success(product);
    }

    /**
     * 更新产品
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        log.info("更新产品请求: id={}", id);
        ProductResponse product = productService.updateProduct(id, request);
        return ApiResponse.success(product);
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        log.info("删除产品请求: id={}", id);
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }

    /**
     * 更新产品状态
     */
    @PostMapping("/{id}/status")
    public ApiResponse<ProductResponse> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        ProductResponse product = productService.updateProductStatus(id, status);
        return ApiResponse.success(product);
    }

    /**
     * 检查产品型号是否可用
     */
    @GetMapping("/check-model")
    public ApiResponse<Map<String, Object>> checkModelAvailability(
            @RequestParam String model,
            @RequestParam(required = false) Long excludeId
    ) {
        boolean available = productService.isModelAvailable(model, excludeId);
        return ApiResponse.success(Map.of("available", available, "model", model));
    }

    /**
     * 为产品添加生产线关联
     */
    @PostMapping("/{productId}/lines/{lineId}")
    public ApiResponse<ProductResponse> addProductionLine(
            @PathVariable Long productId,
            @PathVariable Long lineId,
            @RequestParam(required = false, defaultValue = "0") Integer priority,
            @RequestParam(required = false, defaultValue = "false") Boolean isPrimary
    ) {
        log.info("为产品添加生产线: productId={}, lineId={}", productId, lineId);
        ProductResponse product = productService.addProductionLine(productId, lineId, priority, isPrimary);
        return ApiResponse.success(product);
    }

    /**
     * 移除产品的生产线关联
     */
    @DeleteMapping("/{productId}/lines/{lineId}")
    public ApiResponse<ProductResponse> removeProductionLine(
            @PathVariable Long productId,
            @PathVariable Long lineId
    ) {
        log.info("移除产品生产线关联: productId={}, lineId={}", productId, lineId);
        ProductResponse product = productService.removeProductionLine(productId, lineId);
        return ApiResponse.success(product);
    }

    /**
     * 设置主生产线
     */
    @PostMapping("/{productId}/lines/{lineId}/primary")
    public ApiResponse<ProductResponse> setPrimaryProductionLine(
            @PathVariable Long productId,
            @PathVariable Long lineId
    ) {
        log.info("设置主生产线: productId={}, lineId={}", productId, lineId);
        ProductResponse product = productService.setPrimaryProductionLine(productId, lineId);
        return ApiResponse.success(product);
    }

    /**
     * 获取指定生产线的所有产品
     */
    @GetMapping("/by-line/{lineId}")
    public ApiResponse<List<ProductResponse>> getProductsByProductionLine(@PathVariable Long lineId) {
        List<ProductResponse> products = productService.getProductsByProductionLine(lineId);
        return ApiResponse.success(products);
    }
}
