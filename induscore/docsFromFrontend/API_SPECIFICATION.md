# INDUSCORE 系统 - API 接口规范文档

## 📌 接口约定

### 基础信息
- **Base URL：** `http://api.induscore.com/v1`
- **请求格式：** JSON
- **响应格式：** JSON
- **字符编码：** UTF-8
- **认证方式：** Bearer Token (JWT)

### 统一响应格式
```json
{
  "code": 200,           // 状态码：200成功，其他失败
  "message": "success",  // 消息提示
  "data": {},           // 业务数据
  "timestamp": 1738579200000  // 时间戳
}
```

### 错误码规范
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未认证
- `403` - 权限不足
- `404` - 资源不存在
- `500` - 服务器错误

---

## 1️⃣ 用户认证模块

### 1.1 用户登录
**接口：** `POST /auth/login`

**请求参数：**
```json
{
  "username": "admin@industry.com",  // 用户名或邮箱
  "password": "password123",         // 密码
  "remember": true                   // 是否记住登录
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@industry.com",
      "name": "管理员",
      "avatar": "https://...",
      "roles": ["admin", "production"],
      "permissions": ["user_manage", "system_config"]
    }
  }
}
```

### 1.2 用户注册
**接口：** `POST /auth/register`

**请求参数：**
```json
{
  "username": "user001",
  "email": "user@company.com",
  "companyCode": "ID-2026",
  "password": "password123",
  "confirmPassword": "password123",
  "agreeTerms": true
}
```

### 1.3 退出登录
**接口：** `POST /auth/logout`

**请求头：**
```
Authorization: Bearer {token}
```

---

## 2️⃣ 实时检测模块

### 2.1 获取实时检测统计
**接口：** `GET /detection/realtime/statistics`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "todayTargets": 1284,        // 今日检测目标数
    "warningCount": 24,          // 预警触发次数
    "avgConfidence": 98.2,       // 平均置信度
    "modelVersion": "YOLOv8-Standard"
  }
}
```

### 2.2 获取实时检测记录
**接口：** `GET /detection/realtime/records`

**请求参数：**
```
?limit=10&offset=0
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "type": "normal",           // normal/warning/danger
        "title": "常规人员进入",
        "description": "10:42:15 Cam-01 通道A",
        "confidence": "98%",
        "timestamp": "2026-02-01 10:42:15"
      }
    ],
    "total": 1284
  }
}
```

### 2.3 获取趋势数据
**接口：** `GET /detection/realtime/trend`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "timeLabels": ["10:35", "10:37", "10:39", "10:41", "10:43", "10:45"],
    "detectionCounts": [42, 65, 82, 59, 74, 88]
  }
}
```

### 2.4 控制检测状态
**接口：** `POST /detection/realtime/control`

**请求参数：**
```json
{
  "action": "start",  // start/stop
  "cameraId": "CAM-01"
}
```

---

## 3️⃣ 数据大屏模块

### 3.1 获取 KPI 指标
**接口：** `GET /dashboard/kpi`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "todayDetectionCount": 12842,      // 当日总检测数
    "avgDefectRate": 0.42,             // 平均缺陷率 (%)
    "yieldRate": 99.58,                // 良品率 (%)
    "utilizationRate": 88.4            // 设备稼动率 (%)
  }
}
```

### 3.2 获取缺陷趋势数据
**接口：** `GET /dashboard/defect-trend`

**请求参数：**
```
?startTime=2026-02-01 00:00:00&endTime=2026-02-01 23:59:59
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "timeLabels": ["06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00"],
    "defectCounts": [12, 18, 45, 30, 22, 10, 5, 8],
    "yieldRates": [99.8, 99.7, 98.2, 99.1, 99.5, 99.9, 99.9, 99.8]
  }
}
```

### 3.3 获取缺陷类型分布
**接口：** `GET /dashboard/defect-distribution`

**响应数据：**
```json
{
  "code": 200,
  "data": [
    { "type": "表面划伤", "count": 45, "percentage": 45 },
    { "type": "尺寸超差", "count": 25, "percentage": 25 },
    { "type": "组件缺失", "count": 15, "percentage": 15 },
    { "type": "边缘毛刺", "count": 15, "percentage": 15 }
  ]
}
```

### 3.4 获取实时报警列表
**接口：** `GET /dashboard/alerts`

**响应数据：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "severity": "critical",         // critical/warning/resolved
      "title": "严重划伤",
      "description": "流水线 A-01 | 组件 ID: PR-9942",
      "time": "14:28:45",
      "icon": "material-symbols:warning-rounded"
    }
  ]
}
```

---

## 4️⃣ 历史查询模块

### 4.1 查询检测记录
**接口：** `GET /records/query`

