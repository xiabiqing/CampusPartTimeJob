import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/complete-info',
    name: 'CompleteInfo',
    component: () => import('@/views/CompleteInfo.vue'),
    meta: {
      title: '完善信息',
      requiresAuth: true,
      skipInfoCheck: true // 跳过信息完整性检查
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: {
          title: '兼职列表',
          requiresAuth: true
        }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: '/role-application',
        name: 'RoleApplication',
        component: () => import('@/views/RoleApplication.vue'),
        meta: {
          title: '角色申请',
          requiresAuth: true
        }
      },
      {
        path: '/application-list',
        name: 'ApplicationList',
        component: () => import('@/views/ApplicationList.vue'),
        meta: {
          title: '申请列表',
          requiresAuth: true
        }
      },
      {
        path: '/my-application',
        name: 'MyApplication',
        component: () => import('@/views/MyApplication.vue'),
        meta: {
          title: '我的申请',
          requiresAuth: true
        }
      },
      {
        path: '/my-job-application',
        name: 'MyJobApplication',
        component: () => import('@/views/MyJobApplication.vue'),
        meta: {
          title: '我的兼职申请',
          requiresAuth: true
        }
      },
      {
        path: '/create-recruitment',
        name: 'CreateRecruitment',
        component: () => import('@/views/CreateRecruitment.vue'),
        meta: {
          title: '发布兼职',
          requiresAuth: true
        }
      },
      {
        path: '/my-recruitment',
        name: 'MyRecruitment',
        component: () => import('@/views/MyRecruitment.vue'),
        meta: {
          title: '我的招聘',
          requiresAuth: true
        }
      },
      {
        path: '/job-application-list',
        name: 'JobApplicationList',
        component: () => import('@/views/JobApplicationList.vue'),
        meta: {
          title: '处理申请',
          requiresAuth: true
        }
      },
      {
        path: '/chat-list',
        name: 'ChatList',
        component: () => import('@/views/ChatList.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true
        }
      },
      {
        path: '/chat/:userId/:role',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin-home',
    name: 'AdminHome',
    component: () => import('@/views/AdminHome.vue'),
    meta: {
      title: '管理员首页',
      requiresAdminAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 判断信息是否完整
function isInfoComplete(userDetail) {
  if (!userDetail) return false
  
  const role = userDetail.role !== undefined && userDetail.role !== null ? userDetail.role : 0
  
  // 学生角色必填字段：username(@NotBlank), age(@NotNull), gender(@NotNull), major(@NotBlank), email(@NotBlank)
  if (role === 0 || !role) {
    if (!userDetail.username || userDetail.username.trim() === '') return false
    if (userDetail.age === null || userDetail.age === undefined) return false
    if (userDetail.gender === null || userDetail.gender === undefined) return false
    if (!userDetail.major || userDetail.major.trim() === '') return false
    if (!userDetail.email || userDetail.email.trim() === '') return false
    return true
  }
  
  // 商家角色必填字段：username(@NotBlank), position(@NotBlank), location(@NotBlank), company(@NotBlank)
  if (role === 1) {
    if (!userDetail.username || userDetail.username.trim() === '') return false
    if (!userDetail.position || userDetail.position.trim() === '') return false
    if (!userDetail.location || userDetail.location.trim() === '') return false
    if (!userDetail.company || userDetail.company.trim() === '') return false
    return true
  }
  
  return true
}

// 检查用户信息是否完整
async function checkUserInfoComplete() {
  const userStore = useUserStore()
  try {
    // 如果userDetail已存在，直接检查
    if (userStore.userDetail) {
      return isInfoComplete(userStore.userDetail)
    }
    
    // 否则获取用户信息
    const { getCurrentUser } = await import('@/api/user')
    const res = await getCurrentUser()
    if (res && res.code === 200 && res.data) {
      userStore.setUserDetail(res.data)
      return isInfoComplete(res.data)
    }
    return false
  } catch (error) {
    console.error('检查用户信息失败', error)
    return false
  }
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const adminStore = useAdminStore()
  
  // 初始化用户信息
  if (!userStore.userInfo) {
    userStore.initUserInfo()
  }
  
  // 初始化管理员信息
  if (!adminStore.adminInfo) {
    adminStore.initAdminInfo()
  }

  // 管理员页面检查
  if (to.meta.requiresAdminAuth) {
    if (!adminStore.isLoggedIn()) {
      next('/login')
      return
    }
    if (to.meta.title) {
      document.title = to.meta.title
    }
    next()
    return
  }

  // 如果已登录，访问登录/注册页时跳转
  if ((to.path === '/login' || to.path === '/register')) {
    if (userStore.isLoggedIn()) {
      // 检查信息是否完整，不完整则跳转到完善信息页面
      const isComplete = await checkUserInfoComplete()
      if (!isComplete) {
        next('/complete-info')
        return
      }
      next('/home')
      return
    }
    if (adminStore.isLoggedIn()) {
      next('/admin-home')
      return
    }
  }

  // 如果需要登录但未登录，跳转到登录页
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    next('/login')
    return
  }

  // 检查用户信息完整性（跳过完善信息页面本身）
  if (to.meta.requiresAuth && !to.meta.skipInfoCheck && userStore.isLoggedIn()) {
    const isComplete = await checkUserInfoComplete()
    if (!isComplete) {
      next('/complete-info')
      return
    }
  }

  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  next()
})

export default router

