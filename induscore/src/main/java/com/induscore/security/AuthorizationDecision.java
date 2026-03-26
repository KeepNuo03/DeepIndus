package com.induscore.security;

/**
 * 接口授权决策结果。
 */
public record AuthorizationDecision(boolean allowed, int code, String message) {
    public static AuthorizationDecision allow() {
        return new AuthorizationDecision(true, 200, "ok");
    }

    public static AuthorizationDecision deny(int code, String message) {
        return new AuthorizationDecision(false, code, message);
    }
}
