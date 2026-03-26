# Room 数据库设计

## 目标
通过本地数据库保证弱网可用、数据不丢、可恢复重试。

## 选型结论
- 使用 `Room`（底层是 `SQLite` 关系型数据库）。
- 本地库定位：缓存与队列，不替代服务端主库。

## 当前表结构（已在脚手架中定义）

### 1) `auth_session`
- 作用：存储登录会话与 token。
- 实体：`android-app/app/src/main/java/com/induscore/mobile/data/local/entity/AuthSessionEntity.kt`

### 2) `review_task_cache`
- 作用：缓存复核任务列表，支持离线查看。
- 实体：`android-app/app/src/main/java/com/induscore/mobile/data/local/entity/ReviewTaskCacheEntity.kt`

### 3) `upload_queue`
- 作用：抽检上传队列，支持失败重试。
- 关键字段：
  - `idempotencyKey`（唯一）
  - `status`（pending/uploading/completed/failed）
  - `retryCount`
  - `lastError`
  - `serverRecordId`
- 实体：`android-app/app/src/main/java/com/induscore/mobile/data/local/entity/UploadQueueEntity.kt`

## DAO 与数据库入口
- `AuthSessionDao.kt`
- `ReviewTaskCacheDao.kt`
- `UploadQueueDao.kt`
- `AppDatabase.kt`

## 实现建议
- 后续新增 `notification_cache` 与 `review_draft` 表，完善离线体验。
- 增加数据库迁移脚本（Room Migration）以支持版本升级。

## 风险
- 当前版本无 Migration，修改表结构后需要补迁移，避免用户升级崩溃。

## 下一步
- 补 `AppDatabase` 版本迁移规范。
- 在上传完成后定期清理历史 completed 队列。
