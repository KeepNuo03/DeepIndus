<template>
  <Layout>
    <main class="flex-1 flex flex-col min-w-0 bg-[#0f172a] overflow-hidden">
      <!-- 顶部导航 -->
      <header
        class="h-16 bg-[#020617]/50 border-b border-slate-800 flex items-center justify-between px-8 shrink-0"
      >
        <div class="flex items-center space-x-4 text-sm">
          <span class="text-slate-500">生产看板</span>
          <span class="iconify text-slate-700" data-icon="material-symbols:chevron-right"></span>
          <span class="text-indigo-400 font-medium">历史检测记录 ({{ currentDate }})</span>
        </div>
        <div class="flex items-center space-x-6">
          <div class="flex items-center bg-slate-800/50 rounded-lg px-3 py-1.5 border border-slate-700">
            <span class="iconify text-slate-400 mr-2" data-icon="material-symbols:search"></span>
            <input
              v-model="searchQuery"
              class="bg-transparent border-none outline-none text-xs w-48 text-slate-200"
              placeholder="搜索序列号..."
              type="text"
            />
          </div>
          <div class="flex items-center space-x-3 border-l border-slate-800 pl-6">
            <div class="text-right">
              <p class="text-xs font-bold">李工</p>
              <p class="text-[10px] text-slate-500">高级巡检员</p>
            </div>
            <div class="w-9 h-9 rounded-full border border-slate-700 bg-slate-700"></div>
          </div>
        </div>
      </header>

      <!-- 主内容区 -->
      <div class="flex-1 p-8 space-y-6 overflow-hidden flex flex-col">
        <!-- 筛选条件区域 -->
        <section
          class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-2xl flex flex-wrap items-end gap-6 shrink-0"
        >
          <div class="space-y-2">
            <label class="text-[11px] text-slate-500 font-bold uppercase tracking-wider"
              >时间范围</label
            >
            <div class="relative">
              <span
                class="absolute left-3 top-1/2 -translate-y-1/2 iconify text-slate-500"
                data-icon="material-symbols:calendar-today"
              ></span>
              <input
                v-model="filters.dateRange"
                class="bg-slate-900 border border-slate-700 rounded-lg pl-10 pr-4 py-2 text-xs w-64 focus:ring-1 focus:ring-indigo-500 outline-none"
                type="text"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-[11px] text-slate-500 font-bold uppercase tracking-wider"
              >缺陷类型</label
            >
            <select
              v-model="filters.defectType"
              class="bg-slate-900 border border-slate-700 rounded-lg px-4 py-2 text-xs w-40 focus:ring-1 focus:ring-indigo-500 outline-none"
            >
              <option value="all">全部类型</option>
              <option v-for="type in defectTypes" :key="type.value" :value="type.value">
                {{ type.label }}
              </option>
            </select>
          </div>

          <div class="space-y-2">
            <label class="text-[11px] text-slate-500 font-bold uppercase tracking-wider"
              >检测结果</label
            >
            <div class="flex bg-slate-900 border border-slate-700 rounded-lg p-1">
              <button
                :class="[
                  'px-4 py-1.5 text-xs rounded-md transition-colors',
                  filters.status === 'all'
                    ? 'bg-slate-800 text-white'
                    : 'text-slate-500 hover:text-white',
                ]"
                @click="filters.status = 'all'"
              >
                全部
              </button>
              <button
                :class="[
                  'px-4 py-1.5 text-xs transition-colors',
                  filters.status === 'pass' ? 'text-emerald-400' : 'text-slate-500 hover:text-emerald-400',
                ]"
                @click="filters.status = 'pass'"
              >
                合格
              </button>
              <button
                :class="[
                  'px-4 py-1.5 text-xs transition-colors',
                  filters.status === 'fail' ? 'text-red-400' : 'text-slate-500 hover:text-red-400',
                ]"
                @click="filters.status = 'fail'"
              >
                不合格
              </button>
            </div>
          </div>

          <div class="flex space-x-3 ml-auto">
            <button
              class="flex items-center space-x-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2.5 rounded-lg text-xs font-medium transition-all"
              @click="handleQuery"
            >
              <span class="iconify" data-icon="material-symbols:filter-alt"></span>
              <span>查询记录</span>
            </button>
            <button
              class="flex items-center space-x-2 bg-slate-800 hover:bg-slate-700 text-slate-300 px-5 py-2.5 rounded-lg text-xs font-medium transition-all"
              @click="handleReset"
            >
              <span class="iconify" data-icon="material-symbols:restart-alt"></span>
              <span>重置</span>
            </button>
          </div>
        </section>

        <!-- 操作栏与数据表格 -->
        <section
          class="flex-1 bg-[#1e293b]/20 border border-slate-800 rounded-2xl flex flex-col overflow-hidden"
        >
          <div
            class="px-6 py-4 border-b border-slate-800 flex justify-between items-center bg-slate-900/30"
          >
            <div class="flex items-center space-x-4">
              <span class="text-xs text-slate-400"
                >已选中 <span class="text-indigo-400 font-bold">{{ selectedCount }}</span> 项</span
              >
              <div class="h-4 w-px bg-slate-800"></div>
              <button
                class="flex items-center space-x-1.5 text-xs text-emerald-400 hover:underline"
                @click="handleBatchExport"
              >
                <span class="iconify" data-icon="material-symbols:download"></span
                ><span>批量导出</span>
              </button>
              <button
                class="flex items-center space-x-1.5 text-xs text-amber-400 hover:underline"
                @click="handleBatchMark"
              >
                <span class="iconify" data-icon="material-symbols:bookmark"></span
                ><span>批量标记</span>
              </button>
              <button
                class="flex items-center space-x-1.5 text-xs text-red-400 hover:underline"
                @click="handleBatchDelete"
              >
                <span class="iconify" data-icon="material-symbols:delete-outline"></span
                ><span>批量删除</span>
              </button>
            </div>
            <div class="flex items-center space-x-2">
              <button
                class="p-2 bg-slate-800 rounded-lg hover:bg-slate-700"
                @click="handleRefresh"
              >
                <span class="iconify" data-icon="material-symbols:refresh"></span>
              </button>
              <button class="p-2 bg-slate-800 rounded-lg hover:bg-slate-700">
                <span class="iconify" data-icon="material-symbols:view-column-outline"></span>
              </button>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto px-6 hide-scrollbar">
            <table class="w-full text-left border-separate border-spacing-y-3">
              <thead
                class="sticky top-0 bg-[#161d2f] z-10 text-[11px] text-slate-500 uppercase tracking-widest font-bold"
              >
                <tr>
                  <th class="pb-3 pl-4 w-10">
                    <input
                      v-model="selectAll"
                      class="accent-indigo-500"
                      type="checkbox"
                      @change="toggleSelectAll"
                    />
                  </th>
                  <th class="pb-3">检测编号</th>
                  <th class="pb-3">产品序列号</th>
                  <th class="pb-3">产品名称</th>
                  <th class="pb-3">缺陷描述</th>
                  <th class="pb-3">置信度</th>
                  <th class="pb-3">检测时间</th>
                  <th class="pb-3">检测状态</th>
                  <th class="pb-3 text-right pr-4">操作</th>
                </tr>
              </thead>
              <tbody class="text-xs">
                <tr
                  v-for="record in records"
                  :key="record.id"
                  :class="[
                    'hover:bg-slate-800/50 transition-colors group',
                    record.selected ? 'bg-indigo-500/5 border border-indigo-500/20' : 'bg-slate-900/40',
                  ]"
                >
                  <td
                    :class="[
                      'py-4 pl-4 rounded-l-xl',
                      !record.selected && 'border-l border-y border-transparent group-hover:border-indigo-500/30',
                    ]"
                  >
                    <input
                      v-model="record.selected"
                      class="accent-indigo-500"
                      type="checkbox"
                      @change="updateSelectedCount"
                    />
                  </td>
                  <td class="py-4 font-mono text-slate-400">{{ record.detectionNo }}</td>
                  <td class="py-4 font-bold">{{ record.serialNo }}</td>
                  <td class="py-4">
                    <span
                      v-if="getProductName(record)"
                      class="px-2 py-0.5 bg-blue-500/10 text-blue-400 border border-blue-500/20 rounded-full text-[10px]"
                    >
                      {{ getProductName(record) }}
                    </span>
                    <span v-else class="text-slate-500 text-[10px]">未关联产品</span>
                  </td>
                  <td class="py-4">
                    <span
                      v-if="record.defect"
                      :class="[
                        'px-2 py-0.5 rounded-full border',
                        record.severity === 'critical'
                          ? 'bg-red-500/10 text-red-400 border-red-500/20'
                          : 'bg-amber-500/10 text-amber-400 border-amber-500/20',
                      ]"
                      >{{ getDefectLabel(record.defect) }}</span
                    >
                    <span v-else class="text-slate-500">无异常</span>
                  </td>
                  <td class="py-4 text-slate-400">{{ record.confidence }}</td>
                  <td class="py-4 text-slate-500">{{ record.timestamp }}</td>
                  <td class="py-4">
                    <div class="flex items-center">
                      <span
                        :class="[
                          'w-2 h-2 rounded-full mr-2',
                          record.status === 'pass' ? 'bg-emerald-500' : 'bg-red-500',
                        ]"
                      ></span>
                      <span>{{ record.status === 'pass' ? '合格' : '不合格' }}</span>
                    </div>
                  </td>
                  <td
                    :class="[
                      'py-4 text-right pr-4 rounded-r-xl',
                      !record.selected && 'border-r border-y border-transparent group-hover:border-indigo-500/30',
                    ]"
                  >
                    <button
                      class="text-indigo-400 hover:text-indigo-300 font-medium"
                      @click="handleViewDetail(record)"
                    >
                      详情
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页组件 -->
          <div
            class="px-6 py-4 bg-slate-900/50 border-t border-slate-800 flex justify-between items-center text-xs"
          >
            <div class="text-slate-500">显示 {{ pageStart }} 到 {{ pageEnd }} 共 {{ totalRecords }} 条记录</div>
            <div class="flex items-center space-x-1">
              <button
                class="w-8 h-8 flex items-center justify-center rounded-lg border border-slate-700 text-slate-500 hover:bg-slate-700 transition-colors"
                :disabled="currentPage === 1"
                @click="changePage(currentPage - 1)"
              >
                <span class="iconify" data-icon="material-symbols:chevron-left"></span>
              </button>
              <button
                v-for="page in visiblePages"
                :key="page"
                :class="[
                  'w-8 h-8 flex items-center justify-center rounded-lg transition-colors',
                  page === currentPage
                    ? 'bg-indigo-600 text-white font-bold'
                    : 'border border-slate-700 text-slate-400 hover:bg-slate-700',
                ]"
                @click="changePage(page)"
              >
                {{ page }}
              </button>
              <span v-if="totalPages > 5" class="px-2 text-slate-600">...</span>
              <button
                v-if="totalPages > 5"
                class="w-8 h-8 flex items-center justify-center rounded-lg border border-slate-700 text-slate-400 hover:bg-slate-700 transition-colors"
                @click="changePage(totalPages)"
              >
                {{ totalPages }}
              </button>
              <button
                class="w-8 h-8 flex items-center justify-center rounded-lg border border-slate-700 text-slate-500 hover:bg-slate-700 transition-colors"
                :disabled="currentPage === totalPages"
                @click="changePage(currentPage + 1)"
              >
                <span class="iconify" data-icon="material-symbols:chevron-right"></span>
              </button>
            </div>
          </div>
        </section>
      </div>
    </main>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue';
