import request from '@/utils/request'

export const getCouponList = (merchantId) => {
  // 无 merchantId 时不传参数，后端返回全量可领取券
  const data = merchantId ? { merchantId } : {}
  return request.get(`/coupon/list`, data)
}

export const claimCoupon = (couponId) => {
  return request.post('/coupon/claim', { couponId })
}

export const getMyCoupons = () => {
  return request.get('/coupon/my')
}
