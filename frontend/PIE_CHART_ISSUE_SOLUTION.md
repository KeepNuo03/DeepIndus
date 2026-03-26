# 🎯 饼图显示旧数据问题 - 完整解决方案

## 📊 问题分析

### 当前状态（从截图分析）

**饼图显示了 8 个标签：**
1. `scratch` - 英文（旧数据）❌
2. `oversize` - 英文（旧数据）❌
3. `划伤` - 中文（新数据，正确）✅
4. `划伤` - 中文（重复？）⚠️
5. `斑块` - 中文（新数据，正确）✅
6. `龟裂` - 中文（新数据，正确）✅
7. `麻点` - 中文（新数据，正确）✅
8. 可能还有其他...

### 问题根源

**数据库中同时存在两类数据：**

```sql
-- 旧数据（未标准化，检测时间较早）
defect_type = 'scratch'    -- ❌ 拼写错误（少 s）
defect_type = 'oversize'   -- ❌ 非标准类型
defect_type = 'crack'      -- ❌ 非标准类型

-- 新数据（已标准化，最近的检测）
defect_type = 'scratches'  -- ✅ 正确，前端转换为"划伤"
defect_type = 'patches'    -- ✅ 正确，前端转换为"斑块"
defect_type = 'crazing'    -- ✅ 正确，前端转换为"龟裂"
```

**后端接口返回了所有数据（旧+新）：**
```json
[
  { "type": "scratch", "count": 30 },     // 旧数据
  { "type": "oversize", "count": 20 },    // 旧数据
  { "type": "scratches", "count": 25 },   // 新数据（转换为"划伤"）
  { "type": "patches", "count": 15 }      // 新数据（转换为"斑块"）
]
```

**结果：**
- `scratch` 和 `scratches` 被当作两个不同的类型
- 饼图同时显示了 `scratch`（英文）和 `划伤`（转换后的）

---

## 🔧 解决方案

### ✅ 方案 1：清理数据库（最彻底）⭐

**后端执行 SQL：**
```sql
-- 将所有 scratch 改为 scratches
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type = 'scratch';

-- 将所有 oversize 映射到标准类型
UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type = 'oversize';

-- 将所有 crack 映射到标准类型
UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type = 'crack';
```

**执行后：**
1. 数据库只有标准类型
2. 后端接口返回标准数据
3. 前端饼图正确显示

**优点：** 一劳永逸，数据彻底标准化  
**缺点：** 需要修改数据库

---

### ✅ 方案 2：后端接口映射（不改数据库）

**在后端接口中合并同类数据：**

详细代码见上方 Java 代码示例。

**逻辑：**
```
scratch → 映射为 scratches → 合并计数
oversize → 映射为 patches → 合并计数
划伤 → 映射为 scratches → 合并计数

最终返回：
scratches: 55  (30 + 25 合并)
patches: 35    (20 + 15 合并)
```

**优点：** 不修改数据库  
**缺点：** 每次查询都要映射

---

### ✅ 方案 3：前端强制刷新（临时方案）

**我已经在前端添加了图表强制刷新：**

```javascript
initCharts() {
  // 如果图表已存在，先销毁再重建
  if (this.pieChartInstance) {
    this.pieChartInstance.dispose();  // ← 销毁旧图表
  }
  
  this.pieChartInstance = echarts.init(this.$refs.pieChart);  // 重建
  // ...
}
```

**但这只是治标，根本问题还是后端数据！**

---

## 🚀 立即操作

### 步骤 1：确认问题（2分钟）

**在浏览器中：**
1. 访问 `http://localhost:5173/dashboard`
2. 按 `F12` → Console 标签
3. 查看输出：
   ```
   === 后端返回的缺陷分布数据 ===
   原始数据: [...]
   ```
4. **截图或复制这个日志给后端同学！**

---

### 步骤 2：后端修复（5分钟）

**方法 A：快速 SQL（推荐）**
```sql
-- 执行这 3 条 SQL
UPDATE detection_records SET defect_type = 'scratches' WHERE defect_type = 'scratch';
UPDATE detection_records SET defect_type = 'patches' WHERE defect_type = 'oversize';
UPDATE detection_records SET defect_type = 'crazing' WHERE defect_type = 'crack';
```

**方法 B：代码映射**
- 在 `DashboardService` 中添加映射逻辑
- 见上方完整代码

---

### 步骤 3：验证修复（1分钟）

**前端操作：**
1. 清除浏览器缓存（`Ctrl + Shift + Delete`）
2. 刷新页面（`Ctrl + F5`）
3. 查看饼图

**应该只显示 6 种中文标签，不再有英文！**

---

## 🔄 如何让 ECharts 图表刷新？

### 前端已自动刷新

