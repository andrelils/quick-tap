import request from '@/utils/request'

export const getMerchantInfo = (merchantId) => {
  return request.get(`/merchant/info/${merchantId}`)
}

export const getMerchantPromotion = (deviceId) => {
  return request.get(`/merchant/promotion`, { deviceId })
}

export const getMerchantWifi = (merchantId) => {
  return request.get(`/merchant/wifi`, { merchantId })
}
