<template>
  <view class="wifi-page">
    <view class="wifi-card">
      <view class="wifi-icon-wrapper">
        <view class="wifi-icon">
          <u-icon name="wifi" color="#fff" size="64"></u-icon>
        </view>
        <view class="wifi-wave wave1"></view>
        <view class="wifi-wave wave2"></view>
        <view class="wifi-wave wave3"></view>
      </view>
      
      <text class="wifi-title">{{ wifiInfo?.ssid || 'WiFi连接' }}</text>
      
      <view class="wifi-qr-section" v-if="wifiInfo">
        <view class="qr-container">
          <!-- 这里可以放二维码图片 -->
          <view class="qr-placeholder">
            <u-icon name="scan" color="#1677ff" size="80"></u-icon>
            <text class="qr-tip">WiFi二维码</text>
          </view>
        </view>
        <text class="qr-desc">使用系统相机扫码自动连接WiFi</text>
      </view>

      <view class="wifi-info-card" v-if="wifiInfo">
        <view class="info-row">
          <text class="info-label">WiFi名称</text>
          <text class="info-value">{{ wifiInfo.ssid }}</text>
          <view class="copy-btn" @tap="copyText(wifiInfo.ssid)">
            <u-icon name="copy" size="24" color="#1677ff"></u-icon>
          </view>
        </view>
        <view class="info-row">
          <text class="info-label">WiFi密码</text>
          <text class="info-value">{{ showPassword ? wifiInfo.password : '********' }}</text>
          <view class="copy-btn" @tap="togglePassword">
            <u-icon :name="showPassword ? 'eye' : 'eye-fill'" size="24" color="#1677ff"></u-icon>
          </view>
        </view>
        <view class="info-row">
          <text class="info-label">加密方式</text>
          <text class="info-value">{{ wifiInfo.encryption || 'WPA' }}</text>
        </view>
      </view>

      <view class="action-buttons">
        <button class="action-btn primary" @tap="copyPassword">
          <u-icon name="copy" color="#fff" size="28"></u-icon>
          <text>复制密码</text>
        </button>
        <button class="action-btn secondary" @tap="showWifiQr">
          <u-icon name="scan" color="#1677ff" size="28"></u-icon>
          <text>生成二维码</text>
        </button>
      </view>
    </view>

    <view class="tips-card">
      <view class="tips-title">
        <u-icon name="info-circle" size="28" color="#1677ff"></u-icon>
        <text>连接说明</text>
      </view>
      <view class="tips-list">
        <view class="tip-item">
          <text class="tip-num">1</text>
          <text>使用手机系统相机扫描上方二维码，可自动连接WiFi</text>
        </view>
        <view class="tip-item">
          <text class="tip-num">2</text>
          <text>也可以手动打开WiFi设置，找到对应名称后输入密码</text>
        </view>
        <view class="tip-item">
          <text class="tip-num">3</text>
          <text>如无法连接，请联系店员确认WiFi是否正常</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantWifi } from '@/api/merchant'

const wifiInfo = ref(null)
const showPassword = ref(false)

onLoad((options) => {
  const { merchantId } = options
  if (merchantId) {
    loadWifiInfo(merchantId)
  }
})

const loadWifiInfo = async (merchantId) => {
  try {
    const res = await getMerchantWifi(merchantId)
    wifiInfo.value = res
  } catch (e) {
    console.error('加载WiFi信息失败', e)
    uni.showToast({
      title: '加载WiFi信息失败',
      icon: 'none'
    })
  }
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const copyText = (text) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success'
      })
    }
  })
}

const copyPassword = () => {
  if (wifiInfo.value?.password) {
    copyText(wifiInfo.value.password)
  }
}

const showWifiQr = () => {
  uni.showToast({
    title: '二维码生成中...',
    icon: 'loading'
  })
}
</script>

<style lang="scss" scoped>
.wifi-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 24rpx;
}

.wifi-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 48rpx 32rpx;
  text-align: center;
}

.wifi-icon-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wifi-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.wifi-wave {
  position: absolute;
  border-radius: 50%;
  border: 4rpx solid #b7eb8f;
  animation: wifiPulse 2s ease-out infinite;
}

.wave1 {
  width: 120rpx;
  height: 120rpx;
  animation-delay: 0s;
}

.wave2 {
  width: 160rpx;
  height: 160rpx;
  animation-delay: 0.6s;
}

.wave3 {
  width: 200rpx;
  height: 200rpx;
  animation-delay: 1.2s;
}

@keyframes wifiPulse {
  0% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.2);
    opacity: 0;
  }
}

.wifi-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f1f1f;
  margin-bottom: 32rpx;
  display: block;
}

.wifi-qr-section {
  margin-bottom: 32rpx;
}

.qr-container {
  width: 320rpx;
  height: 320rpx;
  margin: 0 auto;
  background: #fafafa;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  color: #bfbfbf;
}

.qr-tip {
  font-size: 24rpx;
}

.qr-desc {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 16rpx;
  display: block;
}

.wifi-info-card {
  background: #fafafa;
  border-radius: 16rpx;
  padding: 8rpx 24rpx;
  margin-bottom: 32rpx;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 26rpx;
  color: #8c8c8c;
  width: 140rpx;
}

.info-value {
  flex: 1;
  font-size: 28rpx;
  color: #1f1f1f;
}

.copy-btn {
  padding: 8rpx;
}

.action-buttons {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-size: 28rpx;
  border: none;
}

.action-btn.primary {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.action-btn.secondary {
  background: #e6f4ff;
  color: #1677ff;
}

.tips-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-top: 24rpx;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  color: #1f1f1f;
  margin-bottom: 24rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.tip-num {
  width: 40rpx;
  height: 40rpx;
  background: #e6f4ff;
  color: #1677ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  flex-shrink: 0;
}

.tip-item text:last-child {
  flex: 1;
  font-size: 26rpx;
  color: #595959;
  line-height: 1.6;
}
</style>
