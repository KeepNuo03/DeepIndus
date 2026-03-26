# 🧹 后端数据库清理指南 - 旧缺陷类型数据

## ⚠️ 问题描述

数据大屏饼图同时显示了旧数据和新数据：
- **旧数据：** `scratch`, `oversize` - 英文显示（未转换）
- **新数据：** `划伤`, `斑块`, `龟裂`, `麻点` - 中文显示（已转换）

**原因：** 数据库中同时存在旧的检测记录（未标准化）和新的检测记录（已标准化）

---

## 🎯 解决方案

### 方案 A：更新历史数据（推荐）⭐

**执行 SQL 脚本，将所有旧数据映射到标准类型：**

```sql
-- ========================================
-- 缺陷类型标准化 SQL 脚本
-- ========================================

-- 1. 查看当前所有缺陷类型（先确认问题）
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type 
ORDER BY count DESC;

-- 2. 备份数据（可选但推荐）
CREATE TABLE detection_records_backup AS SELECT * FROM detection_records;

-- 3. 修复拼写错误
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type = 'scratch';

-- 4. 映射非标准类型到标准类型
UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type IN ('oversize', 'patch', '尺寸超标');

UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type IN ('crack', 'cracking', '裂纹', '边缘裂纹');

UPDATE detection_records 
SET defect_type = 'inclusion' 
WHERE defect_type IN ('inclusion', '夹杂', '杂质');

UPDATE detection_records 
SET defect_type = 'pitted_surface' 
WHERE defect_type IN ('pitted', 'pit', '麻点');

UPDATE detection_records 
SET defect_type = 'rolled-in_scale' 
WHERE defect_type IN ('scale', 'rolled_in_scale', '氧化皮', '氧化皮卷入');

-- 5. 修复中文值
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type IN ('划伤', '表面划伤', '划痕');

UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type IN ('斑块', '色斑');

UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type IN ('龟裂', '裂纹');

UPDATE detection_records 
SET defect_type = 'inclusion' 
WHERE defect_type IN ('夹杂', '夹渣');

UPDATE detection_records 
SET defect_type = 'pitted_surface' 
WHERE defect_type IN ('麻点', '点蚀');

UPDATE detection_records 
SET defect_type = 'rolled-in_scale' 
WHERE defect_type IN ('氧化皮卷入', '氧化皮');

-- 6. 验证修复结果
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type 
ORDER BY count DESC;

-- 应该只看到 6 种标准类型：
-- scratches, crazing, patches, pitted_surface, inclusion, rolled-in_scale
```

**执行后：**
- 所有历史数据统一为标准类型
- 饼图不再显示旧的英文标签

---

### 方案 B：后端接口添加映射逻辑

**如果不想修改数据库，可以在接口层映射：**

```java
@Service
public class DashboardService {
    
    /**
     * 类型映射表
     */
    private static final Map<String, String> TYPE_MAPPING = Map.ofEntries(
        // 修复拼写错误
        Map.entry("scratch", "scratches"),
        
        // 映射非标准类型
        Map.entry("oversize", "patches"),
        Map.entry("crack", "crazing"),
        Map.entry("pit", "pitted_surface"),
        Map.entry("scale", "rolled-in_scale"),
        
        // 中文映射
        Map.entry("划伤", "scratches"),
        Map.entry("表面划伤", "scratches"),
        Map.entry("龟裂", "crazing"),
        Map.entry("裂纹", "crazing"),
        Map.entry("夹杂", "inclusion"),
        Map.entry("斑块", "patches"),
        Map.entry("麻点", "pitted_surface"),
        Map.entry("氧化皮卷入", "rolled-in_scale"),
        Map.entry("氧化皮", "rolled-in_scale")
    );
    
    /**
     * 获取缺陷分布（带类型标准化）
     */
    public List<DefectDistribution> getDefectDistribution() {
        // 1. 从数据库查询原始数据
        List<DefectDistribution> rawData = recordRepository
            .findDefectDistribution();
        
        // 2. 合并并标准化类型
        Map<String, Integer> standardMap = new HashMap<>();
        
        for (DefectDistribution item : rawData) {
            String rawType = item.getType();
            // 映射到标准类型
            String standardType = mapToStandardType(rawType);
            
            // 累加数量（同类型合并）
            standardMap.merge(standardType, item.getCount(), Integer::sum);
        }
        
        // 3. 转换为返回格式
        List<DefectDistribution> result = new ArrayList<>();
        int total = standardMap.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : standardMap.entrySet()) {
            DefectDistribution dist = new DefectDistribution();
            dist.setType(entry.getKey());
            dist.setCount(entry.getValue());
            dist.setPercentage((entry.getValue() * 100.0) / total);
            result.add(dist);
        }
        
        return result;
    }
    
    /**
     * 映射到标准类型
     */
    private String mapToStandardType(String rawType) {
        if (rawType == null) {
            return null;
        }
        
        // 如果已经是标准类型，直接返回
        Set<String> standardTypes = Set.of(
            "crazing", "inclusion", "patches", 
            "pitted_surface", "rolled-in_scale", "scratches"
        );
        
        if (standardTypes.contains(rawType)) {
            return rawType;
        }
        
        // 尝试映射
        return TYPE_MAPPING.getOrDefault(rawType, rawType);
    }
}
```

