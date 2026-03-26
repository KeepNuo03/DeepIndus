# DEEPINDUS 系统 - 数据模型设计文档

## 📊 核心数据模型

### 1. 用户模型 (User)

```typescript
interface User {
  id: number;                    // 用户ID
  username: string;              // 用户名
  email: string;                 // 邮箱
  phone: string;                 // 手机号
  name: string;                  // 真实姓名
  avatar?: string;               // 头像URL
  department: string;            // 部门名称
  departmentId: number;          // 部门ID
  roles: string[];               // 角色列表 ['admin', 'production']
  permissions: string[];         // 权限列表
  status: 'active' | 'disabled'; // 账号状态
  online: boolean;               // 是否在线
  lastLogin: string;             // 最后登录时间
  lastIp: string;                // 最后登录IP
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
  loginCount: number;            // 登录次数
}
```

---

### 2. 检测记录模型 (DetectionRecord)

```typescript
interface DetectionRecord {
  id: number;                    // 记录ID
  detectionNo: string;           // 检测编号 #DET-2026A01
  serialNo: string;              // 产品序列号 SN-6729-BM-01
  productId: number;             // 产品ID
  productName: string;           // 产品名称
  productionLineId: number;      // 生产线ID
  productionLineName: string;    // 生产线名称
  cameraId: string;              // 摄像头ID
  cameraName: string;            // 摄像头名称
  
  // 缺陷信息
  defect?: string;               // 缺陷描述
  defectType?: string;           // 缺陷类型
  severity?: 'critical' | 'warning' | 'minor';  // 严重程度
  confidence: number;            // 置信度 (0-100)
  
  // 位置信息
  positionX: number;             // X坐标
  positionY: number;             // Y坐标
  area: number;                  // 缺陷面积 (cm²)
  
  // 图像信息
  imageUrl: string;              // 检测图片URL
  thumbnailUrl?: string;         // 缩略图URL
  
  // 状态信息
  status: 'pass' | 'fail';       // 检测结果
  processStatus: '待处理' | '处理中' | '已完成';
  
  // 时间信息
  timestamp: string;             // 检测时间
  createdAt: string;             // 记录创建时间
  
  // AI 信息
  modelId: number;               // 使用的模型ID
  modelName: string;             // 模型名称
  modelVersion: string;          // 模型版本
  
  // 其他
  shift?: string;                // 班次
  operator?: string;             // 操作员
  batchNo?: string;              // 批次号
}
```

---

### 3. 缺陷详情模型 (DefectDetail)

```typescript
interface DefectDetail extends DetectionRecord {
  // 扩展详细信息
  impactLevel: 'A级' | 'B级' | 'C级';  // 影响等级
  statusNote: string;                   // 状态说明
  
  // 处理记录
  processRecords: ProcessRecord[];
  
  // 相似缺陷
  similarDefects: SimilarDefect[];
}

interface ProcessRecord {
  id: number;
  type: 'create' | 'review' | 'confirm' | 'resolve';  // 记录类型
  time: string;                                         // 操作时间
  action: string;                                       // 操作描述
  operator: string;                                     // 操作人员
  note?: string;                                        // 备注
}

interface SimilarDefect {
  id: number;
  detectionNo: string;
  type: string;                 // 缺陷类型
  similarity: number;           // 相似度 (0-100)
  date: string;                 // 日期
  thumbnailUrl: string;         // 缩略图
}
```

---

### 4. 生产线模型 (ProductionLine)

