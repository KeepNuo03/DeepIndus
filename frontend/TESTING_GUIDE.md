# 🧪 前后端联调测试指南

## ✅ API 接入完成状态

### 已接入的所有接口（17个）

#### 🔐 用户认证（2个）
- [x] `POST /auth/login` → login.vue
- [x] `POST /auth/register` → login.vue

#### 📝 检测记录（3个）  
- [x] `GET /records/query` → query.vue
- [x] `POST /records/export` → query.vue
- [x] `DELETE /records/batch` → query.vue

#### 🔍 缺陷详情（2个）
- [x] `GET /defect/:id` → defect_detail.vue
- [x] `POST /defect/:id/review` → defect_detail.vue

#### 📊 数据大屏（4个）
- [x] `GET /dashboard/kpi` → data_dashboard.vue
- [x] `GET /dashboard/defect-trend` → data_dashboard.vue
- [x] `GET /dashboard/defect-distribution` → data_dashboard.vue
- [x] `GET /dashboard/alerts` → data_dashboard.vue

#### 🏭 生产线（1个）
- [x] `GET /production/lines` → production_line.vue

#### 🎥 实时检测（4个）🆕
- [x] `GET /detection/realtime/statistics` → real_time_detect.vue
- [x] `GET /detection/realtime/records` → real_time_detect.vue
- [x] `GET /detection/realtime/trend` → real_time_detect.vue
- [x] `POST /detection/realtime/control` → real_time_detect.vue

#### 🔌 WebSocket（1个）🆕
- [x] `ws://localhost:8000/ws?token=xxx` → real_time_detect.vue

---

## 🚀 启动服务

### 1. 启动后端（Spring Boot）
```bash
# 确保运行在 http://localhost:8000
# 检查健康状态
curl http://localhost:8000/v1/health
```

### 2. 启动前端（Vue）
```bash
cd c:\frontend
npm run dev
# 运行在 http://localhost:5173
```

---

## 🧪 完整测试流程

### 测试 1：登录功能 ⭐

**操作：**
1. 访问 `http://localhost:5173/`
2. 输入用户名：`admin`（或后端已有账号）
3. 输入密码：`admin123`
4. 点击"立即登录"

**检查点：**
- [ ] 显示 loading 遮罩"正在登录..."
- [ ] Network 看到请求：`POST /auth/login`
- [ ] 请求体：`{ username, password, remember }`
- [ ] 响应：`{ code: 200, data: { token, user } }`
- [ ] localStorage 保存了 token
- [ ] 自动跳转到 `/detect`

**F12 验证：**
```javascript
localStorage.getItem('token')  // 应该有值
localStorage.getItem('user')   // 应该有用户信息
```

---

### 测试 2：实时检测页面 🆕⭐

**操作：**
1. 登录后自动跳转到 `/detect`，或手动访问

**检查点：**
- [ ] 页面加载时发送 3 个并行请求：
  - `GET /detection/realtime/statistics`
  - `GET /detection/realtime/records?limit=10`
  - `GET /detection/realtime/trend`
- [ ] 所有请求都带 `Authorization: Bearer {token}`
- [ ] 统计卡片显示真实数据（今日检测目标、预警次数、置信度）
- [ ] 趋势图显示真实数据
- [ ] 检测记录列表显示真实数据
- [ ] WebSocket 连接成功

**测试启停控制：**
- 点击"停止检测"按钮
- 检查请求：`POST /detection/realtime/control`
- 请求体：`{ action: 'stop', cameraId: 'CAM-01' }`
- 按钮文字变为"开启检测"

**WebSocket 测试：**
- F12 → Network → WS 标签
- 应该看到连接：`ws://localhost:8000/ws?token=xxx`
- Status: 101 Switching Protocols

---

### 测试 3：数据大屏页面 ⭐

**操作：**
1. 访问 `http://localhost:5173/dashboard`

**检查点：**
- [ ] 页面加载时同时发送 4 个请求：
  - `GET /dashboard/kpi`
  - `GET /dashboard/defect-trend`
  - `GET /dashboard/defect-distribution`
  - `GET /dashboard/alerts`
- [ ] KPI 卡片显示真实数据
- [ ] 趋势折线图正常渲染
- [ ] 缺陷分布饼图正常渲染
- [ ] 报警列表显示真实数据

**数据验证：**
```javascript
// Console 查看响应
// KPI: { todayDetectionCount, avgDefectRate, yieldRate, utilizationRate }
// Trend: { timeLabels: [...], defectCounts: [...], yieldRates: [...] }
// Distribution: [{ type, count, percentage }]
// Alerts: [{ severity, title, time, description }]
```

---

### 测试 4：历史查询页面 ⭐

**操作：**
1. 访问 `http://localhost:5173/query`

