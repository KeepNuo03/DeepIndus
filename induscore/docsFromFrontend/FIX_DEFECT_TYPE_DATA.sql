-- ============================================
-- 缺陷类型数据清理脚本
-- 将非标准类型映射到标准类型
-- ============================================

-- 1. 查看当前数据库中的缺陷类型分布
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type 
ORDER BY count DESC;

-- 2. 修复拼写错误：scratch → scratches
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type = 'scratch';

-- 3. 映射非标准类型：oversize → patches
UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type = 'oversize';

-- 4. 映射非标准类型：crack → crazing
UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type = 'crack';

-- 5. 修复中文值：划伤 → scratches
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type = '划伤';

-- 6. 修复中文值：斑块 → patches
UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type = '斑块';

-- 7. 修复中文值：龟裂 → crazing
UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type = '龟裂';

-- 8. 修复中文值：夹杂 → inclusion
UPDATE detection_records 
SET defect_type = 'inclusion' 
WHERE defect_type = '夹杂';

-- 9. 修复中文值：麻点 → pitted_surface
UPDATE detection_records 
SET defect_type = 'pitted_surface' 
WHERE defect_type = '麻点';

-- 10. 修复中文值：氧化皮卷入 → rolled-in_scale
UPDATE detection_records 
SET defect_type = 'rolled-in_scale' 
WHERE defect_type = '氧化皮卷入';

-- 11. 其他可能的别名映射
UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type = 'patch';

UPDATE detection_records 
SET defect_type = 'pitted_surface' 
WHERE defect_type IN ('pitted', 'pitted-surface');

UPDATE detection_records 
SET defect_type = 'rolled-in_scale' 
WHERE defect_type = 'scale';

UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type = '表面划伤';

UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type = '边缘裂纹';

-- 12. 验证修复结果（应该只看到 6 种标准类型）
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type 
ORDER BY count DESC;

-- 预期结果应该只有：
-- scratches
-- crazing
-- patches
-- pitted_surface
-- inclusion
-- rolled-in_scale
