# 前端 API 集成示例

## 📝 说明

本文档提供前端如何调用后端 API 的完整示例代码，供后端工程师参考和测试。

---

## 🔧 步骤 1：安装 Axios

```bash
npm install axios
```

---

## 🔧 步骤 2：创建请求工具

**文件位置：** `src/utils/request.js`

```javascript
import axios from 'axios';
import { useRouter } from 'vue-router';

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8000/v1',  // 后端 API 地址
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('请求错误：', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 根据业务 code 判断
    if (res.code === 200) {
      return res;
    } else {
      // 业务错误
      console.error('业务错误：', res.message);
      alert(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
  },
  error => {
    console.error('响应错误：', error);
    
    // HTTP 状态码错误处理
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // Token 过期或无效
          alert('登录已过期，请重新登录');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
          break;
        case 403:
          alert('权限不足');
          break;
        case 404:
          alert('请求的资源不存在');
          break;
        case 500:
          alert('服务器错误，请稍后重试');
          break;
        default:
          alert(error.response.data?.message || '请求失败');
      }
    } else if (error.request) {
      alert('网络错误，请检查网络连接');
    } else {
      alert('请求配置错误');
    }
    
    return Promise.reject(error);
  }
);

export default request;
```

---

## 🔧 步骤 3：创建 API 接口文件

### 3.1 认证接口

**文件位置：** `src/api/auth.js`

```javascript
import request from '@/utils/request';

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  });
}

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  });
}

// 退出登录
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  });
}
```

### 3.2 检测记录接口

**文件位置：** `src/api/detection.js`

```javascript
import request from '@/utils/request';

// 查询检测记录
export function getRecords(params) {
  return request({
    url: '/records/query',
    method: 'get',
    params
  });
}

// 获取缺陷详情
export function getDefectDetail(id) {
  return request({
    url: `/defect/${id}`,
    method: 'get'
  });
}

// 开始人工复核
export function reviewDefect(id, data) {
  return request({
    url: `/defect/${id}/review`,
    method: 'post',
    data
  });
}

// 批量导出
export function exportRecords(data) {
  return request({
    url: '/records/export',
    method: 'post',
    data
  });
}

// 批量删除
export function deleteRecords(data) {
  return request({
    url: '/records/batch',
    method: 'delete',
    data
  });
}
```

### 3.3 数据大屏接口

**文件位置：** `src/api/dashboard.js`

```javascript
import request from '@/utils/request';

// 获取 KPI 指标
export function getKPI() {
  return request({
    url: '/dashboard/kpi',
    method: 'get'
  });
}

// 获取缺陷趋势
export function getDefectTrend(params) {
  return request({
    url: '/dashboard/defect-trend',
    method: 'get',
    params
  });
}

// 获取缺陷分布
export function getDefectDistribution() {
  return request({
    url: '/dashboard/defect-distribution',
    method: 'get'
  });
}

// 获取报警列表
export function getAlerts() {
  return request({
    url: '/dashboard/alerts',
    method: 'get'
  });
}
```

### 3.4 生产线管理接口

**文件位置：** `src/api/production.js`

```javascript
import request from '@/utils/request';

// 获取生产线列表
export function getProductionLines() {
  return request({
    url: '/production/lines',
    method: 'get'
  });
}

// 控制生产线（启动/停止）
export function controlLine(id, action) {
  return request({
    url: `/production/lines/${id}/control`,
    method: 'post',
    data: { action }
  });
}

// 编辑生产线
export function updateLine(id, data) {
  return request({
    url: `/production/lines/${id}`,
    method: 'put',
    data
  });
}
```

### 3.5 产品管理接口

**文件位置：** `src/api/product.js`

```javascript
import request from '@/utils/request';

// 获取产品列表
export function getProducts(params) {
  return request({
    url: '/products',
    method: 'get',
    params
  });
}

// 新增产品
export function createProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  });
}

// 编辑产品
export function updateProduct(id, data) {
  return request({
    url: `/products/${id}`,
    method: 'put',
    data
  });
}

// 删除产品
export function deleteProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  });
}
```

