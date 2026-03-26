<template>
  <aside class="w-64 h-full bg-slate-950 border-r border-slate-800 flex flex-col flex-shrink-0">
    <!-- Logo 区域 -->
    <div class="p-6 flex items-center space-x-3">
      <!-- 工业检测系统 Logo -->
      <div
        class="w-10 h-10 rounded-lg flex items-center justify-center shadow-lg shadow-blue-900/20 relative overflow-hidden bg-gradient-to-br from-blue-600 to-indigo-700"
      >
        <!-- 背景网格 -->
        <div class="absolute inset-0 opacity-20" style="background-image: linear-gradient(0deg, transparent 24%, rgba(255, 255, 255, .1) 25%, rgba(255, 255, 255, .1) 26%, transparent 27%, transparent 74%, rgba(255, 255, 255, .1) 75%, rgba(255, 255, 255, .1) 76%, transparent 77%, transparent), linear-gradient(90deg, transparent 24%, rgba(255, 255, 255, .1) 25%, rgba(255, 255, 255, .1) 26%, transparent 27%, transparent 74%, rgba(255, 255, 255, .1) 75%, rgba(255, 255, 255, .1) 76%, transparent 77%, transparent); background-size: 6px 6px;"></div>
        <!-- Logo 图形 -->
        <div class="relative z-10">
          <div class="w-6 h-6 border-2 border-white/90 rounded-md relative">
            <div class="absolute inset-0.5 border border-white/60 rounded-sm"></div>
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-white rounded-full"></div>
            <div class="absolute top-0 right-0 w-1 h-1 bg-blue-300 rounded-full"></div>
          </div>
        </div>
      </div>
      <span class="text-xl font-bold tracking-tight text-white italic">DEEP<span class="text-blue-500">INDUS</span></span>
    </div>

    <!-- 导航菜单 -->
    <nav class="flex-1 px-4 py-4 space-y-2 overflow-y-auto hide-scrollbar">
      <router-link
        to="/detect"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/detect') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/detect') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="mdi:video-input-component"
        ></span>
        <span class="font-medium">实时检测</span>
      </router-link>

      <router-link
        to="/dashboard"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/dashboard') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/dashboard') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="mdi:chart-box"
        ></span>
        <span class="font-medium">数据大屏</span>
      </router-link>

      <router-link
        to="/query"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/query') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/query') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="mdi:history"
        ></span>
        <span class="font-medium">记录管理</span>
      </router-link>

      <router-link
        to="/production"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/production') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/production') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="material-symbols:precision-manufacturing-rounded"
        ></span>
        <span class="font-medium">生产线管理</span>
      </router-link>

      <router-link
        to="/product"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/product') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/product') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="material-symbols:inventory-2-outline"
        ></span>
        <span class="font-medium">产品管理</span>
      </router-link>

      <router-link
        to="/agent"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/agent') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/agent') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="material-symbols:smart-toy-outline"
        ></span>
        <span class="font-medium">AI 助手</span>
      </router-link>

      <!-- 分组标题 -->
      <div class="pt-6 pb-2 px-4">
        <span class="text-xs font-semibold text-slate-500 uppercase tracking-widest">系统设置</span>
      </div>

      <router-link
        to="/model"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/model') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/model') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="material-symbols:neurology-outline"
        ></span>
        <span class="font-medium">AI 模型管理</span>
      </router-link>

      <router-link
        to="/users"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all group"
        active-class="bg-blue-600/10 text-blue-400 border border-blue-600/20"
        :class="{ 'hover:bg-slate-800': !isActive('/users') }"
      >
        <span
          class="iconify text-xl"
          :class="isActive('/users') ? 'text-blue-400' : 'text-slate-400 group-hover:text-blue-400'"
          data-icon="material-symbols:manage-accounts-outline"
        ></span>
        <span class="font-medium">用户权限管理</span>
      </router-link>

      <router-link
        to="/login"
        class="flex items-center space-x-3 px-4 py-3 rounded-xl transition-all hover:bg-slate-800 group"
      >
        <span
          class="iconify text-xl text-slate-400 group-hover:text-blue-400"
          data-icon="mdi:logout"
        ></span>
        <span class="font-medium">退出登录</span>
      </router-link>
    </nav>

    <!-- 底部状态栏 -->
    <div class="p-4 bg-slate-900/50 border-t border-slate-800">
      <div class="bg-slate-800 rounded-xl p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs text-slate-400 uppercase">GPU 负载</span>
          <span class="text-xs font-bold text-emerald-400">{{ gpuLoad }}%</span>
        </div>
        <div class="w-full bg-slate-700 h-1.5 rounded-full overflow-hidden">
          <div class="bg-blue-500 h-full rounded-full transition-all" :style="{ width: gpuLoad + '%' }"></div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
export default {
  name: 'Sidebar',
  data() {
    return {
      gpuLoad: 42,
    };
  },
  methods: {
    isActive(path) {
      return this.$route.path === path;
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
