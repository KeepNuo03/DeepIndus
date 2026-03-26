package com.induscore.service;

import com.induscore.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理业务接口。
 */
public interface RoleService {

    /**
     * 创建角色
     */
    RoleResponse createRole(RoleRequest request);

    /**
     * 更新角色
     */
    RoleResponse updateRole(Long id, RoleRequest request);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 获取角色详情
     */
    RoleResponse getRoleById(Long id);

    /**
     * 获取所有角色
     */
    List<RoleResponse> getAllRoles();

    /**
     * 获取角色列表（分页）
     */
    Map<String, Object> getRoles(int page, int pageSize);

    /**
     * 分配权限
     */
    RoleResponse assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取所有权限
     */
    List<PermissionResponse> getAllPermissions();

    /**
     * 根据模块获取权限
     */
    Map<String, List<PermissionResponse>> getPermissionsByModule();
}
