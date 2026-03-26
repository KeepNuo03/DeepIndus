package com.induscore.dto;

/**
 * 用户统计响应 DTO。
 */
public record UserStatisticsResponse(
        Integer total,      // 总用户数
        Integer active,     // 启用用户数
        Integer inactive,   // 禁用用户数
        Integer online      // 在线用户数
) {}
