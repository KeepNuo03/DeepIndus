# 缺陷类型更新说明

## 📋 更新内容

已将所有页面的缺陷类型统一为 YOLO 模型的 6 种实际类别。

---

## 🎯 标准缺陷类型（6种）

| 英文值（API传参） | 中文标签（UI显示） | 说明 |
|------------------|------------------|------|
| `crazing` | 龟裂 | 表面龟裂纹理 |
| `inclusion` | 夹杂 | 材料中有杂质 |
| `patches` | 斑块 | 表面斑块状缺陷 |
| `pitted_surface` | 麻点 | 表面麻点状缺陷 |
| `rolled-in_scale` | 氧化皮卷入 | 轧制时氧化皮卷入 |
| `scratches` | 划伤 | 表面划伤 |

---

## 📝 修改的文件

### 1. **新建常量文件**
**文件：** `src/utils/constants.js`

```javascript
// 缺陷类型定义
export const DEFECT_TYPES = [
  { value: 'crazing', label: '龟裂' },
  { value: 'inclusion', label: '夹杂' },
  { value: 'patches', label: '斑块' },
  { value: 'pitted_surface', label: '麻点' },
  { value: 'rolled-in_scale', label: '氧化皮卷入' },
  { value: 'scratches', label: '划伤' },
];

// 转换函数
export function getDefectLabel(value) {
  const defect = DEFECT_TYPES.find(d => d.value === value);
  return defect ? defect.label : value;
}
```

---

### 2. **历史查询页面**
**文件：** `src/pages/query.vue`

**修改内容：**

**① 筛选下拉框（第 60-68 行）**
```vue
<!-- 修改前 -->
<select v-model="filters.defectType">
  <option value="all">全部类型</option>
  <option value="scratch">表面划伤</option>
  <option value="oversize">尺寸超标</option>
  <option value="crack">边缘裂纹</option>
</select>

<!-- 修改后 -->
<select v-model="filters.defectType">
  <option value="all">全部类型</option>
  <option v-for="type in defectTypes" :key="type.value" :value="type.value">
    {{ type.label }}
  </option>
</select>
```

**② 表格显示（第 229 行）**
```vue
<!-- 修改前 -->
{{ record.defect }}

<!-- 修改后 -->
{{ getDefectLabel(record.defect) }}
```

**③ 导入常量和方法**
```javascript
import { DEFECT_TYPES, getDefectLabel } from '@/utils/constants';

data() {
  return {
    defectTypes: DEFECT_TYPES,  // 添加到 data
    // ...
  }
}

methods: {
  getDefectLabel(value) {
    return getDefectLabel(value);
  }
}
```

---

### 3. **数据大屏页面**
**文件：** `src/pages/data_dashboard.vue`

**修改内容：**

**① 饼图数据转换（第 458-469 行）**
```javascript
// 修改前
data: this.distributionData.map((item, index) => ({
  value: item.count,
  name: item.type,  // 直接使用后端返回的值
  itemStyle: { color: colors[index] }
}))

// 修改后
data: this.distributionData.map((item, index) => ({
  value: item.count,
  name: getDefectLabel(item.type),  // 转换为中文
  itemStyle: { color: colors[index % 6] }  // 支持 6 种颜色
}))
```

**② 默认数据更新（第 465-469 行）**
```javascript
// 更新为新的 6 种缺陷类型
[
  { value: 15, name: '龟裂', itemStyle: { color: '#3b82f6' } },
  { value: 12, name: '夹杂', itemStyle: { color: '#6366f1' } },
  { value: 10, name: '斑块', itemStyle: { color: '#f43f5e' } },
  { value: 8, name: '麻点', itemStyle: { color: '#f59e0b' } },
  { value: 6, name: '氧化皮卷入', itemStyle: { color: '#8b5cf6' } },
  { value: 20, name: '划伤', itemStyle: { color: '#ec4899' } },
]
```

**③ 导入方法**
```javascript
import { getDefectLabel } from '@/utils/constants';
```

---

### 4. **缺陷详情页面**
**文件：** `src/pages/defect_detail.vue`

**修改内容：**

**① 缺陷类型显示（第 109 行）**
```vue
<!-- 修改前 -->
{{ defectInfo.type }}

<!-- 修改后 -->
{{ getDefectLabel(defectInfo.type) }}
```

**② 导入和方法**
```javascript
import { getDefectLabel } from '@/utils/constants';

methods: {
  getDefectLabel(value) {
    return getDefectLabel(value);
  }
}
```

---

### 5. **实时检测页面**
**文件：** `src/pages/real_time_detect.vue`

