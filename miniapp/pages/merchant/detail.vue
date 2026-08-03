<template>
  <view class="merchant-detail">
    <view class="merchant-banner" v-if="merchantInfo">
      <view class="banner-mask"></view>
      <view class="merchant-brief">
        <image class="shop-logo" :src="merchantInfo.shopLogo || '/static/logo.png'" mode="aspectFill"></image>
        <view class="shop-info">
          <text class="shop-name">{{ merchantInfo.shopName }}</text>
          <view class="shop-meta">
            <view class="meta-item">
              <u-icon name="map-marker" size="20" color="rgba(255,255,255,0.8)"></u-icon>
              <text>{{ merchantInfo.shopAddress || '暂无地址' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="content-tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'promotion' }"
        @tap="activeTab = 'promotion'"
      >
        <text>一键推广</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'info' }"
        @tap="activeTab = 'info'"
      >
        <text>商家介绍</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'coupon' }"
        @tap="activeTab = 'coupon'"
      >
        <text>优惠券</text>
      </view>
    </view>

    <view class="tab-content">
      <view v-show="activeTab === 'promotion'" class="promotion-panel">
        <view class="panel-tip">
          <u-icon name="info-circle" size="28" color="#1677ff"></u-icon>
          <text>点击下方平台图标，一键跳转到对应平台给商家好评</text>
        </view>
        
        <view class="promotion-grid">
          <view 
            v-for="platform in platforms" 
            :key="platform.id" 
            class="promo-card"
            @tap="handlePromotion(platform)"
          >
            <view class="promo-icon" :style="{ backgroundColor: getPlatformColor(platform.platformCode) }">
              <text class="promo-icon-text">{{ platform.platformName.charAt(0) }}</text>
            </view>
            <text class="promo-name">{{ platform.platformName }}</text>
            <view class="promo-arrow">
              <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
            </view>
          </view>
        </view>

        <view class="wifi-card" @tap="goToWifi" v-if="wifiInfo">
          <view class="wifi-left">
            <view class="wifi-icon">
              <u-icon name="wifi" color="#fff" size="32"></u-icon>
            </view>
            <view class="wifi-info">
              <text class="wifi-label">免费WiFi</text>
              <text class="wifi-ssid">{{ wifiInfo.ssid }}</text>
            </view>
          </view>
          <view class="wifi-action">
            <text class="wifi-btn">一键连接</text>
            <u-icon name="arrow-right" size="20" color="#52c41a"></u-icon>
          </view>
        </view>
      </view>

      <view v-show="activeTab === 'info'" class="info-panel">
        <view class="info-section" v-if="merchantInfo?.shopDesc">
          <text class="info-title">店铺简介</text>
          <text class="info-desc">{{ merchantInfo.shopDesc }}</text>
        </view>

        <view class="info-section" v-if="shopImages.length > 0">
          <text class="info-title">店铺图片</text>
          <view class="image-grid">
            <image 
              v-for="(img, idx) in shopImages" 
              :key="idx" 
              :src="img" 
              mode="aspectFill"
              class="shop-image"
              @tap="previewImage(idx)"
            ></image>
          </view>
        </view>

        <view class="info-section">
          <text class="info-title">联系我们</text>
          <view class="contact-list">
            <view class="contact-item" v-if="merchantInfo?.phone">
              <view class="contact-icon">
                <u-icon name="phone" size="24" color="#1677ff"></u-icon>
              </view>
              <text class="contact-label">联系电话</text>
              <text class="contact-value">{{ merchantInfo.phone }}</text>
              <view class="contact-action" @tap="callPhone">
                <u-icon name="phone-fill" size="28" color="#52c41a"></u-icon>
              </view>
            </view>
            <view class="contact-item" v-if="merchantInfo?.shopAddress">
              <view class="contact-icon">
                <u-icon name="map-marker" size="24" color="#faad14"></u-icon>
              </view>
              <text class="contact-label">店铺地址</text>
              <text class="contact-value">{{ merchantInfo.shopAddress }}</text>
              <view class="contact-action">
                <u-icon name="location" size="28" color="#1677ff"></u-icon>
              </view>
            </view>
            <view class="contact-item" v-if="merchantInfo?.businessHours">
              <view class="contact-icon">
                <u-icon name="clock" size="24" color="#722ed1"></u-icon>
              </view>
              <text class="contact-label">营业时间</text>
              <text class="contact-value">{{ merchantInfo.businessHours }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-show="activeTab === 'coupon'" class="coupon-panel">
        <view class="coupon-list" v-if="coupons.length > 0">
          <view 
            v-for="coupon in coupons" 
            :key="coupon.id" 
            class="coupon-item"
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
              <text class="coupon-valid">
                {{ formatDate(coupon.validStartTime) }} - {{ formatDate(coupon.validEndTime) }}
              </text>
              <view class="coupon-action">
                <view 
                  class="claim-btn" 
                  :class="{ disabled: coupon.issuedCount >= coupon.totalCount }"
                  @tap="handleClaimCoupon(coupon)"
                >
                  {{ coupon.issuedCount >= coupon.totalCount ? '已领完' : '立即领取' }}
                </view>
              </view>
            </view>
          </view>
        </view>
        <view class="empty-coupon" v-else>
          <u-icon name="coupon" size="64" color="#d9d9d9"></u-icon>
          <text class="empty-text">暂无优惠券</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAppStore } from '@/store/app'
