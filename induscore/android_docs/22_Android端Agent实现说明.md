# Android 端 Agent 实现说明

## 目标

- 在不破坏现有首页视觉一致性的前提下，落地 Agent 入口与首版问答能力。
- 与后端已完成的 Agent 能力对齐（会话、工具调用、审计、摘要记忆）。

## UI 设计约束（本次已落实）

- 系统主页右上角增加全局 Agent 入口。
- 系统主页右下角增加 Agent 模块卡片入口。
- Agent 卡片风格与系统主页现有模块卡片保持一致（圆角、边框、阴影、按钮样式）。
- Agent 品牌 logo 参考 Web 端：紫色圆形 + 机器人图标，并在入口与卡片右上角统一复用。

## 本次代码落地（首版）

- 新增 UI 组件：
  - `android-app/app/src/main/java/com/induscore/mobile/ui/components/BrandLogo.kt`
    - 新增 `AgentBrandLogoBadge`
- 新增页面与状态管理：
  - `android-app/app/src/main/java/com/induscore/mobile/ui/screen/AgentChatScreen.kt`
  - `android-app/app/src/main/java/com/induscore/mobile/ui/viewmodel/AgentChatViewModel.kt`
- 新增移动端 Agent 契约：
  - `android-app/app/src/main/java/com/induscore/mobile/data/remote/dto/AgentDtos.kt`
- 扩展接口与仓储：
  - `android-app/app/src/main/java/com/induscore/mobile/data/remote/MobileApiService.kt`
  - `android-app/app/src/main/java/com/induscore/mobile/domain/MobileRepository.kt`
- 导航与首页入口改造：
  - `android-app/app/src/main/java/com/induscore/mobile/ui/InduscoreMobileApp.kt`
  - `android-app/app/src/main/java/com/induscore/mobile/ui/screen/SystemHomeScreen.kt`

## 当前交付边界

- 已支持流式问答：`POST /v1/agent/chat/stream`（默认链路）。
- 已支持同步问答：`POST /v1/agent/chat`（流式失败时自动降级）。
- 已支持会话字段接入（sessionId、traceId、toolCalls 在 UI 展示）。
- 已接入会话列表/详情/归档 API，并在聊天页提供会话管理区。
- 已完成 SSE 事件解析：`meta`、`tool`、`chunk`、`done`、`error`。
- 已完成场景化一键提问：
  - 上传状态页可一键进入 Agent，并注入 `scene=upload_status + idempotencyKey`
  - 复核详情页可一键进入 Agent，并注入 `scene=review_detail + taskId`
  - 支持携带预置问题并自动触发首问
- 已完成会话管理首版：
  - 会话列表分页加载（refresh + load more）
  - 历史会话切换并回填消息
  - 当前会话归档与新建会话
- 已完成 Agent UI 大改版（移动对话优先）：
  - 取消顶部会话 ID 长串展示
  - 主区域改为全屏对话流（消息气泡为主）
  - 底部固定输入框 + 发送按钮
  - 左上角汉堡菜单拉起历史会话抽屉（可下滑）
- 已完成 Kimi 风格精修（本次）：
  - 抽屉内操作由小蓝字改为“图标 + 文字”按钮
  - 输入框改为圆角实体容器，去除方形透明感
  - 输入框聚焦前更低位，聚焦后抬升并贴近键盘
  - 头部下移，避免菜单与标题贴顶
  - 标题改为“质检AI助理”，并在前方显示品牌 logo
  - “返回系统主页”改为实按钮（不再是文本链接）

## 下一步（建议）

1. 完成弱网、超时、模型繁忙（overloaded）等错误态体验收口。
2. 优化工具事件展示（按耗时排序、失败高亮）与会话切换体验。
3. 评估接入 `debug/snapshot` 只读排障视图到开发版。
4. 评估“会话摘要提示”在移动端的可视化呈现。