**这样：**
- `scratch` + `scratches` + `划伤` → 合并为 `scratches`
- `oversize` → 映射为 `patches`
- `crack` → 映射为 `crazing`

---

## 🔄 立即测试

### 后端添加调试日志

```java
@GetMapping("/dashboard/defect-distribution")
public ResponseEntity<ApiResponse> getDefectDistribution() {
    List<DefectDistribution> data = dashboardService.getDefectDistribution();
    
    // 打印每条数据
    log.info("===== 缺陷分布数据 =====");
    data.forEach(item -> {
        log.info("type: {}, count: {}", item.getType(), item.getCount());
    });
    log.info("=======================");
    
    return ResponseEntity.ok(ApiResponse.success(data));
}
```

**查看后端日志，确认返回的 type 值！**

---

## 🎯 验证标准

### 修复后，后端日志应该显示：

```
===== 缺陷分布数据 =====
type: scratches, count: 55        // ✅ 标准类型
type: crazing, count: 25          // ✅ 标准类型
type: patches, count: 23          // ✅ 标准类型
type: pitted_surface, count: 10   // ✅ 标准类型
type: inclusion, count: 8         // ✅ 标准类型
type: rolled-in_scale, count: 5   // ✅ 标准类型
=======================
```

**不应该出现：**
- ❌ scratch (少 s)
- ❌ oversize
- ❌ crack
- ❌ 任何中文

---

## 🧪 前端验证

### 修复后，前端 Console 应该显示：

```
=== 后端返回的缺陷分布数据 ===
原始数据: [
  { type: 'scratches', count: 55 },
  { type: 'crazing', count: 25 },
  { type: 'patches', count: 23 },
  { type: 'pitted_surface', count: 10 },
  { type: 'inclusion', count: 8 },
  { type: 'rolled-in-scale', count: 5 }
]
数据类型: [
  { type: 'scratches', count: 55, 转换后: '划伤' },
  { type: 'crazing', count: 25, 转换后: '龟裂' },
  { type: 'patches', count: 23, 转换后: '斑块' },
  { type: 'pitted_surface', count: 10, 转换后: '麻点' },
  { type: 'inclusion', count: 8, 转换后: '夹杂' },
  { type: 'rolled-in_scale', count: 5, 转换后: '氧化皮卷入' }
]
==============================
```

**饼图显示：**
```
✅ 划伤 (55)
✅ 龟裂 (25)
✅ 斑块 (23)
✅ 麻点 (10)
✅ 夹杂 (8)
✅ 氧化皮卷入 (5)
```

---

## ⏰ 紧急修复优先级

### P0 - 立即执行（5分钟）

```sql
-- 快速修复：合并主要的旧数据
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type IN ('scratch', '划伤', '表面划伤');

UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type IN ('oversize', '斑块');

UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type IN ('crack', '龟裂');
```

**执行后立即重新访问前端页面，应该就正常了！**

---

## 📞 总结

**问题根源：** 数据库中同时存在旧数据（未标准化）和新数据（已标准化）

**解决方案：** 
1. 执行 SQL 更新脚本（见上方）
2. 或在后端接口添加映射逻辑

**修复后：** 所有缺陷类型统一为 6 种标准值，饼图正确显示中文标签

**预计修复时间：** 5-10 分钟
