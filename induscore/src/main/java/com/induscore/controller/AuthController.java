package com.induscore.controller;

import com.induscore.common.ApiResponse;
import com.induscore.dto.LoginRequest;
import com.induscore.dto.RegisterRequest;
import com.induscore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证相关接口（登录/注册/退出）。
 */
@RestController
@RequestMapping({"/v1/auth", "/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录，返回 Token 和用户信息。
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    /**
     * 用户注册，仅返回成功提示。
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功", Map.of());
    }

    /**
     * 退出登录（前端清理 Token 即可）。
     */
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        return ApiResponse.success("退出成功", Map.of());
    }
}
