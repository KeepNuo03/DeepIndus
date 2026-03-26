package com.induscore.mobile.data.remote

import com.google.gson.JsonObject
import com.induscore.mobile.data.remote.dto.ApiEnvelope
import com.induscore.mobile.data.remote.dto.AgentArchiveResponseDto
import com.induscore.mobile.data.remote.dto.AgentChatRequest
import com.induscore.mobile.data.remote.dto.AgentChatResponse
import com.induscore.mobile.data.remote.dto.AgentSessionDetailDto
import com.induscore.mobile.data.remote.dto.AgentSessionPageDto
import com.induscore.mobile.data.remote.dto.LoginPayload
import com.induscore.mobile.data.remote.dto.LoginRequest
import com.induscore.mobile.data.remote.dto.MobileNotificationDto
import com.induscore.mobile.data.remote.dto.MobilePilotMetricsDto
import com.induscore.mobile.data.remote.dto.MobileProfileDto
import com.induscore.mobile.data.remote.dto.MobileReviewDecisionRequest
import com.induscore.mobile.data.remote.dto.MobileReviewListDto
import com.induscore.mobile.data.remote.dto.MobileWorkbenchDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE
import retrofit2.http.Streaming

interface MobileApiService {
    @POST("v1/auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope<LoginPayload>

    @GET("v1/mobile/workbench")
    suspend fun getWorkbench(): ApiEnvelope<MobileWorkbenchDto>

    @GET("v1/mobile/tasks/review")
    suspend fun getReviewTasks(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String = "all"
    ): ApiEnvelope<MobileReviewListDto>

    @GET("v1/mobile/tasks/review/{id}")
    suspend fun getTaskDetail(@Path("id") id: Long): ApiEnvelope<JsonObject>

    @POST("v1/mobile/tasks/{id}/review")
    suspend fun submitTaskReview(
        @Path("id") id: Long,
        @Body body: MobileReviewDecisionRequest
    ): ApiEnvelope<JsonObject>

    @Multipart
    @POST("v1/mobile/uploads/sampling")
    suspend fun uploadSampling(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Part file: MultipartBody.Part,
        @Part("serialNo") serialNo: RequestBody?,
        @Part("productId") productId: RequestBody?,
        @Part("productionLineId") productionLineId: RequestBody?,
        @Part("networkState") networkState: RequestBody?
    ): ApiEnvelope<JsonObject>

    @GET("v1/mobile/uploads/{idempotencyKey}")
    suspend fun getUploadStatus(
        @Path("idempotencyKey") idempotencyKey: String
    ): ApiEnvelope<JsonObject>

    @GET("v1/mobile/notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 10
    ): ApiEnvelope<List<MobileNotificationDto>>

    @GET("v1/mobile/profile")
    suspend fun getProfile(): ApiEnvelope<MobileProfileDto>

    @GET("v1/mobile/pilot/metrics")
    suspend fun getPilotMetrics(): ApiEnvelope<MobilePilotMetricsDto>

    @POST("v1/agent/chat")
    suspend fun chatWithAgent(@Body body: AgentChatRequest): ApiEnvelope<AgentChatResponse>

    @Streaming
    @POST("v1/agent/chat/stream")
    suspend fun streamChatWithAgent(@Body body: AgentChatRequest): Response<ResponseBody>

    @GET("v1/agent/sessions")
    suspend fun listAgentSessions(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiEnvelope<AgentSessionPageDto>

    @GET("v1/agent/sessions/{sessionId}")
    suspend fun getAgentSessionDetail(
        @Path("sessionId") sessionId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): ApiEnvelope<AgentSessionDetailDto>

    @DELETE("v1/agent/sessions/{sessionId}")
    suspend fun archiveAgentSession(
        @Path("sessionId") sessionId: String
    ): ApiEnvelope<AgentArchiveResponseDto>
}
