package com.induscore.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 模型部署请求 DTO。
 */
public record DeployModelRequest(
        @NotNull(message = "版本ID不能为空")
        Long versionId,

        String deployedBy
) {}
