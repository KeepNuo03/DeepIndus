package com.induscore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * AI 模型版本创建/更新请求 DTO。
 */
public record ModelVersionRequest(
        @NotBlank(message = "版本号不能为空")
        @Size(max = 64, message = "版本号长度不能超过64")
        String version,

        @Size(max = 512, message = "描述长度不能超过512")
        String description,

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
        String trainedAt,
        String trainer,
        Integer trainingEpochs,
        Integer batchSize,
        String learningRate,

        // 文件信息
        String modelFile,
        String configFile,
        String labelFile
) {}
