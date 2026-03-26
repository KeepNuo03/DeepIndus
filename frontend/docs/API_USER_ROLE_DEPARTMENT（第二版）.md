# 用户/角色/部门 API 文档（代码对齐版）

## 文档说明

- 本文档以当前后端代码实现为准（`controller + service` 实际行为）。
- 统一前缀：`/v1`
- 统一响应结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710000000000
}
```

- 错误响应结构：

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null,
  "timestamp": 1710000000000
}
```

- 常见错误码：`400` 参数或业务校验失败，`404` 资源不存在，`500` 服务器错误。

---

## 实现状态总览

### 已实现（真实业务逻辑）

- 部门管理：`DepartmentController`
- 角色与权限管理：`RoleController`

### 当前仍为 Mock（占位接口）

- 用户管理：`UserController`
  - `GET /v1/users`
  - `GET /v1/users/roles`

> 说明：用户 CRUD、状态修改、密码重置等接口当前未在控制器层开放，文档不再声明为“已实现”。

---

## 部门管理接口

### 1) 获取部门树（仅 active 部门）

- `GET /v1/departments/tree`
- 响应：`List<DepartmentResponse>`

### 2) 获取部门列表（仅 active 部门）

- `GET /v1/departments`
- 响应：`List<DepartmentResponse>`

### 3) 获取活跃部门列表（下拉）

- `GET /v1/departments/active`
- 响应：`List<DepartmentResponse>`

### 4) 获取部门统计

- `GET /v1/departments/statistics`
- 响应 `data`：

```json
{
  "total": 6,
  "topLevel": 3
}
```

### 5) 获取部门详情

- `GET /v1/departments/{id}`
- 路径参数：
  - `id`：部门 ID

### 6) 创建部门

- `POST /v1/departments`
- `Content-Type: application/json`
- 请求体（`DepartmentRequest`）：

```json
{
  "name": "三车间",
  "code": "WORKSHOP_3",
  "description": "第三生产车间",
  "parentId": 1,
  "managerId": 2,
  "sortOrder": 7,
  "status": "active"
}
```

- 字段约束：
  - `name` 必填，最大 128
  - `code` 必填，最大 64，且唯一
  - `description` 可选，最大 255
  - `status` 建议使用 `active` / `inactive`

### 7) 更新部门（全量请求体）

- `PUT /v1/departments/{id}`
- `Content-Type: application/json`
- 请求体仍为 `DepartmentRequest`，其中 `name` 和 `code` 仍受 `@NotBlank` 约束，**不可只传局部字段**。

```json
{
  "name": "三车间（东区）",
  "code": "WORKSHOP_3",
  "description": "第三生产车间东区厂房",
  "parentId": 1,
  "managerId": 2,
  "sortOrder": 8,
  "status": "active"
}
```

### 8) 删除部门

- `DELETE /v1/departments/{id}`
- 删除限制：
  - 存在子部门时不可删除
  - 部门下存在用户时不可删除

### 9) 更新部门状态

- `POST /v1/departments/{id}/status?status=inactive`
- 查询参数：
  - `status`：仅允许 `active` / `inactive`

### 10) 设置部门负责人

- `POST /v1/departments/{id}/manager?userId=2`
- 查询参数：
  - `userId`：用户 ID（当前接口参数为必填）

---

## 用户接口（当前为 Mock）

### 1) 获取用户列表（Mock）

- `GET /v1/users?page=1&pageSize=20&department=all&status=active&search=`
- 参数：
  - `page` 默认 `1`
  - `pageSize` 默认 `20`
  - `department` 默认 `all`
  - `status` 默认 `active`
  - `search` 可选

- 响应 `data`（当前固定示例结构）：

```json
{
  "statistics": {
    "online": 12,
    "total": 45
  },
  "users": [
    {
      "id": 1,
      "name": "李明",
      "email": "liming@induscore.com",
      "phone": "138****8888",
      "department": "生产部",
      "departmentId": 1,
      "roles": ["admin", "production"],
      "status": "active",
      "online": true,
      "lastLogin": "2026-02-03 14:30",
      "lastIp": "192.168.1.100",
      "createdAt": "2025-12-01",
      "loginCount": 285,
      "avatar": "https://cdn.induscore.com/avatars/user_1.png"
    }
  ],
  "total": 45
}
```

### 2) 获取角色列表（Mock）

- `GET /v1/users/roles`

- 响应 `data`（当前固定示例结构）：

```json
[
  {
    "id": 1,
    "name": "超级管理员",
    "description": "拥有系统所有权限",
    "userCount": 2,
    "permissions": ["user_manage", "role_manage", "system_config"]
  }
]
```

> 注意：历史路径 `GET /v1/roles`（UserController 版本）已移除，避免与角色管理真实接口冲突。

---

## 角色管理接口（真实）

