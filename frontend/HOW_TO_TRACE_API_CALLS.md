# 📚 如何追踪前端 API 调用 - 完整教程

## 🎯 以 Upload 接口为例

我以**上传离线检测**功能为例，详细讲解从按钮点击到 API 调用的完整流程。

---

## 🔍 第 1 步：找到按钮在哪里

### 方法 1：搜索按钮文字

在 VS Code 中：
1. 按 `Ctrl + Shift + F`（全局搜索）
2. 搜索：`上传离线检测`
3. 找到文件：`src/pages/real_time_detect.vue` 第 117 行

### 方法 2：在浏览器中查看

1. 打开页面：`http://localhost:5173/detect`
2. 右键点击"上传离线检测"按钮
3. 选择"检查"或"审查元素"
4. 查看 HTML 代码

---

## 📝 第 2 步：查看按钮代码

**文件：** `src/pages/real_time_detect.vue` 第 113-118 行

```vue
<button
  class="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg flex items-center justify-center space-x-2 transition-all font-medium"
  @click="handleUploadClick"
>
  <span class="iconify" data-icon="mdi:upload"></span>
  <span>上传离线检测</span>
</button>
```

**关键点：** `@click="handleUploadClick"` ⭐

这表示点击按钮会调用 `handleUploadClick` 方法。

---

## 🔍 第 3 步：找到方法定义

### 在同一个文件中查找

**按 `Ctrl + F` 搜索：** `handleUploadClick`

**找到位置：** 第 497 行（在 `<script>` 的 `methods` 中）

```javascript
methods: {
  /**
   * 点击上传按钮 - 触发文件选择
   */
  handleUploadClick() {
    // 触发隐藏的 input[type="file"] 点击事件
    this.$refs.fileInput.click();
  },
  // ...
}
```

---

## 🎯 第 4 步：理解整个流程

### 完整流程图

```
用户点击按钮
    ↓
handleUploadClick() 被调用
    ↓
触发隐藏的 <input type="file"> 点击
    ↓
用户选择文件
    ↓
@change 事件触发
    ↓
handleFileSelect() 被调用
    ↓
创建 FormData
    ↓
调用 uploadDetection(formData)
    ↓
src/api/detection.js 中的 uploadDetection()
    ↓
src/utils/request.js 中的 upload()
    ↓
fetch 发送 POST 请求到后端
    ↓
http://localhost:8000/v1/detection/upload
    ↓
后端处理并返回结果
    ↓
前端显示检测结果
```

---

## 💻 完整代码讲解

### 1. HTML 部分（template）

```vue
<!-- 隐藏的文件选择器 -->
<input
  ref="fileInput"              ← ① 设置 ref，可以在 JS 中通过 this.$refs.fileInput 访问
  type="file"                  ← ② 文件上传类型
  accept="image/*"             ← ③ 只接受图片
  class="hidden"               ← ④ 隐藏，不显示在页面上
  @change="handleFileSelect"   ← ⑤ 文件选择后触发此方法
/>

<!-- 上传按钮 -->
<button @click="handleUploadClick">  ← ⑥ 点击触发此方法
  <span>上传离线检测</span>
</button>
```

---

### 2. JavaScript 部分（script）

#### 2.1 点击按钮处理

```javascript
handleUploadClick() {
  // 触发隐藏的 file input 的点击事件
  // 这样用户点击漂亮的按钮，实际上是在选择文件
  this.$refs.fileInput.click();
}
```

**为什么这样做？**
- 原生的 `<input type="file">` 样式很丑
- 我们用自定义按钮代替
- 点击自定义按钮时，触发隐藏的 input 点击

---

#### 2.2 文件选择处理

```javascript
async handleFileSelect(event) {
  // ① 获取用户选择的文件
  const file = event.target.files[0];
  if (!file) return;
  
  // ② 验证文件类型
  if (!file.type.startsWith('image/')) {
    alert('请上传图片文件');
    return;
  }
  
  // ③ 验证文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    alert('图片大小不能超过 10MB');
    return;
  }
  
  // ④ 显示 loading
  this.loading = true;
  this.loadingText = '正在上传并检测...';
  
  try {
    // ⑤ 创建 FormData 对象（用于文件上传）
    const formData = new FormData();
    formData.append('file', file);                        // 添加文件
    formData.append('serialNo', 'SN-UPLOAD-' + Date.now());  // 添加序列号
    formData.append('productId', '1');                    // 添加产品ID
    
    // ⑥ 调用 API（核心！）
    const res = await uploadDetection(formData);
    
    // ⑦ 处理返回结果
    console.log('检测结果', res.data);
    
    if (res.data.has_defect) {
      alert(`检测完成！发现 ${res.data.detection_count} 个缺陷`);
    } else {
      alert('检测完成！未发现缺陷');
    }
    
    // ⑧ 刷新页面数据
    this.loadAllData();
    
  } catch (error) {
    // ⑨ 错误处理
    console.error('上传检测失败', error);
    alert(error.message || '上传失败');
  } finally {
    // ⑩ 隐藏 loading
    this.loading = false;
    // 清空文件选择，允许重复上传同一文件
    this.$refs.fileInput.value = '';
  }
}
```