**修改内容：**

**① 检测记录列表（第 264-305 行）**
```vue
<!-- 修改前：硬编码的示例 -->
<div>常规人员进入</div>
<div>未穿戴反光衣</div>

<!-- 修改后：动态渲染 -->
<div v-for="record in recentRecords" :key="record.id">
  {{ getDefectLabel(record.title) }}
</div>
```

**② 导入和方法**
```javascript
import { getDefectLabel } from '@/utils/constants';

methods: {
  getDefectLabel(value) {
    return getDefectLabel(value);
  }
}
```

---

## 🔄 数据流转

### 前端发送请求

**查询时（历史查询页面）：**
```javascript
// 用户在下拉框选择"划伤"
filters.defectType = 'scratches'  // 存储英文值

// 调用 API
getRecords({
  defectType: 'scratches'  // 发送英文值给后端
})
```

### 后端返回数据

**后端返回（两种可能）：**

**方式 1：返回英文（推荐）**
```json
{
  "data": {
    "records": [
      {
        "id": 1,
        "defect": "scratches",  // 英文
        "confidence": "98.4%"
      }
    ]
  }
}
```

**方式 2：返回中文**
```json
{
  "data": {
    "records": [
      {
        "id": 1,
        "defect": "划伤",  // 中文
        "confidence": "98.4%"
      }
    ]
  }
}
```

### 前端显示

**前端处理（兼容两种方式）：**
```vue
<!-- 使用转换函数 -->
{{ getDefectLabel(record.defect) }}

<!-- 如果是英文 'scratches' → 显示 '划伤' -->
<!-- 如果是中文 '划伤' → 显示 '划伤' （原样返回）-->
```

**转换逻辑：**
```javascript
getDefectLabel('scratches')  // 返回 '划伤'
getDefectLabel('划伤')      // 返回 '划伤' （找不到匹配，返回原值）
```

---

## 🎨 UI 显示效果

### 历史查询页面筛选框

```
┌─────────────────┐
│ 缺陷类型        │
├─────────────────┤
│ 全部类型  ▼     │
│ ├─ 龟裂         │
│ ├─ 夹杂         │
│ ├─ 斑块         │
│ ├─ 麻点         │
│ ├─ 氧化皮卷入   │
│ └─ 划伤         │
└─────────────────┘
```

### 数据大屏饼图

```
缺陷类型占比
┌──────────────┐
│   🔵 龟裂 21% │
│   🟣 夹杂 17% │
│   🔴 斑块 14% │
│   🟡 麻点 11% │
│   🟣 氧化 8%  │
│   🔴 划伤 29% │
└──────────────┘
```

### 检测记录表格

```
| 缺陷描述 |
|---------|
| 龟裂     |
| 划伤     |
| 无异常   |
```

---

## 🔧 后端接口约定

### 查询接口

**前端发送：**
```
GET /records/query?defectType=scratches
```

**后端返回（建议）：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "defect": "scratches",  // 英文值
        "defectType": "scratches"  // 或者用 defectType 字段
      }
    ]
  }
}
```

**前端显示：**
- `scratches` → 自动转换为 `划伤`

---

### 数据大屏接口

**后端返回（建议）：**
```json
{
  "code": 200,
  "data": [
    { "type": "scratches", "count": 45, "percentage": 45 },
    { "type": "crazing", "count": 25, "percentage": 25 },
    { "type": "patches", "count": 15, "percentage": 15 }
  ]
}
```

**前端显示：**
- 饼图标签：`划伤`、`龟裂`、`斑块`

---

## ✅ 更新完成检查清单

### 前端修改
- [x] 创建 `src/utils/constants.js`
- [x] 更新 `query.vue` 筛选框
- [x] 更新 `query.vue` 表格显示
- [x] 更新 `data_dashboard.vue` 饼图
- [x] 更新 `defect_detail.vue` 类型显示
- [x] 更新 `real_time_detect.vue` 记录列表

### 后端配置
- [ ] 确保接口参数接收英文值（如 `scratches`）
- [ ] 确保返回数据使用英文值或中文值
- [ ] 测试筛选功能是否正常

---

## 🧪 测试方法

### 测试筛选功能

1. 访问历史查询页面：`http://localhost:5173/query`
2. 点击"缺陷类型"下拉框
3. 应该看到 6 种类型：
   - 龟裂
   - 夹杂
   - 斑块
   - 麻点
   - 氧化皮卷入
   - 划伤
4. 选择"划伤"
5. 点击"查询记录"
6. **F12 → Network** 查看请求：
   ```
   GET /records/query?page=1&pageSize=20&defectType=scratches
   ```
