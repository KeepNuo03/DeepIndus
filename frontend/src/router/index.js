import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/pages/login.vue'
import DataDashboard from '@/pages/data_dashboard.vue'
import Query from '@/pages/query.vue'
import RealTimeDetect from '@/pages/real_time_detect.vue'
import DefectDetail from '@/pages/defect_detail.vue'
import ProductionLine from '@/pages/production_line.vue'
import ProductManagement from '@/pages/product_management.vue'
import ModelManagement from '@/pages/model_management.vue'
import UserManagement from '@/pages/user_management.vue'
import AgentChat from '@/pages/agent_chat.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',           // 根路径
      name: 'Login',
      component: Login      // 默认显示登录页面
    },
    {
      path: '/login',
      name: 'LoginPage',
      component: Login
    },
    {
      path: '/dashboard',
      name: 'DataDashboard',
      component: DataDashboard
    },
    {
      path: '/query',
      name: 'Query',
      component: Query
    },
    {
      path: '/detect',
      name: 'RealTimeDetect',
      component: RealTimeDetect
    },
    {
      path: '/defect/:id',
      name: 'DefectDetail',
      component: DefectDetail
    },
    {
      path: '/production',
      name: 'ProductionLine',
      component: ProductionLine
    },
    {
      path: '/product',
      name: 'ProductManagement',
      component: ProductManagement
    },
    {
      path: '/model',
      name: 'ModelManagement',
      component: ModelManagement
    },
    {
      path: '/users',
      name: 'UserManagement',
      component: UserManagement
    },
    {
      path: '/agent',
      name: 'AgentChat',
      component: AgentChat
    },
    {
      path: '/:pathMatch(.*)*',  // 404 兜底
      redirect: '/'              // 非法路径也跳转到登录页
    }
  ]
})

// 路由守卫 - Token 验证
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');

  // 白名单：无需登录即可访问
  const whiteList = ['/', '/login'];

  if (whiteList.includes(to.path)) {
    next();
  } else {
    // 需要登录的页面
    if (token) {
      next();
    } else {
      // 未登录，跳转到登录页
      alert('请先登录');
      next('/login');
    }
  }
});

export default router
