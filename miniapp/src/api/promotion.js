import request from '@/utils/request'

// 获取商家配置的推广平台列表（miniapp首页用，含完整跳转信息）
export const getPromotionPlatforms = (merchantId) => {
  return request.get('/promotion/platforms', { merchantId })
}

// 获取单个推广平台详情（含完整跳转信息）
export const getPromotionPlatformDetail = (id) => {
  return request.get(`/promotion/platform/${id}`)
}

// 记录推广点击日志（需登录）
export const logPromotionClick = (data) => {
  return request.post('/promotion/log', data)
}
