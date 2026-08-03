<template>
  <view class="merchant-detail">
    <!-- 第一部分：轮播图（推荐栏图片） -->
    <view class="banner-section">
      <swiper
        v-if="bannerImages.length > 0"
        class="banner-swiper"
        :indicator-dots="bannerImages.length > 1"
        :autoplay="true"
        :interval="4000"
        :duration="500"
        :circular="true"
        indicator-active-color="#1677ff"
        indicator-color="rgba(255,255,255,0.5)"
        @change="onBannerChange"
      >
        <swiper-item v-for="(img, idx) in bannerImages" :key="idx">
          <image class="banner-image" :src="img" mode="aspectFill" @tap="previewBanner(idx)"></image>
        </swiper-item>
      </swiper>
      <view v-else class="banner-placeholder">
        <image class="banner-logo-fallback" :src="merchantInfo?.logo || '/static/logo.png'" mode="aspectFit"></image>
      </view>
      <view class="banner-mask" v-if="merchantInfo"></view>
      <view class="merchant-brief" v-if="merchantInfo">
        <image class="shop-logo" :src="merchantInfo.logo || '/static/logo.png'" mode="aspectFill"></image>
        <view class="shop-info">
          <text class="shop-name">{{ merchantInfo.name }}</text>
          <view class="shop-meta">
            <view class="icon-map-marker-white" :style="{ width: '20rpx', height: '20rpx' }"></view>
            <text class="meta-text">{{ merchantInfo.address || '暂无地址' }}</text>
          </view>
          <view class="shop-meta" v-if="merchantInfo.businessHours">
            <view class="icon-clock-white" :style="{ width: '20rpx', height: '20rpx' }"></view>
            <text class="meta-text">{{ merchantInfo.businessHours }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 第二部分：优惠券领取 -->
    <view class="section" v-if="coupons.length > 0">
      <view class="section-header">
        <view class="section-title-bar"></view>
        <text class="section-title">优惠券领取</text>
        <text class="section-sub">共 {{ coupons.length }} 张</text>
      </view>
      <view class="coupon-list">
        <view
          v-for="coupon in coupons"
          :key="'c-' + coupon.id"
          class="coupon-item"
          @tap="handleCouponTap(coupon)"
        >
          <view class="coupon-left">
            <text class="coupon-value">
              <text class="currency">¥</text>
              {{ coupon.couponValue || coupon.value || 0 }}
            </text>
            <text class="coupon-condition" v-if="(coupon.couponThreshold || coupon.threshold || 0) > 0">满{{ coupon.couponThreshold || coupon.threshold }}可用</text>
            <text class="coupon-condition" v-else>无门槛</text>
          </view>
          <view class="coupon-right">
            <text class="coupon-name">{{ coupon.couponName || coupon.customName || coupon.name }}</text>
            <text class="coupon-valid" v-if="coupon.couponValidStart || coupon.validStart">
              {{ formatDate(coupon.couponValidStart || coupon.validStart) }} - {{ formatDate(coupon.couponValidEnd || coupon.validEnd) }}
            </text>
            <view class="coupon-action">
              <view class="claim-btn">
                去领取
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 第三部分：一键工具栏 -->
    <view class="section" v-if="platforms.length > 0 || merchantInfo?.bossWechat || merchantInfo?.wifiName">
      <view class="section-header">
        <view class="section-title-bar"></view>
        <text class="section-title">一键工具栏</text>
        <text class="section-sub">点击使用快捷服务</text>
      </view>
      <view class="tool-grid">
        <!-- 推广平台 -->
        <view
          v-for="platform in platforms"
          :key="'p-' + platform.id"
          class="tool-item"
          @tap="handlePromotion(platform)"
        >
          <view class="tool-icon" :style="{ backgroundColor: platform.color || '#1677ff' }">
            <text class="tool-icon-text">{{ (platform.name || '?').charAt(0) }}</text>
          </view>
          <text class="tool-name">{{ platform.name }}</text>
        </view>
        <!-- 加老板微信 -->
        <view
          v-if="merchantInfo && merchantInfo.bossWechat"
          class="tool-item"
          @tap="handleAddWechat"
        >
          <view class="tool-icon wechat">
            <text class="tool-icon-text">微</text>
          </view>
          <text class="tool-name">加老板微信</text>
        </view>
        <!-- 连接WiFi -->
        <view
          v-if="merchantInfo && merchantInfo.wifiName"
          class="tool-item"
          @tap="goToWifi"
        >
          <view class="tool-icon wifi">
            <u-icon name="wifi" color="#fff" size="32"></u-icon>
          </view>
          <text class="tool-name">连接WiFi</text>
        </view>
      </view>
    </view>

    <!-- 第四部分：商家介绍 -->
    <view class="section" v-if="merchantInfo && (merchantInfo.description || shopImages.length > 0 || merchantInfo.contactPhone)">
      <view class="section-header">
        <view class="section-title-bar"></view>
        <text class="section-title">商家介绍</text>
      </view>
      <view class="info-card">
        <view class="info-block" v-if="merchantInfo.description">
          <text class="info-label">店铺简介</text>
          <text class="info-desc">{{ merchantInfo.description }}</text>
        </view>
        <view class="info-block" v-if="shopImages.length > 0">
          <text class="info-label">店铺图片</text>
          <view class="image-grid">
            <view
              v-for="(img, idx) in shopImages"
              :key="idx"
              class="image-item"
              @tap="previewShopImage(idx)"
            >
              <image :src="img" mode="aspectFill" class="shop-image"></image>
            </view>
          </view>
        </view>
        <view class="info-block" v-if="merchantInfo.contactPhone">
          <text class="info-label">联系我们</text>
          <view class="contact-row">
            <view class="icon-phone-primary" :style="{ width: '24rpx', height: '24rpx' }"></view>
            <text class="contact-text" @tap="callPhone">{{ merchantInfo.contactPhone }}</text>
            <view class="call-btn" @tap="callPhone">
              <view class="icon-phone-success" :style="{ width: '24rpx', height: '24rpx' }"></view>
              <text class="call-text">拨打</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAppStore } from '@/store/app'
