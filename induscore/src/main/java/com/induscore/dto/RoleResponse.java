package com.induscore.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应 DTO。
 */
public record RoleResponse(
        Long id,
        String name,
        String code,
        String description,
        Integer userCount,  // 拥有该角色的用户数量
        List<PermissionResponse> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
