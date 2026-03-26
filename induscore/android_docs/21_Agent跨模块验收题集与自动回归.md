# Agent 跨模块验收题集与自动回归

## 目标

- 验证 Agent 已覆盖多业务域，而非仅 `workbench`。
- 验证回答引用了工具数据（非泛化话术）。
- 提供可重复执行的自动回归脚本，供后端与前端联调用。

## 覆盖模块

- 工作台（workbench）
- 检测记录（detection_records）
- 数据大屏（dashboard）
- 生产线（production_line）
- AI 模型管理（model_mgmt）

## 一、手工验收题集（建议）

每个模块至少抽测 3 条问题，观察：
- 是否命中对应工具；
- 是否给出“结论 + 依据 + 下一步建议”；
- 是否避免编造未提供的数据。

### 1) 工作台
- 请汇总当前待复核、处理中、严重告警情况。
- 今天抽检量和告警量是否异常？
- 结合工作台数据，给我 3 条优先行动建议。

期望工具：`getMobileWorkbench`

### 2) 检测记录
- 请分析当前复核任务压力和风险点。
- 近一段检测记录的缺陷分布有什么特征？
- 我们今天复核闭环率大概怎么样？

期望工具：`getDetectionRecordsOverview`、`getPilotMetrics`

### 3) 数据大屏
- 请总结当前 KPI、趋势和告警重点。
- 结合趋势数据，判断质量是否在变差。
- 请给出“当前大屏指标的核心结论”。

期望工具：`getDashboardKpi`、`getDashboardDefectTrend`、`getDashboardAlerts`

### 4) 生产线
- 请分析当前生产线运行状态和重点风险线体。
- 哪些线体处于 maintenance 或 stopped 状态？
- 若某条线体状态异常，下一步排查建议是什么？

期望工具：`getProductionLinesOverview`（可选 `getProductionLineDetail`）

### 5) AI 模型管理
- 请总结当前部署模型与主模型状态。
- 目前模型管理层面最需要关注什么风险？
- 结合统计信息，给我模型治理建议。

期望工具：`getModelManagementOverview`

## 二、自动回归脚本

脚本路径：

- `scripts/agent-regression.ps1`

功能说明：

- 调用 `POST /v1/agent/chat`
- 自动执行 5 条跨模块用例
- 校验：
  - 接口 `code=200`
  - `answer` 非空
  - `toolCalls` 命中预期工具
- 任一用例失败返回非 0 退出码（方便 CI 集成）

## 三、执行方式（PowerShell）

```powershell
$env:AGENT_E2E_TOKEN="你的JWT"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\agent-regression.ps1 -BaseUrl "http://localhost:8000" -ClientType "pc"
```

也可显式传入 token：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\agent-regression.ps1 -BaseUrl "http://localhost:8000" -ClientType "pc" -Token "你的JWT"
```

## 四、失败排查建议

- `工具命中失败`：优先看 `scene`、`pageParams` 与问题关键词是否匹配。
- `请求超时/overloaded`：先确认模型上游状态，再看重试与 fallback 配置。
- `401/403`：检查 `Authorization`、`X-Client-Type`、权限边界。
- `回答为空`：检查模型返回与后端裁剪逻辑，必要时看 `traceId` 与排查接口。

## 五、验收通过标准

- 自动回归脚本 5/5 全通过；
- 手工抽测每模块至少 2 条问题，回答都能引用对应工具数据；
- 无明显“泛化话术替代数据证据”的情况。