import { getRecords, exportRecords, deleteRecords } from '@/api/detection';
import { DEFECT_TYPES, getDefectLabel } from '@/utils/constants';

export default {
  name: 'QueryPage',
  components: {
    Layout,
  },
  data() {
    return {
      currentDate: '',
      searchQuery: '',
      selectAll: false,
      loading: false,
      defectTypes: DEFECT_TYPES,  // 缺陷类型列表
      filters: {
        dateRange: '2026/02/01 - 今日',
        defectType: 'all',
        status: 'all',
      },
      currentPage: 1,
      pageSize: 20,
      totalRecords: 0,
      records: [],  // 从后端加载
    };
  },
  computed: {
    selectedCount() {
      return this.records.filter((r) => r.selected).length;
    },
    totalPages() {
      return Math.ceil(this.totalRecords / this.pageSize);
    },
    pageStart() {
      return (this.currentPage - 1) * this.pageSize + 1;
    },
    pageEnd() {
      return Math.min(this.currentPage * this.pageSize, this.totalRecords);
    },
    visiblePages() {
      const pages = [];
      const maxVisible = 3;
      for (let i = 1; i <= Math.min(maxVisible, this.totalPages); i++) {
        pages.push(i);
      }
      return pages;
    },
  },
  mounted() {
    this.updateCurrentDate();
    this.loadRecords();  // 加载检测记录
  },
  methods: {
    updateCurrentDate() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      this.currentDate = `${year}-${month}-${day}`;
    },
    
    /**
     * 加载检测记录
     * 请求：GET /v1/records/query（与实时检测、数据大屏不是同一接口）
     *
     * 无数据时排查：
     * 1. 确认请求 URL 为 baseURL + /records/query，无拼写/错误环境/Mock
     * 2. 默认不传 dateStart/dateEnd，后端返回全部日期；传了则只查该范围，需包含「今天」
     * 3. defectType、status 默认 all（不传），避免把刚上传记录筛掉
     * 4. 上传离线检测成功后需再请求本接口或点「刷新」，新记录才会出现在列表
     */
    async loadRecords() {
      this.loading = true;
      try {
        // 仅传有意义的参数，默认不传日期与类型/状态，按时间倒序返回全部记录
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
        };
        if (this.filters.defectType !== 'all') params.defectType = this.filters.defectType;
        if (this.filters.status !== 'all') params.status = this.filters.status;
        if (this.searchQuery && this.searchQuery.trim()) params.search = this.searchQuery.trim();

        const res = await getRecords(params);
        console.log('[记录管理] API 返回:', res.data);

        this.records = (res.data.records || []).map(record => ({
          ...record,
          selected: false
        }));
        console.log('[记录管理] 第一条记录:', this.records[0]);
        this.totalRecords = res.data.total ?? 0;
      } catch (error) {
        console.error('加载记录失败', error);
        alert(error.message || '加载数据失败');
      } finally {
        this.loading = false;
      }
    },
    toggleSelectAll() {
      this.records.forEach((record) => {
        record.selected = this.selectAll;
      });
    },
    updateSelectedCount() {
      this.selectAll = this.records.every((r) => r.selected);
    },
    handleQuery() {
      // 重新查询，回到第一页
      this.currentPage = 1;
      this.loadRecords();
    },
    
    handleReset() {
      // 重置筛选条件
      this.filters = {
        dateRange: '2026/02/01 - 今日',
        defectType: 'all',
        status: 'all',
      };
      this.searchQuery = '';
      this.currentPage = 1;
      this.loadRecords();
    },
    
    async handleBatchExport() {
      const selectedIds = this.records
        .filter((r) => r.selected)
        .map((r) => r.id);
      
      if (selectedIds.length === 0) {
        alert('请先选择要导出的记录');
        return;
      }
      
      try {
        const res = await exportRecords({
          recordIds: selectedIds,
          format: 'excel'
        });
        
        // 打开下载链接
        if (res.data.downloadUrl) {
          window.open(res.data.downloadUrl, '_blank');
        }
      } catch (error) {
        console.error('导出失败', error);
        alert(error.message || '导出失败');
      }
    },
    
    handleBatchMark() {
      // 批量标记功能（待后端实现）
      const selectedIds = this.records.filter((r) => r.selected).map((r) => r.id);
      if (selectedIds.length === 0) {
        alert('请先选择要标记的记录');
        return;
      }
      alert('批量标记功能待后端实现');
    },
    
    async handleBatchDelete() {
      const selectedIds = this.records
        .filter((r) => r.selected)
        .map((r) => r.id);
      
      if (selectedIds.length === 0) {
        alert('请先选择要删除的记录');
        return;
      }
      
      if (!confirm(`确定要删除选中的 ${selectedIds.length} 条记录吗？`)) {
        return;
      }
      
      try {
        await deleteRecords({
          recordIds: selectedIds
        });
        
        alert('删除成功');
        // 重新加载数据
        this.loadRecords();
      } catch (error) {
        console.error('删除失败', error);
        alert(error.message || '删除失败');
      }
    },
    
    handleRefresh() {
      // 刷新数据
      this.loadRecords();
    },
    /**
     * 获取产品名称（兼容多种字段名）
     */
    getProductName(record) {
      // 尝试多种可能的字段名
      return record.productName || record.product_name || record.product || null;
    },

    handleViewDetail(record) {
      // 优先使用 record.id（后端常用数字主键查详情），无则用 record.detectionNo；含 # 等特殊字符时编码
      const id = (record.id != null && record.id !== '') ? String(record.id) : (record.detectionNo != null ? String(record.detectionNo) : '');
      if (!id) {
        alert('该记录缺少 id 或 detectionNo，无法查看详情');
        return;
      }
      const paramId = /#/.test(id) ? encodeURIComponent(id) : id;
      this.$router.push({
        name: 'DefectDetail',
        params: { id: paramId }
      });
    },
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
        this.loadRecords();  // 加载新页面数据
      }
    },
    
    /**
     * 缺陷类型转换：英文 → 中文
     */
    getDefectLabel(value) {
      return getDefectLabel(value);
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
