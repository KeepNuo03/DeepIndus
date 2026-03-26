package com.induscore.dto;

import java.util.List;

/**
 * 批量删除请求 DTO。
 */
public record RecordsBatchDeleteRequest(List<Long> recordIds) {}
