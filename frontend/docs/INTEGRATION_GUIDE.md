# DEEPINDUS 系统 - 前后端对接指南

## 🎯 对接目标

让后端工程师能够快速理解前端需求，实现无缝对接，确保系统稳定运行。

---

## 📋 对接前准备

### 后端工程师需要了解的内容

1. ✅ **阅读项目说明：** `README.md`
2. ✅ **查看 API 规范：** `API_SPECIFICATION.md`
3. ✅ **理解数据模型：** `DATA_MODELS.md`
4. ✅ **运行前端项目：** `npm install && npm run dev`
5. ✅ **访问所有页面：** 熟悉业务流程

---

## 🏗️ 技术栈建议

### 后端技术选型

**推荐方案 A（Python）：**
- 框架：FastAPI / Django
- 数据库：PostgreSQL / MySQL
- 缓存：Redis
- 队列：Celery / RQ
- WebSocket：Socket.IO / Channels
- AI 推理：PyTorch / TensorFlow

**推荐方案 B（Node.js）：**
- 框架：Express / NestJS
- 数据库：PostgreSQL / MySQL
- 缓存：Redis
- 队列：Bull
- WebSocket：Socket.IO
- AI 推理：调用 Python 微服务

**推荐方案 C（Java）：**
- 框架：Spring Boot
- 数据库：MySQL
- 缓存：Redis
- 队列：RabbitMQ
- WebSocket：Spring WebSocket
- AI 推理：调用 Python 微服务

---

## 🔌 接口对接步骤

### 第一阶段：基础框架（1-2天）

#### 1. 搭建后端项目框架
- [x] 创建项目结构
- [x] 配置数据库连接
- [x] 配置 CORS（允许前端域名）
- [x] 配置日志系统
- [x] 配置异常处理

#### 2. 实现用户认证
- [x] 用户登录接口
- [x] JWT Token 生成与验证
- [x] 密码加密（bcrypt）
- [x] 中间件：Token 验证
- [x] 中间件：权限验证

**测试前端：** 登录页面能否成功登录并跳转

---

### 第二阶段：核心业务（3-5天）

#### 3. 实现检测记录查询
- [x] 分页查询接口
- [x] 多条件筛选
- [x] 搜索功能
- [x] 批量操作接口

**测试前端：** 历史查询页面能否正常展示数据

#### 4. 实现缺陷详情
- [x] 详情查询接口
- [x] 处理记录接口
- [x] 相似缺陷推荐

**测试前端：** 缺陷详情页面数据展示

#### 5. 实现数据大屏
- [x] KPI 统计接口
- [x] 趋势图数据接口
- [x] 缺陷分布接口
- [x] 报警列表接口

**测试前端：** 数据大屏各图表数据加载

---

### 第三阶段：生产管理（2-3天）

#### 6. 实现生产线管理
- [x] 生产线 CRUD
- [x] 摄像头配置
- [x] 工位管理
- [x] 生产统计

**测试前端：** 生产线管理页面功能

#### 7. 实现产品管理
- [x] 产品 CRUD
- [x] 质量标准配置
- [x] 检测配置管理

**测试前端：** 产品管理页面功能

---

### 第四阶段：高级功能（2-3天）

#### 8. 实现 AI 模型管理
- [x] 模型上传接口
- [x] 模型版本管理
- [x] 模型部署控制
- [x] 性能统计接口

**测试前端：** AI 模型管理页面

#### 9. 实现用户权限管理
- [x] 用户 CRUD
- [x] 角色权限配置
- [x] 操作日志记录

**测试前端：** 用户权限管理页面

#### 10. 实现实时检测（最复杂）
- [x] WebSocket 服务搭建
- [x] 视频流接入
- [x] AI 模型推理集成
- [x] 实时数据推送

**测试前端：** 实时检测页面能接收实时数据

---

## 🔧 开发环境配置

### CORS 配置示例（Express）
```javascript
app.use(cors({
  origin: 'http://localhost:5173',
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS']
}));
```

### CORS 配置示例（FastAPI）
```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

---

## 🧪 接口测试

### 使用 Postman 测试

**步骤：**
1. 导入 API 文档（如有 OpenAPI 规范）
2. 配置环境变量（base_url, token）
3. 按顺序测试接口

**测试顺序：**
```
1. POST /auth/login         → 获取 token
2. GET  /users              → 测试认证
3. GET  /records/query      → 测试业务接口
4. GET  /dashboard/kpi      → 测试统计接口
```

### 前端联调测试

**创建 axios 实例：**
```javascript
// src/utils/request.js
import axios from 'axios';

