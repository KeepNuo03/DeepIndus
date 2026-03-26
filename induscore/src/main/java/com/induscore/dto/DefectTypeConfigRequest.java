package com.induscore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 缺陷类型配置请求 DTO。
 */
public record DefectTypeConfigRequest(
        Long id,
        @NotBlank(message = "缺陷类型名称不能为空") String name,
        Double threshold,
        @NotNull(message = "启用状态不能为空") Boolean enabled
) {}