### 1) 角色分页列表

- `GET /v1/roles?page=1&pageSize=20`
- 参数默认值：
  - `page=1`
  - `pageSize=20`
- 响应 `data`：
  - `roles`: `RoleResponse[]`
  - `total`: 总数
  - `page`: 当前页
  - `pageSize`: 每页大小

### 2) 获取所有角色（下拉）

- `GET /v1/roles/all`
- 响应：`RoleResponse[]`

### 3) 获取角色详情

- `GET /v1/roles/{id}`
- 响应：`RoleResponse`

### 4) 创建角色

- `POST /v1/roles`
- `Content-Type: application/json`
- 请求体（`RoleRequest`）：

```json
{
  "name": "测试角色",
  "code": "test_role",
  "description": "用于测试的角色",
  "permissionIds": [1, 2, 3]
}
```

- 字段约束：
  - `name` 必填，最大 64
  - `code` 必填，最大 64，且唯一
  - `description` 可选，最大 255

### 5) 更新角色（全量请求体）

- `PUT /v1/roles/{id}`
- 请求体仍为 `RoleRequest`，其中 `name` 和 `code` 仍受 `@NotBlank` 约束，**不可只传局部字段**。

### 6) 删除角色

- `DELETE /v1/roles/{id}`
- 删除限制：角色下存在用户时不可删除。

### 7) 分配角色权限

- `POST /v1/roles/{id}/permissions`
- `Content-Type: application/json`
- 请求体：`Long[]`（权限 ID 数组）

```json
[1, 2, 3, 4, 5]
```

---

## 权限管理接口（真实）

### 1) 获取所有权限

- `GET /v1/permissions`
- 响应：`PermissionResponse[]`

### 2) 按模块分组获取权限

- `GET /v1/permissions/by-module`
- 响应：`Map<String, PermissionResponse[]>`
- 示例：

```json
{
  "user": [
    {"id": 1, "code": "user:view", "name": "查看用户", "module": "user"}
  ],
  "product": [
    {"id": 13, "code": "product:view", "name": "查看产品", "module": "product"}
  ]
}
```

---

## 主要响应对象说明

### `DepartmentResponse`

- `id`, `name`, `code`, `description`
- `parentId`, `parentName`
- `managerId`, `managerName`
- `sortOrder`, `status`
- `userCount`, `childrenCount`, `children`
- `createdAt`, `updatedAt`

### `RoleResponse`

- `id`, `name`, `code`, `description`
- `userCount`
- `permissions`: `PermissionResponse[]`
- `createdAt`, `updatedAt`

### `PermissionResponse`

- `id`, `code`, `name`, `module`, `description`, `createdAt`

---

## 变更记录（本次修订）

- 修正用户角色列表路径冲突：文档改为 `GET /v1/users/roles`。
- 删除/下线未在控制器开放的用户 CRUD、状态、密码相关 API 声明。
- 标注用户接口当前为 Mock，占位用途。
- 统一补充 `ApiResponse` 标准结构（`code/message/data/timestamp`）。
- 修正部门、角色更新接口文档：请求体为 `@Valid` DTO，不支持仅传局部字段。
# 用户权限管理 + 部门管理 API 接口文档

## 概述

后端已完成用户权限管理和部门管理的真实业务逻辑实现，替换原有的 Mock 数据。

**新增功能模块**：
1. **部门管理** - 多级部门结构、部门负责人、部门人数统计
2. **用户管理** - 完整的 CRUD、部门关联、角色分配、状态管理
3. **角色管理** - 角色权限配置、权限点管理
4. **权限管理** - 基于模块的权限点控制

**初始化数据**：
- 6个部门：生产部、质检部、技术部、管理部、一车间、二车间
- 6个角色：超级管理员、系统管理员、生产主管、质检员、技术员、普通用户
- 24个权限点：覆盖所有功能模块
- 5个测试用户：admin、liming、zhangsan、wangwu、zhaoliu（密码均为123456）

---

## 部门管理接口

### 1. 获取部门树形结构
```
GET /v1/departments/tree
```

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "生产部",
      "code": "PROD",
      "description": "负责生产管理和质检工作",
      "parentId": null,
      "parentName": null,
      "managerId": null,
      "managerName": null,
      "sortOrder": 1,
      "status": "active",
      "userCount": 2,
      "childrenCount": 4,
      "children": [
        {
          "id": 2,
          "name": "质检部",
          "code": "QC",
          "parentId": 1,
          "parentName": "生产部",
          "userCount": 2,
          "children": []
        },
        {
          "id": 5,
          "name": "一车间",
          "code": "WORKSHOP_1",
          "parentId": 1,
          "parentName": "生产部",
          "userCount": 0,
          "children": []
        }
      ]
    }
  ]
}
```

### 2. 获取部门列表（平级）
```
GET /v1/departments
```

### 3. 获取活跃部门（下拉选择用）
```
GET /v1/departments/active
```

### 4. 获取部门统计
```
GET /v1/departments/statistics
```

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "total": 6,
    "topLevel": 3
  }
}
```

