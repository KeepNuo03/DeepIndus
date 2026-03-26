package com.induscore.dto.agent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * Agent 模块 DTO 聚合，便于前后端按统一契约联调。
 */
public final class AgentDtos {
    private AgentDtos() {
    }

    public record ChatMessage(
            @NotBlank(message = "role 不能为空") String role,
            @NotBlank(message = "content 不能为空") String content
    ) {
    }

    public record ChatContext(
            String scene,
            String page,
            Map<String, Object> pageParams,
            Long taskId,
            String idempotencyKey,
            String timeRange
    ) {
    }

    public record ChatOptions(
            Boolean stream,
            @Min(value = 64, message = "maxTokens 不能小于 64")
            @Max(value = 4096, message = "maxTokens 不能大于 4096")
            Integer maxTokens,
            @DecimalMin(value = "0.0", message = "temperature 不能小于 0")
            @DecimalMax(value = "1.0", message = "temperature 不能大于 1")
            Double temperature
    ) {
    }

    public record ChatRequest(
            String sessionId,
            List<@Valid ChatMessage> messages,
            @Valid ChatMessage message,
            @Valid ChatContext context,
            @Valid ChatOptions options,
            Boolean stream
    ) {
    }

    public record ToolCall(
            String tool,
            boolean success,
            long latencyMs
    ) {
    }

    public record Usage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {
    }

    public record ChatResponse(
            String sessionId,
            String answer,
            List<ToolCall> toolCalls,
            String traceId,
            Usage usage
    ) {
    }

    public record SessionSummary(
            String sessionId,
            String title,
            String lastMessageAt,
            String clientType
    ) {
    }

    public record SessionPage(
            List<SessionSummary> items,
            long total,
            int page,
            int pageSize
    ) {
    }

    public record MessageItem(
            String role,
            String content,
            String timestamp
    ) {
    }

    public record SessionDetail(
            String sessionId,
            String title,
            List<MessageItem> messages,
            long total,
            int page,
            int pageSize
    ) {
    }

    public record ArchiveResponse(
            String sessionId,
            String status
    ) {
    }

    public record DebugSession(
            String sessionId,
            String title,
            String status,
            String clientType,
            String createdAt,
            String updatedAt
    ) {
    }

    public record DebugMessage(
            Long id,
            String role,
            String content,
            Integer tokenCount,
            String traceId,
            String createdAt
    ) {
    }

    public record DebugToolCall(
            Long id,
            String traceId,
            String toolName,
            boolean success,
            String errorCode,
            Integer latencyMs,
            String inputJson,
            String outputJson,
            String createdAt
    ) {
    }

    public record DebugAudit(
            Long id,
            String traceId,
            String responseStatus,
            String requestSummary,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            String createdAt
    ) {
    }

    /**
     * 只读排查聚合视图：
     * - session: 会话主信息
     * - messages: 会话消息（按时间升序）
     * - toolCalls: 工具调用（可按 trace 过滤）
     * - audits: 审计日志（可按 trace 过滤）
     */
    public record DebugSnapshot(
            String filterSessionId,
            String filterTraceId,
            DebugSession session,
            List<DebugMessage> messages,
            List<DebugToolCall> toolCalls,
            List<DebugAudit> audits
    ) {
    }
}
