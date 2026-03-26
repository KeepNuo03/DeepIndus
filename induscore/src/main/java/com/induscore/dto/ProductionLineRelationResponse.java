package com.induscore.dto;

/**
 * 产品与生产线关联响应 DTO。
 */
public record ProductionLineRelationResponse(
        Long id,
        Long productionLineId,
        String productionLineName,
        String productionLineStatus,
        String productionLineLocation,
        Integer priority,
        Boolean isPrimary
) {}
