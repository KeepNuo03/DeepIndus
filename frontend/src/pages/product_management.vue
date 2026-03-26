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
          <span class="text-indigo-400 font-medium">产品管理</span>
        </div>
        <div class="flex items-center space-x-3">
          <div class="flex items-center bg-slate-800/50 rounded-lg px-3 py-1.5 border border-slate-700">
            <span class="iconify text-slate-400 mr-2" data-icon="material-symbols:search"></span>
            <input
              v-model="searchQuery"
              class="bg-transparent border-none outline-none text-xs w-48 text-slate-200"
              placeholder="搜索产品型号..."
              type="text"
            />
          </div>
          <button
            class="flex items-center space-x-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs transition-all"
            @click="handleAddProduct"
          >
            <span class="iconify" data-icon="material-symbols:add"></span>
            <span>新增产品</span>
          </button>
        </div>
      </header>

      <!-- 主内容区 -->
      <div class="flex-1 overflow-y-auto p-8 space-y-6 hide-scrollbar">
        <!-- 分类标签 -->
        <div class="flex items-center space-x-2">
          <button
            v-for="category in categories"
            :key="category.id"
            :class="[
              'px-4 py-2 rounded-lg text-sm font-medium transition-all',
              activeCategory === category.id
                ? 'bg-indigo-600 text-white'
                : 'bg-slate-800/50 text-slate-400 hover:bg-slate-800',
            ]"
            @click="activeCategory = category.id"
          >
            {{ category.name }}
            <span class="ml-2 text-xs opacity-70">({{ category.count }})</span>
          </button>
        </div>

        <!-- 产品列表 -->
        <div class="grid grid-cols-3 gap-6">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl overflow-hidden hover:border-indigo-500/50 transition-all group"
          >
            <!-- 产品图片 -->
            <div class="relative aspect-video bg-slate-900 flex items-center justify-center border-b border-slate-800 overflow-hidden">
              <img
                v-if="product.name === '热轧带钢'"
                :src="rezhagangImage"
                alt="热轧带钢"
                class="w-full h-full object-cover"
              />
              <div v-else class="text-slate-600 text-sm">暂无图片</div>
              <div
                :class="[
                  'absolute top-3 right-3 px-2 py-1 rounded-full text-xs font-bold',
                  product.status === 'active'
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    : 'bg-slate-700/50 text-slate-400 border border-slate-600/20',
                ]"
              >
                {{ product.status === 'active' ? '生产中' : '已停产' }}
              </div>
            </div>

            <!-- 产品信息 -->
            <div class="p-5">
              <div class="mb-4">
                <h3 class="text-lg font-bold text-white mb-1">{{ product.name }}</h3>
                <p class="text-xs text-slate-500 font-mono">型号: {{ product.model }}</p>
              </div>

              <!-- 规格参数 -->
              <div class="grid grid-cols-2 gap-3 mb-4">
                <div class="bg-slate-900/50 p-3 rounded-lg">
                  <div class="text-xs text-slate-500 mb-1">尺寸规格</div>
                  <div class="text-sm font-bold text-white">{{ product.dimensions }}</div>
                </div>
                <div class="bg-slate-900/50 p-3 rounded-lg">
                  <div class="text-xs text-slate-500 mb-1">材质</div>
                  <div class="text-sm font-bold text-white">{{ product.material }}</div>
                </div>
                <div class="bg-slate-900/50 p-3 rounded-lg">
                  <div class="text-xs text-slate-500 mb-1">检测标准</div>
                  <div class="text-sm font-bold text-blue-400">{{ product.standard }}</div>
                </div>
                <div class="bg-slate-900/50 p-3 rounded-lg">
                  <div class="text-xs text-slate-500 mb-1">缺陷阈值</div>
                  <div class="text-sm font-bold text-amber-400">{{ product.threshold }}%</div>
                </div>
              </div>

              <!-- 统计数据 -->
              <div class="grid grid-cols-3 gap-2 mb-4 text-center">
                <div>
                  <div class="text-xs text-slate-500">今日产量</div>
                  <div class="text-lg font-bold text-white">{{ product.todayOutput }}</div>
                </div>
                <div>
                  <div class="text-xs text-slate-500">良率</div>
                  <div class="text-lg font-bold text-emerald-400">{{ product.yieldRate }}%</div>
                </div>
                <div>
                  <div class="text-xs text-slate-500">批次</div>
                  <div class="text-lg font-bold text-blue-400">{{ product.batchCount }}</div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center space-x-2">
                <button
                  class="flex-1 py-2 bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-400 rounded-lg text-xs font-medium transition-all"
                  @click="handleViewDetail(product)"
                >
                  查看详情
                </button>
                <button
                  class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                  @click="handleEdit(product)"
                >
                  <span class="iconify" data-icon="material-symbols:edit-outline"></span>
                </button>
                <button
                  class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                  @click="handleCopy(product)"
                >
                  <span class="iconify" data-icon="material-symbols:content-copy-outline"></span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 产品详情抽屉（模拟） -->
    <transition
      enter-active-class="transition-all duration-300"
      leave-active-class="transition-all duration-300"
      enter-from-class="translate-x-full"
      enter-to-class="translate-x-0"
      leave-from-class="translate-x-0"
      leave-to-class="translate-x-full"
    >
      <div
        v-if="selectedProduct"
        class="fixed right-0 top-0 bottom-0 w-[600px] bg-[#1e293b] border-l border-slate-800 shadow-2xl z-50 overflow-y-auto hide-scrollbar"
      >
        <div class="sticky top-0 bg-[#0d1425] border-b border-slate-800 p-6 z-10">
          <div class="flex items-center justify-between mb-2">
            <h2 class="text-xl font-bold text-white">产品详情</h2>
            <button
              class="p-2 hover:bg-slate-800 rounded-lg transition-colors"
              @click="selectedProduct = null"
            >
              <span class="iconify text-slate-400" data-icon="material-symbols:close"></span>
            </button>
          </div>
          <p class="text-sm text-slate-500">{{ selectedProduct.model }}</p>
        </div>

        <div class="p-6 space-y-6">
          <!-- 基本信息 -->
          <section>
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">基本信息</h3>
            <div class="space-y-3">
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">产品名称</span>
                <span class="text-white font-medium">{{ selectedProduct.name }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">产品型号</span>
                <span class="text-white font-mono">{{ selectedProduct.model }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">产品分类</span>
                <span class="text-white">{{ selectedProduct.category }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">创建时间</span>
                <span class="text-slate-400">{{ selectedProduct.createdAt }}</span>
              </div>
            </div>
          </section>

          <!-- 规格参数 -->
          <section>
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">规格参数</h3>
            <div class="grid grid-cols-2 gap-3">
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="text-xs text-slate-500 mb-2">尺寸规格</div>
                <div class="text-base font-bold text-white">{{ selectedProduct.dimensions }}</div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="text-xs text-slate-500 mb-2">重量</div>
                <div class="text-base font-bold text-white">{{ selectedProduct.weight }}kg</div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="text-xs text-slate-500 mb-2">材质</div>
                <div class="text-base font-bold text-white">{{ selectedProduct.material }}</div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="text-xs text-slate-500 mb-2">表面处理</div>
                <div class="text-base font-bold text-white">{{ selectedProduct.surfaceTreatment }}</div>
              </div>
            </div>
          </section>

          <!-- 质量标准 -->
          <section>
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">质量标准</h3>
            <div class="space-y-3">
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-slate-300">检测标准</span>
                  <span class="text-sm font-bold text-blue-400">{{ selectedProduct.standard }}</span>
                </div>
                <div class="text-xs text-slate-500">国际标准认证</div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-slate-300">缺陷容忍阈值</span>
                  <span class="text-sm font-bold text-amber-400">{{ selectedProduct.threshold }}%</span>
                </div>
                <div class="text-xs text-slate-500">超过阈值将标记为不合格</div>
              </div>
              <div class="bg-slate-900/50 p-4 rounded-xl">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-slate-300">目标良率</span>
                  <span class="text-sm font-bold text-emerald-400">{{ selectedProduct.targetYield }}%</span>
                </div>
                <div class="text-xs text-slate-500">生产目标设定</div>
              </div>
            </div>
          </section>

          <!-- 检测配置 -->
          <section>
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">检测配置</h3>
            <div class="space-y-2">
              <div
                v-for="defect in selectedProduct.defectTypes"
                :key="defect.name"
                class="flex items-center justify-between bg-slate-900/50 p-3 rounded-lg"
              >
                <div class="flex items-center space-x-3">
                  <span
                    :class="[
                      'w-2 h-2 rounded-full',
                      defect.enabled ? 'bg-emerald-500' : 'bg-slate-500',
                    ]"
                  ></span>
                  <span class="text-sm text-slate-300">{{ defect.label }}</span>
                </div>
                <div class="flex items-center space-x-3">
                  <span class="text-xs text-slate-500">阈值: {{ defect.threshold }}%</span>
                  <label class="relative inline-block w-10 h-5">
                    <input
                      v-model="defect.enabled"
                      type="checkbox"
                      class="sr-only peer"
                    />
                    <div
                      class="w-10 h-5 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-5 peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-600"
                    ></div>
                  </label>
                </div>
              </div>
            </div>
          </section>

          <!-- 生产配置 -->
          <section>
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">
              生产配置
              <span class="text-xs text-slate-500 font-normal ml-2">
                ({{ selectedProduct.productionLines ? selectedProduct.productionLines.length : '无数据' }})
              </span>
            </h3>
            <div v-if="selectedProduct.productionLines && selectedProduct.productionLines.length > 0" class="space-y-3">
              <div
                v-for="line in selectedProduct.productionLines"
                :key="line.id"
                class="bg-slate-900/50 border border-slate-800 rounded-xl p-4"
              >
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center space-x-2">
                    <span
                      class="iconify text-emerald-500"
                      data-icon="material-symbols:precision-manufacturing-rounded"
                    ></span>
                    <span class="text-sm font-bold text-white">{{ line.productionLineName }}</span>
                    <span
                      v-if="line.isPrimary"
                      class="px-2 py-0.5 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded text-xs font-medium"
                    >
                      主
                    </span>
                  </div>
                </div>
                <div class="text-xs text-slate-500">
                  <span class="iconify inline mr-1" data-icon="material-symbols:location-on-outline"></span>
                  {{ line.productionLineLocation }}
                </div>
              </div>
            </div>
            <div v-else class="text-xs text-slate-500">
              暂无生产线数据
            </div>
          </section>

          <!-- 生产统计 -->
          <section>
            <h3 class="text-sm font-bold text-slate-400 uppercase tracking-wider mb-4">生产统计</h3>
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gradient-to-br from-blue-500/10 to-indigo-500/10 border border-blue-500/20 p-4 rounded-xl">
                <div class="text-xs text-slate-400 mb-1">累计产量</div>
                <div class="text-2xl font-bold text-white">{{ selectedProduct.totalOutput }}</div>
              </div>
              <div class="bg-gradient-to-br from-emerald-500/10 to-green-500/10 border border-emerald-500/20 p-4 rounded-xl">
                <div class="text-xs text-slate-400 mb-1">历史良率</div>
                <div class="text-2xl font-bold text-emerald-400">{{ selectedProduct.avgYieldRate }}%</div>
              </div>
            </div>
          </section>

          <!-- 操作按钮 -->
          <div class="flex space-x-3">
            <button
              class="flex-1 py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-medium transition-all"
              @click="handleEditConfig(selectedProduct)"
            >
              编辑配置
            </button>
            <button
              class="px-4 py-3 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl transition-all"
              @click="handleExport(selectedProduct)"
            >
              <span class="iconify text-xl" data-icon="material-symbols:download"></span>
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 遮罩层 -->
    <transition
      enter-active-class="transition-opacity duration-300"
      leave-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="selectedProduct"
        class="fixed inset-0 bg-black/50 backdrop-blur-sm z-40"
        @click="selectedProduct = null"
      ></div>
    </transition>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue';
import rezhagangImage from '@/assets/热轧带钢.png';

export default {
  name: 'ProductManagement',
  components: {
    Layout,
  },
  data() {
    return {
      rezhagangImage,
      searchQuery: '',
      activeCategory: 'all',
      selectedProduct: null,
      categories: [
        { id: 'all', name: '全部产品', count: 12 },
        { id: 'steel', name: '钢材制品', count: 8 },
        { id: 'aluminum', name: '铝材制品', count: 3 },
        { id: 'plastic', name: '塑料制品', count: 1 },
      ],
      products: [
        {
          id: 1,
          name: '热轧带钢',
          model: 'INDUS-ST-CRS-001',
          category: 'steel',
          dimensions: '1000×2000×2mm',
          material: 'Q235钢',
          standard: 'GB/T 3280-2015',
          threshold: 0.5,
          targetYield: 99.5,
          weight: 31.4,
          surfaceTreatment: '镀锌处理',
          status: 'active',
          todayOutput: 2840,
          yieldRate: 99.58,
          batchCount: 42,
          totalOutput: 125420,
          avgYieldRate: 99.52,
          createdAt: '2026-01-15',
          defectTypes: [
            { name: 'scratches', label: '划伤', threshold: 0.3, enabled: true },
            { name: 'crazing', label: '龟裂', threshold: 0.2, enabled: true },
            { name: 'patches', label: '斑块', threshold: 0.3, enabled: true },
            { name: 'pitted_surface', label: '麻点', threshold: 0.1, enabled: false },
          ],
          productionLines: [
            {
              id: 1,
              productionLineId: 1,
              productionLineName: '生产线 A-01',
              productionLineLocation: '车间一 A区',
              isPrimary: true,
            },
          ],
        },
        {
          id: 2,
          name: '精密钢管',
          model: 'INDUS-ST-PIPE-002',
          category: 'steel',
          dimensions: 'Φ50×3×6000mm',
          material: '20#钢',
          standard: 'GB/T 8162-2018',
          threshold: 0.3,
          targetYield: 99.8,
          weight: 21.8,
          surfaceTreatment: '抛光处理',
          status: 'active',
          todayOutput: 1580,
          yieldRate: 99.75,
          batchCount: 28,
          totalOutput: 89650,
          avgYieldRate: 99.68,
          createdAt: '2026-01-20',
          defectTypes: [
            { name: 'scratches', label: '划伤', threshold: 0.2, enabled: true },
            { name: 'crazing', label: '龟裂', threshold: 0.3, enabled: true },
            { name: 'inclusion', label: '夹杂', threshold: 0.5, enabled: true },
          ],
        },
        {
          id: 3,
          name: '工业级冷压钢板',
          model: 'INDUS-ST-HRS-003',
          category: 'steel',
          dimensions: 'H300×150×6.5×9mm',
          material: 'Q345B',
          standard: 'GB/T 11263-2017',
          threshold: 0.8,
          targetYield: 99.2,
          weight: 45.2,
          surfaceTreatment: '喷涂防锈漆',
          status: 'active',
          todayOutput: 980,
          yieldRate: 99.38,
          batchCount: 15,
          totalOutput: 45230,
          avgYieldRate: 99.25,
          createdAt: '2026-02-01',
          defectTypes: [
            { name: 'crazing', label: '龟裂', threshold: 0.1, enabled: true },
            { name: 'patches', label: '斑块', threshold: 0.8, enabled: true },
            { name: 'rolled-in_scale', label: '氧化皮卷入', threshold: 0.5, enabled: true },
          ],
        },
        {
          id: 4,
          name: '铝合金板材',
          model: 'INDUS-AL-PLT-004',
          category: 'aluminum',
          dimensions: '1220×2440×3mm',
          material: '6061-T6',
          standard: 'GB/T 3880-2012',
          threshold: 0.4,
          targetYield: 99.6,
          weight: 24.6,
          surfaceTreatment: '阳极氧化',
          status: 'active',
          todayOutput: 1250,
          yieldRate: 99.68,
          batchCount: 22,
          totalOutput: 67890,
          avgYieldRate: 99.55,
          createdAt: '2026-01-25',
          defectTypes: [
            { name: 'scratches', label: '划伤', threshold: 0.3, enabled: true },
            { name: 'rolled-in_scale', label: '氧化皮卷入', threshold: 0.4, enabled: true },
            { name: 'patches', label: '斑块', threshold: 0.2, enabled: true },
          ],
        },
        {
          id: 5,
          name: '不锈钢板',
          model: 'INDUS-ST-SS-005',
          category: 'steel',
          dimensions: '1500×3000×1.5mm',
          material: '304不锈钢',
          standard: 'GB/T 3280-2015',
          threshold: 0.3,
          targetYield: 99.7,
          weight: 52.9,
          surfaceTreatment: '2B面处理',
          status: 'active',
          todayOutput: 1680,
          yieldRate: 99.82,
          batchCount: 31,
          totalOutput: 95420,
          avgYieldRate: 99.71,
          createdAt: '2026-01-10',
          defectTypes: [
            { name: 'scratches', label: '划伤', threshold: 0.2, enabled: true },
            { name: 'pitted_surface', label: '麻点', threshold: 0.3, enabled: true },
            { name: 'patches', label: '斑块', threshold: 0.1, enabled: false },
          ],
        },
        {
          id: 6,
          name: '镀锌钢卷',
          model: 'INDUS-ST-GI-006',
          category: 'steel',
          dimensions: '1250×C×0.8mm',
          material: 'DC01+Z',
          standard: 'GB/T 2518-2019',
          threshold: 0.6,
          targetYield: 99.4,
          weight: 7.85,
          surfaceTreatment: '热镀锌',
          status: 'active',
          todayOutput: 3250,
          yieldRate: 99.51,
          batchCount: 56,
          totalOutput: 185620,
          avgYieldRate: 99.45,
          createdAt: '2026-01-05',
          defectTypes: [
            { name: 'rolled-in_scale', label: '氧化皮卷入', threshold: 0.5, enabled: true },
            { name: 'scratches', label: '划伤', threshold: 0.6, enabled: true },
            { name: 'patches', label: '斑块', threshold: 0.3, enabled: false },
          ],
        },
      ],
    };
  },
  computed: {
    filteredProducts() {
      let products = this.products;

      // 分类筛选
      if (this.activeCategory !== 'all') {
        products = products.filter((p) => p.category === this.activeCategory);
      }

      // 搜索筛选
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        products = products.filter(
          (p) =>
            p.name.toLowerCase().includes(query) ||
            p.model.toLowerCase().includes(query)
        );
      }

      return products;
    },
  },
  methods: {
    handleAddProduct() {
      console.log('新增产品');
      alert('新增产品功能开发中...');
    },
    handleViewDetail(product) {
      this.selectedProduct = product;
    },
    handleEdit(product) {
      console.log('编辑产品', product);
      alert(`编辑产品 ${product.name} 功能开发中...`);
    },
    handleCopy(product) {
      console.log('复制产品', product);
      alert(`复制产品 ${product.name} 功能开发中...`);
    },
    handleEditConfig(product) {
      console.log('编辑配置', product);
      alert(`编辑 ${product.name} 配置功能开发中...`);
    },
    handleExport(product) {
      console.log('导出产品', product);
      alert(`导出 ${product.name} 数据功能开发中...`);
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
