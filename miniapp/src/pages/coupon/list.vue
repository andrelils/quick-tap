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
              {{ coupon.amount ?? coupon.value ?? coupon.discountValue ?? 0 }}
            </text>
            <text class="coupon-condition" v-if="(coupon.minAmount ?? 0) > 0">满{{ coupon.minAmount }}可用</text>
            <text class="coupon-condition" v-else>无门槛</text>
          </view>
          <view class="coupon-right">
            <text class="coupon-name">{{ coupon.title ?? coupon.name ?? coupon.couponName }}</text>
            <text class="coupon-desc">{{ coupon.description || '欢迎使用' }}</text>
            <text class="coupon-valid">
              有效期至 {{ formatDate(coupon.endTime ?? coupon.validEndTime) }}
            </text>
            <view class="coupon-footer">
              <text class="coupon-count">剩余 {{ coupon.totalCount - coupon.issuedCount }} 张</text>
              <view 
                class="claim-btn" 
                :class="{ disabled: coupon.issuedCount >= coupon.totalCount }"
                @tap="handleClaim(coupon)"
              >
                {{ coupon.issuedCount >= coupon.totalCount ? '已领完' : '去领取' }}
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

        <view class="empty-state" v-if="!isLoggedIn" @tap="goToLogin">
          <view class="login-icon">
            <u-icon name="lock" size="56" color="#faad14"></u-icon>
          </view>
          <text class="empty-text">登录后查看我的优惠券</text>
          <text class="empty-desc">点击去登录 ></text>
        </view>

        <view class="empty-state" v-else-if="myCoupons.length === 0">
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
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getCouponList, claimCoupon, getMyCoupons } from '@/api/coupon'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const activeTab = ref('available')
const availableCoupons = ref([])
const myCoupons = ref([])
const merchantId = ref(null)

onLoad((options) => {
  if (options.merchantId) {
    merchantId.value = options.merchantId
  }
  // 无 merchantId 时也加载全量可领取券（后端不过滤）
  loadAvailableCoupons()
})

onShow(() => {
  // 每次显示页面时刷新登录态，登录后自动加载我的优惠券
  if (isLoggedIn.value) {
    loadMyCoupons()
  }
})

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/user/mine' })
}

const loadAvailableCoupons = async () => {
  try {
    const res = await getCouponList(merchantId.value)
    availableCoupons.value = res || []
  } catch (e) {
    console.error('加载优惠券失败', e)
  }
}

const loadMyCoupons = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await getMyCoupons()
    myCoupons.value = res || []
  } catch (e) {
    // 401 表示用户未登录，属于正常情况，不输出错误日志
    if (e?.statusCode === 401 || e?.code === 401) {
      // token 失效，清理本地登录态
      userStore.logout()
      return
    }
    console.error('加载我的优惠券失败', e)
  }
}

const handleClaim = (coupon) => {
  if (coupon.issuedCount >= coupon.totalCount) return
  const url = coupon.link || coupon.thirdPartyUrl || coupon.linkUrl || coupon.externalUrl
  if (!url) {
    uni.showToast({ title: '暂无可领取链接', icon: 'none' })
    return
  }
  // 跳转到第三方领券页面
  uni.navigateTo({
    url: `/pages/promotion/jump?type=coupon&name=${encodeURIComponent(coupon.couponName || coupon.name || '优惠券')}&url=${encodeURIComponent(url)}&merchantId=${merchantId.value}`
  })
}

const getCouponValue = (couponId) => {
  const coupon = availableCoupons.value.find(c => c.id === couponId)
  return coupon?.amount ?? coupon?.value ?? coupon?.discountValue ?? 0
}

const getCouponName = (couponId) => {
  const coupon = availableCoupons.value.find(c => c.id === couponId)
  return coupon?.title ?? coupon?.name ?? coupon?.couponName ?? '优惠券'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.coupon-page {
  min-height: 100vh;
  background: $bg-page;
}

.coupon-tabs {
  display: flex;
  background: $bg-card;
  position: sticky;
  top: 0;
  z-index: $z-header;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 28rpx 0;
  font-size: $font-size-md;
  color: $text-regular;
  position: relative;
}

.tab-item.active {
  color: $error-color;
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
  background: $error-color;
  border-radius: 3rpx;
}

.coupon-content {
  padding: $spacing-md;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.coupon-card {
  background: $bg-card;
  border-radius: $border-radius-lg;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: $shadow-sm;
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
  background: $gradient-error;
  padding: $spacing-lg $spacing-sm;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $text-white;
  position: relative;
}

.coupon-card.used .coupon-left,
.coupon-card.expired .coupon-left {
  background: linear-gradient(135deg, $text-placeholder 0%, #d9d9d9 100%);
}

.coupon-left::before,
.coupon-left::after {
  content: '';
  position: absolute;
  right: -12rpx;
  width: 24rpx;
  height: 24rpx;
  background: $bg-page;
  border-radius: 50%;
}

.coupon-left::before {
  top: -12rpx;
}

.coupon-left::after {
  bottom: -12rpx;
}

.coupon-value {
  font-size: $font-size-xxl;
  font-weight: bold;
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: $font-size-sm;
  margin-right: 4rpx;
}

.coupon-condition {
  font-size: $font-size-xs;
  opacity: 0.9;
  margin-top: $spacing-xs;
  text-align: center;
}

.coupon-right {
  flex: 1;
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: $font-size-lg;
  font-weight: bold;
  color: $text-primary;
}

.coupon-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-xs;
  flex: 1;
}

.coupon-code {
  font-size: 22rpx;
  color: $text-secondary;
  margin-top: $spacing-xs;
}

.coupon-valid {
  font-size: 22rpx;
  color: $text-secondary;
  margin-top: $spacing-xs;
}

.coupon-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $spacing-sm;
}

.coupon-count {
  font-size: 22rpx;
  color: $text-placeholder;
}

.coupon-time {
  font-size: 22rpx;
  color: $text-placeholder;
}

.claim-btn {
  padding: 10rpx 28rpx;
  background: $gradient-error;
  color: $text-white;
  border-radius: $border-radius-full;
  font-size: $font-size-sm;
}

.claim-btn.disabled {
  background: $text-placeholder;
}

.coupon-status {
  position: absolute;
  top: $spacing-sm;
  right: $spacing-sm;
  padding: 4rpx $spacing-sm;
  background: rgba(0,0,0,0.5);
  color: $text-white;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx $spacing-xl;
  background: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-sm;
}

.login-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(250, 173, 20, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-sm;
}

.empty-text {
  font-size: $font-size-md;
  color: $text-secondary;
  margin-top: $spacing-md;
}

.empty-desc {
  font-size: $font-size-sm;
  color: $primary-color;
  margin-top: $spacing-sm;
}
</style>
