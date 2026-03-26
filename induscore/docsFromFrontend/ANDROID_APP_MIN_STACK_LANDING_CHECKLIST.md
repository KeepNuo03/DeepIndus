# 安卓 App 落地清单（最小技术栈）

## 1. 目标与范围

本清单用于把当前系统落地为 **PC + 安卓双端**：
- PC 端继续承担：全量检测、模型管理、权限管理、生产线管理、记录管理。
- 安卓端首期只做：登录、现场抽检上传、复核任务、消息提醒、离线重试。

首期原则：
- 不做复杂后台管理能力。
- 不把 PC 全功能迁移到安卓。
- 优先闭环：可用、可重试、可追踪、可审计。

---

## 2. 安卓端最小技术栈（推荐）

### 2.1 开发栈
- 语言：`Kotlin`
- UI：`Jetpack Compose`（或已有团队熟悉可用 XML）
- 网络：`Retrofit + OkHttp + Moshi/Gson`
- 本地数据库：`Room (SQLite)`
- 后台任务：`WorkManager`（离线上传重试核心）
- 依赖注入：`Hilt`（可选，但建议）
- 图片处理：`Coil`（展示），压缩可用 `Compressor` 或自实现

### 2.2 为什么是最小栈
- 全是安卓主流官方/事实标准组件，学习资料多。
- 能满足你最核心场景：弱网缓存 + 队列重试 + API 对接。

---

## 3. 安卓页面与后端接口一一对应

后端已提供移动端路径：`/v1/mobile/**`（并要求 `X-Client-Type: android`）。

## 3.1 页面与接口映射总表

1) 登录页  
- 接口：`POST /v1/auth/login`  
- 用途：获取 JWT（后续接口统一带 `Authorization`）。

2) 工作台页  
- 接口：`GET /v1/mobile/workbench`  
- 展示：待复核数、处理中数、今日抽检数、严重告警数、上传队列状态。

3) 我的页  
- 接口：`GET /v1/mobile/profile`  
- 展示：用户、角色、岗位、端类型。

4) 复核任务列表页  
- 接口：`GET /v1/mobile/tasks/review?page=&pageSize=&status=`  
- 展示：任务列表；status 取 `all|todo|processing|done`。

5) 复核详情页  
- 接口：`GET /v1/mobile/tasks/review/{id}`  
- 展示：缺陷详情、图片、处理记录等。

6) 复核提交页（可在详情页内）  
- 接口：`POST /v1/mobile/tasks/{id}/review`  
- body：`{ "action": "confirm|reject", "note": "..." }`

7) 抽检上传页  
- 接口：`POST /v1/mobile/uploads/sampling`  
- 关键 Header：`Idempotency-Key`  
- form-data：`file`, `serialNo?`, `productId?`, `productionLineId?`, `networkState?`

8) 上传状态页（或上传结果弹层）  
- 接口：`GET /v1/mobile/uploads/{idempotencyKey}`  
- 展示：`processing|completed|failed`、`retryCount`、`recordId`。

9) 通知页  
- 接口：`GET /v1/mobile/notifications?limit=10`  
- 展示：严重告警通知。

10) 试点指标页（管理员/主管可见）  
- 接口：`GET /v1/mobile/pilot/metrics`  
- 展示：A-01 试点闭环率、恢复率、目标阈值。

---

## 4. 接口调用统一规范（安卓端）

每个请求统一加：
- `Authorization: Bearer <token>`
- `X-Client-Type: android`

上传接口加：
- `Idempotency-Key: <UUID>`（客户端生成并持久化）

错误码处理：
- `401`：token 过期/未登录 -> 跳登录
- `403`：权限或端类型受限 -> 提示无权限
- `404`：任务或上传状态不存在 -> 提示并刷新
- `500`：服务器错误 -> 进入重试队列

---

## 5. 安卓端本地数据库（Room）设计

## 5.1 选择建议
- 首期用 `Room`，不要在安卓端接 MySQL。
- 安卓本地库定位：**离线缓存 + 上传队列 + 草稿**，不是主数据源。

## 5.2 最小表设计

### 表1：`auth_session`
- `id` (PK, 固定1)
- `accessToken`
- `userId`
- `username`
- `rolesJson`
- `expiredAt`
- `updatedAt`

用途：会话持久化、冷启动免登录判断。

### 表2：`review_task_cache`
- `taskId` (PK)
- `detectionNo`
- `serialNo`
- `defect`
- `severity`
- `processStatus`
- `timestamp`
- `imageUrl`
- `syncedAt`

用途：任务列表离线查看。

