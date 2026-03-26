package com.induscore.dto;

/**
 * 用户查询请求 DTO。
 */
public record UserQueryRequest(
        String department,  // all 或部门ID
        String status,      // all/active/inactive
        String search,      // 搜索关键字（姓名、用户名、邮箱、手机号）
        Integer page,
        Integer pageSize
) {
    public UserQueryRequest {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
    }
}
