package com.induscore.service.agent;

import com.induscore.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户级并发闸门：
 * - 控制同一用户同时进行中的 Agent 请求数；
 * - 防止流式请求叠加导致上游模型被瞬时打爆。
 */
@Component
public class AgentConcurrencyGuard {
    private final int maxConcurrentPerUser;
    private final ConcurrentHashMap<Long, AtomicInteger> inFlightByUser = new ConcurrentHashMap<>();

    public AgentConcurrencyGuard(
            @Value("${agent.chat.max-concurrent-per-user:1}") int maxConcurrentPerUser
    ) {
        this.maxConcurrentPerUser = Math.max(1, maxConcurrentPerUser);
    }

    public void acquire(Long userId) {
        AtomicInteger counter = inFlightByUser.computeIfAbsent(userId, key -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        if (current > maxConcurrentPerUser) {
            counter.decrementAndGet();
            throw new ApiException(429, "当前账号并发请求过多，请等待上一条对话完成后重试");
        }
    }

    public void release(Long userId) {
        AtomicInteger counter = inFlightByUser.get(userId);
        if (counter == null) {
            return;
        }
        int left = counter.decrementAndGet();
        if (left <= 0) {
            inFlightByUser.remove(userId, counter);
        }
    }
}
