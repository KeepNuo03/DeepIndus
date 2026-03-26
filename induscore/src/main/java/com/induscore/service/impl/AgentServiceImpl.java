package com.induscore.service.impl;

import com.induscore.common.ApiException;
import com.induscore.dto.agent.AgentDtos;
import com.induscore.model.AgentAuditLog;
import com.induscore.model.AgentMessage;
import com.induscore.model.AgentSession;
import com.induscore.model.AgentToolCallLog;
import com.induscore.repository.AgentAuditLogRepository;
import com.induscore.repository.AgentMessageRepository;
import com.induscore.repository.AgentSessionRepository;
import com.induscore.repository.AgentToolCallLogRepository;
import com.induscore.security.RequestAuthContext;
import com.induscore.security.RequestAuthContextHolder;
import com.induscore.service.AgentService;
import com.induscore.service.agent.AgentAuditService;
import com.induscore.service.agent.AgentConcurrencyGuard;
import com.induscore.service.agent.AgentOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Agent 核心服务实现：
 * - 会话/消息持久化；
 * - 同步与流式统一复用 chat 主链路；
 * - 引入并发闸门、消息分页、上下文窗口等容量治理能力。
 */
@Service
public class AgentServiceImpl implements AgentService {
    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter SESSION_ID_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT).withZone(ZoneId.systemDefault());

    private final AgentOrchestrator orchestrator;
    private final AgentAuditService auditService;
    private final AgentSessionRepository agentSessionRepository;
    private final AgentMessageRepository agentMessageRepository;
    private final AgentToolCallLogRepository agentToolCallLogRepository;
    private final AgentAuditLogRepository agentAuditLogRepository;
    private final AgentConcurrencyGuard concurrencyGuard;
    private final int contextWindowMessages;
    private final int messageMaxChars;
    private final int defaultSessionDetailPageSize;
    private final int maxSessionDetailPageSize;
    private final boolean summaryEnabled;
    private final int summaryTriggerMessages;
    private final int summaryRecentKeepMessages;
    private final int summaryMaxChars;
    private final int summarySourceMaxMessages;

    public AgentServiceImpl(
            AgentOrchestrator orchestrator,
            AgentAuditService auditService,
            AgentSessionRepository agentSessionRepository,
            AgentMessageRepository agentMessageRepository,
            AgentToolCallLogRepository agentToolCallLogRepository,
            AgentAuditLogRepository agentAuditLogRepository,
            AgentConcurrencyGuard concurrencyGuard,
            @Value("${agent.chat.context-window-messages:24}") int contextWindowMessages,
            @Value("${agent.chat.max-message-chars:8000}") int messageMaxChars,
            @Value("${agent.chat.session-detail-default-page-size:50}") int defaultSessionDetailPageSize,
            @Value("${agent.chat.session-detail-max-page-size:200}") int maxSessionDetailPageSize,
            @Value("${agent.chat.summary.enabled:true}") boolean summaryEnabled,
            @Value("${agent.chat.summary.trigger-messages:30}") int summaryTriggerMessages,
            @Value("${agent.chat.summary.recent-keep-messages:12}") int summaryRecentKeepMessages,
            @Value("${agent.chat.summary.max-chars:1200}") int summaryMaxChars,
            @Value("${agent.chat.summary.source-max-messages:120}") int summarySourceMaxMessages
    ) {
        this.orchestrator = orchestrator;
        this.auditService = auditService;
        this.agentSessionRepository = agentSessionRepository;
        this.agentMessageRepository = agentMessageRepository;
        this.agentToolCallLogRepository = agentToolCallLogRepository;
        this.agentAuditLogRepository = agentAuditLogRepository;
        this.concurrencyGuard = concurrencyGuard;
        this.contextWindowMessages = Math.max(6, contextWindowMessages);
        this.messageMaxChars = Math.max(512, messageMaxChars);
        this.defaultSessionDetailPageSize = Math.max(20, defaultSessionDetailPageSize);
        this.maxSessionDetailPageSize = Math.max(this.defaultSessionDetailPageSize, maxSessionDetailPageSize);
        this.summaryEnabled = summaryEnabled;
        this.summaryTriggerMessages = Math.max(10, summaryTriggerMessages);
        this.summaryRecentKeepMessages = Math.max(6, summaryRecentKeepMessages);
        this.summaryMaxChars = Math.max(300, summaryMaxChars);
        this.summarySourceMaxMessages = Math.max(this.summaryRecentKeepMessages + 1, summarySourceMaxMessages);
    }

    @Override
    @Transactional
    public AgentDtos.ChatResponse chat(RequestAuthContext context, AgentDtos.ChatRequest request) {
        validateContext(context);
        AgentDtos.ChatRequest normalizedRequest = normalizeRequest(request);
        validateRequest(normalizedRequest);
        concurrencyGuard.acquire(context.userId());
        try {
            String traceId = buildTraceId();
            String sessionId = resolveSessionId(normalizedRequest.sessionId());
            AgentSession session = getOrCreateSession(sessionId, context);

            AgentDtos.ChatMessage userMessage = normalizedRequest.messages().get(normalizedRequest.messages().size() - 1);
            String userContent = truncateForStorage(userMessage.content());
            appendMessage(session.getSessionId(), userMessage.role(), userContent, null, null);

            // 模型上下文窗口仅保留最近 N 条，避免长会话拖垮 token 与延迟。
            AgentDtos.ChatRequest modelRequest = buildModelContextRequest(normalizedRequest, session);

            AgentOrchestrator.AgentDraft draft;
            RequestAuthContextHolder.set(context);
            try {
                draft = orchestrator.run(context, modelRequest);
            } finally {
                RequestAuthContextHolder.clear();
            }
            appendMessage(
                    session.getSessionId(),
                    "assistant",
                    truncateForStorage(draft.answer()),
                    draft.usage() == null ? null : draft.usage().completionTokens(),
                    traceId
            );
            session.setUpdatedAt(LocalDateTime.now());
            // 会话标题自动生成：
            // 1) 新会话默认 title 为空，不预填“新会话”；
            // 2) 若历史脏数据仍是“新会话”，也允许覆盖为自动标题。
            if (shouldGenerateTitle(session.getTitle())) {
                session.setTitle(extractTitle(userContent));
            }
            refreshSessionSummary(session);
            agentSessionRepository.save(session);

            AgentDtos.ChatResponse response = new AgentDtos.ChatResponse(
                    session.getSessionId(),
                    draft.answer(),
                    draft.toolCalls(),
                    traceId,
                    draft.usage()
            );
            auditService.logChat(context, session.getSessionId(), traceId, normalizedRequest, response);
            return response;
        } finally {
            concurrencyGuard.release(context.userId());
        }
    }

    @Override
    /**
     * SSE 只是输出通道，核心业务仍复用 chat()，避免出现两套逻辑漂移。
     */
    public void streamChat(RequestAuthContext context, AgentDtos.ChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            RequestAuthContextHolder.set(context);
            try {
                AgentDtos.ChatResponse response = chat(context, request);

                Map<String, Object> meta = new HashMap<>();
                meta.put("sessionId", response.sessionId());
                meta.put("traceId", response.traceId());
                sendEvent(emitter, "meta", meta);

                if (response.toolCalls() != null) {
                    for (AgentDtos.ToolCall toolCall : response.toolCalls()) {
                        Map<String, Object> toolStart = new HashMap<>();
                        toolStart.put("phase", "end");
                        toolStart.put("tool", toolCall.tool());
                        toolStart.put("success", toolCall.success());
                        toolStart.put("latencyMs", toolCall.latencyMs());
                        sendEvent(emitter, "tool", toolStart);
                    }
                }

                for (String chunk : splitChunks(response.answer(), 24)) {
                    Map<String, Object> chunkPayload = new HashMap<>();
                    chunkPayload.put("text", chunk);
                    sendEvent(emitter, "chunk", chunkPayload);
                }

                Map<String, Object> donePayload = new HashMap<>();
                donePayload.put("status", "ok");
                donePayload.put("usage", response.usage());
                sendEvent(emitter, "done", donePayload);
                emitter.complete();
            } catch (ApiException ex) {
                sendErrorAndComplete(emitter, mapAgentErrorCode(ex), ex.getMessage());
                try {
                    auditService.logError(context, normalizeNullable(request == null ? null : request.sessionId()), buildTraceId(), "AGENT_REQUEST_ERROR", ex.getMessage());
                } catch (Exception auditEx) {
                    log.warn("agent_stream_audit_error_failed", auditEx);
                }
            } catch (Exception ex) {
                log.error("agent_stream_unhandled_exception", ex);
                sendErrorAndComplete(emitter, "AGENT_INTERNAL_ERROR", "Agent 服务暂时不可用，请稍后重试");
                try {
                    auditService.logError(context, normalizeNullable(request == null ? null : request.sessionId()), buildTraceId(), "AGENT_INTERNAL_ERROR", ex.getMessage());
                } catch (Exception auditEx) {
                    log.warn("agent_stream_audit_error_failed", auditEx);
                }
            } finally {
                RequestAuthContextHolder.clear();
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AgentDtos.SessionPage listSessions(RequestAuthContext context, int page, int pageSize) {
        validateContext(context);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        // 会话列表仅展示 active，会话删除（archive）后不会再出现在侧边栏。
        Page<AgentSession> pageResult = agentSessionRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(
                context.userId(),
                "active",
                pageable
        );

        List<AgentDtos.SessionSummary> items = pageResult.getContent().stream()
                .map(s -> new AgentDtos.SessionSummary(
                        s.getSessionId(),
                        (s.getTitle() == null || s.getTitle().isBlank()) ? "新会话" : s.getTitle(),
                        TIME_FORMATTER.format((s.getUpdatedAt() == null ? LocalDateTime.now() : s.getUpdatedAt()).atZone(ZoneId.systemDefault()).toInstant()),
                        (s.getClientType() == null || s.getClientType().isBlank()) ? "pc" : s.getClientType()
                ))
                .collect(Collectors.toList());

        return new AgentDtos.SessionPage(items, pageResult.getTotalElements(), safePage, safePageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentDtos.SessionDetail getSessionDetail(RequestAuthContext context, String sessionId, int page, int pageSize) {
        validateContext(context);
        // 会话详情强制分页，避免长会话一次性拉全量消息导致慢查询和大响应体。
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), maxSessionDetailPageSize);
        if (pageSize <= 0) {
            safePageSize = defaultSessionDetailPageSize;
        }
        AgentSession session = requireSessionOwnedByUser(sessionId, context.userId());
        PageRequest pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<AgentMessage> pageResult = agentMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId(), pageable);
        List<AgentDtos.MessageItem> messages = pageResult.getContent()
                .stream()
                .map(m -> new AgentDtos.MessageItem(
                        m.getRole(),
                        m.getContent(),
                        TIME_FORMATTER.format((m.getCreatedAt() == null ? LocalDateTime.now() : m.getCreatedAt()).atZone(ZoneId.systemDefault()).toInstant())
                ))
                .toList();
        return new AgentDtos.SessionDetail(
                session.getSessionId(),
                session.getTitle(),
                messages,
                pageResult.getTotalElements(),
                safePage,
                safePageSize
        );
    }

    @Override
    @Transactional
    public AgentDtos.ArchiveResponse archiveSession(RequestAuthContext context, String sessionId) {
        validateContext(context);
        AgentSession session = requireSessionOwnedByUser(sessionId, context.userId());
        session.setStatus("archived");
        session.setUpdatedAt(LocalDateTime.now());
        agentSessionRepository.save(session);
        return new AgentDtos.ArchiveResponse(session.getSessionId(), session.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public AgentDtos.DebugSnapshot debugSnapshot(RequestAuthContext context, String sessionId, String traceId) {
        validateContext(context);
        String safeSessionId = normalizeNullable(sessionId);
        String safeTraceId = normalizeNullable(traceId);
        if (safeSessionId == null && safeTraceId == null) {
            throw new ApiException(400, "sessionId 和 traceId 不能同时为空");
        }

        if (safeSessionId == null) {
            AgentAuditLog traceAudit = agentAuditLogRepository.findByTraceId(safeTraceId)
                    .orElseThrow(() -> new ApiException(404, "traceId 对应审计记录不存在"));
            safeSessionId = traceAudit.getSessionId();
        }

        AgentSession session = requireSessionOwnedByUser(safeSessionId, context.userId());
        List<AgentMessage> messages = agentMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId());

        List<AgentToolCallLog> toolCalls = safeTraceId == null
                ? agentToolCallLogRepository.findTop200BySessionIdOrderByCreatedAtDesc(session.getSessionId())
                : agentToolCallLogRepository.findByTraceIdOrderByCreatedAtAsc(safeTraceId);

        List<AgentAuditLog> audits;
        if (safeTraceId == null) {
            audits = agentAuditLogRepository.findTop100BySessionIdOrderByCreatedAtDesc(session.getSessionId());
        } else {
            audits = agentAuditLogRepository.findByTraceId(safeTraceId).map(List::of).orElseGet(List::of);
        }

        return new AgentDtos.DebugSnapshot(
                safeSessionId,
                safeTraceId,
                new AgentDtos.DebugSession(
                        session.getSessionId(),
                        session.getTitle(),
                        session.getStatus(),
                        session.getClientType(),
                        formatTime(session.getCreatedAt()),
                        formatTime(session.getUpdatedAt())
                ),
                messages.stream()
                        .map(m -> new AgentDtos.DebugMessage(
                                m.getId(),
                                m.getRole(),
                                m.getContent(),
                                m.getTokenCount(),
                                m.getTraceId(),
                                formatTime(m.getCreatedAt())
                        ))
                        .toList(),
                toolCalls.stream()
                        .map(t -> new AgentDtos.DebugToolCall(
                                t.getId(),
                                t.getTraceId(),
                                t.getToolName(),
                                Boolean.TRUE.equals(t.getSuccess()),
                                t.getErrorCode(),
                                t.getLatencyMs(),
                                t.getInputJson(),
                                t.getOutputJson(),
                                formatTime(t.getCreatedAt())
                        ))
                        .toList(),
                audits.stream()
                        .map(a -> new AgentDtos.DebugAudit(
                                a.getId(),
                                a.getTraceId(),
                                a.getResponseStatus(),
                                a.getRequestSummary(),
                                a.getModelName(),
                                a.getPromptTokens(),
                                a.getCompletionTokens(),
                                a.getTotalTokens(),
                                formatTime(a.getCreatedAt())
                        ))
                        .toList()
        );
    }

    private void validateContext(RequestAuthContext context) {
        if (context == null || context.userId() == null) {
            throw new ApiException(401, "未登录或登录已失效");
        }
    }

    private void validateRequest(AgentDtos.ChatRequest request) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            throw new ApiException(400, "messages 不能为空");
        }
        AgentDtos.ChatMessage last = request.messages().get(request.messages().size() - 1);
        if (last.role() == null || !"user".equalsIgnoreCase(last.role())) {
            throw new ApiException(400, "最后一条消息必须是 user 角色");
        }
        if (request.context() != null && request.context().scene() != null) {
            String scene = request.context().scene().toLowerCase(Locale.ROOT);
            if (!SetHolder.ALLOWED_SCENES.contains(scene) && !scene.startsWith("web_")) {
                throw new ApiException(400, "不支持的 scene: " + request.context().scene());
            }
        }
    }

    private AgentDtos.ChatRequest normalizeRequest(AgentDtos.ChatRequest request) {
        if (request == null) {
            throw new ApiException(400, "请求体不能为空");
        }
        List<AgentDtos.ChatMessage> normalizedMessages = request.messages();
        if (normalizedMessages == null || normalizedMessages.isEmpty()) {
            if (request.message() != null) {
                normalizedMessages = List.of(request.message());
            }
        }
        return new AgentDtos.ChatRequest(
                request.sessionId(),
                normalizedMessages,
                request.message(),
                request.context(),
                request.options(),
                request.stream()
        );
    }

    private AgentSession getOrCreateSession(String sessionId, RequestAuthContext context) {
        AgentSession state = agentSessionRepository.findBySessionId(sessionId).orElseGet(() -> {
            AgentSession created = new AgentSession();
            created.setSessionId(sessionId);
            created.setUserId(context.userId());
            created.setClientType(normalizeClientType(context.clientType()));
            // 仅在列表展示时兜底显示“新会话”，数据库内先留空，
            // 避免阻止后续自动标题生成。
            created.setTitle(null);
            created.setStatus("active");
            created.setCreatedAt(LocalDateTime.now());
            created.setUpdatedAt(LocalDateTime.now());
            return created;
        });
        if (!Objects.equals(state.getUserId(), context.userId())) {
            throw new ApiException(403, "无权限访问该会话");
        }
        return agentSessionRepository.save(state);
    }

    private AgentSession requireSessionOwnedByUser(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(400, "sessionId 不能为空");
        }
        return agentSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(404, "会话不存在"));
    }

    private void appendMessage(String sessionId, String role, String content, Integer tokenCount, String traceId) {
        AgentMessage message = new AgentMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(truncateForStorage(content));
        message.setTokenCount(tokenCount);
        message.setTraceId(traceId);
        message.setCreatedAt(LocalDateTime.now());
        agentMessageRepository.save(message);
    }

    private String resolveSessionId(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId;
        }
        return "ag_s_" + SESSION_ID_TIME.format(Instant.now()) + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String buildTraceId() {
        return "tr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String normalizeClientType(String clientType) {
        if (clientType == null || clientType.isBlank()) {
            return "pc";
        }
        return clientType.trim().toLowerCase(Locale.ROOT);
    }

    private String extractTitle(String content) {
        if (content == null || content.isBlank()) {
            return "新会话";
        }
        String normalized = content.trim().replaceAll("\\s+", " ");
        // 常见口语前缀清理，让标题更像主流大模型的自动摘要标题。
        normalized = normalized
                .replaceFirst("^(请|麻烦|帮我|请帮我|我想|我需要)", "")
                .replaceFirst("^(分析|总结|看下|看一下|解释一下)", "")
                .trim();
        if (normalized.isBlank()) {
            normalized = content.trim().replaceAll("\\s+", " ");
        }
        return normalized.length() <= 24 ? normalized : normalized.substring(0, 24) + "...";
    }

    private boolean shouldGenerateTitle(String title) {
        if (title == null || title.isBlank()) {
            return true;
        }
        String normalized = title.trim();
        return "新会话".equals(normalized) || "new chat".equalsIgnoreCase(normalized);
    }

    private String truncateForStorage(String content) {
        if (content == null) {
            return "";
        }
        if (content.length() <= messageMaxChars) {
            return content;
        }
        // 统一在服务层做兜底截断，防止异常长文本持续推高存储和查询成本。
        return content.substring(0, messageMaxChars);
    }

    private AgentDtos.ChatRequest buildModelContextRequest(AgentDtos.ChatRequest request, AgentSession session) {
        String sessionId = session.getSessionId();
        PageRequest pageable = PageRequest.of(0, contextWindowMessages, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AgentMessage> latest = agentMessageRepository
                .findBySessionIdOrderByCreatedAtDesc(sessionId, pageable)
                .getContent();
        if (latest.isEmpty()) {
            return request;
        }
        List<AgentDtos.ChatMessage> contextMessages = latest.stream()
                .map(m -> new AgentDtos.ChatMessage(m.getRole(), m.getContent()))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(contextMessages);
        if (summaryEnabled && session.getSummaryText() != null && !session.getSummaryText().isBlank()) {
            contextMessages.add(0, new AgentDtos.ChatMessage(
                    "system",
                    "历史会话摘要（系统自动维护）:\n" + session.getSummaryText()
            ));
        }
        return new AgentDtos.ChatRequest(
                request.sessionId(),
                contextMessages,
                request.message(),
                request.context(),
                request.options(),
                request.stream()
        );
    }

    /**
     * 最小摘要记忆实现：
     * - 当会话消息超过阈值时，抽取“旧消息窗口”生成摘要；
     * - 近期消息仍走原始窗口，摘要用于补充更早历史语义。
     */
    private void refreshSessionSummary(AgentSession session) {
        if (!summaryEnabled) {
            return;
        }
        long total = agentMessageRepository.countBySessionId(session.getSessionId());
        if (total <= summaryTriggerMessages) {
            return;
        }
        int olderCount = (int) Math.max(0, total - summaryRecentKeepMessages);
        int fetchSize = Math.min(olderCount, summarySourceMaxMessages);
        if (fetchSize <= 0) {
            return;
        }
        PageRequest pageable = PageRequest.of(0, fetchSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<AgentMessage> olderMessages = agentMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(session.getSessionId(), pageable)
                .getContent();
        String summary = buildHeuristicSummary(olderMessages);
        if (summary == null || summary.isBlank()) {
            return;
        }
        session.setSummaryText(summary);
        session.setSummaryUpdatedAt(LocalDateTime.now());
    }

    /**
     * 摘要生成（最小版）：
     * - 仅抽取用户提问与助手结论片段；
     * - 目标是稳定、低成本，不依赖额外模型调用。
     */
    private String buildHeuristicSummary(List<AgentMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        List<String> points = new ArrayList<>();
        for (AgentMessage message : messages) {
            if (message.getContent() == null || message.getContent().isBlank()) {
                continue;
            }
            String role = message.getRole() == null ? "unknown" : message.getRole().toLowerCase(Locale.ROOT);
            String content = message.getContent().trim().replaceAll("\\s+", " ");
            if (content.length() > 120) {
                content = content.substring(0, 120) + "...";
            }
            if ("user".equals(role)) {
                points.add("用户关注：" + content);
            } else if ("assistant".equals(role)) {
                points.add("助手结论：" + content);
            }
            if (points.size() >= 16) {
                break;
            }
        }
        if (points.isEmpty()) {
            return null;
        }
        String summary = String.join(" | ", points);
        if (summary.length() > summaryMaxChars) {
            summary = summary.substring(0, summaryMaxChars);
        }
        return summary;
    }

    private String formatTime(LocalDateTime dateTime) {
        return TIME_FORMATTER.format((dateTime == null ? LocalDateTime.now() : dateTime).atZone(ZoneId.systemDefault()).toInstant());
    }

    private String normalizeNullable(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private List<String> splitChunks(String text, int chunkSize) {
        if (text == null || text.isBlank()) {
            return List.of("");
        }
        int size = Math.max(chunkSize, 1);
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += size) {
            chunks.add(text.substring(i, Math.min(i + size, text.length())));
        }
        return chunks;
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(data));
    }

    private void sendErrorAndComplete(SseEmitter emitter, String errorCode, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("code", errorCode);
            payload.put("message", message);
            sendEvent(emitter, "error", payload);
            // Keep the SSE contract symmetric: always emit done to let client clear loading state.
            Map<String, Object> donePayload = new HashMap<>();
            donePayload.put("status", "error");
            donePayload.put("usage", null);
            sendEvent(emitter, "done", donePayload);
            emitter.complete();
        } catch (IOException ioException) {
            emitter.completeWithError(ioException);
        }
    }

    private String mapAgentErrorCode(ApiException ex) {
        int code = ex.getCode();
        return switch (code) {
            case 400 -> "AGENT_INVALID_ARGUMENT";
            case 401 -> "UNAUTHORIZED";
            case 402 -> "AGENT_QUOTA_EXCEEDED";
            case 403 -> "AGENT_NO_PERMISSION";
            case 404 -> "AGENT_SESSION_NOT_FOUND";
            case 429 -> "AGENT_RATE_LIMITED";
            case 504 -> "AGENT_MODEL_TIMEOUT";
            case 529 -> "AGENT_MODEL_OVERLOADED";
            case 503 -> "AGENT_UPSTREAM_UNAVAILABLE";
            default -> "AGENT_INTERNAL_ERROR";
        };
    }

    private static final class SetHolder {
        private static final java.util.Set<String> ALLOWED_SCENES = java.util.Set.of(
                "home",
                "workbench",
                "detection_records",
                "review_list",
                "review_detail",
                "upload",
                "upload_status",
                "notifications",
                "metrics",
                "dashboard",
                "production_line",
                "line_overview",
                "model_mgmt",
                "model_management",
                "general"
        );
    }
}
