package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.PermissionResponse;
import com.induscore.dto.RoleRequest;
import com.induscore.dto.RoleResponse;
import com.induscore.model.Permission;
import com.induscore.model.Role;
import com.induscore.repository.PermissionRepository;
import com.induscore.repository.RoleRepository;
import com.induscore.repository.UserRepository;
import com.induscore.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理业务实现。
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new ApiException(400, "角色编码已存在: " + request.code());
        }

        Role role = new Role();
        role.setName(request.name());
        role.setCode(request.code());
        role.setDescription(request.description());

        // 分配权限
        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (Long permId : request.permissionIds()) {
                Permission perm = permissionRepository.findById(permId)
                        .orElseThrow(() -> new ApiException(404, "权限不存在: " + permId));
                permissions.add(perm);
            }
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
        return mapToResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "角色不存在: " + id));

        if (request.code() != null && !request.code().equals(role.getCode())) {
            if (roleRepository.existsByCodeAndIdNot(request.code(), id)) {
                throw new ApiException(400, "角色编码已存在: " + request.code());
            }
            role.setCode(request.code());
        }

        if (request.name() != null) role.setName(request.name());
        if (request.description() != null) role.setDescription(request.description());

        // 更新权限
        if (request.permissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (Long permId : request.permissionIds()) {
                Permission perm = permissionRepository.findById(permId)
                        .orElseThrow(() -> new ApiException(404, "权限不存在: " + permId));
                permissions.add(perm);
            }
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
        return mapToResponse(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "角色不存在: " + id));

        // 检查是否有用户使用该角色
        long userCount = userRepository.countByRoleId(id);
        if (userCount > 0) {
            throw new ApiException(400, "该角色下有 " + userCount + " 个用户，无法删除");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "角色不存在: " + id));
        role.getPermissions().size(); // 强制初始化
        return mapToResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRoles(int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Role> result = roleRepository.findAll(pageable);

        List<RoleResponse> list = result.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("roles", list);
        data.put("total", result.getTotalElements());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    @Override
    @Transactional
    public RoleResponse assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(404, "角色不存在: " + roleId));

        Set<Permission> permissions = new HashSet<>();
        if (permissionIds != null) {
            for (Long permId : permissionIds) {
                Permission perm = permissionRepository.findById(permId)
                        .orElseThrow(() -> new ApiException(404, "权限不存在: " + permId));
                permissions.add(perm);
            }
        }

        role.setPermissions(permissions);
        roleRepository.save(role);
        return mapToResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsByModule() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.groupingBy(PermissionResponse::module));
    }

    private RoleResponse mapToResponse(Role role) {
        int userCount = (int) userRepository.countByRoleId(role.getId());

        List<PermissionResponse> permissions = role.getPermissions().stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.toList());

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                userCount,
                permissions,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private PermissionResponse mapPermissionToResponse(Permission perm) {
        return new PermissionResponse(
                perm.getId(),
                perm.getCode(),
                perm.getName(),
                perm.getModule(),
                perm.getDescription(),
                perm.getCreatedAt()
        );
    }
}
