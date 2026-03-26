# 🐛 后端缺陷类型数据问题 - 需要修复

## ⚠️ 问题描述

数据大屏的饼图显示了错误的缺陷类型标签，出现了：
- ❌ `scratch` - 英文（少了 s，不在标准类型中）
- ❌ `oversize` - 英文（不在标准类型中）
- ❌ `crack` - 英文（不在标准类型中）
- ⚠️ `划伤` - 中文（应该返回英文 `scratches`）
- ⚠️ `斑块` - 中文（应该返回英文 `patches`）

**饼图截图显示的实际值，说明后端返回了混乱的数据！**

---

## 🎯 标准约定

### 前后端已约定的 6 种标准缺陷类型

| 英文值（后端必须返回这个）| 中文标签（前端自动转换）| YOLO 类别 |
|------------------------|---------------------|-----------|
| `crazing` | 龟裂 | class 0 |
| `inclusion` | 夹杂 | class 1 |
| `patches` | 斑块 | class 2 |
| `pitted_surface` | 麻点 | class 3 |
| `rolled-in_scale` | 氧化皮卷入 | class 4 |
| `scratches` | 划伤 | class 5 |

**注意：**
- ✅ 使用 `scratches`（有 s）
- ❌ 不要用 `scratch`（少 s）
- ✅ 使用 `pitted_surface`（有下划线）
- ❌ 不要用 `pitted-surface`（连字符）
- ✅ 使用 `rolled-in_scale`（连字符）

---

## 🔍 问题定位

### 接口：`GET /dashboard/defect-distribution`

**当前后端返回的数据（有问题）：**
```json
{
  "code": 200,
  "data": [
    { "type": "scratch", "count": 45 },      // ❌ 错误：应该是 scratches
    { "type": "oversize", "count": 25 },     // ❌ 错误：不在标准类型中
    { "type": "crack", "count": 15 },        // ❌ 错误：不在标准类型中
    { "type": "划伤", "count": 10 },         // ❌ 错误：不应该返回中文
    { "type": "斑块", "count": 8 }           // ❌ 错误：不应该返回中文
  ]
}
```

**正确的返回格式应该是：**
```json
{
  "code": 200,
  "data": [
    { "type": "scratches", "count": 45, "percentage": 45 },           // ✅ 正确
    { "type": "crazing", "count": 25, "percentage": 25 },             // ✅ 正确
    { "type": "patches", "count": 15, "percentage": 15 },             // ✅ 正确
    { "type": "pitted_surface", "count": 10, "percentage": 10 },      // ✅ 正确
    { "type": "inclusion", "count": 8, "percentage": 8 },             // ✅ 正确
    { "type": "rolled-in_scale", "count": 5, "percentage": 5 }        // ✅ 正确
  ]
}
```

---

## 🔧 需要修复的地方

### 1. 检查数据库中的缺陷类型字段

**问题：** 数据库中可能存储了错误的值

**排查 SQL：**
```sql
-- 查看数据库中有哪些缺陷类型
SELECT DISTINCT defect_type FROM detection_records;

-- 或
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type;
```

**如果看到：**
```
scratch      ← 错误，应该是 scratches
oversize     ← 错误，不在标准类型中
crack        ← 错误，不在标准类型中
划伤         ← 错误，不应该存中文
```

**需要修复数据：**
```sql
-- 修复示例（根据实际情况调整）
UPDATE detection_records SET defect_type = 'scratches' WHERE defect_type = 'scratch';
UPDATE detection_records SET defect_type = 'scratches' WHERE defect_type = '划伤';
UPDATE detection_records SET defect_type = 'patches' WHERE defect_type = '斑块';

-- 删除或转换不标准的类型
-- oversize, crack 等需要映射到标准类型
```

---

### 2. 检查后端统计接口代码

**可能的问题代码（伪代码）：**
```java
// ❌ 错误写法
public List<DefectDistribution> getDefectDistribution() {
    return recordRepository.groupByDefectType();
    // 直接返回数据库中的原始值，可能包含 scratch, oversize 等
}
```

