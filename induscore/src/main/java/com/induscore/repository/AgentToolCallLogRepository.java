package com.induscore.repository;

import com.induscore.model.AgentToolCallLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Agent 工具调用日志数据访问层。
 */
public interface AgentToolCallLogRepository extends JpaRepository<AgentToolCallLog, Long> {
    List<AgentToolCallLog> findByTraceIdOrderByCreatedAtAsc(String traceId);

    List<AgentToolCallLog> findTop200BySessionIdOrderByCreatedAtDesc(String sessionId);
}
