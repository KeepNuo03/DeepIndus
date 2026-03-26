package com.induscore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 模型创建/更新请求 DTO。
 */
public record ModelRequest(
        @NotBlank(message = "模型名称不能为空")
        @Size(max = 128, message = "模型名称长度不能超过128")
        String name,

        @NotBlank(message = "模型编码不能为空")
        @Size(max = 64, message = "模型编码长度不能超过64")
        String code,

        @Size(max = 512, message = "描述长度不能超过512")
        String description,

        @Size(max = 64, message = "架构类型长度不能超过64")
        String architecture,

        @Size(max = 128, message = "数据集名称长度不能超过128")
        String dataset,

        @Size(max = 64, message = "任务类型长度不能超过64")
        String taskType,

        String modelFilePath,
        String configFilePath
) {}
