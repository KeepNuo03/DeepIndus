# INDUSCORE 系统 - 前后端对接文档索引

## 📚 文档目录

欢迎后端工程师！请按照以下顺序阅读文档：

---

## 🎯 推荐阅读顺序

### 1️⃣ **快速开始**（必读）⭐
**文件：** [QUICK_START_FOR_BACKEND.md](./QUICK_START_FOR_BACKEND.md)  
**时间：** 10 分钟  
**内容：** 5分钟快速上手、浏览所有页面、第一周目标

👉 **从这里开始！** 让你快速了解项目并运行起来

---

### 2️⃣ **项目说明**（必读）⭐
**文件：** [README.md](./README.md)  
**时间：** 10 分钟  
**内容：** 项目概述、功能模块、技术栈、设计规范

---

### 3️⃣ **API 接口规范**（必读）⭐
**文件：** [API_SPECIFICATION.md](./API_SPECIFICATION.md)  
**时间：** 30 分钟  
**内容：** 详细的接口定义、请求/响应格式、接口优先级

👉 **最重要的文档！** 这是你开发的主要依据

---

### 4️⃣ **数据模型设计**（必读）⭐
**文件：** [DATA_MODELS.md](./DATA_MODELS.md)  
**时间：** 20 分钟  
**内容：** 数据库表结构、字段定义、关系设计、索引建议

👉 **数据库设计参考** 包含完整的表结构和字段说明

---

### 5️⃣ **前后端对接指南**（推荐）
**文件：** [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md)  
**时间：** 20 分钟  
**内容：** 对接流程、技术选型、部署方案、测试方法

---

### 6️⃣ **前端 API 集成示例**（参考）
**文件：** [FRONTEND_API_INTEGRATION.md](./FRONTEND_API_INTEGRATION.md)  
**时间：** 15 分钟  
**内容：** 前端如何调用接口、WebSocket 使用、完整示例代码

👉 **了解前端调用方式** 方便测试和调试

---

## 📋 文档总览

| 文档名称 | 重要性 | 阅读时间 | 主要内容 |
|---------|-------|---------|---------|
| QUICK_START_FOR_BACKEND.md | ⭐⭐⭐ | 10 min | 快速上手指南 |
| README.md | ⭐⭐⭐ | 10 min | 项目概述 |
| API_SPECIFICATION.md | ⭐⭐⭐ | 30 min | 接口定义（最重要） |
| DATA_MODELS.md | ⭐⭐⭐ | 20 min | 数据库设计 |
| INTEGRATION_GUIDE.md | ⭐⭐ | 20 min | 对接流程 |
| FRONTEND_API_INTEGRATION.md | ⭐⭐ | 15 min | 前端集成示例 |

**总计阅读时间：** 约 1.5-2 小时

---

## 🎯 关键信息速查

### 技术栈
- **前端：** Vue 3 + Vite + Tailwind CSS + ECharts
- **后端建议：** FastAPI / NestJS / Spring Boot
- **数据库建议：** PostgreSQL / MySQL
- **缓存：** Redis
- **实时通信：** WebSocket

### API 基础信息
- **Base URL：** `http://api.induscore.com/v1`
- **认证方式：** Bearer Token (JWT)
- **请求格式：** JSON
- **响应格式：** JSON

### 核心接口数量
- 用户认证：3 个
- 实时检测：4 个
- 数据大屏：4 个
- 历史查询：3 个
- 缺陷详情：2 个
- 生产线：4 个
- 产品管理：4 个
- AI 模型：5 个
- 用户权限：8 个

**总计：** 约 37 个接口

### 数据库表数量
- 核心业务表：约 15 张
- 关联表：约 5 张

**总计：** 约 20 张表

---

## 🚀 快速开始三步走

### Step 1: 了解项目（30分钟）
```bash
# 运行前端项目
cd c:\frontend
npm install
npm run dev

# 浏览所有页面
打开浏览器访问 http://localhost:5173
```

### Step 2: 阅读核心文档（1小时）
```
✅ QUICK_START_FOR_BACKEND.md
✅ README.md
✅ API_SPECIFICATION.md
✅ DATA_MODELS.md
```

### Step 3: 开始开发（第2天）
```
1. 搭建后端项目框架
2. 设计数据库表结构
3. 实现用户登录接口
4. 前端联调测试
```

---

## 💡 开发提示

### 看到前端数据，知道后端要返回什么

**前端代码：**
```javascript
// src/pages/query.vue
records: [
  {
    id: 1,
    detectionNo: "#DET-2026A01",
    serialNo: "SN-6729-BM-01",
    defect: "表面严重划痕",
    confidence: "98.4%",
    timestamp: "2026-02-01 14:22:05",
    status: "fail"
  }
]
```

**后端应该返回：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "detectionNo": "#DET-2026A01",
        "serialNo": "SN-6729-BM-01",
        "defect": "表面严重划痕",
        "confidence": "98.4%",
        "timestamp": "2026-02-01 14:22:05",
        "status": "fail"
      }
    ],
    "total": 100
  }
}
```

### 看到前端调用，知道接口怎么写

**前端调用：**
```javascript
const res = await getRecords({
  page: 1,
  pageSize: 20,
  status: 'fail'
});
```

**后端接口：**
```python
# FastAPI 示例
@app.get("/records/query")
def get_records(
    page: int = 1,
    pageSize: int = 20,
    status: str = 'all'
):
    # 查询数据库
    records = db.query_records(page, pageSize, status)
    return {
        "code": 200,
        "data": {
            "records": records,
            "total": total_count
        }
    }
```

---

## 🎁 附加资源

### 前端项目结构图

```
c:\frontend/
│
├── docs/                        # 📚 对接文档（你正在看的）
│   ├── INDEX.md                 # 文档索引
│   ├── QUICK_START_FOR_BACKEND.md  # 快速开始
│   ├── README.md                # 项目说明
│   ├── API_SPECIFICATION.md     # API 规范
│   ├── DATA_MODELS.md           # 数据模型
│   ├── INTEGRATION_GUIDE.md     # 对接指南
│   └── FRONTEND_API_INTEGRATION.md  # 集成示例
│
├── src/                         # 源代码
│   ├── pages/                   # 页面组件（包含 Mock 数据）
│   ├── components/              # 公共组件
│   ├── router/                  # 路由配置
│   └── main.js                  # 入口文件
│
├── index.html                   # HTML 模板
├── package.json                # 依赖配置
└── vite.config.js              # Vite 配置
```

---

## 🤝 合作愉快

感谢选择 INDUSCORE 项目！

**前端团队已经：**
- ✅ 完成 9 个核心功能模块的 UI 开发
- ✅ 统一配色风格
- ✅ 准备详细的对接文档
- ✅ 提供完整的数据示例

**现在交给后端团队：**
- 实现 API 接口
- 设计数据库
- 搭建 WebSocket 服务
- 集成 AI 推理

**让我们一起打造一个优秀的工业检测系统！** 🚀

---

## 📞 遇到问题？

**常见问题：**
1. 前端项目跑不起来？→ 检查 Node.js 版本（需要 >= 20.19.0）
2. 看不懂 Vue 代码？→ 先看业务逻辑，不懂语法可以问
3. 不知道接口怎么写？→ 参考 `API_SPECIFICATION.md`
4. 数据库怎么设计？→ 参考 `DATA_MODELS.md`
5. 不知道从哪开始？→ 先实现登录接口

**随时沟通，我们一起解决！** 💪
