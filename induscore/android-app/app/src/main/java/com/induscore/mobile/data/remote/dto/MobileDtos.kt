package com.induscore.mobile.data.remote.dto

data class MobileWorkbenchDto(
    val pendingReviewCount: Long = 0,
    val processingCount: Long = 0,
    val todaySamplingCount: Long = 0,
    val severeAlertCount: Long = 0,
    val uploadQueueStatus: String = "idle"
)

data class MobileReviewTaskDto(
    val id: Long,
    val detectionNo: String? = null,
    val serialNo: String? = null,
    val defect: String? = null,
    val severity: String? = null,
    val processStatus: String? = null,
    val timestamp: String? = null,
    val imageUrl: String? = null
)

data class MobileReviewListDto(
    val items: List<MobileReviewTaskDto> = emptyList(),
    val total: Long = 0,
    val page: Int = 1,
    val pageSize: Int = 20,
    val status: String = "all"
)

data class MobileReviewTaskDetailDto(
    val id: Long,
    val detectionNo: String? = null,
    val serialNo: String? = null,
    val defect: String? = null,
    val severity: String? = null,
    val processStatus: String? = null,
    val timestamp: String? = null,
    val imageUrl: String? = null,
    val modelSuggestion: String? = null,
    val reviewResult: String? = null,
    val reviewComment: String? = null
)

data class MobileReviewDecisionRequest(
    val action: String,
    val note: String? = null
)

data class MobileUploadStatusDto(
    val idempotencyKey: String,
    val status: String,
    val message: String? = null,
    val serverRecordId: Long? = null,
    val retryCount: Int? = null,
    val updatedAt: String? = null
)

data class MobileNotificationDto(
    val id: Long? = null,
    val title: String? = null,
    val severity: String? = null,
    val timestamp: String? = null,
    val taskStatus: String? = null,
    val detectionNo: String? = null
)

data class MobileProfileDto(
    val userId: Long? = null,
    val username: String? = null,
    val name: String? = null,
    val departmentId: Long? = null,
    val position: String? = null,
    val clientType: String? = null,
    val roles: List<String>? = null,
)

data class MobilePilotMetricsDto(
    val pilotLine: String? = null,
    val targetCloseLoopRate: String? = null,
    val targetUploadRecoveryRate: String? = null,
    val targetApiP95LatencyMs: Int? = null,
    val todayReviewTotal: Long? = null,
    val todayReviewClosed: Long? = null,
    val todayReviewCloseLoopRate: String? = null,
    val uploadCompleted: Long? = null,
    val uploadFailed: Long? = null,
    val uploadRecoveryRate: String? = null,
    val generatedAt: String? = null
)
