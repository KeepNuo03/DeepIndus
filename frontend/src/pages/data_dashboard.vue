<template>
  <Layout>
    <main class="flex-1 flex flex-col min-w-0 bg-[#0f172a] overflow-hidden">
      <!-- 顶部导航 -->
      <header
        class="h-14 border-b border-slate-800 bg-[#0d1425]/50 flex items-center justify-between px-6 shrink-0"
      >
        <div class="flex items-center space-x-3">
          <span class="text-slate-500 text-sm">主控制台</span>
          <span class="iconify text-slate-600" data-icon="material-symbols:chevron-right"></span>
          <span class="text-blue-400 text-sm font-medium">数据大屏</span>
        </div>
        <div class="flex items-center space-x-6">
          <div class="text-right">
            <p class="text-[10px] text-slate-500 uppercase tracking-widest">Current Date</p>
            <p class="text-xs font-mono text-slate-300">{{ currentDateTime }}</p>
          </div>
          <div class="h-8 w-px bg-slate-800"></div>
          <div class="flex items-center space-x-3">
            <div class="text-right">
              <p class="text-xs font-semibold text-slate-200">Admin_01</p>
              <p class="text-[10px] text-emerald-500 bg-emerald-500/10 px-1 rounded">在线</p>
            </div>
            <div class="w-8 h-8 rounded-full bg-slate-700 border border-slate-600 overflow-hidden"></div>
          </div>
        </div>
      </header>

      <!-- 主内容区 - 数据大屏展示 -->
      <div class="flex-1 p-6 space-y-6 overflow-y-auto hide-scrollbar">
        <!-- KPI 指标卡片 -->
        <div class="grid grid-cols-4 gap-6">
          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-xl shadow-sm hover:border-blue-500/50 transition-all group"
          >
            <div class="flex justify-between items-start mb-3">
              <span class="text-slate-400 text-sm">当日总检测数</span>
              <span
                class="iconify text-blue-500 text-xl"
                data-icon="material-symbols:biotech-outline"
              ></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-2xl font-bold text-white">{{ kpiData.todayDetectionCount.toLocaleString() }}</h3>
              <span class="text-emerald-500 text-xs font-medium">+12.5%</span>
            </div>
            <div class="mt-4 h-1 bg-slate-800 rounded-full overflow-hidden">
              <div class="h-full bg-blue-500 w-[75%]"></div>
            </div>
          </div>

          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-xl shadow-sm hover:border-red-500/50 transition-all"
          >
            <div class="flex justify-between items-start mb-3">
              <span class="text-slate-400 text-sm">平均缺陷率</span>
              <span
                class="iconify text-red-500 text-xl"
                data-icon="material-symbols:error-outline-rounded"
              ></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-2xl font-bold text-white">{{ kpiData.avgDefectRate }}%</h3>
              <span class="text-red-400 text-xs font-medium">-0.05%</span>
            </div>
            <div class="mt-4 h-1 bg-slate-800 rounded-full overflow-hidden">
              <div class="h-full bg-red-500 w-[15%]"></div>
            </div>
          </div>

          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-xl shadow-sm hover:border-emerald-500/50 transition-all"
          >
            <div class="flex justify-between items-start mb-3">
              <span class="text-slate-400 text-sm">良品率 (Yield)</span>
              <span
                class="iconify text-emerald-500 text-xl"
                data-icon="material-symbols:verified-outline"
              ></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-2xl font-bold text-white">{{ kpiData.yieldRate }}%</h3>
              <span class="text-emerald-500 text-xs font-medium">+0.02%</span>
            </div>
            <div class="mt-4 h-1 bg-slate-800 rounded-full overflow-hidden">
              <div class="h-full bg-emerald-500 w-[99%]"></div>
            </div>
          </div>

          <div
            class="bg-[#1e293b]/30 border border-slate-800 p-5 rounded-xl shadow-sm hover:border-amber-500/50 transition-all"
          >
            <div class="flex justify-between items-start mb-3">
              <span class="text-slate-400 text-sm">设备稼动率</span>
              <span
                class="iconify text-amber-500 text-xl"
                data-icon="material-symbols:precision-manufacturing-outline"
              ></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-2xl font-bold text-white">{{ kpiData.utilizationRate }}%</h3>
              <span class="text-slate-500 text-xs font-medium">稳定</span>
            </div>
            <div class="mt-4 h-1 bg-slate-800 rounded-full overflow-hidden">
              <div class="h-full bg-amber-500 w-[88%]"></div>
            </div>
          </div>
        </div>

        <!-- 中间图表区 -->
        <div class="grid grid-cols-12 gap-6">
          <!-- 缺陷趋势折线图 -->
          <div class="col-span-8 bg-[#1e293b]/30 border border-slate-800 p-6 rounded-xl">
            <div class="flex items-center justify-between mb-6">
              <h4 class="font-bold flex items-center">
                <span class="w-1.5 h-4 bg-blue-500 rounded-full mr-2"></span>
                24小时缺陷趋势分析
              </h4>
              <div class="flex space-x-2">
                <span class="px-2 py-1 bg-slate-800 rounded text-[10px] text-slate-400"
                  >实时更新中</span
                >
              </div>
            </div>
            <div ref="trendChart" class="w-full h-[300px]"></div>
          </div>

          <!-- 缺陷类型分布饼图 -->
          <div class="col-span-4 bg-[#1e293b]/30 border border-slate-800 p-6 rounded-xl">
            <div class="flex items-center justify-between mb-6">
              <h4 class="font-bold flex items-center">
                <span class="w-1.5 h-4 bg-indigo-500 rounded-full mr-2"></span>
                缺陷类型占比
              </h4>
            </div>
            <div ref="pieChart" class="w-full h-[300px]"></div>
          </div>
        </div>

        <!-- 底部图表与列表区 -->
        <div class="grid grid-cols-12 gap-6 pb-6">
          <!-- 缺陷位置热力图 -->
          <div
            class="col-span-7 bg-[#1e293b]/30 border border-slate-800 p-6 rounded-xl relative overflow-hidden"
          >
            <div class="flex items-center justify-between mb-6">
              <h4 class="font-bold flex items-center">
                <span class="w-1.5 h-4 bg-orange-500 rounded-full mr-2"></span>
                工件缺陷热力分布图
              </h4>
              <select
                class="bg-slate-800 border-none text-[10px] rounded px-2 py-1 text-slate-300 focus:ring-1 focus:ring-blue-500"
              >
                <option>流水线_A1</option>
                <option>流水线_B2</option>
              </select>
            </div>
            <div
              class="flex justify-center items-center h-[280px] bg-slate-900/50 rounded-lg relative"
            >
              <div class="text-slate-600 text-sm">工件热力图区域</div>
              <!-- 模拟热点 -->
              <div
                class="absolute top-1/4 left-1/3 w-12 h-12 bg-red-500/40 rounded-full blur-xl animate-pulse"
              ></div>
              <div
                class="absolute top-1/2 left-1/2 w-8 h-8 bg-orange-500/30 rounded-full blur-lg"
              ></div>
              <div
                class="absolute bottom-1/4 right-1/4 w-16 h-16 bg-red-500/20 rounded-full blur-2xl"
              ></div>
            </div>
          </div>

          <!-- 实时报警列表 -->
          <div class="col-span-5 bg-[#1e293b]/30 border border-slate-800 p-6 rounded-xl flex flex-col">
            <div class="flex items-center justify-between mb-4">
              <h4 class="font-bold flex items-center text-red-400">
                <span class="w-1.5 h-4 bg-red-500 rounded-full mr-2"></span>
                实时异常报警列表
              </h4>
              <span class="text-[10px] text-red-500/80 animate-pulse-red">LIVE</span>
            </div>
            <div class="flex-1 overflow-y-auto space-y-3 pr-2 hide-scrollbar">
              <div
                v-for="alert in alerts"
                :key="alert.id"
                :class="[
                  'p-3 border-l-4 rounded-r-lg flex items-center',
                  alert.severity === 'critical'
                    ? 'bg-red-500/5 border-red-500'
                    : alert.severity === 'warning'
                      ? 'bg-amber-500/5 border-amber-500'
                      : 'bg-slate-800/20 border-slate-600 grayscale opacity-60',
                ]"
              >
                <div
                  :class="[
                    'w-10 h-10 rounded-lg flex items-center justify-center mr-3',
                    alert.severity === 'critical'
                      ? 'bg-red-500/10'
                      : alert.severity === 'warning'
                        ? 'bg-amber-500/10'
                        : 'bg-slate-700/20',
                  ]"
                >
                  <span
                    class="iconify text-xl"
                    :class="
                      alert.severity === 'critical'
                        ? 'text-red-500'
                        : alert.severity === 'warning'
                          ? 'text-amber-500'
                          : 'text-slate-500'
                    "
                    :data-icon="alert.icon"
                  ></span>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex justify-between">
                    <span
                      :class="[
                        'text-xs font-bold',
                        alert.severity === 'resolved' ? 'text-slate-400' : 'text-slate-200',
                      ]"
                      >{{ alert.title }}</span
                    >
                    <span class="text-[10px] text-slate-500 font-mono">{{ alert.time }}</span>
                  </div>
                  <p class="text-[10px] text-slate-400 truncate">{{ alert.description }}</p>
                </div>
              </div>
            </div>
            <button
              class="mt-4 w-full py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] rounded transition-colors uppercase tracking-widest"
            >
              查看全部记录
            </button>
          </div>
        </div>
      </div>
    </main>
  </Layout>
