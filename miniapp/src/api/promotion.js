import request from '@/utils/request'

// 获取商家配置的推广平台列表（miniapp首页用，含完整跳转信息）
// 调用 MiniappController 的 /merchant/promotion 接口
export const getPromotionPlatforms = (merchantId) => {
  return request.get('/merchant/promotion', { merchantId })
}

// 获取单个推广平台详情（含完整跳转信息）
export const getPromotionPlatformDetail = (id) => {
  return request.get(`/promotion/platform/${id}`)
}

// 记录推广点击日志（需登录）
// 后端未提供此接口
export const logPromotionClick = (data) => {
  console.warn('后端未提供推广点击日志接口')
  return Promise.resolve({ success: true })
}
