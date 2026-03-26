package com.induscore.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 模型版本响应 DTO。
 */
public record ModelVersionResponse(
        Long id,
        Long modelId,
        String version,
        String description,
        String status,
        Boolean isCurrent,

        // 性能指标
        Double accuracy,
        Double recall,
        Double f1Score,
        Double precision,
        Double map,
        Double map50_95,
        Double inferenceSpeed,
        Double modelSizeMb,

        // 训练信息
        Integer trainingDataSize,
        LocalDate trainedAt,
        String trainer,
        Integer trainingEpochs,
        Integer batchSize,
        String learningRate,

        // 文件信息
        String modelFile,
        String configFile,
        String labelFile,

        // 部署信息
        LocalDateTime deployedAt,
        String deployedBy,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
