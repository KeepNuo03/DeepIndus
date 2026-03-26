# DEEPINDUS 后端交接小结（精简版）

## 1. 项目一句话介绍

- **系统定位**：工业钢材等产品的外观缺陷检测与生产质量管理平台  
- **前端技术栈**：Vue 3 + Vite + Tailwind CSS + ECharts + Iconify  
- **后端接口约定**：RESTful + WebSocket（详见 `docs/API_SPECIFICATION.md`、`docs/DATA_MODELS.md`）

---

## 2. 当前前端与接口对接进度（按页面）

- **登录页 `/login`**
  - 已实现：`src/api/auth.js`，调用 `/auth/login`、`/auth/register`、`/auth/logout`
  - 期望后端：返回 JWT + 用户信息（格式见 `API_SPECIFICATION.md`「用户认证模块」）

- **实时检测页 `/detect`（实时视频流监控）**
  - 已实现 API 封装（`src/api/detection.js`）并在 `real_time_detect.vue` 中真实调用：
    - `GET /detection/realtime/statistics` → 顶部统计卡片
    - `GET /detection/realtime/records` → 右侧「最近检测记录」
    - `GET /detection/realtime/trend` → 右侧折线图趋势
    - `POST /detection/realtime/control` → 「开启/停止检测」按钮
    - `POST /detection/upload` → 「上传离线检测」按钮（表单上传图片）
  - WebSocket：
    - 前端已在 `real_time_detect.vue` 中直接连接：`ws://localhost:8000/ws?token=...`
    - 期待消息类型：`detection` / `alert` / `status`（格式见 `API_SPECIFICATION.md` 尾部 WebSocket 小节）

- **数据大屏页 `/dashboard`**
  - 已实现 API 封装（`src/api/dashboard.js`）并在 `data_dashboard.vue` 中真实调用：
    - `GET /dashboard/kpi` → KPI 卡片（当日检测数、平均缺陷率、良品率、稼动率）
    - `GET /dashboard/defect-trend` → 「24 小时缺陷趋势分析」折线图  
    - `GET /dashboard/defect-distribution` → 「缺陷类型占比」饼图  
    - `GET /dashboard/alerts` → 右侧「实时异常报警列表」

- **历史查询页 `/query`**
  - 已实现 API 封装（`src/api/detection.js`）并在 `query.vue` 中真实调用：
    - `GET /records/query` → 表格数据（分页、按缺陷类型 / 状态 / 搜索）
    - `POST /records/export` → 批量导出（返回 `downloadUrl` 并在前端直接 `window.open`）
    - `DELETE /records/batch` → 批量删除
  - 表格中「详情」按钮路由到 `/defect/:id`，依赖：
    - `GET /defect/:id` → 缺陷详情数据（在 `API_SPECIFICATION.md` 中已定义）

- **生产线管理页 `/production` 及后续管理页面**
  - 生产线相关：前端已封装但尚未全面改造页面逻辑
    - `src/api/production.js`：`GET /production/lines`、`POST /production/lines/:id/control`、`PUT /production/lines/:id`、`PUT /production/lines/:lineId/cameras/:cameraId`
  - 产品管理 / 模型管理 / 用户权限等页面：
    - UI 已完备，目前仍主要使用 Mock 数据；对应 API 规范已在 `API_SPECIFICATION.md` 中写好（`/products`、`/models`、`/users`、`/roles` 等）。

---

## 3. 已完成的“接口层”工作概览

- **统一请求工具**：`src/utils/request.js`
  - 基础地址：`http://localhost:8000/v1`
  - 自动注入 `Authorization: Bearer <token>`（非 `/auth/*` 接口）
  - 统一处理 `code !== 200`、`401` 时清理本地 Token 并跳转登录

- **已落地的前端 API 封装模块**（与文档对齐）：
  - `src/api/auth.js` → `/auth/*`
  - `src/api/detection.js` → `/records/*`、`/defect/*`、`/detection/realtime/*`、`/detection/upload`
  - `src/api/dashboard.js` → `/dashboard/*`
  - `src/api/production.js` → `/production/lines*`

- **接口与数据模型文档**（保持为“权威规范”，不需你再反推前端）：
  - `docs/API_SPECIFICATION.md`：请求/响应字段定义 + 示例
  - `docs/DATA_MODELS.md`：User / DetectionRecord / ProductionLine / Product / AIModel 等核心结构与建表建议

---

## 4. 建议的后端开发优先级（聚焦“能联调起来”）

- **P0（先做，能立刻看到效果）**
  - 认证：`POST /auth/login`、`POST /auth/register`、`POST /auth/logout`
  - 历史查询：`GET /records/query`、`GET /defect/:id`
  - 数据大屏：`GET /dashboard/kpi`、`GET /dashboard/defect-trend`、`GET /dashboard/defect-distribution`、`GET /dashboard/alerts`
  - 实时检测统计：`GET /detection/realtime/statistics`、`GET /detection/realtime/records`、`GET /detection/realtime/trend`

- **P1（有了 P0 之后，完善日常功能）**
  - 实时检测控制与离线检测：`POST /detection/realtime/control`、`POST /detection/upload`
  - 历史记录操作：`POST /records/export`、`DELETE /records/batch`
  - 生产线：`GET /production/lines` 及控制/编辑接口

- **P2（管理/运维向功能，可稍后排期）**
  - 产品、AI 模型、用户与角色、操作日志等（参见 `API_LIST.md` 与 `API_SPECIFICATION.md` 中相应模块）
  - WebSocket 消息细化（报警、状态广播等）

---

## 5. 联调要点（按这一节就能开工）

- **服务地址约定**
  - HTTP：`http://localhost:8000/v1`
  - WebSocket：`ws://localhost:8000/ws?token=<JWT>`

- **认证与错误码**
  - 请求头：`Authorization: Bearer <token>`（前端已自动加）
  - 统一响应结构：见 `API_SPECIFICATION.md` 顶部「接口约定」

- **调试方式建议**
  - 启动前端：在 `c:\frontend` 运行 `npm install && npm run dev`
  - 登录成功后，依次在页面中触发操作：
    - `/detect`：观察统计卡片 / 趋势图 / 最近记录是否有数据
    - `/dashboard`：查看 KPI、趋势、饼图、报警是否正常
    - `/query`：筛选、分页、导出、删除是否命中你的接口

---

## 6. 如果只看三份文档

- **项目与业务概览**：`docs/README.md`  
- **接口与字段标准**：`docs/API_SPECIFICATION.md`  
- **数据库/模型设计**：`docs/DATA_MODELS.md`  

其余文档可按需查阅，不必全部从头读完。
