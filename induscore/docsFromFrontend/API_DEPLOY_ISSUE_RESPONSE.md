# 后端回复：模型部署接口问题排查

## 问题原因

**前端调用方式不正确**。

后端接口期望：
- 路径参数 `{id}` = **模型ID**（ai_models表的id）
- 版本ID在 **请求体（Request Body）** 中传递

## 正确的调用方式

### POST /v1/models/{modelId}/deploy

**请求格式**：
```http
POST /v1/models/1/deploy
Content-Type: application/json

{
  "versionId": 1,
  "deployedBy": "管理员姓名"
}
```

**参数说明**：
| 参数位置 | 参数名 | 类型 | 必填 | 说明 |
|---------|-------|------|------|------|
| 路径参数 | id | Long | 是 | 模型ID（ai_models表的id） |
| 请求体 | versionId | Long | 是 | 版本ID（model_versions表的id） |
| 请求体 | deployedBy | String | 否 | 部署人姓名 |

## 前端三种尝试的问题分析

### ❌ 方式1：只有路径参数
```
POST /v1/models/1/deploy
（无请求体）
```
**错误**：缺少请求体，versionId为空

### ❌ 方式2：查询参数
```
POST /v1/models/1/deploy?versionId=1
```
**错误**：后端不从查询参数读取versionId

### ❌ 方式3：误解id含义
```
POST /v1/models/1/deploy  （把1当成versionId）
```
**错误**：路径的1是模型ID，不是版本ID

## 当前数据对照

根据数据库：
- **模型** YOLOv11-Industrial：id = 1
- **版本** v1.0.0：id = 1（model_versions表）

所以正确的调用：
```http
POST /v1/models/1/deploy
Content-Type: application/json

{
  "versionId": 1
}
```

## 关键代码确认

### Controller 层
```java
@PostMapping("/{id}/deploy")
public ApiResponse<ModelResponse> deployModel(
        @PathVariable Long id,                    // 模型ID
        @Valid @RequestBody DeployModelRequest request   // 请求体
) {
    // request.versionId() 获取版本ID
}
```

### DTO 定义
```java
public record DeployModelRequest(
        @NotNull(message = "版本ID不能为空")
        Long versionId,
        String deployedBy
) {}
```

## 前端代码示例（JavaScript/Fetch）

```javascript
// 部署模型
async function deployModel(modelId, versionId) {
  const response = await fetch(`/v1/models/${modelId}/deploy`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',  // 必须设置！
    },
    body: JSON.stringify({
      versionId: versionId,                // 版本ID在请求体中
      deployedBy: '管理员'                 // 可选
    })
  });
  return response.json();
}

// 使用示例
await deployModel(1, 1);  // 部署模型1的版本1
```

## 检查清单

前端请确认：
- [ ] 使用 POST 方法
- [ ] URL 格式正确：`/v1/models/{模型ID}/deploy`
- [ ] 设置请求头：`Content-Type: application/json`
- [ ] 请求体是有效的 JSON 对象
- [ ] 请求体包含 `versionId` 字段（数字类型）

## 其他接口规范

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 部署模型 | POST | `/v1/models/{modelId}/deploy` | 请求体传versionId |
| 下线模型 | POST | `/v1/models/{modelId}/undeploy` | 无请求体 |
| 切换版本 | POST | `/v1/models/{modelId}/versions/{versionId}/switch` | 路径传versionId |

---

**如有其他问题请联系后端**
