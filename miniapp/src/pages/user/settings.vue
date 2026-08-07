<template>
  <view class="page">
    <view class="profile-card" v-if="userStore.isLoggedIn">
      <image class="avatar" :src="userStore.userInfo?.avatar || '/static/avatar.png'" mode="aspectFill"></image>
      <view class="profile-detail">
        <text class="nickname">{{ userStore.userInfo?.nickname || '微信用户' }}</text>
        <text class="phone">{{ userStore.userInfo?.phone || '未绑定手机号' }}</text>
      </view>
    </view>

    <view class="menu-card">
      <view class="menu-item" @tap="clearCache">
        <view class="menu-icon">
          <u-icon name="trash" color="#fa8c16" size="26"></u-icon>
        </view>
        <text class="menu-text">清除缓存</text>
        <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
      </view>

      <view class="menu-item" @tap="goToAbout">
        <view class="menu-icon">
          <u-icon name="info-circle" color="#1677ff" size="26"></u-icon>
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

    <view class="footer-info">
      <text class="version">{{ APP_NAME }} v1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import { APP_NAME } from '@/config/app'

const userStore = useUserStore()

const clearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除本地缓存吗？',
    success: (res) => {
      if (res.confirm) {
        const keys = uni.getStorageInfoSync().keys || []
        keys.forEach(k => {
          // 保留登录态
          if (k !== 'token' && k !== 'userInfo') {
            uni.removeStorageSync(k)
          }
        })
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    }
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
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;
}

.profile-card {
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-xl;
  display: flex;
  align-items: center;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-sm;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: $bg-gray;
}

.profile-detail {
  flex: 1;
  margin-left: $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.nickname {
  font-size: $font-size-xl;
  font-weight: bold;
  color: $text-primary;
}

.phone {
  font-size: $font-size-sm;
  color: $text-secondary;
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
</style>
