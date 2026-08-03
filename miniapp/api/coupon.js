import request from '@/utils/request'

export const getCouponList = (merchantId) => {
  return request.get(`/coupon/list`, { merchantId })
}

export const claimCoupon = (couponId) => {
  return request.post('/coupon/claim', { couponId })
}

export const getMyCoupons = () => {
  return request.get('/coupon/my')
}
