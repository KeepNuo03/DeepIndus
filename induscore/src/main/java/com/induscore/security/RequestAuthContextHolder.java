package com.induscore.security;

import java.util.Optional;

/**
 * 通过 ThreadLocal 在一次请求链路内传递认证上下文。
 */
public final class RequestAuthContextHolder {
    private static final ThreadLocal<RequestAuthContext> CONTEXT = new ThreadLocal<>();

    private RequestAuthContextHolder() {
    }

    public static void set(RequestAuthContext context) {
        CONTEXT.set(context);
    }

    public static Optional<RequestAuthContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
