import request from '@/utils/request'

export const getOverview = (params) => {
  return request.get('/admin/statistics/overview', { params })
}

export const getTrend = (params) => {
  return request.get('/admin/statistics/trend', { params })
}

export const getMerchantStats = (merchantId) => {
  return request.get(`/admin/statistics/merchant/${merchantId}`)
}

export const getTopMerchants = (params) => {
  return request.get('/admin/statistics/top/merchants', { params })
}

export const getAiStats = (params) => {
  return request.get('/admin/statistics/ai-stats', { params })
}
