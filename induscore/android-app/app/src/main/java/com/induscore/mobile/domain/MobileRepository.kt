package com.induscore.mobile.domain

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.induscore.mobile.data.local.dao.ReviewTaskCacheDao
import com.induscore.mobile.data.local.dao.UploadOperationLogDao
import com.induscore.mobile.data.local.dao.UploadQueueDao
import com.induscore.mobile.data.local.entity.UploadOperationLogEntity
import com.induscore.mobile.data.local.entity.ReviewTaskCacheEntity
import com.induscore.mobile.data.local.entity.UploadQueueEntity
import com.induscore.mobile.data.remote.ApiBusinessException
import com.induscore.mobile.data.remote.MobileApiService
import com.induscore.mobile.data.remote.dto.MobileNotificationDto
import com.induscore.mobile.data.remote.dto.MobilePilotMetricsDto
import com.induscore.mobile.data.remote.dto.MobileProfileDto
import com.induscore.mobile.data.remote.dto.MobileUploadStatusDto
import com.induscore.mobile.data.remote.dto.MobileReviewDecisionRequest
import com.induscore.mobile.data.remote.dto.MobileReviewTaskDetailDto
import com.induscore.mobile.data.remote.dto.MobileReviewTaskDto
import com.induscore.mobile.data.remote.dto.MobileWorkbenchDto
import com.induscore.mobile.data.remote.dto.AgentArchiveResponseDto
import com.induscore.mobile.data.remote.dto.AgentChatContext
import com.induscore.mobile.data.remote.dto.AgentChatMessage
import com.induscore.mobile.data.remote.dto.AgentChatOptions
import com.induscore.mobile.data.remote.dto.AgentChatRequest
import com.induscore.mobile.data.remote.dto.AgentChatResponse
import com.induscore.mobile.data.remote.dto.AgentSessionDetailDto
import com.induscore.mobile.data.remote.dto.AgentSessionPageDto
import com.induscore.mobile.data.remote.dto.AgentUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class MobileRepository(
    private val mobileApiService: MobileApiService,
    private val reviewTaskCacheDao: ReviewTaskCacheDao,
    private val uploadQueueDao: UploadQueueDao,
    private val uploadOperationLogDao: UploadOperationLogDao
) {
    sealed interface AgentStreamEvent {
        data class Meta(val sessionId: String?, val traceId: String?) : AgentStreamEvent
        data class Tool(val tool: String?, val success: Boolean, val latencyMs: Long) : AgentStreamEvent
        data class Chunk(val text: String) : AgentStreamEvent
        data class Done(val status: String?, val usage: AgentUsage?) : AgentStreamEvent
        data class Error(val errorCode: String?, val message: String?) : AgentStreamEvent
    }

    suspend fun getWorkbench(): MobileWorkbenchDto {
        val response = mobileApiService.getWorkbench()
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun getReviewTasks(page: Int, pageSize: Int, status: String): List<MobileReviewTaskDto> {
        val response = mobileApiService.getReviewTasks(page = page, pageSize = pageSize, status = status)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        val items = response.data.items
        reviewTaskCacheDao.upsertAll(items.map { it.toEntity() })
        return items
    }

    suspend fun enqueueUpload(
        localFilePath: String,
        serialNo: String? = null,
        productId: Long? = null,
        productionLineId: Long? = null,
        networkState: String? = null
    ): String {
        val key = "MOB-" + UUID.randomUUID().toString()
        uploadQueueDao.upsert(
            UploadQueueEntity(
                idempotencyKey = key,
                localFilePath = localFilePath,
                serialNo = serialNo,
                productId = productId,
                productionLineId = productionLineId,
                networkState = networkState,
                status = "pending"
            )
        )
        return key
    }

    fun observeUploadQueue(): Flow<List<UploadQueueEntity>> {
        return uploadQueueDao.observeQueue()
    }

    fun observeRecentOperationLogs(limit: Int = 10): Flow<List<UploadOperationLogEntity>> {
        return uploadOperationLogDao.observeRecent(limit)
    }

    suspend fun appendOperationLog(
        actionType: String,
        targetScope: String,
        affectedCount: Int,
        success: Boolean,
        message: String?
    ) {
        uploadOperationLogDao.insert(
            UploadOperationLogEntity(
                actionType = actionType,
                targetScope = targetScope,
                affectedCount = affectedCount,
                success = success,
                message = message
            )
        )
    }

    suspend fun markUploadForManualRetry(id: Long, resetRetryCount: Boolean = true): Boolean {
        val current = uploadQueueDao.findById(id) ?: return false
        val nextRetry = if (resetRetryCount) 0 else current.retryCount
        uploadQueueDao.update(
            current.copy(
                status = "pending",
                retryCount = nextRetry,
                lastError = null,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun markAllFailedForManualRetry(resetRetryCount: Boolean = true): Int {
        val failed = uploadQueueDao.getFailedOrDead()
        failed.forEach { item ->
            val nextRetry = if (resetRetryCount) 0 else item.retryCount
            uploadQueueDao.update(
                item.copy(
                    status = "pending",
                    retryCount = nextRetry,
                    lastError = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return failed.size
    }

    suspend fun markDeadForManualRetry(resetRetryCount: Boolean = true): Int {
        val deadItems = uploadQueueDao.getDeadOnly()
        deadItems.forEach { item ->
            val nextRetry = if (resetRetryCount) 0 else item.retryCount
            uploadQueueDao.update(
                item.copy(
                    status = "pending",
                    retryCount = nextRetry,
                    lastError = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return deadItems.size
    }

    suspend fun cleanupCompletedHistory(keepRecentMs: Long): Int {
        val before = System.currentTimeMillis() - keepRecentMs
        return uploadQueueDao.cleanupCompleted(before)
    }

    suspend fun cleanupDeadHistory(keepRecentMs: Long): Int {
        val before = System.currentTimeMillis() - keepRecentMs
        return uploadQueueDao.cleanupDead(before)
    }

    suspend fun getReviewTaskDetail(taskId: Long): MobileReviewTaskDetailDto {
        val response = mobileApiService.getTaskDetail(taskId)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data.toReviewTaskDetail(taskId)
    }

    suspend fun submitTaskReview(taskId: Long, decision: String, comment: String?) {
        val action = when (decision.lowercase()) {
            "approve" -> "confirm"
            "reject" -> "reject"
            else -> decision
        }
        val payload = MobileReviewDecisionRequest(
            action = action,
            note = comment?.takeIf { it.isNotBlank() }
        )
        val response = mobileApiService.submitTaskReview(taskId, payload)
        if (response.code != 200) {
            throw ApiBusinessException(response.code, response.message)
        }
    }

    suspend fun getUploadStatus(idempotencyKey: String): MobileUploadStatusDto {
        val localItem = uploadQueueDao.findByKey(idempotencyKey)
        val response = mobileApiService.getUploadStatus(idempotencyKey)
        if (response.code != 200 || response.data == null) {
            // 后端不可用时兜底本地状态，保障弱网场景可追踪。
            if (localItem != null) {
                return MobileUploadStatusDto(
                    idempotencyKey = localItem.idempotencyKey,
                    status = localItem.status,
                    message = localItem.lastError,
                    serverRecordId = localItem.serverRecordId,
                    retryCount = localItem.retryCount,
                    updatedAt = localItem.updatedAt.toString()
                )
            }
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data.toUploadStatus(idempotencyKey)
    }

    suspend fun getNotifications(limit: Int = 10): List<MobileNotificationDto> {
        val response = mobileApiService.getNotifications(limit = limit)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun getProfile(): MobileProfileDto {
        val response = mobileApiService.getProfile()
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun getPilotMetrics(): MobilePilotMetricsDto {
        val response = mobileApiService.getPilotMetrics()
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun chatWithAgent(
        question: String,
        sessionId: String? = null,
        scene: String = "general",
        page: String = "android_agent",
        pageParams: Map<String, Any?>? = null,
        taskId: Long? = null,
        idempotencyKey: String? = null
    ): AgentChatResponse {
        val userMessage = AgentChatMessage(role = "user", content = question)
        val request = AgentChatRequest(
            sessionId = sessionId,
            messages = listOf(userMessage),
            message = userMessage,
            context = AgentChatContext(
                scene = scene,
                page = page,
                pageParams = pageParams,
                taskId = taskId,
                idempotencyKey = idempotencyKey
            ),
            options = AgentChatOptions(stream = false),
            stream = false
        )
        val response = mobileApiService.chatWithAgent(request)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun streamChatWithAgent(
        question: String,
        sessionId: String? = null,
        scene: String = "general",
        page: String = "android_agent",
        pageParams: Map<String, Any?>? = null,
        taskId: Long? = null,
        idempotencyKey: String? = null,
        onEvent: (AgentStreamEvent) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val userMessage = AgentChatMessage(role = "user", content = question)
            val request = AgentChatRequest(
                sessionId = sessionId,
                messages = listOf(userMessage),
                message = userMessage,
                context = AgentChatContext(
                    scene = scene,
                    page = page,
                    pageParams = pageParams,
                    taskId = taskId,
                    idempotencyKey = idempotencyKey
                ),
                options = AgentChatOptions(stream = true),
                stream = true
            )

            val response = mobileApiService.streamChatWithAgent(request)
            if (!response.isSuccessful || response.body() == null) {
                throw ApiBusinessException(response.code(), response.message())
            }
            response.body()!!.use { body ->
                val source = body.source()
                var currentEvent: String? = null
                val dataLines = mutableListOf<String>()
                while (!source.exhausted()) {
                    val rawLine = source.readUtf8Line() ?: break
                    if (rawLine.isBlank()) {
                        dispatchSseEvent(currentEvent, dataLines, onEvent)
                        currentEvent = null
                        dataLines.clear()
                        continue
                    }
                    when {
                        rawLine.startsWith("event:") -> currentEvent = rawLine.substringAfter("event:").trim()
                        rawLine.startsWith("data:") -> dataLines.add(rawLine.substringAfter("data:").trim())
                    }
                }
                dispatchSseEvent(currentEvent, dataLines, onEvent)
            }
        }
    }

    suspend fun listAgentSessions(page: Int = 1, pageSize: Int = 20): AgentSessionPageDto {
        val response = mobileApiService.listAgentSessions(page = page, pageSize = pageSize)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun getAgentSessionDetail(
        sessionId: String,
        page: Int = 1,
        pageSize: Int = 50
    ): AgentSessionDetailDto {
        val response = mobileApiService.getAgentSessionDetail(sessionId = sessionId, page = page, pageSize = pageSize)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    suspend fun archiveAgentSession(sessionId: String): AgentArchiveResponseDto {
        val response = mobileApiService.archiveAgentSession(sessionId)
        if (response.code != 200 || response.data == null) {
            throw ApiBusinessException(response.code, response.message)
        }
        return response.data
    }

    private fun JsonObject.toReviewTaskDetail(taskId: Long): MobileReviewTaskDetailDto {
        return MobileReviewTaskDetailDto(
            id = getAsLong("id") ?: taskId,
            detectionNo = getAsString("detectionNo"),
            serialNo = getAsString("serialNo"),
            defect = getAsString("defect"),
            severity = getAsString("severity"),
            processStatus = getAsString("processStatus"),
            timestamp = getAsString("timestamp"),
            imageUrl = getAsString("imageUrl"),
            modelSuggestion = getAsString("modelSuggestion"),
            reviewResult = getAsString("reviewResult"),
            reviewComment = getAsString("reviewComment")
        )
    }

    private fun JsonObject.toUploadStatus(idempotencyKey: String): MobileUploadStatusDto {
        val uploadStatus = optJsonObject("uploadStatus") ?: this
        return MobileUploadStatusDto(
            idempotencyKey = uploadStatus.getAsString("idempotencyKey") ?: idempotencyKey,
            status = uploadStatus.getAsString("status") ?: "unknown",
            message = uploadStatus.getAsString("message"),
            serverRecordId = uploadStatus.getAsLong("recordId") ?: uploadStatus.getAsLong("serverRecordId"),
            retryCount = uploadStatus.getAsInt("retryCount"),
            updatedAt = uploadStatus.getAsString("updatedAt")
        )
    }

    private fun JsonObject.getAsString(key: String): String? {
        if (!has(key) || get(key).isJsonNull) return null
        return runCatching { get(key).asString }.getOrNull()
    }

    private fun JsonObject.getAsBoolean(key: String): Boolean? {
        if (!has(key) || get(key).isJsonNull) return null
        return runCatching { get(key).asBoolean }.getOrNull()
    }

    private fun JsonObject.getAsLong(key: String): Long? {
        if (!has(key) || get(key).isJsonNull) return null
        return runCatching { get(key).asLong }.getOrNull()
    }

    private fun JsonObject.getAsInt(key: String): Int? {
        if (!has(key) || get(key).isJsonNull) return null
        return runCatching { get(key).asInt }.getOrNull()
    }

    private fun JsonObject.optJsonObject(key: String): JsonObject? {
        if (!has(key) || get(key).isJsonNull) return null
        return runCatching { getAsJsonObject(key) }.getOrNull()
    }

    private fun dispatchSseEvent(
        eventName: String?,
        dataLines: List<String>,
        onEvent: (AgentStreamEvent) -> Unit
    ) {
        if (eventName.isNullOrBlank()) {
            return
        }
        val data = dataLines.joinToString("\n")
        val payload = runCatching {
            if (data.isBlank()) JsonObject() else JsonParser.parseString(data).asJsonObject
        }.getOrElse { JsonObject() }
        when (eventName.lowercase()) {
            "meta" -> onEvent(
                AgentStreamEvent.Meta(
                    sessionId = payload.getAsString("sessionId"),
                    traceId = payload.getAsString("traceId")
                )
            )
            "tool" -> onEvent(
                AgentStreamEvent.Tool(
                    tool = payload.getAsString("tool"),
                    success = payload.getAsBoolean("success") ?: false,
                    latencyMs = payload.getAsLong("latencyMs") ?: 0L
                )
            )
            "chunk" -> {
                val text = payload.getAsString("text").orEmpty()
                if (text.isNotEmpty()) {
                    onEvent(AgentStreamEvent.Chunk(text))
                }
            }
            "done" -> onEvent(
                AgentStreamEvent.Done(
                    status = payload.getAsString("status"),
                    usage = payload.optJsonObject("usage")?.let { usage ->
                        AgentUsage(
                            promptTokens = usage.getAsInt("promptTokens") ?: 0,
                            completionTokens = usage.getAsInt("completionTokens") ?: 0,
                            totalTokens = usage.getAsInt("totalTokens") ?: 0
                        )
                    }
                )
            )
            "error" -> onEvent(
                AgentStreamEvent.Error(
                    errorCode = payload.getAsString("errorCode"),
                    message = payload.getAsString("message")
                )
            )
        }
    }

    private fun MobileReviewTaskDto.toEntity(): ReviewTaskCacheEntity {
        return ReviewTaskCacheEntity(
            taskId = id,
            detectionNo = detectionNo,
            serialNo = serialNo,
            defect = defect,
            severity = severity,
            processStatus = processStatus,
            timestamp = timestamp,
            imageUrl = imageUrl
        )
    }
}
