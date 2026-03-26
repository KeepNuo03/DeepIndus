package com.induscore.repository;

import com.induscore.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门数据访问接口。
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据编码查找部门
     */
    Optional<Department> findByCode(String code);

    /**
     * 检查编码是否已存在
     */
    boolean existsByCode(String code);

    /**
     * 检查编码是否与其他部门重复
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 根据状态查询部门列表
     */
    List<Department> findByStatusOrderBySortOrderAsc(String status);

    /**
     * 查询所有顶级部门（无上级）
     */
    List<Department> findByParentIdIsNullAndStatusOrderBySortOrderAsc(String status);

    /**
     * 查询指定父部门的所有子部门
     */
    List<Department> findByParentIdAndStatusOrderBySortOrderAsc(Long parentId, String status);

    /**
     * 根据负责人ID查询管理的部门
     */
    List<Department> findByManagerId(Long managerId);

    /**
     * 搜索部门（名称或描述包含关键字）
     */
    @Query("SELECT d FROM Department d WHERE " +
           "(d.name LIKE %:keyword% OR d.description LIKE %:keyword% OR d.code LIKE %:keyword%) " +
           "AND d.status = :status " +
           "ORDER BY d.sortOrder ASC")
    List<Department> searchByKeyword(@Param("keyword") String keyword, @Param("status") String status);

    /**
     * 统计部门的子部门数量
     */
    @Query("SELECT COUNT(d) FROM Department d WHERE d.parentId = :parentId AND d.status = 'active'")
    long countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 统计部门的员工数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.departmentId = :deptId AND u.status = 'active'")
    long countUsersByDepartmentId(@Param("deptId") Long deptId);
}
