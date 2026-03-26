package com.induscore.service.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.dto.agent.AgentDtos;
import com.induscore.model.AgentAuditLog;
import com.induscore.model.AgentToolCallLog;
import com.induscore.repository.AgentAuditLogRepository;
import com.induscore.repository.AgentToolCallLogRepository;
import com.induscore.security.RequestAuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 审计落库服务：
 * - 每次 chat 都写入审计主记录；
 * - 每次工具调用都写入工具调用明细；
 * - 异常路径会补写错误审计，便于排障。
 */
@Service
public class AgentAuditService {
    private static final Logger log = LoggerFactory.getLogger(AgentAuditService.class);
    private final AgentAuditLogRepository auditLogRepository;
    private final AgentToolCallLogRepository toolCallLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentAuditService(
            AgentAuditLogRepository auditLogRepository,
            AgentToolCallLogRepository toolCallLogRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.toolCallLogRepository = toolCallLogRepository;
    }

    public void logChat(RequestAuthContext context, String sessionId, String traceId, AgentDtos.ChatRequest request, AgentDtos.ChatResponse response) {
        String scene = request.context() == null ? "general" : String.valueOf(request.context().scene());
        log.info("agent_chat userId={} clientType={} sessionId={} traceId={} scene={} toolCalls={} totalTokens={}",
                context.userId(),
                context.clientType(),
                sessionId,
                traceId,
                scene,
                response.toolCalls() == null ? 0 : response.toolCalls().size(),
                response.usage() == null ? 0 : response.usage().totalTokens());

        // 审计主记录：一条对话请求对应一条主审计。
        try {
            AgentAuditLog audit = new AgentAuditLog();
            audit.setTraceId(traceId);
            audit.setSessionId(sessionId);
            audit.setUserId(context.userId());
            audit.setClientType(normalizeClientType(context.clientType()));
            audit.setRequestSummary(buildRequestSummary(request));
            audit.setResponseStatus("success");
            audit.setModelName("llm");
            if (response.usage() != null) {
                audit.setPromptTokens(response.usage().promptTokens());
                audit.setCompletionTokens(response.usage().completionTokens());
                audit.setTotalTokens(response.usage().totalTokens());
            }
            auditLogRepository.save(audit);

            if (response.toolCalls() != null) {
                String inputJson = toJsonSafely(request.context());
                for (AgentDtos.ToolCall toolCall : response.toolCalls()) {
                    // 工具调用明细：用于还原“调用了什么、是否成功、耗时多少”。
                    AgentToolCallLog logRow = new AgentToolCallLog();
                    logRow.setSessionId(sessionId);
                    logRow.setTraceId(traceId);
                    logRow.setToolName(toolCall.tool());
                    logRow.setInputJson(inputJson);
                    Map<String, Object> output = new HashMap<>();
                    output.put("success", toolCall.success());
                    output.put("latencyMs", toolCall.latencyMs());
                    logRow.setOutputJson(toJsonSafely(output));
                    logRow.setSuccess(toolCall.success());
                    logRow.setErrorCode(toolCall.success() ? null : "TOOL_CALL_FAILED");
                    logRow.setLatencyMs((int) Math.min(Integer.MAX_VALUE, toolCall.latencyMs()));
                    toolCallLogRepository.save(logRow);
                }
            }
        } catch (Exception ex) {
            log.warn("agent_audit_log_chat_failed sessionId={} traceId={}", sessionId, traceId, ex);
        }
    }

    public void logError(RequestAuthContext context, String sessionId, String traceId, String errorCode, String message) {
        log.warn("agent_error userId={} clientType={} sessionId={} traceId={} errorCode={} message={}",
                context == null ? null : context.userId(),
                context == null ? null : context.clientType(),
                sessionId,
                traceId,
                errorCode,
                message);

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            AgentAuditLog audit = new AgentAuditLog();
            audit.setTraceId(traceId);
            audit.setSessionId(sessionId);
            audit.setUserId(context == null || context.userId() == null ? -1L : context.userId());
            audit.setClientType(context == null ? "unknown" : normalizeClientType(context.clientType()));
            audit.setRequestSummary(message == null ? "error" : truncate(message, 512));
            audit.setResponseStatus(errorCode == null ? "error" : errorCode);
            auditLogRepository.save(audit);
        } catch (Exception ex) {
            log.warn("agent_audit_log_error_failed sessionId={} traceId={}", sessionId, traceId, ex);
        }
    }

    private String buildRequestSummary(AgentDtos.ChatRequest request) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            return "empty_request";
        }
        AgentDtos.ChatMessage last = request.messages().get(request.messages().size() - 1);
        return truncate(last.content(), 512);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen);
    }

    private String toJsonSafely(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    private String normalizeClientType(String clientType) {
        if (clientType == null || clientType.isBlank()) {
            return "pc";
        }
        return clientType;
    }
}
