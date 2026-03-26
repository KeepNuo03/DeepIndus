package com.induscore.service;

import com.induscore.dto.agent.AgentDtos;
import com.induscore.security.RequestAuthContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AgentService {
    /**
     * 同步对话：一次请求返回完整答案。
     */
    AgentDtos.ChatResponse chat(RequestAuthContext context, AgentDtos.ChatRequest request);

    /**
     * 流式对话：通过 SSE 逐段返回模型输出。
     */
    void streamChat(RequestAuthContext context, AgentDtos.ChatRequest request, SseEmitter emitter);

    /**
     * 查询当前用户会话列表。
     */
    AgentDtos.SessionPage listSessions(RequestAuthContext context, int page, int pageSize);

    /**
     * 查询单个会话详情（仅当前用户）。
     */
    AgentDtos.SessionDetail getSessionDetail(RequestAuthContext context, String sessionId, int page, int pageSize);

    /**
     * 归档会话（软删除语义）。
     */
    AgentDtos.ArchiveResponse archiveSession(RequestAuthContext context, String sessionId);

    /**
     * 只读排查接口：按 sessionId / traceId 聚合 Agent 关键落库数据。
     */
    AgentDtos.DebugSnapshot debugSnapshot(RequestAuthContext context, String sessionId, String traceId);
}
