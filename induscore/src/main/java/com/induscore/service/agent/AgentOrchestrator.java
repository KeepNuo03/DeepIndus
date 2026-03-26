package com.induscore.service.agent;

import com.induscore.dto.agent.AgentDtos;
import com.induscore.security.RequestAuthContext;

import java.util.List;

/**
 * Agent 编排抽象。
 * 当前为骨架接口，后续由 LangChain4j 实现替换。
 */
public interface AgentOrchestrator {

    AgentDraft run(RequestAuthContext context, AgentDtos.ChatRequest request);

    record AgentDraft(
            String answer,
            List<AgentDtos.ToolCall> toolCalls,
            AgentDtos.Usage usage
    ) {
    }
}
