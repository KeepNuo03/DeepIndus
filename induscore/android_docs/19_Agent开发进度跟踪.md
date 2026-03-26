# Agent 开发进度跟踪

## 目标
- 跟踪双端 AI Agent（后端 + Android）研发进度。
- 作为每日开发记录与里程碑验收依据。
- 与 `17_双端AI_Agent设计与实施方案_LangChain4j.md`、`18_Agent接口契约与联调规范.md` 保持一致。

## 当前状态总览
- 当前阶段：**Phase A 已完成，Phase A+（提示词稳定化与最小摘要记忆）已完成**
- 当前日期：2026-03-25
- 负责人：
  - 后端 + Android Agent：AI 开发助手
  - PC UI：前端工程师同事

## 里程碑看板

### M1：后端骨架（已完成）
- [x] 新增 Agent 控制器与基础路由
  - `POST /v1/agent/chat`
  - `POST /v1/agent/chat/stream`
  - `GET /v1/agent/sessions`
  - `GET /v1/agent/sessions/{sessionId}`
  - `DELETE /v1/agent/sessions/{sessionId}`
- [x] 新增 Agent DTO 契约（请求/响应/会话结构）
- [x] 新增 AgentService 接口与实现骨架
- [x] 新增编排接口与默认占位实现（联调模式）
- [x] 新增审计骨架日志
- [x] Maven 编译通过（`mvn -DskipTests compile`）

### M2：LangChain4j 接入（进行中）
- [x] 引入 LangChain4j 依赖与基础配置
- [x] 新建 `ModelGateway` 与 Provider 抽象
- [x] 用 LangChain4j 实现 `AgentOrchestrator`
- [x] 接入首批只读工具（workbench / notifications / upload status）
- [x] 工具调用日志与 traceId 打通
- [x] `/chat` 与 `/chat/stream` 返回模型驱动结果（未配置 API Key 时自动降级联调文案）

### M3：Android 端接入（进行中）
- [x] 首页增加 Agent 双入口（右上角全局入口 + 主页卡片入口）
- [x] 主页 Agent 卡片按现有模块卡片风格落地（含右上角小 logo）
- [x] 新增 `AgentChatScreen` 与 `AgentChatViewModel`（同步问答首版）
- [x] 新增 Android 端 Agent DTO 与 API 接口定义（`/v1/agent/chat`、会话查询/归档）
- [x] 升级为 SSE 流式渲染（`/v1/agent/chat/stream`，chunk 增量展示 + done/error 收口）
- [x] 上传状态页接入场景化问答（一键提问，自动注入 `upload_status + idempotencyKey`）
- [x] 复核详情页接入场景化问答（一键提问，自动注入 `review_detail + taskId`）
- [x] 会话列表分页 + 会话切换 + 会话归档（Android 端首版）
- [ ] 错误处理与重试机制收口到现有 `UiState`

### M4：联调与验收（待开始）
- [ ] 后端 + Android 联调通过（同步与流式）
- [ ] 权限场景验证（401/403/越权拦截）
- [ ] 稳定性验证（超时、断流、限流）
- [ ] 回归验证不影响现有主链路（上传/复核/通知）
- [ ] 试点验收记录沉淀

### M5：Phase A 稳定性与容量治理（已完成）
- [x] 模型指数退避重试（overloaded/timeout/429/503 可重试）
- [x] 可选 fallback 模型自动降级（主模型失败时切换）
- [x] 用户级并发闸门（防止同账号并发请求打爆上游）
- [x] 会话消息分页查询（避免长会话一次性全量拉取）
- [x] 消息长度上限（避免单条超长内容持续膨胀）
- [x] 模型上下文窗口（仅最近 N 条进入 Prompt）
- [x] 归档会话定时清理任务（可配置开关，默认关闭）

### M6：Phase A+ Prompt/Memory 最小升级（已完成）
- [x] `toolData` 从 `Map.toString()` 升级为稳定 JSON 序列化
- [x] 会话新增最小摘要记忆（超过阈值后自动维护摘要）
- [x] Prompt 注入“历史摘要 + 最近窗口”，兼顾语义连续性与 token 控制
- [x] 新增摘要相关配置项与手动 SQL 增量脚本

## 今日完成记录

### 2026-03-23
- 完成 Agent 后端骨架代码落地：
  - `src/main/java/com/induscore/controller/AgentController.java`
  - `src/main/java/com/induscore/dto/agent/AgentDtos.java`
  - `src/main/java/com/induscore/service/AgentService.java`
  - `src/main/java/com/induscore/service/impl/AgentServiceImpl.java`
  - `src/main/java/com/induscore/service/agent/AgentOrchestrator.java`
  - `src/main/java/com/induscore/service/agent/DefaultAgentOrchestrator.java`
  - `src/main/java/com/induscore/service/agent/AgentAuditService.java`
