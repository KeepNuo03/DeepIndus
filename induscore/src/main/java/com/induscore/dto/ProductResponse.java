package com.induscore.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品响应 DTO。
 */
public record ProductResponse(
        Long id,
        String name,
        String model,
        String category,
        String dimensions,
        String material,
        Double weight,
        String surfaceTreatment,
        String standard,
        Double threshold,
        Double targetYield,
        String status,
        String imageUrl,
        String description,
        List<DefectTypeConfigResponse> defectTypes,
        List<ProductionLineRelationResponse> productionLines,
        // 统计字段（可由业务层计算后填充）
        Integer todayOutput,
        Double yieldRate,
        Integer batchCount,
        Long totalOutput,
        Double avgYieldRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
