package com.induscore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 用户创建/更新请求 DTO。
 */
public record UserRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 64, message = "用户名长度不能超过64")
        String username,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128")
        String email,

        @NotBlank(message = "姓名不能为空")
        @Size(max = 128, message = "姓名长度不能超过128")
        String name,

        @Size(max = 20, message = "手机号长度不能超过20")
        String phone,

        @Size(max = 64, message = "员工编号长度不能超过64")
        String employeeNo,

        @Size(max = 128, message = "职位长度不能超过128")
        String position,

        Long departmentId,  // 所属部门ID

        String avatar,  // 头像URL

        String status,  // active/inactive

        String password,  // 密码（创建时必填，更新时可选）

        List<Long> roleIds  // 角色ID列表
) {}
