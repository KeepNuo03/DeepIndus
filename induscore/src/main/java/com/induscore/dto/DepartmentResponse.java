package com.induscore.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门响应 DTO。
 */
public record DepartmentResponse(
        Long id,
        String name,
        String code,
        String description,
        Long parentId,
        String parentName,
        Long managerId,
        String managerName,
        Integer sortOrder,
        String status,
        Integer userCount,      // 部门人数
        Integer childrenCount,  // 子部门数量
        List<DepartmentResponse> children,  // 子部门列表
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