### 3.6 AI 模型管理接口

**文件位置：** `src/api/model.js`

```javascript
import request from '@/utils/request';

// 获取模型列表
export function getModels() {
  return request({
    url: '/models',
    method: 'get'
  });
}

// 部署模型
export function deployModel(id, data) {
  return request({
    url: `/models/${id}/deploy`,
    method: 'post',
    data
  });
}

// 下线模型
export function undeployModel(id) {
  return request({
    url: `/models/${id}/undeploy`,
    method: 'post'
  });
}

// 上传模型
export function uploadModel(formData) {
  return request({
    url: '/models/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 获取模型对比数据
export function getModelComparison(params) {
  return request({
    url: '/models/comparison',
    method: 'get',
    params
  });
}
```

### 3.7 用户管理接口

**文件位置：** `src/api/user.js`

```javascript
import request from '@/utils/request';

// 获取用户列表
export function getUsers(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  });
}

// 新增用户
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  });
}

// 编辑用户
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  });
}

// 重置密码
export function resetPassword(id) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'post'
  });
}

// 切换用户状态
export function toggleUserStatus(id, status) {
  return request({
    url: `/users/${id}/toggle-status`,
    method: 'post',
    data: { status }
  });
}

// 获取角色列表
export function getRoles() {
  return request({
    url: '/roles',
    method: 'get'
  });
}

// 获取操作日志
export function getOperationLogs(params) {
  return request({
    url: '/logs/operations',
    method: 'get',
    params
  });
}
```

---

## 🔧 步骤 4：在组件中使用 API

### 示例：登录页面改造

**文件：** `src/pages/login.vue`

```javascript
// 在 <script> 中添加
import { login } from '@/api/auth';

export default {
  name: 'LoginPage',
  data() {
    return {
      activeForm: 'login',
      loginForm: {
        username: '',
        password: '',
        remember: false
      },
      loading: false
    };
  },
  methods: {
    async handleLogin() {
      // 简单验证
      if (!this.loginForm.username || !this.loginForm.password) {
        alert('请输入用户名和密码');
        return;
      }

      this.loading = true;
      try {
        // 调用登录接口
        const res = await login(this.loginForm);
        
        // 保存 token 和用户信息
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        
        // 跳转到实时检测页面
        this.$router.push('/detect');
      } catch (error) {
        console.error('登录失败', error);
        // 错误已在拦截器中处理
      } finally {
        this.loading = false;
      }
    }
  }
};
```

### 示例：查询页面改造

**文件：** `src/pages/query.vue`

```javascript
// 在 <script> 中添加
import { getRecords, exportRecords, deleteRecords } from '@/api/detection';

export default {
  name: 'QueryPage',
  data() {
    return {
      records: [],      // 从 API 获取
      loading: false,
      filters: {
        dateRange: '2026/02/01 - 今日',
        defectType: 'all',
        status: 'all',
      },
      currentPage: 1,
      pageSize: 20,
      total: 0
    };
  },
  mounted() {
    this.loadRecords();
  },
  methods: {
    async loadRecords() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          defectType: this.filters.defectType,
          status: this.filters.status,
          search: this.searchQuery
        };
        
        const res = await getRecords(params);
        this.records = res.data.records;
        this.total = res.data.total;
      } catch (error) {
        console.error('加载记录失败', error);
      } finally {
        this.loading = false;
      }
    },
    
    handleQuery() {
      this.currentPage = 1;
      this.loadRecords();
    },
    
    async handleBatchExport() {
      const selectedIds = this.records
        .filter(r => r.selected)
        .map(r => r.id);
      
      if (selectedIds.length === 0) {
        alert('请先选择要导出的记录');
        return;
      }
      
      try {
        const res = await exportRecords({
          recordIds: selectedIds,
          format: 'excel'
        });
        
        // 下载文件
        window.open(res.data.downloadUrl);
      } catch (error) {
        console.error('导出失败', error);
      }
    },
    
    changePage(page) {
      this.currentPage = page;
      this.loadRecords();
    }
  }
};
```

