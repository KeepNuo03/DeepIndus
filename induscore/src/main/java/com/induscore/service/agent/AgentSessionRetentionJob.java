package com.induscore.service.agent;

import com.induscore.repository.AgentSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Agent 会话留存清理任务：
 * - 仅清理“已归档”且超过保留期的会话；
 * - 依赖外键级联自动清理消息和工具调用日志。
 */
@Component
public class AgentSessionRetentionJob {
    private static final Logger log = LoggerFactory.getLogger(AgentSessionRetentionJob.class);

    private final AgentSessionRepository agentSessionRepository;
    private final boolean enabled;
    private final int retentionDays;

    public AgentSessionRetentionJob(
            AgentSessionRepository agentSessionRepository,
            @Value("${agent.chat.retention-cleanup.enabled:false}") boolean enabled,
            @Value("${agent.chat.retention-days:180}") int retentionDays
    ) {
        this.agentSessionRepository = agentSessionRepository;
        this.enabled = enabled;
        this.retentionDays = Math.max(7, retentionDays);
    }

    @Scheduled(cron = "${agent.chat.retention-cleanup.cron:0 0 4 * * ?}")
    @Transactional
    public void cleanupArchivedSessions() {
        if (!enabled) {
            return;
        }
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        int deleted = agentSessionRepository.deleteArchivedSessionsBefore(threshold);
        if (deleted > 0) {
            log.info("agent_session_retention_cleanup deletedSessions={} threshold={}", deleted, threshold);
        }
    }
}
