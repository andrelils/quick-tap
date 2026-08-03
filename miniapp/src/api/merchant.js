import request from '@/utils/request'

// 检查 NFC/二维码 ID 是否已绑定商家（首页进入时调用，公开接口）
// params: { type: 'nfc' | 'qrcode', code: '设备编号或二维码code' }
export const checkMerchantBind = (params) => {
  return request.get('/merchant/check-bind', params)
}

// 获取商家完整信息（含轮播图、店铺图片、老板微信、WiFi等）
export const getMerchantInfo = (merchantId) => {
  return request.get(`/merchant/info/${merchantId}`)
}

// 获取商家推广平台配置（含跳转模板与参数，用于一键工具栏）
export const getMerchantPromotion = (merchantId) => {
  return request.get('/merchant/promotion', { merchantId })
}

// 获取商家 WiFi 信息
export const getMerchantWifi = (merchantId) => {
  return request.get('/merchant/wifi', { merchantId })
}

// 商家自助入驻：创建 admin 账号 + merchant 记录 + 绑定二维码/NFC 设备
export const registerMerchant = (data) => {
  return request.post('/merchant/register', data)
}
