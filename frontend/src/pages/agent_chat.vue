<template>
  <Layout>
    <main class="flex-1 flex min-w-0 bg-slate-950 overflow-hidden">
      <aside class="w-[360px] border-r border-slate-800 bg-slate-900/70 flex flex-col">
        <div class="p-5 border-b border-slate-800">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h2 class="text-lg font-semibold text-slate-100">质检AI助理</h2>
              <p class="text-xs text-slate-400 mt-1">你的专属质检工作助理</p>
            </div>
            <button
              class="h-10 px-4 bg-indigo-600 hover:bg-indigo-500 text-white text-sm rounded-xl transition-all font-medium"
              @click="createNewSession"
            >
              新对话
            </button>
          </div>
          <button
            class="w-full h-10 px-3 bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm rounded-xl transition-all border border-slate-700"
            @click="loadSessions"
          >
            刷新会话列表
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-3 space-y-2 hide-scrollbar">
          <div
            v-for="item in sessions"
            :key="item.sessionId"
            :class="[
              'rounded-xl p-4 border cursor-pointer transition-all',
              activeSessionId === item.sessionId
                ? 'bg-indigo-500/15 border-indigo-400/50'
                : 'bg-slate-900 border-slate-800 hover:border-slate-600',
            ]"
            @click="handleSelectSession(item.sessionId)"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <div class="text-sm font-medium text-slate-100 truncate">{{ item.title || '未命名会话' }}</div>
                <div class="text-xs text-slate-400 mt-1">{{ formatTime(item.lastMessageAt) }}</div>
              </div>
              <button
                class="text-xs text-rose-300 hover:text-rose-200 px-2 py-1 rounded-lg hover:bg-rose-500/10"
                @click.stop="handleArchiveSession(item.sessionId)"
              >
                删除
              </button>
            </div>
          </div>
          <div v-if="!sessions.length" class="text-sm text-slate-500 p-3">暂无会话</div>
        </div>
      </aside>

      <section class="flex-1 flex flex-col min-w-0 bg-slate-950">
        <header class="h-20 border-b border-slate-800 px-8 flex items-center justify-between bg-slate-900/40">
          <div>
            <div class="text-xl font-semibold text-slate-100">质检AI助理</div>
            <div class="text-sm text-slate-400 mt-1">
              {{ streamStatusText }}
              <span v-if="traceId" class="ml-2 text-xs text-slate-500">traceId: {{ traceId }}</span>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button
              v-if="isStreaming"
              class="h-11 px-4 text-sm rounded-xl bg-rose-600/20 text-rose-300 hover:bg-rose-600/30 border border-rose-500/30"
              @click="stopStreaming"
            >
              停止生成
            </button>
            <button
              class="h-11 px-4 text-sm rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700"
              @click="goBack"
            >
              返回
            </button>
          </div>
        </header>

        <div ref="messageListEl" class="flex-1 overflow-y-auto px-8 py-6 hide-scrollbar">
          <div class="max-w-4xl mx-auto space-y-5">
            <template v-if="messages.length">
              <div
                v-for="msg in messages"
                :key="msg.id"
                :class="[
                  'flex items-start gap-3',
                  msg.role === 'user' ? 'justify-end' : 'justify-start',
                ]"
              >
                <div
                  v-if="msg.role !== 'user'"
                  class="w-9 h-9 rounded-full bg-indigo-600/20 border border-indigo-500/40 flex items-center justify-center text-xs font-semibold text-indigo-200 shrink-0"
                >
                  AI
                </div>

                <div
                  :class="[
                    'max-w-[80%] rounded-2xl px-5 py-4 border shadow-sm',
                    msg.role === 'user'
                      ? 'bg-indigo-600 text-white border-indigo-500'
                      : 'bg-slate-900 text-slate-100 border-slate-700',
                  ]"
                >
                  <div class="text-[11px] opacity-80 mb-2">
                    {{ msg.role === 'user' ? '你' : '质检AI助理' }}
                  </div>
                  <div class="text-base leading-7 whitespace-pre-wrap">{{ msg.content || (isStreaming ? '正在思考中...' : '') }}</div>
                </div>

                <div
                  v-if="msg.role === 'user'"
                  class="w-9 h-9 rounded-full bg-slate-700/80 border border-slate-600 flex items-center justify-center text-xs font-semibold text-slate-200 shrink-0"
                >
                  我
                </div>
              </div>
            </template>

            <div v-else class="pt-16 text-center">
              <div class="text-2xl font-semibold text-slate-200 mb-3">今天想问点什么？</div>
              <div class="text-base text-slate-400">
                例如：帮我分析今日告警趋势，或总结最近上传失败的共性问题。
              </div>
            </div>
          </div>
        </div>

        <footer class="border-t border-slate-800 px-8 py-5 bg-slate-900/40">
          <div class="max-w-4xl mx-auto">
            <div class="text-sm text-slate-400 mb-3">{{ contextHintText }}</div>
            <div class="bg-slate-900 border border-slate-700 rounded-2xl p-3">
              <textarea
                v-model="inputText"
                class="w-full h-28 resize-none bg-transparent px-3 py-2 text-base text-slate-100 outline-none"
                placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
                @keydown.enter.exact.prevent="handleSend"
                @keydown.enter.shift.exact.stop
              />
              <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-800">
                <button
                  class="h-11 px-4 rounded-xl text-sm font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:opacity-50"
                  :disabled="isStreaming"
                  @click="sendSyncOnce"
                >
                  同步请求
                </button>
                <button
                  class="h-11 px-5 rounded-xl text-sm font-semibold bg-indigo-600 hover:bg-indigo-500 text-white disabled:opacity-50"
                  :disabled="sendingDisabled"
                  @click="handleSend"
                >
                  {{ isStreaming ? '生成中...' : '发送消息' }}
                </button>
              </div>
            </div>
          </div>
        </footer>
      </section>
    </main>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue';
