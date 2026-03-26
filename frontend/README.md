# DEEPINDUS 工业缺陷检测系统 - 前端项目

## 🎯 项目简介

这是一个**工业钢材等产品的外观缺陷检测管理系统**前端项目，通过 AI 视觉检测技术实现：
- ✅ 实时视频流缺陷检测
- ✅ 数据可视化大屏
- ✅ 生产线与产品管理
- ✅ AI 模型管理
- ✅ 用户权限管理

**项目状态：** MVP 已完成，等待后端对接 🚀

---

## 📢 给后端工程师

### 👉 请先阅读：[BACKEND_HANDOVER.md](./BACKEND_HANDOVER.md)

这是专门为后端工程师准备的交接文档，包含：
- 项目概述
- 文档清单
- 快速上手指南
- 开发优先级

### 📚 完整文档目录

所有对接文档都在 `docs/` 目录下：

1. **[文档索引](./docs/INDEX.md)** - 📑 从这里开始
2. **[快速开始](./docs/QUICK_START_FOR_BACKEND.md)** - ⚡ 5分钟上手
3. **[项目说明](./docs/README.md)** - 📋 功能清单
4. **[API 规范](./docs/API_SPECIFICATION.md)** - 🔌 接口定义（最重要）
5. **[数据模型](./docs/DATA_MODELS.md)** - 🗄️ 数据库设计
6. **[对接指南](./docs/INTEGRATION_GUIDE.md)** - 🤝 对接流程
7. **[集成示例](./docs/FRONTEND_API_INTEGRATION.md)** - 💻 代码示例

---

## 🚀 快速启动

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

访问：`http://localhost:5173`

### 构建生产版本
```bash
npm run build
```

---

## 🏗️ 技术栈

- **框架：** Vue 3 (Composition API + Options API)
- **构建工具：** Vite 7
- **路由：** Vue Router 4
- **样式：** Tailwind CSS (CDN)
- **图表：** ECharts 5
- **图标：** Iconify (CDN)

---

## 📁 项目结构

```
c:\frontend/
├── docs/                        # 📚 后端对接文档
│   ├── INDEX.md                 # 文档索引
│   ├── QUICK_START_FOR_BACKEND.md
│   ├── API_SPECIFICATION.md     # ⭐ 接口规范
│   ├── DATA_MODELS.md           # ⭐ 数据模型
│   └── ...
│
├── src/
│   ├── pages/                   # 9个页面组件
│   │   ├── login.vue
│   │   ├── real_time_detect.vue
│   │   ├── data_dashboard.vue
│   │   ├── query.vue
│   │   ├── defect_detail.vue
│   │   ├── production_line.vue
│   │   ├── product_management.vue
│   │   ├── model_management.vue
│   │   └── user_management.vue
│   │
│   ├── components/              # 公共组件
│   │   ├── Layout.vue           # 布局组件
│   │   └── Sidebar.vue          # 侧边栏
│   │
│   ├── router/                  # 路由配置
│   │   └── index.js
│   │
│   └── main.js                  # 入口文件
│
├── BACKEND_HANDOVER.md          # 📋 后端交接文档入口
└── package.json
```

---

## 🎨 系统截图

### 核心功能页面

1. **登录页面** - 用户认证入口
2. **实时检测** - AI 视频流监控
3. **数据大屏** - 可视化仪表盘
4. **历史查询** - 检测记录查询
5. **缺陷详情** - 详细分析页面
6. **生产线管理** - 生产监控
7. **产品管理** - 产品库管理
8. **AI 模型管理** - 模型部署
9. **用户权限** - 权限配置

---

## 📊 功能完成度

| 模块 | 前端完成度 | 后端完成度 |
|------|-----------|-----------|
| 用户认证 | ✅ 100% | ⏳ 0% |
| 实时检测 | ✅ 100% | ⏳ 0% |
| 数据大屏 | ✅ 100% | ⏳ 0% |
| 历史查询 | ✅ 100% | ⏳ 0% |
| 缺陷详情 | ✅ 100% | ⏳ 0% |
| 生产线管理 | ✅ 100% | ⏳ 0% |
| 产品管理 | ✅ 100% | ⏳ 0% |
| AI 模型 | ✅ 100% | ⏳ 0% |
| 用户权限 | ✅ 100% | ⏳ 0% |

**前端 MVP：** ✅ 已完成  
**后端 API：** ⏳ 等待开发

---

## 🎯 下一步

### 对于后端工程师

**立即阅读：** [BACKEND_HANDOVER.md](./BACKEND_HANDOVER.md)

这是你需要的所有信息！

### 对于前端工程师

前端 MVP 已完成，等待后端接口对接。

**待后端完成后需要做：**
1. 创建 `src/utils/request.js` - Axios 封装
2. 创建 `src/api/` 目录 - 接口调用
3. 修改各页面组件 - 调用真实 API
4. 添加路由守卫 - Token 验证
5. 集成 WebSocket - 实时通信

---

## 📝 更新日志

### v1.0.0 - 2026-02-03
- ✅ 完成 9 个核心功能页面
- ✅ 统一配色风格
- ✅ 完成组件化架构
- ✅ 准备后端对接文档

---

## 📞 联系我们

**项目负责人：** [您的名字]  
**技术支持：** [您的联系方式]

---

**让我们一起打造优秀的工业检测系统！** 🎉
