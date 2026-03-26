package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.LoginRequest;
import com.induscore.dto.RegisterRequest;
import com.induscore.dto.UserResponse;
import com.induscore.model.User;
import com.induscore.repository.RoleRepository;
import com.induscore.repository.UserRepository;
import com.induscore.security.JwtUtil;
import com.induscore.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证业务实现类。
 */
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * 历史初始化 SQL 中使用的错误密码哈希（宣称为 123456，实际无法通过 BCrypt 校验）。
     * 兼容策略：当用户输入 123456 且命中该旧哈希时，自动升级为标准 BCrypt 哈希。
     */
    private static final String LEGACY_BROKEN_DEFAULT_HASH =
            "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.";


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        // 通过用户名或邮箱查询用户
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                        request.username(), request.username()
                )
                .orElseThrow(() -> new ApiException(401, "用户名或密码错误"));

        // 校验密码（兼容历史错误哈希，首次成功登录后自动修复）
        if (!matchesPasswordWithLegacyFallback(user, request.password())) {
            throw new ApiException(401, "用户名或密码错误");
        }

        // 校验账户状态
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new ApiException(403, "账号已被禁用");
        }

        // 生成 token 并返回用户信息
        String token = generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", toResponse(user));
        return data;
    }

    /**
     * 校验密码，并兼容历史错误哈希自动升级。
     */
    private boolean matchesPasswordWithLegacyFallback(User user, String rawPassword) {
        if (passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return true;
        }

        // 历史脚本错误哈希兼容：仅允许默认密码触发升级，避免放宽鉴权边界
        if (LEGACY_BROKEN_DEFAULT_HASH.equals(user.getPasswordHash()) && "123456".equals(rawPassword)) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void register(RegisterRequest request) {
        // 校验密码一致性
        if (!request.password().equals(request.confirmPassword())) {
            throw new ApiException(400, "两次密码输入不一致");
        }
        // 校验用户名/邮箱唯一性
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ApiException(400, "用户名已存在");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(400, "邮箱已存在");
        }

        // 生成新用户并加密密码
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setName(request.username());
        user.setAvatar("https://cdn.induscore.com/avatars/default.png");
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 默认分配生产角色
        roleRepository.findByCode("production")
                .ifPresent(role -> user.getRoles().add(role));
        userRepository.save(user);
    }

    /**
     * 生成用户 JWT Token。
     */
    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        Set<String> permissions = new HashSet<>();
        user.getRoles().forEach(role -> role.getPermissions().forEach(permission -> permissions.add(permission.getCode())));
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles().stream().map(r -> r.getCode()).toList());
        claims.put("permissions", permissions);
        return jwtUtil.generateToken(claims, user.getUsername());
    }

    /**
     * 将实体转为前端需要的用户结构。
     */
    private UserResponse toResponse(User user) {
        List<com.induscore.dto.RoleSimpleResponse> roles = user.getRoles().stream()
                .map(r -> new com.induscore.dto.RoleSimpleResponse(r.getId(), r.getName(), r.getCode()))
                .collect(Collectors.toList());

        String lastLoginStr = user.getLastLoginAt() != null
                ? user.getLastLoginAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

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
                null,  // departmentName 需要额外查询，这里简化处理
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
