<template>
  <view class="page">
    <view class="card-list" v-if="isLoggedIn">
      <view v-for="item in list" :key="item.id" class="card">
        <view class="card-icon">
          <u-icon name="clock" color="#13c2c2" size="30"></u-icon>
        </view>
        <view class="card-main">
          <text class="card-title">{{ item.merchantName || '未知商家' }}</text>
          <text class="card-sub" v-if="item.deviceName">设备：{{ item.deviceName }}（{{ item.deviceNo }}）</text>
          <text class="card-sub" v-else-if="item.deviceNo">设备：{{ item.deviceNo }}</text>
          <text class="card-time">{{ formatTime(item.createdAt) }}</text>
        </view>
      </view>

      <view class="empty-state" v-if="list.length === 0">
        <u-icon name="clock" size="64" color="#d9d9d9"></u-icon>
        <text class="empty-text">暂无扫描记录</text>
        <text class="empty-desc">扫描设备二维码或 NFC 后即可查看</text>
      </view>
    </view>

    <view class="empty-state" v-else @tap="goToLogin">
      <view class="login-icon">
        <u-icon name="lock" size="56" color="#faad14"></u-icon>
      </view>
      <text class="empty-text">登录后查看扫描记录</text>
      <text class="empty-desc">点击去登录 ></text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyScanLogs } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const list = ref([])

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/user/mine' })
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(String(time).replace(/-/g, '/'))
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onLoad(async () => {
  if (!isLoggedIn.value) return
  try {
    list.value = (await getMyScanLogs()) || []
  } catch (e) {
    console.error('加载扫描记录失败', e)
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.card {
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  box-shadow: $shadow-sm;
}

.card-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: $border-radius;
  background: $bg-gray;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;
  flex-shrink: 0;
}

.card-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-title {
  font-size: $font-size-lg;
  font-weight: bold;
  color: $text-primary;
}

.card-sub {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.card-time {
  font-size: 22rpx;
  color: $text-placeholder;
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
  color: $text-placeholder;
  margin-top: $spacing-sm;
}
</style>
