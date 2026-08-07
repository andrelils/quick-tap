<template>
  <view class="home-page">
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="logo-area">
          <view class="logo-icon">
            <u-icon name="thumb-up-fill" color="#fff" size="32"></u-icon>
          </view>
          <view class="logo-text">
            <text class="title">晓居智能</text>
            <text class="subtitle">NFC智能推广 · 一键好评</text>
          </view>
        </view>
      </view>
    </view>

    <view class="scan-section">
      <view class="scan-card" @tap="handleScan">
        <view class="scan-icon-wrapper">
          <view class="scan-icon">
            <u-icon name="scan" color="#1677ff" size="48"></u-icon>
          </view>
          <view class="scan-ring ring1"></view>
          <view class="scan-ring ring2"></view>
        </view>
        <view class="scan-text">
          <text class="scan-title">碰一碰 / 扫一扫</text>
          <text class="scan-desc">点击扫描NFC标签或二维码</text>
        </view>
      </view>
    </view>

    <view class="quick-actions">
      <view class="action-item" @tap="goToWifi" v-if="merchantInfo">
        <view class="action-icon wifi">
          <u-icon name="wifi" color="#fff" size="28"></u-icon>
        </view>
        <text class="action-text">连WiFi</text>
      </view>
      <view class="action-item" @tap="goToCoupon" v-if="merchantInfo">
        <view class="action-icon coupon">
          <u-icon name="coupon" color="#fff" size="28"></u-icon>
        </view>
        <text class="action-text">领优惠</text>
      </view>
      <view class="action-item" @tap="goToRegister" v-if="!userStore.isLoggedIn">
        <view class="action-icon register">
          <u-icon name="account" color="#fff" size="28"></u-icon>
        </view>
        <text class="action-text">注册绑定</text>
      </view>
      <view class="action-item" @tap="goToMine">
        <view class="action-icon mine">
          <u-icon name="person-fill" color="#fff" size="28"></u-icon>
        </view>
        <text class="action-text">我的</text>
      </view>
    </view>

    <view class="merchant-section" v-if="merchantInfo">
      <view class="section-header">
        <text class="section-title">商家信息</text>
      </view>
      <view class="merchant-card" @tap="goToMerchantDetail">
        <view class="merchant-header">
          <image class="merchant-logo" :src="merchantInfo.shopLogo || '/static/logo.png'" mode="aspectFill"></image>
          <view class="merchant-info">
            <text class="merchant-name">{{ merchantInfo.shopName }}</text>
            <view class="merchant-address">
              <u-icon name="map-marker" size="20" color="#999"></u-icon>
              <text class="address-text">{{ merchantInfo.shopAddress || '暂无地址' }}</text>
            </view>
          </view>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        <view class="merchant-desc" v-if="merchantInfo.shopDesc">
          <text>{{ merchantInfo.shopDesc }}</text>
        </view>
        <view class="merchant-images" v-if="merchantImages.length > 0">
          <image 
            v-for="(img, idx) in merchantImages" 
            :key="idx" 
            :src="img" 
            mode="aspectFill"
            class="merchant-image"
          ></image>
        </view>
      </view>
    </view>

    <view class="promotion-section" v-if="platforms.length > 0">
      <view class="section-header">
        <text class="section-title">一键推广</text>
        <text class="section-more">去好评，享更多优惠</text>
      </view>
      <view class="promotion-grid">
        <view
          v-for="platform in platforms"
          :key="platform.id"
          class="promo-item"
          @tap="handlePromotion(platform)"
        >
          <view class="promo-icon" :style="{ backgroundColor: platform.color || '#1677ff' }">
            <text class="promo-icon-text">{{ (platform.name || '?').charAt(0) }}</text>
          </view>
          <text class="promo-name">{{ platform.name }}</text>
        </view>
      </view>
    </view>

    <view class="empty-section" v-if="!merchantInfo">
      <view class="empty-icon">
        <u-icon name="info-circle" size="64" color="#d9d9d9"></u-icon>
      </view>
      <text class="empty-text">请扫描商家的NFC标签或二维码</text>
      <text class="empty-desc">开始体验便捷的好评推广服务</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { getPromotionPlatforms } from '@/api/promotion'
import { checkDeviceInfo } from '@/api/device'

const userStore = useUserStore()
const appStore = useAppStore()

const merchantInfo = ref(null)
const platforms = ref([])
const merchantImages = ref([])
const deviceCheckLoading = ref(false)
const deviceError = ref(null)

onMounted(async () => {
  // 检查设备信息
  await checkDevice()

  if (appStore.currentMerchant) {
    merchantInfo.value = appStore.currentMerchant
    loadPlatforms(appStore.currentMerchant.id)
  }
})

onShow(() => {
  if (appStore.currentMerchant) {
    merchantInfo.value = appStore.currentMerchant
    loadPlatforms(appStore.currentMerchant.id)
  }
})

