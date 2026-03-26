package com.induscore.repository;

import com.induscore.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口。
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据编码查找角色。
     */
    Optional<Role> findByCode(String code);

    /**
     * 检查编码是否已存在。
     */
    boolean existsByCode(String code);

    /**
     * 检查编码是否与其他角色重复。
     */
    boolean existsByCodeAndIdNot(String code, Long id);
}
