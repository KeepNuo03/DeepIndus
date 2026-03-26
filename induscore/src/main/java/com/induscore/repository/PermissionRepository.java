package com.induscore.repository;

import com.induscore.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 权限数据访问层。
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    /**
     * 根据权限编码查询。
     */
    Optional<Permission> findByCode(String code);
}
