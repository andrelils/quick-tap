<template>
  <view class="page">
    <view class="card-list" v-if="isLoggedIn">
      <view v-for="item in list" :key="item.id" class="card">
        <view class="card-left">
          <view class="card-icon">
            <u-icon name="scan" color="#52c41a" size="30"></u-icon>
          </view>
        </view>
        <view class="card-main">
          <view class="card-title-row">
            <text class="card-title">{{ item.name || item.deviceNo }}</text>
            <text class="tag" :class="item.type === 'nfc' ? 'tag-nfc' : 'tag-qr'">{{ item.type === 'nfc' ? 'NFC' : '二维码' }}</text>
          </view>
          <text class="card-sub">设备编号：{{ item.deviceNo }}</text>
          <text class="card-sub" v-if="item.merchantName">所属商家：{{ item.merchantName }}</text>
          <text class="card-time">绑定时间：{{ formatTime(item.boundAt) }}</text>
        </view>
      </view>

      <view class="empty-state" v-if="list.length === 0">
        <u-icon name="scan" size="64" color="#d9d9d9"></u-icon>
        <text class="empty-text">暂无绑定设备</text>
        <text class="empty-desc">扫描设备二维码或 NFC 碰一碰后自动绑定</text>
      </view>
    </view>

    <view class="empty-state" v-else @tap="goToLogin">
      <view class="login-icon">
        <u-icon name="lock" size="56" color="#faad14"></u-icon>
      </view>
      <text class="empty-text">登录后查看我的设备</text>
      <text class="empty-desc">点击去登录 ></text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyDevices } from '@/api/user'
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
    list.value = (await getMyDevices()) || []
  } catch (e) {
    console.error('加载设备失败', e)
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

.card-left {
  margin-right: $spacing-md;
}

.card-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: $border-radius;
  background: $bg-gray;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.card-title {
  font-size: $font-size-lg;
  font-weight: bold;
  color: $text-primary;
}

.tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: $border-radius-full;
}

.tag-nfc {
  background: rgba(82, 196, 26, 0.12);
  color: #52c41a;
}

.tag-qr {
  background: rgba(22, 119, 255, 0.12);
  color: #1677ff;
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