**我已经添加了强制刷新逻辑：**
```javascript
// 每次初始化前先销毁旧图表
if (this.pieChartInstance) {
  this.pieChartInstance.dispose();
}
// 重新创建
this.pieChartInstance = echarts.init(this.$refs.pieChart);
```

### 手动触发刷新

**如果需要手动刷新，有两种方法：**

**方法 1：刷新浏览器页面**
```
F5 或 Ctrl + R
```

**方法 2：硬刷新（清除缓存）**
```
Ctrl + F5 或 Ctrl + Shift + R
```

**方法 3：清除缓存后刷新**
```
Ctrl + Shift + Delete → 清除缓存 → 刷新
```

---

## 💡 为什么会同时显示旧数据和新数据？

### 数据库状态示例

```sql
-- 假设数据库中有这些记录：

id | defect_type  | timestamp
---|--------------|-------------------
1  | scratch      | 2026-02-01 10:00  ← 旧数据
2  | scratch      | 2026-02-01 11:00  ← 旧数据
3  | scratches    | 2026-02-03 14:00  ← 新数据
4  | scratches    | 2026-02-03 15:00  ← 新数据
5  | oversize     | 2026-02-01 10:30  ← 旧数据
6  | patches      | 2026-02-03 14:30  ← 新数据
```

**后端统计查询：**
```sql
SELECT defect_type, COUNT(*) 
FROM detection_records 
GROUP BY defect_type;

-- 结果：
scratch:    2  ← 被当作一个独立类型
scratches:  2  ← 被当作另一个独立类型
oversize:   1
patches:    1
```

**前端接收并显示：**
```
scratch (2)     → 前端无法转换，显示英文 ❌
scratches (2)   → 前端转换为"划伤" ✅
oversize (1)    → 前端无法转换，显示英文 ❌
patches (1)     → 前端转换为"斑块" ✅
```

**所以同时出现了英文和中文！**

---

## 🎯 解决办法（给后端）

### 立即执行这个 SQL（30秒搞定）

```sql
-- 合并同类数据
UPDATE detection_records 
SET defect_type = 'scratches' 
WHERE defect_type IN ('scratch', 'scratches', '划伤', '表面划伤');

UPDATE detection_records 
SET defect_type = 'patches' 
WHERE defect_type IN ('oversize', 'patches', '斑块', '色斑');

UPDATE detection_records 
SET defect_type = 'crazing' 
WHERE defect_type IN ('crack', 'crazing', '龟裂', '裂纹', '边缘裂纹');

UPDATE detection_records 
SET defect_type = 'pitted_surface' 
WHERE defect_type IN ('pitted', 'pit', 'pitted_surface', '麻点');

UPDATE detection_records 
SET defect_type = 'inclusion' 
WHERE defect_type IN ('inclusion', '夹杂', '夹渣', '杂质');

UPDATE detection_records 
SET defect_type = 'rolled-in_scale' 
WHERE defect_type IN ('scale', 'rolled_in_scale', 'rolled-in_scale', '氧化皮', '氧化皮卷入');

-- 验证
SELECT defect_type, COUNT(*) 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type;

-- 应该只看到 6 种类型
```

**执行后效果：**
```
scratches:       55  ← 合并了 scratch(30) + scratches(25)
patches:         35  ← 合并了 oversize(20) + patches(15)
crazing:         25
pitted_surface:  10
inclusion:        8
rolled-in_scale:  5
```

---

## 🔄 前端自动刷新

**前端已经添加了强制刷新机制：**
- ✅ 每次 `loadAllData()` 都会重新获取数据
- ✅ 每次 `initCharts()` 都会先销毁旧图表再重建
- ✅ 后端修复后，只需刷新浏览器页面即可

---

## ✅ 总结

### 问题本质
**不是 ECharts 初始化问题，是数据库中有旧数据！**

### 解决步骤
1. **后端执行 SQL** - 更新所有旧数据为标准类型（5分钟）
2. **刷新浏览器** - `Ctrl + F5` 硬刷新（5秒）
3. **查看饼图** - 应该只显示 6 种中文标签

### 我已经做了什么
- ✅ 前端添加了图表强制刷新机制
- ✅ 前端添加了调试日志（Console 可查看后端数据）
- ✅ 创建了给后端的修复文档（含 SQL 脚本）

### 后端需要做什么
- ✅ 执行 SQL 更新脚本
- ✅ 或在接口中添加映射逻辑
- ✅ 确保只返回 6 种标准英文类型

---

## 📞 立即行动

**给后端同学：**
1. 📄 查看文档：`BACKEND_OLD_DATA_CLEANUP.md`
2. 💻 执行 SQL 脚本（复制粘贴即可）
3. 🔄 重启后端服务（如果用了方案 B）
4. 📞 通知前端重新测试

**前端同学：**
1. 等待后端修复
2. 收到通知后刷新页面（`Ctrl + F5`）
3. 查看饼图是否正常

**预计 10 分钟内解决！** 🚀