**请求参数：**
```
?page=1
&pageSize=20
&dateStart=2026-02-01
&dateEnd=2026-02-01
&defectType=all        // all/scratch/oversize/crack
&status=all            // all/pass/fail
&search=SN-6729        // 搜索关键词
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "detectionNo": "#DET-2026A01",
        "serialNo": "SN-6729-BM-01",
        "defect": "表面严重划痕",
        "severity": "critical",
        "confidence": "98.4%",
        "timestamp": "2026-02-01 14:22:05",
        "status": "fail",              // pass/fail
        "selected": false
      }
    ],
    "total": 2842,
    "page": 1,
    "pageSize": 20
  }
}
```

### 4.2 批量导出记录
**接口：** `POST /records/export`

**请求参数：**
```json
{
  "recordIds": [1, 2, 3],
  "format": "excel"      // excel/pdf/csv
}
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "downloadUrl": "https://cdn.induscore.com/exports/records_20260201.xlsx"
  }
}
```

### 4.3 批量删除记录
**接口：** `DELETE /records/batch`

**请求参数：**
```json
{
  "recordIds": [1, 2, 3]
}
```

---

## 5️⃣ 缺陷详情模块

### 5.1 获取缺陷详情
**接口：** `GET /defect/:id`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "detectionNo": "#DET-2026A01",
    "serialNo": "SN-6729-BM-01",
    "type": "表面严重划痕",
    "confidence": "98.4%",
    "severity": "严重",                  // 严重/中等/轻微
    "positionX": "342px",
    "positionY": "156px",
    "area": "2.4cm²",
    "impactLevel": "A级",
    "productionLine": "流水线 A-01",
    "shift": "早班 08:00-16:00",
    "timestamp": "2026-02-01 14:22:05",
    "model": "YOLOv8-Industrial",
    "status": "待处理",                  // 待处理/处理中/已完成
    "statusNote": "等待质检员人工复核确认",
    "imageUrl": "https://cdn.induscore.com/defects/20260201/image001.jpg",
    "processRecords": [
      {
        "id": 1,
        "type": "create",               // create/review/confirm/resolve
        "time": "2026-02-01 14:22:05",
        "action": "AI 系统自动检测到缺陷",
        "operator": "System_Auto"
      }
    ],
    "similarDefects": [
      {
        "id": 1,
        "type": "表面划痕",
        "similarity": "96.2%",
        "date": "2026-01-31",
        "thumbnailUrl": "https://..."
      }
    ]
  }
}
```

### 5.2 开始人工复核
**接口：** `POST /defect/:id/review`

**请求参数：**
```json
{
  "action": "confirm",   // confirm/reject
  "note": "确认为严重划痕，需要返工"
}
```

---

## 6️⃣ 生产线管理模块

### 6.1 获取生产线列表
**接口：** `GET /production/lines`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "statistics": {
      "total": 6,
      "running": 4,
      "maintenance": 1,
      "todayProduction": 12842,
      "avgYield": 99.58
    },
    "lines": [
      {
        "id": 1,
        "name": "生产线 A-01",
        "status": "running",          // running/maintenance/stopped
        "location": "车间一 A区",
        "shift": "早班 08:00-16:00",
        "runningTime": "6h 42m",
        "todayOutput": 2840,
        "targetOutput": 3200,
        "qualifiedCount": 2828,
        "defectCount": 12,
        "yieldRate": 99.58,
        "utilizationRate": 88.75,
        "cycleTime": 12.5,
        "cameras": [
          {
            "id": 1,
            "name": "CAM-A01-01",
            "position": "入料口",
            "online": true,
            "ip": "192.168.1.101"
          }
        ],
        "stations": [
          {
            "id": 1,
            "name": "工位1",
            "status": "working"        // working/idle/maintenance
          }
        ]
      }
    ]
  }
}
```

### 6.2 启动/停止生产线
**接口：** `POST /production/lines/:id/control`

**请求参数：**
```json
{
  "action": "start"     // start/stop
}
```

### 6.3 编辑生产线
**接口：** `PUT /production/lines/:id`

**请求参数：**
```json
{
  "name": "生产线 A-01",
  "location": "车间一 A区",
  "targetOutput": 3200
}
```

### 6.4 配置摄像头
**接口：** `PUT /production/lines/:lineId/cameras/:cameraId`

**请求参数：**
```json
{
  "name": "CAM-A01-01",
  "position": "入料口",
  "ip": "192.168.1.101",
  "resolution": "1920x1080"
}
```

---

## 7️⃣ 产品管理模块

### 7.1 获取产品列表
**接口：** `GET /products`

