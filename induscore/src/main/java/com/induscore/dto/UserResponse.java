package com.induscore.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应 DTO。
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String name,
        String phone,
        String employeeNo,
        String position,
        String avatar,
        String status,
        Byte onlineStatus,
        Boolean online,
        String lastLoginAt,
        String lastLoginIp,
        Integer loginCount,
        Long departmentId,
        String departmentName,
        List<RoleSimpleResponse> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
