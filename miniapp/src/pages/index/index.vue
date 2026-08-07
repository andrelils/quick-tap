<template>
  <view class="entry-page">
    <view class="entry-content">
      <view class="logo-icon">
        <view class="icon-scan" :style="{ width: '80rpx', height: '80rpx' }"></view>
      </view>
      <text class="app-title">碰一碰好评卡</text>
      <text class="app-subtitle">智能推广 · 一键好评</text>
      <view class="loading-dots">
        <view class="dot"></view>
        <view class="dot"></view>
        <view class="dot"></view>
      </view>
      <text class="status-text">{{ statusText }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { checkMerchantBind } from '@/api/merchant'

const statusText = ref('正在识别设备...')
const hasDevice = ref(false)  // 是否识别到设备
const deviceCode = ref('')  // 设备编码

/**
 * 解析URL参数，支持多种来源
 */
const parseUrlParams = () => {
  const params = {}

  // 1. 从 hash 中解析: #/pages/index/index?code=xxx
  const hash = window.location.hash
  if (hash && hash.includes('?')) {
    const hashQuery = hash.split('?')[1]
    const urlParams = new URLSearchParams(hashQuery)
    for (const [key, value] of urlParams) {
      params[key] = value
    }
  }

  // 2. 从 search 中解析: ?code=xxx#/pages/index/index
  const search = window.location.search
  if (search && search.startsWith('?')) {
    const searchParams = new URLSearchParams(search)
    for (const [key, value] of searchParams) {
      if (!params[key]) params[key] = value
    }
  }

  // 3. 从完整 URL 中提取（处理 hash 路由参数丢失情况）
  const fullUrl = window.location.href
  const codeMatch = fullUrl.match(/[?&](?:code|deviceNo|q)=([^&#]+)/)
  if (codeMatch && !params.code) {
    params.code = decodeURIComponent(codeMatch[1])
  }

  return params
}

/**
 * 主要逻辑：检查设备信息
 * 1. 如果有设备信息，检查是否已绑定商家
 *    - 已绑定：跳转到商家详情页
 *    - 未绑定：跳转到注册页
 * 2. 如果没有设备信息：停留在首页，显示提示信息
 */
const processEntry = async () => {
  statusText.value = '正在识别设备...'

  // 先尝试 onLoad 传入的参数，再从 URL 解析
  const onLoadOptions = window.__onLoadOptions || {}
  const urlParams = parseUrlParams()
  const mergedOptions = { ...onLoadOptions, ...urlParams }

  // 提取 code（支持多种字段名）
  const code = mergedOptions.code || mergedOptions.deviceNo || mergedOptions.q || ''

  console.log('[INDEX] URL参数:', urlParams)
  console.log('[INDEX] onLoad参数:', onLoadOptions)
  console.log('[INDEX] 最终code:', code)

  if (!code) {
    // 【重要修改】未识别到设备，停留在首页，不跳转
    statusText.value = '请通过NFC碰一碰或扫描二维码进入'
    hasDevice.value = false
    deviceCode.value = ''
    console.log('[INDEX] 未识别到设备，停留在首页')
    return
  }

  // 有设备编码，标记为已识别
  hasDevice.value = true
  deviceCode.value = code

  try {
    statusText.value = '正在校验绑定信息...'
    const res = await checkMerchantBind({ code })
    console.log('[INDEX] 绑定校验结果:', res)

    if (res && res.bound && res.merchantId) {
      // 设备已绑定，跳转到商家详情页
      statusText.value = '正在进入商家页...'
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/merchant/detail?merchantId=${res.merchantId}`
        })
      }, 500)
    } else {
      // 设备未绑定，跳转到注册页
      statusText.value = '设备未绑定，前往注册页'
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/user/register-bind?code=${encodeURIComponent(code)}`
        })
      }, 800)
    }
  } catch (e) {
    console.error('[INDEX] 绑定校验失败:', e)
    // 【修改】校验失败时，也跳转到注册页让用户完成绑定
    statusText.value = '前往注册绑定设备'
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/user/register-bind?code=${encodeURIComponent(code)}`
      })
    }, 800)
  }
}

onLoad((options) => {
  console.log('[INDEX] onLoad options:', JSON.stringify(options))
  // 保存 onLoad 参数供 processEntry 使用
  window.__onLoadOptions = options || {}
})

onMounted(() => {
  console.log('[INDEX] onMounted, hash:', window.location.hash)
  // 确保 DOM 已就绪后再执行
  setTimeout(() => {
    processEntry()
  }, 100)
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.entry-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #69c0ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-lg;
}

.entry-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
}

.logo-icon {
  width: 160rpx;
  height: 160rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-lg;
  backdrop-filter: blur(10px);
}

.icon-scan {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffffff'%3E%3Cpath d='M9.5 6.5v3h-3v-3h3m1-1h-5v5h5v-5zm5 6v3h-3v-3h3m1-1h-5v5h5v-5zm-11 6h10v1.5h-10v-1.5z'/%3E%3Cpath d='M17.5 15.5v-2h-1.5v1.5h-1.5v1.5h-1.5v1.5h-1.5v-1.5h-1.5v1.5H6v-1.5h1.5v-1.5H6v-1.5h1.5v1.5h1.5v-1.5h1.5v-1.5h3v-1.5h-3v1.5h-1.5v-1.5H6v-1.5h1.5v1.5h1.5v-1.5h-1.5v-1.5H6v-1.5h1.5v1.5h1.5v-1.5h1.5v-1.5h1.5v1.5h1.5v-1.5h1.5v-1.5h-1.5v1.5h1.5v-1.5h1.5v-1.5h1.5v1.5h1.5v1.5h-1.5v1.5h1.5v-1.5h-1.5v-1.5h-1.5v-1.5h1.5v-1.5h1.5v-1.5h1.5v-1.5h-1.5v1.5h1.5v-1.5h1.5v-1.5h1.5v-1.5h-1.5v-1.5h1.5v-1.5h-1.5v-1.5h-1.5v-1.5h-1.5v-1.5h1.5v-1.5h1.5v-1.5h1.5v1.5h-1.5v1.5h1.5v-1.5h-1.5v-1.5h1.5v-1.5h-1.5v1.5h-1.5v-1.5h-1.5v-1.5h1.5v-1.5h1.5v-1.5h1.5v1.5h-1.5v1.5h1.5v-1.5h-1.5v-1.5h1.5v1.5h1.5v1.5h-1.5v1.5h1.5v-1.5h-1.5v-1.5h1.5v-1.5h-1.5v1.5h-1.5v-1.5h1.5v-1.5h1.5v-1.5h1.5v1.5h-1.5v1.5h1.5v-1.5h-1.5v-1.5h-1.5v-1.5h1.5v-1.5h1.5v-1.5h1.5v1.5h-1.5v1.5z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
}

.app-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  letter-spacing: 4rpx;
}

.app-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 2rpx;
}

.loading-dots {
  display: flex;
  gap: 16rpx;
  margin: $spacing-lg 0 $spacing-sm;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ffffff;
  animation: bounce 1.4s infinite ease-in-out both;

  &:nth-child(1) { animation-delay: -0.32s; }
  &:nth-child(2) { animation-delay: -0.16s; }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.status-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 1rpx;
}
</style>
