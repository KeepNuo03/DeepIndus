package com.induscore.service.agent.model;

import com.induscore.dto.agent.AgentDtos;
import com.induscore.security.RequestAuthContext;

public interface ModelGateway {
    String generateReply(RequestAuthContext context, AgentDtos.ChatRequest request, String prompt);
}