```typescript
interface ProductionLine {
  id: number;                    // 生产线ID
  name: string;                  // 生产线名称
  code: string;                  // 生产线编码
  status: 'running' | 'maintenance' | 'stopped';  // 状态
  location: string;              // 位置
  shift: string;                 // 当前班次
  runningTime: string;           // 运行时长
  
  // 产能数据
  todayOutput: number;           // 今日产量
  targetOutput: number;          // 目标产量
  qualifiedCount: number;        // 良品数
  defectCount: number;           // 次品数
  yieldRate: number;             // 良率 (%)
  utilizationRate: number;       // 稼动率 (%)
  cycleTime: number;             // 平均节拍 (秒)
  
  // 配置信息
  cameras: Camera[];             // 摄像头列表
  stations: Station[];           // 工位列表
  
  // 时间信息
  createdAt: string;
  updatedAt: string;
}

interface Camera {
  id: number;
  name: string;                  // 摄像头名称 CAM-A01-01
  position: string;              // 位置 (入料口/检测工位/出料口)
  online: boolean;               // 是否在线
  ip: string;                    // IP地址
  resolution?: string;           // 分辨率
  fps?: number;                  // 帧率
}

interface Station {
  id: number;
  name: string;                  // 工位名称
  status: 'working' | 'idle' | 'maintenance';  // 状态
  operator?: string;             // 操作员
}
```

---

### 5. 产品模型 (Product)

```typescript
interface Product {
  id: number;                    // 产品ID
  name: string;                  // 产品名称
  model: string;                 // 产品型号 INDUS-ST-CRS-001
  category: 'steel' | 'aluminum' | 'plastic';  // 产品分类
  
  // 规格参数
  dimensions: string;            // 尺寸规格
  material: string;              // 材质
  weight: number;                // 重量 (kg)
  surfaceTreatment: string;      // 表面处理
  
  // 质量标准
  standard: string;              // 检测标准 (国标)
  threshold: number;             // 缺陷容忍阈值 (%)
  targetYield: number;           // 目标良率 (%)
  
  // 检测配置
  defectTypes: DefectTypeConfig[];
  
  // 生产统计
  status: 'active' | 'inactive'; // 产品状态
  todayOutput: number;           // 今日产量
  yieldRate: number;             // 当前良率
  batchCount: number;            // 批次数
  totalOutput: number;           // 累计产量
  avgYieldRate: number;          // 平均良率
  
  // 其他信息
  imageUrl?: string;             // 产品图片
  description?: string;          // 产品描述
  createdAt: string;
  updatedAt: string;
}

interface DefectTypeConfig {
  name: string;                  // 缺陷类型名称
  threshold: number;             // 检测阈值 (%)
  enabled: boolean;              // 是否启用
}
```

---

### 6. AI 模型模型 (AIModel)

```typescript
interface AIModel {
  id: number;                    // 模型ID
  name: string;                  // 模型名称
  version: string;               // 版本号
  status: 'deployed' | 'training' | 'testing' | 'archived';  // 状态
  isMain: boolean;               // 是否为主模型
  description: string;           // 模型描述
  
  // 训练信息
  trainedAt: string;             // 训练日期
  trainer: string;               // 训练者
  trainingDataSize: number;      // 训练样本数
  
  // 性能指标
  accuracy: number;              // 准确率 (%)
  recall: number;                // 召回率 (%)
  f1Score: number;               // F1 分数 (0-1)
  inferenceSpeed: number;        // 推理速度 (ms)
  
  // 模型参数
  architecture: string;          // 架构 (YOLOv8/ResNet/MobileNet)
  size: number;                  // 模型大小 (MB)
  batchSize: number;             // 批次大小
  learningRate: string;          // 学习率
  epochs: number;                // 训练轮数
  
  // 文件信息
  modelFile: string;             // 模型文件路径
  configFile?: string;           // 配置文件路径
  
  // 版本历史
  versions: ModelVersion[];
  
  // 时间信息
  createdAt: string;
  updatedAt: string;
  deployedAt?: string;           // 部署时间
}

interface ModelVersion {
  id: number;
  version: string;               // 版本号
  accuracy: number;              // 准确率
  current: boolean;              // 是否为当前版本
  createdAt: string;
  changelog?: string;            // 变更日志
}
```

---

### 7. 角色权限模型 (Role)

```typescript
interface Role {
  id: number;                    // 角色ID
  name: string;                  // 角色名称
  code: string;                  // 角色编码
  description: string;           // 角色描述
  userCount: number;             // 用户数量
  permissions: string[];         // 权限列表
  createdAt: string;
  updatedAt: string;
}
```

