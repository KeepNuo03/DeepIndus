<template>
  <Layout>
    <main class="flex-1 flex flex-col min-w-0 bg-[#0f172a] overflow-hidden">
      <!-- 顶部导航 -->
      <header
        class="h-16 bg-[#020617]/50 border-b border-slate-800 flex items-center justify-between px-8 shrink-0"
      >
        <div class="flex items-center space-x-4">
          <button
            class="p-2 hover:bg-slate-800 rounded-lg transition-colors"
            @click="goBack"
          >
            <span class="iconify text-slate-400" data-icon="material-symbols:arrow-back"></span>
          </button>
          <div class="flex items-center space-x-3 text-sm">
            <span class="text-slate-500">检测记录</span>
            <span class="iconify text-slate-700" data-icon="material-symbols:chevron-right"></span>
            <span class="text-indigo-400 font-medium">缺陷详情 {{ defectId }}</span>
          </div>
        </div>
        <div class="flex items-center space-x-3">
          <button
            class="flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg text-xs transition-all"
          >
            <span class="iconify" data-icon="material-symbols:download"></span>
            <span>导出报告</span>
          </button>
          <button
            class="flex items-center space-x-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs transition-all"
          >
            <span class="iconify" data-icon="material-symbols:print"></span>
            <span>打印</span>
          </button>
        </div>
      </header>

      <!-- 主内容区 -->
      <div class="flex-1 overflow-y-auto p-8 space-y-6 hide-scrollbar">
        <!-- Loading 状态 -->
        <div v-if="loading" class="flex items-center justify-center h-96">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-400">加载中...</p>
          </div>
        </div>

        <!-- 无数据 / 接口异常 -->
        <div v-else-if="!defectInfo.id && !defectInfo.detectionNo" class="flex items-center justify-center h-96">
          <div class="text-center text-slate-400">
            <p class="text-lg mb-2">未找到该缺陷详情</p>
            <p class="text-sm">可能原因：记录不存在或接口返回格式异常，请查看控制台或联系后端确认 GET /v1/defect/:id 的响应。</p>
            <button class="mt-4 px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded-lg text-sm" @click="goBack">返回</button>
          </div>
        </div>
        
        <!-- 数据内容（兼容仅有 detectionNo 无 id 的返回） -->
        <div v-else class="grid grid-cols-12 gap-6">
          <!-- 左侧：图片展示区 -->
          <div class="col-span-8 space-y-6">
            <!-- 主图片 -->
            <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl overflow-hidden">
              <div class="p-6 border-b border-slate-800 flex justify-between items-center">
                <h3 class="font-bold text-lg flex items-center">
                  <span class="w-1 h-5 bg-indigo-500 rounded-full mr-3"></span>
                  缺陷图像
                </h3>
                <div class="flex items-center space-x-2">
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-xs transition-all"
                    title="放大"
                  >
                    <span class="iconify" data-icon="material-symbols:zoom-in"></span>
                  </button>
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-xs transition-all"
                    title="旋转"
                  >
                    <span class="iconify" data-icon="material-symbols:rotate-right"></span>
                  </button>
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-xs transition-all"
                    title="下载"
                  >
                    <span class="iconify" data-icon="material-symbols:download"></span>
                  </button>
                </div>
              </div>
              <div ref="imageContainer" class="relative bg-slate-900 aspect-video flex items-center justify-center">
                <template v-if="imageSrc && !imageError">
                  <img
                    :src="imageSrc"
                    ref="defectImage"
                    class="w-full h-full object-contain bg-slate-900"
                    alt="缺陷图像"
                    @error="handleImageError"
                    @load="handleImageLoad"
                  />
                </template>
                <!-- 检测图像占位（无图或加载失败） -->
                <div v-else class="w-full h-full bg-slate-800 flex items-center justify-center text-slate-600">
                  {{ imageError ? '缺陷图像加载失败' : '暂无缺陷图像' }}
                </div>
                <!-- 图片信息叠加 -->
                <div
                  class="absolute bottom-4 left-4 bg-black/60 backdrop-blur px-3 py-2 rounded-lg text-xs space-y-1"
                >
                  <div class="text-slate-300">拍摄时间: {{ defectInfo.timestamp }}</div>
                  <div class="text-slate-300">摄像头: CAM-01 | 分辨率: 1920x1080</div>
                </div>
              </div>
            </section>

            <!-- 缺陷分析 -->
            <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6">
              <h3 class="font-bold text-lg flex items-center mb-6">
                <span class="w-1 h-5 bg-blue-500 rounded-full mr-3"></span>
                检测分析
              </h3>
              <div class="grid grid-cols-3 gap-6">
                <div class="space-y-2">
                  <div class="text-xs text-slate-500 uppercase tracking-wider">缺陷类型</div>
                  <div
                    class="px-3 py-2 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 font-bold"
                  >
                    {{ getDefectLabel(defectInfo.type) }}
                  </div>
                </div>
                <div class="space-y-2">
                  <div class="text-xs text-slate-500 uppercase tracking-wider">置信度</div>
                  <div class="text-2xl font-bold text-white">{{ defectInfo.confidence }}</div>
                  <div class="h-2 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      class="h-full bg-gradient-to-r from-emerald-500 to-blue-500"
                      :style="{ width: defectInfo.confidence }"
                    ></div>
                  </div>
                </div>
                <div class="space-y-2">
                  <div class="text-xs text-slate-500 uppercase tracking-wider">严重程度</div>
                  <div class="flex items-center space-x-2">
                    <span
                      :class="[
                        'px-3 py-1 rounded-full text-xs font-bold',
                        defectInfo.severity === '严重'
                          ? 'bg-red-500/10 text-red-400 border border-red-500/20'
                          : 'bg-amber-500/10 text-amber-400 border border-amber-500/20',
                      ]"
                    >
                      {{ defectInfo.severity }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="mt-6 grid grid-cols-4 gap-4">
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">缺陷位置 X</div>
                  <div class="text-lg font-mono font-bold">{{ defectInfo.positionX }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">缺陷位置 Y</div>
                  <div class="text-lg font-mono font-bold">{{ defectInfo.positionY }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">缺陷面积</div>
                  <div class="text-lg font-mono font-bold">{{ defectInfo.area }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">影响等级</div>
                  <div class="text-lg font-bold text-red-400">{{ defectInfo.impactLevel }}</div>
                </div>
              </div>
            </section>
          </div>

          <!-- 右侧：详情信息 -->
          <div class="col-span-4 space-y-6">
            <!-- 基本信息 -->
            <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6">
              <h3 class="font-bold flex items-center mb-4">
                <span class="w-1 h-5 bg-emerald-500 rounded-full mr-3"></span>
                基本信息
              </h3>
              <div class="space-y-4 text-sm">
                <div class="flex justify-between">
                  <span class="text-slate-500">检测编号</span>
                  <span class="font-mono text-slate-300">{{ defectInfo.detectionNo }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-500">产品序列号</span>
                  <span class="font-bold">{{ defectInfo.serialNo }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-500">生产线</span>
                  <span class="text-slate-300">{{ defectInfo.productionLine }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-500">班次</span>
                  <span class="text-slate-300">{{ defectInfo.shift }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-500">检测时间</span>
                  <span class="text-slate-300">{{ defectInfo.timestamp }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-500">AI 模型</span>
                  <span class="text-blue-400">{{ defectInfo.model }}</span>
                </div>
              </div>
            </section>

            <!-- 处理状态 -->
            <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6">
              <h3 class="font-bold flex items-center mb-4">
                <span class="w-1 h-5 bg-amber-500 rounded-full mr-3"></span>
                处理状态
              </h3>
              <div class="space-y-3">
                <div
                  :class="[
                    'p-4 rounded-xl border-2',
                    defectInfo.status === '待处理'
                      ? 'bg-amber-500/5 border-amber-500/20'
                      : defectInfo.status === '处理中'
                        ? 'bg-blue-500/5 border-blue-500/20'
                        : 'bg-emerald-500/5 border-emerald-500/20',
                  ]"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs text-slate-500">当前状态</span>
                    <span
                      :class="[
                        'px-2 py-1 rounded-full text-xs font-bold',
                        defectInfo.status === '待处理'
                          ? 'bg-amber-500/20 text-amber-400'
                          : defectInfo.status === '处理中'
                            ? 'bg-blue-500/20 text-blue-400'
                            : 'bg-emerald-500/20 text-emerald-400',
                      ]"
                    >
                      {{ defectInfo.status }}
                    </span>
                  </div>
                  <div class="text-sm text-slate-300">{{ defectInfo.statusNote }}</div>
                </div>

                <button
                  v-if="defectInfo.status === '待处理'"
                  class="w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold transition-all"
                  @click="handleReview"
                >
                  开始人工复核
                </button>
              </div>
            </section>

            <!-- 处理记录 -->
            <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6">
              <h3 class="font-bold flex items-center mb-4">
                <span class="w-1 h-5 bg-purple-500 rounded-full mr-3"></span>
                处理记录
              </h3>
              <div class="space-y-4 max-h-[300px] overflow-y-auto hide-scrollbar">
                <div
                  v-for="record in processRecords"
                  :key="record.id"
                  class="relative pl-6 pb-4 border-l-2 border-slate-700 last:border-l-0 last:pb-0"
                >
                  <div
                    :class="[
                      'absolute left-0 top-0 w-3 h-3 rounded-full -translate-x-[7px]',
                      record.type === 'create'
                        ? 'bg-blue-500'
                        : record.type === 'review'
                          ? 'bg-amber-500'
                          : 'bg-emerald-500',
                    ]"
                  ></div>
                  <div class="text-xs text-slate-500 mb-1">{{ record.time }}</div>
                  <div class="text-sm font-medium text-slate-200">{{ record.action }}</div>
                  <div class="text-xs text-slate-400 mt-1">{{ record.operator }}</div>
                </div>
              </div>
            </section>
          </div>
        </div>

        <!-- 相似缺陷 -->
        <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6">
          <h3 class="font-bold text-lg flex items-center mb-6">
            <span class="w-1 h-5 bg-cyan-500 rounded-full mr-3"></span>
            相似缺陷推荐
          </h3>
          <div class="grid grid-cols-5 gap-4">
            <div
              v-for="similar in similarDefects"
              :key="similar.id"
              class="bg-slate-900/50 rounded-xl overflow-hidden border border-slate-700 hover:border-indigo-500/50 transition-all cursor-pointer group"
            >
              <div class="aspect-square bg-slate-800 flex items-center justify-center">
                <span class="text-slate-600 text-xs">图像 {{ similar.id }}</span>
              </div>
              <div class="p-3 space-y-1">
                <div class="text-xs font-bold text-slate-200 truncate">{{ getDefectLabel(similar.type) }}</div>
                <div class="text-[10px] text-slate-500">
                  相似度: <span class="text-emerald-400">{{ similar.similarity }}</span>
                </div>
                <div class="text-[10px] text-slate-500">{{ similar.date }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue';
import { getDefectDetail, reviewDefect } from '@/api/detection';
import { BASE_URL } from '@/utils/request';
import { getDefectLabel } from '@/utils/constants';

export default {
  name: 'DefectDetail',
  components: {
    Layout,
  },
  data() {
    return {
      defectId: '',
      loading: false,
      imageError: false,
      detections: [],
      detectionCount: 0,
      imageMeta: {
        naturalWidth: 0,
        naturalHeight: 0,
        displayWidth: 0,
        displayHeight: 0,
        offsetX: 0,
        offsetY: 0,
      },
      // 缺陷详情数据 - 从后端加载
      defectInfo: {},
      // 处理记录 - 从后端加载
      processRecords: [],
      // 相似缺陷 - 从后端加载
      similarDefects: [],
    };
  },
  computed: {
    imageSrc() {
      const raw = this.defectInfo?.imageUrl;
      if (!raw) return '';
      if (/^https?:\/\//i.test(raw)) return raw;
      const base = BASE_URL.endsWith('/v1') && raw.startsWith('/v1/')
        ? BASE_URL.slice(0, -3)
        : BASE_URL;
      return `${base}${raw}`;
    },
    detectionBoxes() {
      const {
        naturalWidth,
        naturalHeight,
        displayWidth,
        displayHeight,
        offsetX,
        offsetY,
      } = this.imageMeta;
      if (!naturalWidth || !naturalHeight || !displayWidth || !displayHeight) return [];
      const scaleX = displayWidth / naturalWidth;
      const scaleY = displayHeight / naturalHeight;
      const boxes = (this.detections || [])
        .filter(d => d && d.bbox)
        .map((d) => {
          const { x1, y1, x2, y2 } = d.bbox;
          let bx1 = Number(x1);
          let by1 = Number(y1);
          let bx2 = Number(x2);
          let by2 = Number(y2);
          if (![bx1, by1, bx2, by2].every(Number.isFinite)) return null;
          const maxVal = Math.max(bx1, by1, bx2, by2);
          const minVal = Math.min(bx1, by1, bx2, by2);
          const isNormalized = minVal >= 0 && maxVal <= 1;
          if (isNormalized) {
            bx1 *= naturalWidth;
            by1 *= naturalHeight;
            bx2 *= naturalWidth;
            by2 *= naturalHeight;
          }
          const left = offsetX + bx1 * scaleX;
          const top = offsetY + by1 * scaleY;
          const width = (bx2 - bx1) * scaleX;
          const height = (by2 - by1) * scaleY;
          if (width <= 0 || height <= 0) return null;
          const confidence = d.confidence != null ? `${d.confidence}` : '';
          const label = `${d.class_name || '缺陷'}${confidence ? ` ${confidence}` : ''}`;
          return {
            style: {
              left: `${left}px`,
              top: `${top}px`,
              width: `${width}px`,
              height: `${height}px`,
              boxShadow: '0 0 12px rgba(239, 68, 68, 0.4)',
              zIndex: 2,
            },
            label,
          };
        })
        .filter(Boolean);
      if (process.env.NODE_ENV !== 'production' && this.detections?.length && !boxes.length) {
        console.warn('[缺陷详情] detections 有值但未生成 bbox，请检查 bbox 坐标/格式', this.detections);
      }
      return boxes;
    },
  },
  mounted() {
    // 从路由参数获取缺陷 ID（可能为 detectionNo 如 #DET-2026A01 或数字 id，Vue Router 会解码 path 中的编码）
    const rawId = this.$route.params.id;
    if (rawId) {
      try {
        this.defectId = decodeURIComponent(rawId);
      } catch {
        this.defectId = rawId;
      }
      this.loadDefectDetail();
    }
  },
  methods: {
    handleImageError() {
      this.imageError = true;
    },
    handleImageLoad() {
      this.imageError = false;
      this.updateImageMetrics();
    },
    updateImageMetrics() {
      const img = this.$refs.defectImage;
      const container = this.$refs.imageContainer;
      if (!img || !container) return;
      const imgRect = img.getBoundingClientRect();
      const containerRect = container.getBoundingClientRect();
      const offsetX = imgRect.left - containerRect.left;
      const offsetY = imgRect.top - containerRect.top;
      this.imageMeta = {
        naturalWidth: img.naturalWidth || 0,
        naturalHeight: img.naturalHeight || 0,
        displayWidth: imgRect.width || 0,
        displayHeight: imgRect.height || 0,
        offsetX: Number.isFinite(offsetX) ? offsetX : 0,
        offsetY: Number.isFinite(offsetY) ? offsetY : 0,
      };
    },
    /**
     * 加载缺陷详情
     * 兼容：res.data 直接为详情对象，或 res.data.data 为详情对象（部分后端会再包一层 data）
     */
    async loadDefectDetail() {
      this.loading = true;
      this.defectInfo = {};
      this.processRecords = [];
      this.similarDefects = [];
      this.imageError = false;
      this.detections = [];
      this.detectionCount = 0;
      this.imageMeta = {
        naturalWidth: 0,
        naturalHeight: 0,
        displayWidth: 0,
        displayHeight: 0,
        offsetX: 0,
        offsetY: 0,
      };
      const requestUrl = `/defect/${encodeURIComponent(String(this.defectId))}`;
      if (process.env.NODE_ENV !== 'production') {
        console.log('[缺陷详情] 请求', { defectId: this.defectId, url: requestUrl });
      }
      try {
        const res = await getDefectDetail(this.defectId);
        const raw = (res && (res.data?.data != null ? res.data.data : res.data)) || {};
        this.defectInfo = typeof raw === 'object' && raw !== null ? raw : {};
        this.processRecords = Array.isArray(this.defectInfo.processRecords) ? this.defectInfo.processRecords : [];
        this.similarDefects = Array.isArray(this.defectInfo.similarDefects) ? this.defectInfo.similarDefects : [];
        this.detections = Array.isArray(this.defectInfo.detections) ? this.defectInfo.detections : [];
        this.detectionCount = this.defectInfo.detectionCount ?? this.detections.length;
        if (!this.defectInfo.id && !this.defectInfo.detectionNo && process.env.NODE_ENV !== 'production') {
          console.warn('[缺陷详情] 接口返回无 id/detectionNo', res);
        }
        this.$nextTick(() => {
          this.updateImageMetrics();
        });
      } catch (error) {
        console.error('加载缺陷详情失败', { defectId: this.defectId, url: requestUrl, error });
        alert(error.message || '加载缺陷详情失败');
      } finally {
        this.loading = false;
      }
    },
    
    goBack() {
      this.$router.back();
    },
    
    async handleReview() {
      const action = prompt('请输入复核结果（confirm/reject）:', 'confirm');
      if (!action) return;
      
      const note = prompt('请输入复核备注:');
      if (!note) return;
      
      try {
        await reviewDefect(this.defectId, {
          action,
          note
        });
        
        alert('人工复核提交成功');
        // 重新加载详情
        this.loadDefectDetail();
      } catch (error) {
        console.error('复核失败', error);
        alert(error.message || '提交复核失败');
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
