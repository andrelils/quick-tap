<template>
  <a-layout class="basic-layout">
    <a-layout-sider
      v-model:collapsed="appStore.collapsed"
      :trigger="null"
      collapsible
      width="240"
      class="layout-sider"
    >
      <div class="sider-logo">
        <div class="logo-icon">
          <LikeOutlined />
        </div>
        <span v-if="!appStore.collapsed" class="logo-text">{{ APP_NAME }}</span>
      </div>
      
      <a-menu
        theme="dark"
        mode="inline"
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        @click="handleMenuClick"
        @openChange="handleOpenChange"
      >
        <a-menu-item key="/dashboard">
          <template #icon>
            <PieChartOutlined />
          </template>
          <span>仪表盘</span>
        </a-menu-item>

        <a-sub-menu v-if="canAccess('merchant') && !isMerchantView" key="merchant">
          <template #icon>
            <ShopOutlined />
          </template>
          <template #title>商家管理</template>
          <a-menu-item key="/merchant/list">商家列表</a-menu-item>
          <a-menu-item key="/merchant/quota">额度管理</a-menu-item>
        </a-sub-menu>

        <a-menu-item v-if="showMyMerchantMenu" key="/merchant/my">
          <template #icon>
            <ShopOutlined />
          </template>
          <span>我的商家</span>
        </a-menu-item>

        <a-menu-item v-if="canAccess('device')" key="/device/list">
          <template #icon>
            <PhoneOutlined />
          </template>
          <span>设备管理</span>
        </a-menu-item>

        <a-sub-menu v-if="canAccess('ai')" key="ai">
          <template #icon>
            <BulbOutlined />
          </template>
          <template #title>AI创作</template>
          <a-menu-item key="/ai/generate">AI创作</a-menu-item>
          <a-menu-item key="/ai/corpus">语料管理</a-menu-item>
          <a-menu-item key="/ai/config">创作配置</a-menu-item>
          <a-menu-item v-if="userStore.isAdmin && !isMerchantView" key="/ai/merchant-config">商家配置总览</a-menu-item>
        </a-sub-menu>

        <a-sub-menu v-if="showMarketingMenu" key="marketing">
          <template #icon>
            <ShareAltOutlined />
          </template>
          <template #title>营销管理</template>
          <a-menu-item v-if="userStore.isSuperAdmin && !isMerchantView" key="/marketing/platforms">推广平台总配置</a-menu-item>
          <a-menu-item key="/marketing/merchant-promotion">我的推广平台</a-menu-item>
          <a-menu-item v-if="userStore.isSuperAdmin && !isMerchantView" key="/marketing/coupons">优惠券</a-menu-item>
          <a-menu-item v-if="userStore.isSuperAdmin && !isMerchantView" key="/marketing/plans">套餐管理</a-menu-item>
          <a-menu-item v-if="userStore.isSuperAdmin && !isMerchantView" key="/marketing/orders">订单管理</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="system">
          <template #icon>
            <SettingOutlined />
          </template>
          <template #title>系统设置</template>
          <a-menu-item v-if="(canAccess('settings') || canAccess('system')) && !isMerchantView" key="/system/settings">系统配置</a-menu-item>
          <a-menu-item key="/system/profile">个人中心</a-menu-item>
          <a-menu-item v-if="(canAccess('user') || canAccess('system')) && !isMerchantView" key="/system/user">用户管理</a-menu-item>
          <a-menu-item v-if="(canAccess('role') || canAccess('system')) && !isMerchantView" key="/system/role">角色管理</a-menu-item>
          <a-menu-item v-if="userStore.isSuperAdmin && !isMerchantView" key="/system/merchant-access">商家权限配置</a-menu-item>
          <a-menu-item v-if="userStore.isMerchant || isMerchantView" key="/system/my-quota">当前额度</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    
    <a-layout>
      <a-layout-header class="layout-header">
        <div class="header-left">
          <span class="trigger" @click="appStore.toggleCollapsed">
            <MenuUnfoldOutlined v-if="appStore.collapsed" />
            <MenuFoldOutlined v-else />
          </span>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <div v-if="userStore.isAdmin" class="merchant-select-wrapper">
            <ShopOutlined class="merchant-select-icon" />
            <a-select
              v-model:value="selectedMerchantId"
              placeholder="切换商家视图"
              style="width: 220px"
              allow-clear
              show-search
              :filter-option="filterMerchantOption"
              @change="handleMerchantChange"
            >
              <a-select-opt-group v-for="group in groupedMerchants" :key="group.label" :label="group.label">
                <a-select-option v-for="m in group.options" :key="m.id" :value="String(m.id)">{{ m.name }}</a-select-option>
              </a-select-opt-group>
            </a-select>
          </div>
          <a-dropdown>
            <div class="user-info">
              <a-avatar size="small" style="background-color: #1677ff">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'A').charAt(0) }}
          </a-avatar>
          <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
              <DownOutlined />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="goToProfile">
                  <template #icon>
                    <UserOutlined />
                  </template>
                  个人中心
                </a-menu-item>
                <a-menu-item key="settings" @click="goToSettings">
                  <template #icon>
                    <SettingOutlined />
                  </template>
                  系统设置
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <template #icon>
                    <LogoutOutlined />
                  </template>
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <a-layout-content class="layout-content">
        <router-view v-slot="{ Component }" :key="routerViewKey">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PieChartOutlined,
  ShopOutlined,
  PhoneOutlined,
  BulbOutlined,
  ShareAltOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  DownOutlined,
  LikeOutlined
} from '@ant-design/icons-vue'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { APP_NAME } from '@/config/app'
import { getMerchantList } from '@/api/merchant'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const selectedKeys = ref([route.path])
const openKeys = ref([])

