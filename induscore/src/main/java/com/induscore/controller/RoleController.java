package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.PermissionResponse;
import com.induscore.dto.RoleRequest;
import com.induscore.dto.RoleResponse;
import com.induscore.service.RoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理接口控制器。
 */
@RestController
@RequestMapping("/v1")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 获取角色列表
     */
    @GetMapping("/roles")
    public ApiResponse<Map<String, Object>> getRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Map<String, Object> data = roleService.getRoles(page, pageSize);
        return ApiResponse.success(data);
    }

    /**
     * 获取所有角色（用于下拉选择）
     */
    @GetMapping("/roles/all")
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ApiResponse.success(roles);
    }

    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/roles/{id}")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    /**
     * 创建角色
     */
    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("创建角色请求: name={}, code={}", request.name(), request.code());
        RoleResponse role = roleService.createRole(request);
        return ApiResponse.success(role);
    }

    /**
     * 更新角色
     */
    @PutMapping("/roles/{id}")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request
    ) {
        log.info("更新角色请求: id={}", id);
        RoleResponse role = roleService.updateRole(id, request);
        return ApiResponse.success(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        log.info("删除角色请求: id={}", id);
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }

    /**
     * 分配权限
     */
    @PostMapping("/roles/{id}/permissions")
    public ApiResponse<RoleResponse> assignPermissions(
            @PathVariable Long id,
            @RequestBody List<Long> permissionIds
    ) {
        log.info("分配权限: roleId={}, permissions={}", id, permissionIds);
        RoleResponse role = roleService.assignPermissions(id, permissionIds);
        return ApiResponse.success(role);
    }

    /**
     * 获取所有权限
     */
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        return ApiResponse.success(roleService.getAllPermissions());
    }

    /**
     * 按模块获取权限
     */
    @GetMapping("/permissions/by-module")
    public ApiResponse<Map<String, List<PermissionResponse>>> getPermissionsByModule() {
        return ApiResponse.success(roleService.getPermissionsByModule());
    }
}
