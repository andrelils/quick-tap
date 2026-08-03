<template>
  <view class="wifi-page">
    <!-- 加载中 -->
    <view class="state-card" v-if="loading">
      <u-icon name="loading" size="56" color="#1677ff"></u-icon>
      <text class="state-text">正在加载WiFi信息...</text>
    </view>

    <!-- 无效参数 / 无WiFi配置 -->
    <view class="state-card" v-else-if="!wifiInfo || !wifiInfo.ssid">
      <view class="state-icon">
        <u-icon name="wifi" size="64" color="#d9d9d9"></u-icon>
      </view>
      <text class="state-title">{{ errorMessage || '暂未配置WiFi信息' }}</text>
      <text class="state-desc" v-if="wifiInfo && !wifiInfo.ssid">商家尚未配置WiFi，请联系店员获取</text>
      <text class="state-desc" v-else>请通过NFC碰一碰或扫码进入商家页面</text>
    </view>

    <!-- 正常展示 -->
    <template v-else>
      <view class="wifi-card">
        <view class="wifi-icon-wrapper">
          <view class="wifi-icon">
            <u-icon name="wifi" color="#fff" size="64"></u-icon>
          </view>
          <view class="wifi-wave wave1"></view>
          <view class="wifi-wave wave2"></view>
          <view class="wifi-wave wave3"></view>
        </view>

        <text class="wifi-title">{{ wifiInfo.ssid }}</text>

        <view class="wifi-qr-section">
          <view class="qr-container">
            <view class="qr-placeholder">
              <u-icon name="scan" color="#1677ff" size="80"></u-icon>
              <text class="qr-tip">WiFi二维码</text>
            </view>
          </view>
          <text class="qr-desc">使用系统相机扫码自动连接WiFi</text>
        </view>

        <view class="wifi-info-card">
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
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantWifi } from '@/api/merchant'

const wifiInfo = ref(null)
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

onLoad((options) => {
  const { merchantId } = options
  if (!merchantId) {
    errorMessage.value = '未识别到商家信息'
    return
  }
  loadWifiInfo(merchantId)
})

const loadWifiInfo = async (merchantId) => {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await getMerchantWifi(merchantId)
    if (res && res.ssid) {
      wifiInfo.value = res
    } else {
      // 接口返回成功但 SSID 为空，说明商家未配置 WiFi
      wifiInfo.value = res || {}
    }
  } catch (e) {
    console.error('加载WiFi信息失败', e)
    errorMessage.value = 'WiFi信息加载失败'
  } finally {
    loading.value = false
  }
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const copyText = (text) => {
  if (!text) {
    uni.showToast({ title: '内容为空', icon: 'none' })
    return
  }
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
  } else {
    uni.showToast({ title: '暂无密码信息', icon: 'none' })
  }
}

const showWifiQr = () => {
  if (!wifiInfo.value?.ssid) {
    uni.showToast({ title: '暂无WiFi信息', icon: 'none' })
    return
  }
  uni.showToast({
    title: '二维码生成中...',
    icon: 'loading'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.wifi-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;
}

.state-card {
  background: $bg-card;
  border-radius: $border-radius-xl;
  padding: 120rpx $spacing-xl;
  text-align: center;
  box-shadow: $shadow-sm;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.state-icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: $bg-gray-light;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-md;
}

.state-title {
  font-size: $font-size-lg;
  color: $text-primary;
  font-weight: 500;
  margin-top: $spacing-md;
}

.state-text {
  font-size: $font-size-md;
  color: $text-secondary;
  margin-top: $spacing-md;
}

.state-desc {
  font-size: $font-size-sm;
  color: $text-placeholder;
  margin-top: $spacing-sm;
}

.wifi-card {
  background: $bg-card;
  border-radius: $border-radius-xl;
  padding: $spacing-xl $spacing-md;
  text-align: center;
  box-shadow: $shadow-sm;
}

.wifi-icon-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wifi-icon {
  width: 120rpx;
  height: 120rpx;
  background: $gradient-success;
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
  font-size: $font-size-xl;
  font-weight: bold;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  display: block;
}

.wifi-qr-section {
  margin-bottom: $spacing-lg;
}

.qr-container {
  width: 320rpx;
  height: 320rpx;
  margin: 0 auto;
  background: $bg-gray-light;
  border-radius: $border-radius;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  color: $text-placeholder;
}

.qr-tip {
  font-size: $font-size-sm;
}

.qr-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-sm;
  display: block;
}

.wifi-info-card {
  background: $bg-gray-light;
  border-radius: $border-radius;
  padding: $spacing-xs $spacing-md;
  margin-bottom: $spacing-lg;
}

.info-row {
  display: flex;
  align-items: center;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  width: 140rpx;
}

.info-value {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.copy-btn {
  padding: $spacing-xs;
}

.action-buttons {
  display: flex;
  gap: $spacing-md;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: $border-radius-full;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  font-size: $font-size-md;
  border: none;
}

.action-btn.primary {
  background: $gradient-success;
  color: $text-white;
}

.action-btn.secondary {
  background: $bg-info;
  color: $primary-color;
}

.tips-card {
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-top: $spacing-md;
  box-shadow: $shadow-sm;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: $font-size-md;
  font-weight: bold;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;
}

.tip-num {
  width: 40rpx;
  height: 40rpx;
  background: $bg-info;
  color: $primary-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: bold;
  flex-shrink: 0;
}

.tip-item text:last-child {
  flex: 1;
  font-size: 26rpx;
  color: $text-regular;
  line-height: 1.6;
}
</style>