---

#### 2.3 API 调用（src/api/detection.js）

```javascript
// 第 75-77 行
export function uploadDetection(formData) {
  return upload('/detection/upload', formData);
}
```

这个函数调用了 `request.js` 中的 `upload` 方法。

---

#### 2.4 实际发送请求（src/utils/request.js）

```javascript
// 第 77-87 行
export function upload(url, formData) {
  const token = localStorage.getItem('token');

  return fetch(`${BASE_URL}${url}`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,  // 添加 token
    },
    body: formData,  // 文件数据
  }).then((res) => res.json());
}
```

**最终发送：**
```
POST http://localhost:8000/v1/detection/upload
Headers:
  Authorization: Bearer eyJhbGci...
Body (multipart/form-data):
  file: [文件内容]
  serialNo: SN-UPLOAD-1738579200000
  productId: 1
```

---

## 🔎 如何追踪任意按钮的 API 调用

### 通用方法（适用于所有按钮）

#### 步骤 1：找到按钮文字
例如：`批量导出`

#### 步骤 2：全局搜索按钮文字
```
Ctrl + Shift + F
搜索: 批量导出
```

#### 步骤 3：查看按钮代码
找到后查看是否有 `@click="methodName"`

例如：
```vue
<button @click="handleBatchExport">
  批量导出
</button>
```

#### 步骤 4：搜索方法名
在同一文件中搜索：`handleBatchExport`

#### 步骤 5：查看方法实现
```javascript
async handleBatchExport() {
  // 这里会调用 API
  const res = await exportRecords({...});
}
```

#### 步骤 6：找到 API 定义
查看 import 语句：
```javascript
import { exportRecords } from '@/api/detection';
```

打开 `src/api/detection.js`，找到：
```javascript
export function exportRecords(data) {
  return post('/records/export', data);
}
```

#### 步骤 7：得到接口信息
- **接口路径：** `/records/export`
- **请求方法：** `POST`
- **完整 URL：** `http://localhost:8000/v1/records/export`

---

## 📋 各页面按钮与 API 对应表

### 登录页面（login.vue）

| 按钮文字 | 点击事件 | 调用的方法 | API 接口 |
|---------|---------|-----------|---------|
| 立即登录 | `@submit.prevent="handleLogin"` | `handleLogin()` | `POST /auth/login` |
| 提交注册申请 | `@submit.prevent="handleRegister"` | `handleRegister()` | `POST /auth/register` |

---

### 实时检测页面（real_time_detect.vue）

| 按钮文字 | 点击事件 | 调用的方法 | API 接口 |
|---------|---------|-----------|---------|
| 停止检测/开启检测 | `@click="toggleDetection"` | `toggleDetection()` | `POST /detection/realtime/control` |
| 上传离线检测 | `@click="handleUploadClick"` | `handleFileSelect()` | `POST /detection/upload` |
| 导出今日报表 | `@click="handleExportReport"` | `handleExportReport()` | 待实现 |

**页面加载时自动调用：**
- `GET /detection/realtime/statistics`
- `GET /detection/realtime/records`
- `GET /detection/realtime/trend`

---

### 历史查询页面（query.vue）

| 按钮文字 | 点击事件 | 调用的方法 | API 接口 |
|---------|---------|-----------|---------|
| 查询记录 | `@click="handleQuery"` | `handleQuery()` → `loadRecords()` | `GET /records/query` |
| 重置 | `@click="handleReset"` | `handleReset()` → `loadRecords()` | `GET /records/query` |
| 批量导出 | `@click="handleBatchExport"` | `handleBatchExport()` | `POST /records/export` |
| 批量删除 | `@click="handleBatchDelete"` | `handleBatchDelete()` | `DELETE /records/batch` |
| 刷新 | `@click="handleRefresh"` | `handleRefresh()` → `loadRecords()` | `GET /records/query` |
| 详情 | `@click="handleViewDetail(record)"` | `handleViewDetail()` | 路由跳转 |
| 页码 | `@click="changePage(page)"` | `changePage()` → `loadRecords()` | `GET /records/query` |

**页面加载时自动调用：**
- `GET /records/query?page=1&pageSize=20`

---