**请求参数：**
```
?category=all          // all/steel/aluminum/plastic
&search=INDUS-ST
&page=1
&pageSize=20
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "工业级冷轧钢板",
        "model": "INDUS-ST-CRS-001",
        "category": "steel",
        "dimensions": "1000×2000×2mm",
        "material": "Q235钢",
        "standard": "GB/T 3280-2015",
        "threshold": 0.5,
        "targetYield": 99.5,
        "weight": 31.4,
        "surfaceTreatment": "镀锌处理",
        "status": "active",          // active/inactive
        "todayOutput": 2840,
        "yieldRate": 99.58,
        "batchCount": 42,
        "totalOutput": 125420,
        "avgYieldRate": 99.52,
        "createdAt": "2026-01-15",
        "imageUrl": "https://...",
        "defectTypes": [
          {
            "name": "表面划痕",
            "threshold": 0.3,
            "enabled": true
          }
        ]
      }
    ],
    "total": 12
  }
}
```

### 7.2 新增产品
**接口：** `POST /products`

**请求参数：**
```json
{
  "name": "工业级冷轧钢板",
  "model": "INDUS-ST-CRS-001",
  "category": "steel",
  "dimensions": "1000×2000×2mm",
  "material": "Q235钢",
  "standard": "GB/T 3280-2015",
  "threshold": 0.5,
  "defectTypes": [
    {
      "name": "表面划痕",
      "threshold": 0.3,
      "enabled": true
    }
  ]
}
```

### 7.3 编辑产品
**接口：** `PUT /products/:id`

### 7.4 删除产品
**接口：** `DELETE /products/:id`

---

## 8️⃣ AI 模型管理模块

### 8.1 获取模型列表
**接口：** `GET /models`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "statistics": {
      "totalModels": 8,
      "deployed": 3,
      "avgAccuracy": 98.7,
      "abTests": 2
    },
    "models": [
      {
        "id": 1,
        "name": "YOLOv8-Industrial-Pro",
        "version": "v3.2.1",
        "status": "deployed",         // deployed/training/testing/archived
        "isMain": true,
        "description": "针对钢材表面缺陷优化的专业检测模型",
        "trainedAt": "2026-02-01",
        "trainer": "李工",
        "accuracy": 98.8,
        "recall": 97.5,
        "f1Score": 0.982,
        "inferenceSpeed": 12.5,       // ms
        "size": 142.5,                // MB
        "trainingDataSize": 125000,
        "architecture": "YOLOv8",
        "batchSize": 32,
        "learningRate": "0.001",
        "epochs": 150,
        "modelFile": "models/yolov8_v3.2.1.pt",
        "versions": [
          {
            "id": 1,
            "version": "v3.2.1",
            "accuracy": 98.8,
            "current": true,
            "createdAt": "2026-02-01"
          }
        ]
      }
    ]
  }
}
```

### 8.2 上传模型
**接口：** `POST /models/upload`

**请求格式：** `multipart/form-data`

**请求参数：**
```
file: [模型文件]
name: "YOLOv8-Industrial-Pro"
version: "v3.2.2"
description: "优化了检测速度"
```

### 8.3 部署模型
**接口：** `POST /models/:id/deploy`

**请求参数：**
```json
{
  "target": "production",    // production/test
  "replaceMain": false       // 是否替换主模型
}
```

### 8.4 下线模型
**接口：** `POST /models/:id/undeploy`

### 8.5 获取模型对比数据
**接口：** `GET /models/comparison`

**请求参数：**
```
?modelIds=1,2,3,4
&metric=accuracy       // accuracy/speed/f1
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "labels": ["YOLOv8-Pro", "ResNet-DefectNet", "MobileNet-Light"],
    "accuracy": [98.8, 99.2, 96.5],
    "recall": [97.5, 98.8, 95.2],
    "f1Score": [98.2, 99.0, 95.8]
  }
}
```

---

## 9️⃣ 用户权限管理模块

### 9.1 获取用户列表
**接口：** `GET /users`

**请求参数：**
```
?page=1
&pageSize=20
&department=production  // all/production/quality/tech/admin
&search=liming
&status=active         // all/active/disabled
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "statistics": {
      "online": 12,
      "total": 45
    },
    "users": [
      {
        "id": 1,
        "name": "李明",
        "email": "liming@induscore.com",
        "phone": "138****8888",
        "department": "生产部",
        "departmentId": 1,
        "roles": ["admin", "production"],
        "status": "active",           // active/disabled
        "online": true,
        "lastLogin": "2026-02-03 14:30",
        "lastIp": "192.168.1.100",
        "createdAt": "2025-12-01",
        "loginCount": 285,
        "avatar": "https://..."
      }
    ],
    "total": 45
  }
}
```

### 9.2 新增用户
**接口：** `POST /users`

**请求参数：**
```json
{
  "name": "张三",
  "email": "zhangsan@induscore.com",
  "phone": "138****0000",
  "department": "生产部",
  "roles": ["production", "operator"],
  "password": "password123"
}
```

### 9.3 编辑用户
**接口：** `PUT /users/:id`

### 9.4 重置密码
**接口：** `POST /users/:id/reset-password`

**响应：**
```json
{
  "code": 200,
  "data": {
    "newPassword": "Aa123456"  // 返回新密码（生产环境应发送邮件）
  }
}
```

### 9.5 启用/禁用用户
**接口：** `POST /users/:id/toggle-status`

**请求参数：**
```json
{
  "status": "disabled"   // active/disabled
}
```

### 9.6 获取角色列表
**接口：** `GET /roles`

**响应数据：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "超级管理员",
      "description": "拥有系统所有权限",
      "userCount": 2,
      "permissions": ["user_manage", "role_manage", "system_config"]
    }
  ]
}
```