### 5. 获取部门详情
```
GET /v1/departments/{id}
```

### 6. 创建部门
```
POST /v1/departments
Content-Type: application/json

{
  "name": "三车间",
  "code": "WORKSHOP_3",
  "description": "第三生产车间",
  "parentId": 1,
  "managerId": null,
  "sortOrder": 7,
  "status": "active"
}
```

### 7. 更新部门
```
PUT /v1/departments/{id}
Content-Type: application/json

{
  "name": "三车间（东区）",
  "description": "第三生产车间东区厂房"
}
```

### 8. 删除部门
```
DELETE /v1/departments/{id}
```
**注意**：部门下有子部门或用户时无法删除

### 9. 更新部门状态
```
POST /v1/departments/{id}/status?status=inactive
```

### 10. 设置部门负责人
```
POST /v1/departments/{id}/manager?userId=2
```

---

## 用户管理接口

### 1. 获取用户列表
```
GET /v1/users?page=1&pageSize=20&department=all&status=all&search=
```

**参数说明**：
- `page`：页码，默认1
- `pageSize`：每页条数，默认20
- `department`：部门ID或'all'
- `status`：'all'/'active'/'inactive'
- `search`：搜索关键字（姓名、用户名、邮箱、手机号）

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "users": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@induscore.com",
        "name": "系统管理员",
        "phone": "13800138000",
        "employeeNo": "EMP001",
        "position": "系统管理员",
        "avatar": "https://cdn.induscore.com/avatars/admin.png",
        "status": "active",
        "onlineStatus": 1,
        "online": true,
        "lastLoginAt": "2026-03-11 14:30:00",
        "lastLoginIp": "192.168.1.100",
        "loginCount": 128,
        "departmentId": 4,
        "departmentName": "管理部",
        "roles": [
          {
            "id": 1,
            "name": "超级管理员",
            "code": "super_admin"
          }
        ],
        "createdAt": "2025-12-01T10:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20,
    "statistics": {
      "total": 5,
      "active": 4,
      "inactive": 1,
      "online": 2
    }
  }
}
```

### 2. 获取用户统计
```
GET /v1/users/statistics
```

### 3. 获取用户详情
```
GET /v1/users/{id}
```

### 4. 创建用户
```
POST /v1/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@induscore.com",
  "name": "测试用户",
  "phone": "13912345678",
  "employeeNo": "EMP006",
  "position": "工程师",
  "departmentId": 1,
  "avatar": "https://example.com/avatar.png",
  "status": "active",
  "password": "123456",
  "roleIds": [4, 5]
}
```

### 5. 更新用户
```
PUT /v1/users/{id}
Content-Type: application/json

{
  "name": "测试用户（已修改）",
  "phone": "13987654321",
  "departmentId": 2,
  "roleIds": [4]
}
```

### 6. 删除用户
```
DELETE /v1/users/{id}
```

### 7. 更新用户状态
```
POST /v1/users/{id}/status?status=inactive
```

### 8. 分配角色
```
POST /v1/users/{id}/roles
Content-Type: application/json

{
  "roleIds": [3, 4]
}
```

### 9. 修改密码
```
POST /v1/users/{id}/password
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

### 10. 重置密码（管理员）
```
POST /v1/users/{id}/reset-password
```

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "newPassword": "123456"
  }
}
```

---

## 角色管理接口

### 1. 获取角色列表（分页）
```
GET /v1/roles?page=1&pageSize=20
```

### 2. 获取所有角色（下拉选择）
```
GET /v1/roles/all
```

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "超级管理员",
      "code": "super_admin",
      "description": "系统最高权限，拥有所有功能",
      "userCount": 1,
      "permissions": [
        {
          "id": 1,
          "code": "user:view",
          "name": "查看用户",
          "module": "user"
        }
      ]
    }
  ]
}
```

### 3. 获取角色详情
```
GET /v1/roles/{id}
```

### 4. 创建角色
```
POST /v1/roles
Content-Type: application/json

{
  "name": "测试角色",
  "code": "test_role",
  "description": "用于测试的角色",
  "permissionIds": [1, 2, 3, 13, 14, 17]
}
```

### 5. 更新角色
```
PUT /v1/roles/{id}
Content-Type: application/json

{
  "name": "测试角色（已修改）",
  "description": "修改后的描述",
  "permissionIds": [1, 2, 3]
}
```

### 6. 删除角色
```
DELETE /v1/roles/{id}
```
**注意**：角色下有用户时无法删除

### 7. 分配权限
```
POST /v1/roles/{id}/permissions
Content-Type: application/json

[1, 2, 3, 4, 5]
```