---

## 🔧 步骤 5：WebSocket 集成

**文件位置：** `src/utils/websocket.js`

```javascript
class WebSocketClient {
  constructor(url) {
    this.url = url;
    this.ws = null;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.listeners = new Map();
  }

  // 连接
  connect(token) {
    const wsUrl = `${this.url}?token=${token}`;
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log('WebSocket 已连接');
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        this.handleMessage(message);
      } catch (error) {
        console.error('解析消息失败', error);
      }
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket 错误', error);
    };

    this.ws.onclose = () => {
      console.log('WebSocket 已断开');
      this.stopHeartbeat();
      // 5秒后重连
      this.reconnectTimer = setTimeout(() => {
        this.connect(token);
      }, 5000);
    };
  }

  // 发送消息
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  // 监听消息
  on(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, []);
    }
    this.listeners.get(type).push(callback);
  }

  // 处理消息
  handleMessage(message) {
    const callbacks = this.listeners.get(message.type);
    if (callbacks) {
      callbacks.forEach(callback => callback(message.data));
    }
  }

  // 心跳
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'ping' });
    }, 30000); // 30秒一次
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
    }
  }

  // 断开连接
  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }
    this.stopHeartbeat();
    if (this.ws) {
      this.ws.close();
    }
  }
}

// 导出单例
export const wsClient = new WebSocketClient('ws://localhost:8000/ws');
```

### WebSocket 使用示例

**在实时检测页面：**

```javascript
// src/pages/real_time_detect.vue

import { wsClient } from '@/utils/websocket';

export default {
  mounted() {
    // 连接 WebSocket
    const token = localStorage.getItem('token');
    wsClient.connect(token);

    // 监听检测结果
    wsClient.on('detection', (data) => {
      console.log('收到检测结果', data);
      // 更新页面数据
      this.addNewRecord(data);
    });

    // 监听报警
    wsClient.on('alert', (data) => {
      console.log('收到报警', data);
      // 显示报警提示
      this.showAlert(data);
    });
  },
  
  beforeUnmount() {
    // 断开连接
    wsClient.disconnect();
  },
  
  methods: {
    addNewRecord(data) {
      // 添加到记录列表
      this.recentRecords.unshift({
        id: data.id,
        type: data.type,
        title: data.title,
        description: data.description,
        confidence: data.confidence,
        timestamp: data.timestamp
      });
    }
  }
};
```

---

## 🔧 步骤 6：添加路由守卫

**文件位置：** `src/router/index.js`

在现有路由配置中添加：

```javascript
// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  // 白名单：无需登录即可访问
  const whiteList = ['/', '/login'];
  
  if (whiteList.includes(to.path)) {
    next();
  } else {
    // 需要登录的页面
    if (token) {
      next();
    } else {
      // 未登录，跳转到登录页
      next('/login');
    }
  }
});
```

---

## 📝 完整改造示例

### 登录页面完整改造

**修改 `src/pages/login.vue`：**

```vue
<template>
  <!-- 模板保持不变 -->
  <div>
    <!-- 添加 loading 提示 -->
    <div v-if="loading" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-slate-800 p-6 rounded-xl">
        <span class="text-white">登录中...</span>
      </div>
    </div>
    
    <!-- 原有内容 -->
  </div>
</template>

<script>
import { login, register } from '@/api/auth';

export default {
  name: 'LoginPage',
  data() {
    return {
      activeForm: 'login',
      loading: false,
      loginForm: {
        username: '',
        password: '',
        remember: false
      },
      registerForm: {
        username: '',
        email: '',
        companyCode: '',
        password: '',
        confirmPassword: '',
        agreeTerms: false
      }
    };
  },
  methods: {
    toggleForm(type) {
      this.activeForm = type;
    },
    
    async handleLogin() {
      // 验证表单
      if (!this.loginForm.username || !this.loginForm.password) {
        alert('请输入用户名和密码');
        return;
      }

      this.loading = true;
      try {
        const res = await login(this.loginForm);
        
        // 保存 token 和用户信息
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        
        // 跳转
        this.$router.push('/detect');
      } catch (error) {
        // 错误已在拦截器中处理
      } finally {
        this.loading = false;
      }
    },
    
    async handleRegister() {
      // 验证表单
      if (!this.registerForm.agreeTerms) {
        alert('请先同意服务协议');
        return;
      }
      
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('两次密码输入不一致');
        return;
      }

      this.loading = true;
      try {
        await register(this.registerForm);
        alert('注册成功！请登录');
        this.activeForm = 'login';
      } catch (error) {
        // 错误已在拦截器中处理
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
```

