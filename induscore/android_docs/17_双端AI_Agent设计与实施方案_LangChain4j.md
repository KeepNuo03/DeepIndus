# 双端 AI Agent 设计与实施方案（LangChain4j）

## 1. 文档目标

本方案用于指导在现有 INDUSCORE 系统中落地一个可在 **PC 端 + Android 端** 共用的业务型 AI Agent。  
目标不是“通用聊天”，而是围绕质检业务提供可控、可审计、可复用的助手能力。

本文档达到的工程粒度：
- 后端工程师可据此直接创建模块、接口、数据表并开发；
- PC 与 Android 工程师可据此直接接入会话 API；
- 测试可据此编写联调、权限、回归与压测用例。

---

## 2. 当前业务基线（来自现有仓库）

### 2.1 核心业务能力（已存在）
- 认证与权限：JWT + 角色/权限 + `X-Client-Type` 端隔离。
- 检测与缺陷：实时检测、缺陷详情、人工复核。
- 记录与看板：历史查询、KPI、趋势、告警。
- 移动端作业：工作台、复核任务、抽检上传、上传状态、通知、试点指标。

### 2.2 与 Agent 相关的重要现实约束
- 移动端与 PC 端能力边界已在后端授权层定义（`/v1/mobile/**` 与管理接口隔离）。
- 部分管理接口/用户列表在文档中存在 Mock 历史，Agent 一期不依赖这些不稳定能力。
- 现有上传状态幂等会话有内存态实现，Agent 一期不参与上传写入主链路，避免放大风险。

---

## 3. 一期目标、边界与价值

## 3.1 一期目标（必须达成）
- 提供统一对话入口，PC/Android 共享同一后端 Agent 服务。
- 支持只读业务工具调用：查任务、查上传状态、查通知、查看板指标、查缺陷详情。
- 支持流式回答（SSE），并具备审计追踪、权限校验、超时控制。

## 3.2 一期不做（明确排除）
- 不做自动写操作（如自动复核提交、批量删除、模型部署）。
- 不改动既有主链路（复核、上传、上传状态）交互路径。
- 不引入复杂自治代理（多 Agent 自主规划）和高风险代码执行工具。

## 3.3 业务价值
- 一线人员：快速解释异常、给下一步建议，减少跨页查找成本。
- 管理人员：按自然语言汇总 KPI 与告警，提升决策速度。
- 运维/测试：统一问题问答入口，减少“接口口径不一致”沟通损耗。

---

## 4. 技术栈选择

## 4.1 后端 Agent 编排框架
- 选型：`LangChain4j`
- 原因：
  - 与现有 Spring Boot 主栈一致，团队迁移成本低；
  - Tool Calling 与结构化输出能力适合业务 Agent；
  - 可在 Java 中保持类型安全和分层设计。

## 4.2 模型接入策略
- 采用 Provider 抽象层，避免锁定单一模型厂商。
- 统一接口：`ModelGateway`，底层可切换 OpenAI/阿里/智谱/本地部署。

## 4.3 会话与缓存
- 短会话记忆：Redis（推荐）或 MySQL（低并发可先用）。
- 审计与会话元信息：MySQL。

## 4.4 双端通信协议
- 对话主协议：HTTP + SSE（流式）。
- 兜底协议：HTTP JSON（非流式）。

---

## 5. 总体架构

```mermaid
flowchart LR
    PC[PC Web] --> API[AgentController]
    AND[Android] --> API
    API --> AUTH[JWT + X-Client-Type + Role Guard]
    AUTH --> ORCH[AgentOrchestrator]
    ORCH --> AI[AiService (LangChain4j)]
    AI --> MODEL[ModelGateway]
    AI --> TOOLS[ToolRegistry]
    TOOLS --> BIZ[现有业务服务/控制器能力]
    ORCH --> MEM[Session Memory]
    ORCH --> AUDIT[Audit Logger]
    BIZ --> DB[(MySQL)]
```

