package com.induscore.dto;

/**
 * 安卓端复核任务项。
 */
public record MobileReviewTaskResponse(
        Long id,
        String detectionNo,
        String serialNo,
        String defect,
        String severity,
        String processStatus,
        String timestamp,
        String imageUrl
) {}
