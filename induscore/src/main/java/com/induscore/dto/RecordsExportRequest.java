package com.induscore.dto;

import java.util.List;

/**
 * 批量导出请求 DTO。
 */
public record RecordsExportRequest(List<Long> recordIds, String format) {}