### 数据大屏页面（data_dashboard.vue）

**页面加载时自动调用（无按钮）：**
- `GET /dashboard/kpi`
- `GET /dashboard/defect-trend`
- `GET /dashboard/defect-distribution`
- `GET /dashboard/alerts`

---

### 缺陷详情页面（defect_detail.vue）

| 按钮文字 | 点击事件 | 调用的方法 | API 接口 |
|---------|---------|-----------|---------|
| 返回 | `@click="goBack"` | `goBack()` | 路由返回 |
| 开始人工复核 | `@click="handleReview"` | `handleReview()` | `POST /defect/:id/review` |

**页面加载时自动调用：**
- `GET /defect/:id`

---

### 生产线管理页面（production_line.vue）

| 按钮文字 | 点击事件 | 调用的方法 | API 接口 |
|---------|---------|-----------|---------|
| 刷新 | `@click="handleRefresh"` | `handleRefresh()` → `loadProductionLines()` | `GET /production/lines` |
| 新增生产线 | `@click="handleAddLine"` | `handleAddLine()` | 待实现 |
| 启动 | `@click="handleStart(line)"` | `handleStart()` | `POST /production/lines/:id/control` |
| 停止 | `@click="handlePause(line)"` | `handlePause()` | `POST /production/lines/:id/control` |

**页面加载时自动调用：**
- `GET /production/lines`

---

## 🔍 如何在浏览器中追踪 API 调用

### 方法 1：Network 面板（推荐）

1. **打开开发者工具**
   - 按 `F12` 或右键→检查

2. **切换到 Network 标签**
   - 勾选"Preserve log"（保留日志）
   - 清空之前的记录

3. **点击按钮**
   - 例如：点击"上传离线检测"

4. **查看新出现的请求**
   - 名称通常是接口路径，如：`upload`
   - 点击这个请求查看详情

5. **查看请求详情**
   - **Headers 标签页：**
     - Request URL: `http://localhost:8000/v1/detection/upload`
     - Request Method: `POST`
     - Authorization: `Bearer xxx`
   
   - **Payload 标签页：**
     - 查看发送的数据
     - FormData 会显示：file, serialNo, productId
   
   - **Response 标签页：**
     - 查看后端返回的数据

---

### 方法 2：Console 日志

**在代码中添加日志：**

```javascript
async handleFileSelect(event) {
  console.log('=== 开始上传 ===');
  
  const file = event.target.files[0];
  console.log('选择的文件', file);
  
  const formData = new FormData();
  formData.append('file', file);
  console.log('FormData 创建完成');
  
  const res = await uploadDetection(formData);
  console.log('接口返回', res);
  
  console.log('=== 上传完成 ===');
}
```

**在浏览器 Console 查看输出。**

---

## 📖 实际调用示例

### 示例 1：上传离线检测

**步骤 1：用户操作**
```
用户在实时检测页面点击"上传离线检测"按钮
```

**步骤 2：前端处理**
```javascript
// 1. handleUploadClick 被调用
handleUploadClick() {
  this.$refs.fileInput.click();  // 弹出文件选择对话框
}

// 2. 用户选择文件后，handleFileSelect 被调用
handleFileSelect(event) {
  const file = event.target.files[0];
  
  // 3. 创建 FormData
  const formData = new FormData();
  formData.append('file', file);
  
  // 4. 调用 API
  const res = await uploadDetection(formData);
}
```

**步骤 3：API 层**
```javascript
// src/api/detection.js
export function uploadDetection(formData) {
  return upload('/detection/upload', formData);
}
```

**步骤 4：请求工具层**
```javascript
// src/utils/request.js
export function upload(url, formData) {
  const token = localStorage.getItem('token');
  
  return fetch(`http://localhost:8000/v1${url}`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  }).then(res => res.json());
}
```

**步骤 5：实际发送的请求**
```
POST http://localhost:8000/v1/detection/upload

Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...

Body:
  ------WebKitFormBoundary...
  Content-Disposition: form-data; name="file"; filename="test.jpg"
  Content-Type: image/jpeg
  
  [文件二进制内容]
  ------WebKitFormBoundary...
  Content-Disposition: form-data; name="serialNo"
  
  SN-UPLOAD-1738579200000
  ------WebKitFormBoundary...
```

**步骤 6：后端返回**
```json
{
  "code": 200,
  "message": "检测完成",
  "data": {
    "detections": [
      {
        "class_id": 0,
        "class_name": "表面划痕",
        "confidence": 0.984,
        "bbox": { "x1": 100, "y1": 150, "x2": 200, "y2": 250 }
      }
    ],
    "detection_count": 1,
    "has_defect": true,
    "inference_time_ms": 45.2
  }
}
```

**步骤 7：前端显示**
```javascript
// 显示检测结果
alert(`检测完成！发现 ${res.data.detection_count} 个缺陷`);

