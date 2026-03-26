package com.induscore.repository;

import com.induscore.model.AgentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Agent 审计主日志数据访问层。
 */
public interface AgentAuditLogRepository extends JpaRepository<AgentAuditLog, Long> {
    Optional<AgentAuditLog> findByTraceId(String traceId);

    List<AgentAuditLog> findTop100BySessionIdOrderByCreatedAtDesc(String sessionId);
}