- 完成设计与契约文档：
  - `android_docs/17_双端AI_Agent设计与实施方案_LangChain4j.md`
  - `android_docs/18_Agent接口契约与联调规范.md`
- 完成 M2 第一轮接入：
  - `pom.xml` 引入 `langchain4j` 与 `langchain4j-open-ai`
  - 新增 `ModelGateway` 与 `LangChain4jModelGateway`
  - `DefaultAgentOrchestrator` 改为“工具数据 + 模型生成”路径
  - 新增 `AgentToolService`（workbench / notifications / upload status）
  - `application.properties` 新增 `agent.model.*` 配置项

### 2026-03-25
- 完成 Phase A 全量后端改造：
  - `LangChain4jModelGateway` 新增重试与 fallback 能力
  - `AgentServiceImpl` 新增上下文窗口、消息截断、会话详情分页
  - `AgentConcurrencyGuard` 新增用户级并发闸门
  - `AgentSessionRetentionJob` 新增归档会话保留期清理
  - `application.properties` 新增 `agent.model.retry.*`、`agent.model.fallback.*`、`agent.chat.*` 配置
- SSE 错误态保持 `error + done` 对称收口，避免前端“思考中”悬挂。
- 完成 Phase B 第一批（检测记录 + 数据大屏）：
  - `AgentToolService` 新增检测记录与数据大屏工具方法
  - `DefaultAgentOrchestrator` 新增 scene 路由与关键词兜底策略
  - `AgentServiceImpl` 新增 `detection_records`、`dashboard` scene 支持
- 完成 Phase B 第二批（生产线 + AI 模型管理）：
  - `AgentToolService` 新增生产线与模型管理工具方法
  - `DefaultAgentOrchestrator` 新增 `production_line`、`model_mgmt` 路由与兜底策略
  - `AgentServiceImpl` 新增 `production_line/line_overview/model_mgmt/model_management` scene 支持
- 完成跨模块验收基线：
  - 新增验收文档 `android_docs/21_Agent跨模块验收题集与自动回归.md`
  - 新增自动回归脚本 `scripts/agent-regression.ps1`
- 完成 Phase A+ 最小升级：
  - `DefaultAgentOrchestrator`：工具数据统一 JSON 化，减少 prompt 结构波动
  - `AgentServiceImpl`：新增“旧消息摘要 + 最近窗口”混合记忆路径
  - `AgentSession`：新增 `summaryText/summaryUpdatedAt` 持久化字段
  - `application.properties`：新增 `agent.chat.summary.*` 配置
  - 新增手动 SQL：`agent_p0_1_summary_memory_manual.sql`
- 启动 Android Agent 第一批落地：
  - 系统主页新增右上角全局 Agent 入口（品牌 logo）
  - 系统主页新增 Agent 模块卡片入口（保持与现有模块视觉一致）
  - 新增 `AgentBrandLogoBadge` 组件，统一入口与卡片小图标
  - 新增 `AgentChatScreen`、`AgentChatViewModel`（同步对话首版）
  - 新增 Agent 移动端契约与仓储能力（`MobileApiService`、`MobileRepository` 扩展）
- 完成 Android Agent 第二批（流式渲染）：
  - `MobileApiService` 新增 `streamChatWithAgent`（SSE）
  - `MobileRepository` 新增 SSE 事件解析（meta/tool/chunk/done/error）
  - `AgentChatViewModel` 改为流式优先，失败自动降级到同步接口
- 完成 Android Agent 第三批（场景化接入）：
  - `UploadStatusScreen` 新增“一键提问 Agent（分析上传状态）”
  - `ReviewTaskDetailScreen` 新增“一键提问 Agent（分析当前任务）”
  - `InduscoreMobileApp` 与 `AgentChatScreen` 支持路由参数透传与自动首问
  - `AgentChatViewModel` 注入 `scene/taskId/idempotencyKey/pageParams`
- 完成 Android Agent 第四批（会话管理）：
  - `AgentChatViewModel` 新增会话分页加载、会话切换、当前会话归档能力
  - `AgentChatScreen` 新增会话管理区（刷新、加载更多、新建、切换、归档）
  - 会话管理与对话发送状态联动，避免并发状态冲突

## 风险与阻塞
- 当前无阻塞。
- 待确认事项：
  - 模型 Provider 首选方案（用于 LangChain4j 接入）
  - 会话存储一期是否先内存/Redis/MySQL

## 下一步（当前计划）
- 进入 Android Agent 端接入（在已稳定的 Prompt/Memory 基线上落地双端联调）：
  1. 新增 Android Agent API 与流式渲染链路；
  2. 在工作台/上传状态/复核详情注入场景上下文；
  3. 完成端到端回归（含弱网与超时场景）。

## 使用说明
- 每次开发结束后更新：
  - `里程碑看板` 勾选状态
  - `今日完成记录`
  - `风险与阻塞`
  - `下一步计划`
