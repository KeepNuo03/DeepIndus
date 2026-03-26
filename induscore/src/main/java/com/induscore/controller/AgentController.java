package com.induscore.controller;

import com.induscore.common.ApiException;
import com.induscore.common.ApiResponse;
import com.induscore.dto.agent.AgentDtos;
import com.induscore.security.RequestAuthContext;
import com.induscore.security.RequestAuthContextHolder;
import com.induscore.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 双端共用 Agent 接口。
 */
@RestController
@RequestMapping("/v1/agent")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 同步对话接口。
     */
    @PostMapping("/chat")
    public ApiResponse<AgentDtos.ChatResponse> chat(@Valid @RequestBody AgentDtos.ChatRequest request) {
        return ApiResponse.success(agentService.chat(requiredContext(), request));
    }

    /**
     * 流式对话接口（SSE）。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody AgentDtos.ChatRequest request) {
        SseEmitter emitter = new SseEmitter(30_000L);
        agentService.streamChat(requiredContext(), request, emitter);
        return emitter;
    }

    /**
     * 会话分页列表。
     */
    @GetMapping("/sessions")
    public ApiResponse<AgentDtos.SessionPage> listSessions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        int safePage = page == null ? 1 : page;
        int safePageSize = pageSize == null ? 20 : pageSize;
        return ApiResponse.success(agentService.listSessions(requiredContext(), safePage, safePageSize));
    }

    /**
     * 会话详情。
     */
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<AgentDtos.SessionDetail> getSessionDetail(
            @PathVariable String sessionId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        int safePage = page == null ? 1 : page;
        int safePageSize = pageSize == null ? 50 : pageSize;
        return ApiResponse.success(agentService.getSessionDetail(requiredContext(), sessionId, safePage, safePageSize));
    }

    /**
     * 会话归档（只修改状态，不做物理删除）。
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<AgentDtos.ArchiveResponse> archiveSession(@PathVariable String sessionId) {
        return ApiResponse.success(agentService.archiveSession(requiredContext(), sessionId));
    }

    /**
     * P0.1 只读排查接口：
     * - 支持 sessionId 或 traceId 任一条件；
     * - 返回会话、消息、工具调用、审计日志的聚合视图；
     * - 严格按当前登录用户做数据隔离。
     */
    @GetMapping("/debug/snapshot")
    public ApiResponse<AgentDtos.DebugSnapshot> debugSnapshot(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String traceId
    ) {
        return ApiResponse.success(agentService.debugSnapshot(requiredContext(), sessionId, traceId));
    }

    private RequestAuthContext requiredContext() {
        RequestAuthContext context = RequestAuthContextHolder.get()
                .orElseThrow(() -> new ApiException(401, "未登录或登录已失效"));
        if (context.userId() == null) {
            throw new ApiException(401, "未登录或登录已失效");
        }
        return context;
    }
}
