-- Agent P0 manual schema script (MySQL 8+)
-- Execute manually in terminal. Not managed by Flyway.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS agent_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    client_type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    summary_text MEDIUMTEXT NULL,
    summary_updated_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_session_session_id (session_id),
    KEY idx_agent_session_user_updated (user_id, updated_at),
    KEY idx_agent_session_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agent_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    message_role VARCHAR(16) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    token_count INT NULL,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_agent_message_session_created (session_id, created_at),
    KEY idx_agent_message_trace (trace_id),
    CONSTRAINT fk_agent_message_session
        FOREIGN KEY (session_id) REFERENCES agent_session (session_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agent_tool_call_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    tool_name VARCHAR(64) NOT NULL,
    input_json LONGTEXT NULL,
    output_json LONGTEXT NULL,
    success TINYINT(1) NOT NULL,
    error_code VARCHAR(64) NULL,
    latency_ms INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_agent_tool_call_session_created (session_id, created_at),
    KEY idx_agent_tool_call_trace (trace_id),
    KEY idx_agent_tool_call_tool (tool_name),
    CONSTRAINT fk_agent_tool_call_session
        FOREIGN KEY (session_id) REFERENCES agent_session (session_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agent_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trace_id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    client_type VARCHAR(32) NOT NULL,
    request_summary VARCHAR(512) NULL,
    response_status VARCHAR(32) NOT NULL,
    model_name VARCHAR(64) NULL,
    model_latency_ms INT NULL,
    prompt_tokens INT NULL,
    completion_tokens INT NULL,
    total_tokens INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_audit_trace (trace_id),
    KEY idx_agent_audit_session_created (session_id, created_at),
    KEY idx_agent_audit_user_created (user_id, created_at),
    KEY idx_agent_audit_status (response_status),
    CONSTRAINT fk_agent_audit_session
        FOREIGN KEY (session_id) REFERENCES agent_session (session_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
