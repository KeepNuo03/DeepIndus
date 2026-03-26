<template>
  <div class="bg-[#0f172a] font-sans text-slate-200 min-h-screen flex items-center justify-center p-4">
    <!-- Loading 遮罩 -->
    <div
      v-if="loading"
      class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50"
    >
      <div class="bg-slate-800 p-6 rounded-xl shadow-2xl flex items-center space-x-3">
        <div class="w-6 h-6 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
        <span class="text-white font-medium">{{ loadingText }}</span>
      </div>
    </div>
    <!-- 背景装饰：工业感网格与光晕 -->
    <div class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
      <div
        class="absolute inset-0 opacity-10"
        style="background-image: radial-gradient(#334155 1px, transparent 1px); background-size: 30px 30px;"
      ></div>
      <div
        class="absolute -top-[10%] -left-[10%] w-[40%] h-[40%] bg-blue-500/10 blur-[120px] rounded-full"
      ></div>
      <div
        class="absolute -bottom-[10%] -right-[10%] w-[40%] h-[40%] bg-indigo-500/10 blur-[120px] rounded-full"
      ></div>
    </div>

    <!-- 主容器 -->
    <div
      class="relative z-10 w-full max-w-[1100px] flex flex-col md:flex-row bg-[#1e293b]/50 backdrop-blur-xl border border-slate-700/50 rounded-3xl shadow-2xl overflow-hidden animate-slide-in"
    >
      <!-- 左侧：品牌展示区 -->
      <div
        class="w-full md:w-1/2 p-8 md:p-12 flex flex-col justify-between bg-gradient-to-br from-slate-800 to-slate-900 border-r border-slate-700/50"
      >
        <div>
          <div class="flex items-center space-x-3 mb-8">
            <!-- 工业检测系统 Logo -->
            <div
              class="w-12 h-12 rounded-xl flex items-center justify-center shadow-lg shadow-blue-500/20 relative overflow-hidden bg-gradient-to-br from-blue-600 to-indigo-700"
            >
              <!-- 背景网格 -->
              <div class="absolute inset-0 opacity-20" style="background-image: linear-gradient(0deg, transparent 24%, rgba(255, 255, 255, .1) 25%, rgba(255, 255, 255, .1) 26%, transparent 27%, transparent 74%, rgba(255, 255, 255, .1) 75%, rgba(255, 255, 255, .1) 76%, transparent 77%, transparent), linear-gradient(90deg, transparent 24%, rgba(255, 255, 255, .1) 25%, rgba(255, 255, 255, .1) 26%, transparent 27%, transparent 74%, rgba(255, 255, 255, .1) 75%, rgba(255, 255, 255, .1) 76%, transparent 77%, transparent); background-size: 8px 8px;"></div>
              <!-- Logo 图形 -->
              <div class="relative z-10">
                <div class="w-7 h-7 border-2 border-white/90 rounded-md relative">
                  <div class="absolute inset-1 border border-white/60 rounded-sm"></div>
                  <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-2 h-2 bg-white rounded-full"></div>
                  <div class="absolute top-0 right-0 w-1.5 h-1.5 bg-blue-300 rounded-full"></div>
                </div>
              </div>
            </div>
            <span class="text-2xl font-bold tracking-tight text-white italic"
              >DEEP<span class="text-blue-500">INDUS</span></span
            >
          </div>
          <h1 class="text-4xl md:text-5xl font-black text-white leading-tight mb-6">
            数字化工业<br />
            <span
              class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-400"
              >智能质检引擎</span
            >
          </h1>
          <p class="text-slate-400 text-lg max-w-md leading-relaxed">
            连接生产一线与决策终端，为您的工业生态提供高强度的数据支撑与安全保障。
          </p>
        </div>

        <div class="mt-12 space-y-4">
          <div class="flex items-center space-x-4 text-sm text-slate-400">
            <span class="iconify text-blue-500 text-xl" data-icon="solar:shield-check-bold"></span>
            <span>金融级数据加密协议</span>
          </div>
          <div class="flex items-center space-x-4 text-sm text-slate-400">
            <span class="iconify text-blue-500 text-xl" data-icon="solar:global-bold"></span>
            <span>2026年全球工业互联标准</span>
          </div>
        </div>
      </div>

      <!-- 右侧：表单区 -->
      <div class="w-full md:w-1/2 p-8 md:p-12 relative overflow-hidden flex flex-col justify-center">
        <!-- 登录表单 -->
        <Transition
          mode="out-in"
          enter-active-class="form-transition"
          leave-active-class="form-transition"
          enter-from-class="opacity-0 translate-x-12"
          enter-to-class="opacity-100 translate-x-0"
          leave-from-class="opacity-100 translate-x-0"
          leave-to-class="opacity-0 -translate-x-12"
        >
          <div v-if="activeForm === 'login'" id="loginForm" class="opacity-100 translate-x-0">
            <div class="mb-8">
              <h2 class="text-2xl font-bold text-white mb-2">欢迎回来</h2>
              <p class="text-slate-400">请输入您的凭据以访问控制台</p>
            </div>

            <form class="space-y-5" @submit.prevent="handleLogin">
              <div class="space-y-2">
                <label class="text-sm font-medium text-slate-300 ml-1">用户名或邮箱</label>
                <div class="relative group">
                  <span
                    class="iconify absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-blue-500 transition-colors"
                    data-icon="solar:user-bold"
                  ></span>
                  <input
                    v-model="loginForm.username"
                    type="text"
                    placeholder="admin@industry.com"
                    class="w-full bg-slate-900/50 border border-slate-700 rounded-xl py-3.5 pl-12 pr-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all"
                  />
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-sm font-medium text-slate-300 ml-1">安全密码</label>
                <div class="relative group">
                  <span
                    class="iconify absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-blue-500 transition-colors"
                    data-icon="solar:lock-password-bold"
                  ></span>
                  <input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="••••••••"
                    class="w-full bg-slate-900/50 border border-slate-700 rounded-xl py-3.5 pl-12 pr-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all"
                  />
                </div>
              </div>

              <div class="flex items-center justify-between text-sm">
                <label class="flex items-center space-x-2 cursor-pointer group">
                  <input
                    v-model="loginForm.remember"
                    type="checkbox"
                    class="w-4 h-4 rounded border-slate-700 bg-slate-800 text-blue-600 focus:ring-blue-500/20"
                  />
                  <span class="text-slate-400 group-hover:text-slate-200 transition-colors"
                    >记住我的账户</span
                  >
                </label>
                <a href="#" class="text-blue-400 hover:text-blue-300 font-medium">忘记密码？</a>
              </div>

              <button
                type="submit"
                :disabled="loading"
                :class="[
                  'w-full font-bold py-4 rounded-xl shadow-lg transition-all transform',
                  loading 
                    ? 'bg-blue-600/50 cursor-not-allowed' 
                    : 'bg-blue-600 hover:bg-blue-500 shadow-blue-600/20 active:scale-[0.98]'
                ]"
                class="text-white"
              >
                {{ loading ? '登录中...' : '立即登录' }}
              </button>
            </form>

            <p class="mt-8 text-center text-slate-400 text-sm">
              还没有账号？
              <button
                type="button"
                class="text-blue-400 hover:underline font-bold"
                @click="toggleForm('register')"
              >
                申请注册
              </button>
            </p>
          </div>
        </Transition>

        <!-- 注册表单 -->
        <Transition
          mode="out-in"
          enter-active-class="form-transition"
          leave-active-class="form-transition"
          enter-from-class="opacity-0 translate-x-12"
          enter-to-class="opacity-100 translate-x-0"
          leave-from-class="opacity-100 translate-x-0"
          leave-to-class="opacity-0 -translate-x-12"
        >
          <div
            v-if="activeForm === 'register'"
            id="registerForm"
            class="absolute inset-x-8 md:inset-x-12 top-1/2 -translate-y-1/2"
          >
            <div class="mb-6">
              <h2 class="text-2xl font-bold text-white mb-2">加入工业全联接</h2>
              <p class="text-slate-400">创建您的管理员访问账号</p>
            </div>

            <form class="space-y-4" @submit.prevent="handleRegister">
              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-xs font-medium text-slate-300">用户名</label>
                  <input
                    v-model="registerForm.username"
                    type="text"
                    placeholder="User"
                    class="w-full bg-slate-900/50 border border-slate-700 rounded-lg py-3 px-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 transition-all"
                  />
                </div>
                <div class="space-y-2">
                  <label class="text-xs font-medium text-slate-300">企业代码</label>
                  <input
                    v-model="registerForm.companyCode"
                    type="text"
                    placeholder="ID-2026"
                    class="w-full bg-slate-900/50 border border-slate-700 rounded-lg py-3 px-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 transition-all"
                  />
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-xs font-medium text-slate-300">电子邮箱</label>
                <input
                  v-model="registerForm.email"
                  type="email"
                  placeholder="email@enterprise.com"
                  class="w-full bg-slate-900/50 border border-slate-700 rounded-lg py-3 px-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 transition-all"
                />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-xs font-medium text-slate-300">设置密码</label>
                  <input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="••••••••"
                    class="w-full bg-slate-900/50 border border-slate-700 rounded-lg py-3 px-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 transition-all"
                  />
                </div>
                <div class="space-y-2">
                  <label class="text-xs font-medium text-slate-300">确认密码</label>
                  <input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="••••••••"
                    class="w-full bg-slate-900/50 border border-slate-700 rounded-lg py-3 px-4 text-white placeholder:text-slate-600 focus:outline-none focus:border-blue-500 transition-all"
                  />
                </div>
              </div>

              <div class="flex items-start space-x-2 py-2">
                <input
                  id="terms"
                  v-model="registerForm.agreeTerms"
                  type="checkbox"
                  class="mt-1 w-4 h-4 rounded border-slate-700 bg-slate-800 text-blue-600 focus:ring-blue-500/20"
                />
                <label
                  for="terms"
                  class="text-xs text-slate-400 leading-relaxed cursor-pointer"
                >
                  我已阅读并同意
                  <a href="#" class="text-blue-400">《核心引擎服务协议》</a>
                  与
                  <a href="#" class="text-blue-400">《数据隐私条款》</a>
                </label>
              </div>

              <button
                type="submit"
                :disabled="loading"
                :class="[
                  'w-full font-bold py-3.5 rounded-xl shadow-lg transition-all transform',
                  loading 
                    ? 'bg-indigo-600/50 cursor-not-allowed' 
                    : 'bg-indigo-600 hover:bg-indigo-500 shadow-indigo-600/20 active:scale-[0.98]'
                ]"
                class="text-white"
              >
                {{ loading ? '提交中...' : '提交注册申请' }}
              </button>
            </form>

            <p class="mt-6 text-center text-slate-400 text-sm">
              已有通行账号？
              <button
                type="button"
                class="text-blue-400 hover:underline font-bold"
                @click="toggleForm('login')"
              >
                返回登录
              </button>
            </p>
          </div>
        </Transition>
      </div>
    </div>

    <footer class="fixed bottom-6 z-10 text-slate-500 text-xs tracking-widest uppercase">
      © 2026 DEEPINDUS TECHNOLOGY INC. ALL RIGHTS RESERVED.
    </footer>
  </div>