**检查点：**
- [ ] 页面加载时请求：`GET /records/query?page=1&pageSize=20`
- [ ] 表格显示真实记录
- [ ] 显示总记录数

**测试分页：**
- 点击页码 2
- 检查请求：`GET /records/query?page=2&pageSize=20`
- 表格数据更新

**测试筛选：**
- 选择缺陷类型："表面划伤"
- 选择检测结果："不合格"
- 点击"查询记录"
- 检查请求参数：`?page=1&pageSize=20&defectType=scratch&status=fail`

**测试搜索：**
- 输入序列号：`SN-6729`
- 检查请求参数：`?search=SN-6729`

**测试批量导出：**
- 勾选 2 条记录
- 点击"批量导出"
- 检查请求：`POST /records/export`
- 请求体：`{ recordIds: [1, 2], format: 'excel' }`
- 检查是否打开下载链接

**测试批量删除：**
- 勾选 2 条记录
- 点击"批量删除"
- 确认删除
- 检查请求：`DELETE /records/batch`
- 请求体：`{ recordIds: [1, 2] }`
- 列表自动刷新

**测试详情跳转：**
- 点击某条记录的"详情"按钮
- 检查是否跳转到 `/defect/{检测编号}`

---

### 测试 5：缺陷详情页面 ⭐

**操作：**
1. 从查询页面点击"详情"
2. 或访问 `http://localhost:5173/defect/%23DET-2026A01`

**检查点：**
- [ ] 页面加载时请求：`GET /defect/{id}`
- [ ] 显示完整的缺陷信息
- [ ] 显示检测分析数据
- [ ] 显示处理记录时间线
- [ ] 显示相似缺陷推荐

**测试人工复核：**
- 点击"开始人工复核"
- 输入复核结果：`confirm`
- 输入备注：`确认为严重缺陷`
- 检查请求：`POST /defect/{id}/review`
- 请求体：`{ action: 'confirm', note: '确认为严重缺陷' }`
- 页面重新加载详情

---

### 测试 6：生产线管理页面 ⭐

**操作：**
1. 访问 `http://localhost:5173/production`

**检查点：**
- [ ] 页面加载时请求：`GET /production/lines`
- [ ] 响应包含：`{ statistics: {...}, lines: [...] }`
- [ ] 统计卡片显示正确（运行中、维护中、今日产量、平均良率）
- [ ] 生产线列表显示

**测试启停控制：**
- 点击某个生产线的"停止"按钮
- 确认操作
- 检查请求：`POST /production/lines/{id}/control`
- 请求体：`{ action: 'stop' }`
- 生产线状态更新

**测试刷新：**
- 点击"刷新"按钮
- 检查是否重新请求 `/production/lines`

---

### 测试 7：Token 验证 ⭐

**测试路由守卫：**
1. 清除 localStorage 中的 token
   ```javascript
   localStorage.clear()
   ```
2. 直接访问 `http://localhost:5173/query`
3. 应该提示"请先登录"并跳转到 `/login`

**测试 Token 过期：**
1. 修改 localStorage 中的 token 为无效值
   ```javascript
   localStorage.setItem('token', 'invalid_token')
   ```
2. 访问任意内部页面
3. 请求后端返回 401
4. 应该提示"登录已过期"并跳转到 `/login`

---

## 🔍 调试技巧

### Network 面板检查

**正常的请求应该是这样：**

```
请求示例：GET /dashboard/kpi

General:
  Request URL: http://localhost:8000/v1/dashboard/kpi
  Request Method: GET
  Status Code: 200 OK

Request Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json

Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "todayDetectionCount": 12842,
      "avgDefectRate": 0.42,
      "yieldRate": 99.58,
      "utilizationRate": 88.4
    }
  }
```

---

## 🐛 常见问题解决

### 问题 1：页面显示空数据

**排查步骤：**
1. F12 → Network 查看请求是否发送
2. 查看响应数据格式
3. 检查字段名是否匹配

**解决方案：**
- 后端返回的字段名要和前端期望一致
- 参考 `docs/API_SPECIFICATION.md`

---

### 问题 2：WebSocket 连接失败

**现象：** Console 显示 WebSocket connection failed

**排查：**
1. 检查后端 WebSocket 服务是否启动
2. 检查 URL 是否正确：`ws://localhost:8000/ws?token=xxx`
3. 检查 token 是否有效

**解决方案：**
- 后端配置 WebSocket 端点
- 确保 token 验证通过

---

### 问题 3：图表不显示

**原因：** 数据格式不对或 ECharts 未正确初始化

**排查：**
1. Console 查看是否有错误
2. 检查数据是否已加载：
   ```javascript
   // 在 mounted() 后打印
   console.log('趋势数据', this.trendData);
   ```