**正确的写法（需要映射）：**
```java
// ✅ 正确写法
public List<DefectDistribution> getDefectDistribution() {
    List<DefectDistribution> rawData = recordRepository.groupByDefectType();
    
    // 映射到标准类型
    return rawData.stream()
        .map(item -> {
            String standardType = mapToStandardType(item.getType());
            item.setType(standardType);
            return item;
        })
        .collect(Collectors.toList());
}

// 类型映射函数
private String mapToStandardType(String rawType) {
    Map<String, String> typeMapping = new HashMap<>();
    // 标准化映射
    typeMapping.put("scratch", "scratches");      // 修复少s的问题
    typeMapping.put("划伤", "scratches");         // 中文映射
    typeMapping.put("oversize", "patches");       // 将 oversize 映射到标准类型
    typeMapping.put("crack", "crazing");          // 将 crack 映射到标准类型
    typeMapping.put("斑块", "patches");           // 中文映射
    
    return typeMapping.getOrDefault(rawType, rawType);
}
```

---

### 3. 检查 YOLO 模型返回的类别名

**问题：** YOLO 模型的类别名可能与约定不一致

**排查：**
```python
# 在 YOLO 服务中检查
model = YOLO('models/best.pt')
print(model.names)
# 应该输出：{0: 'crazing', 1: 'inclusion', ...}
```

**如果输出不对（如 {0: 'scratch', 1: 'oversize'}）：**

**方案 A：修改 YOLO 服务返回时映射**
```python
# yolo-service/main.py

# 类别映射
CLASS_NAME_MAPPING = {
    'scratch': 'scratches',
    'oversize': 'patches',
    'crack': 'crazing',
    # ... 其他映射
}

@app.post("/detect/image")
async def detect_image(file: UploadFile):
    results = model(img)
    
    detections = []
    for box in results[0].boxes:
        class_name = model.names[int(box.cls[0])]
        
        # 映射到标准类别名
        standard_name = CLASS_NAME_MAPPING.get(class_name, class_name)
        
        detection = {
            "class_name": standard_name,  # 使用标准名称
            "confidence": float(box.conf[0])
        }
        detections.append(detection)
```

**方案 B：重新训练模型时使用标准类别名**
- 修改训练数据的 `classes.txt` 或 `data.yaml`
- 确保类别名为：crazing, inclusion, patches, pitted_surface, rolled-in_scale, scratches

---

## 🧪 调试方法

### 在浏览器中查看后端返回的原始数据

**步骤：**
1. 访问数据大屏：`http://localhost:5173/dashboard`
2. 按 `F12` 打开开发者工具
3. 切换到 **Network** 标签
4. 找到请求：`defect-distribution`
5. 点击查看 **Response** 标签

**查看后端返回了什么：**
```json
{
  "code": 200,
  "data": [
    { "type": "???", "count": 45 },  // ← 看这里的 type 值是什么
    { "type": "???", "count": 25 }
  ]
}
```

**请截图或复制后端返回的完整数据给我们！**

---

## 📋 给后端同学的修复清单

### 需要检查的地方

#### ✅ 检查点 1：数据库数据
```sql
-- 执行这个查询，看看数据库中存的是什么
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type 
ORDER BY count DESC;
```

**预期结果：** 应该只有 6 种标准类型
```
scratches          45
crazing            25
patches            15
pitted_surface     10
inclusion          8
rolled-in_scale    5
```

**如果看到其他值（scratch, oversize, crack, 中文等）：**
- 需要更新数据库数据
- 使用上面的 UPDATE SQL 修正

---

#### ✅ 检查点 2：后端接口代码

**文件位置：** 大概在 `DashboardController.java` 或类似文件

**检查返回的数据：**
```java
@GetMapping("/dashboard/defect-distribution")
public ResponseEntity<ApiResponse> getDefectDistribution() {
    List<DefectDistribution> data = dashboardService.getDefectDistribution();
    
    // ← 在这里打印日志，看看返回了什么
    System.out.println("缺陷分布数据: " + data);
    
    return ResponseEntity.ok(ApiResponse.success(data));
}
```

**必须返回的格式：**
```json
[
  { "type": "scratches", "count": 45, "percentage": 45 },
  { "type": "crazing", "count": 25, "percentage": 25 },
  { "type": "patches", "count": 15, "percentage": 15 },
  { "type": "pitted_surface", "count": 10, "percentage": 10 },
  { "type": "inclusion", "count": 8, "percentage": 8 },
  { "type": "rolled-in_scale", "count": 5, "percentage": 5 }
]
```

**关键点：**
- ✅ `type` 字段必须是英文
- ✅ 必须是 6 种标准类型之一
- ✅ 拼写必须完全一致（注意 s、下划线、连字符）
- ❌ 不要返回中文
- ❌ 不要返回其他类型（oversize, crack 等）

---

#### ✅ 检查点 3：YOLO 模型类别名

**在 YOLO 服务中检查：**
```python
# yolo-service/main.py 启动时打印
model = YOLO('models/best.pt')
print("YOLO 类别:", model.names)
# 应该输出：{0: 'crazing', 1: 'inclusion', 2: 'patches', ...}
```

