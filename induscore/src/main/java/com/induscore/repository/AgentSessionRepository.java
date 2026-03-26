package com.induscore.repository;

import com.induscore.model.AgentSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Agent 会话数据访问层。
 */
public interface AgentSessionRepository extends JpaRepository<AgentSession, Long> {
    Optional<AgentSession> findBySessionId(String sessionId);

    Optional<AgentSession> findBySessionIdAndUserId(String sessionId, Long userId);

    Page<AgentSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    /**
     * 仅查询指定状态的会话（用于会话列表隐藏 archived）。
     */
    Page<AgentSession> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status, Pageable pageable);

    @Modifying
    @Query("delete from AgentSession s where s.status = 'archived' and s.updatedAt < :threshold")
    int deleteArchivedSessionsBefore(@Param("threshold") LocalDateTime threshold);
}
