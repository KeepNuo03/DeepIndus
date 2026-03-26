package com.induscore.dto;

import java.time.LocalDateTime;

/**
 * 权限响应 DTO。
 */
public record PermissionResponse(
        Long id,
        String code,
        String name,
        String module,
        String description,
        LocalDateTime createdAt
) {}