</template>

<script>
import { login, register } from '@/api/auth';

export default {
  name: 'LoginPage',
  data() {
    return {
      activeForm: 'login',
      loading: false,
      loadingText: '处理中...',
      // 登录表单数据
      loginForm: {
        username: '',
        password: '',
        remember: false
      },
      // 注册表单数据
      registerForm: {
        username: '',
        email: '',
        companyCode: '',
        password: '',
        confirmPassword: '',
        agreeTerms: false
      }
    };
  },
  methods: {
    toggleForm(type) {
      this.activeForm = type;
    },
    
    async handleLogin() {
      // 表单验证
      if (!this.loginForm.username || !this.loginForm.password) {
        alert('请输入用户名和密码');
        return;
      }

      this.loading = true;
      this.loadingText = '正在登录...';
      try {
        // 调用后端登录接口
        const res = await login({
          username: this.loginForm.username,
          password: this.loginForm.password,
          remember: this.loginForm.remember
        });

        // 登录成功
        localStorage.setItem("token", res.data.token);
        localStorage.setItem("user", JSON.stringify(res.data.user));
        
        // 跳转到实时检测页面
        this.$router.push("/detect");
      } catch (error) {
        console.error('登录失败', error);
        alert(error.message || '登录失败，请检查用户名和密码');
      } finally {
        this.loading = false;
      }
    },
    
    async handleRegister() {
      // 表单验证
      if (!this.registerForm.username || !this.registerForm.email || 
          !this.registerForm.password) {
        alert('请填写所有必填项');
        return;
      }

      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('两次密码输入不一致');
        return;
      }

      if (!this.registerForm.agreeTerms) {
        alert('请先阅读并同意服务协议');
        return;
      }

      this.loading = true;
      this.loadingText = '正在提交注册申请...';
      try {
        // 调用后端注册接口
        await register({
          username: this.registerForm.username,
          email: this.registerForm.email,
          companyCode: this.registerForm.companyCode,
          password: this.registerForm.password,
          confirmPassword: this.registerForm.confirmPassword,
          agreeTerms: this.registerForm.agreeTerms
        });

        // 注册成功
        alert('注册成功！请登录');
        this.activeForm = 'login';
        // 清空注册表单
        this.registerForm = {
          username: '',
          email: '',
          companyCode: '',
          password: '',
          confirmPassword: '',
          agreeTerms: false
        };
      } catch (error) {
        console.error('注册失败', error);
        alert(error.message || '注册失败，请稍后重试');
      } finally {
        this.loading = false;
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

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-in {
  animation: slideIn 0.5s ease-out forwards;
}

.form-transition {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>