---

## 🧪 后端测试清单

### 使用前端页面测试后端接口

**1. 测试登录接口**
- 打开 `http://localhost:5173/`
- 输入账号密码
- 点击"立即登录"
- 检查：是否返回 token，是否跳转成功

**2. 测试历史查询**
- 登录后访问 `http://localhost:5173/query`
- 检查：页面是否显示记录列表
- 点击分页、筛选、搜索
- 检查：接口参数是否正确传递

**3. 测试数据大屏**
- 访问 `http://localhost:5173/dashboard`
- 检查：KPI 数据、图表数据是否正常

**4. 测试缺陷详情**
- 在查询页面点击"详情"
- 检查：详情数据是否完整

**5. 测试生产线管理**
- 访问 `http://localhost:5173/production`
- 点击"启动/停止"按钮
- 检查：状态是否更新

**6. 测试产品管理**
- 访问 `http://localhost:5173/product`
- 点击"查看详情"
- 检查：侧边栏数据是否正确

**7. 测试 AI 模型**
- 访问 `http://localhost:5173/model`
- 点击"部署"按钮
- 检查：状态是否更新

**8. 测试用户管理**
- 访问 `http://localhost:5173/users`
- 切换标签（用户/角色/日志）
- 检查：数据是否正确加载

---

## ⚠️ 重要提示

### 前端需要后端配合的地方

**1. CORS 配置**
后端必须允许 `http://localhost:5173` 跨域访问

**2. Token 格式**
前端期望收到的 token 格式：
```json
{
  "token": "eyJhbGci..."  // JWT 字符串
}
```

**3. 分页参数**
前端发送：`?page=1&pageSize=20`  
后端返回：`{ data: [...], total: 100 }`

**4. 图片 URL**
必须返回完整的 URL（包含协议和域名）  
示例：`https://cdn.induscore.com/images/xxx.jpg`

**5. WebSocket 连接**
URL 示例：`ws://localhost:8000/ws?token=xxx`

---

## 📞 对接流程

```
1. 后端搭建框架 → 2. 实现登录接口 → 3. 前端测试登录
   ↓
4. 实现业务接口 → 5. 前端联调测试 → 6. 修复问题
   ↓
7. 实现 WebSocket → 8. 前端测试实时功能 → 9. 完成对接
```

---

## 🎯 验收标准

### 联调完成的标准
- [ ] 所有页面能加载真实数据
- [ ] 所有表单能成功提交
- [ ] 所有按钮功能正常
- [ ] 图表数据正确渲染
- [ ] 实时推送功能正常
- [ ] 无接口报错
- [ ] 响应速度符合要求

### 可以上线的标准
- [ ] 所有功能验收通过
- [ ] 性能测试通过
- [ ] 安全测试通过
- [ ] 压力测试通过
- [ ] 文档完整
- [ ] 部署方案明确

---

## 🚀 预期对接时间

**总计：** 2-3 周

- **第 1 周：** 基础功能对接（登录、查询、大屏）
- **第 2 周：** 管理功能对接（生产、产品、模型、用户）
- **第 3 周：** 实时功能对接（WebSocket、AI 推理）+ 测试优化

**并行开发：** 前端可继续优化 UI，后端专注接口开发
