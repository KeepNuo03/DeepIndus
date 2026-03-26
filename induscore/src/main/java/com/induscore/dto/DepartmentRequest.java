package com.induscore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 部门创建/更新请求 DTO。
 */
public record DepartmentRequest(
        @NotBlank(message = "部门名称不能为空")
        @Size(max = 128, message = "部门名称长度不能超过128")
        String name,

        @NotBlank(message = "部门编码不能为空")
        @Size(max = 64, message = "部门编码长度不能超过64")
        String code,

        @Size(max = 255, message = "描述长度不能超过255")
        String description,

        Long parentId,  // 上级部门ID，可选

        Long managerId, // 部门负责人ID，可选

        Integer sortOrder,  // 排序号，可选

        String status  // active/inactive，可选，默认active
) {}