7. 后端应该返回所有类型为 `scratches` 的记录

---

### 测试数据大屏

1. 访问数据大屏：`http://localhost:5173/dashboard`
2. 查看"缺陷类型占比"饼图
3. 应该显示中文标签：
   - 龟裂 XX%
   - 夹杂 XX%
   - 斑块 XX%
   - 麻点 XX%
   - 氧化皮卷入 XX%
   - 划伤 XX%

---

### 测试详情页面

1. 访问缺陷详情页
2. 查看"缺陷类型"卡片
3. 应该显示中文：如"划伤"

---

## 💡 兼容性说明

### 前端转换函数的容错性

```javascript
getDefectLabel('scratches')     // 返回 '划伤' ✅
getDefectLabel('划伤')          // 返回 '划伤' ✅ （已经是中文）
getDefectLabel('unknown_type')  // 返回 'unknown_type' ✅ （未知类型，原样返回）
getDefectLabel(null)            // 返回 null ✅
getDefectLabel(undefined)       // 返回 undefined ✅
```

**这意味着：**
- 后端返回英文 → 自动转中文 ✅
- 后端返回中文 → 保持中文 ✅
- 后端返回未知值 → 原样显示 ✅

**前端代码具有很好的容错性！** 💪

---

## 🔄 如何添加新的缺陷类型？

**只需要修改一个文件：** `src/utils/constants.js`

```javascript
export const DEFECT_TYPES = [
  { value: 'crazing', label: '龟裂' },
  { value: 'inclusion', label: '夹杂' },
  { value: 'patches', label: '斑块' },
  { value: 'pitted_surface', label: '麻点' },
  { value: 'rolled-in_scale', label: '氧化皮卷入' },
  { value: 'scratches', label: '划伤' },
  // 添加新类型
  { value: 'new_defect', label: '新缺陷' },  // ← 在这里添加
];
```

**所有页面会自动更新！** 🎉

---

## 📊 后端返回数据示例

### 推荐：返回英文值

**优点：**
- 数据库存储标准化
- 便于统计和查询
- 前端自动转换显示

**示例：**

**查询记录接口：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "detectionNo": "#DET-2026A01",
        "serialNo": "SN-6729-BM-01",
        "defect": "scratches",      // 英文 ⭐
        "defectType": "scratches",  // 或用这个字段
        "confidence": "98.4%",
        "status": "fail"
      }
    ]
  }
}
```

**数据大屏分布接口：**
```json
{
  "code": 200,
  "data": [
    { "type": "scratches", "count": 45, "percentage": 45 },     // 英文 ⭐
    { "type": "crazing", "count": 25, "percentage": 25 },
    { "type": "patches", "count": 15, "percentage": 15 },
    { "type": "pitted_surface", "count": 10, "percentage": 10 },
    { "type": "inclusion", "count": 8, "percentage": 8 },
    { "type": "rolled-in_scale", "count": 5, "percentage": 5 }
  ]
}
```

**缺陷详情接口：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "type": "scratches",          // 英文 ⭐
    "defectType": "scratches",    // 或用这个字段
    "confidence": "98.4%",
    "severity": "critical"
  }
}
```

---

## 🎯 前后端约定

### 字段命名统一

**建议使用：**
- `defect` - 缺陷描述字段
- `defectType` - 缺陷类型字段

**值格式：**
- `crazing` - 龟裂
- `inclusion` - 夹杂
- `patches` - 斑块
- `pitted_surface` - 麻点
- `rolled-in_scale` - 氧化皮卷入
- `scratches` - 划伤

**注意：**
- 使用小写
- 使用下划线或连字符
- 与 YOLO 模型类别名保持一致

---

## 🎉 更新完成！

### ✅ 前端已完成
- 所有页面统一使用 6 种缺陷类型
- UI 显示中文标签
- API 传递英文值
- 自动转换函数

### 🔄 后端需要配合
- 接受英文值筛选（如 `?defectType=scratches`）
- 返回数据可以是英文值（前端自动转中文）
- 或返回中文值（前端原样显示）

**建议后端返回英文值，前端统一转换！** ⭐

---

## 📞 测试验证

**测试筛选功能：**
1. 打开历史查询页面
2. 选择"划伤"
3. F12 Network 看到：`?defectType=scratches` ✅
4. 表格显示中文"划伤" ✅

**测试数据大屏：**
1. 打开数据大屏页面
2. 饼图显示中文标签 ✅
3. 鼠标悬停显示详细信息 ✅

**完美！** 🎊