### 9.7 编辑角色权限
**接口：** `PUT /roles/:id`

**请求参数：**
```json
{
  "name": "超级管理员",
  "description": "拥有系统所有权限",
  "permissions": ["user_manage", "role_manage", "system_config"]
}
```

### 9.8 获取操作日志
**接口：** `GET /logs/operations`

**请求参数：**
```
?page=1
&pageSize=50
&startDate=2026-02-01
&endDate=2026-02-03
&userId=1
&type=all              // all/create/update/delete/login
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "logs": [
      {
        "id": 1,
        "time": "2026-02-03 14:32:15",
        "userId": 1,
        "userName": "李明",
        "type": "create",           // create/update/delete/login
        "content": "新增用户：王小明",
        "ip": "192.168.1.100",
        "status": "success",        // success/failure
        "userAgent": "Mozilla/5.0..."
      }
    ],
    "total": 328
  }
}
```

---

## 🔟 其他通用接口

### 10.1 获取部门列表
**接口：** `GET /departments`

**响应数据：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "生产部",
      "userCount": 18,
      "parentId": null
    }
  ]
}
```

### 10.2 文件上传
**接口：** `POST /upload`

**请求格式：** `multipart/form-data`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "url": "https://cdn.induscore.com/uploads/20260201/file001.jpg",
    "filename": "file001.jpg",
    "size": 2048576
  }
}
```

### 10.3 获取系统配置
**接口：** `GET /system/config`

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "systemName": "INDUSCORE",
    "version": "1.0.0",
    "websocketUrl": "ws://api.induscore.com/ws"
  }
}
```

---

## 🔌 WebSocket 实时通信

### 连接地址
```
ws://api.induscore.com/ws?token={jwt_token}
```

### 消息格式
```json
{
  "type": "detection",      // detection/alert/status
  "data": {
    "cameraId": "CAM-01",
    "detectionResult": {
      "type": "表面划痕",
      "confidence": 98.4,
      "position": { "x": 342, "y": 156 },
      "timestamp": "2026-02-03 14:30:05"
    }
  }
}
```

### 消息类型
- `detection` - 检测结果推送
- `alert` - 报警通知
- `status` - 设备状态更新
- `message` - 系统消息

---

## 📝 接口优先级

### P0 - 核心接口（必须实现）
- [x] 用户登录/注册
- [x] 获取检测记录（历史查询）
- [x] 获取缺陷详情
- [x] 获取数据大屏 KPI
- [x] 获取生产线列表

### P1 - 重要接口
- [x] 实时检测统计
- [x] 模型列表与部署
- [x] 用户管理 CRUD
- [x] 产品管理 CRUD

### P2 - 增强接口
- [x] 批量操作
- [x] 数据导出
- [x] WebSocket 实时推送
- [x] 操作日志

---

## 🔒 权限验证

所有需要登录的接口都需要在请求头中携带 Token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 权限级别
- `public` - 无需认证（登录、注册）
- `user` - 需要登录
- `admin` - 需要管理员权限
- `custom` - 自定义权限检查

---

## 📞 对接联系

**前端负责人：** [您的名字]  
**技术栈：** Vue 3 + Vue Router + ECharts  
**对接方式：** RESTful API + WebSocket  
**测试环境：** http://localhost:5173

---

## ⚠️ 注意事项

1. **跨域问题：** 需要后端配置 CORS
2. **Token 过期：** 建议 Token 有效期 2 小时，支持刷新
3. **文件上传：** 大文件建议使用 OSS 直传
4. **实时推送：** 优先使用 WebSocket，降级方案可用轮询
5. **分页参数：** 统一使用 page/pageSize
6. **时间格式：** 统一使用 `YYYY-MM-DD HH:mm:ss`
7. **ID 类型：** 建议使用数字型 ID
