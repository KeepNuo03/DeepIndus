-- V6: 修复 users.online_status 列类型，确保与实体映射一致
ALTER TABLE users
MODIFY COLUMN online_status TINYINT DEFAULT 0 COMMENT '在线状态：0离线 1在线';
