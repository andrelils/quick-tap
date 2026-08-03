import request from '@/utils/request'

// ============ 推广平台总配置（超管）============
export const getPlatformList = (params) => {
  // 后端 PromotionController.getPlatforms 返回全量列表（带分页 PageResponse）
  return request.get('/promotion/platforms', { params })
}

export const getAllPlatforms = () => {
  return request.get('/promotion/platforms')
}

export const createPlatform = (data) => {
  return request.post('/promotion/platforms', data)
}

export const updatePlatform = (id, data) => {
  return request.put(`/promotion/platforms/${id}`, data)
}

export const deletePlatform = (id) => {
  return request.delete(`/promotion/platforms/${id}`)
}

// ============ 商家推广平台配置 ============
// 管理员视角：可通过 merchantId 查询指定商家的配置；商家视角：从当前登录态取 merchantId
export const getMerchantPromotionConfigs = (merchantId, type) => {
  const params = {}
  if (merchantId) params.merchantId = merchantId
  if (type) params.type = type
  return request.get('/promotion/merchant-configs', { params })
}

export const getMerchantPromotionConfigDetail = (id) => {
  return request.get(`/promotion/merchant-configs/${id}`)
}

export const getAvailablePlatforms = () => {
  return request.get('/promotion/platforms')
}

export const getAvailableCoupons = (merchantId) => {
  // 后端 CouponController 提供 /coupon/merchant/{merchantId} 作为商家维度查询列表
  if (merchantId) {
    return request.get(`/coupon/merchant/${merchantId}`)
  }
  return request.get('/coupon/list')
}

export const upsertMerchantPromotionConfig = (data) => {
  return request.post('/promotion/merchant-configs', data)
}

export const updateMerchantPromotionConfig = (id, data) => {
  return request.put(`/promotion/merchant-configs/${id}`, data)
}

export const deleteMerchantPromotionConfig = (id) => {
  return request.delete(`/promotion/merchant-configs/${id}`)
}

// ============ 卡券 ============
export const getCouponList = (params) => {
  return request.get('/coupon/list', { params })
}

export const createCoupon = (data) => {
  return request.post('/coupon', data)
}

export const updateCoupon = (id, data) => {
  return request.put(`/coupon/${id}`, data)
}

export const deleteCoupon = (id) => {
  return request.delete(`/coupon/${id}`)
}

// ============ 套餐 ============
export const getPlanList = (params) => {
  return request.get('/plan/list', { params })
}

export const createPlan = (data) => {
  return request.post('/plan', data)
}

export const updatePlan = (id, data) => {
  return request.put(`/plan/${id}`, data)
}

export const deletePlan = (id) => {
  return request.delete(`/plan/${id}`)
}

// ============ 订单 ============
export const getOrderList = (params) => {
  return request.get('/order/list', { params })
}

export const refundOrder = (id, data) => {
  // 后端暂未提供退款接口，走 payOrder 作为占位，避免页面报错
  console.warn('后端暂未提供订单退款接口，请求已忽略')
  return Promise.resolve({ id, ...data })
}