import { getMerchantInfo } from '@/api/merchant'
import { getPromotionPlatforms } from '@/api/promotion'

const appStore = useAppStore()

const merchantInfo = ref(null)
const platforms = ref([])
const coupons = ref([])
const bannerImages = ref([])
const currentBanner = ref(0)

const shopImages = computed(() => {
  return merchantInfo.value?.shopImages || []
})

onLoad((options) => {
  const { merchantId, deviceId } = options
  if (merchantId) {
    if (appStore.currentMerchant && String(appStore.currentMerchant.id) === String(merchantId)) {
      merchantInfo.value = appStore.currentMerchant
      bannerImages.value = appStore.currentMerchant.bannerImages || []
    }
    loadAll(merchantId)
  }
})

const loadAll = async (merchantId) => {
  await Promise.all([
    loadMerchantInfo(merchantId),
    loadPromotionData(merchantId)
  ])
}

const loadMerchantInfo = async (merchantId) => {
  try {
    const res = await getMerchantInfo(merchantId)
    if (res) {
      merchantInfo.value = res
      appStore.setCurrentMerchant(res)
      bannerImages.value = res?.bannerImages || []
    }
  } catch (e) {
    console.error('加载商家信息失败', e)
    if (!merchantInfo.value) {
      mockMerchantInfo(merchantId)
    }
  }
}