const checkDevice = async () => {
  deviceCheckLoading.value = true
  deviceError.value = null

  try {
    const deviceInfo = await checkDeviceInfo()

    if (deviceInfo && deviceInfo.status === 'inactive') {
      uni.showModal({
        title: '设备未激活',
        content: '请激活设备后再使用此功能',
        showCancel: false
      })
    } else if (deviceInfo && deviceInfo.status === 'error') {
      deviceError.value = '设备状态异常，请稍后重试'
      uni.showToast({
        title: '设备状态异常',
        icon: 'none'
      })
    }
  } catch (e) {
    console.error('设备检查失败', e)
    deviceError.value = '检查设备信息失败'
    // 不阻断页面显示，仅记录错误
  } finally {
    deviceCheckLoading.value = false
  }
}

const handleScan = () => {
  // #ifdef MP-WEIXIN
  wx.scanCode({
    success: (res) => {
      parseScanResult(res.result)
    },
    fail: () => {
      uni.showToast({
        title: '扫码取消',
        icon: 'none'
      })
    }
  })
  // #endif
  
  // #ifdef H5
  uni.showModal({
    title: '提示',
    content: 'H5环境暂不支持扫码，请使用微信小程序扫描',
    showCancel: false
  })
  // #endif
}

const parseScanResult = (result) => {
  try {
    const url = new URL(result)
    const deviceId = url.searchParams.get('d') || url.searchParams.get('device')
    const merchantId = url.searchParams.get('m') || url.searchParams.get('merchant')
    
    if (deviceId || merchantId) {
      uni.navigateTo({
        url: `/pages/merchant/detail?deviceId=${deviceId || ''}&merchantId=${merchantId || ''}`
      })
    } else {
      uni.showToast({
        title: '无效的二维码',
        icon: 'none'
      })
    }
  } catch (e) {
    uni.showToast({
      title: '扫码结果无效',
      icon: 'none'
    })
  }
}

const loadPlatforms = async (merchantId) => {
  try {
    const res = await getPromotionPlatforms(merchantId)
    // 兼容新老返回结构
    if (Array.isArray(res)) {
      platforms.value = res
    } else if (res && Array.isArray(res.platforms)) {
      platforms.value = res.platforms
    } else {
      platforms.value = []
    }
  } catch (e) {
    console.error('加载推广平台失败', e)
    platforms.value = []
  }
}

const handlePromotion = (platform) => {
  if (!merchantInfo.value) return

  uni.navigateTo({
    url: `/pages/promotion/jump?id=${platform.id}&merchantId=${merchantInfo.value.id}`
  })
}

const goToWifi = () => {
  uni.navigateTo({
    url: `/pages/wifi/index?merchantId=${merchantInfo.value.id}`
  })
}

const goToCoupon = () => {
  uni.navigateTo({
    url: `/pages/coupon/list?merchantId=${merchantInfo.value.id}`
  })
}

const goToRegister = () => {
  uni.navigateTo({
    url: '/pages/user/register-bind'
  })
}

const goToMine = () => {
  uni.switchTab({
    url: '/pages/user/mine'
  })
}

const goToMerchantDetail = () => {
  uni.navigateTo({
    url: `/pages/merchant/detail?merchantId=${merchantInfo.value.id}`
  })
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  padding-bottom: 40rpx;
}

.header {
  position: relative;
  padding-top: 88rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #69b1ff 100%);
  border-radius: 0 0 60rpx 60rpx;
}

.header-content {
  position: relative;
  padding: 40rpx 32rpx 60rpx;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.logo-icon {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  display: flex;
  flex-direction: column;
  color: #fff;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
}

.subtitle {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 4rpx;
}

.scan-section {
  padding: 0 24rpx;
  margin-top: -80rpx;
  position: relative;
  z-index: 10;
}

.scan-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 60rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(22, 119, 255, 0.15);
}

.scan-icon-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.scan-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.scan-ring {
  position: absolute;
  border-radius: 50%;
  border: 2rpx solid #bae0ff;
  animation: pulse 2s ease-out infinite;
}

.ring1 {
  width: 140rpx;
  height: 140rpx;
  animation-delay: 0s;
}

.ring2 {
  width: 160rpx;
  height: 160rpx;
  animation-delay: 1s;
}

@keyframes pulse {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  100% {
    transform: scale(1.2);
    opacity: 0;
  }
}

.scan-text {
  text-align: center;
}

.scan-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f1f1f;
  display: block;
}

.scan-desc {
  font-size: 26rpx;
  color: #8c8c8c;
  margin-top: 12rpx;
  display: block;
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 24rpx;
  background: #fff;
  margin: 24rpx;
  border-radius: 24rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.action-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon.wifi {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.action-icon.coupon {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.action-icon.register {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
}

.action-icon.mine {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.action-text {
  font-size: 24rpx;
  color: #595959;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 0 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.section-more {
  font-size: 24rpx;
  color: #8c8c8c;
}

.merchant-section {
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.merchant-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
}

.merchant-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.merchant-logo {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background: #f0f0f0;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.merchant-address {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.address-text {
  font-size: 24rpx;
  color: #8c8c8c;
}

.merchant-desc {
  margin-top: 24rpx;
  font-size: 26rpx;
  color: #595959;
  line-height: 1.6;
}

.merchant-images {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
  overflow-x: auto;
}

.merchant-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.promotion-section {
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.promotion-grid {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx 16rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx 0;
}

.promo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.promo-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.promo-icon-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

.promo-name {
  font-size: 24rpx;
  color: #595959;
}

.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
}

.empty-icon {
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #595959;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #8c8c8c;
}
</style>
