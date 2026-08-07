<template>
  <view class="mine-page">
    <view class="user-header" :style="{ paddingTop: headerPadding + 'px' }">
      <view class="header-bg"></view>
      <view class="user-info" v-if="userStore.isLoggedIn">
        <image class="avatar" :src="userStore.userInfo?.avatar || '/static/avatar.png'" mode="aspectFill"></image>
        <view class="user-detail">
          <text class="nickname">{{ userStore.userInfo?.nickname || '微信用户' }}</text>
          <text class="phone" v-if="userStore.userInfo?.phone">{{ userStore.userInfo.phone }}</text>
          <view class="bind-tag" v-if="userStore.userInfo?.phone">
            <u-icon name="checkmark-circle" size="20" color="#52c41a"></u-icon>
            <text>已注册绑定</text>
          </view>
        </view>
      </view>
      <view class="user-info login-prompt" v-else @tap="goToLogin">
        <view class="avatar default-avatar">
          <u-icon name="account" color="#fff" size="48"></u-icon>
        </view>
        <view class="user-detail">
          <text class="nickname">点击登录</text>
          <text class="phone">登录后享受更多服务</text>
        </view>
        <u-icon name="arrow-right" size="24" color="rgba(255,255,255,0.6)"></u-icon>
      </view>
    </view>

    <view class="stats-card" v-if="userStore.isLoggedIn">
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalScans || 0 }}</text>
        <text class="stat-label">扫描次数</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalPromotions || 0 }}</text>
        <text class="stat-label">推广点击</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.coupons || 0 }}</text>
        <text class="stat-label">优惠券</text>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-card">
        <view class="menu-item" @tap="goToRegisterBind" v-if="!userStore.userInfo?.phone">
          <view class="menu-icon">
            <view class="icon-link" :style="{ width: '28rpx', height: '28rpx' }"></view>
          </view>
          <text class="menu-text">注册绑定设备</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @tap="goToMyDevices">
          <view class="menu-icon">
            <u-icon name="scan" color="#52c41a" size="28"></u-icon>
          </view>
          <text class="menu-text">我的设备</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @tap="goToMyCoupons">
          <view class="menu-icon">
            <u-icon name="coupon" color="#faad14" size="28"></u-icon>
          </view>
          <text class="menu-text">我的优惠券</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
          <text class="menu-badge" v-if="stats.coupons > 0">{{ stats.coupons }}张</text>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @tap="goToPromotionHistory">
          <view class="menu-icon">
            <view class="icon-chart-pie" :style="{ width: '28rpx', height: '28rpx' }"></view>
          </view>
          <text class="menu-text">推广记录</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @tap="goToScanHistory">
          <view class="menu-icon">
            <view class="icon-clock-cyan" :style="{ width: '28rpx', height: '28rpx' }"></view>
          </view>
          <text class="menu-text">扫描记录</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @tap="goToSettings">
          <view class="menu-icon">
            <view class="icon-setting" :style="{ width: '28rpx', height: '28rpx' }"></view>
          </view>
          <text class="menu-text">设置</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @tap="goToAbout">
          <view class="menu-icon">
            <view class="icon-info-circle" :style="{ width: '28rpx', height: '28rpx' }"></view>
          </view>
          <text class="menu-text">关于我们</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
      </view>

      <view class="menu-card" v-if="userStore.isLoggedIn">
        <view class="menu-item logout" @tap="handleLogout">
          <text class="menu-text logout-text">退出登录</text>
        </view>
      </view>
    </view>

    <view class="footer-info">
      <text class="version">{{ APP_NAME }} v1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { APP_NAME } from '@/config/app'
import { getMyStats } from '@/api/user'

const userStore = useUserStore()

const stats = ref({
  totalScans: 0,
  totalPromotions: 0,
  coupons: 0
})

const headerPadding = ref(88)

function initNavBar() {
  const sysInfo = uni.getSystemInfoSync()
  const statusBarHeight = sysInfo.statusBarHeight || 20
  try {
    const menuRect = uni.getMenuButtonBoundingClientRect()
    const topPadding = menuRect.bottom + 8
    headerPadding.value = Math.max(topPadding, statusBarHeight + 52)
  } catch (e) {
    headerPadding.value = statusBarHeight + 44
  }
}

onLoad(() => {
  initNavBar()
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadStats()
  }
})