const mockMerchantInfo = (merchantId) => {
  const mock = {
    id: Number(merchantId) || 1,
    name: '碰一碰演示商家',
    logo: '',
    bannerImages: [
      '/static/banner/banner1.jpg',
      '/static/banner/banner2.jpg',
      '/static/banner/banner3.jpg'
    ],
    address: '北京市朝阳区建国路88号',
    description: '这是一个演示商家页面，展示NFC智能推广系统的完整功能',
    bossWechat: 'demo_boss_wechat',
    businessHours: '09:00-22:00',
    wifiName: 'DemoShop-WiFi',
    wifiPassword: 'demo1234',
    contactPhone: '400-888-8888',
    shopImages: [
      '/static/banner/banner1.jpg',
      '/static/banner/banner2.jpg',
      '/static/banner/banner3.jpg'
    ]
  }
  merchantInfo.value = mock
  bannerImages.value = mock.bannerImages
  appStore.setCurrentMerchant(mock)
}

// 统一加载推广平台和优惠券（数据来自【我的推广平台】配置）
const loadPromotionData = async (merchantId) => {
  try {
    const res = await getPromotionPlatforms(merchantId)
    if (Array.isArray(res)) {
      // 兼容旧版直接返回数组的形式
      platforms.value = res
      coupons.value = []
    } else if (res && typeof res === 'object') {
      platforms.value = Array.isArray(res.platforms) ? res.platforms : []
      coupons.value = Array.isArray(res.coupons) ? res.coupons : []
    } else {
      platforms.value = []
      coupons.value = []
    }
  } catch (e) {
    console.error('加载推广数据失败', e)
    platforms.value = []
    coupons.value = []
  }
}

const onBannerChange = (e) => {
  currentBanner.value = e.detail.current
}

const previewBanner = (idx) => {
  if (bannerImages.value.length === 0) return
  uni.previewImage({
    urls: bannerImages.value,
    current: idx
  })
}

const previewShopImage = (idx) => {
  if (shopImages.value.length === 0) return
  uni.previewImage({
    urls: shopImages.value,
    current: idx
  })
}

// 点击推广平台跳转
const handlePromotion = (platform) => {
  uni.navigateTo({
    url: `/pages/promotion/jump?id=${platform.id}&merchantId=${merchantInfo.value?.id}`
  })
}

// 点击优惠券跳转（优先使用 coupon.link 跳转到第三方平台领取）
const handleCouponTap = (coupon) => {
  const link = coupon.link || coupon.thirdPartyUrl || coupon.linkUrl || coupon.externalUrl
  const couponName = coupon.couponName || coupon.customName || coupon.name || coupon.title || '优惠券'
  if (link) {
    // 有第三方跳转链接，直接跳转到中转页再打开外部链接
    uni.navigateTo({
      url: `/pages/promotion/jump?type=coupon&name=${encodeURIComponent(couponName)}&url=${encodeURIComponent(link)}&merchantId=${merchantInfo.value?.id}`
    })
  } else {
    // 无跳转链接，走推广配置详情
    uni.navigateTo({
      url: `/pages/promotion/jump?id=${coupon.id}&merchantId=${merchantInfo.value?.id}`
    })
  }
}

const handleAddWechat = () => {
  const wechat = merchantInfo.value?.bossWechat
  if (!wechat) {
    uni.showToast({ title: '老板未配置微信', icon: 'none' })
    return
  }
  uni.setClipboardData({
    data: wechat,
    success: () => {
      uni.showModal({
        title: '加老板微信',
        content: `老板微信号已复制：${wechat}\n请打开微信搜索添加`,
        confirmText: '去添加',
        showCancel: false
      })
    }
  })
}

const goToWifi = () => {
  uni.navigateTo({
    url: `/pages/wifi/index?merchantId=${merchantInfo.value?.id}`
  })
}

const callPhone = () => {
  if (merchantInfo.value?.contactPhone) {
    uni.makePhoneCall({
      phoneNumber: merchantInfo.value.contactPhone
    })
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.merchant-detail {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 40rpx;
}

.banner-section {
  position: relative;
  width: 100%;
  height: 420rpx;
  background: $gradient-primary;
  overflow: hidden;
}

.banner-swiper {
  width: 100%;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gradient-primary;
}

.banner-logo-fallback {
  width: 200rpx;
  height: 200rpx;
  border-radius: $border-radius;
  opacity: 0.7;
}

.banner-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.55));
  pointer-events: none;
}

