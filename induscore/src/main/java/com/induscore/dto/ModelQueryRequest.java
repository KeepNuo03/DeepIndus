package com.induscore.dto;

/**
 * AI 模型查询请求 DTO。
 */
public record ModelQueryRequest(
        String status,
        String architecture,
        String keyword,
        Integer page,
        Integer pageSize
) {
    public ModelQueryRequest {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
    }
}