**解决方案：**
- 确保数据格式正确
- 确保在数据加载后才初始化图表

---

### 问题 4：CORS 错误

**现象：** 
```
Access to fetch at 'http://localhost:8000/v1/...' from origin 'http://localhost:5173' 
has been blocked by CORS policy
```

**解决方案（后端）：**

**Spring Boot 配置：**
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

---

## 📊 完整测试矩阵

| 页面 | 接口数 | 状态 | 测试优先级 |
|------|--------|------|-----------|
| 登录页 | 2 | ✅ 已接入 | P0 |
| 实时检测 | 4 + WS | ✅ 已接入 | P0 |
| 数据大屏 | 4 | ✅ 已接入 | P0 |
| 历史查询 | 3 | ✅ 已接入 | P0 |
| 缺陷详情 | 2 | ✅ 已接入 | P0 |
| 生产线 | 1 | ✅ 已接入 | P0 |

---

## 🎯 测试顺序建议

### 第 1 步：基础功能
```
1. 登录功能 （必须先通过）
2. Token 验证 （登录后访问其他页面）
```

### 第 2 步：数据展示
```
3. 数据大屏 （验证统计数据）
4. 历史查询 （验证列表数据）
5. 生产线管理 （验证生产数据）
```

### 第 3 步：详情与操作
```
6. 缺陷详情 （验证详情数据）
7. 人工复核 （验证POST操作）
8. 批量操作 （导出、删除）
```

### 第 4 步：实时功能
```
9. 实时检测页面 （验证实时数据）
10. WebSocket 推送 （验证实时通信）
11. 启停控制 （验证控制操作）
```

---

## 📝 测试用例模板

### 用例示例：测试历史查询

**用例编号：** TC-001  
**用例名称：** 历史查询-列表加载  
**前置条件：** 已登录，数据库有检测记录  

**测试步骤：**
1. 访问 `http://localhost:5173/query`
2. 观察页面加载

**预期结果：**
- 页面显示检测记录列表
- 显示分页信息
- 数据与数据库一致

**实际结果：** [填写]  
**测试状态：** [通过/失败]  
**备注：** [如有问题填写]

---

## ✅ 验收清单

### 功能验收

**登录模块：**
- [ ] 正确账号能登录成功
- [ ] 错误账号提示失败
- [ ] Token 正确保存
- [ ] 注册功能正常

**实时检测：**
- [ ] 统计数据正确显示
- [ ] 趋势图正常渲染
- [ ] 记录列表正常显示
- [ ] 启停控制正常
- [ ] WebSocket 连接成功

**数据大屏：**
- [ ] 4 个接口都正常
- [ ] KPI 数据正确
- [ ] 两个图表正常渲染
- [ ] 报警列表正常

**历史查询：**
- [ ] 列表正常加载
- [ ] 分页功能正常
- [ ] 筛选功能正常
- [ ] 搜索功能正常
- [ ] 导出功能正常
- [ ] 删除功能正常

**缺陷详情：**
- [ ] 详情数据完整
- [ ] 复核功能正常
- [ ] 处理记录显示
- [ ] 相似缺陷显示

**生产线：**
- [ ] 列表正常加载
- [ ] 统计数据正确
- [ ] 启停控制正常

**权限控制：**
- [ ] 未登录无法访问
- [ ] Token 过期自动跳转
- [ ] 所有请求都带 Authorization

---

## 🎊 联调完成标志

**当所有复选框都勾选时，表示联调成功！**

- 前端能正常调用所有后端接口
- 数据能正确展示
- 所有交互功能正常
- 没有接口报错
- 性能满足要求

---

## 📞 问题反馈格式

**如遇到问题，请提供：**

1. **问题描述**
   - 具体操作步骤
   - 预期结果 vs 实际结果

2. **错误信息**
   - F12 Console 的错误日志
   - F12 Network 的请求/响应截图

3. **环境信息**
   - 前端版本号
   - 后端版本号
   - 浏览器版本

**示例：**
```
【问题】登录后无法访问数据大屏

【操作步骤】
1. 输入 admin/admin123 登录成功
2. 访问 /dashboard
3. 页面空白

【错误信息】
Console: Uncaught TypeError: Cannot read property 'todayDetectionCount' of undefined
Network: GET /dashboard/kpi 返回 401

【环境】
前端: Vue 3.5.27
后端: Spring Boot 3.2.0
浏览器: Chrome 130
```

---

## 🚀 开始测试吧！

**测试流程：**
1. ✅ 启动前后端服务
2. ✅ 按顺序测试每个功能
3. ✅ 记录问题并反馈
4. ✅ 修复后重新测试
5. ✅ 全部通过即可上线

**祝测试顺利！** 🎉
