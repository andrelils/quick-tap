<template>
  <view class="coupon-page">
    <view class="coupon-tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'available' }"
        @tap="activeTab = 'available'"
      >
        <text>可领取</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'mine' }"
        @tap="activeTab = 'mine'"
      >
        <text>我的优惠券</text>
      </view>
    </view>

    <view class="coupon-content">
      <view v-show="activeTab === 'available'" class="coupon-list">
        <view 
          v-for="coupon in availableCoupons" 
          :key="coupon.id" 
          class="coupon-card"
        >
          <view class="coupon-left">
            <text class="coupon-value">
              <text class="currency">¥</text>
              {{ coupon.discountValue }}
            </text>
            <text class="coupon-condition" v-if="coupon.minAmount > 0">满{{ coupon.minAmount }}可用</text>
            <text class="coupon-condition" v-else>无门槛</text>
          </view>
          <view class="coupon-right">
            <text class="coupon-name">{{ coupon.couponName }}</text>
            <text class="coupon-desc">{{ coupon.description || '欢迎使用' }}</text>
            <text class="coupon-valid">
              有效期至 {{ formatDate(coupon.validEndTime) }}
            </text>
            <view class="coupon-footer">
              <text class="coupon-count">剩余 {{ coupon.totalCount - coupon.issuedCount }} 张</text>
              <view 
                class="claim-btn" 
                :class="{ disabled: coupon.issuedCount >= coupon.totalCount }"
                @tap="handleClaim(coupon)"
              >
                {{ coupon.issuedCount >= coupon.totalCount ? '已领完' : '立即领取' }}
              </view>
            </view>
          </view>
        </view>

        <view class="empty-state" v-if="availableCoupons.length === 0">
          <u-icon name="coupon" size="64" color="#d9d9d9"></u-icon>
          <text class="empty-text">暂无可领取的优惠券</text>
        </view>
      </view>

      <view v-show="activeTab === 'mine'" class="coupon-list">
        <view 
          v-for="record in myCoupons" 
          :key="record.id" 
          class="coupon-card mine"
          :class="{ used: record.status === 1, expired: record.status === 2 }"
        >
          <view class="coupon-left">
            <text class="coupon-value">
              <text class="currency">¥</text>
              {{ getCouponValue(record.couponId) }}
            </text>
            <text class="coupon-condition">{{ getCouponName(record.couponId) }}</text>
          </view>
          <view class="coupon-right">
            <text class="coupon-name">{{ getCouponName(record.couponId) }}</text>
            <text class="coupon-code">券码：{{ record.couponCode }}</text>
            <text class="coupon-valid">
              {{ record.status === 1 ? '已使用' : record.status === 2 ? '已过期' : '未使用' }}
            </text>
            <view class="coupon-footer">
              <text class="coupon-time">领取时间：{{ formatDate(record.createTime) }}</text>
            </view>
          </view>
          <view class="coupon-status" v-if="record.status !== 0">
            <text>{{ record.status === 1 ? '已使用' : '已过期' }}</text>
          </view>
        </view>

        <view class="empty-state" v-if="myCoupons.length === 0">
          <u-icon name="coupon" size="64" color="#d9d9d9"></u-icon>
          <text class="empty-text">暂无优惠券</text>
          <text class="empty-desc">去领取商家的优惠券吧</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCouponList, claimCoupon, getMyCoupons } from '@/api/coupon'

const activeTab = ref('available')
const availableCoupons = ref([])
const myCoupons = ref([])
const merchantId = ref(null)

onLoad((options) => {
  if (options.merchantId) {
    merchantId.value = options.merchantId
    loadAvailableCoupons()
  }
  loadMyCoupons()
})

const loadAvailableCoupons = async () => {
  try {
    const res = await getCouponList(merchantId.value)
    availableCoupons.value = res || []
  } catch (e) {
    console.error('加载优惠券失败', e)
  }
}

const loadMyCoupons = async () => {
  try {
    const res = await getMyCoupons()
    myCoupons.value = res || []
  } catch (e) {
    console.error('加载我的优惠券失败', e)
  }
}

const handleClaim = async (coupon) => {
  if (coupon.issuedCount >= coupon.totalCount) return
  
  try {
    await claimCoupon(coupon.id)
    uni.showToast({
      title: '领取成功',
      icon: 'success'
    })
    loadAvailableCoupons()
    loadMyCoupons()
  } catch (e) {
    console.error('领取失败', e)
  }
}

const getCouponValue = (couponId) => {
  const coupon = availableCoupons.value.find(c => c.id === couponId)
  return coupon?.discountValue || 0
}

const getCouponName = (couponId) => {
  const coupon = availableCoupons.value.find(c => c.id === couponId)
  return coupon?.couponName || '优惠券'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.coupon-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.coupon-tabs {
  display: flex;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 28rpx 0;
  font-size: 28rpx;
  color: #595959;
  position: relative;
}

.tab-item.active {
  color: #ff4d4f;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  background: #ff4d4f;
  border-radius: 3rpx;
}

.coupon-content {
  padding: 24rpx;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.coupon-card {
  background: #fff;
  border-radius: 20rpx;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.coupon-card.mine {
  opacity: 1;
}

.coupon-card.used,
.coupon-card.expired {
  opacity: 0.6;
}

.coupon-left {
  width: 180rpx;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  padding: 32rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
}

.coupon-card.used .coupon-left,
.coupon-card.expired .coupon-left {
  background: linear-gradient(135deg, #bfbfbf 0%, #d9d9d9 100%);
}

.coupon-left::before,
.coupon-left::after {
  content: '';
  position: absolute;
  right: -12rpx;
  width: 24rpx;
  height: 24rpx;
  background: #f5f6fa;
  border-radius: 50%;
}

.coupon-left::before {
  top: -12rpx;
}

.coupon-left::after {
  bottom: -12rpx;
}

.coupon-value {
  font-size: 48rpx;
  font-weight: bold;
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 24rpx;
  margin-right: 4rpx;
}

.coupon-condition {
  font-size: 20rpx;
  opacity: 0.9;
  margin-top: 8rpx;
  text-align: center;
}

.coupon-right {
  flex: 1;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.coupon-desc {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
  flex: 1;
}

.coupon-code {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
}

.coupon-valid {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
}

.coupon-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.coupon-count {
  font-size: 22rpx;
  color: #bfbfbf;
}

.coupon-time {
  font-size: 22rpx;
  color: #bfbfbf;
}

.claim-btn {
  padding: 10rpx 28rpx;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: #fff;
  border-radius: 28rpx;
  font-size: 24rpx;
}

.claim-btn.disabled {
  background: #d9d9d9;
}

.coupon-status {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 4rpx 12rpx;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border-radius: 8rpx;
  font-size: 20rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
  background: #fff;
  border-radius: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
  margin-top: 24rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #bfbfbf;
  margin-top: 12rpx;
}
</style>
