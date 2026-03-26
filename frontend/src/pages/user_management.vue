<template>
  <Layout>
    <main class="flex-1 flex flex-col min-w-0 bg-[#0f172a] overflow-hidden">
      <!-- 顶部导航 -->
      <header
        class="h-16 bg-[#020617]/50 border-b border-slate-800 flex items-center justify-between px-8 shrink-0"
      >
        <div class="flex items-center space-x-4 text-sm">
          <span class="text-slate-500">系统管理</span>
          <span class="iconify text-slate-700" data-icon="material-symbols:chevron-right"></span>
          <span class="text-indigo-400 font-medium">用户权限管理</span>
        </div>
        <div class="flex items-center space-x-3">
          <div class="flex items-center bg-slate-800/50 rounded-lg px-3 py-1.5 border border-slate-700">
            <span class="iconify text-slate-400 mr-2" data-icon="material-symbols:search"></span>
            <input
              v-model="searchQuery"
              class="bg-transparent border-none outline-none text-xs w-48 text-slate-200"
              placeholder="搜索用户名或邮箱..."
              type="text"
            />
          </div>
          <button
            class="flex items-center space-x-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs transition-all"
            @click="handleAddUser"
          >
            <span class="iconify" data-icon="material-symbols:add"></span>
            <span>新增用户</span>
          </button>
        </div>
      </header>

      <!-- 主内容区 -->
      <div class="flex-1 flex overflow-hidden">
        <!-- 左侧：标签页 -->
        <div class="w-64 border-r border-slate-800 bg-[#020617]/30 flex flex-col shrink-0">
          <div class="p-4">
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-3">视图切换</h3>
            <div class="space-y-1">
              <button
                v-for="tab in tabs"
                :key="tab.id"
                :class="[
                  'w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all text-left',
                  activeTab === tab.id
                    ? 'bg-indigo-600/10 text-indigo-400 border border-indigo-600/20'
                    : 'text-slate-400 hover:bg-slate-800',
                ]"
                @click="activeTab = tab.id"
              >
                <span class="iconify text-xl" :data-icon="tab.icon"></span>
                <span class="font-medium">{{ tab.name }}</span>
                <span class="ml-auto text-xs">{{ tab.count }}</span>
              </button>
            </div>
          </div>

          <!-- 部门树 -->
          <div class="flex-1 p-4 overflow-y-auto hide-scrollbar">
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-3">部门组织</h3>
            <div class="space-y-1">
              <div
                v-for="dept in departments"
                :key="dept.id"
                :class="[
                  'px-3 py-2 rounded-lg text-xs cursor-pointer transition-all',
                  selectedDept === dept.id
                    ? 'bg-slate-800 text-white'
                    : 'text-slate-500 hover:bg-slate-800/50',
                ]"
                @click="selectedDept = dept.id"
              >
                <div class="flex items-center justify-between">
                  <span>{{ dept.name }}</span>
                  <span class="text-[10px]">{{ dept.userCount }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="p-4 border-t border-slate-800">
            <div class="bg-slate-800/50 rounded-xl p-3 space-y-2">
              <div class="flex justify-between text-xs">
                <span class="text-slate-500">在线用户</span>
                <span class="text-emerald-400 font-bold">{{ statistics.online }}</span>
              </div>
              <div class="flex justify-between text-xs">
                <span class="text-slate-500">总用户数</span>
                <span class="text-slate-300 font-bold">{{ statistics.total }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：内容区 -->
        <div class="flex-1 flex flex-col overflow-hidden">
          <!-- 用户列表 -->
          <div v-if="activeTab === 'users'" class="flex-1 overflow-y-auto p-8 space-y-4 hide-scrollbar">
            <div
              v-for="user in filteredUsers"
              :key="user.id"
              class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6 hover:border-indigo-500/50 transition-all"
            >
              <div class="flex items-start justify-between">
                <!-- 用户信息 -->
                <div class="flex items-start space-x-4">
                  <div class="relative">
                    <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-xl font-bold">
                      {{ user.name.charAt(0) }}
                    </div>
                    <span
                      :class="[
                        'absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 border-[#1e293b]',
                        user.online ? 'bg-emerald-500' : 'bg-slate-500',
                      ]"
                    ></span>
                  </div>
                  <div>
                    <div class="flex items-center space-x-3 mb-2">
                      <h3 class="text-lg font-bold text-white">{{ user.name }}</h3>
                      <span
                        :class="[
                          'px-2 py-1 rounded-full text-xs font-bold',
                          user.status === 'active'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : 'bg-slate-700/50 text-slate-400 border border-slate-600/20',
                        ]"
                      >
                        {{ user.status === 'active' ? '正常' : '禁用' }}
                      </span>
                    </div>
                    <div class="flex items-center space-x-4 text-xs text-slate-400 mb-2">
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:mail-outline"></span>
                        <span>{{ user.email }}</span>
                      </span>
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:call-outline"></span>
                        <span>{{ user.phone }}</span>
                      </span>
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:groups-outline"></span>
                        <span>{{ user.department }}</span>
                      </span>
                    </div>
                    <div class="flex items-center space-x-2">
                      <span
                        v-for="role in user.roles"
                        :key="role"
                        :class="[
                          'px-2 py-1 rounded text-[10px] font-bold',
                          getRoleColor(role),
                        ]"
                      >
                        {{ getRoleName(role) }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="flex items-center space-x-2">
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                    title="编辑"
                    @click="handleEdit(user)"
                  >
                    <span class="iconify" data-icon="material-symbols:edit-outline"></span>
                  </button>
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                    title="重置密码"
                    @click="handleResetPassword(user)"
                  >
                    <span class="iconify" data-icon="material-symbols:lock-reset"></span>
                  </button>
                  <button
                    v-if="user.status === 'active'"
                    class="px-3 py-2 bg-amber-600/20 hover:bg-amber-600/30 text-amber-400 rounded-lg text-xs font-medium transition-all"
                    @click="handleDisable(user)"
                  >
                    禁用
                  </button>
                  <button
                    v-else
                    class="px-3 py-2 bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 rounded-lg text-xs font-medium transition-all"
                    @click="handleEnable(user)"
                  >
                    启用
                  </button>
                </div>
              </div>

              <!-- 详细信息 -->
              <div class="grid grid-cols-4 gap-4 mt-4 pt-4 border-t border-slate-700">
                <div>
                  <div class="text-xs text-slate-500 mb-1">最后登录</div>
                  <div class="text-sm text-slate-300">{{ user.lastLogin }}</div>
                </div>
                <div>
                  <div class="text-xs text-slate-500 mb-1">登录IP</div>
                  <div class="text-sm text-slate-300 font-mono">{{ user.lastIp }}</div>
                </div>
                <div>
                  <div class="text-xs text-slate-500 mb-1">创建时间</div>
                  <div class="text-sm text-slate-300">{{ user.createdAt }}</div>
                </div>
                <div>
                  <div class="text-xs text-slate-500 mb-1">登录次数</div>
                  <div class="text-sm text-slate-300">{{ user.loginCount }} 次</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 角色管理 -->
          <div v-else-if="activeTab === 'roles'" class="flex-1 overflow-y-auto p-8 hide-scrollbar">
            <div class="grid grid-cols-3 gap-6">
              <div
                v-for="role in roles"
                :key="role.id"
                class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6 hover:border-indigo-500/50 transition-all"
              >
                <div class="flex items-start justify-between mb-4">
                  <div>
                    <h3 class="text-lg font-bold text-white mb-1">{{ role.name }}</h3>
                    <p class="text-xs text-slate-500">{{ role.description }}</p>
                  </div>
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                    @click="handleEditRole(role)"
                  >
                    <span class="iconify" data-icon="material-symbols:edit-outline"></span>
                  </button>
                </div>

                <div class="bg-slate-900/50 rounded-xl p-4 mb-4">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs text-slate-500">用户数量</span>
                    <span class="text-xl font-bold text-white">{{ role.userCount }}</span>
                  </div>
                </div>

                <div class="space-y-2">
                  <div class="text-xs text-slate-500 mb-2">权限列表</div>
                  <div class="space-y-1 max-h-40 overflow-y-auto hide-scrollbar">
                    <div
                      v-for="permission in role.permissions"
                      :key="permission.id || permission.code"
                      class="flex items-center justify-between bg-slate-800/50 px-3 py-2 rounded-lg text-xs"
                    >
                      <span class="text-slate-300">{{ permission.name || getPermissionName(permission.code) }}</span>
                      <span class="iconify text-emerald-500" data-icon="material-symbols:check-circle"></span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 新增角色卡片 -->
              <button
                class="bg-[#1e293b]/30 border-2 border-dashed border-slate-700 rounded-2xl p-6 hover:border-indigo-500/50 transition-all flex flex-col items-center justify-center min-h-[300px]"
                @click="handleAddRole"
              >
                <span class="iconify text-5xl text-slate-600 mb-3" data-icon="material-symbols:add-circle-outline"></span>
                <span class="text-slate-400 font-medium">新增角色</span>
              </button>
            </div>
          </div>

          <!-- 操作日志 -->
          <div v-else-if="activeTab === 'logs'" class="flex-1 overflow-y-auto p-8 hide-scrollbar">
            <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl overflow-hidden">
              <table class="w-full text-left">
                <thead class="bg-slate-900/50 text-xs text-slate-500 uppercase tracking-wider">
                  <tr>
                    <th class="p-4">时间</th>
                    <th class="p-4">用户</th>
                    <th class="p-4">操作类型</th>
                    <th class="p-4">操作内容</th>
                    <th class="p-4">IP地址</th>
                    <th class="p-4">状态</th>
                  </tr>
                </thead>
                <tbody class="text-sm">
                  <tr
                    v-for="log in operationLogs"
                    :key="log.id"
                    class="border-t border-slate-800 hover:bg-slate-800/30 transition-colors"
                  >
                    <td class="p-4 text-slate-400">{{ log.time }}</td>
                    <td class="p-4">
                      <span class="font-medium text-white">{{ log.user }}</span>
                    </td>
                    <td class="p-4">
                      <span
                        :class="[
                          'px-2 py-1 rounded text-xs font-bold',
                          log.type === 'create'
                            ? 'bg-emerald-500/10 text-emerald-400'
                            : log.type === 'update'
                              ? 'bg-blue-500/10 text-blue-400'
                              : log.type === 'delete'
                                ? 'bg-red-500/10 text-red-400'
                                : 'bg-slate-700/50 text-slate-400',
                        ]"
                      >
                        {{ getLogTypeName(log.type) }}
                      </span>
                    </td>
                    <td class="p-4 text-slate-300">{{ log.content }}</td>
                    <td class="p-4 text-slate-400 font-mono text-xs">{{ log.ip }}</td>
                    <td class="p-4">
                      <span
                        :class="[
                          'px-2 py-1 rounded text-xs font-bold',
                          log.status === 'success'
                            ? 'bg-emerald-500/10 text-emerald-400'
                            : 'bg-red-500/10 text-red-400',
                        ]"
                      >
                        {{ log.status === 'success' ? '成功' : '失败' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </main>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue';
import {
  getUsers,
  getUserRoles,
} from '@/api/user';
import { getDepartmentTree, getActiveDepartments } from '@/api/department';
import { getRoles } from '@/api/role';

export default {
  name: 'UserManagement',
  components: {
    Layout,
  },
  data() {
    return {
      loading: false,
      searchQuery: '',
      activeTab: 'users',
      selectedDept: 'all',
      currentPage: 1,
      pageSize: 20,
      searchDebounceTimer: null,
      totalUsers: 0,
      statistics: {
        online: 0,
        total: 0,
        active: 0,
        inactive: 0,
      },
      tabs: [
        { id: 'users', name: '用户管理', icon: 'material-symbols:person-outline', count: 0 },
        { id: 'roles', name: '角色管理', icon: 'material-symbols:shield-person-outline', count: 0 },
        { id: 'logs', name: '操作日志', icon: 'material-symbols:history', count: 0 },
      ],
      departments: [{ id: 'all', name: '全部部门', userCount: 0 }],
      users: [],
      roles: [],
      operationLogs: [
        {
          id: 1,
          time: '2026-02-03 14:32:15',
          user: '李明',
          type: 'create',
          content: '新增用户：王小明',
          ip: '192.168.1.100',
          status: 'success',
        },
        {
          id: 2,
          time: '2026-02-03 14:28:42',
          user: '张华',
          type: 'update',
          content: '修改角色权限：质检员',
          ip: '192.168.1.101',
          status: 'success',
        },
        {
          id: 3,
          time: '2026-02-03 14:20:18',
          user: '李明',
          type: 'delete',
          content: '删除用户：测试账号01',
          ip: '192.168.1.100',
          status: 'success',
        },
        {
          id: 4,
          time: '2026-02-03 13:55:30',
          user: '王芳',
          type: 'update',
          content: '重置密码：刘强',
          ip: '192.168.1.102',
          status: 'success',
        },
        {
          id: 5,
          time: '2026-02-03 11:42:05',
          user: '张华',
          type: 'login',
          content: '用户登录系统',
          ip: '192.168.1.101',
          status: 'success',
        },
      ],
    };
  },
  computed: {
    filteredUsers() {
      return this.users;
    },
  },
  watch: {
    selectedDept() {
      this.currentPage = 1;
      this.loadUsers();
    },
    searchQuery() {
      this.currentPage = 1;
      this.scheduleLoadUsers();
    },
  },
  mounted() {
    this.loadData();
  },
  beforeUnmount() {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
      this.searchDebounceTimer = null;
    }
  },
  methods: {
    /**
     * 加载所有数据
     */
    async loadData() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadDepartments(),
          this.loadUsers(),
          this.loadRoles(),
        ]);
      } catch (error) {
        console.error('加载数据失败', error);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 加载用户列表
     */
    async loadUsers() {
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          department: this.selectedDept === 'all' ? 'all' : this.selectedDept,
          status: 'active',
          search: this.searchQuery,
        };
        const res = await getUsers(params);
        const data = res.data || {};

        // 映射后端字段到前端字段
        this.users = (data.users || []).map((user) => ({
          id: user.id,
          name: user.name,
          email: user.email,
          phone: user.phone || '-',
          department: user.departmentName || user.department || '-',
          departmentId: String(user.departmentId || ''),
          roles: Array.isArray(user.roles)
            ? user.roles
                .map((r) => (typeof r === 'string' ? r : r.code))
                .filter(Boolean)
            : [],
          status: user.status,
          online: user.online || user.onlineStatus === 1,
          lastLogin: user.lastLogin || user.lastLoginAt || '-',
          lastIp: user.lastLoginIp || '-',
          createdAt: user.createdAt ? String(user.createdAt).split('T')[0] : '-',
          loginCount: user.loginCount || 0,
        }));

        this.totalUsers = data.total || 0;
        const localStatistics = {
          total: this.users.length,
          active: this.users.filter((u) => u.status === 'active').length,
          inactive: this.users.filter((u) => u.status === 'inactive').length,
          online: this.users.filter((u) => u.online).length,
        };
        this.statistics = { ...localStatistics, ...(data.statistics || {}) };

        // 更新 tabs 计数
        this.tabs[0].count = this.statistics.total;
      } catch (error) {
        console.error('加载用户失败', error);
        alert('加载用户列表失败: ' + (error.message || '未知错误'));
      }
    },
    scheduleLoadUsers() {
      if (this.searchDebounceTimer) {
        clearTimeout(this.searchDebounceTimer);
      }
      this.searchDebounceTimer = setTimeout(() => {
        this.loadUsers();
      }, 300);
    },
    flattenDepartmentTree(nodes = [], depth = 0, result = []) {
      nodes.forEach((node) => {
        result.push({
          id: String(node.id),
          name: `${'  '.repeat(depth)}${node.name}`,
          userCount: node.userCount || 0,
        });
        if (Array.isArray(node.children) && node.children.length > 0) {
          this.flattenDepartmentTree(node.children, depth + 1, result);
        }
      });
      return result;
    },

    /**
     * 加载部门列表
     */
    async loadDepartments() {
      try {
        // 按文档优先使用树形接口，前端展开成列表展示
        const res = await getDepartmentTree();
        const tree = res.data || [];
        const depts = this.flattenDepartmentTree(tree);
        const allDeptCount = depts.reduce((sum, d) => sum + (d.userCount || 0), 0);
        this.departments = [
          { id: 'all', name: '全部部门', userCount: allDeptCount },
          ...depts,
        ];
      } catch (error) {
        // 树形接口异常时降级为活跃部门接口，保证页面可用
        try {
          const fallback = await getActiveDepartments();
          const activeDepts = fallback.data || [];
          const allDeptCount = activeDepts.reduce((sum, d) => sum + (d.userCount || 0), 0);
          this.departments = [
            { id: 'all', name: '全部部门', userCount: allDeptCount },
            ...activeDepts.map((d) => ({
              id: String(d.id),
              name: d.name,
              userCount: d.userCount || 0,
            })),
          ];
        } catch (fallbackError) {
          console.error('加载部门失败', fallbackError);
        }
      }
    },

    /**
     * 加载角色列表
     */
    async loadRoles() {
      try {
        const [rolePageRes, userRoleRes] = await Promise.all([
          getRoles({ page: 1, pageSize: 100 }),
          getUserRoles(),
        ]);
        const rolePageData = rolePageRes.data || {};
        const roles = Array.isArray(rolePageData.roles)
          ? rolePageData.roles
          : Array.isArray(rolePageData)
            ? rolePageData
            : [];
        const userRoles = userRoleRes.data || [];
        const roleNameMap = new Map(
          userRoles
            .filter((r) => r && r.id != null)
            .map((r) => [String(r.id), { name: r.name, description: r.description }])
        );

        this.roles = roles.map((role) => ({
          id: role.id,
          name: roleNameMap.get(String(role.id))?.name || role.name,
          description: roleNameMap.get(String(role.id))?.description || role.description || '-',
          userCount: role.userCount || 0,
          permissions: role.permissions
            ? role.permissions.map((p) => ({
                id: p.id,
                code: p.code,
                name: p.name,
              }))
            : [],
        }));

        // 更新 tabs 计数
        this.tabs[1].count = this.roles.length;
      } catch (error) {
        console.error('加载角色失败', error);
      }
    },

    getRoleColor(role) {
      const colors = {
        admin: 'bg-red-500/10 text-red-400 border border-red-500/20',
        production: 'bg-blue-500/10 text-blue-400 border border-blue-500/20',
        quality: 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20',
        tech: 'bg-purple-500/10 text-purple-400 border border-purple-500/20',
        inspector: 'bg-amber-500/10 text-amber-400 border border-amber-500/20',
        super_admin: 'bg-red-500/10 text-red-400 border border-red-500/20',
        system_admin: 'bg-indigo-500/10 text-indigo-400 border border-indigo-500/20',
        production_manager: 'bg-blue-500/10 text-blue-400 border border-blue-500/20',
        quality_inspector: 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20',
        technician: 'bg-purple-500/10 text-purple-400 border border-purple-500/20',
        operator: 'bg-slate-600/10 text-slate-400 border border-slate-600/20',
      };
      return colors[role] || colors.operator;
    },
    getRoleName(role) {
      const names = {
        admin: '管理员',
        production: '生产',
        quality: '质检',
        tech: '技术',
        inspector: '检验员',
        super_admin: '超级管理员',
        system_admin: '系统管理员',
        production_manager: '生产主管',
        quality_inspector: '质检员',
        technician: '技术员',
        operator: '普通用户',
      };
      return names[role] || role;
    },
    getPermissionName(permission) {
      const names = {
        'user:view': '查看用户',
        'user:create': '创建用户',
        'user:update': '更新用户',
        'user:delete': '删除用户',
        'role:view': '查看角色',
        'role:create': '创建角色',
        'role:update': '更新角色',
        'role:delete': '删除角色',
        'dept:view': '查看部门',
        'dept:create': '创建部门',
        'dept:update': '更新部门',
        'dept:delete': '删除部门',
        'product:view': '查看产品',
        'product:create': '创建产品',
        'product:update': '更新产品',
        'product:delete': '删除产品',
        'detection:view': '查看检测',
        'detection:control': '控制检测',
        'model:view': '查看模型',
        'model:deploy': '部署模型',
        'production:view': '查看生产线',
        'production:control': '控制生产线',
        'system:config': '系统配置',
        'log:view': '查看日志',
      };
      return names[permission] || permission;
    },
    getLogTypeName(type) {
      const names = {
        create: '新增',
        update: '修改',
        delete: '删除',
        login: '登录',
      };
      return names[type] || type;
    },
    handleAddUser() {
      console.log('新增用户');
      alert('新增用户功能开发中...');
    },
    handleEdit(user) {
      console.log('编辑用户', user);
      alert(`编辑用户 ${user.name} 功能开发中...`);
    },
    handleResetPassword() {
      alert('第二版接口文档中用户重置密码接口当前未开放（Mock阶段）。');
    },
    handleDisable() {
      alert('第二版接口文档中用户状态修改接口当前未开放（Mock阶段）。');
    },
    handleEnable() {
      alert('第二版接口文档中用户状态修改接口当前未开放（Mock阶段）。');
    },
    handleAddRole() {
      console.log('新增角色');
      alert('新增角色功能开发中...');
    },
    handleEditRole(role) {
      console.log('编辑角色', role);
      alert(`编辑角色 ${role.name} 功能开发中...`);
    },
  },
};
</script>

<style scoped>
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