---

### 8. 操作日志模型 (OperationLog)

```typescript
interface OperationLog {
  id: number;                    // 日志ID
  time: string;                  // 操作时间
  userId: number;                // 用户ID
  userName: string;              // 用户名
  type: 'create' | 'update' | 'delete' | 'login';  // 操作类型
  module: string;                // 操作模块
  content: string;               // 操作内容描述
  ip: string;                    // IP地址
  status: 'success' | 'failure'; // 操作状态
  userAgent?: string;            // 浏览器信息
  duration?: number;             // 操作耗时 (ms)
}
```

---

### 9. 部门模型 (Department)

```typescript
interface Department {
  id: number;                    // 部门ID
  name: string;                  // 部门名称
  code: string;                  // 部门编码
  userCount: number;             // 用户数量
  parentId?: number;             // 父部门ID
  level: number;                 // 层级
  sort: number;                  // 排序
  createdAt: string;
  updatedAt: string;
}
```

---

### 10. 统计数据模型

#### 10.1 数据大屏 KPI
```typescript
interface DashboardKPI {
  todayDetectionCount: number;   // 当日检测数
  avgDefectRate: number;         // 平均缺陷率 (%)
  yieldRate: number;             // 良品率 (%)
  utilizationRate: number;       // 设备稼动率 (%)
}
```

#### 10.2 趋势数据
```typescript
interface TrendData {
  timeLabels: string[];          // 时间标签
  defectCounts: number[];        // 缺陷数量
  yieldRates: number[];          // 良率数据
}
```

#### 10.3 缺陷分布
```typescript
interface DefectDistribution {
  type: string;                  // 缺陷类型
  count: number;                 // 数量
  percentage: number;            // 占比 (%)
}
```

---

## 🗄️ 数据库设计建议

### 核心表清单

```sql
-- 用户表
users (id, username, email, phone, name, password_hash, department_id, status, created_at, updated_at)

-- 角色表
roles (id, name, code, description, created_at, updated_at)

-- 用户角色关联表
user_roles (user_id, role_id)

-- 权限表
permissions (id, name, code, module, description)

-- 角色权限关联表
role_permissions (role_id, permission_id)

-- 部门表
departments (id, name, code, parent_id, level, sort, created_at, updated_at)

-- 检测记录表
detection_records (id, detection_no, serial_no, product_id, production_line_id, 
                   camera_id, defect_type, severity, confidence, 
                   position_x, position_y, area, image_url, 
                   status, process_status, timestamp, model_id, 
                   shift, batch_no, created_at)

-- 缺陷处理记录表
process_records (id, detection_id, type, action, operator, note, created_at)

-- 生产线表
production_lines (id, name, code, location, status, target_output, created_at, updated_at)

-- 摄像头表
cameras (id, name, position, production_line_id, ip, resolution, fps, online, created_at)

-- 工位表
stations (id, name, production_line_id, status, operator, created_at, updated_at)

-- 产品表
products (id, name, model, category, dimensions, material, weight, 
          surface_treatment, standard, threshold, target_yield, 
          status, image_url, created_at, updated_at)

-- 产品缺陷配置表
product_defect_config (id, product_id, defect_name, threshold, enabled)

-- AI模型表
ai_models (id, name, version, status, is_main, description, 
           architecture, accuracy, recall, f1_score, inference_speed, 
           size, training_data_size, batch_size, learning_rate, epochs,
           model_file, trained_at, trainer, created_at, updated_at)

-- 模型版本表
model_versions (id, model_id, version, accuracy, current, changelog, created_at)

-- 操作日志表
operation_logs (id, user_id, user_name, type, module, content, 
                ip, status, user_agent, duration, created_at)

-- 生产统计表（按日统计）
production_statistics (id, date, production_line_id, output, 
                       qualified_count, defect_count, yield_rate, 
                       utilization_rate, cycle_time)
```

