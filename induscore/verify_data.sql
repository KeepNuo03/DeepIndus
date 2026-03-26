-- 验证数据库数据是否插入成功
-- 在 MySQL 中执行此脚本

USE induscore;

-- 1. 查看热轧带钢产品
SELECT '【1. 产品表】热轧带钢产品' as check_item;
SELECT id, name, model, category, status, created_at 
FROM products 
WHERE model = 'INDUS-HR-STRIP-001';

-- 2. 查看热轧带钢的缺陷配置
SELECT '【2. 缺陷配置】热轧带钢的检测类型' as check_item;
SELECT p.name as product_name, pdc.name as defect_name, pdc.threshold, pdc.enabled
FROM product_defect_configs pdc
JOIN products p ON pdc.product_id = p.id
WHERE p.model = 'INDUS-HR-STRIP-001';

-- 3. 查看产品与生产线的关联
SELECT '【3. 产品-生产线关联】' as check_item;
SELECT p.name as product_name, p.model, pl.name as line_name, plr.is_primary, plr.priority
FROM product_line_relations plr
JOIN products p ON plr.product_id = p.id
JOIN production_lines pl ON plr.production_line_id = pl.id
WHERE p.model = 'INDUS-HR-STRIP-001';

-- 4. 查看检测记录表的新字段
SELECT '【4. 检测记录表结构】确认新字段存在' as check_item;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'induscore'
  AND TABLE_NAME = 'detection_records'
  AND COLUMN_NAME IN ('product_id', 'production_line_id');

-- 5. 查看所有产品及关联的生产线数量
SELECT '【5. 所有产品统计】' as check_item;
SELECT 
    p.id,
    p.name,
    p.model,
    COUNT(plr.id) as line_count
FROM products p
LEFT JOIN product_line_relations plr ON p.id = plr.product_id
GROUP BY p.id, p.name, p.model;

-- 6. 查看所有生产线及可生产的产品数量
SELECT '【6. 生产线可生产产品统计】' as check_item;
SELECT 
    pl.id,
    pl.name,
    pl.status,
    COUNT(plr.id) as product_count
FROM production_lines pl
LEFT JOIN product_line_relations plr ON pl.id = plr.production_line_id
GROUP BY pl.id, pl.name, pl.status;

-- 7. 查看检测记录示例（如果有的话）
SELECT '【7. 检测记录】带产品和生产线关联的检测记录' as check_item;
SELECT 
    dr.id,
    dr.detection_no,
    dr.serial_no,
    dr.status,
    p.name as product_name,
    pl.name as line_name,
    dr.defect,
    dr.created_at
FROM detection_records dr
LEFT JOIN products p ON dr.product_id = p.id
LEFT JOIN production_lines pl ON dr.production_line_id = pl.id
ORDER BY dr.id DESC
LIMIT 5;