const request = axios.create({
  baseURL: 'http://localhost:8000/v1',  // 后端地址
  timeout: 10000
});

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token 过期，跳转登录
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
```

---

## 🚀 部署对接

### 前端部署

**构建命令：**
```bash
npm run build
```

**输出目录：** `dist/`

**部署方式：**
- Nginx 静态托管
- CDN 加速
- Docker 容器化

**Nginx 配置示例：**
```nginx
server {
    listen 80;
    server_name deepindus.com;
    root /var/www/deepindus/dist;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend:8000/v1/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket 代理
    location /ws {
        proxy_pass http://backend:8000/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 🔒 安全对接

### Token 验证流程

**前端：**
1. 登录成功后将 token 存储到 `localStorage`
2. 每次请求在 Header 中携带 token
3. Token 过期自动跳转登录页

**后端：**
1. 验证 Token 签名
2. 检查 Token 是否过期
3. 提取用户信息和权限
4. 返回 401 表示未认证

### 权限验证

**前端路由守卫：**
```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  // 白名单页面
  if (to.path === '/login' || to.path === '/') {
    next();
    return;
  }
  
  // 需要登录
  if (!token) {
    next('/login');
    return;
  }
  
  next();
});
```

**后端权限验证：**
- 中间件检查用户权限
- 返回 403 表示权限不足

---

## 📊 数据对接注意事项

### 1. 时间格式
**前端使用：** `YYYY-MM-DD HH:mm:ss`  
**后端返回：** 统一使用此格式或 ISO 8601

### 2. 分页参数
**前端发送：**
```
?page=1&pageSize=20
```

**后端返回：**
```json
{
  "data": [...],
  "total": 100,
  "page": 1,
  "pageSize": 20
}
```

### 3. ID 类型
**统一使用：** 数字型 ID（不建议使用 UUID）

### 4. 布尔值
**统一使用：** `true` / `false`（不使用 0/1）

### 5. 空值处理
**前端期望：**
- 数组为空返回 `[]`
- 对象为空返回 `{}`
- 字符串为空返回 `""`
- 数字为空返回 `null`

### 6. 百分比
**统一使用：** 数字（0-100），前端自动添加 `%` 符号

---

## 🐛 常见问题处理

### 1. 跨域问题
**现象：** 浏览器报 CORS 错误  
**解决：** 后端配置 CORS，允许前端域名

### 2. Token 过期
**现象：** 接口返回 401  
**解决：** 前端跳转登录页，清除旧 Token

### 3. WebSocket 连接失败
**现象：** 实时检测无数据  
**解决：** 检查 WebSocket 服务是否启动，Nginx 是否配置代理

### 4. 图片无法显示
**现象：** 图片 URL 404  
**解决：** 检查图片服务器配置，CORS 设置

### 5. 数据不更新
**现象：** 页面显示旧数据  
**解决：** 检查缓存策略，增加时间戳参数

---

## 🧪 测试清单

### 功能测试

**登录模块：**
- [ ] 正确账号密码能登录
- [ ] 错误账号密码提示错误
- [ ] 登录后能正常跳转
- [ ] Token 能正常保存

**实时检测：**
- [ ] 页面加载显示统计数据
- [ ] 能接收实时推送
- [ ] 趋势图数据正确
- [ ] 记录列表能刷新

**历史查询：**
- [ ] 分页功能正常
- [ ] 筛选条件生效
- [ ] 搜索功能正常
- [ ] 详情按钮能跳转

**数据大屏：**
- [ ] KPI 数据正确
- [ ] 图表数据加载
- [ ] 报警列表实时更新

**生产管理：**
- [ ] 生产线列表加载
- [ ] 启停功能正常
- [ ] 统计数据准确

**产品管理：**
- [ ] 产品列表展示
- [ ] 分类筛选正常
- [ ] 详情抽屉打开

**AI 模型：**
- [ ] 模型列表加载
- [ ] 性能图表显示
- [ ] 部署功能正常

**用户权限：**
- [ ] 用户列表加载
- [ ] 角色权限展示
- [ ] 日志记录查询

---

## 📞 对接沟通

### 联调会议安排
- **第1次：** 技术方案评审（1小时）
- **第2次：** API 接口对接（2小时）
- **第3次：** 数据联调测试（3小时）
- **第4次：** 完整功能测试（半天）

### 沟通渠道
- **技术文档：** 本目录下的 Markdown 文件
- **接口文档：** 建议使用 Swagger/Apifox
- **问题反馈：** GitHub Issues / 项目群
- **紧急问题：** 电话/微信

---

## 🎯 开发优先级建议

### 第 1 周：核心功能
```
Day 1-2: 用户认证 + 数据库设计
Day 3-4: 检测记录查询 + 缺陷详情
Day 5: 数据大屏统计接口
```

### 第 2 周：生产管理
```
Day 1-2: 生产线管理
Day 3-4: 产品管理
Day 5: 测试联调
```

### 第 3 周：高级功能
```
Day 1-2: AI 模型管理
Day 3-4: 用户权限管理
Day 5: 测试联调
```

### 第 4 周：实时功能
```
Day 1-3: WebSocket 实时推送
Day 4-5: AI 推理集成
```

---

## 📝 Mock 数据说明

### 前端当前使用 Mock 数据
所有页面组件的 `data()` 中都包含示例数据，这些数据的结构就是后端需要返回的格式。

**示例位置：**
- `src/pages/query.vue` - 第 142 行开始的 `records` 数组
- `src/pages/production_line.vue` - 第 32 行开始的 `productionLines` 数组
- `src/pages/product_management.vue` - 第 51 行开始的 `products` 数组
- `src/pages/model_management.vue` - 第 30 行开始的 `models` 数组
- `src/pages/user_management.vue` - 第 47 行开始的 `users` 数组

### 如何使用 Mock 数据
后端开发时可以参考这些数据结构，确保返回的字段与前端期望一致。

---

## 🔄 数据流转示意

### 典型业务流程

```
1. 用户登录
   前端 → POST /auth/login → 后端验证 → 返回 Token → 前端存储

2. 查看数据大屏
   前端 → GET /dashboard/kpi → 后端查询数据库 → 返回统计数据 → 前端渲染图表

3. 查询历史记录
   前端 → GET /records/query?page=1 → 后端分页查询 → 返回记录列表 → 前端表格展示

4. 查看缺陷详情
   前端 → GET /defect/:id → 后端查询详情 → 返回完整信息 → 前端详情页展示

5. 实时检测
   前端 WebSocket 连接 → 后端 AI 推理 → 推送结果 → 前端实时显示
```

---

## 🛠️ 前端需要修改的地方

### 集成真实 API 后需要修改

**1. 创建 API 请求工具：**
```bash
# 创建文件
src/utils/request.js      # Axios 封装
src/api/auth.js           # 认证接口
src/api/detection.js      # 检测接口
src/api/production.js     # 生产管理接口
src/api/product.js        # 产品管理接口
src/api/model.js          # 模型管理接口
src/api/user.js           # 用户管理接口
```

**2. 修改页面组件：**
- 移除 Mock 数据
- 在 `mounted()` 中调用 API
- 处理加载状态和错误

**示例改造（query.vue）：**
```javascript
// 改造前
data() {
  return {
    records: [{ id: 1, ... }]  // Mock 数据
  }
}

// 改造后
import { getRecords } from '@/api/detection';

data() {
  return {
    records: [],
    loading: false
  }
},
async mounted() {
  this.loading = true;
  try {
    const res = await getRecords({ page: 1, pageSize: 20 });
    this.records = res.data.records;
  } catch (error) {
    console.error('获取记录失败', error);
  } finally {
    this.loading = false;
  }
}
```

---

## 📦 交付物清单

### 后端需要提供

**代码仓库：**
- [ ] 后端项目代码
- [ ] README.md（启动说明）
- [ ] requirements.txt / package.json（依赖列表）
- [ ] .env.example（环境变量模板）

**API 文档：**
- [ ] Swagger / Apifox 在线文档
- [ ] Postman Collection（可选）

**数据库：**
- [ ] 数据库 DDL 脚本
- [ ] 初始化数据脚本
- [ ] ER 图（可选）

**部署文档：**
- [ ] Docker Compose 配置
- [ ] 部署步骤说明
- [ ] 环境变量说明

---

## ⚡ 性能优化建议

### 后端优化
1. **数据库查询优化**
   - 添加必要索引
   - 避免 N+1 查询
   - 使用连接池

2. **缓存策略**
   - 统计数据缓存 5 分钟
   - 用户权限缓存 30 分钟
   - 配置数据缓存 1 小时

3. **接口响应速度**
   - 列表查询 < 500ms
   - 详情查询 < 200ms
   - 统计查询 < 1s

### 前端优化（已实现）
- ✅ 路由懒加载
- ✅ 图表按需加载
- ✅ 列表虚拟滚动（如需要）

---

## 📞 技术支持

### 前端负责人
**姓名：** [您的名字]  
**联系方式：** [您的联系方式]  
**工作时间：** 周一至周五 9:00-18:00

### 对接时间表
- **沟通时间：** 每天 10:00、15:00 定时同步
- **联调时间：** 每周三、周五下午
- **上线时间：** [待定]

---

## ✅ 验收标准

### 功能验收
- [ ] 所有页面能正常访问
- [ ] 所有表单能提交成功
- [ ] 所有列表能正常分页
- [ ] 所有图表能正常展示
- [ ] 实时功能能正常推送

### 性能验收
- [ ] 首屏加载 < 3s
- [ ] 接口响应 < 1s
- [ ] 图表渲染流畅
- [ ] 无明显卡顿

### 安全验收
- [ ] 未登录无法访问内部页面
- [ ] Token 过期自动跳转
- [ ] 权限不足有提示
- [ ] 敏感数据已脱敏

---

## 🎉 预期成果

**完成后的系统能够：**
1. ✅ 用户登录并进入系统
2. ✅ 实时查看检测数据和视频流
3. ✅ 查询历史检测记录
4. ✅ 查看缺陷详情并进行处理
5. ✅ 管理生产线和产品
6. ✅ 部署和管理 AI 模型
7. ✅ 管理用户和权限
8. ✅ 查看各类统计报表

**最终目标：** 一个功能完整、性能稳定、安全可靠的工业缺陷检测管理系统！🚀