设计原则：
- Agent 是“旁路增强”，不是“主链路入口”；
- 任何工具调用都先过权限网关；
- 所有行为可追踪可回放（审计日志）。

---

## 6. 后端详细设计（可直接开工）

## 6.1 包结构建议

在后端新增包：

- `com.induscore.agent.controller`
- `com.induscore.agent.service`
- `com.induscore.agent.llm`
- `com.induscore.agent.tool`
- `com.induscore.agent.policy`
- `com.induscore.agent.memory`
- `com.induscore.agent.audit`
- `com.induscore.agent.dto`

## 6.2 核心类清单

- `AgentController`
  - `POST /v1/agent/chat`（同步）
  - `POST /v1/agent/chat/stream`（SSE）
- `AgentOrchestrator`
  - 会话装配、Prompt 组装、调用 AiService、工具执行编排
- `QualityAssistantService`（LangChain4j AiService 接口）
- `ModelGateway` + `ProviderAdapter`
  - 屏蔽不同模型 SDK 差异
- `ToolRegistry`
  - 统一注册工具及元数据
- `PolicyGuard`
  - 工具调用前权限、参数、风险校验
- `AgentAuditService`
  - 会话审计、工具审计、错误审计
- `SessionMemoryStore`
  - 会话上下文读写

## 6.3 API 设计（一期）

### 6.3.1 同步对话
`POST /v1/agent/chat`

请求：
```json
{
  "sessionId": "optional-session-id",
  "messages": [
    {"role": "user", "content": "请汇总今天待复核任务和严重告警"}
  ],
  "context": {
    "scene": "workbench",
    "clientType": "android"
  }
}
```

响应：
```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "sessionId": "ag_s_20260322_xxx",
    "answer": "今日待复核 12 条，严重告警 3 条，建议优先处理...",
    "toolCalls": [
      {"tool": "getMobileWorkbench", "success": true},
      {"tool": "getMobileNotifications", "success": true}
    ],
    "traceId": "trace_xxx"
  }
}
```

### 6.3.2 流式对话
`POST /v1/agent/chat/stream`（`text/event-stream`）

事件约定：
- `event: chunk`：增量文本
- `event: tool`：工具调用开始/结束
- `event: done`：结束
- `event: error`：失败

## 6.4 工具设计（一期只读）

仅开放以下只读工具，避免写操作风险：

1. `getMobileWorkbench`
   - 对应：`GET /v1/mobile/workbench`
2. `getMobileReviewTasks`
   - 对应：`GET /v1/mobile/tasks/review`
3. `getMobileTaskDetail`
   - 对应：`GET /v1/mobile/tasks/review/{id}`
4. `getMobileUploadStatus`
   - 对应：`GET /v1/mobile/uploads/{idempotencyKey}`
5. `getMobileNotifications`
   - 对应：`GET /v1/mobile/notifications`
6. `getMobilePilotMetrics`
   - 对应：`GET /v1/mobile/pilot/metrics`
7. `getDashboardKpi`
   - 对应：`GET /v1/dashboard/kpi`
8. `getDashboardAlerts`
   - 对应：`GET /v1/dashboard/alerts`
9. `getDefectDetail`
   - 对应：`GET /v1/defect/{id}`
10. `queryRecords`
    - 对应：`GET /v1/records/query`

工具输入必须定义 DTO + Bean Validation（如 `@NotNull`、`@Min`）。

## 6.5 权限与策略

Agent 侧权限策略：
- 复用现有 JWT 解析与角色/权限；
- 复用 `X-Client-Type` 端能力约束；
- 工具执行前再做一次策略校验（双保险）：
  - `clientType=android` 禁止调用 PC 管理工具；
  - 角色不满足时直接拒绝工具执行并返回可读提示；
  - 工具参数越界/非法时拒绝并审计。

## 6.6 Prompt 设计

