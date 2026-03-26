package com.induscore.service;

import com.induscore.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理业务接口。
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserResponse createUser(UserRequest request);

    /**
     * 更新用户
     */
    UserResponse updateUser(Long id, UserRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 获取用户详情
     */
    UserResponse getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    UserResponse getUserByUsername(String username);

    /**
     * 查询用户列表
     */
    Map<String, Object> getUsers(UserQueryRequest query);

    /**
     * 获取用户统计
     */
    UserStatisticsResponse getUserStatistics();

    /**
     * 更新用户状态
     */
    UserResponse updateUserStatus(Long id, String status);

    /**
     * 分配角色
     */
    UserResponse assignRoles(Long userId, AssignRolesRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 重置密码（管理员）
     */
    String resetPassword(Long userId);

    /**
     * 更新登录信息
     */
    void updateLoginInfo(Long userId, String ip);

    /**
     * 更新在线状态
     */
    void updateOnlineStatus(Long userId, boolean online);
}
