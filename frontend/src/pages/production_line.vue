<template>
  <Layout>
    <main class="flex-1 flex flex-col min-w-0 bg-[#0f172a] overflow-hidden">
      <!-- 顶部导航 -->
      <header
        class="h-16 bg-[#020617]/50 border-b border-slate-800 flex items-center justify-between px-8 shrink-0"
      >
        <div class="flex items-center space-x-4 text-sm">
          <span class="text-slate-500">生产管理</span>
          <span class="iconify text-slate-700" data-icon="material-symbols:chevron-right"></span>
          <span class="text-indigo-400 font-medium">生产线管理</span>
        </div>
        <div class="flex items-center space-x-3">
          <button
            class="flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg text-xs transition-all"
            @click="handleRefresh"
          >
            <span class="iconify" data-icon="material-symbols:refresh"></span>
            <span>刷新</span>
          </button>
          <button
            class="flex items-center space-x-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs transition-all"
            @click="handleAddLine"
          >
            <span class="iconify" data-icon="material-symbols:add"></span>
            <span>新增生产线</span>
          </button>
        </div>
      </header>

      <!-- 主内容区 -->
      <div class="flex-1 overflow-y-auto p-8 space-y-6 hide-scrollbar">
        <!-- 总览统计 -->
        <div class="grid grid-cols-4 gap-6">
          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-emerald-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">运行中</span>
              <span class="iconify text-emerald-500 text-2xl" data-icon="material-symbols:play-circle-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-emerald-400">{{ statistics.running }}</h3>
              <span class="text-xs text-slate-500">/ {{ statistics.total }}</span>
            </div>
          </div>

          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-amber-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">维护中</span>
              <span class="iconify text-amber-500 text-2xl" data-icon="material-symbols:build-circle-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-amber-400">{{ statistics.maintenance }}</h3>
              <span class="text-xs text-slate-500">条生产线</span>
            </div>
          </div>

          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-blue-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">今日产量</span>
              <span class="iconify text-blue-500 text-2xl" data-icon="material-symbols:package-2-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-blue-400">{{ statistics.todayProduction }}</h3>
              <span class="text-emerald-500 text-xs font-medium">+8.2%</span>
            </div>
          </div>

          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-purple-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">平均良率</span>
              <span class="iconify text-purple-500 text-2xl" data-icon="material-symbols:verified-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-purple-400">{{ statistics.avgYield }}</h3>
              <span class="text-slate-500 text-xs">%</span>
            </div>
          </div>
        </div>

        <!-- 生产线列表 -->
        <div class="grid grid-cols-1 gap-6">
          <div
            v-for="line in productionLines"
            :key="line.id"
            class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl overflow-hidden hover:border-indigo-500/50 transition-all"
          >
            <div class="p-6">
              <div class="flex items-start justify-between mb-6">
                <!-- 生产线基本信息 -->
                <div class="flex items-start space-x-4">
                  <div
                    :class="[
                      'w-14 h-14 rounded-xl flex items-center justify-center',
                      line.status === 'running'
                        ? 'bg-emerald-500/10 border-2 border-emerald-500/20'
                        : line.status === 'maintenance'
                          ? 'bg-amber-500/10 border-2 border-amber-500/20'
                          : 'bg-slate-700/50 border-2 border-slate-600/20',
                    ]"
                  >
                    <span
                      :class="[
                        'iconify text-3xl',
                        line.status === 'running'
                          ? 'text-emerald-500'
                          : line.status === 'maintenance'
                            ? 'text-amber-500'
                            : 'text-slate-500',
                      ]"
                      data-icon="material-symbols:precision-manufacturing-rounded"
                    ></span>
                  </div>
                  <div>
                    <div class="flex items-center space-x-3 mb-2">
                      <h3 class="text-xl font-bold text-white">{{ line.name }}</h3>
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-bold flex items-center space-x-1',
                          line.status === 'running'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : line.status === 'maintenance'
                              ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                              : 'bg-slate-700/50 text-slate-400 border border-slate-600/20',
                        ]"
                      >
                        <span
                          :class="[
                            'w-2 h-2 rounded-full',
                            line.status === 'running'
                              ? 'bg-emerald-500 animate-pulse'
                              : line.status === 'maintenance'
                                ? 'bg-amber-500'
                                : 'bg-slate-500',
                          ]"
                        ></span>
                        <span>{{ getStatusText(line.status) }}</span>
                      </span>
                    </div>
                    <div class="flex items-center space-x-4 text-xs text-slate-400">
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:location-on-outline"></span>
                        <span>{{ line.location }}</span>
                      </span>
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:groups-outline"></span>
                        <span>{{ line.shift }}</span>
                      </span>
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:schedule"></span>
                        <span>运行时长: {{ line.runningTime }}</span>
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="flex items-center space-x-2">
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                    title="编辑"
                    @click="handleEdit(line)"
                  >
                    <span class="iconify" data-icon="material-symbols:edit-outline"></span>
                  </button>
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                    title="设置"
                    @click="handleSettings(line)"
                  >
                    <span class="iconify" data-icon="material-symbols:settings-outline"></span>
                  </button>
                  <button
                    v-if="line.status === 'running'"
                    class="px-3 py-2 bg-amber-600/20 hover:bg-amber-600/30 text-amber-400 rounded-lg text-xs font-medium transition-all"
                    @click="handlePause(line)"
                  >
                    停止
                  </button>
                  <button
                    v-else
                    class="px-3 py-2 bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 rounded-lg text-xs font-medium transition-all"
                    @click="handleStart(line)"
                  >
                    启动
                  </button>
                </div>
              </div>

              <!-- 生产数据 -->
              <div class="grid grid-cols-6 gap-4 mb-6">
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">今日产量</div>
                  <div class="text-xl font-bold text-white">{{ line.todayOutput }}</div>
                  <div class="text-xs text-emerald-400 mt-1">目标: {{ line.targetOutput }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">良品数</div>
                  <div class="text-xl font-bold text-emerald-400">{{ line.qualifiedCount }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">次品数</div>
                  <div class="text-xl font-bold text-red-400">{{ line.defectCount }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">良率</div>
                  <div class="text-xl font-bold text-blue-400">{{ line.yieldRate }}%</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">稼动率</div>
                  <div class="text-xl font-bold text-purple-400">{{ line.utilizationRate }}%</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">平均节拍</div>
                  <div class="text-xl font-bold text-slate-300">{{ line.cycleTime }}s</div>
                </div>
              </div>

              <!-- 可生产产品 -->
              <div class="mb-6">
                <div class="flex items-center space-x-2 mb-3">
                  <span class="iconify text-blue-500" data-icon="material-symbols:inventory-2-outline"></span>
                  <span class="text-sm font-bold text-slate-300">可生产产品</span>
                </div>
                <div v-if="getLineProducts(line.id).length > 0" class="flex flex-wrap gap-2">
                  <span
                    v-for="product in getLineProducts(line.id)"
                    :key="product.id || $index"
                    class="px-3 py-1.5 bg-blue-500/10 text-blue-400 border border-blue-500/20 rounded-lg text-xs font-medium"
                  >
                    {{ product.name || product.productName || '未命名产品' }}
                  </span>
                </div>
                <div v-else class="text-xs text-slate-500">
                  暂无产品数据 ({{ lineProducts[line.id] ? '空数组' : '未加载' }})
                </div>
              </div>

              <!-- 摄像头与工位 -->
              <div class="grid grid-cols-2 gap-4">
                <!-- 摄像头配置 -->
                <div class="bg-slate-900/30 border border-slate-800 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-3">
                    <h4 class="text-sm font-bold flex items-center">
                      <span class="iconify text-blue-500 mr-2" data-icon="material-symbols:videocam-outline"></span>
                      摄像头配置
                    </h4>
                    <button
                      class="text-xs text-indigo-400 hover:text-indigo-300"
                      @click="handleConfigCamera(line)"
                    >
                      配置
                    </button>
                  </div>
                  <div class="space-y-2">
                    <div
                      v-for="camera in line.cameras"
                      :key="camera.id"
                      class="flex items-center justify-between text-xs bg-slate-800/50 px-3 py-2 rounded-lg"
                    >
                      <div class="flex items-center space-x-2">
                        <span
                          :class="[
                            'w-2 h-2 rounded-full',
                            camera.online ? 'bg-emerald-500' : 'bg-slate-500',
                          ]"
                        ></span>
                        <span class="text-slate-300">{{ camera.name }}</span>
                      </div>
                      <span class="text-slate-500">{{ camera.position }}</span>
                    </div>
                  </div>
                </div>

                <!-- 工位状态 -->
                <div class="bg-slate-900/30 border border-slate-800 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-3">
                    <h4 class="text-sm font-bold flex items-center">
                      <span class="iconify text-emerald-500 mr-2" data-icon="material-symbols:workspaces-outline"></span>
                      工位状态
                    </h4>
                    <button
                      class="text-xs text-indigo-400 hover:text-indigo-300"
                      @click="handleConfigStation(line)"
                    >
                      管理
                    </button>
                  </div>
                  <div class="grid grid-cols-4 gap-2">
                    <div
                      v-for="station in line.stations"
                      :key="station.id"
                      :class="[
                        'p-2 rounded-lg text-center text-xs font-bold',
                        station.status === 'working'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : station.status === 'idle'
                            ? 'bg-slate-700/50 text-slate-400 border border-slate-600/20'
                            : 'bg-amber-500/10 text-amber-400 border border-amber-500/20',
                      ]"
                    >
                      {{ station.name }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue';
import { getProductionLines, controlLine, getLineProducts } from '@/api/production';

export default {
  name: 'ProductionLine',
  components: {
    Layout,
  },
  data() {
    return {
      loading: false,
      statistics: {
        total: 0,
        running: 0,
        maintenance: 0,
        todayProduction: 0,
        avgYield: 0,
      },
      productionLines: [],  // 从后端加载
      lineProducts: {},  // 存储每条生产线的产品列表，key为lineId
    };
  },
  mounted() {
    this.loadProductionLines();
  },
  methods: {
    /**
     * 加载生产线列表
     */
    async loadProductionLines() {
      this.loading = true;
      try {
        const res = await getProductionLines();

        // 更新统计数据
        this.statistics = res.data.statistics;

        // 更新生产线列表
        this.productionLines = res.data.lines;

        // 为每条生产线加载可生产的产品列表
        this.loadAllLineProducts();
      } catch (error) {
        console.error('加载生产线失败', error);
        alert(error.message || '加载生产线数据失败');
      } finally {
        this.loading = false;
      }
    },

    /**
     * 加载所有生产线的产品列表
     */
    async loadAllLineProducts() {
      if (!this.productionLines || this.productionLines.length === 0) return;

      console.log('[生产线] 开始加载产品列表，生产线数量:', this.productionLines.length);
      console.log('[生产线] 生产线IDs:', this.productionLines.map(l => l.id));

      // 并行加载所有生产线的产品
      const promises = this.productionLines.map(async (line) => {
        try {
          console.log(`[生产线] 正在加载 lineId=${line.id} 的产品...`);
          const res = await getLineProducts(line.id);
          console.log(`[生产线] lineId=${line.id} 原始返回:`, res);
          // 兼容多种响应格式: res.data 可能是数组，也可能是 { records: [...] }
          const products = Array.isArray(res.data)
            ? res.data
            : (res.data?.records || res.data?.data || []);
          console.log(`[生产线] lineId=${line.id} 提取的产品列表:`, products);
          this.lineProducts = {
            ...this.lineProducts,
            [line.id]: products,
          };
        } catch (error) {
          console.warn(`[生产线] 加载 lineId=${line.id} 的产品列表失败`, error);
          this.lineProducts = {
            ...this.lineProducts,
            [line.id]: [],
          };
        }
      });

      await Promise.all(promises);
      console.log('[生产线] 所有产品列表加载完成:', this.lineProducts);
    },

    /**
     * 获取生产线的产品列表
     */
    getLineProducts(lineId) {
      return this.lineProducts[lineId] || [];
    },
    
    getStatusText(status) {
      const statusMap = {
        running: '运行中',
        maintenance: '维护中',
        stopped: '已停止',
      };
      return statusMap[status] || '未知';
    },
    handleRefresh() {
      // 刷新生产线数据
      this.loadProductionLines();
    },
    handleAddLine() {
      console.log('新增生产线');
      alert('新增生产线功能开发中...');
    },
    handleEdit(line) {
      console.log('编辑生产线', line);
      alert(`编辑生产线 ${line.name} 功能开发中...`);
    },
    handleSettings(line) {
      console.log('生产线设置', line);
      alert(`生产线 ${line.name} 设置功能开发中...`);
    },
    async handleStart(line) {
      try {
        await controlLine(line.id, 'start');
        line.status = 'running';
        alert(`${line.name} 已启动`);
      } catch (error) {
        console.error('启动失败', error);
        alert(error.message || '启动生产线失败');
      }
    },
    
    async handlePause(line) {
      if (!confirm(`确定要停止 ${line.name} 吗？`)) {
        return;
      }
      
      try {
        await controlLine(line.id, 'stop');
        line.status = 'stopped';
        alert(`${line.name} 已停止`);
      } catch (error) {
        console.error('停止失败', error);
        alert(error.message || '停止生产线失败');
      }
    },
    handleConfigCamera(line) {
      console.log('配置摄像头', line);
      alert(`配置 ${line.name} 摄像头功能开发中...`);
    },
    handleConfigStation(line) {
      console.log('管理工位', line);
      alert(`管理 ${line.name} 工位功能开发中...`);
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
