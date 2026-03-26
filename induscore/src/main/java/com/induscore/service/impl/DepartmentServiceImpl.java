package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.DepartmentRequest;
import com.induscore.dto.DepartmentResponse;
import com.induscore.model.Department;
import com.induscore.model.User;
import com.induscore.repository.DepartmentRepository;
import com.induscore.repository.UserRepository;
import com.induscore.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理业务实现。
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new ApiException(400, "部门编码已存在: " + request.code());
        }

        Department dept = new Department();
        dept.setName(request.name());
        dept.setCode(request.code());
        dept.setDescription(request.description());
        dept.setParentId(request.parentId());
        dept.setManagerId(request.managerId());
        dept.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        dept.setStatus(request.status() != null ? request.status() : "active");

        departmentRepository.save(dept);
        return mapToResponse(dept);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "部门不存在: " + id));

        if (request.code() != null && !request.code().equals(dept.getCode())) {
            if (departmentRepository.existsByCodeAndIdNot(request.code(), id)) {
                throw new ApiException(400, "部门编码已存在: " + request.code());
            }
            dept.setCode(request.code());
        }

        if (request.name() != null) dept.setName(request.name());
        if (request.description() != null) dept.setDescription(request.description());
        if (request.parentId() != null) dept.setParentId(request.parentId());
        if (request.managerId() != null) dept.setManagerId(request.managerId());
        if (request.sortOrder() != null) dept.setSortOrder(request.sortOrder());
        if (request.status() != null) dept.setStatus(request.status());

        departmentRepository.save(dept);
        return mapToResponse(dept);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "部门不存在: " + id));

        // 检查是否有子部门
        long childrenCount = departmentRepository.countChildrenByParentId(id);
        if (childrenCount > 0) {
            throw new ApiException(400, "该部门下有 " + childrenCount + " 个子部门，无法删除");
        }

        // 检查是否有用户
        long userCount = departmentRepository.countUsersByDepartmentId(id);
        if (userCount > 0) {
            throw new ApiException(400, "该部门下有 " + userCount + " 个用户，无法删除");
        }

        departmentRepository.delete(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "部门不存在: " + id));
        dept.getChildren().size(); // 强制初始化
        return mapToResponse(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentTree() {
        List<Department> allDepts = departmentRepository.findByStatusOrderBySortOrderAsc("active");
        for (Department dept : allDepts) {
            dept.getChildren().size();
        }

        // 构建树形结构
        Map<Long, DepartmentResponse> responseMap = allDepts.stream()
                .collect(Collectors.toMap(Department::getId, this::mapToResponse));

        List<DepartmentResponse> tree = new ArrayList<>();
        for (Department dept : allDepts) {
            if (dept.getParentId() == null) {
                tree.add(buildTree(dept, responseMap, allDepts));
            }
        }
        return tree;
    }

    private DepartmentResponse buildTree(Department dept, Map<Long, DepartmentResponse> responseMap, List<Department> allDepts) {
        DepartmentResponse response = responseMap.get(dept.getId());

        List<DepartmentResponse> children = allDepts.stream()
                .filter(d -> dept.getId().equals(d.getParentId()))
                .map(d -> buildTree(d, responseMap, allDepts))
                .collect(Collectors.toList());

        // 重新构建带children的response
        return new DepartmentResponse(
                response.id(), response.name(), response.code(),
                response.description(), response.parentId(), response.parentName(),
                response.managerId(), response.managerName(),
                response.sortOrder(), response.status(),
                response.userCount(), children.size(), children,
                response.createdAt(), response.updatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findByStatusOrderBySortOrderAsc("active")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveDepartments() {
        return getAllDepartments();
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartmentStatus(Long id, String status) {
        if (!"active".equals(status) && !"inactive".equals(status)) {
            throw new ApiException(400, "无效的状态值: " + status);
        }
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "部门不存在: " + id));
        dept.setStatus(status);
        departmentRepository.save(dept);
        return mapToResponse(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentStatistics() {
        List<Department> depts = departmentRepository.findByStatusOrderBySortOrderAsc("active");
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", depts.size());
        stats.put("topLevel", depts.stream().filter(Department::isTopLevel).count());
        return stats;
    }

    @Override
    @Transactional
    public DepartmentResponse setDepartmentManager(Long deptId, Long userId) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ApiException(404, "部门不存在: " + deptId));

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(404, "用户不存在: " + userId));
            dept.setManagerId(userId);
        } else {
            dept.setManagerId(null);
        }

        departmentRepository.save(dept);
        return mapToResponse(dept);
    }

    private DepartmentResponse mapToResponse(Department dept) {
        int userCount = (int) departmentRepository.countUsersByDepartmentId(dept.getId());
        int childrenCount = (int) departmentRepository.countChildrenByParentId(dept.getId());

        String parentName = dept.getParent() != null ? dept.getParent().getName() : null;
        String managerName = dept.getManager() != null ? dept.getManager().getName() : null;

        return new DepartmentResponse(
                dept.getId(),
                dept.getName(),
                dept.getCode(),
                dept.getDescription(),
                dept.getParentId(),
                parentName,
                dept.getManagerId(),
                managerName,
                dept.getSortOrder(),
                dept.getStatus(),
                userCount,
                childrenCount,
                null,  // children 在树形查询时填充
                dept.getCreatedAt(),
                dept.getUpdatedAt()
        );
    }
}
