# Agent 接口契约与联调规范

## 1. 目的与适用范围

本文档定义双端共用 AI Agent 的接口契约，用于支持后端、PC、Android 并行开发。  
本契约服务于 `android_docs/17_双端AI_Agent设计与实施方案_LangChain4j.md` 的一期目标（只读工具 Agent）。

适用对象：
- 后端（Spring Boot + LangChain4j）
- PC 前端
- Android 客户端
- 测试与联调

---

## 2. 通用约定

## 2.1 Base URL
- 本地：`http://localhost:8000/`
- 统一前缀：`/v1/agent`

## 2.2 鉴权与头信息

所有 Agent 接口必须携带：
- `Authorization: Bearer <jwt>`
- `X-Client-Type: pc|android|mobile`
- `Content-Type: application/json`（SSE 接口请求体仍是 JSON）

可选头：
- `X-Request-Id: <uuid>`（客户端生成，便于全链路追踪）

## 2.3 时间与字符集
- 时间统一 ISO-8601，UTC 或带时区偏移。
- 文本统一 UTF-8。

## 2.4 响应 envelope（非 SSE）

与现有移动端风格对齐，统一使用：

```json
{
  "code": 200,
  "message": "ok",
  "data": {}
}
```

---

## 3. 接口总览

| 接口 | 方法 | 用途 | 返回类型 |
|------|------|------|----------|
| `/v1/agent/chat` | POST | 同步问答 | JSON envelope |
| `/v1/agent/chat/stream` | POST | 流式问答 | `text/event-stream` |
| `/v1/agent/sessions` | GET | 会话列表 | JSON envelope |
| `/v1/agent/sessions/{sessionId}` | GET | 会话详情（含消息） | JSON envelope |
| `/v1/agent/sessions/{sessionId}` | DELETE | 结束/归档会话 | JSON envelope |

说明：
- 会话接口建议一期一起实现，便于双端历史会话管理。
- 若排期紧，可先实现前两个接口，后面三个可在一周内补齐。

---

## 4. 数据模型（契约）

## 4.1 Message

```json
{
  "role": "system|user|assistant|tool",
  "content": "string",
  "timestamp": "2026-03-22T12:00:00+08:00"
}
```

## 4.2 ChatRequest

```json
{
  "sessionId": "ag_s_20260322_xxx",
  "messages": [
    {"role": "user", "content": "请汇总今天待复核和严重告警"}
  ],
  "context": {
    "scene": "workbench",
    "page": "android/workbench",
    "taskId": 123,
    "idempotencyKey": "MOB-xxx",
    "timeRange": "today"
  },
  "options": {
    "stream": false,
    "maxTokens": 1024,
    "temperature": 0.2
  }
}
```

字段说明：
- `sessionId`：可空；为空时服务端新建会话并返回。
- `messages`：至少 1 条，最后一条必须为 `user`。
- `context.scene`：建议枚举：
  - `home`
  - `workbench`
  - `review_list`
  - `review_detail`
  - `upload`
  - `upload_status`
  - `notifications`
  - `metrics`
- `options`：可空；服务端可做安全截断。

## 4.3 ChatResponseData（同步）

```json
{
  "sessionId": "ag_s_20260322_xxx",
  "answer": "string",
  "toolCalls": [
    {
      "tool": "getMobileWorkbench",
      "success": true,
      "latencyMs": 58
    }
  ],
  "traceId": "tr_20260322_xxx",
  "usage": {
    "promptTokens": 420,
    "completionTokens": 180,
    "totalTokens": 600
  }
}
```

## 4.4 SessionSummary

```json
{
  "sessionId": "ag_s_20260322_xxx",
  "title": "工作台告警解读",
  "lastMessageAt": "2026-03-22T12:10:00+08:00",
  "clientType": "android"
}
```

---

## 5. 同步接口契约

## 5.1 POST `/v1/agent/chat`

请求：`ChatRequest`  
响应：`ApiEnvelope<ChatResponseData>`

成功示例：
```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "sessionId": "ag_s_20260322_abc",
    "answer": "今日待复核 12 条，严重告警 3 条，建议优先处理 A 产线任务。",
    "toolCalls": [
      {"tool": "getMobileWorkbench", "success": true, "latencyMs": 43},
      {"tool": "getMobileNotifications", "success": true, "latencyMs": 66}
    ],
    "traceId": "tr_abc",
    "usage": {"promptTokens": 312, "completionTokens": 126, "totalTokens": 438}
  }
}
```

---

## 6. 流式接口契约（SSE）

## 6.1 POST `/v1/agent/chat/stream`

请求：`ChatRequest`  
响应头：`Content-Type: text/event-stream`

事件流格式：

```
event: meta
data: {"sessionId":"ag_s_xxx","traceId":"tr_xxx"}

event: tool
data: {"phase":"start","tool":"getMobileWorkbench"}

event: tool
data: {"phase":"end","tool":"getMobileWorkbench","success":true,"latencyMs":51}

event: chunk
data: {"text":"今日待复核"}

event: chunk
data: {"text":" 12 条，严重告警 3 条。"}

event: done
data: {"usage":{"promptTokens":300,"completionTokens":120,"totalTokens":420}}
```

错误事件：

```
event: error
data: {"code":"AGENT_MODEL_TIMEOUT","message":"模型响应超时，请稍后重试","traceId":"tr_xxx"}
```

SSE 客户端处理要求：
- PC/Android 均需增量拼接 `chunk.text`。
- 收到 `done` 或 `error` 后关闭流。
- 超时/断线允许客户端重试一次（指数退避）。

---

