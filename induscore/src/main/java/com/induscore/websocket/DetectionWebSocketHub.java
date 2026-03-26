package com.induscore.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.induscore.common.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话中心，用于广播实时检测消息。
 */
@Component
public class DetectionWebSocketHub {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public DetectionWebSocketHub(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 注册会话。
     */
    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    /**
     * 注销会话。
     */
    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }

    /**
     * 广播消息（前端期望结构：{type, data}）。
     */
    public void broadcast(String type, Map<String, Object> data) {
        Map<String, Object> payload = Map.of("type", type, "data", data);
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException ex) {
            throw new ApiException(500, "WebSocket 推送失败");
        }
    }
}