// 刷新数据
this.loadAllData();
```

---

## 🎓 学习路径

### 如果你想了解某个按钮调用什么接口

**快速方法：**

1. **看按钮代码** → 找到 `@click="methodName"`
2. **搜索方法名** → 在 `<script>` 的 `methods` 中找到
3. **看方法内容** → 找到 `await someAPI(...)`
4. **查看 import** → 找到 `import { someAPI } from '@/api/xxx'`
5. **打开 API 文件** → 看到完整的接口路径

**实战练习：**

试试追踪"批量删除"按钮：
```
1. 搜索"批量删除" → 找到 query.vue
2. 看按钮代码 → @click="handleBatchDelete"
3. 搜索方法 → handleBatchDelete()
4. 看到调用 → await deleteRecords({...})
5. 查看 import → import { deleteRecords } from '@/api/detection'
6. 打开 API 文件 → export function deleteRecords(data) { return del('/records/batch', data); }
7. 得出结论 → DELETE /records/batch
```

---

## 🧪 实际测试 Upload 接口

### 在浏览器中测试

1. **访问实时检测页面**
   ```
   http://localhost:5173/detect
   ```

2. **打开开发者工具（F12）**
   - 切换到 Network 标签
   - 勾选 "Preserve log"

3. **点击"上传离线检测"按钮**

4. **选择一张图片**
   - 例如：test.jpg

5. **观察 Network 面板**
   - 应该看到新的请求：`upload`
   - 点击查看详情

6. **检查请求**
   ```
   Request URL: http://localhost:8000/v1/detection/upload
   Request Method: POST
   Status Code: 200 OK (如果成功)
   
   Request Headers:
     Authorization: Bearer eyJhbGci...
     Content-Type: multipart/form-data; boundary=...
   
   Request Payload:
     file: (binary)
     serialNo: SN-UPLOAD-1738579200000
     productId: 1
   
   Response:
     {
       "code": 200,
       "data": {
         "detections": [...],
         "has_defect": true
       }
     }
   ```

7. **查看页面反应**
   - 应该弹出 Alert：`检测完成！发现 X 个缺陷`
   - 页面数据刷新

---

## 💡 调试技巧

### 1. 在方法中打断点

在 VS Code 中：
1. 找到方法（如 `handleFileSelect`）
2. 点击行号左侧，添加红点（断点）
3. 在浏览器中操作
4. 代码会在断点处暂停
5. 可以查看变量值

### 2. 使用 Console.log

```javascript
handleFileSelect(event) {
  console.log('>>> 1. 文件选择', event.target.files[0]);
  
  const formData = new FormData();
  console.log('>>> 2. 创建 FormData');
  
  const res = await uploadDetection(formData);
  console.log('>>> 3. API 返回', res);
}
```

### 3. 使用 Network 面板

- 查看请求是否发送
- 查看请求参数是否正确
- 查看响应数据是否正确
- 查看状态码（200/401/500）

---

## 🎯 快速参考

### 想知道某个功能用了哪个接口？

**查看 `src/api/` 目录下的文件：**

```
src/api/
├── auth.js          → 登录、注册
├── detection.js     → 检测记录、缺陷详情、实时检测、上传
├── dashboard.js     → 数据大屏
└── production.js    → 生产线管理
```

**每个文件都导出了多个 API 函数，函数名就是功能名。**

### 想知道页面加载时调用了哪些接口？

**查看页面的 `mounted()` 生命周期：**

```javascript
mounted() {
  this.loadAllData();  // 加载数据的方法
}
```

**然后查看 `loadAllData()` 方法，看它调用了哪些 API。**

---

## 📞 总结

### Upload 接口调用链

```
点击按钮
  ↓
@click="handleUploadClick"
  ↓
this.$refs.fileInput.click()
  ↓
用户选择文件
  ↓
@change="handleFileSelect"
  ↓
uploadDetection(formData)
  ↓
src/api/detection.js
  ↓
src/utils/request.js upload()
  ↓
fetch POST http://localhost:8000/v1/detection/upload
  ↓
后端返回结果
  ↓
前端显示 Alert 并刷新数据
```

### 关键点总结

1. **按钮 → 方法：** 通过 `@click="methodName"` 绑定
2. **方法 → API：** 通过 `await apiFunction()` 调用
3. **API → 请求：** 通过 `request.js` 统一发送
4. **请求 → 后端：** fetch 发送 HTTP 请求

**记住这个链条，所有按钮都是这样工作的！** ⭐

---

**现在试试追踪其他按钮吧！** 🚀
