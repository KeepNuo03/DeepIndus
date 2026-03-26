package com.induscore.dto;

import java.util.List;

/**
 * 分配角色请求 DTO。
 */
public record AssignRolesRequest(
        List<Long> roleIds
) {}