### 表3：`upload_queue`
- `id` (PK, 自增)
- `idempotencyKey` (UNIQUE)
- `localFilePath`
- `serialNo`
- `productId`
- `productionLineId`
- `networkState`
- `status` (`pending|uploading|completed|failed`)
- `retryCount`
- `lastError`
- `serverRecordId`
- `createdAt`
- `updatedAt`

用途：离线上传重试主表。

### 表4：`review_draft`
- `taskId` (PK)
- `action`
- `note`
- `updatedAt`

用途：现场填写后临时保存草稿，防止切后台丢数据。

### 表5：`notification_cache`
- `notificationId` (PK)
- `title`
- `severity`
- `timestamp`
- `taskStatus`
- `detectionNo`
- `syncedAt`

用途：通知列表缓存。

---

## 6. 离线上传与重试实现（WorkManager）

## 6.1 上传流程（建议）
1. 用户拍照 -> 本地压缩 -> 写入 `upload_queue(status=pending)`  
2. 立即尝试发起上传（网络可用时）  
3. 请求头带 `Idempotency-Key`  
4. 成功：更新 `status=completed`，保存 `serverRecordId`  
5. 失败：更新 `status=failed`，记录 `lastError`  
6. `WorkManager` 按网络条件重试 failed/pending

## 6.2 幂等关键点
- 同一业务动作只生成一个 `idempotencyKey`。
- 重试时复用同 key，避免服务端重复落库。
- 查询 `GET /v1/mobile/uploads/{idempotencyKey}` 作为状态对账。

## 6.3 重试策略
- 最大重试：5 次（可配置）
- 退避：指数退避（如 10s/30s/60s/120s）
- 仅在 `CONNECTED` 网络下执行 Worker

---

## 7. 安卓端 YOLO 本地部署（最小可行方案）

你当前架构中，PC/服务端检测是主链路。安卓本地 YOLO 建议作为“现场快速预判”，最终可仍以上传服务端结果为准。

## 7.1 推荐技术栈
- 推理框架优先级：
  1. `TFLite`（首选，资料最多）
  2. `NCNN`（性能更优，但集成复杂度更高）

首期建议：先 `TFLite`，稳定后再评估 NCNN。

## 7.2 模型落地步骤
1. 训练端导出模型（YOLO -> ONNX/TFLite）
2. 量化压缩（FP16 或 INT8）降低体积和耗电
3. 安卓端集成推理库
4. 实现前处理（resize/normalize）和后处理（NMS）
5. 实机压测（时延、发热、耗电、精度）
6. 根据机型分级配置（低端机只上传云端检测）

## 7.3 性能目标建议（首期）
- 单帧推理时延：中端机目标 < 300ms
- APK 增量体积：尽量 < 30MB（模型单独下载更优）
- 连续推理不明显发烫（现场可持续使用 15 分钟+）

## 7.4 发布策略建议
- 模型热更新：从后端下载模型文件 + 校验 hash
- 保留版本回滚：下载失败或精度异常可回退上一版本
- 高风险场景下允许关闭本地 YOLO，仅走云端

---

## 8. 后端配合项（你当前 Spring Boot 已具备基础）

已具备：
- `JWT` + 移动端路径分组
- 端类型控制（`X-Client-Type`）
- 上传幂等状态查询
- 试点指标接口

建议继续增强（可选）：
- 上传状态持久化到数据库（当前为内存态，重启会丢）
- 上传文件对象存储化（MinIO/OSS）而非本地磁盘
- 通知改为消息推送（FCM）+ WebSocket/轮询兜底

---

## 9. 两周 MVP 执行清单（给非安卓背景团队）

第1周：
- [ ] 建安卓工程（Kotlin + Compose + Retrofit + Room + WorkManager）
- [ ] 完成登录、Token 存储、全局拦截器
- [ ] 完成工作台/任务列表页（含缓存）
- [ ] 完成抽检上传（含 Idempotency-Key）

第2周：
- [ ] 完成复核详情与复核提交
- [ ] 完成离线队列重试 Worker
- [ ] 完成通知页与上传状态页
- [ ] 接通试点指标页
- [ ] 联调与现场弱网测试

---

## 10. 验收与上线门槛

- 功能：9个页面/流程可走通，无阻断
- 数据：离线重试成功率 >= 99%
- 权限：移动端无法访问 PC 管理接口
- 性能：关键查询接口 P95 < 500ms（上传除外）
- 运维：可观测（上传失败日志、重试日志、401/403统计）

---

## 11. 你可以直接交给安卓同学的最小任务包

1. 按本文件第3节完成页面-接口对接。  
2. 按第5节建 Room 表结构。  
3. 按第6节实现 WorkManager 上传队列。  
4. 按第7节接入 TFLite 本地推理（先做可选开关）。  
5. 按第9节两周清单推进并每日回归第10节指标。  

