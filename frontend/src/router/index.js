import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'dish',
        name: 'Dish',
        component: () => import('@/views/admin/Dish.vue'),
        meta: { title: '菜品管理', icon: 'Food' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/admin/Category.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      },
      {
        path: 'table',
        name: 'Table',
        component: () => import('@/views/admin/Table.vue'),
        meta: { title: '桌台管理', icon: 'Grid' }
      },
      {
        path: 'area',
        name: 'Area',
        component: () => import('@/views/admin/Area.vue'),
        meta: { title: '区域管理', icon: 'Location' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/admin/Order.vue'),
        meta: { title: '订单管理', icon: 'Document' }
      },
      {
        path: 'member',
        name: 'Member',
        component: () => import('@/views/admin/Member.vue'),
        meta: { title: '会员管理', icon: 'User' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/admin/User.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/menu',
    name: 'Menu',
    component: () => import('@/views/customer/Menu.vue'),
    meta: { title: '点餐', public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：后端除 /auth/login 外均需登录，前端对应校验 token
router.beforeEach((to) => {
  const token = localStorage.getItem('token')

  if (to.meta.public) {
    // 已登录访问登录页则回首页
    if (token && to.path === '/login') return { path: '/' }
    return true
  }

  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 餐饮管理系统`
    : '餐饮管理系统'
})

export default router
