package com.induscore.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于路径与方法的轻量接口授权策略。
 */
@Component
public class RequestAuthorizationService {
    private final Set<String> allowedMobileClientTypes;
    private final Set<String> allowedMobileRoles;

    public RequestAuthorizationService(
            @Value("${mobile.auth.allowed-client-types:android,mobile}") String allowedMobileClientTypes,
            @Value("${mobile.auth.allowed-roles:super_admin,admin,production_manager,qc_inspector}") String allowedMobileRoles
    ) {
        this.allowedMobileClientTypes = splitLowercase(allowedMobileClientTypes);
        this.allowedMobileRoles = splitLowercase(allowedMobileRoles);
    }

    public AuthorizationDecision authorize(HttpServletRequest request, RequestAuthContext context) {
        String path = request.getRequestURI();
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String clientType = lower(context.clientType(), "pc");

        if (path.startsWith("/v1/mobile/")) {
            if (!allowedMobileClientTypes.contains(clientType)) {
                return AuthorizationDecision.deny(403, "当前客户端不允许访问移动端接口");
            }
            if (!hasAnyRole(context, allowedMobileRoles)) {
                return AuthorizationDecision.deny(403, "无移动端作业权限");
            }
            return AuthorizationDecision.allow();
        }

        // 移动端禁止直接访问管理中台接口（端能力控制）
        if (allowedMobileClientTypes.contains(clientType) && isManagementPath(path)) {
            return AuthorizationDecision.deny(403, "移动端不允许访问管理中台接口");
        }

        if (path.startsWith("/v1/users") || path.startsWith("/v1/roles") || path.startsWith("/v1/departments")) {
            if (!hasAnyRole(context, Set.of("super_admin", "admin"))) {
                return AuthorizationDecision.deny(403, "无用户权限管理访问权限");
            }
        }

        if (path.startsWith("/v1/models")) {
            if ("GET".equals(method)) {
                if (!hasAnyPermission(context, Set.of("model:view")) && !hasAnyRole(context, Set.of("super_admin", "admin", "technician"))) {
                    return AuthorizationDecision.deny(403, "无模型查看权限");
                }
            } else {
                if (!hasAnyPermission(context, Set.of("model:deploy")) && !hasAnyRole(context, Set.of("super_admin", "admin", "technician"))) {
                    return AuthorizationDecision.deny(403, "无模型管理权限");
                }
            }
        }

        if ("/v1/records/export".equals(path) || ("/v1/records/batch".equals(path) && "DELETE".equals(method))) {
            if (!hasAnyPermission(context, Set.of("detection:control")) && !hasAnyRole(context, Set.of("super_admin", "admin", "production_manager", "qc_inspector"))) {
                return AuthorizationDecision.deny(403, "无记录批量操作权限");
            }
        }
        return AuthorizationDecision.allow();
    }

    private boolean isManagementPath(String path) {
        return path.startsWith("/v1/models")
                || path.startsWith("/v1/users")
                || path.startsWith("/v1/roles")
                || path.startsWith("/v1/departments")
                || path.startsWith("/v1/production")
                || path.startsWith("/v1/products");
    }

    private boolean hasAnyRole(RequestAuthContext context, Set<String> requiredRoles) {
        return context.roles().stream().map(r -> lower(r, "")).anyMatch(requiredRoles::contains);
    }

    private boolean hasAnyPermission(RequestAuthContext context, Set<String> requiredPermissions) {
        return context.permissions().stream().map(p -> lower(p, "")).anyMatch(requiredPermissions::contains);
    }

    private Set<String> splitLowercase(String csv) {
        return Arrays.stream(csv.split(","))
                .map(s -> s == null ? "" : s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private String lower(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
