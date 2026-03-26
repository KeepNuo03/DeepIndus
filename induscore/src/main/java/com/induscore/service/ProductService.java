package com.induscore.service;

import com.induscore.dto.ProductQueryRequest;
import com.induscore.dto.ProductRequest;
import com.induscore.dto.ProductResponse;
import java.util.List;
import java.util.Map;

/**
 * 产品管理业务接口。
 */
public interface ProductService {

    /**
     * 创建产品
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * 更新产品
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * 删除产品
     */
    void deleteProduct(Long id);

    /**
     * 根据ID获取产品详情
     */
    ProductResponse getProductById(Long id);

    /**
     * 分页查询产品列表
     */
    Map<String, Object> getProducts(ProductQueryRequest query);

    /**
     * 获取所有活跃的产品（用于下拉选择）
     */
    List<ProductResponse> getActiveProducts();

    /**
     * 更新产品状态
     */
    ProductResponse updateProductStatus(Long id, String status);

    /**
     * 为产品添加生产线关联
     */
    ProductResponse addProductionLine(Long productId, Long lineId, Integer priority, Boolean isPrimary);

    /**
     * 移除产品的生产线关联
     */
    ProductResponse removeProductionLine(Long productId, Long lineId);

    /**
     * 设置主生产线
     */
    ProductResponse setPrimaryProductionLine(Long productId, Long lineId);

    /**
     * 获取生产线关联的所有产品
     */
    List<ProductResponse> getProductsByProductionLine(Long lineId);

    /**
     * 检查产品型号是否可用
     */
    boolean isModelAvailable(String model, Long excludeId);
}
