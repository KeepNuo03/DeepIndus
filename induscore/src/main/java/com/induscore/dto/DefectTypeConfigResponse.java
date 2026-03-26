package com.induscore.dto;

/**
 * 缺陷类型配置响应 DTO。
 */
public record DefectTypeConfigResponse(
        Long id,
        String name,
        Double threshold,
        Boolean enabled
) {}
