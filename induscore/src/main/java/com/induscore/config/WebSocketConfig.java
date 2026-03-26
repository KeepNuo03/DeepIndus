package com.induscore.config;

import com.induscore.websocket.DetectionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置，提供实时检测推送通道。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DetectionWebSocketHandler detectionWebSocketHandler;

    public WebSocketConfig(DetectionWebSocketHandler detectionWebSocketHandler) {
        this.detectionWebSocketHandler = detectionWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(detectionWebSocketHandler, "/ws")
                .setAllowedOrigins("http://localhost:5173");
    }
}
