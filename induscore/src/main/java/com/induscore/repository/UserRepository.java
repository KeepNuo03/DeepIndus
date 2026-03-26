package com.induscore.repository;

import com.induscore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名或邮箱查询用户。
     */
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    /**
     * 根据用户名查询用户。
     */
    Optional<User> findByUsername(String username);

    /**
     * 判断用户名是否存在（忽略大小写）。
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * 判断邮箱是否存在（忽略大小写）。
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * 判断用户名是否存在（精确匹配）。
     */
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否存在（精确匹配）。
     */
    boolean existsByEmail(String email);

    /**
     * 判断用户名是否存在（排除指定ID）。
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * 判断邮箱是否存在（排除指定ID）。
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * 根据部门ID查询用户。
     */
    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * 根据状态查询用户。
     */
    Page<User> findByStatus(String status, Pageable pageable);

    /**
     * 根据状态统计用户数。
     */
    long countByStatus(String status);

    /**
     * 根据在线状态统计用户数。
     */
    long countByOnlineStatus(byte onlineStatus);

    /**
     * 搜索用户（姓名、用户名、邮箱、手机号、员工编号）。
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.name LIKE :keyword OR " +
           "u.username LIKE :keyword OR " +
           "u.email LIKE :keyword OR " +
           "u.phone LIKE :keyword OR " +
           "u.employeeNo LIKE :keyword")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计拥有指定角色的用户数。
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") Long roleId);
}
