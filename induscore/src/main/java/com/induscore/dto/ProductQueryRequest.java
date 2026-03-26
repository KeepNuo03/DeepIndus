package com.induscore.dto;

/**
 * 产品查询参数 DTO。
 */
public record ProductQueryRequest(
        String category, // all/steel/aluminum/plastic
        String search,
        String status, // all/active/inactive
        Integer page,
        Integer pageSize
) {
    public ProductQueryRequest {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (category == null) {
            category = "all";
        }
        if (status == null) {
            status = "all";
        }
    }
}
