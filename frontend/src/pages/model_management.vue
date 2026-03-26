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
          <span class="text-indigo-400 font-medium">AI 模型管理</span>
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
            @click="handleUploadModel"
          >
            <span class="iconify" data-icon="material-symbols:upload"></span>
            <span>上传新模型</span>
          </button>
        </div>
      </header>

      <!-- 主内容区 -->
      <div class="flex-1 overflow-y-auto p-8 space-y-6 hide-scrollbar">
        <!-- 统计概览 -->
        <div class="grid grid-cols-4 gap-6">
          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-blue-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">模型总数</span>
              <span class="iconify text-blue-500 text-2xl" data-icon="material-symbols:model-training-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-white">{{ statistics.totalModels }}</h3>
              <span class="text-xs text-slate-500">个模型</span>
            </div>
          </div>

          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-emerald-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">部署中</span>
              <span class="iconify text-emerald-500 text-2xl" data-icon="material-symbols:deployed-code-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-emerald-400">{{ statistics.deployed }}</h3>
              <span class="text-emerald-500 text-xs font-medium">运行中</span>
            </div>
          </div>

          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-purple-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">平均准确率</span>
              <span class="iconify text-purple-500 text-2xl" data-icon="material-symbols:target"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-purple-400">{{ statistics.avgAccuracy }}%</h3>
            </div>
          </div>

          <div class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-5 hover:border-amber-500/50 transition-all">
            <div class="flex items-center justify-between mb-3">
              <span class="text-slate-400 text-sm">A/B 测试</span>
              <span class="iconify text-amber-500 text-2xl" data-icon="material-symbols:science-outline"></span>
            </div>
            <div class="flex items-baseline space-x-2">
              <h3 class="text-3xl font-bold text-amber-400">{{ statistics.abTests }}</h3>
              <span class="text-xs text-slate-500">进行中</span>
            </div>
          </div>
        </div>

        <!-- 模型列表 -->
        <div v-if="loading" class="flex items-center justify-center h-64">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-400">加载中...</p>
          </div>
        </div>
        <div v-else-if="models.length === 0" class="flex items-center justify-center h-64">
          <div class="text-center text-slate-500">
            <p class="text-lg mb-2">暂无模型数据</p>
            <p class="text-sm mb-4">请检查后端 API 是否正确返回模型列表</p>
            <div class="space-x-2">
              <button
                class="px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded-lg text-xs transition-all"
                @click="showDebugInfo = !showDebugInfo"
              >
                {{ showDebugInfo ? '隐藏' : '显示' }}调试信息
              </button>
              <button
                class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 rounded-lg text-xs transition-all text-white"
                @click="testApiConnection"
              >
                测试 API 连接
              </button>
            </div>
            <pre v-if="showDebugInfo" class="mt-4 text-left text-xs bg-slate-900 p-4 rounded-lg overflow-auto max-w-lg max-h-64">{{ debugInfo }}</pre>
          </div>
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="model in models"
            :key="model.id"
            class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl overflow-hidden hover:border-indigo-500/50 transition-all"
          >
            <div class="p-6">
              <div class="flex items-start justify-between mb-6">
                <!-- 模型基本信息 -->
                <div class="flex items-start space-x-4">
                  <div
                    :class="[
                      'w-16 h-16 rounded-xl flex items-center justify-center',
                      model.status === 'deployed'
                        ? 'bg-emerald-500/10 border-2 border-emerald-500/20'
                        : model.status === 'active'
                          ? 'bg-blue-500/10 border-2 border-blue-500/20'
                          : 'bg-slate-700/50 border-2 border-slate-600/20',
                    ]"
                  >
                    <span
                      :class="[
                        'iconify text-3xl',
                        model.status === 'deployed'
                          ? 'text-emerald-500'
                          : model.status === 'active'
                            ? 'text-blue-500'
                            : 'text-slate-500',
                      ]"
                      data-icon="material-symbols:neurology-outline"
                    ></span>
                  </div>
                  <div>
                    <div class="flex items-center space-x-3 mb-2">
                      <h3 class="text-xl font-bold text-white">{{ model.name }}</h3>
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-bold flex items-center space-x-1',
                          model.status === 'deployed'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : model.status === 'active'
                              ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                              : 'bg-slate-700/50 text-slate-400 border border-slate-600/20',
                        ]"
                      >
                        <span
                          v-if="model.status === 'deployed'"
                          class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"
                        ></span>
                        <span>{{ getStatusText(model.status) }}</span>
                      </span>
                      <span
                        v-if="model.isMain"
                        class="px-2 py-1 bg-amber-500/10 text-amber-400 text-xs font-bold rounded border border-amber-500/20"
                      >
                        主模型
                      </span>
                    </div>
                    <div class="flex items-center space-x-4 text-xs text-slate-400 mb-3">
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:label-outline"></span>
                        <span>版本 {{ model.version }}</span>
                      </span>
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:calendar-month-outline"></span>
                        <span>{{ model.trainedAt }}</span>
                      </span>
                      <span class="flex items-center space-x-1">
                        <span class="iconify" data-icon="material-symbols:person-outline"></span>
                        <span>{{ model.trainer }}</span>
                      </span>
                    </div>
                    <p class="text-sm text-slate-500">{{ model.description }}</p>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="flex items-center space-x-2">
                  <button
                    v-if="model.status !== 'deployed'"
                    class="px-4 py-2 bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 rounded-lg text-xs font-medium transition-all"
                    @click="handleDeploy(model)"
                  >
                    部署
                  </button>
                  <button
                    v-else
                    class="px-4 py-2 bg-amber-600/20 hover:bg-amber-600/30 text-amber-400 rounded-lg text-xs font-medium transition-all"
                    @click="handleUndeploy(model)"
                  >
                    下线
                  </button>
                  <button
                    class="p-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 transition-all"
                    @click="handleViewDetails(model)"
                  >
                    <span class="iconify" data-icon="material-symbols:info-outline"></span>
                  </button>
                </div>
              </div>

              <!-- 性能指标 -->
              <div class="grid grid-cols-6 gap-4 mb-6">
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">准确率</div>
                  <div class="text-xl font-bold text-white">{{ model.accuracy }}%</div>
                  <div class="mt-2 h-1 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      class="h-full bg-gradient-to-r from-emerald-500 to-blue-500"
                      :style="{ width: model.accuracy + '%' }"
                    ></div>
                  </div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">召回率</div>
                  <div class="text-xl font-bold text-emerald-400">{{ model.recall }}%</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">F1 分数</div>
                  <div class="text-xl font-bold text-blue-400">{{ model.f1Score }}</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">推理速度</div>
                  <div class="text-xl font-bold text-purple-400">{{ model.inferenceSpeed }}ms</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">模型大小</div>
                  <div class="text-xl font-bold text-slate-300">{{ model.size }}MB</div>
                </div>
                <div class="bg-slate-900/50 p-4 rounded-xl">
                  <div class="text-xs text-slate-500 mb-1">训练样本</div>
                  <div class="text-xl font-bold text-amber-400">{{ model.trainingDataSize }}K</div>
                </div>
              </div>

              <!-- 训练详情与版本 -->
              <div class="grid grid-cols-2 gap-4">
                <!-- 训练配置 -->
                <div class="bg-slate-900/30 border border-slate-800 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-3">
                    <h4 class="text-sm font-bold flex items-center">
                      <span class="iconify text-blue-500 mr-2" data-icon="material-symbols:settings-outline"></span>
                      训练配置
                    </h4>
                  </div>
                  <div class="space-y-2 text-xs">
                    <div class="flex justify-between">
                      <span class="text-slate-500">基础架构</span>
                      <span class="text-slate-300 font-mono">{{ model.architecture }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-slate-500">批次大小</span>
                      <span class="text-slate-300">{{ model.batchSize }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-slate-500">学习率</span>
                      <span class="text-slate-300">{{ model.learningRate }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-slate-500">训练轮数</span>
                      <span class="text-slate-300">{{ model.epochs }} epochs</span>
                    </div>
                  </div>
                </div>

                <!-- 版本历史 -->
                <div class="bg-slate-900/30 border border-slate-800 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-3">
                    <h4 class="text-sm font-bold flex items-center">
                      <span class="iconify text-purple-500 mr-2" data-icon="material-symbols:history"></span>
                      版本历史
                    </h4>
                    <button
                      class="text-xs text-indigo-400 hover:text-indigo-300"
                      @click="handleViewVersions(model)"
                    >
                      查看全部
                    </button>
                  </div>
                  <div class="space-y-2">
                    <div
                      v-for="version in (model.versions || []).slice(0, 3)"
                      :key="version.id"
                      class="flex items-center justify-between text-xs bg-slate-800/50 px-3 py-2 rounded-lg"
                    >
                      <div class="flex items-center space-x-2">
                        <span
                          :class="[
                            'w-2 h-2 rounded-full',
                            version.current ? 'bg-emerald-500' : 'bg-slate-500',
                          ]"
                        ></span>
                        <span class="text-slate-300 font-mono">{{ version.version }}</span>
                      </div>
                      <span class="text-slate-500">{{ version.accuracy }}%</span>
                    </div>
                    <div v-if="!(model.versions && model.versions.length)" class="text-xs text-slate-500 text-center py-2">
                      暂无版本信息
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 性能对比图表 -->
        <section class="bg-[#1e293b]/30 border border-slate-800 rounded-2xl p-6">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold flex items-center">
              <span class="w-1 h-5 bg-blue-500 rounded-full mr-3"></span>
              模型性能对比
            </h3>
            <div class="flex items-center space-x-2">
              <button
                v-for="metric in comparisonMetrics"
                :key="metric.id"
                :class="[
                  'px-3 py-1.5 rounded-lg text-xs font-medium transition-all',
                  activeMetric === metric.id
                    ? 'bg-indigo-600 text-white'
                    : 'bg-slate-800/50 text-slate-400 hover:bg-slate-800',
                ]"
                @click="activeMetric = metric.id"
              >
                {{ metric.name }}
              </button>
            </div>
          </div>
          <div ref="comparisonChart" class="w-full h-[300px]"></div>
        </section>
      </div>
    </main>
  </Layout>
</template>

<script>
import * as echarts from 'echarts';
import Layout from '@/components/Layout.vue';
import { getModels, getModelStatistics, deployModel, undeployModel } from '@/api/model';

export default {
  name: 'ModelManagement',
  components: {
    Layout,
  },
  data() {
    return {
      loading: false,
      showDebugInfo: false,
      debugInfo: '',
      statistics: {
        totalModels: 0,
        deployed: 0,
        avgAccuracy: 0,
        abTests: 0,
      },
      activeMetric: 'accuracy',
      comparisonMetrics: [
        { id: 'accuracy', name: '准确率' },
        { id: 'speed', name: '推理速度' },
        { id: 'f1', name: 'F1分数' },
      ],
      chartInstance: null,
      loading: false,
      models: [],
    };
  },
  async mounted() {
    window.addEventListener('resize', this.handleResize);
    // 先加载数据，数据加载完成后再初始化图表
    await this.loadModelData();
    this.initComparisonChart();
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },
  methods: {
    /**
     * 获取状态显示文本
     * 后端状态: inactive(未激活) / active(就绪) / deployed(部署中)
     */
    getStatusText(status) {
      const statusMap = {
        inactive: '未激活',
        active: '就绪',
        deployed: '已部署',
        // 兼容旧状态
        training: '训练中',
        testing: '测试中',
        archived: '已归档',
      };
      return statusMap[status] || '未知';
    },
    initComparisonChart() {
      if (!this.$refs.comparisonChart) {
        console.log('[模型管理] 图表容器不存在，跳过初始化');
        return;
      }

      try {
        if (this.chartInstance) {
          this.chartInstance.dispose();
        }
        this.chartInstance = echarts.init(this.$refs.comparisonChart);
        this.updateChart();
      } catch (error) {
        console.error('[模型管理] 图表初始化失败:', error);
      }
    },
    updateChart() {
      if (!this.chartInstance || !this.$refs.comparisonChart) {
        console.log('[模型管理] 图表实例不存在，跳过更新');
        return;
      }

      try {
        const models = this.models || [];
        if (models.length === 0) {
          console.log('[模型管理] 无模型数据，清空图表');
          this.chartInstance.clear();
          return;
        }

        const option = {
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow',
            },
          },
          legend: {
            data: models.slice(0, 4).map((m) => m.name || '未命名模型'),
            textStyle: {
              color: '#94a3b8',
              fontSize: 11,
            },
            bottom: 0,
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '3%',
            containLabel: true,
          },
          xAxis: {
            type: 'category',
            data: ['准确率', '召回率', 'F1分数'],
            axisLine: {
              lineStyle: {
                color: '#334155',
              },
            },
            axisLabel: {
              color: '#94a3b8',
              fontSize: 11,
            },
          },
          yAxis: {
            type: 'value',
            min: 0,
            max: 100,
            splitLine: {
              lineStyle: {
                color: '#1e293b',
              },
            },
            axisLabel: {
              color: '#94a3b8',
              fontSize: 11,
              formatter: '{value}%',
            },
          },
          series: models.slice(0, 4).map((model, index) => ({
            name: model.name || '未命名模型',
            type: 'bar',
            data: [
              model.accuracy || 0,
              model.recall || 0,
              parseFloat(((model.f1Score || 0) * 100).toFixed(1)),
            ],
            itemStyle: {
              color: ['#3b82f6', '#10b981', '#8b5cf6', '#f59e0b'][index % 4],
            },
          })),
        };
        this.chartInstance.setOption(option, true);
        console.log('[模型管理] 图表更新成功');
      } catch (error) {
        console.error('[模型管理] 图表更新失败:', error);
      }
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    },

    /**
     * 加载模型数据和统计数据
     */
    async loadModelData() {
      this.loading = true;
      console.log('[模型管理] ===== 开始加载数据 =====');

      try {
        // 先加载模型列表
        console.log('[模型管理] 1. 正在请求模型列表...');
        let modelsRes;
        try {
          modelsRes = await getModels();
          console.log('[模型管理] 1. 模型列表响应:', modelsRes);
        } catch (modelError) {
          console.error('[模型管理] 1. 模型列表请求失败:', modelError);
          throw new Error('获取模型列表失败: ' + modelError.message);
        }

        // 处理模型数据
        let models = modelsRes.data || [];
        if (!Array.isArray(models)) {
          models = models.models || models.records || models.data || models.list || [];
        }
        console.log('[模型管理] 提取的模型数量:', models.length);

        this.models = models.map((model) => {
          // 从 versions 数组中找到当前版本（isCurrent: true）
          const versions = model.versions || [];
          const currentVersion = versions.find(v => v.isCurrent === true) || versions[0] || {};

          // 提取当前版本的性能指标
          const accuracy = currentVersion.accuracy ?? 0;
          const recall = currentVersion.recall ?? 0;
          const f1Score = currentVersion.f1Score ?? 0;
          const inferenceSpeed = currentVersion.inferenceSpeed ?? 0;
          const modelSize = currentVersion.modelSizeMb ?? 0;
          const trainingDataSize = currentVersion.trainingDataSize ?? 0;

          // 训练配置字段可能在当前版本中，也可能在模型主对象中
          const architecture = currentVersion.architecture || model.architecture || 'YOLOv11';
          const batchSize = currentVersion.batchSize || model.batchSize || 16;
          const learningRate = currentVersion.learningRate || model.learningRate || '0.001';
          const epochs = currentVersion.epochs || model.epochs || 100;

          console.log('[模型管理] 处理模型:', model.name, {
            accuracy, recall, f1Score, inferenceSpeed, modelSize, trainingDataSize,
            currentVersionFound: !!currentVersion,
            hasVersions: versions.length > 0
          });

          return {
            ...model,
            // 从当前版本提取性能指标，确保是数字类型
            accuracy: parseFloat(accuracy) || 0,
            recall: parseFloat(recall) || 0,
            f1Score: parseFloat(f1Score) || 0,
            inferenceSpeed: parseFloat(inferenceSpeed) || 0,
            size: parseFloat(modelSize) || 0,
            trainingDataSize: parseInt(trainingDataSize) || 0,
            // 训练配置字段
            architecture: architecture,
            batchSize: batchSize,
            learningRate: learningRate,
            epochs: epochs,
            // 其他字段保持兼容
            version: model.version || model.currentVersion || 'v1.0.0',
            description: model.description || 'AI 缺陷检测模型',
            trainedAt: model.trainedAt || model.createdAt || '-',
            trainer: model.trainer || 'System',
            isMain: model.status === 'deployed' || model.isMain,
            // 保留原始 versions 数组用于版本历史显示
            versions: versions,
          };
        });

        // 再加载统计数据
        console.log('[模型管理] 2. 正在请求统计数据...');
        let statsRes;
        try {
          statsRes = await getModelStatistics();
          console.log('[模型管理] 2. 统计数据响应:', statsRes);
        } catch (statsError) {
          console.error('[模型管理] 2. 统计数据请求失败:', statsError);
          // 统计失败不影响模型列表显示
          statsRes = { data: {} };
        }

        // 处理统计数据
        let stats = statsRes.data || {};
        if (stats.data && typeof stats.data === 'object' && !Array.isArray(stats.data)) {
          stats = stats.data;
        }
        this.statistics = {
          totalModels: stats.totalModels ?? this.models.length,
          deployed: stats.deployed ?? this.models.filter((m) => m.status === 'deployed').length,
          avgAccuracy: stats.avgAccuracy ?? 0,
          abTests: stats.abTests ?? 0,
        };

        // 保存调试信息
        this.debugInfo = JSON.stringify({
          modelsRes,
          extractedModels: models,
          modelsLength: models.length,
          statsRes,
          extractedStats: stats,
        }, null, 2);

        console.log('[模型管理] ===== 数据加载完成 =====');

        // 更新图表
        this.$nextTick(() => {
          this.updateChart();
        });
      } catch (error) {
        console.error('[模型管理] 加载失败:', error);
        this.debugInfo = JSON.stringify({ error: error.message, stack: error.stack }, null, 2);
        alert('加载模型数据失败: ' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
        console.log('[模型管理] 加载状态已重置');
      }
    },

    handleRefresh() {
      this.loadModelData();
    },

    handleUploadModel() {
      console.log('上传新模型');
      alert('上传新模型功能开发中...');
    },

    /**
     * 部署模型
     */
    async handleDeploy(model) {
      if (!confirm(`确定要部署模型 ${model.name} 吗？部署后将自动下线当前部署的模型。`)) {
        return;
      }

      try {
        // 后端要求：
        // - 路径参数：模型ID (model.id)
        // - 请求体：{ versionId: xxx }
        const versionId = model.currentVersionId || model.id;
        console.log('[模型管理] 部署模型:', { modelId: model.id, versionId });
        await deployModel(model.id, versionId);
        alert(`模型 ${model.name} 部署成功`);
        // 重新加载数据以更新状态
        this.loadModelData();
      } catch (error) {
        console.error('部署模型失败', error);
        alert(error.message || '部署模型失败');
      }
    },

    /**
     * 下线模型
     */
    async handleUndeploy(model) {
      if (!confirm(`确定要下线模型 ${model.name} 吗？`)) {
        return;
      }

      try {
        await undeployModel(model.id);
        alert(`模型 ${model.name} 已下线`);
        // 重新加载数据以更新状态
        this.loadModelData();
      } catch (error) {
        console.error('下线模型失败', error);
        alert(error.message || '下线模型失败');
      }
    },

    handleViewDetails(model) {
      console.log('查看详情', model);
      alert(`查看模型 ${model.name} 详情功能开发中...`);
    },

    handleViewVersions(model) {
      console.log('查看版本历史', model);
      alert(`查看 ${model.name} 版本历史功能开发中...`);
    },

    /**
     * 测试 API 连接
     */
    async testApiConnection() {
      try {
        console.log('[模型管理] 测试 API 连接...');
        const response = await fetch('http://localhost:8000/v1/models', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });
        console.log('[模型管理] 测试响应状态:', response.status);
        const data = await response.json();
        console.log('[模型管理] 测试响应数据:', data);
        alert('API 测试成功！状态: ' + response.status + '\n数据: ' + JSON.stringify(data).slice(0, 200));
      } catch (error) {
        console.error('[模型管理] API 测试失败:', error);
        alert('API 测试失败: ' + error.message);
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
</style>