---

## 🔗 表关系说明

### 用户相关
```
users ←→ user_roles ←→ roles ←→ role_permissions ←→ permissions
users → departments
```

### 检测相关
```
detection_records → products
detection_records → production_lines
detection_records → cameras
detection_records → ai_models
detection_records ← process_records (1对多)
```

### 生产相关
```
production_lines ← cameras (1对多)
production_lines ← stations (1对多)
production_lines ← production_statistics (1对多)
```

### 产品相关
```
products ← product_defect_config (1对多)
products ← detection_records (1对多)
```

---

## 📝 数据约束

### 必填字段
- 所有 ID 字段
- 用户：username, email, password
- 检测记录：detection_no, serial_no, confidence, timestamp
- 生产线：name, code, status
- 产品：name, model, category, standard

### 唯一性约束
- users.username
- users.email
- detection_records.detection_no
- production_lines.code
- products.model
- ai_models.name + version

### 外键约束
- 所有关联表需要设置外键约束
- 删除策略：级联删除或设置为 NULL

---

## 🔢 枚举值定义

### 用户状态
```
active - 正常
disabled - 禁用
```

### 检测结果
```
pass - 合格
fail - 不合格
```

### 缺陷严重程度
```
critical - 严重
warning - 警告
minor - 轻微
```

### 生产线状态
```
running - 运行中
maintenance - 维护中
stopped - 已停止
```

### 工位状态
```
working - 工作中
idle - 空闲
maintenance - 维护中
```

### 模型状态
```
deployed - 已部署
training - 训练中
testing - 测试中
archived - 已归档
```

### 处理状态
```
待处理 - pending
处理中 - processing
已完成 - completed
```

### 操作类型
```
create - 新增
update - 修改
delete - 删除
login - 登录
```

---

## 💾 数据存储建议

### 图片存储
- **方案1：** 本地文件系统 + Nginx 静态服务
- **方案2：** 对象存储（OSS/S3）
- **建议：** 使用对象存储，支持 CDN 加速

### 模型文件存储
- **路径：** `/models/{model_name}/{version}/model.pt`
- **大小限制：** 单个模型 < 500MB
- **建议：** 单独存储，不放数据库

### 缓存策略
- **Redis 缓存：**
  - 用户 Token（2小时过期）
  - 用户权限信息（30分钟）
  - 统计数据（5分钟）
  - 在线用户列表（实时）

---

## 🔐 数据安全

### 敏感数据
- 密码：使用 bcrypt 加密，salt rounds >= 10
- Token：使用 JWT，包含用户ID和权限
- 个人信息：手机号、邮箱需脱敏显示

### 数据备份
- 建议每日增量备份
- 重要操作前全量备份
- 保留至少 30 天的历史数据

---

## 📊 索引建议

### 高频查询字段需要建立索引
```sql
-- 用户表
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_department_id ON users(department_id);

-- 检测记录表
CREATE INDEX idx_detection_serial_no ON detection_records(serial_no);
CREATE INDEX idx_detection_timestamp ON detection_records(timestamp);
CREATE INDEX idx_detection_product_id ON detection_records(product_id);
CREATE INDEX idx_detection_status ON detection_records(status);

-- 操作日志表
CREATE INDEX idx_logs_user_id ON operation_logs(user_id);
CREATE INDEX idx_logs_created_at ON operation_logs(created_at);
CREATE INDEX idx_logs_type ON operation_logs(type);
```

---

## 🎯 数据初始化

### 系统启动时需要的初始数据

1. **默认管理员账号**
```
用户名: admin
密码: Admin@123456
角色: 超级管理员
```

2. **默认角色**
- 超级管理员
- 生产管理员
- 质检员
- 技术人员
- 操作员

3. **默认权限**
- 用户管理、角色管理
- 系统配置、数据导出
- 模型管理、生产管理等

4. **示例数据**
- 2-3 条生产线
- 5-10 个产品型号
- 2-3 个 AI 模型
- 若干检测记录（可选）
