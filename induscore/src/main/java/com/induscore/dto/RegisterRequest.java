package com.induscore.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求 DTO。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        String email,
        String companyCode,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码至少6位")
        String password,
        @NotBlank(message = "确认密码不能为空")
        String confirmPassword,
        @AssertTrue(message = "请先同意服务协议")
        Boolean agreeTerms
) {}
