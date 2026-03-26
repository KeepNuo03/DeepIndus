<template>
  <div class="bg-slate-900 text-slate-200 font-sans h-screen flex overflow-hidden">
    <!-- 侧边栏组件 -->
    <Sidebar />

    <!-- 主内容区域 -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <slot></slot>
    </div>

    <button
      v-if="showAgentEntry"
      class="fixed right-6 bottom-6 z-40 w-14 h-14 rounded-full bg-indigo-600 hover:bg-indigo-500 border border-indigo-400/30 shadow-lg shadow-indigo-900/40 flex items-center justify-center transition-all"
      title="打开 AI 助手"
      @click="openAgentPanel"
    >
      <span class="iconify text-2xl text-white" data-icon="material-symbols:smart-toy-outline"></span>
    </button>
  </div>
</template>

<script>
import Sidebar from './Sidebar.vue';

export default {
  name: 'Layout',
  components: {
    Sidebar,
  },
  computed: {
    showAgentEntry() {
      return this.$route.name !== 'AgentChat';
    },
  },
  methods: {
    openAgentPanel() {
      const currentPath = this.$route.fullPath || this.$route.path || '';
      this.$router.push({
        name: 'AgentChat',
        query: {
          scene: 'web_page',
          page: this.$route.name || this.$route.path || 'unknown',
          from: currentPath,
        },
      });
    },
  },
};
</script>

<style scoped>
/* 布局样式 */
</style>