拆分三层 Prompt：
- `System Prompt`：角色定位、事实边界、禁编造、单位格式、输出风格。
- `Policy Prompt`：禁止越权、禁止执行未授权动作、引用工具结果优先。
- `Scene Prompt`：按页面场景补充上下文（`workbench/upload_status/task_detail`）。

关键规则：
- 无工具结果不下结论，必须声明“数据不足”；
- 返回建议必须可执行（步骤化，不空泛）；
- 对风险建议必须加“需人工确认”。

## 6.7 会话与记忆策略

一期建议：
- 会话窗口记忆：保留最近 20 轮；
- 会话过期：24 小时；
- 不把敏感字段（token、手机号）写入长期记忆。

会话键：
- `sessionId` + `userId` + `clientType`

## 6.8 审计与可观测性

每次请求记录：
- `traceId`、`sessionId`、`userId`、`clientType`
- 用户输入摘要（脱敏）
- 模型调用耗时、token 数
- 工具调用列表（成功/失败、耗时）
- 最终响应状态码与错误码

日志与指标：
- 日志：JSON 结构化日志；
- 指标：QPS、P95、工具失败率、超时率、平均 token 成本。

## 6.9 数据库设计（新增表）

### 6.9.1 会话主表
```sql
CREATE TABLE IF NOT EXISTS agent_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  client_type VARCHAR(32) NOT NULL,
  title VARCHAR(128) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 6.9.2 消息表
```sql
CREATE TABLE IF NOT EXISTS agent_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL,
  role VARCHAR(16) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  token_count INT NULL,
  trace_id VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_agent_message_session_created (session_id, created_at)
);
```

### 6.9.3 工具调用审计
```sql
CREATE TABLE IF NOT EXISTS agent_tool_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL,
  trace_id VARCHAR(64) NOT NULL,
  tool_name VARCHAR(64) NOT NULL,
  input_json JSON NULL,
  output_json JSON NULL,
  success TINYINT(1) NOT NULL,
  error_code VARCHAR(64) NULL,
  latency_ms INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_agent_tool_trace (trace_id),
  INDEX idx_agent_tool_session (session_id, created_at)
);
```

### 6.9.4 会话级审计
```sql
CREATE TABLE IF NOT EXISTS agent_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  trace_id VARCHAR(64) NOT NULL UNIQUE,
  session_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  client_type VARCHAR(32) NOT NULL,
  request_summary VARCHAR(512) NULL,
  response_status VARCHAR(32) NOT NULL,
  model_name VARCHAR(64) NULL,
  model_latency_ms INT NULL,
  prompt_tokens INT NULL,
  completion_tokens INT NULL,
  total_tokens INT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. PC 端接入设计

## 7.1 交互形态
- 全局入口：右下角助手按钮（不打断现有页面）。
- 场景入口：
  - 历史查询页：可将筛选条件传入 `context`；
  - 缺陷详情页：可将 defectId 传入 `context`；
  - 看板页：可将时间范围传入 `context`。

## 7.2 前端协议要求
- 优先 SSE 流式展示；
- 每次请求携带当前 JWT；
- `context.scene` 与 `context.pageParams` 必传，用于场景化回答。

---

## 8. Android 端接入设计

## 8.1 入口位置建议
- 一级入口：系统主页新增“智能助手”模块；
- 二级入口：
  - `UploadStatusScreen`：失败项处置建议；
  - `ReviewTaskDetailScreen`：复核说明建议；
  - `SamplingUploadScreen`：检测结果解读。

## 8.2 Android 实现约束（基于现有代码）
- 沿用 `ServiceLocator` 新增 `AgentApiService`；
- 沿用 `ViewModel + StateFlow + UiState`；
- Agent 失败不可阻断主功能按钮（上传/复核必须可继续）。

## 8.3 Android 请求头
- 继承现有 `AuthInterceptor`：
  - `Authorization: Bearer <token>`
  - `X-Client-Type: android`

---

## 9. 安全与合规设计

