<template>
  <Layout>
    <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- 顶部栏 -->
      <header
        class="h-16 bg-[#020617]/50 backdrop-blur-md border-b border-slate-800 flex items-center justify-between px-8 flex-shrink-0"
      >
        <div class="flex items-center space-x-4">
          <span class="iconify text-slate-500" data-icon="mdi:chevron-right"></span>
          <nav class="flex text-sm space-x-2">
            <span class="text-slate-500">控制面板</span>
            <span class="text-slate-500">/</span>
            <span class="text-white font-medium">实时视频流检测</span>
          </nav>
        </div>
        <div class="flex items-center space-x-6">
          <div class="flex items-center space-x-2 px-3 py-1.5 bg-slate-800 rounded-full text-xs">
            <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span class="text-slate-300">系统在线</span>
            <span class="text-slate-500">| 2026-02-01 10:45:22</span>
          </div>
          <div class="flex items-center space-x-3">
            <div class="relative">
              <span
                class="iconify text-2xl text-slate-400 hover:text-white cursor-pointer"
                data-icon="mdi:bell-outline"
              ></span>
              <span
                class="absolute -top-1 -right-1 w-4 h-4 bg-rose-500 text-[10px] flex items-center justify-center rounded-full text-white border-2 border-slate-950"
                >3</span
              >
            </div>
            <div class="h-8 w-px bg-slate-800 mx-2"></div>
            <div class="flex items-center space-x-3 cursor-pointer group">
              <div class="text-right">
                <div class="text-sm font-semibold text-white group-hover:text-blue-400 transition-colors">
                  李慕华
                </div>
                <div class="text-[10px] text-slate-500 uppercase">高级监控管理员</div>
              </div>
              <div
                class="w-10 h-10 rounded-full border-2 border-slate-700 p-0.5 overflow-hidden ring-2 ring-blue-500/20 bg-slate-700"
              ></div>
            </div>
          </div>
        </div>
      </header>

      <!-- 内容滚动区 -->
      <div class="flex-1 overflow-y-auto p-6 space-y-6 bg-[#0f172a]">
        <!-- 顶部数据概览 -->
        <div class="grid grid-cols-4 gap-6">
          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-2xl hover:border-blue-500/50 transition-all group"
          >
            <div class="flex justify-between items-start">
              <div class="space-y-1">
                <p class="text-slate-400 text-sm">今日检测目标</p>
                <h3 class="text-2xl font-bold font-mono">{{ statistics.todayTargets.toLocaleString() }}</h3>
              </div>
              <div class="p-3 bg-blue-500/10 rounded-xl group-hover:bg-blue-500/20">
                <span class="iconify text-blue-500 text-2xl" data-icon="mdi:target-variant"></span>
              </div>
            </div>
            <div class="mt-4 flex items-center text-xs text-emerald-400">
              <span class="iconify mr-1" data-icon="mdi:trending-up"></span>
              <span>较昨日 +12.5%</span>
            </div>
          </div>

          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-2xl hover:border-amber-500/50 transition-all group"
          >
            <div class="flex justify-between items-start">
              <div class="space-y-1">
                <p class="text-slate-400 text-sm">预警触发次数</p>
                <h3 class="text-2xl font-bold font-mono text-amber-500">{{ statistics.warningCount }}</h3>
              </div>
              <div class="p-3 bg-amber-500/10 rounded-xl group-hover:bg-amber-500/20">
                <span
                  class="iconify text-amber-500 text-2xl"
                  data-icon="mdi:alert-octagon-outline"
                ></span>
              </div>
            </div>
            <div class="mt-4 flex items-center text-xs text-rose-400">
              <span class="iconify mr-1" data-icon="mdi:trending-up"></span>
              <span>异常趋势波动</span>
            </div>
          </div>

          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-2xl hover:border-purple-500/50 transition-all group"
          >
            <div class="flex justify-between items-start">
              <div class="space-y-1">
                <p class="text-slate-400 text-sm">算法平均置信度</p>
                <h3 class="text-2xl font-bold font-mono text-purple-400">{{ statistics.avgConfidence }}%</h3>
              </div>
              <div class="p-3 bg-purple-500/10 rounded-xl group-hover:bg-purple-500/20">
                <span class="iconify text-purple-500 text-2xl" data-icon="mdi:brain"></span>
              </div>
            </div>
            <div class="mt-4 flex items-center text-xs text-slate-500">
              <span>{{ statistics.modelVersion }} 模型</span>
            </div>
          </div>

          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-2xl flex items-center justify-between"
          >
            <div class="space-y-4 w-full">
              <!-- 隐藏的文件选择器 -->
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                class="hidden"
                @change="handleFileSelect"
              />
              
              <button
                class="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg flex items-center justify-center space-x-2 transition-all font-medium"
                @click="handleUploadClick"
              >
                <span class="iconify" data-icon="mdi:upload"></span>
                <span>上传离线检测</span>
              </button>
              <button
                class="w-full py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg flex items-center justify-center space-x-2 transition-all font-medium border border-slate-700"
                @click="handleExportReport"
              >
                <span class="iconify" data-icon="mdi:database-export"></span>
                <span>导出今日报表</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 核心功能区 -->
        <div class="grid grid-cols-3 gap-6">
          <!-- 视频检测主画面 -->
          <div
            class="col-span-2 bg-[#1e293b]/30 border border-slate-800 rounded-2xl overflow-hidden flex flex-col"
          >
            <div
              class="p-4 border-b border-slate-800 flex items-center justify-between bg-[#1e293b]/80 backdrop-blur"
            >
              <div class="flex items-center space-x-4">
                <h3 class="font-bold flex items-center">
                  <span class="w-3 h-3 bg-rose-500 rounded-full mr-3 animate-pulse"></span>
                  主监控频道 - 01节点
                </h3>
                <div class="flex bg-slate-900 rounded-lg p-1 space-x-1">
                  <button class="px-3 py-1 text-xs bg-slate-800 rounded text-blue-400 font-medium">
                    单路
                  </button>
                  <button
                    class="px-3 py-1 text-xs hover:bg-slate-800 rounded text-slate-500 transition-colors"
                  >
                    九宫格
                  </button>
                </div>
              </div>
              <div class="flex items-center space-x-2">
                <button class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-400">
                  <span class="iconify" data-icon="mdi:camera"></span>
                </button>
                <button class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-400">
                  <span class="iconify" data-icon="mdi:cog"></span>
                </button>
                <button
                  :class="[
                    'flex items-center space-x-2 px-4 py-1.5 rounded-lg transition-all',
                    isDetecting
                      ? 'bg-rose-600/20 text-rose-500 border border-rose-600/30 hover:bg-rose-600 hover:text-white'
                      : 'bg-emerald-600/20 text-emerald-500 border border-emerald-600/30 hover:bg-emerald-600 hover:text-white',
                  ]"
                  @click="toggleDetection"
                >
                  <span class="iconify" data-icon="mdi:power"></span>
                  <span class="text-sm font-semibold">{{ isDetecting ? '停止检测' : '开启检测' }}</span>
                </button>
              </div>
            </div>
            <div class="relative bg-black aspect-video group cursor-crosshair overflow-hidden">
              <!-- 本机摄像头画面 -->
              <!-- 实时视频：仅在没有离线结果且摄像头打开时显示 -->
              <video
                v-if="!currentFrameUrl && mediaStream"
                ref="videoEl"
                autoplay
                playsinline
                muted
                class="w-full h-full object-cover"
              />
              <!-- 离线 / 标注结果：有 currentFrameUrl 时优先显示 -->
              <img
                v-else-if="currentFrameUrl"
                :src="currentFrameUrl"
                alt="检测画面"
                class="absolute inset-0 z-10 w-full h-full object-contain pointer-events-none bg-black"
              />
              <!-- 无摄像头且无离线结果时占位 -->
              <div
                v-else-if="!cameraError"
                class="absolute inset-0 flex items-center justify-center text-slate-500 text-sm"
              >
                点击「开启检测」打开本机摄像头
              </div>
              <!-- 摄像头错误提示 -->
              <div
                v-else
                class="absolute inset-0 z-10 flex items-center justify-center text-rose-400 text-sm px-4"
              >
                {{ cameraError }}
              </div>
              <!-- 叠加状态 -->
              <div
                class="absolute bottom-4 left-4 bg-black/60 backdrop-blur px-3 py-1.5 rounded-lg border border-white/10 text-[10px] font-mono grid grid-cols-2 gap-x-4 gap-y-1"
              >
                <div class="text-slate-400">FPS: <span class="text-emerald-400">{{ frameFps }}</span></div>
                <div class="text-slate-400">RES: <span class="text-white">{{ videoSize }}</span></div>
                <div class="text-slate-400">推理: <span class="text-emerald-400">{{ lastInferenceTime }}ms</span></div>
                <div class="text-slate-400">状态: <span :class="isDetecting ? 'text-rose-400' : 'text-slate-500'">{{ isDetecting ? '检测中' : '已停止' }}</span></div>
              </div>
            </div>
            <div class="grid grid-cols-3 gap-1 p-1 bg-slate-900">
              <div class="relative aspect-video border-2 border-blue-500/50 rounded overflow-hidden">
                <div class="w-full h-full bg-slate-700"></div>
                <span class="absolute bottom-1 right-1 text-[8px] bg-black/50 text-white px-1"
                  >CAM 01</span
                >
              </div>
              <div
                class="relative aspect-video border border-slate-700 rounded overflow-hidden opacity-60 hover:opacity-100 transition-opacity"
              >
                <div class="w-full h-full bg-slate-700"></div>
                <span class="absolute bottom-1 right-1 text-[8px] bg-black/50 text-white px-1"
                  >CAM 02</span
                >
              </div>
              <div
                class="relative aspect-video border border-slate-700 rounded overflow-hidden opacity-60 hover:opacity-100 transition-opacity"
              >
                <div class="w-full h-full bg-slate-700"></div>
                <span class="absolute bottom-1 right-1 text-[8px] bg-black/50 text-white px-1"
                  >CAM 03</span
                >
              </div>
            </div>
          </div>

          <!-- 侧边趋势与记录 -->
          <div class="flex flex-col space-y-6">
            <!-- 实时检测趋势图 -->
            <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 flex-1 flex flex-col">
              <div class="flex items-center justify-between mb-6">
                <h3 class="font-bold">实时频率趋势</h3>
                <span
                  class="iconify text-slate-500 cursor-help"
                  data-icon="mdi:information-outline"
                ></span>
              </div>
              <div ref="chartTrend" class="flex-1 w-full min-h-[160px]"></div>
            </div>

            <!-- 最近操作记录 -->
            <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 h-80 flex flex-col">
              <div class="flex items-center justify-between mb-4">
                <h3 class="font-bold">最近检测记录</h3>
                <button class="text-xs text-blue-500 hover:underline">查看全部</button>
              </div>
              <div class="flex-1 overflow-y-auto space-y-3 pr-2 scrollbar-thin hide-scrollbar">
                <!-- 动态渲染检测记录 -->
                <div
                  v-for="record in recentRecords"
                  :key="record.id"
                  :class="[
                    'p-3 rounded-xl flex items-center space-x-3',
                    record.type === 'danger' || record.type === 'warning'
                      ? 'bg-rose-500/5 border border-rose-500/20'
                      : 'bg-slate-900/50 border border-slate-800'
                  ]"
                >
                  <div
                    :class="[
                      'w-10 h-10 rounded-lg flex items-center justify-center border',
                      record.type === 'danger' || record.type === 'warning'
                        ? 'bg-rose-500/10 border-rose-500/20'
                        : 'bg-emerald-500/10 border-emerald-500/20'
                    ]"
                  >
                    <span
                      class="iconify"
                      :class="record.type === 'danger' || record.type === 'warning' ? 'text-rose-500' : 'text-emerald-500'"
                      :data-icon="record.type === 'danger' || record.type === 'warning' ? 'mdi:alert-box' : 'mdi:check-circle'"
                    ></span>
                  </div>
                  <div class="flex-1">
                    <div
                      :class="[
                        'text-sm font-medium',
                        record.type === 'danger' || record.type === 'warning' ? 'text-rose-400' : ''
                      ]"
                    >
                      {{ getDefectLabel(record.title) }}
                    </div>
                    <div class="text-[10px] text-slate-500">{{ record.description }}</div>
                  </div>
                  <div
                    :class="[
                      'text-xs font-mono',
                      record.type === 'danger' || record.type === 'warning' ? 'text-rose-400' : 'text-slate-400'
                    ]"
                  >
                    {{ record.confidence }}
                  </div>
                </div>
                
                <!-- 无数据提示 -->
                <div
                  v-if="recentRecords.length === 0"
                  class="p-6 text-center text-slate-500 text-sm"
                >
                  暂无检测记录
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
import * as echarts from 'echarts';
import Layout from '@/components/Layout.vue';
import { 
  getRealtimeStatistics, 
  getRealtimeRecords, 
  getRealtimeTrend, 
  controlDetection,
  uploadDetection,
  uploadRealtimeFrame 
} from '@/api/detection';
import { getDefectDistribution } from '@/api/dashboard';
import { getDefectLabel } from '@/utils/constants';

