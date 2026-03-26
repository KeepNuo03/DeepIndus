package com.induscore.repository;

import com.induscore.model.AgentMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Agent 消息数据访问层。
 */
public interface AgentMessageRepository extends JpaRepository<AgentMessage, Long> {
    List<AgentMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    Page<AgentMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId, Pageable pageable);

    Page<AgentMessage> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    long countBySessionId(String sessionId);
}