onShow(() => {
  if (userStore.isLoggedIn) {
    loadStats()
  }
})

const loadStats = async () => {
  try {
    const res = await getMyStats()
    stats.value = {
      totalScans: res?.totalScans || 0,
      totalPromotions: res?.totalPromotions || 0,
      coupons: res?.coupons || 0
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const goToLogin = () => {
  // #ifdef MP-WEIXIN
  wx.login({
    success: async (res) => {
      if (res.code) {
        try {
          await userStore.loginByWechat(res.code)
          loadStats()
        } catch (e) {
          uni.showToast({
            title: '登录失败',
            icon: 'none'
          })
        }
      }
    }
  })
  // #endif
  
  // #ifdef H5
  uni.showToast({
    title: '请在微信小程序中使用',
    icon: 'none'
  })
  // #endif
}

const goToRegisterBind = () => {
  uni.navigateTo({
    url: '/pages/user/register-bind'
  })
}

const goToMyDevices = () => {
  uni.navigateTo({
    url: '/pages/user/devices'
  })
}

const goToMyCoupons = () => {
  uni.navigateTo({
    url: '/pages/coupon/list'
  })
}

const goToPromotionHistory = () => {
  uni.navigateTo({
    url: '/pages/user/promotion-history'
  })
}

const goToScanHistory = () => {
  uni.navigateTo({
    url: '/pages/user/scan-history'
  })
}

const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/user/settings'
  })
}

const goToAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: `${APP_NAME}系统 v1.0.0\n\nNFC智能推广，一键好评，助力商家成长`,
    showCancel: false
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.mine-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: $spacing-xl;
}

.user-header {
  position: relative;
  padding-top: 0;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: $header-height-lg;
  background: $gradient-primary;
  border-radius: $header-radius;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  padding: $spacing-lg $spacing-md;
  z-index: 1;
}

.login-prompt {
  transition: opacity 0.2s;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: $bg-card;
  border: 4rpx solid rgba(255,255,255,0.3);
}

.default-avatar {
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-detail {
  flex: 1;
  margin-left: $spacing-md;
  color: $text-white;
}

.nickname {
  font-size: $font-size-xl;
  font-weight: bold;
  display: block;
}

.phone {
  font-size: $font-size-sm;
  opacity: 0.85;
  margin-top: $spacing-xs;
  display: block;
}

.bind-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  margin-top: $spacing-sm;
  padding: 6rpx $spacing-sm;
  background: rgba(255,255,255,0.2);
  border-radius: $border-radius-full;
  font-size: 22rpx;
  opacity: 0.9;
}

.stats-card {
  margin: -80rpx $spacing-md $spacing-md;
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-xl 0;
  display: flex;
  align-items: center;
  position: relative;
  z-index: $z-card;
  box-shadow: $shadow-md;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: bold;
  color: $text-primary;
  display: block;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-xs;
  display: block;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: $border-color;
}

.menu-section {
  padding: 0 $spacing-md;
}

.menu-card {
  background: $bg-card;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $border-color;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: $border-radius;
  background: $bg-gray;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;
}

.menu-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.menu-badge {
  font-size: $font-size-sm;
  color: $error-color;
  margin-right: $spacing-sm;
}

.menu-item.logout {
  justify-content: center;
}

.logout-text {
  color: $error-color;
  font-size: $font-size-lg;
  font-weight: 500;
}

.footer-info {
  text-align: center;
  padding: $spacing-xl 0;
}

.version {
  font-size: $font-size-sm;
  color: $text-placeholder;
}

.icon-link {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%231677ff'%3E%3Cpath d='M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-chart-pie {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23722ed1'%3E%3Cpath d='M11 2v20c-5.07-.5-9-4.79-9-10s3.93-9.5 9-10zm2.03 0v8.99H22c-.47-4.74-4.24-8.52-8.97-8.99zm0 11.01V22c4.74-.47 8.5-4.25 8.97-8.99h-8.97z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-clock-cyan {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2313c2c2' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cpolyline points='12 6 12 12 16 14'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-setting {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%238c8c8c'%3E%3Cpath d='M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 00.12-.61l-1.92-3.32a.488.488 0 00-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 00-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94 0 .31.02.64.07.94l-2.03 1.58a.49.49 0 00-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}

.icon-info-circle {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%238c8c8c'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
  vertical-align: middle;
}
</style>