const selectedMerchantId = ref(userStore.currentMerchantId || undefined)
const merchantList = ref([])

const isMerchantView = computed(() => {
  return userStore.isSuperAdmin && !!appStore.merchantId
})

// "我的商家"菜单：商家角色 或 超管商家视图 下显示
const showMyMerchantMenu = computed(() => {
  if (userStore.isMerchant) return !!userStore.userInfo?.merchantId
  if (isMerchantView.value) return !!appStore.merchantId
  return false
})

// 当前"我的商家"实际跳转的商家ID
const myMerchantId = computed(() => {
  if (userStore.isMerchant) return userStore.userInfo?.merchantId
  if (isMerchantView.value) return appStore.merchantId
  return null
})

// 营销管理菜单显示逻辑：超管（非商家视图）显示完整菜单；商家角色显示"我的推广平台"；超管商家视图显示"我的推广平台"
const showMarketingMenu = computed(() => {
  if (canAccess('marketing')) return true
  if (isMerchantView.value) return true
  return false
})

const routerViewKey = computed(() => {
  return route.path + '_' + (appStore.merchantId || 'all')
})

const roleLabelMap = {
  super_admin: '超级管理员',
  admin: '管理员',
  merchant: '商家',
  '1': '超级管理员',
  '2': '管理员',
  '3': '商家'
}

const groupedMerchants = computed(() => {
  const groups = {}
  merchantList.value.forEach((merchant) => {
    const roleKey = merchant.role || merchant.type || 'merchant'
    const label = roleLabelMap[roleKey] || '商家'
    if (!groups[label]) {
      groups[label] = []
    }
    groups[label].push(merchant)
  })
  const sortOrder = ['超级管理员', '管理员', '商家']
  return Object.keys(groups)
    .sort((a, b) => {
      const indexA = sortOrder.indexOf(a)
      const indexB = sortOrder.indexOf(b)
      if (indexA === -1 && indexB === -1) return a.localeCompare(b)
      if (indexA === -1) return 1
      if (indexB === -1) return -1
      return indexA - indexB
    })
    .map((label) => ({
      label,
      options: groups[label]
    }))
})

const loadMerchantList = async () => {
  try {
    const res = await getMerchantList({ page: 1, pageSize: 100 })
    merchantList.value = res.list || []
    // Restore current merchant object from persisted id
    const persistedId = userStore.currentMerchantId
    if (persistedId && !appStore.currentMerchant) {
      const merchant = merchantList.value.find(m => String(m.id) === String(persistedId))
      if (merchant) {
        appStore.setCurrentMerchant(merchant)
      }
    }
  } catch (e) {
    console.error('加载商家列表失败', e)
  }
}

const handleMerchantChange = (value) => {
  if (value) {
    const merchant = merchantList.value.find(m => String(m.id) === String(value))
    appStore.setCurrentMerchant(merchant || null)
    userStore.setCurrentMerchantId(value)
  } else {
    appStore.setCurrentMerchant(null)
    userStore.setCurrentMerchantId('')
  }
  // 商家视图下需要跳转走的路径（仅超管可见的页面）
  const merchantViewHiddenPaths = ['/merchant/list', '/merchant/quota', '/marketing/platforms', '/marketing/coupons', '/marketing/plans', '/marketing/orders', '/system/user', '/system/role', '/system/merchant-access', '/system/settings', '/ai/merchant-config']
  if (value && merchantViewHiddenPaths.some(p => route.path === p || route.path.startsWith(p + '/'))) {
    router.push('/dashboard')
    return
  }
  // 切换商家视图时，若正在查看其他商家的详情页，跳转到新商家详情页
  if (route.path.startsWith('/merchant/detail/') && route.params.id !== String(value)) {
    router.push(value ? `/merchant/detail/${value}` : '/dashboard')
  }
}

const filterMerchantOption = (input, option) => {
  const label = option.children?.[0]?.children
  if (typeof label === 'string') {
    return label.toLowerCase().includes(input.toLowerCase())
  }
  return false
}

const menuTitleMap = {
  '/dashboard': '仪表盘',
  '/merchant/list': '商家列表',
  '/merchant/quota': '额度管理',
  '/merchant/my': '我的商家',
  '/device/list': '设备管理',
  '/ai/generate': 'AI创作',
  '/ai/corpus': '语料管理',
  '/ai/config': '创作配置',
  '/ai/merchant-config': '商家配置总览',
  '/marketing/platforms': '推广平台总配置',
  '/marketing/merchant-promotion': '我的推广平台',
  '/marketing/coupons': '优惠券',
  '/marketing/plans': '套餐管理',
  '/marketing/orders': '订单管理',
  '/system/settings': '系统配置',
  '/system/profile': '个人中心',
  '/system/user': '用户管理',
  '/system/role': '角色管理',
  '/system/merchant-access': '商家权限配置'
}