强制规则：
- 工具白名单（禁止动态任意函数调用）；
- 参数白名单（枚举值、分页上限）；
- 统一脱敏（手机号、邮箱、token、路径）；
- 写操作二期前全部禁用；
- 拒绝回答越权数据请求并返回标准错误语义。

建议新增错误码：
- `AGENT_NO_PERMISSION`
- `AGENT_TOOL_NOT_ALLOWED`
- `AGENT_INVALID_ARGUMENT`
- `AGENT_MODEL_TIMEOUT`
- `AGENT_UPSTREAM_UNAVAILABLE`

---

## 10. 性能、稳定性、成本

## 10.1 性能目标（一期）
- Agent 首字返回（TTFB）P95 < 2.5s
- 完整响应 P95 < 8s
- 工具调用超时默认 2s~3s，单请求最多串行 3 个工具

## 10.2 保护策略
- 全局并发上限与用户级限流（如 10 req/min/user）；
- 模型超时后快速失败并给兜底话术；
- 工具失败可部分降级，优先返回可用结论。

## 10.3 成本策略
- 每次对话最大 token 限制；
- 长对话自动摘要压缩；
- 高成本模型仅对管理角色开放。

---

## 11. 测试与验收

## 11.1 单元测试
- 工具参数校验、权限拦截、Prompt 构建、错误映射。

## 11.2 集成测试
- `AgentController` 对 10 个只读工具的成功路径与拒绝路径；
- `clientType=android` 越权访问管理工具必须 403。

## 11.3 端到端测试
- PC：看板页发问 -> 工具调用 -> 结果展示；
- Android：上传状态页发问 -> 建议返回；
- 网络抖动/模型不可用时，主业务链路不受影响。

## 11.4 上线门槛
- 零越权；
- P95 达标；
- 审计日志完整率 100%；
- 回归不影响既有移动端试点指标口径。

---

## 12. 实施计划（建议 4 周）

第 1 周：
- 建 Agent 模块骨架、接入模型、实现同步对话、接审计表。

第 2 周：
- 上线 SSE 流式；
- 打通 10 个只读工具；
- 完成策略与权限守卫。

第 3 周：
- PC/Android 双端接入最小可用入口；
- 完成联调与异常兜底。

第 4 周：
- 压测、灰度、问题收敛；
- 输出试点复盘报告与二期写操作设计草案。

---

## 13. 二期路线（写操作）

仅在以下前提满足后开启：
- 审计链路与 operator 字段可追溯到真实用户；
- 二次确认交互在双端完成；
- 风险动作（复核提交、批量删除、模型部署）具备回滚机制。

二期可开放：
- `submitReviewSuggestion`（先草稿后人工确认）
- `createFollowupTask`（需权限）
- `exportSummaryReport`（带权限与限频）

---

## 14. 关键工程检查清单（开发前）

- [ ] 已引入 LangChain4j 依赖并封装 `ModelGateway`
- [ ] 已新增 4 张 Agent 表并迁移
- [ ] 已实现 `AgentController` 同步 + SSE
- [ ] 已实现 ToolRegistry 与 10 个只读工具
- [ ] 已实现 PolicyGuard（角色/端类型/参数）
- [ ] 已实现审计日志与 traceId 贯通
- [ ] PC/Android 都能发起会话并显示流式响应
- [ ] 主链路回归通过（复核、上传、上传状态）

---

## 15. 参考实现锚点（现有代码）

- 移动端 API 定义：`android-app/app/src/main/java/com/induscore/mobile/data/remote/MobileApiService.kt`
- 移动端状态管理：`android-app/app/src/main/java/com/induscore/mobile/ui/state/UiState.kt`
- 移动端 DI：`android-app/app/src/main/java/com/induscore/mobile/di/ServiceLocator.kt`
- 授权策略：`src/main/java/com/induscore/security/RequestAuthorizationService.java`
- 服务配置：`src/main/resources/application.properties`

本方案落地时，必须遵循现有端隔离策略，不得绕过 `X-Client-Type` 与角色权限边界。
