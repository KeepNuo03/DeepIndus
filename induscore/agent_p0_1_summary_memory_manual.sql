-- Agent summary memory schema patch (MySQL 8+)
-- Execute manually if your existing agent_session table was created before summary fields.

SET NAMES utf8mb4;

ALTER TABLE agent_session
    ADD COLUMN IF NOT EXISTS summary_text MEDIUMTEXT NULL AFTER status,
    ADD COLUMN IF NOT EXISTS summary_updated_at DATETIME NULL AFTER summary_text;