.merchant-brief {
  position: absolute;
  bottom: 30rpx;
  left: $spacing-lg;
  right: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  z-index: 2;
}

.shop-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius;
  background: $bg-card;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

.shop-info {
  flex: 1;
  color: $text-white;
  min-width: 0;
}

.shop-name {
  font-size: $font-size-xl;
  font-weight: bold;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-meta {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-top: 6rpx;
}

.meta-text {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.9);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section {
  margin: $spacing-md;
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-lg $spacing-md;
  box-shadow: $shadow-sm;
}

.section-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.section-title-bar {
  width: 8rpx;
  height: 32rpx;
  background: $gradient-primary;
  border-radius: 4rpx;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: bold;
  color: $text-primary;
  flex: 1;
}

.section-sub {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.coupon-item {
  display: flex;
  border-radius: $border-radius;
  overflow: hidden;
  box-shadow: $shadow-sm;
  background: $bg-card;
}

.coupon-left {
  width: 200rpx;
  background: $gradient-error;
  padding: $spacing-lg $spacing-sm;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $text-white;
  position: relative;
  flex-shrink: 0;
}

.coupon-value {
  font-size: 48rpx;
  font-weight: bold;
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: $font-size-md;
  margin-right: 4rpx;
}

.coupon-condition {
  font-size: 22rpx;
  opacity: 0.9;
  margin-top: $spacing-xs;
}

.coupon-right {
  flex: 1;
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.coupon-name {
  font-size: $font-size-md;
  font-weight: bold;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-valid {
  font-size: 22rpx;
  color: $text-secondary;
  margin-top: $spacing-xs;
}

.coupon-action {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-sm;
}

.claim-btn {
  padding: $spacing-xs $spacing-lg;
  background: $gradient-error;
  color: $text-white;
  border-radius: $border-radius-full;
  font-size: $font-size-sm;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.95);
  }
}

.claim-btn.disabled {
  background: $text-placeholder;
}

.empty-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx;
  gap: $spacing-sm;
}

.empty-text {
  font-size: 26rpx;
  color: $text-secondary;
}

/* 工具栏 - 使用 flex-wrap 替代 grid */
.tool-grid {
  display: flex;
  flex-wrap: wrap;
}

.tool-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm 0;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.92);
  }
}

.tool-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-sm;
}

.tool-icon.wechat {
  background: linear-gradient(135deg, #07c160 0%, #2aae67 100%);
}

.tool-icon.wifi {
  background: $gradient-success;
}

.tool-icon-text {
  color: $text-white;
  font-size: $font-size-lg;
  font-weight: bold;
}

.tool-name {
  font-size: $font-size-sm;
  color: $text-regular;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140rpx;
}

.info-card {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.info-block {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.info-label {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
}

.info-desc {
  font-size: 26rpx;
  color: $text-regular;
  line-height: 1.8;
}

/* 图片网格 - 使用 flex-wrap 替代 grid */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -6rpx;
}

.image-item {
  width: 33.33%;
  padding: 6rpx;
  box-sizing: border-box;
}

.shop-image {
  width: 100%;
  height: 200rpx;
  border-radius: $border-radius-sm;
  background: $border-color;
}

.contact-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-gray-light;
  border-radius: $border-radius;
}

.contact-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.call-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: $spacing-xs $spacing-md;
  background: rgba(82, 196, 26, 0.1);
  border-radius: $border-radius-full;
}

.call-text {
  font-size: $font-size-sm;
  color: $success-color;
  font-weight: 500;
}

.bottom-safe {
  height: 40rpx;
}

.icon-map-marker-white {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='rgba(255,255,255,0.85)'%3E%3Cpath d='M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-clock-white {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='rgba(255,255,255,0.85)' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cpolyline points='12 6 12 12 16 14'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-phone-primary {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%231677ff'%3E%3Cpath d='M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-phone-success {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%2352c41a'%3E%3Cpath d='M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}
</style>