**如果类别名不对：**
- 需要在检测结果返回前进行映射
- 或重新训练模型使用标准类别名

---

## 🔧 快速修复方案

### 方案 1：在后端统一映射（推荐）⭐

**创建类型映射工具类：**

```java
public class DefectTypeMapper {
    
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>();
    
    static {
        // 修复拼写错误
        TYPE_MAPPING.put("scratch", "scratches");
        
        // 映射非标准类型
        TYPE_MAPPING.put("oversize", "patches");     // 尺寸超标 → 斑块
        TYPE_MAPPING.put("crack", "crazing");        // 裂纹 → 龟裂
        
        // 中文映射
        TYPE_MAPPING.put("划伤", "scratches");
        TYPE_MAPPING.put("龟裂", "crazing");
        TYPE_MAPPING.put("夹杂", "inclusion");
        TYPE_MAPPING.put("斑块", "patches");
        TYPE_MAPPING.put("麻点", "pitted_surface");
        TYPE_MAPPING.put("氧化皮卷入", "rolled-in_scale");
        
        // 其他可能的别名
        TYPE_MAPPING.put("表面划伤", "scratches");
        TYPE_MAPPING.put("边缘裂纹", "crazing");
    }
    
    /**
     * 将任意缺陷类型映射到标准类型
     */
    public static String mapToStandard(String rawType) {
        if (rawType == null) {
            return null;
        }
        
        // 如果已经是标准类型，直接返回
        String[] standardTypes = {"crazing", "inclusion", "patches", 
                                   "pitted_surface", "rolled-in_scale", "scratches"};
        if (Arrays.asList(standardTypes).contains(rawType)) {
            return rawType;
        }
        
        // 尝试映射
        return TYPE_MAPPING.getOrDefault(rawType, rawType);
    }
}
```

**在接口中使用：**
```java
@GetMapping("/dashboard/defect-distribution")
public ResponseEntity<ApiResponse> getDefectDistribution() {
    List<DefectDistribution> rawData = recordRepository.groupByDefectType();
    
    // 映射到标准类型
    List<DefectDistribution> standardData = rawData.stream()
        .map(item -> {
            String standardType = DefectTypeMapper.mapToStandard(item.getType());
            item.setType(standardType);
            return item;
        })
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(ApiResponse.success(standardData));
}
```

---

### 方案 2：更新数据库数据

**如果数据库中存储了错误的值，需要批量更新：**

```sql
-- 1. 修复拼写错误
UPDATE detection_records SET defect_type = 'scratches' WHERE defect_type = 'scratch';

-- 2. 映射非标准类型
UPDATE detection_records SET defect_type = 'patches' WHERE defect_type = 'oversize';
UPDATE detection_records SET defect_type = 'crazing' WHERE defect_type = 'crack';

-- 3. 修复中文值
UPDATE detection_records SET defect_type = 'scratches' WHERE defect_type = '划伤';
UPDATE detection_records SET defect_type = 'patches' WHERE defect_type = '斑块';
UPDATE detection_records SET defect_type = 'crazing' WHERE defect_type = '龟裂';
UPDATE detection_records SET defect_type = 'inclusion' WHERE defect_type = '夹杂';
UPDATE detection_records SET defect_type = 'pitted_surface' WHERE defect_type = '麻点';
UPDATE detection_records SET defect_type = 'rolled-in_scale' WHERE defect_type = '氧化皮卷入';

-- 4. 验证修复结果
SELECT defect_type, COUNT(*) as count 
FROM detection_records 
WHERE defect_type IS NOT NULL 
GROUP BY defect_type;

-- 应该只看到 6 种标准类型
```

---

### 方案 3：YOLO 服务层映射

**如果 YOLO 模型的类别名不标准，在 Python 服务中映射：**

```python
# yolo-service/main.py

# 类别名映射
YOLO_TO_STANDARD = {
    'scratch': 'scratches',
    'oversize': 'patches',
    'crack': 'crazing',
    # 如果模型返回中文
    '划伤': 'scratches',
    '斑块': 'patches',
}

@app.post("/detect/image")
async def detect_image(file: UploadFile):
    results = model(img)
    
    detections = []
    for box in results[0].boxes:
        raw_class_name = model.names[int(box.cls[0])]
        
        # 映射到标准名称
        standard_class_name = YOLO_TO_STANDARD.get(raw_class_name, raw_class_name)
        
        detection = {
            "class_id": int(box.cls[0]),
            "class_name": standard_class_name,  # ← 使用标准名称
            "confidence": float(box.conf[0])
        }
        detections.append(detection)
    
    return {"code": 200, "data": {"detections": detections}}
```

