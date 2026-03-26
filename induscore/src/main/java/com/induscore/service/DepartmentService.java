package com.induscore.service;

import com.induscore.dto.DepartmentRequest;
import com.induscore.dto.DepartmentResponse;

import java.util.List;
import java.util.Map;

/**
 * 部门管理业务接口。
 */
public interface DepartmentService {

    /**
     * 创建部门
     */
    DepartmentResponse createDepartment(DepartmentRequest request);

    /**
     * 更新部门
     */
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    /**
     * 删除部门
     */
    void deleteDepartment(Long id);

    /**
     * 获取部门详情
     */
    DepartmentResponse getDepartmentById(Long id);

    /**
     * 获取所有部门（树形结构）
     */
    List<DepartmentResponse> getDepartmentTree();

    /**
     * 获取部门列表（平级）
     */
    List<DepartmentResponse> getAllDepartments();

    /**
     * 获取活跃部门列表（用于下拉选择）
     */
    List<DepartmentResponse> getActiveDepartments();

    /**
     * 更新部门状态
     */
    DepartmentResponse updateDepartmentStatus(Long id, String status);

    /**
     * 获取部门统计
     */
    Map<String, Object> getDepartmentStatistics();

    /**
     * 设置部门负责人
     */
    DepartmentResponse setDepartmentManager(Long deptId, Long userId);
}
