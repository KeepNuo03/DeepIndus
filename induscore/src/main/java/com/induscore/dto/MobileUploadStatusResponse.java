package com.induscore.dto;

/**
 * 安卓端抽检上传状态。
 */
public record MobileUploadStatusResponse(
        String idempotencyKey,
        String status,
        boolean deduplicated,
        String message,
        Long recordId,
        int retryCount,
        long updatedAt
) {}