---

## 🧪 测试验证

### 修复后的测试步骤

**1. 重启后端服务**
```bash
# 应用代码修改或数据库更新后
# 重启 Spring Boot
```

**2. 清除前端缓存**
```bash
# 浏览器中
Ctrl + Shift + Delete → 清除缓存
```

**3. 测试接口**
```bash
# 使用 Postman 或 curl 测试
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8000/v1/dashboard/defect-distribution

# 查看返回的 type 字段，应该只有：
# crazing, inclusion, patches, pitted_surface, rolled-in_scale, scratches
```

**4. 刷新前端页面**
```bash
# 访问数据大屏
http://localhost:5173/dashboard

# 饼图应该显示：
龟裂、夹杂、斑块、麻点、氧化皮卷入、划伤
```

---

## 📊 标准对照表（给后端参考）

### YOLO 类别 ID 与标准名称对照

| Class ID | 标准英文名（必须用这个）| 中文名 | 别名（需要映射） |
|----------|---------------------|--------|----------------|
| 0 | `crazing` | 龟裂 | crack, 龟裂, 裂纹 |
| 1 | `inclusion` | 夹杂 | 夹杂, 杂质 |
| 2 | `patches` | 斑块 | patch, 斑块, oversize |
| 3 | `pitted_surface` | 麻点 | pitted, 麻点 |
| 4 | `rolled-in_scale` | 氧化皮卷入 | scale, 氧化皮 |
| 5 | `scratches` | 划伤 | scratch, 划伤, 划痕 |

**关键：**
- ✅ 永远返回"标准英文名"这一列的值
- ✅ 如果数据库或 YOLO 返回"别名"，需要映射
- ❌ 不要返回中文
- ❌ 不要返回别名

---

## 🔍 调试命令

### 后端日志添加

**在缺陷分布接口中添加日志：**
```java
@GetMapping("/dashboard/defect-distribution")
public ResponseEntity<ApiResponse> getDefectDistribution() {
    List<DefectDistribution> data = dashboardService.getDefectDistribution();
    
    // 打印每条数据的类型
    data.forEach(item -> {
        log.info("缺陷类型: type={}, count={}", item.getType(), item.getCount());
    });
    
    return ResponseEntity.ok(ApiResponse.success(data));
}
```

**查看日志输出，确认返回的 type 值！**

---

## ✅ 验收标准

### 修复后应该看到

**接口返回：**
```json
{
  "code": 200,
  "data": [
    { "type": "scratches", "count": 45 },      // ✅ 标准英文
    { "type": "crazing", "count": 25 },        // ✅ 标准英文
    { "type": "patches", "count": 15 },        // ✅ 标准英文
    { "type": "pitted_surface", "count": 10 }, // ✅ 标准英文
    { "type": "inclusion", "count": 8 },       // ✅ 标准英文
    { "type": "rolled-in_scale", "count": 5 }  // ✅ 标准英文
  ]
}
```

**前端饼图显示：**
```
龟裂 (25)          // ✅ 中文显示
夹杂 (8)           // ✅ 中文显示
斑块 (15)          // ✅ 中文显示
麻点 (10)          // ✅ 中文显示
氧化皮卷入 (5)     // ✅ 中文显示
划伤 (45)          // ✅ 中文显示
```

**不应该看到：**
- ❌ scratch（少 s）
- ❌ oversize（非标准类型）
- ❌ crack（非标准类型）
- ❌ 任何中文值（后端不应返回中文）

---

## 🎯 总结

### 问题根源
**后端返回了不标准的缺陷类型值**，包括：
1. 拼写错误（scratch 而不是 scratches）
2. 非标准类型（oversize, crack）
3. 中文值（划伤、斑块）

### 解决方案
**后端需要：**
1. 检查数据库中的 `defect_type` 字段值
2. 在接口中添加类型映射逻辑
3. 确保返回的都是 6 种标准英文值
4. 或更新数据库数据为标准值

### 前端状态
**前端已完成：**
- ✅ 转换函数正确
- ✅ Mock 数据已清理
- ✅ 等待后端返回正确数据

---

## 📞 联系方式

**前端已就绪，等待后端修复！**

**请后端同学：**
1. 查看上述检查点
2. 修复数据或添加映射
3. 测试接口返回
4. 通知前端重新测试

**修复后请通知我们进行联调！** 🚀
