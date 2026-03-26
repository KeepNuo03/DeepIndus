package com.induscore.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 模型响应 DTO。
 */
public record ModelResponse(
        Long id,
        String name,
        String code,
        String description,
        String architecture,
        String dataset,
        String taskType,
        String status,
        Boolean isMain,
        String currentVersion,
        Long currentVersionId,
        String modelFilePath,
        String configFilePath,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ModelVersionResponse> versions,
        ModelVersionResponse currentVersionDetail
) {}
