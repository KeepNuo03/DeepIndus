package com.induscore.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 实时检测 WebSocket 处理器。
 *
 * 仅用于建立连接和维护会话，业务推送由 DetectionWebSocketHub 统一广播。
 */
@Component
public class DetectionWebSocketHandler extends TextWebSocketHandler {

    private final DetectionWebSocketHub hub;

    public DetectionWebSocketHandler(DetectionWebSocketHub hub) {
        this.hub = hub;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        hub.register(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        hub.unregister(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 目前不处理客户端消息，预留心跳或控制指令
    }
}
