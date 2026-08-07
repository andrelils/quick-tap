import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(
  (config) => {
    // 静态资源路径（/uploads/**）不需要认证，跳过添加 token
    if (!config.url?.startsWith('/uploads')) {
      const userStore = useUserStore()
      if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
      }
    }
    // 分页参数兼容处理：前端通用 page/current，后端统一 pageNum
    if (config.params && typeof config.params === 'object') {
      if (config.params.current != null && config.params.pageNum == null) {
        config.params.pageNum = config.params.current
      }
      if (config.params.page != null && config.params.pageNum == null) {
        config.params.pageNum = config.params.page
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 检查code是否为成功状态（支持字符串和数字形式）
    if (res.code === '1000' || res.code === 1000 || res.code === 0 || res.status === 200) {
      return res.data
    } else if (res.code === '2001' || res.code === 2001 || res.code === 401) {
      // 未认证或token过期
      const userStore = useUserStore()
      userStore.logout()
      message.error('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(res.message || '未授权'))
    } else {
      // 其他错误
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    console.error('请求错误:', error)
    const status = error.response?.status
    const resData = error.response?.data
    if (status === 401 || resData?.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      message.error(resData?.message || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(resData?.message || '未授权'))
    }
    message.error(resData?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service

// 上传接口（a-upload 原生请求，不经过 axios 拦截器）成功判断
// 后端成功 code 为字符串 "1000"，兼容数字 0 / "0" / 1000 / status 200
export const isSuccessCode = (res) => {
  return !!res && res.data != null && (
    res.code === 0 || res.code === '0' || res.code === 1000 || res.code === '1000' || res.status === 200
  )
}