</template>

<script>
import * as echarts from 'echarts';
import Layout from '@/components/Layout.vue';
import { getKPI, getDefectTrend, getDefectDistribution, getAlerts } from '@/api/dashboard';
import { getDefectLabel } from '@/utils/constants';

export default {
  name: 'DataDashboard',
  components: {
    Layout,
  },
  data() {
    return {
      currentDateTime: '',
      loading: false,
      trendChartInstance: null,
      pieChartInstance: null,
      // ⚙️ 时区修正配置（临时方案）
      // 如果后端时间有偏差，调整这个值
      // 例如：后端慢 2 小时，设置为 2
      timeOffsetHours: 2,  // ← 修正 2 小时
      // KPI 数据
      kpiData: {
        todayDetectionCount: 0,
        avgDefectRate: 0,
        yieldRate: 0,
        utilizationRate: 0
      },
      // 趋势数据
      trendData: {
        timeLabels: [],
        defectCounts: [],
        yieldRates: []
      },
      // 分布数据
      distributionData: [],
      // 报警列表
      alerts: [],  // 从后端加载
    };
  },
  mounted() {
    this.updateDateTime();
    setInterval(this.updateDateTime, 1000);
    this.loadAllData();  // 加载所有数据
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    if (this.trendChartInstance) {
      this.trendChartInstance.dispose();
    }
    if (this.pieChartInstance) {
      this.pieChartInstance.dispose();
    }
  },
  methods: {
    updateDateTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      this.currentDateTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    
    /**
     * 加载所有数据
     */
    async loadAllData() {
      this.loading = true;
      try {
        // 并行加载所有数据
        const [kpiRes, trendRes, distRes, alertsRes] = await Promise.all([
          getKPI(),
          getDefectTrend(),
          getDefectDistribution(),
          getAlerts()
        ]);
        
        // 更新 KPI 数据
        this.kpiData = kpiRes.data;
        
        // 🔧 临时修正：调整后端返回的时间（如果有时差）
        if (this.timeOffsetHours !== 0 && trendRes.data.timeLabels) {
          console.log(`⚙️  应用时区修正：+${this.timeOffsetHours} 小时`);
          this.trendData = {
            ...trendRes.data,
            timeLabels: trendRes.data.timeLabels.map(time => this.adjustTime(time, this.timeOffsetHours))
          };
        } else {
          this.trendData = trendRes.data;
        }
        
        // 🔍 调试：检查时间数据
        console.log('');
        console.log('🕐 ============ 时间调试信息 ============');
        console.log('📥 后端返回的时间标签（原始）:', trendRes.data.timeLabels);
        if (this.timeOffsetHours !== 0) {
          console.log('🔧 前端修正后的时间:', this.trendData.timeLabels);
        }
        console.log('⏰ 当前真实时间:', new Date().toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit' }));
        if (trendRes.data.timeLabels && trendRes.data.timeLabels.length > 0) {
          const lastTimeOriginal = trendRes.data.timeLabels[trendRes.data.timeLabels.length - 1];
          const lastTimeAdjusted = this.trendData.timeLabels[this.trendData.timeLabels.length - 1];
          console.log('📊 图表最后时间点（后端）:', lastTimeOriginal);
          console.log('📊 图表最后时间点（修正后）:', lastTimeAdjusted);
        }
        console.log('🌍 浏览器时区偏移:', new Date().getTimezoneOffset(), '分钟 (负数表示东时区)');
        console.log('========================================');
        console.log('');
        
        // 更新分布数据
        this.distributionData = distRes.data;
        
        // 🔍 调试：打印后端返回的原始数据
        console.log('=== 后端返回的缺陷分布数据 ===');
        console.log('原始数据:', distRes.data);
        console.log('数据类型:', this.distributionData.map(item => ({
          type: item.type,
          count: item.count,
          转换后: this.getDefectLabel ? this.getDefectLabel(item.type) : '未定义转换函数'
        })));
        console.log('==============================');
        
        // 更新报警列表
        this.alerts = alertsRes.data;
        
        // 初始化图表（使用真实数据）
        this.initCharts();
      } catch (error) {
        console.error('加载数据失败', error);
        alert(error.message || '加载数据失败');
      } finally {
        this.loading = false;
      }
    },
    
    initCharts() {
      // 检查数据是否已加载
      if (!this.trendData.timeLabels || this.trendData.timeLabels.length === 0) {
        console.warn('趋势数据为空，暂不初始化图表');
        return;
      }
      
      // 趋势图配置
      if (!this.$refs.trendChart) return;
      this.trendChartInstance = echarts.init(this.$refs.trendChart);
      this.trendChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '3%',
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          data: this.trendData.timeLabels,
          axisLine: {
            lineStyle: {
              color: '#334155',
            },
          },
          axisLabel: {
            color: '#94a3b8',
            fontSize: 10,
          },
        },
        yAxis: [
          {
            type: 'value',
            name: '缺陷数量',
            position: 'left',
            splitLine: {
              lineStyle: {
                color: '#1e293b',
              },
            },
            axisLabel: {
              color: '#94a3b8',
              fontSize: 10,
              formatter: '{value} 个'
            },
            axisLine: {
              show: true,
              lineStyle: {
                color: '#3b82f6'
              }
            },
            nameTextStyle: {
              color: '#3b82f6',
              fontSize: 11
            }
          },
          {
            type: 'value',
            name: '良品率',
            position: 'right',
            min: 95,
            max: 100,
            splitLine: {
              show: false
            },
            axisLabel: {
              color: '#94a3b8',
              fontSize: 10,
              formatter: '{value}%'
            },
            axisLine: {
              show: true,
              lineStyle: {
                color: '#10b981'
              }
            },
            nameTextStyle: {
              color: '#10b981',
              fontSize: 11
            }
          }
        ],
        series: [
          {
            name: '缺陷数量',
            type: 'line',
            yAxisIndex: 0,  // 使用左侧 Y 轴
            smooth: true,
            showSymbol: false,
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
            data: this.trendData.defectCounts,
          },
          {
            name: '良品率%',
            type: 'line',
            yAxisIndex: 1,  // 使用右侧 Y 轴
            smooth: true,
            showSymbol: false,
            lineStyle: {
              width: 2,
              color: '#10b981',
              type: 'dashed',
            },
            data: this.trendData.yieldRates,
          },
        ],
      });

      // 饼图配置
      if (!this.$refs.pieChart) return;
      
      // 检查数据是否存在
      if (!this.distributionData || this.distributionData.length === 0) {
        console.warn('缺陷分布数据为空，暂不初始化饼图');
        return;
      }
      
      this.pieChartInstance = echarts.init(this.$refs.pieChart);
      this.pieChartInstance.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
        },
        legend: {
          bottom: '5%',
          left: 'center',
          textStyle: {
            color: '#94a3b8',
            fontSize: 10,
          },
        },
        series: [
          {
            name: '缺陷类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 6,
              borderColor: '#131c31',
              borderWidth: 2,
            },
            label: {
              show: false,
            },
            labelLine: {
              show: false,
            },
            data: this.distributionData.map((item, index) => ({
              value: item.count,
              name: getDefectLabel(item.type),  // 转换为中文显示
              itemStyle: {
                color: ['#3b82f6', '#6366f1', '#f43f5e', '#f59e0b', '#8b5cf6', '#ec4899'][index % 6]
              }
            })),
          },
        ],
      });
    },
    handleResize() {
      if (this.trendChartInstance) {
        this.trendChartInstance.resize();
      }
      if (this.pieChartInstance) {
        this.pieChartInstance.resize();
      }
    },
    
    /**
     * 缺陷类型转换：英文 → 中文
     */
    getDefectLabel(value) {
      return getDefectLabel(value);
    },
    
    /**
     * 调整时间（临时方案，修正时区偏差）
     * @param timeStr - 时间字符串，格式 "HH:mm"
     * @param hours - 要调整的小时数（正数表示加，负数表示减）
     * @returns 调整后的时间字符串
     */
    adjustTime(timeStr, hours) {
      if (!timeStr || hours === 0) return timeStr;
      
      try {
        // 解析时间字符串 "HH:mm"
        const [hour, minute] = timeStr.split(':').map(Number);
        
        // 创建日期对象
        const date = new Date();
        date.setHours(hour);
        date.setMinutes(minute);
        
        // 添加小时数
        date.setHours(date.getHours() + hours);
        
        // 格式化返回
        const newHour = String(date.getHours()).padStart(2, '0');
        const newMinute = String(date.getMinutes()).padStart(2, '0');
        
        return `${newHour}:${newMinute}`;
      } catch (error) {
        console.error('时间调整失败', error);
        return timeStr;
      }
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
