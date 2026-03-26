package com.induscore.mobile.data.remote.dto

data class AgentChatMessage(
    val role: String,
    val content: String
)

data class AgentChatContext(
    val scene: String? = null,
    val page: String? = null,
    val pageParams: Map<String, Any?>? = null,
    val taskId: Long? = null,
    val idempotencyKey: String? = null,
    val timeRange: String? = null
)

data class AgentChatOptions(
    val stream: Boolean? = false,
    val maxTokens: Int? = null,
    val temperature: Double? = null
)

data class AgentChatRequest(
    val sessionId: String? = null,
    val messages: List<AgentChatMessage> = emptyList(),
    val message: AgentChatMessage,
    val context: AgentChatContext? = null,
    val options: AgentChatOptions? = null,
    val stream: Boolean? = false
)

data class AgentToolCall(
    val tool: String? = null,
    val success: Boolean = true,
    val latencyMs: Long = 0
)

data class AgentUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

data class AgentChatResponse(
    val sessionId: String,
    val answer: String,
    val toolCalls: List<AgentToolCall> = emptyList(),
    val traceId: String? = null,
    val usage: AgentUsage? = null
)

data class AgentSessionSummaryDto(
    val sessionId: String,
    val title: String? = null,
    val lastMessageAt: String? = null,
    val clientType: String? = null
)

data class AgentSessionPageDto(
    val items: List<AgentSessionSummaryDto> = emptyList(),
    val total: Long = 0,
    val page: Int = 1,
    val pageSize: Int = 20
)

data class AgentMessageItemDto(
    val role: String,
    val content: String,
    val timestamp: String? = null
)

data class AgentSessionDetailDto(
    val sessionId: String,
    val title: String? = null,
    val messages: List<AgentMessageItemDto> = emptyList(),
    val total: Long = 0,
    val page: Int = 1,
    val pageSize: Int = 50
)

data class AgentArchiveResponseDto(
    val sessionId: String,
    val status: String
)