const BROWSER_CAMERA_ID = 'browser';
const FRAME_INTERVAL_MS = 800;
const PANEL_REFRESH_INTERVAL_MS = 3000;

export default {
  name: 'RealTimeDetect',
  components: {
    Layout,
  },
  data() {
    return {
      isDetecting: false,
      chartInstance: null,
      loading: false,
      ws: null,
      mediaStream: null,
      frameTimerId: null,
      panelRefreshTimerId: null,
      // 视频流区域当前显示的画面（实时帧或离线标注图）
      currentFrameUrl: '',
      annotatedImage: '',
      offlineDetections: [],
      lastInferenceTime: 0,
      cameraError: '',
      frameFps: '0',
      videoSize: '--',
      // 统计数据
      statistics: {
        todayTargets: 0,
        warningCount: 0,
        avgConfidence: 0,
        modelVersion: 'YOLOv8-Standard'
      },
      recentRecords: [],
      trendData: {
        timeLabels: [],
        detectionCounts: []
      }
    };
  },
  mounted() {
    this.loadAllData();
    this.connectWebSocket();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    this.stopRealtimeDetection();
    window.removeEventListener('resize', this.handleResize);
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
    if (this.ws) {
      this.ws.close();
    }
  },
  methods: {
    /**
     * 加载所有数据
     */
    async loadAllData() {
      this.loading = true;
      try {
        // 并行加载数据
        const [statsRes, recordsRes, trendRes] = await Promise.all([
          getRealtimeStatistics(),
          getRealtimeRecords({ limit: 10 }),
          getRealtimeTrend()
        ]);
        
        // 更新统计数据
        this.statistics = statsRes.data;
        
        // 更新记录列表
        this.recentRecords = recordsRes.data.records || [];
        
        // 更新趋势数据
        this.trendData = trendRes.data;
        
        // 初始化图表
        this.initChart();
      } catch (error) {
        console.error('加载实时数据失败', error);
        alert(error.message || '加载数据失败');
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 连接 WebSocket
     */
    connectWebSocket() {
      const token = localStorage.getItem('token');
      if (!token) {
        console.warn('未找到 token，无法连接 WebSocket');
        return;
      }
      
      const wsUrl = `ws://localhost:8000/ws?token=${token}`;
      this.ws = new WebSocket(wsUrl);
      
      this.ws.onopen = () => {
        console.log('WebSocket 已连接');
      };
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleWebSocketMessage(message);
        } catch (error) {
          console.error('解析 WebSocket 消息失败', error);
        }
      };
      
      this.ws.onerror = (error) => {
        console.error('WebSocket 错误', error);
      };
      
      this.ws.onclose = () => {
        console.log('WebSocket 已断开');
        // 5秒后重连
        setTimeout(() => {
          if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
            this.connectWebSocket();
          }
        }, 5000);
      };
    },
    
    /**
     * 处理 WebSocket 消息
     */
    handleWebSocketMessage(message) {
      if (message.type === 'detection') {
        this.addNewDetection(message.data);
        this.refreshPanelData();
      } else if (message.type === 'alert') {
        this.showAlert(message.data);
        this.refreshPanelData();
      } else if (message.type === 'status') {
        this.updateStatus(message.data);
      }
    },
    
    /**
     * 添加新检测记录
     */
    addNewDetection(data) {
      // 添加到记录列表顶部
      this.recentRecords.unshift({
        id: data.id || Date.now(),
        type: data.type || 'normal',
        title: data.title || '检测完成',
        description: data.description || '',
        confidence: data.confidence || '0%',
        timestamp: data.timestamp || new Date().toLocaleTimeString()
      });
      
      // 最多保留 20 条记录
      if (this.recentRecords.length > 20) {
        this.recentRecords = this.recentRecords.slice(0, 20);
      }
      
      // 更新统计数据
      this.statistics.todayTargets++;
      if (data.type === 'warning' || data.type === 'danger') {
        this.statistics.warningCount++;
      }
    },
    
    showAlert(data) {
      console.log('收到报警', data);
      // 可以显示通知或更新报警列表
    },
    
    updateStatus(data) {
      console.log('状态更新', data);
      // 更新页面状态
    },
    
    /**
     * 点击上传按钮 - 触发文件选择
     */
    handleUploadClick() {
      // 触发隐藏的 input[type="file"] 点击事件
      this.$refs.fileInput.click();
    },
    
    /**
     * 文件选择后 - 上传文件
     */
    async handleFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        alert('请上传图片文件');
        return;
      }
      
      // 检查文件大小（最大 10MB）
      if (file.size > 10 * 1024 * 1024) {
        alert('图片大小不能超过 10MB');
        return;
      }
      
      this.loading = true;
      this.loadingText = '正在上传并检测...';
      
      try {
        // 创建 FormData
        const formData = new FormData();
        formData.append('file', file);
        formData.append('serialNo', 'SN-UPLOAD-' + Date.now());
        formData.append('productId', '1');
        
        // 调用上传检测接口 POST /v1/detection/upload（带 JWT）
        // 兼容：res.data.data（约定）或 res.data（单层）；annotated_image 可能在 data 或 yolo.data 下
        const res = await uploadDetection(formData);
        const inner = res?.data?.data != null ? res.data.data : res?.data;
        const annotated =
          (inner && (
            inner.annotated_image ||
            (inner.yolo && (inner.yolo.annotated_image || (inner.yolo.data && inner.yolo.data.annotated_image)))
          )) || '';
        const detections =
          (inner && Array.isArray(inner.detections) ? inner.detections : null) ||
          (inner?.yolo?.data?.detections) ||
          [];
        const record = inner ? inner.record : null;
        const hasDefect = detections.length > 0 || (record && record.type && record.type !== 'normal');

        if (!annotated && inner) {
          console.warn('上传检测未取到 annotated_image，请核对路径。inner.yolo.data =', inner.yolo?.data);
        }

        // 视频流区域显示源：用返回的标注图更新 currentFrameUrl，确保触发视图更新
        this.currentFrameUrl = annotated;
        this.annotatedImage = annotated;
        this.offlineDetections = detections;
        this.$nextTick();

        if (!hasDefect) {
          alert('检测完成！未发现缺陷');
        } else {
          const defectCount = detections.length || (record && record.detection_count) || 1;
          alert(`检测完成！发现 ${defectCount} 个缺陷`);
        }

        // 仅刷新本页统计/最近记录；记录管理列表需在「记录管理」页点刷新或重新进入才会拉取新数据
        this.loadAllData();
      } catch (error) {
        console.error('上传检测失败', error);
        alert(error.message || '上传失败');
      } finally {
        this.loading = false;
        // 清空文件选择，允许重复上传同一文件
        this.$refs.fileInput.value = '';
      }
    },
    
    /**
     * 导出今日报表
     */
    handleExportReport() {
      alert('导出今日报表功能待后端实现');
    },
    
    async toggleDetection() {
      if (this.isDetecting) {
        this.stopRealtimeDetection();
        return;
      }
      try {
        this.cameraError = '';
        // 先清空离线结果，否则 v-if="!currentFrameUrl && mediaStream" 下 <video> 不会挂载，$refs.videoEl 为 undefined
        this.currentFrameUrl = '';
        this.annotatedImage = '';
        this.offlineDetections = [];
        await this.$nextTick();

        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        this.mediaStream = stream;
        await this.$nextTick();
        const video = this.$refs.videoEl;
        if (video) {
          video.srcObject = stream;
          await video.play();
          this.videoSize = `${video.videoWidth || 0}×${video.videoHeight || 0}`;
        }
        await controlDetection({ action: 'start', cameraId: BROWSER_CAMERA_ID });
        this.isDetecting = true;
        this.frameTimerId = setInterval(() => this.captureAndSendFrame(), FRAME_INTERVAL_MS);
        this.panelRefreshTimerId = setInterval(() => this.refreshPanelData(), PANEL_REFRESH_INTERVAL_MS);
        const intervalSec = FRAME_INTERVAL_MS / 1000;
        this.frameFps = (1 / intervalSec).toFixed(1);
      } catch (err) {
        console.error('开启检测失败', err);
        if (this.mediaStream) {
          this.mediaStream.getTracks().forEach(t => t.stop());
          this.mediaStream = null;
        }
        this.cameraError = err.name === 'NotAllowedError' ? '请允许使用摄像头' : (err.message || '无法打开摄像头');
        alert(this.cameraError);
      }
    },

    stopRealtimeDetection() {
      if (this.frameTimerId) {
        clearInterval(this.frameTimerId);
        this.frameTimerId = null;
      }
      if (this.panelRefreshTimerId) {
        clearInterval(this.panelRefreshTimerId);
        this.panelRefreshTimerId = null;
      }
      if (this.mediaStream) {
        this.mediaStream.getTracks().forEach(t => t.stop());
        this.mediaStream = null;
      }
      controlDetection({ action: 'stop', cameraId: BROWSER_CAMERA_ID }).catch(e => console.warn('停止检测接口调用失败', e));
      this.isDetecting = false;
      this.currentFrameUrl = '';
      this.annotatedImage = '';
      this.offlineDetections = [];
      this.cameraError = '';
      this.frameFps = '0';
      this.videoSize = '--';
    },

    captureAndSendFrame() {
      const video = this.$refs.videoEl;
      if (!video || !this.mediaStream || video.readyState !== 4 || video.videoWidth === 0) return;
      const w = video.videoWidth;
      const h = video.videoHeight;
      const canvas = document.createElement('canvas');
      canvas.width = w;
      canvas.height = h;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0);
      canvas.toBlob(blob => {
        if (!blob || !this.isDetecting) return;
        const formData = new FormData();
        formData.append('file', blob, 'frame.jpg');
        formData.append('cameraId', BROWSER_CAMERA_ID);
        uploadRealtimeFrame(formData).then(res => {
          if (res.code === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
            return;
          }
          if (res.code !== 200) {
            console.warn('帧检测接口异常', res.message || res);
            return;
          }
          const data = res.data || {};
          if (data.annotated_image) {
            this.annotatedImage = data.annotated_image;
            // 实时检测如需使用同一展示源，也可以同步更新 currentFrameUrl
            // this.currentFrameUrl = data.annotated_image;
          }
          if (typeof data.inference_time_ms === 'number') this.lastInferenceTime = data.inference_time_ms;
          if (data.record) {
            this.addNewDetection({
              id: data.record.id,
              type: data.record.type || (data.has_defect ? 'warning' : 'normal'),
              title: data.record.defect || data.record.class_name || '检测完成',
              description: data.record.serialNo || '',
              confidence: data.record.confidence != null ? `${data.record.confidence}%` : '',
              timestamp: data.record.timestamp || new Date().toLocaleTimeString()
            });
          }
          if (data.has_defect) this.refreshPanelData();
        }).catch(e => console.warn('上传帧失败', e));
      }, 'image/jpeg', 0.85);
    },

    async refreshPanelData() {
      try {
        const [statsRes, trendRes, distRes] = await Promise.all([
          getRealtimeStatistics(),
          getRealtimeTrend(),
          getDefectDistribution()
        ]);
        if (statsRes && statsRes.data) this.statistics = statsRes.data;
        if (trendRes && trendRes.data) this.trendData = trendRes.data;
        if (this.chartInstance && this.trendData.timeLabels && this.trendData.timeLabels.length) {
          this.chartInstance.setOption({
            xAxis: { data: this.trendData.timeLabels },
            series: [{ data: this.trendData.detectionCounts }]
          });
        }
      } catch (e) {
        console.warn('刷新面板失败', e);
      }
    },
    
    initChart() {
      if (!this.$refs.chartTrend) return;
      
      // 检查数据是否存在
      if (!this.trendData.timeLabels || this.trendData.timeLabels.length === 0) {
        console.warn('趋势数据为空，暂不初始化图表');
        return;
      }

      this.chartInstance = echarts.init(this.$refs.chartTrend, 'dark');
      const option = {
        backgroundColor: 'transparent',
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10%',
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.trendData.timeLabels,
          axisLine: {
            lineStyle: {
              color: '#334155',
            },
          },
          axisLabel: {
            color: '#64748b',
            fontSize: 10,
          },
        },
        yAxis: {
          type: 'value',
          splitLine: {
            lineStyle: {
              color: '#1e293b',
            },
          },
          axisLabel: {
            color: '#64748b',
            fontSize: 10,
          },
        },
        series: [
          {
            name: '检测数',
            type: 'line',
            smooth: true,
            data: this.trendData.detectionCounts,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(59, 130, 246, 0.3)',
                },
                {
                  offset: 1,
                  color: 'rgba(59, 130, 246, 0)',
                },
              ]),
            },
            lineStyle: {
              width: 3,
              color: '#3b82f6',
            },
            symbol: 'none',
          },
        ],
      };
      this.chartInstance.setOption(option);
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize();
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
@keyframes pulse-red {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }
}

.animate-pulse-red {
  animation: pulse-red 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
