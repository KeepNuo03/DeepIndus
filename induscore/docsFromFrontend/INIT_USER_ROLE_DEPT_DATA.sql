-- 初始化数据脚本（适用于直接终端执行）
-- 执行前请确保表结构已创建（V1-V4 迁移已执行）

-- ============================================
-- 1. 创建部门表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '部门名称',
    code VARCHAR(64) NOT NULL UNIQUE COMMENT '部门编码',
    description VARCHAR(255) COMMENT '部门描述',
    parent_id BIGINT COMMENT '上级部门ID',
    manager_id BIGINT COMMENT '部门负责人ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT NOW(),
    updated_at DATETIME NOT NULL DEFAULT NOW(),
    FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================
-- 2. 扩展 users 表字段（如果不存在则添加）
-- ============================================
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS department_id BIGINT COMMENT '所属部门ID',
ADD COLUMN IF NOT EXISTS phone VARCHAR(20) COMMENT '手机号',
ADD COLUMN IF NOT EXISTS last_login_at DATETIME COMMENT '最后登录时间',
ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(64) COMMENT '最后登录IP',
ADD COLUMN IF NOT EXISTS login_count INT DEFAULT 0 COMMENT '登录次数',
ADD COLUMN IF NOT EXISTS online_status TINYINT DEFAULT 0 COMMENT '在线状态',
ADD COLUMN IF NOT EXISTS employee_no VARCHAR(64) COMMENT '员工编号',
ADD COLUMN IF NOT EXISTS position VARCHAR(128) COMMENT '职位';

-- 添加外键（如果不存在）
ALTER TABLE users DROP FOREIGN KEY IF EXISTS fk_user_department;
ALTER TABLE users ADD CONSTRAINT fk_user_department 
FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department_id);
CREATE INDEX IF NOT EXISTS idx_users_employee_no ON users(employee_no);

-- ============================================
-- 3. 初始化部门数据（使用 REPLACE INTO 避免重复）
-- ============================================
REPLACE INTO departments (id, name, code, description, parent_id, sort_order, status, created_at, updated_at) VALUES
(1, '生产部', 'PROD', '负责生产管理和质检工作', NULL, 1, 'active', NOW(), NOW()),
(2, '质检部', 'QC', '负责产品质量检测', 1, 2, 'active', NOW(), NOW()),
(3, '技术部', 'TECH', '负责技术开发和系统维护', NULL, 3, 'active', NOW(), NOW()),
(4, '管理部', 'MGMT', '负责公司行政管理', NULL, 4, 'active', NOW(), NOW()),
(5, '一车间', 'WORKSHOP_1', '第一生产车间', 1, 5, 'active', NOW(), NOW()),
(6, '二车间', 'WORKSHOP_2', '第二生产车间', 1, 6, 'active', NOW(), NOW());

-- ============================================
-- 4. 初始化权限数据（使用 REPLACE INTO 避免重复）
-- ============================================
REPLACE INTO permissions (id, code, name, module, description, created_at, updated_at) VALUES
(1, 'user:view', '查看用户', 'user', '查看用户列表和详情', NOW(), NOW()),
(2, 'user:create', '创建用户', 'user', '创建新用户', NOW(), NOW()),
(3, 'user:update', '更新用户', 'user', '修改用户信息', NOW(), NOW()),
(4, 'user:delete', '删除用户', 'user', '删除用户', NOW(), NOW()),
(5, 'role:view', '查看角色', 'role', '查看角色列表和详情', NOW(), NOW()),
(6, 'role:create', '创建角色', 'role', '创建新角色', NOW(), NOW()),
(7, 'role:update', '更新角色', 'role', '修改角色信息', NOW(), NOW()),
(8, 'role:delete', '删除角色', 'role', '删除角色', NOW(), NOW()),
(9, 'dept:view', '查看部门', 'department', '查看部门列表', NOW(), NOW()),
(10, 'dept:create', '创建部门', 'department', '创建新部门', NOW(), NOW()),
(11, 'dept:update', '更新部门', 'department', '修改部门信息', NOW(), NOW()),
(12, 'dept:delete', '删除部门', 'department', '删除部门', NOW(), NOW()),
(13, 'product:view', '查看产品', 'product', '查看产品信息', NOW(), NOW()),
(14, 'product:create', '创建产品', 'product', '创建新产品', NOW(), NOW()),
(15, 'product:update', '更新产品', 'product', '修改产品信息', NOW(), NOW()),
(16, 'product:delete', '删除产品', 'product', '删除产品', NOW(), NOW()),
(17, 'detection:view', '查看检测', 'detection', '查看检测记录', NOW(), NOW()),
(18, 'detection:control', '控制检测', 'detection', '启停检测任务', NOW(), NOW()),
(19, 'model:view', '查看模型', 'model', '查看AI模型', NOW(), NOW()),
(20, 'model:deploy', '部署模型', 'model', '部署AI模型', NOW(), NOW()),
(21, 'production:view', '查看生产线', 'production', '查看生产线信息', NOW(), NOW()),
(22, 'production:control', '控制生产线', 'production', '启停生产线', NOW(), NOW()),
(23, 'system:config', '系统配置', 'system', '系统配置管理', NOW(), NOW()),
(24, 'log:view', '查看日志', 'log', '查看操作日志', NOW(), NOW());

