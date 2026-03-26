# AI 模型管理 API 接口文档

## 概述

后端已完成 AI 模型管理功能的真实逻辑实现，替换原有的 Mock 数据。现在支持：
- 多模型管理（当前已部署 YOLOv11-Industrial）
- 模型版本管理
- 模型部署/下线（单模型机制：一次只能有一个模型处于部署状态）
- 模型性能指标追踪

## 核心机制：单模型部署

**重要**：系统采用「单模型部署」机制：
- 任何时候只能有一个模型处于 `deployed` 状态
- 部署新模型时，当前部署的模型会自动下线（状态变为 `active`）
- 模型被部署后自动成为「主模型」(`isMain=true`)

## API 接口列表

### 1. 获取模型列表

```
GET /v1/models?page=1&pageSize=20&status=all&architecture=all&keyword=
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "models": [
      {
        "id": 1,
        "name": "YOLOv11-Industrial",
        "code": "yolov11-industrial",
        "description": "基于YOLOv11n在NET-DET数据集训练的热轧带钢缺陷检测模型...",
        "architecture": "YOLOv11",
        "dataset": "NET-DET",
        "taskType": "hot_rolled_steel_defect",
        "status": "deployed",
        "isMain": true,
        "currentVersion": "v1.0.0",
        "currentVersionId": 1,
        "modelFilePath": "models/yolov11_industrial_v1.0.0.pt",
        "configFilePath": null,
        "createdAt": "2026-02-15T10:00:00",
        "updatedAt": "2026-02-15T10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 20
  }
}
```

### 2. 获取模型统计信息

```
GET /v1/models/statistics
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalModels": 1,
    "deployedModels": 1,
    "activeModels": 0,
    "draftModels": 0,
    "avgAccuracy": 96.5,
    "totalVersions": 1,
    "abTests": 0
  }
}
```

### 3. 获取当前部署的模型信息

```
GET /v1/models/deployed/info
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "YOLOv11-Industrial",
    "code": "yolov11-industrial",
    "version": "v1.0.0",
    "architecture": "YOLOv11",
    "dataset": "NET-DET",
    "status": "deployed",
    "accuracy": 96.5,
    "recall": 94.2,
    "f1Score": 0.953,
    "inferenceSpeed": 8.5,
    "modelSize": 5.2
  }
}
```

### 4. 获取模型详情（含版本列表）

```
GET /v1/models/{id}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "YOLOv11-Industrial",
    ...
    "versions": [
      {
        "id": 1,
        "modelId": 1,
        "version": "v1.0.0",
        "description": "YOLOv11n在NET-DET数据集上的初版训练模型",
        "status": "deployed",
        "isCurrent": true,
        "accuracy": 96.5,
        "recall": 94.2,
        "f1Score": 0.953,
        "precision": 95.8,
        "map": 92.3,
        "map50_95": 78.6,
        "inferenceSpeed": 8.5,
        "modelSizeMb": 5.2,
        "trainingDataSize": 50000,
        "trainedAt": "2026-02-15",
        "trainer": "李工",
        "trainingEpochs": 200,
        "batchSize": 16,
        "learningRate": "0.001",
        "modelFile": "models/yolov11_industrial_v1.0.0.pt",
        "deployedAt": "2026-03-11T10:00:00",
        "deployedBy": "system",
        "createdAt": "2026-02-15T10:00:00"
      }
    ],
    "currentVersionDetail": { ... }
  }
}
```

### 5. 部署模型（关键接口）

```
POST /v1/models/{id}/deploy
Content-Type: application/json

{
  "versionId": 1,
  "deployedBy": "管理员姓名"
}
```

**业务逻辑**：
- 指定版本会被标记为 `deployed` 和 `isCurrent=true`
- 模型状态变为 `deployed`，`isMain=true`
- **自动下线其他已部署的模型**（它们的 status 变为 `active`）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "deployed",
    "isMain": true,
    "currentVersion": "v1.0.0"
  }
}
```

### 6. 下线模型

```
POST /v1/models/{id}/undeploy
```

**业务逻辑**：
- 模型状态变为 `inactive`
- `isMain` 设为 `false`
- 当前版本状态变为 `ready`，`isCurrent=false`

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "inactive",
    "isMain": false
  }
}
```

### 7. 设置主模型

```
POST /v1/models/{id}/main?isMain=true
```

### 8. 创建模型版本

```
POST /v1/models/{modelId}/versions
Content-Type: application/json

{
  "version": "v1.1.0",
  "description": "改进版本",
  "accuracy": 97.0,
  "recall": 95.0,
  "f1Score": 0.96,
  "precision": 96.5,
  "map": 93.0,
  "map50_95": 80.0,
  "inferenceSpeed": 8.0,
  "modelSizeMb": 5.5,
  "trainingDataSize": 60000,
  "trainedAt": "2026-03-01",
  "trainer": "李工",
  "trainingEpochs": 250,
  "batchSize": 32,
  "learningRate": "0.0005",
  "modelFile": "models/yolov11_industrial_v1.1.0.pt"
}
```

### 9. 切换版本（同模型）

```
POST /v1/models/{modelId}/versions/{versionId}/switch
```

## 状态说明

### 模型状态 (status)
| 状态 | 含义 | 可用操作 |
|------|------|----------|
| `inactive` | 未激活 | 部署、编辑、删除 |
| `active` | 就绪待命 | 部署、编辑 |
| `deployed` | 已部署（正在质检） | 下线、切换版本 |

### 版本状态 (status)
| 状态 | 含义 |
|------|------|
| `draft` | 草稿 |
| `ready` | 就绪 |
| `deployed` | 已部署 |
| `deprecated` | 已弃用 |

## 前端页面建议

### 模型列表页
- 显示所有模型卡片
- **部署按钮**：仅对 `inactive` 或 `active` 状态的模型显示
- **下线按钮**：仅对 `deployed` 状态的模型显示
- **部署中模型高亮**：当前 `deployed` 状态的模型需要特殊样式标识

### 模型详情页
- 显示模型基本信息
- 版本列表表格
- 每个版本显示：版本号、性能指标、状态
- **部署此版本**按钮：将指定版本设为当前版本并部署

### 实时检测页
- 显示当前使用的模型信息（可调用 `/v1/models/deployed/info`）
- 显示：模型名称、版本、准确率、推理速度

## 注意事项

1. **单模型限制**：部署模型A时，如果模型B正在部署状态，B会自动下线
2. **默认回退**：如果没有部署任何模型，检测服务会使用默认值 "YOLOv11-Industrial"
3. **版本切换**：同一模型可以切换不同版本，不需要下线再部署
4. **YOLO服务集成**：部署后，检测服务会自动将模型信息传递给YOLO推理服务

## 当前已存在的数据

已预置一条模型数据供测试：
- **模型**：YOLOv11-Industrial
- **版本**：v1.0.0
- **状态**：已部署 (deployed)
- **架构**：YOLOv11
- **数据集**：NET-DET
- **任务**：hot_rolled_steel_defect

## 联系后端

如有 API 相关问题，请联系后端开发者。
