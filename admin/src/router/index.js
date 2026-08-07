import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/store/user'
import { APP_TITLE } from '@/config/app'
import { canAccessRoute, getRedirectPath, logAccessDenied } from '@/utils/routerGuard'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'DashboardOutlined' }
      },
      {
        path: 'merchant',
        name: 'Merchant',
        redirect: '/merchant/list',
        meta: { title: '商家管理', icon: 'ShopOutlined' },
        children: [
          {
            path: 'list',
            name: 'MerchantList',
            component: () => import('@/views/merchant/list.vue'),
            meta: { title: '商家列表' }
          },
          {
            path: 'detail/:id',
            name: 'MerchantDetail',
            component: () => import('@/views/merchant/detail.vue'),
            meta: { title: '商家详情管理', hidden: true }
          },
          {
            path: 'quota',
            name: 'MerchantQuota',
            component: () => import('@/views/merchant/quota.vue'),
            meta: { title: '额度管理' }
          }
        ]
      },
      {
        path: 'device',
        name: 'Device',
        redirect: '/device/list',
        meta: { title: '设备管理', icon: 'MobileOutlined' },
        children: [
          {
            path: 'list',
            name: 'DeviceList',
            component: () => import('@/views/device/list.vue'),
            meta: { title: '设备管理' }
          }
        ]
      },
      {
        path: 'ai',
        name: 'AI',
        redirect: '/ai/generate',
        meta: { title: 'AI创作', icon: 'RobotOutlined' },
        children: [
          {
            path: 'generate',
            name: 'AiGenerate',
            component: () => import('@/views/ai/generate.vue'),
            meta: { title: 'AI创作' }
          },
          {
            path: 'corpus',
            name: 'CorpusManage',
            component: () => import('@/views/ai/corpus.vue'),
            meta: { title: '语料管理' }
          },
          {
            path: 'config',
            name: 'AiConfig',
            component: () => import('@/views/ai/config.vue'),
            meta: { title: '创作配置' }
          },
          {
            path: 'merchant-config',
            name: 'MerchantConfig',
            component: () => import('@/views/ai/merchantConfig.vue'),
            meta: { title: '商家配置总览', roles: ['super_admin', 'admin'] }
          }
        ]
      },
      {
        path: 'marketing',
        name: 'Marketing',
        redirect: '/marketing/platforms',
        meta: { title: '营销管理', icon: 'ShareAltOutlined' },
        children: [
          {
            path: 'platforms',
            name: 'PlatformManage',
            component: () => import('@/views/marketing/platforms.vue'),
            meta: { title: '推广平台总配置', superAdminOnly: true }
          },
          {
            path: 'merchant-promotion',
            name: 'MerchantPromotion',
            component: () => import('@/views/marketing/merchantPromotion.vue'),
            meta: { title: '我的推广平台' }
          },
          {
            path: 'promotion-detail/:id',
            name: 'PromotionDetail',
            component: () => import('@/views/marketing/promotionDetail.vue'),
            meta: { title: '推广配置详情', hidden: true }
          },
          {
            path: 'coupons',
            name: 'CouponManage',
            component: () => import('@/views/marketing/coupons.vue'),
            meta: { title: '优惠券' }
          },
          {
            path: 'plans',
            name: 'PlanManage',
            component: () => import('@/views/marketing/plans.vue'),
            meta: { title: '套餐管理' }
          },
          {
            path: 'orders',
            name: 'OrderManage',
            component: () => import('@/views/marketing/orders.vue'),
            meta: { title: '订单管理' }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/settings',
        meta: { title: '系统设置', icon: 'SettingOutlined' },
        children: [
          {
            path: 'settings',
            name: 'SystemSettings',
            component: () => import('@/views/system/settings.vue'),
            meta: { title: '系统配置' }
          },
          {
            path: 'profile',
            name: 'Profile',
            component: () => import('@/views/system/profile.vue'),
            meta: { title: '个人中心' }
          },
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/user.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'role',
            name: 'RoleManage',
            component: () => import('@/views/system/role.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'merchant-access',
            name: 'MerchantAccess',
            component: () => import('@/views/system/merchantAccess.vue'),
            meta: { title: '商家权限配置', superAdminOnly: true }
          },
          {
            path: 'my-quota',
            name: 'MyQuota',
            component: () => import('@/views/system/myQuota.vue'),
            meta: { title: '当前额度', roles: ['merchant'] }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()

  // Check if route is accessible
  const accessResult = canAccessRoute(to, userStore)

  if (accessResult.allowed) {
    next()
  } else {
    // Log access denied event
    logAccessDenied({
      reason: accessResult.reason,
      routeName: to.name,
      role: userStore.userInfo?.role,
      userId: userStore.userInfo?.id
    })

    // Redirect to appropriate page
    const redirectPath = getRedirectPath(accessResult.reason)

    if (accessResult.reason === 'NOT_LOGGED_IN') {
      // Redirect to login with original URL as redirect target
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      // Redirect to dashboard or accessible page
      next({ path: redirectPath })
    }
  }
})

router.afterEach((to) => {
  NProgress.done()
  document.title = to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE
})

export default router