const canAccess = (permission) => {
  return userStore.hasPermission(permission)
}

const breadcrumbs = computed(() => {
  const path = route.path
  const crumbs = [{ title: '仪表盘', path: '/dashboard' }]
  
  if (path !== '/dashboard') {
    const segments = path.split('/').filter(Boolean)
    if (segments.length >= 2) {
      const parentTitle = getParentTitle(segments[0])
      if (parentTitle) {
        crumbs.push({ title: parentTitle, path: '' })
      }
    }
    crumbs.push({ title: menuTitleMap[path] || route.meta.title || '', path })
  }
  
  return crumbs
})

const getParentTitle = (key) => {
  const map = {
    merchant: '商家管理',
    device: '设备管理',
    ai: 'AI创作',
    marketing: '营销管理',
    system: '系统设置'
  }
  return map[key] || ''
}

watch(
  () => route.path,
  (newPath) => {
    // 商家详情页时高亮"我的商家"菜单（商家角色/商家视图下）
    if (newPath.startsWith('/merchant/detail/') && showMyMerchantMenu.value) {
      selectedKeys.value = ['/merchant/my']
    } else {
      selectedKeys.value = [newPath]
    }
    const segments = newPath.split('/').filter(Boolean)
    if (segments.length >= 2) {
      const parentKey = segments[0]
      if (!openKeys.value.includes(parentKey)) {
        openKeys.value.push(parentKey)
      }
    }
  },
  { immediate: true }
)

const handleMenuClick = ({ key }) => {
  // "我的商家"菜单：根据当前角色/视图跳转到对应的商家详情页
  if (key === '/merchant/my') {
    const mid = myMerchantId.value
    if (mid) {
      router.push(`/merchant/detail/${mid}`)
    } else {
      message.warning('未绑定商家信息')
    }
    return
  }
  if (key !== route.path) {
    router.push(key)
  }
}

const handleOpenChange = (keys) => {
  openKeys.value = keys
}

onMounted(() => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.fetchUserInfo().catch(() => {})
  }
  if (userStore.isAdmin) {
    loadMerchantList()
  }
  // 商家列表新增/编辑/删除后实时刷新"切换商家视图"下拉
  window.addEventListener('merchant-changed', loadMerchantList)
})

onUnmounted(() => {
  window.removeEventListener('merchant-changed', loadMerchantList)
})

const goToProfile = () => {
  router.push('/system/profile')
}

const goToSettings = () => {
  router.push('/system/settings')
}

const handleLogout = () => {
  Modal.confirm({
    title: '提示',
    content: '确定要退出登录吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      userStore.logout()
      message.success('已退出登录')
      router.push('/login')
    }
  })
}
</script>

<style lang="scss" scoped>
.basic-layout {
  height: 100vh;
}

.layout-sider {
  background: linear-gradient(180deg, #001529 0%, #000c17 100%);
  
  :deep(.ant-layout-sider-children) {
    display: flex;
    flex-direction: column;
  }
}

.sider-logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.4);
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
}

:deep(.ant-menu-dark) {
  flex: 1;
  border-right: none;
}

.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 2px rgba(0, 21, 41, 0.04);
  position: relative;
  z-index: 10;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  color: #595959;
  transition: color 0.3s;
  
  &:hover {
    color: #1677ff;
  }
}

.breadcrumb {
  margin: 0;
  
  :deep(.ant-breadcrumb-link) {
    font-size: 14px;
    color: $text-secondary;
    
    &:hover {
      color: $primary-color;
    }
  }
  
  :deep(.ant-breadcrumb-separator) {
    color: #bfbfbf;
    margin: 0 8px;
  }
  
  :deep(.ant-breadcrumb > span:last-child .ant-breadcrumb-link) {
    color: $text-color;
    font-weight: 500;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.merchant-select-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 32px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s;

  &:hover {
    border-color: #1677ff;
    background: #fff;
  }

  :deep(.ant-select) {
    .ant-select-selector {
      height: 30px !important;
      min-height: 30px !important;
      border: none !important;
      background: transparent !important;
      box-shadow: none !important;
    }

    .ant-select-selection-item,
    .ant-select-selection-placeholder {
      line-height: 30px !important;
    }

    .ant-select-arrow {
      top: 0 !important;
      right: 8px !important;
      height: 100% !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      transform: none !important;
    }
  }
}

.merchant-select-icon {
  color: #8c8c8c;
  font-size: 14px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
  height: 64px;
  
  &:hover {
    background: #f5f5f5;
  }
}

.user-name {
  font-size: 14px;
  color: #595959;
  margin: 0 4px;
}

.layout-content {
  background: linear-gradient(180deg, #f0f5ff 0%, #f5f7fa 200px);
  overflow-y: auto;
  padding: 0;
  min-height: 0;
}

.layout-content > * {
  padding: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
