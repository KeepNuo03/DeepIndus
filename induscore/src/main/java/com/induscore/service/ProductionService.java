package com.induscore.service;

import com.induscore.model.Product;
import java.util.List;
import java.util.Map;

/**
 * 生产线业务接口。
 */
public interface ProductionService {
    /**
     * 获取生产线列表与统计。
     */
    Map<String, Object> getProductionLines();

    /**
     * 获取生产线可生产的产品列表。
     */
    List<Product> getProductsByProductionLine(Long productionLineId);

    /**
     * 为生产线添加可生产的产品。
     */
    void addProductToProductionLine(Long productionLineId, Long productId);

    /**
     * 从生产线移除可生产的产品。
     */
    void removeProductFromProductionLine(Long productionLineId, Long productId);
}