import {
  buildAgentContext,
  chatAgent,
  streamAgentChat,
  getAgentSessions,
  getAgentSessionDetail,
  archiveAgentSession,
} from '@/api/agent';

export default {
  name: 'AgentChat',
  components: {
    Layout,
  },
  data() {
    return {
      loading: false,
      inputText: '',
      messages: [],
      sessions: [],
      activeSessionId: '',
      streamStatusText: '就绪',
      traceId: '',
      isStreaming: false,
      streamAbortController: null,
      lastAssistantMessageId: '',
    };
  },
  computed: {
    sendingDisabled() {
      return this.isStreaming || !this.inputText.trim();
    },
    currentScene() {
      return this.$route.query.scene || 'web_general';
    },
    currentPageParams() {
      const from = this.$route.query.from || '';
      const page = this.$route.query.page || '';
      const params = {};
      if (from) params.from = from;
      if (page) params.page = page;
      return params;
    },
    contextHintText() {
      const labelMap = {
        web_general: '通用对话',
        web_page: '页面辅助',
        query: '记录查询辅助',
        defect_detail: '缺陷详情辅助',
        dashboard: '看板分析辅助',
      };
      const sceneLabel = labelMap[this.currentScene] || '通用对话';
      const page = this.currentPageParams.page || this.currentPageParams.from || '';
      if (!page) {
        return `当前模式：${sceneLabel}`;
      }
      return `当前模式：${sceneLabel}（来源：${page}）`;
    },
  },
  mounted() {
    this.loadSessions();
  },
  methods: {
    async loadSessions() {
      try {
        const res = await getAgentSessions({ page: 1, pageSize: 20 });
        const data = res.data || {};
        this.sessions = data.items || [];
      } catch (error) {
        console.error('加载会话失败', error);
        alert(error.message || '加载会话失败');
      }
    },
    async handleSelectSession(sessionId) {
      this.activeSessionId = sessionId;
      this.traceId = '';
      try {
        const res = await getAgentSessionDetail(sessionId);
        const data = res.data || {};
        const list = data.messages || [];
        this.messages = list.map((item, idx) => ({
          id: `${sessionId}_${idx}`,
          role: item.role === 'assistant' ? 'assistant' : 'user',
          content: item.content || '',
          timestamp: item.timestamp || '',
        }));
        this.scrollToBottom();
      } catch (error) {
        console.error('加载会话详情失败', error);
        alert(error.message || '加载会话详情失败');
      }
    },
    async handleArchiveSession(sessionId) {
      if (!confirm('确定删除该会话吗？')) return;
      try {
        await archiveAgentSession(sessionId);
        if (this.activeSessionId === sessionId) {
          this.activeSessionId = '';
          this.messages = [];
        }
        this.loadSessions();
      } catch (error) {
        console.error('删除会话失败', error);
        alert(error.message || '删除会话失败');
      }
    },
    createNewSession() {
      this.activeSessionId = '';
      this.traceId = '';
      this.messages = [];
      this.streamStatusText = '已新建会话';
    },
    appendUserMessage(text) {
      this.messages.push({
        id: `u_${Date.now()}`,
        role: 'user',
        content: text,
        timestamp: new Date().toISOString(),
      });
      this.scrollToBottom();
    },
    createAssistantPlaceholder() {
      const id = `a_${Date.now()}`;
      this.lastAssistantMessageId = id;
      this.messages.push({
        id,
        role: 'assistant',
        content: '',
        timestamp: new Date().toISOString(),
      });
      this.scrollToBottom();
    },
    appendAssistantChunk(chunkText) {
      const msg = this.messages.find((item) => item.id === this.lastAssistantMessageId);
      if (!msg) return;
      msg.content += chunkText;
      this.$nextTick(() => this.scrollToBottom());
    },
    buildRequestPayload(userText) {
      return {
        sessionId: this.activeSessionId || null,
        message: {
          role: 'user',
          content: userText,
        },
        stream: true,
        context: buildAgentContext(this.currentScene, this.currentPageParams),
      };
    },
    async handleSend() {
      const text = this.inputText.trim();
      if (!text || this.isStreaming) return;

      this.appendUserMessage(text);
      this.createAssistantPlaceholder();
      this.inputText = '';
      this.isStreaming = true;
      this.streamStatusText = '生成中...';
      this.traceId = '';

      const payload = this.buildRequestPayload(text);
      const controller = new AbortController();
      this.streamAbortController = controller;

      try {
        await streamAgentChat(
          payload,
          {
            onOpen: (meta) => {
              if (meta.sessionId) this.activeSessionId = meta.sessionId;
              if (meta.traceId) this.traceId = meta.traceId;
              this.streamStatusText = '流式连接已建立';
            },
            onTool: () => {
              this.streamStatusText = 'Agent 正在调用工具...';
            },
            onChunk: (chunkText) => {
              this.appendAssistantChunk(chunkText);
              this.streamStatusText = '生成中...';
            },
            onDone: () => {
              this.streamStatusText = '完成';
            },
            onError: (errData) => {
              const code = errData?.code ? `(${errData.code})` : '';
              this.streamStatusText = `错误 ${code}`.trim();
            },
          },
          { signal: controller.signal, retry: 1 },
        );
        await this.loadSessions();
      } catch (error) {
        console.error('流式请求失败', error);
        this.streamStatusText = '请求失败';
        if (this.lastAssistantMessageId) {
          const msg = this.messages.find((item) => item.id === this.lastAssistantMessageId);
          if (msg && !msg.content) {
            msg.content = `请求失败：${error.message || '未知错误'}`;
          }
        }
      } finally {
        this.isStreaming = false;
        this.streamAbortController = null;
        this.scrollToBottom();
      }
    },
    async sendSyncOnce() {
      const text = this.inputText.trim();
      if (!text || this.isStreaming) return;
      this.appendUserMessage(text);
      this.inputText = '';
      this.streamStatusText = '同步请求中...';

      try {
        const res = await chatAgent({
          sessionId: this.activeSessionId || null,
          message: { role: 'user', content: text },
          stream: false,
          context: buildAgentContext(this.currentScene, this.currentPageParams),
        });
        const data = res.data || {};
        if (data.sessionId) this.activeSessionId = data.sessionId;
        if (data.traceId) this.traceId = data.traceId;
        this.messages.push({
          id: `a_${Date.now()}`,
          role: 'assistant',
          content: data.answer || '(空响应)',
          timestamp: new Date().toISOString(),
        });
        this.streamStatusText = '同步完成';
        await this.loadSessions();
        this.scrollToBottom();
      } catch (error) {
        console.error('同步请求失败', error);
        this.streamStatusText = '同步失败';
        alert(error.message || '同步请求失败');
      }
    },
    stopStreaming() {
      if (!this.streamAbortController) return;
      this.streamAbortController.abort();
      this.isStreaming = false;
      this.streamStatusText = '已手动停止';
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.messageListEl;
        if (el) {
          el.scrollTop = el.scrollHeight;
        }
      });
    },
    formatTime(value) {
      if (!value) return '-';
      const dt = new Date(value);
      if (Number.isNaN(dt.getTime())) return value;
      const yyyy = dt.getFullYear();
      const mm = String(dt.getMonth() + 1).padStart(2, '0');
      const dd = String(dt.getDate()).padStart(2, '0');
      const hh = String(dt.getHours()).padStart(2, '0');
      const min = String(dt.getMinutes()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
    },
    goBack() {
      const from = this.$route.query.from;
      if (from) {
        this.$router.push(from);
        return;
      }
      this.$router.back();
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