-- ============================================
-- 5. 初始化角色数据（使用 REPLACE INTO 避免重复）
-- ============================================
REPLACE INTO roles (id, name, code, description, created_at, updated_at) VALUES
(1, '超级管理员', 'super_admin', '系统最高权限，拥有所有功能', NOW(), NOW()),
(2, '系统管理员', 'admin', '系统管理，用户/角色/部门管理', NOW(), NOW()),
(3, '生产主管', 'production_manager', '生产管理，生产线控制', NOW(), NOW()),
(4, '质检员', 'qc_inspector', '质检工作，查看检测记录', NOW(), NOW()),
(5, '技术员', 'technician', '技术维护，模型管理', NOW(), NOW()),
(6, '普通用户', 'user', '普通查看权限', NOW(), NOW());

-- ============================================
-- 6. 清空并重新初始化角色-权限关联
-- ============================================
-- 先清空现有权限关联（避免累积）
DELETE FROM role_permissions WHERE role_id IN (1, 2, 3, 4, 5, 6);

-- 超级管理员拥有所有权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
(1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24);

-- 系统管理员：用户/角色/部门管理权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
(2, 9), (2, 10), (2, 11), (2, 12), (2, 23), (2, 24);

-- 生产主管：产品和生产线管理
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 13), (3, 14), (3, 15), (3, 16), (3, 21), (3, 22), (3, 17);

-- 质检员：查看检测记录
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 17), (4, 18);

-- 技术员：模型管理
INSERT INTO role_permissions (role_id, permission_id) VALUES
(5, 19), (5, 20), (5, 17);

-- 普通用户：只读权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
(6, 1), (6, 5), (6, 9), (6, 13), (6, 17), (6, 19), (6, 21);

-- ============================================
-- 7. 初始化用户数据（密码都是 '123456' 的 BCrypt 加密）
-- ============================================
-- 先清空现有用户角色关联（避免重复错误）
DELETE FROM user_roles WHERE user_id IN (1, 2, 3, 4, 5);

-- 删除并重新创建测试用户
DELETE FROM users WHERE id IN (1, 2, 3, 4, 5);

INSERT INTO users (id, username, email, name, phone, employee_no, position, department_id, avatar, 
    password_hash, status, online_status, login_count, created_at, updated_at) VALUES
(1, 'admin', 'admin@induscore.com', '系统管理员', '13800138000', 'EMP001', '系统管理员', 4, 
    'https://cdn.induscore.com/avatars/admin.png',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'active', 1, 128, NOW(), NOW()),
(2, 'liming', 'liming@induscore.com', '李明', '13812345678', 'EMP002', '生产主管', 1,
    'https://cdn.induscore.com/avatars/user_2.png',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'active', 0, 85, NOW(), NOW()),
(3, 'zhangsan', 'zhangsan@induscore.com', '张三', '13987654321', 'EMP003', '质检员', 2,
    'https://cdn.induscore.com/avatars/user_3.png',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'active', 0, 56, NOW(), NOW()),
(4, 'wangwu', 'wangwu@induscore.com', '王五', '13711112222', 'EMP004', '技术员', 3,
    'https://cdn.induscore.com/avatars/user_4.png',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'active', 1, 42, NOW(), NOW()),
(5, 'zhaoliu', 'zhaoliu@induscore.com', '赵六', '13633334444', 'EMP005', '质检员', 2,
    'https://cdn.induscore.com/avatars/user_5.png',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'inactive', 0, 12, NOW(), NOW());

-- ============================================
-- 8. 分配用户角色
-- ============================================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),  -- admin 是超级管理员
(2, 3),  -- 李明是生产主管
(2, 4),  -- 李明也是质检员
(3, 4),  -- 张三是质检员
(4, 5),  -- 王五是技术员
(5, 4);  -- 赵六是质检员

-- ============================================
-- 9. 创建操作日志表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    user_name VARCHAR(64) COMMENT '操作用户名',
    operation_type VARCHAR(32) NOT NULL COMMENT '操作类型',
    module VARCHAR(64) NOT NULL COMMENT '操作模块',
    description VARCHAR(512) COMMENT '操作描述',
    request_method VARCHAR(16) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_status INT COMMENT '响应状态码',
    ip_address VARCHAR(64) COMMENT '操作IP地址',
    user_agent VARCHAR(255) COMMENT '浏览器User-Agent',
    duration_ms INT COMMENT '执行耗时（毫秒）',
    status VARCHAR(16) DEFAULT 'success' COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL,
    INDEX idx_operation_user (user_id),
    INDEX idx_operation_module (module),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 完成提示
-- ============================================
SELECT '初始化完成！' AS message;
SELECT CONCAT('部门数量: ', COUNT(*)) FROM departments;
SELECT CONCAT('角色数量: ', COUNT(*)) FROM roles;
SELECT CONCAT('权限数量: ', COUNT(*)) FROM permissions;
SELECT CONCAT('用户数量: ', COUNT(*)) FROM users WHERE id IN (1,2,3,4,5);
