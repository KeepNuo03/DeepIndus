package com.induscore.service;

import com.induscore.dto.LoginRequest;
import com.induscore.dto.RegisterRequest;

import java.util.Map;

/**
 * 认证业务接口。
 */
public interface AuthService {
    /**
     * 登录并返回 token + user 信息。
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * 注册新用户。
     */
    void register(RegisterRequest request);
}

