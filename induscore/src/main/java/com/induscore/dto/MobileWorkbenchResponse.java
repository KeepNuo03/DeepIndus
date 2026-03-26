package com.induscore.dto;

/**
 * 安卓端工作台汇总信息。
 */
public record MobileWorkbenchResponse(
        long pendingReviewCount,
        long processingCount,
        long todaySamplingCount,
        long severeAlertCount,
        String uploadQueueStatus
) {}
