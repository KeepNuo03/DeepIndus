package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.*;
import com.induscore.model.Role;
import com.induscore.model.User;
import com.induscore.repository.DepartmentRepository;
import com.induscore.repository.RoleRepository;
import com.induscore.repository.UserRepository;
import com.induscore.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理业务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                          DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(400, "用户名已存在: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(400, "邮箱已存在: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setEmployeeNo(request.employeeNo());
        user.setPosition(request.position());
        user.setDepartmentId(request.departmentId());
        user.setAvatar(request.avatar());
        user.setStatus(request.status() != null ? request.status() : "active");

        // 设置默认密码或传入的密码
        String rawPassword = request.password() != null ? request.password() : "123456";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        // 分配角色
        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.roleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ApiException(404, "角色不存在: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + id));

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new ApiException(400, "用户名已存在: " + request.username());
            }
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ApiException(400, "邮箱已存在: " + request.email());
            }
            user.setEmail(request.email());
        }

        if (request.name() != null) user.setName(request.name());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.employeeNo() != null) user.setEmployeeNo(request.employeeNo());
        if (request.position() != null) user.setPosition(request.position());
        if (request.departmentId() != null) user.setDepartmentId(request.departmentId());
        if (request.avatar() != null) user.setAvatar(request.avatar());
        if (request.status() != null) user.setStatus(request.status());

        // 更新角色
        if (request.roleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.roleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ApiException(404, "角色不存在: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + id));
        user.getRoles().size(); // 强制初始化
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + username));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUsers(UserQueryRequest query) {
        PageRequest pageable = PageRequest.of(query.page() - 1, query.pageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> result;

        // 根据筛选条件查询
        if (!"all".equals(query.department()) && query.department() != null) {
            Long deptId = Long.parseLong(query.department());
            result = userRepository.findByDepartmentId(deptId, pageable);
        } else if (!"all".equals(query.status()) && query.status() != null) {
            result = userRepository.findByStatus(query.status(), pageable);
        } else if (query.search() != null && !query.search().isBlank()) {
            String keyword = "%" + query.search().trim() + "%";
            result = userRepository.findByKeyword(keyword, pageable);
        } else {
            result = userRepository.findAll(pageable);
        }

        List<UserResponse> list = result.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("users", list);
        data.put("total", result.getTotalElements());
        data.put("page", query.page());
        data.put("pageSize", query.pageSize());
        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        long total = userRepository.count();
        long active = userRepository.countByStatus("active");
        long inactive = userRepository.countByStatus("inactive");
        long online = userRepository.countByOnlineStatus((byte) 1);

        return new UserStatisticsResponse(
                (int) total, (int) active, (int) inactive, (int) online
        );
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, String status) {
        if (!"active".equals(status) && !"inactive".equals(status)) {
            throw new ApiException(400, "无效的状态值: " + status);
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + id));
        user.setStatus(status);
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse assignRoles(Long userId, AssignRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + userId));

        Set<Role> roles = new HashSet<>();
        if (request.roleIds() != null) {
            for (Long roleId : request.roleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ApiException(404, "角色不存在: " + roleId));
                roles.add(role);
            }
        }

        user.setRoles(roles);
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + userId));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new ApiException(400, "原密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + userId));

        String newPassword = "123456"; // 默认重置密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return newPassword;
    }

    @Override
    @Transactional
    public void updateLoginInfo(Long userId, String ip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + userId));

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        user.setLoginCount(user.getLoginCount() + 1);
        user.setOnlineStatus((byte) 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Long userId, boolean online) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "用户不存在: " + userId));
        user.setOnlineStatus(online ? (byte) 1 : (byte) 0);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        String deptName = null;
        if (user.getDepartmentId() != null) {
            deptName = departmentRepository.findById(user.getDepartmentId())
                    .map(d -> d.getName())
                    .orElse("未知部门");
        }

        String lastLoginStr = user.getLastLoginAt() != null
                ? user.getLastLoginAt().format(DATE_TIME)
                : null;

        List<RoleSimpleResponse> roles = user.getRoles().stream()
                .map(r -> new RoleSimpleResponse(r.getId(), r.getName(), r.getCode()))
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getEmployeeNo(),
                user.getPosition(),
                user.getAvatar(),
                user.getStatus(),
                user.getOnlineStatus(),
                user.isOnline(),
                lastLoginStr,
                user.getLastLoginIp(),
                user.getLoginCount(),
                user.getDepartmentId(),
                deptName,
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