import { getMerchantInfo, getMerchantWifi } from '@/api/merchant'
import { getPromotionPlatforms } from '@/api/promotion'
import { getCouponList, claimCoupon } from '@/api/coupon'

const appStore = useAppStore()

const merchantInfo = ref(null)
const wifiInfo = ref(null)
const platforms = ref([])
const coupons = ref([])
const activeTab = ref('promotion')

const shopImages = computed(() => {
  if (!merchantInfo.value?.shopImages) return []
  try {
    return JSON.parse(merchantInfo.value.shopImages)
  } catch {
    return []
  }
})

onLoad((options) => {
  const { merchantId, deviceId } = options
  if (merchantId) {
    loadMerchantInfo(merchantId)
    loadPlatforms(merchantId)
    loadWifi(merchantId)
    loadCoupons(merchantId)
  }
})

const loadMerchantInfo = async (merchantId) => {
  try {
    const res = await getMerchantInfo(merchantId)
    merchantInfo.value = res
    appStore.setCurrentMerchant(res)
  } catch (e) {
    console.error('加载商家信息失败', e)
  }
}

const loadPlatforms = async (merchantId) => {
  try {
    const res = await getPromotionPlatforms(merchantId)
    platforms.value = res || []
  } catch (e) {
    console.error('加载推广平台失败', e)
  }
}

const loadWifi = async (merchantId) => {
  try {
    const res = await getMerchantWifi(merchantId)
    wifiInfo.value = res
  } catch (e) {
    console.error('加载WiFi信息失败', e)
  }
}

const loadCoupons = async (merchantId) => {
  try {
    const res = await getCouponList(merchantId)
    coupons.value = res || []
  } catch (e) {
    console.error('加载优惠券失败', e)
  }
}

const platformColors = {
  douyin: '#fe2c55',
  kuaishou: '#ff4906',
  xiaohongshu: '#ff2442',
  dianping: '#ff6600',
  meituan: '#ffd100',
  weibo: '#e6162d',
  bilibili: '#fb7299'
}

const getPlatformColor = (code) => {
  return platformColors[code] || '#1677ff'
}

const handlePromotion = (platform) => {
  uni.navigateTo({
    url: `/pages/promotion/jump?platformId=${platform.id}&merchantId=${merchantInfo.value?.id}`
  })
}

const goToWifi = () => {
  uni.navigateTo({
    url: `/pages/wifi/index?merchantId=${merchantInfo.value?.id}`
  })
}

