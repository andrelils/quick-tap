import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, wechatLogin } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(uni.getStorageSync('userInfo') || null)

  const isLoggedIn = computed(() => !!token.value)

  const setToken = (newToken) => {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  const loginByWechat = async (code) => {
    const res = await wechatLogin(code)
    if (res.token) {
      setToken(res.token)
      if (res.userInfo) {
        setUserInfo(res.userInfo)
      }
    }
    return res
  }

  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    setUserInfo(res)
    return res
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    loginByWechat,
    fetchUserInfo,
    logout
  }
})
