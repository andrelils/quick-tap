import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const currentMerchantId = ref(localStorage.getItem('currentMerchantId') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'super_admin' || userInfo.value?.role === 'admin')
  const isMerchant = computed(() => userInfo.value?.role === 'merchant')
  const isSuperAdmin = computed(() => userInfo.value?.role === 'super_admin')
  const permissions = computed(() => userInfo.value?.permissions || [])

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setCurrentMerchantId = (id) => {
    currentMerchantId.value = id
    if (id) {
      localStorage.setItem('currentMerchantId', String(id))
    } else {
      localStorage.removeItem('currentMerchantId')
    }
  }

  const login = async (loginForm) => {
    const res = await loginApi(loginForm)
    if (res.token) {
      setToken(res.token)
    }
    if (res.userInfo) {
      setUserInfo(res.userInfo)
    }
    return res
  }

  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    setUserInfo(res)
    return res
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (e) {
      console.error('登出接口调用失败', e)
    }
    token.value = ''
    userInfo.value = null
    currentMerchantId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('currentMerchantId')
  }

  const hasPermission = (perm) => {
    const perms = permissions.value
    if (!perms || perms.length === 0) return false
    if (perms.includes('*')) return true
    if (perms.includes(perm)) return true
    // 检查是否有以 perm. 开头的子权限（如 merchant → merchant.view、merchant.create）
    return perms.some(p => p === perm || p.startsWith(perm + '.'))
  }

  return {
    token,
    userInfo,
    currentMerchantId,
    isLoggedIn,
    isAdmin,
    isMerchant,
    isSuperAdmin,
    permissions,
    setToken,
    setUserInfo,
    setCurrentMerchantId,
    login,
    fetchUserInfo,
    logout,
    hasPermission
  }
})