const handleClaimCoupon = async (coupon) => {
  if (coupon.issuedCount >= coupon.totalCount) return
  
  try {
    await claimCoupon(coupon.id)
    uni.showToast({
      title: '领取成功',
      icon: 'success'
    })
    loadCoupons(merchantInfo.value?.id)
  } catch (e) {
    console.error('领取优惠券失败', e)
  }
}

const previewImage = (index) => {
  uni.previewImage({
    urls: shopImages.value,
    current: index
  })
}

const callPhone = () => {
  if (merchantInfo.value?.phone) {
    uni.makePhoneCall({
      phoneNumber: merchantInfo.value.phone
    })
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.merchant-detail {
  min-height: 100vh;
  background: #f5f6fa;
}

.merchant-banner {
  position: relative;
  height: 320rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  display: flex;
  align-items: flex-end;
  padding: 0 32rpx 40rpx;
}

.banner-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.1));
}

.merchant-brief {
  display: flex;
  align-items: center;
  gap: 24rpx;
  position: relative;
  z-index: 1;
}

.shop-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 4rpx solid rgba(255,255,255,0.5);
}

.shop-info {
  flex: 1;
  color: #fff;
}

.shop-name {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}

.shop-meta {
  margin-top: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  opacity: 0.9;
}

.content-tabs {
  display: flex;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1rpx solid #f0f0f0;
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
  color: #1677ff;
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
  background: #1677ff;
  border-radius: 3rpx;
}

.tab-content {
  padding: 24rpx;
}

.panel-tip {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  background: #e6f4ff;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  font-size: 24rpx;
  color: #1677ff;
  line-height: 1.5;
}

.promotion-grid {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.promo-card {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.promo-card:last-child {
  border-bottom: none;
}

.promo-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
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
  flex: 1;
  margin-left: 24rpx;
  font-size: 30rpx;
  color: #1f1f1f;
  font-weight: 500;
}

.promo-arrow {
  margin-left: auto;
}

.wifi-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.wifi-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.wifi-icon {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wifi-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.wifi-label {
  font-size: 28rpx;
  color: #1f1f1f;
  font-weight: 500;
}

.wifi-ssid {
  font-size: 24rpx;
  color: #8c8c8c;
}

.wifi-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.wifi-btn {
  font-size: 26rpx;
  color: #52c41a;
  font-weight: 500;
}

.info-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.info-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
  margin-bottom: 20rpx;
  display: block;
}

.info-desc {
  font-size: 26rpx;
  color: #595959;
  line-height: 1.8;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.shop-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12rpx;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: #fafafa;
  border-radius: 16rpx;
}

.contact-icon {
  width: 64rpx;
  height: 64rpx;
  background: #f0f0f0;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.contact-label {
  font-size: 26rpx;
  color: #8c8c8c;
  width: 120rpx;
}

.contact-value {
  flex: 1;
  font-size: 28rpx;
  color: #1f1f1f;
}

.contact-action {
  padding: 8rpx;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.coupon-item {
  background: #fff;
  border-radius: 20rpx;
  display: flex;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.coupon-left {
  width: 200rpx;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  padding: 32rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
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
  font-size: 28rpx;
  margin-right: 4rpx;
}

.coupon-condition {
  font-size: 22rpx;
  opacity: 0.9;
  margin-top: 8rpx;
}

.coupon-right {
  flex: 1;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.coupon-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.coupon-valid {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
}

.coupon-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 16rpx;
}

.claim-btn {
  padding: 12rpx 32rpx;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: #fff;
  border-radius: 32rpx;
  font-size: 24rpx;
}

.claim-btn.disabled {
  background: #d9d9d9;
}

.empty-coupon {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx;
  gap: 16rpx;
  background: #fff;
  border-radius: 24rpx;
}

.empty-text {
  font-size: 26rpx;
  color: #8c8c8c;
}
</style>
