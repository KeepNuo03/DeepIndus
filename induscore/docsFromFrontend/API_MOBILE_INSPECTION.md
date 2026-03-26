# 安卓端巡检/抽检 API（P0）

## 通用约定
- BasePath: `/v1/mobile`
- 认证：`Authorization: Bearer <token>`
- 端标识：`X-Client-Type: android`（未携带会按 `pc` 处理并拒绝访问移动接口）
- 上传幂等：`Idempotency-Key: <uuid>`

## 1) 工作台
- `GET /v1/mobile/workbench`
- 返回：待复核数、处理中数、今日抽检数、严重告警数、上传队列状态

## 2) 当前用户资料
- `GET /v1/mobile/profile`
- 返回：用户、角色、岗位、端类型

## 3) 复核任务列表
- `GET /v1/mobile/tasks/review?page=1&pageSize=20&status=all`
- status: `all|todo|processing|done`

## 4) 复核任务详情
- `GET /v1/mobile/tasks/review/{id}`

## 5) 提交复核
- `POST /v1/mobile/tasks/{id}/review`
- body:
```json
{
  "action": "confirm",
  "note": "现场已确认并处理"
}
```

## 6) 抽检上传
- `POST /v1/mobile/uploads/sampling`
- form-data: `file`, `serialNo?`, `productId?`, `productionLineId?`, `networkState?`
- header: `Idempotency-Key`（建议客户端必传）
- 语义：
  - 首次上传：执行检测并返回结果
  - 重复上传（同 key）：直接返回历史结果或处理中状态，不重复写入

## 7) 查询上传状态
- `GET /v1/mobile/uploads/{idempotencyKey}`
- 返回：`processing|completed|failed` + `recordId`

## 8) 通知列表
- `GET /v1/mobile/notifications?limit=10`
- 返回最近严重告警记录

## 9) 试点指标
- `GET /v1/mobile/pilot/metrics`
- 返回试点线体（A-01）及闭环率、恢复率等指标

## 错误码（关键）
- `401`：未认证或 Token 失效
- `403`：端类型不允许或角色/权限不足
- `404`：任务或上传状态不存在
