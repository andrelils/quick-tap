<template>
  <view class="mine-page">
    <view class="user-header">
      <view class="header-bg"></view>
      <view class="user-info" v-if="userStore.isLoggedIn">
        <image class="avatar" :src="userStore.userInfo?.avatar || '/static/avatar.png'" mode="aspectFill"></image>
        <view class="user-detail">
          <text class="nickname">{{ userStore.userInfo?.nickname || '微信用户' }}</text>
          <text class="phone" v-if="userStore.userInfo?.phone">{{ userStore.userInfo.phone }}</text>
          <view class="bind-tag" v-if="userStore.userInfo?.adminAccount">
            <u-icon name="checkmark-circle" size="20" color="#52c41a"></u-icon>
            <text>已绑定管理账号</text>
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
        <view class="menu-item" @tap="goToRegisterBind" v-if="!userStore.userInfo?.adminAccount">
          <view class="menu-icon">
            <u-icon name="link" color="#1677ff" size="28"></u-icon>
          </view>
          <text class="menu-text">绑定管理后台账号</text>
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
            <u-icon name="chart-pie" color="#722ed1" size="28"></u-icon>
          </view>
          <text class="menu-text">推广记录</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @tap="goToScanHistory">
          <view class="menu-icon">
            <u-icon name="clock" color="#13c2c2" size="28"></u-icon>
          </view>
          <text class="menu-text">扫描记录</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @tap="goToSettings">
          <view class="menu-icon">
            <u-icon name="setting" color="#8c8c8c" size="28"></u-icon>
          </view>
          <text class="menu-text">设置</text>
          <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @tap="goToAbout">
          <view class="menu-icon">
            <u-icon name="info-circle" color="#8c8c8c" size="28"></u-icon>
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
      <text class="version">晓居智能 v1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const stats = ref({
  totalScans: 0,
  totalPromotions: 0,
  coupons: 0
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
  // TODO: 调用接口加载统计数据
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
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToMyCoupons = () => {
  uni.navigateTo({
    url: '/pages/coupon/list'
  })
}

const goToPromotionHistory = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToScanHistory = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToSettings = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: '晓居智能系统 v1.0.0\n\nNFC智能推广，一键好评，助力商家成长',
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
.mine-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.user-header {
  position: relative;
  padding-top: 88rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 380rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  border-radius: 0 0 60rpx 60rpx;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  padding: 40rpx 32rpx;
  z-index: 1;
}

.login-prompt {
  cursor: pointer;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: #fff;
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
  margin-left: 24rpx;
  color: #fff;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}

.phone {
  font-size: 26rpx;
  opacity: 0.85;
  margin-top: 8rpx;
  display: block;
}

.bind-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 12rpx;
  padding: 6rpx 16rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 20rpx;
  font-size: 22rpx;
  opacity: 0.9;
}

.stats-card {
  margin: -80rpx 24rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 0;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 10;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #1f1f1f;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
  display: block;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: #f0f0f0;
}

.menu-section {
  padding: 0 24rpx;
}

.menu-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #1f1f1f;
}

.menu-badge {
  font-size: 24rpx;
  color: #ff4d4f;
  margin-right: 12rpx;
}

.menu-item.logout {
  justify-content: center;
}

.logout-text {
  color: #ff4d4f;
  font-size: 30rpx;
  font-weight: 500;
}

.footer-info {
  text-align: center;
  padding: 40rpx 0;
}

.version {
  font-size: 24rpx;
  color: #bfbfbf;
}
</style>
