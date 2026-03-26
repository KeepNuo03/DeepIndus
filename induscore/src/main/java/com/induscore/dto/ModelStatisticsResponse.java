package com.induscore.dto;

/**
 * AI 模型统计响应 DTO。
 */
public record ModelStatisticsResponse(
        Integer totalModels,
        Integer deployedModels,
        Integer activeModels,
        Integer draftModels,
        Double avgAccuracy,
        Integer totalVersions,
        Integer abTests
) {}
