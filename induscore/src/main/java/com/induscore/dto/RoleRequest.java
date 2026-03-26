package com.induscore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色创建/更新请求 DTO。
 */
public record RoleRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 64, message = "角色名称长度不能超过64")
        String name,

        @NotBlank(message = "角色编码不能为空")
        @Size(max = 64, message = "角色编码长度不能超过64")
        String code,

        @Size(max = 255, message = "描述长度不能超过255")
        String description,

        List<Long> permissionIds  // 权限ID列表
) {}
