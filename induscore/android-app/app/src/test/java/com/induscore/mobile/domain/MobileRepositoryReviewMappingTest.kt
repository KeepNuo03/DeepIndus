package com.induscore.mobile.domain

import com.google.gson.JsonObject
import com.induscore.mobile.data.local.dao.ReviewTaskCacheDao
import com.induscore.mobile.data.local.dao.UploadOperationLogDao
import com.induscore.mobile.data.local.dao.UploadQueueDao
import com.induscore.mobile.data.local.entity.UploadOperationLogEntity
import com.induscore.mobile.data.local.entity.ReviewTaskCacheEntity
import com.induscore.mobile.data.local.entity.UploadQueueEntity
import com.induscore.mobile.data.remote.MobileApiService
import com.induscore.mobile.data.remote.dto.ApiEnvelope
import com.induscore.mobile.data.remote.dto.LoginPayload
import com.induscore.mobile.data.remote.dto.LoginRequest
import com.induscore.mobile.data.remote.dto.MobileNotificationDto
import com.induscore.mobile.data.remote.dto.MobilePilotMetricsDto
import com.induscore.mobile.data.remote.dto.MobileProfileDto
import com.induscore.mobile.data.remote.dto.MobileReviewDecisionRequest
import com.induscore.mobile.data.remote.dto.MobileReviewListDto
import com.induscore.mobile.data.remote.dto.MobileWorkbenchDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Test

class MobileRepositoryReviewMappingTest {

    @Test
    fun `submit review should map approve to confirm`() = runBlocking {
        val capture = mutableListOf<MobileReviewDecisionRequest>()
        val api = buildFakeApi(capture)
        val repo = MobileRepository(api, FakeReviewTaskCacheDao(), FakeUploadQueueDao(), FakeUploadOperationLogDao())

        repo.submitTaskReview(taskId = 100L, decision = "approve", comment = "ok")
        repo.submitTaskReview(taskId = 101L, decision = "reject", comment = "ng")

        assertEquals("confirm", capture[0].action)
        assertEquals("ok", capture[0].note)
        assertEquals("reject", capture[1].action)
        assertEquals("ng", capture[1].note)
    }

    private fun buildFakeApi(capture: MutableList<MobileReviewDecisionRequest>): MobileApiService {
        return object : MobileApiService {
            override suspend fun login(body: LoginRequest): ApiEnvelope<LoginPayload> {
                error("unused")
            }

            override suspend fun getWorkbench(): ApiEnvelope<MobileWorkbenchDto> {
                error("unused")
            }

            override suspend fun getReviewTasks(page: Int, pageSize: Int, status: String): ApiEnvelope<MobileReviewListDto> {
                error("unused")
            }

            override suspend fun getTaskDetail(id: Long): ApiEnvelope<JsonObject> {
                error("unused")
            }

            override suspend fun submitTaskReview(id: Long, body: MobileReviewDecisionRequest): ApiEnvelope<JsonObject> {
                capture += body
                return ApiEnvelope(code = 200, message = "ok", data = JsonObject(), timestamp = System.currentTimeMillis())
            }

            override suspend fun uploadSampling(
                idempotencyKey: String,
                file: MultipartBody.Part,
                serialNo: RequestBody?,
                productId: RequestBody?,
                productionLineId: RequestBody?,
                networkState: RequestBody?
            ): ApiEnvelope<JsonObject> {
                error("unused")
            }

            override suspend fun getUploadStatus(idempotencyKey: String): ApiEnvelope<JsonObject> {
                error("unused")
            }

            override suspend fun getNotifications(limit: Int): ApiEnvelope<List<MobileNotificationDto>> {
                error("unused")
            }

            override suspend fun getProfile(): ApiEnvelope<MobileProfileDto> {
                error("unused")
            }

            override suspend fun getPilotMetrics(): ApiEnvelope<MobilePilotMetricsDto> {
                error("unused")
            }
        }
    }

    private class FakeReviewTaskCacheDao : ReviewTaskCacheDao {
        override fun observeAll(): Flow<List<ReviewTaskCacheEntity>> = emptyFlow()
        override suspend fun upsertAll(items: List<ReviewTaskCacheEntity>) = Unit
        override suspend fun clear() = Unit
    }

    private class FakeUploadQueueDao : UploadQueueDao {
        override fun observeQueue(): Flow<List<UploadQueueEntity>> = emptyFlow()
        override suspend fun getRetryable(maxRetries: Int): List<UploadQueueEntity> = emptyList()
        override suspend fun getFailedOrDead(): List<UploadQueueEntity> = emptyList()
        override suspend fun getDeadOnly(): List<UploadQueueEntity> = emptyList()
        override suspend fun upsert(item: UploadQueueEntity): Long = 1L
        override suspend fun update(item: UploadQueueEntity) = Unit
        override suspend fun findByKey(key: String): UploadQueueEntity? = null
        override suspend fun findById(id: Long): UploadQueueEntity? = null
        override suspend fun cleanupCompleted(before: Long): Int = 0
        override suspend fun cleanupDead(before: Long): Int = 0
    }

    private class FakeUploadOperationLogDao : UploadOperationLogDao {
        override fun observeRecent(limit: Int): Flow<List<UploadOperationLogEntity>> = emptyFlow()
        override suspend fun insert(item: UploadOperationLogEntity): Long = 1L
        override suspend fun cleanupBefore(before: Long): Int = 0
    }
}
