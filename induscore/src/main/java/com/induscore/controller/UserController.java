package com.induscore.controller;

import com.induscore.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户与角色相关接口（当前为 Mock 数据占位）。
 */
@RestController
@RequestMapping("/v1")
public class UserController {

    /**
     * 获取用户列表（分页/筛选参数暂不落库）。
     */
    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "all") String department,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "active") String status
    ) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("name", "李明");
        user.put("email", "liming@induscore.com");
        user.put("phone", "138****8888");
        user.put("department", "生产部");
        user.put("departmentId", 1);
        user.put("roles", List.of("admin", "production"));
        user.put("status", "active");
        user.put("online", true);
        user.put("lastLogin", "2026-02-03 14:30");
        user.put("lastIp", "192.168.1.100");
        user.put("createdAt", "2025-12-01");
        user.put("loginCount", 285);
        user.put("avatar", "https://cdn.induscore.com/avatars/user_1.png");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("online", 12);
        statistics.put("total", 45);

        Map<String, Object> data = new HashMap<>();
        data.put("statistics", statistics);
        data.put("users", List.of(user));
        data.put("total", 45);

        return ApiResponse.success(data);
    }

    /**
     * 获取角色列表。
     */
    @GetMapping("/users/roles")
    public ApiResponse<List<Map<String, Object>>> getRoles() {
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "超级管理员");
        role.put("description", "拥有系统所有权限");
        role.put("userCount", 2);
        role.put("permissions", List.of("user_manage", "role_manage", "system_config"));

        return ApiResponse.success(List.of(role));
    }
}
