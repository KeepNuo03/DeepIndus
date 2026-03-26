package com.induscore.dto;

/**
 * 角色简略信息 DTO（用于用户响应中嵌套）。
 */
public record RoleSimpleResponse(
        Long id,
        String name,
        String code
) {}
