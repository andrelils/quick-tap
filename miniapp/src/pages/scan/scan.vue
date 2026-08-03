<template>
  <view class="scan-page">
    <view class="scan-header">
      <text class="scan-title">扫一扫</text>
    </view>
    <view class="scan-content">
      <view class="scan-area">
        <view class="scan-box">
          <view class="scan-line"></view>
          <view class="scan-corner top-left"></view>
          <view class="scan-corner top-right"></view>
          <view class="scan-corner bottom-left"></view>
          <view class="scan-corner bottom-right"></view>
        </view>
        <text class="scan-tip">将二维码/条码放入框内，即可自动扫描</text>
      </view>
    </view>
    <view class="scan-footer">
      <view class="scan-btn" @tap="handleScan">
        <text>模拟扫描</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const handleScan = () => {
  uni.showToast({
    title: '扫描功能开发中',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.scan-page {
  min-height: 100vh;
  background: #000;
  display: flex;
  flex-direction: column;
}

.scan-header {
  padding: 20rpx 30rpx;
  background: #000;
}

.scan-title {
  font-size: $font-size-lg;
  color: $text-white;
  font-weight: 500;
}

.scan-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xl;
}

.scan-box {
  width: 500rpx;
  height: 500rpx;
  position: relative;
  background: transparent;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  border-radius: $border-radius;
}

.scan-line {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, $primary-color, transparent);
  animation: scan 2s ease-in-out infinite;
}

@keyframes scan {
  0%, 100% { transform: translateY(-100rpx); }
  50% { transform: translateY(100rpx); }
}

.scan-corner {
  position: absolute;
  width: 40rpx;
  height: 40rpx;
  border-color: $primary-color;
  border-style: solid;
  border-width: 0;
}

.top-left {
  top: -2rpx;
  left: -2rpx;
  border-top-width: 6rpx;
  border-left-width: 6rpx;
  border-top-left-radius: $border-radius;
}

.top-right {
  top: -2rpx;
  right: -2rpx;
  border-top-width: 6rpx;
  border-right-width: 6rpx;
  border-top-right-radius: $border-radius;
}

.bottom-left {
  bottom: -2rpx;
  left: -2rpx;
  border-bottom-width: 6rpx;
  border-left-width: 6rpx;
  border-bottom-left-radius: $border-radius;
}

.bottom-right {
  bottom: -2rpx;
  right: -2rpx;
  border-bottom-width: 6rpx;
  border-right-width: 6rpx;
  border-bottom-right-radius: $border-radius;
}

.scan-tip {
  font-size: $font-size-md;
  color: rgba(255, 255, 255, 0.6);
}

.scan-footer {
  padding: $spacing-xl;
  display: flex;
  justify-content: center;
}

.scan-btn {
  padding: $spacing-lg 80rpx;
  background: $primary-color;
  border-radius: $border-radius-full;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: $font-size-md;
    color: $text-white;
  }
}
</style>
