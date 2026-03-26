# API 接口清单 - 快速索引

## 📌 说明

这是所有 API 接口的快速索引表，方便后端工程师快速查找。

详细定义请查看：[API_SPECIFICATION.md](./API_SPECIFICATION.md)

---

## 🔐 1. 用户认证模块（3个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 用户登录 | POST | `/auth/login` | P0 | 返回 token 和用户信息 |
| 用户注册 | POST | `/auth/register` | P0 | 创建新用户 |
| 退出登录 | POST | `/auth/logout` | P1 | 清除 token |

---

## 🎥 2. 实时检测模块（4个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取实时统计 | GET | `/detection/realtime/statistics` | P1 | 今日目标数、预警次数等 |
| 获取实时记录 | GET | `/detection/realtime/records` | P1 | 最近检测记录列表 |
| 获取趋势数据 | GET | `/detection/realtime/trend` | P1 | 图表趋势数据 |
| 控制检测状态 | POST | `/detection/realtime/control` | P2 | 启动/停止检测 |

---

## 📊 3. 数据大屏模块（4个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取 KPI 指标 | GET | `/dashboard/kpi` | P0 | 4项关键指标 |
| 获取缺陷趋势 | GET | `/dashboard/defect-trend` | P0 | 24小时趋势图数据 |
| 获取缺陷分布 | GET | `/dashboard/defect-distribution` | P0 | 饼图数据 |
| 获取报警列表 | GET | `/dashboard/alerts` | P1 | 实时报警列表 |

---

## 📝 4. 历史查询模块（3个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 查询检测记录 | GET | `/records/query` | P0 | 支持分页、筛选、搜索 |
| 批量导出记录 | POST | `/records/export` | P1 | 导出为 Excel/PDF |
| 批量删除记录 | DELETE | `/records/batch` | P1 | 批量删除 |

---

## 🔍 5. 缺陷详情模块（2个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取缺陷详情 | GET | `/defect/:id` | P0 | 完整的缺陷信息 |
| 开始人工复核 | POST | `/defect/:id/review` | P1 | 提交复核结果 |

---

## 🏭 6. 生产线管理模块（4个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取生产线列表 | GET | `/production/lines` | P0 | 包含统计和摄像头 |
| 启动/停止生产线 | POST | `/production/lines/:id/control` | P1 | 控制生产线 |
| 编辑生产线 | PUT | `/production/lines/:id` | P1 | 修改生产线信息 |
| 配置摄像头 | PUT | `/production/lines/:lineId/cameras/:cameraId` | P2 | 摄像头参数配置 |

---

## 📦 7. 产品管理模块（4个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取产品列表 | GET | `/products` | P0 | 支持分类和搜索 |
| 新增产品 | POST | `/products` | P1 | 创建产品型号 |
| 编辑产品 | PUT | `/products/:id` | P1 | 修改产品信息 |
| 删除产品 | DELETE | `/products/:id` | P2 | 删除产品 |

---

## 🤖 8. AI 模型管理模块（5个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取模型列表 | GET | `/models` | P0 | 包含统计和版本 |
| 上传模型 | POST | `/models/upload` | P1 | 上传模型文件 |
| 部署模型 | POST | `/models/:id/deploy` | P1 | 部署到生产 |
| 下线模型 | POST | `/models/:id/undeploy` | P1 | 停止使用 |
| 性能对比 | GET | `/models/comparison` | P2 | 图表对比数据 |

---

## 👥 9. 用户权限管理模块（8个接口）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取用户列表 | GET | `/users` | P0 | 支持分页、部门筛选 |
| 新增用户 | POST | `/users` | P1 | 创建新用户 |
| 编辑用户 | PUT | `/users/:id` | P1 | 修改用户信息 |
| 重置密码 | POST | `/users/:id/reset-password` | P1 | 生成新密码 |
| 切换状态 | POST | `/users/:id/toggle-status` | P1 | 启用/禁用 |
| 获取角色列表 | GET | `/roles` | P0 | 角色和权限 |
| 编辑角色 | PUT | `/roles/:id` | P1 | 修改角色权限 |
| 获取操作日志 | GET | `/logs/operations` | P1 | 审计日志 |

---

## 🌐 10. 通用接口（3个）

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取部门列表 | GET | `/departments` | P1 | 组织架构 |
| 文件上传 | POST | `/upload` | P1 | 通用文件上传 |
| 系统配置 | GET | `/system/config` | P2 | 系统基本信息 |

---

## 🔌 11. WebSocket（实时通信）

| 功能 | 连接地址 | 优先级 | 说明 |
|------|---------|--------|------|
| 实时推送 | `ws://api.deepindus.com/ws` | P2 | 检测结果、报警等实时推送 |

**消息类型：**
- `detection` - 检测结果
- `alert` - 报警通知
- `status` - 状态更新

---

## 📈 接口统计

### 按优先级统计
- **P0（必须）：** 11 个接口
- **P1（重要）：** 19 个接口
- **P2（增强）：** 7 个接口

**总计：** 37 个接口 + 1 个 WebSocket

### 按模块统计
- 用户认证：3 个
- 实时检测：4 个
- 数据大屏：4 个
- 历史查询：3 个
- 缺陷详情：2 个
- 生产线：4 个
- 产品：4 个
- AI 模型：5 个
- 用户权限：8 个

---

## 🎯 开发顺序建议

### 第 1 天
```
✅ POST /auth/login
✅ POST /auth/register
✅ JWT 认证中间件
```

### 第 2-3 天
```
✅ GET /records/query
✅ GET /defect/:id
✅ GET /dashboard/kpi
✅ GET /dashboard/defect-trend
✅ GET /dashboard/defect-distribution
```

### 第 4-5 天
```
✅ GET /production/lines
✅ GET /products
✅ GET /models
✅ GET /users
✅ GET /roles
```

### 第 6-10 天
```
✅ 所有 POST/PUT/DELETE 接口
✅ 批量操作接口
```

### 第 11-15 天
```
✅ WebSocket 服务
✅ AI 推理集成
✅ 实时推送功能
```

---

## 💡 快速查找

### 想知道某个页面需要什么接口？

**实时检测页面：**
- `/detection/realtime/*` （4个接口）

**数据大屏页面：**
- `/dashboard/*` （4个接口）

**历史查询页面：**
- `/records/query`
- `/defect/:id`

**生产线管理页面：**
- `/production/lines` （4个接口）

**产品管理页面：**
- `/products` （4个接口）

**AI 模型页面：**
- `/models` （5个接口）

**用户权限页面：**
- `/users` （5个接口）
- `/roles` （2个接口）
- `/logs/operations`

---

## 🔗 相关文档

- **详细接口定义：** [API_SPECIFICATION.md](./API_SPECIFICATION.md)
- **数据库表结构：** [DATA_MODELS.md](./DATA_MODELS.md)
- **前端调用示例：** [FRONTEND_API_INTEGRATION.md](./FRONTEND_API_INTEGRATION.md)

---

**开发愉快！** 🚀
