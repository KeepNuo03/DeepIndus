-- Flyway 修复脚本
-- 在 MySQL 中执行此脚本来修复迁移失败问题

USE induscore;

-- 查看当前的 Flyway 迁移历史
SELECT version, description, type, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_on;

-- 删除失败的 V3.1 迁移记录（如果存在）
DELETE FROM flyway_schema_history WHERE version = '3.1';

-- 如果 V3 的表结构已经创建但记录有问题，也可以删除 V3 记录重新执行
-- DELETE FROM flyway_schema_history WHERE version = '3';

-- 查看清理后的结果
SELECT version, description, success 
FROM flyway_schema_history 
ORDER BY installed_on;
