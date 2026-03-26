package com.induscore.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 产品与生产线关联请求 DTO。
 */
public record ProductionLineRelationRequest(
        @NotNull(message = "生产线ID不能为空") Long productionLineId,
        Integer priority,
        Boolean isPrimary
) {}