---

## 权限管理接口

### 1. 获取所有权限
```
GET /v1/permissions
```

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "code": "user:view",
      "name": "查看用户",
      "module": "user",
      "description": "查看用户列表和详情"
    },
    {
      "id": 13,
      "code": "product:view",
      "name": "查看产品",
      "module": "product",
      "description": "查看产品信息"
    }
  ]
}
```

### 2. 按模块获取权限
```
GET /v1/permissions/by-module
```

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "user": [
      {"id": 1, "code": "user:view", "name": "查看用户"},
      {"id": 2, "code": "user:create", "name": "创建用户"},
      {"id": 3, "code": "user:update", "name": "更新用户"},
      {"id": 4, "code": "user:delete", "name": "删除用户"}
    ],
    "product": [
      {"id": 13, "code": "product:view", "name": "查看产品"},
      {"id": 14, "code": "product:create", "name": "创建产品"}
    ]
  }
}
```

---

## 前端实现建议

### 1. 部门管理页面

**树形展示**：
```javascript
// 调用接口获取树形数据
const treeData = await fetch('/v1/departments/tree').then(r => r.json());

// 渲染树形结构，支持展开/折叠
// 显示部门名称、人数、状态徽章
```

**新增/编辑部门**：
- 部门名称、编码（唯一）、描述
- 上级部门（下拉选择，可为空）
- 部门负责人（用户下拉选择）
- 排序号

### 2. 用户管理页面

**用户列表**：
```javascript
// 筛选条件
const params = new URLSearchParams({
  page: 1,
  pageSize: 20,
  department: 'all',  // 或具体部门ID
  status: 'active',   // all/active/inactive
  search: ''          // 搜索关键字
});

const data = await fetch(`/v1/users?${params}`).then(r => r.json());
```

**新增/编辑用户**：
- 基本信息：用户名、邮箱、姓名、手机号、员工编号、职位
- 部门选择：下拉选择部门
- 角色分配：多选角色
- 头像上传
- 密码设置（新增时必填）

**操作按钮**：
- 启用/禁用：调用 `POST /v1/users/{id}/status`
- 分配角色：调用 `POST /v1/users/{id}/roles`
- 重置密码：调用 `POST /v1/users/{id}/reset-password`
- 删除：调用 `DELETE /v1/users/{id}`

### 3. 角色管理页面

**角色列表**：显示角色名称、编码、用户数量

**权限配置**：
```javascript
// 按模块分组展示权限
const permissionsByModule = await fetch('/v1/permissions/by-module').then(r => r.json());

// 用户界面按模块分组展示复选框
// user模块：查看用户、创建用户、更新用户、删除用户
// product模块：查看产品、创建产品...
```

**分配权限**：
- 全选/取消全选
- 按模块批量选择
- 保存时调用 `POST /v1/roles/{id}/permissions`

---

## 权限码对照表

| 模块 | 权限码 | 名称 |
|------|--------|------|
| user | user:view | 查看用户 |
| user | user:create | 创建用户 |
| user | user:update | 更新用户 |
| user | user:delete | 删除用户 |
| role | role:view | 查看角色 |
| role | role:create | 创建角色 |
| role | role:update | 更新角色 |
| role | role:delete | 删除角色 |
| department | dept:view | 查看部门 |
| department | dept:create | 创建部门 |
| department | dept:update | 更新部门 |
| department | dept:delete | 删除部门 |
| product | product:view | 查看产品 |
| product | product:create | 创建产品 |
| product | product:update | 更新产品 |
| product | product:delete | 删除产品 |
| detection | detection:view | 查看检测 |
| detection | detection:control | 控制检测 |
| model | model:view | 查看模型 |
| model | model:deploy | 部署模型 |
| production | production:view | 查看生产线 |
| production | production:control | 控制生产线 |
| system | system:config | 系统配置 |
| log | log:view | 查看日志 |

---

## 注意事项

1. **部门树形结构**：使用 `/v1/departments/tree` 获取完整树形数据
2. **用户在线状态**：`onlineStatus` 字段，1表示在线，0表示离线
3. **角色删除限制**：角色下有用户时无法删除
4. **部门删除限制**：部门下有子部门或用户时无法删除
5. **密码安全**：重置密码后返回明文，前端应提示用户修改
6. **权限校验**：后续会添加基于这些权限点的接口权限控制

---

## 测试账号

| 用户名 | 密码 | 角色 | 部门 |
|--------|------|------|------|
| admin | 123456 | 超级管理员 | 管理部 |
| liming | 123456 | 生产主管、质检员 | 生产部 |
| zhangsan | 123456 | 质检员 | 质检部 |
| wangwu | 123456 | 技术员 | 技术部 |
| zhaoliu | 123456 | 质检员 | 质检部（已禁用）|
