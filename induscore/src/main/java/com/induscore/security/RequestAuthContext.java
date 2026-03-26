package com.induscore.security;

import java.util.Collections;
import java.util.Set;

/**
 * 请求级认证上下文。
 */
public record RequestAuthContext(
        Long userId,
        String username,
        String clientType,
        Set<String> roles,
        Set<String> permissions
) {
    public RequestAuthContext {
        roles = roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
        permissions = permissions == null ? Collections.emptySet() : Collections.unmodifiableSet(permissions);
    }
}
