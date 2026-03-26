package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.DepartmentRequest;
import com.induscore.dto.DepartmentResponse;
import com.induscore.service.DepartmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 部门管理接口控制器。
 */
@RestController
@RequestMapping("/v1")
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 获取部门树形结构
     */
    @GetMapping("/departments/tree")
    public ApiResponse<List<DepartmentResponse>> getDepartmentTree() {
        return ApiResponse.success(departmentService.getDepartmentTree());
    }

    /**
     * 获取部门列表（平级）
     */
    @GetMapping("/departments")
    public ApiResponse<List<DepartmentResponse>> getAllDepartments() {
        return ApiResponse.success(departmentService.getAllDepartments());
    }

    /**
     * 获取活跃部门列表（用于下拉选择）
     */
    @GetMapping("/departments/active")
    public ApiResponse<List<DepartmentResponse>> getActiveDepartments() {
        return ApiResponse.success(departmentService.getActiveDepartments());
    }

    /**
     * 获取部门统计
     */
    @GetMapping("/departments/statistics")
    public ApiResponse<Map<String, Object>> getDepartmentStatistics() {
        return ApiResponse.success(departmentService.getDepartmentStatistics());
    }

    /**
     * 根据ID获取部门详情
     */
    @GetMapping("/departments/{id}")
    public ApiResponse<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return ApiResponse.success(departmentService.getDepartmentById(id));
    }

    /**
     * 创建部门
     */
    @PostMapping("/departments")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        log.info("创建部门请求: name={}, code={}", request.name(), request.code());
        DepartmentResponse dept = departmentService.createDepartment(request);
        return ApiResponse.success(dept);
    }

    /**
     * 更新部门
     */
    @PutMapping("/departments/{id}")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        log.info("更新部门请求: id={}", id);
        DepartmentResponse dept = departmentService.updateDepartment(id, request);
        return ApiResponse.success(dept);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/departments/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        log.info("删除部门请求: id={}", id);
        departmentService.deleteDepartment(id);
        return ApiResponse.success(null);
    }

    /**
     * 更新部门状态
     */
    @PostMapping("/departments/{id}/status")
    public ApiResponse<DepartmentResponse> updateDepartmentStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.info("更新部门状态: id={}, status={}", id, status);
        DepartmentResponse dept = departmentService.updateDepartmentStatus(id, status);
        return ApiResponse.success(dept);
    }

    /**
     * 设置部门负责人
     */
    @PostMapping("/departments/{id}/manager")
    public ApiResponse<DepartmentResponse> setDepartmentManager(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        log.info("设置部门负责人: deptId={}, userId={}", id, userId);
        DepartmentResponse dept = departmentService.setDepartmentManager(id, userId);
        return ApiResponse.success(dept);
    }
}