## 7. 会话管理接口

## 7.1 GET `/v1/agent/sessions`

查询参数：
- `page`（默认 1）
- `pageSize`（默认 20，最大 100）

响应：
```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "items": [
      {
        "sessionId": "ag_s_xxx",
        "title": "上传失败原因分析",
        "lastMessageAt": "2026-03-22T12:10:00+08:00",
        "clientType": "android"
      }
    ],
    "total": 16,
    "page": 1,
    "pageSize": 20
  }
}
```

## 7.2 GET `/v1/agent/sessions/{sessionId}`

响应：
```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "sessionId": "ag_s_xxx",
    "title": "上传失败原因分析",
    "messages": [
      {"role":"user","content":"为什么上传失败？","timestamp":"2026-03-22T12:00:00+08:00"},
      {"role":"assistant","content":"网络状态为蜂窝且超过阈值...","timestamp":"2026-03-22T12:00:01+08:00"}
    ]
  }
}
```

## 7.3 DELETE `/v1/agent/sessions/{sessionId}`

语义：软删除/归档当前用户会话。  
响应：
```json
{
  "code": 200,
  "message": "ok",
  "data": {"sessionId":"ag_s_xxx","status":"archived"}
}
```

---

## 8. 错误码规范

| HTTP | code | 含义 | 客户端处理 |
|------|------|------|-----------|
| 400 | `AGENT_INVALID_ARGUMENT` | 参数错误 | 前端提示并停留当前页面 |
| 401 | `UNAUTHORIZED` | token 失效 | 走现有登录失效流程 |
| 403 | `AGENT_NO_PERMISSION` | 无权限访问工具/数据 | 提示无权限，不重试 |
| 404 | `AGENT_SESSION_NOT_FOUND` | 会话不存在 | 刷新会话列表 |
| 408/504 | `AGENT_MODEL_TIMEOUT` | 模型超时 | 建议用户重试 |
| 429 | `AGENT_RATE_LIMITED` | 限流 | 提示稍后重试 |
| 502/503 | `AGENT_UPSTREAM_UNAVAILABLE` | 模型或上游服务不可用 | 提示降级文案 |
| 500 | `AGENT_INTERNAL_ERROR` | 未知错误 | 提示通用错误 + traceId |

响应示例：
```json
{
  "code": 503,
  "message": "Agent upstream unavailable",
  "data": {
    "errorCode": "AGENT_UPSTREAM_UNAVAILABLE",
    "traceId": "tr_xxx"
  }
}
```

---

## 9. 工具调用白名单（一期）

后端只允许以下工具在 Agent 中被调用：
- `getMobileWorkbench`
- `getMobileReviewTasks`
- `getMobileTaskDetail`
- `getMobileUploadStatus`
- `getMobileNotifications`
- `getMobilePilotMetrics`
- `getDashboardKpi`
- `getDashboardAlerts`
- `getDefectDetail`
- `queryRecords`

规则：
- 工具名与参数都做白名单校验；
- `clientType=android` 时禁止调用管理域写工具；
- 工具失败不等于会话失败，允许部分降级回答。

---

## 10. 并发、超时、限流

建议默认值：
- 单请求最大工具调用数：3
- 单工具超时：3s
- 模型总超时：12s
- 用户级限流：10 req/min
- SSE 空闲超时：30s

客户端建议：
- SSE 失败后仅自动重试 1 次；
- 重试间隔 1s -> 2s（指数退避）。

---

## 11. 安全与审计要求

- 日志必须记录 `traceId`、`sessionId`、`userId`、`clientType`。
- 对用户输入做脱敏（手机号、邮箱、token、路径）。
- 禁止将完整敏感原文写入持久化日志。
- 所有工具调用必须落 `agent_tool_call_log`。

---

## 12. 前后端并行开发清单

## 12.1 后端先行（M1）
- [ ] `POST /chat` 可用
- [ ] `POST /chat/stream` 可用（SSE 事件符合本文）
- [ ] 错误码按本文返回
- [ ] traceId 贯通并回传

## 12.2 PC 前端并行（M2）
- [ ] 同步模式可用
- [ ] SSE 增量渲染可用
- [ ] 错误提示与重试策略落地
- [ ] 会话列表与详情可用

## 12.3 Android 并行（M2）
- [ ] `AgentApiService` + `ViewModel` 打通
- [ ] `UiState` 适配流式增量文本
- [ ] 主业务按钮不被 Agent 阻塞
- [ ] scene/context 按页面传递

---

## 13. 联调验收用例（最小集）

1. Android `workbench` 场景提问，返回待复核与告警摘要。  
2. Android `upload_status` 场景提问，返回失败原因与建议。  
3. PC 看板场景提问，返回 KPI 与趋势摘要。  
4. 无权限用户请求管理数据，返回 403 + `AGENT_NO_PERMISSION`。  
5. 模型超时触发，返回 `AGENT_MODEL_TIMEOUT`，客户端可恢复。  
6. SSE 正常结束返回 `done`，token usage 回传。  

---

## 14. 版本策略

- 本契约版本：`v1.0`
- 兼容策略：
  - 新增字段只增不删；
  - 枚举新增必须兼容旧客户端；
  - 破坏性变更需新增 `/v2/agent/*`。

---

## 15. 参考文档

- `android_docs/17_双端AI_Agent设计与实施方案_LangChain4j.md`
- `android_docs/03_接口映射与联调规范.md`
- `android-app/app/src/main/java/com/induscore/mobile/data/remote/MobileApiService.kt`
- `src/main/java/com/induscore/security/RequestAuthorizationService.java`
