<template>
  <view class="promotion-jump">
    <view class="jump-header">
      <view class="platform-icon" :style="{ backgroundColor: platformColor }">
        <text class="icon-text">{{ platformName.charAt(0) }}</text>
      </view>
      <text class="platform-name">{{ platformName }}</text>
      <text class="jump-desc" v-if="platformDesc">{{ platformDesc }}</text>
      <text class="jump-desc" v-else>正在跳转到 {{ platformName }}...</text>
    </view>

    <view class="action-section">
      <!-- URL Scheme 模式：小程序中尝试唤起 APP -->
      <button v-if="jumpMode === 'scheme' && schemeUrl" class="action-btn primary" open-type="launchApp" @tap="handleSchemeJump">
        打开{{ platformName }}APP
      </button>

      <!-- H5/webview 模式：直接打开链接 -->
      <button v-if="(jumpMode === 'webview' || jumpMode === 'scheme') && webUrl" class="action-btn primary" @tap="openWebUrl">
        在浏览器中打开
      </button>

      <!-- 小程序跳转模式 -->
      <button v-if="jumpMode === 'miniprogram' && miniprogramAppid" class="action-btn primary" @tap="navigateToMiniProgram">
        跳转到{{ platformName }}小程序
      </button>

      <!-- 复制链接模式 -->
      <button v-if="jumpMode === 'copy' || fallbackUrl" class="action-btn secondary" @tap="copyLink">
        复制链接，在浏览器中打开
      </button>

      <view class="link-box" v-if="currentLink">
        <text class="link-label">链接地址</text>
        <text class="link-url">{{ currentLink }}</text>
      </view>
    </view>

    <view class="tips-section">
      <view class="tip-title">
        <text class="tip-icon">i</text>
        <text>温馨提示</text>
      </view>
      <view class="tip-list">
        <text>1. 点击上方按钮可跳转到对应平台</text>
        <text>2. 若跳转失败，请复制链接在浏览器中打开</text>
        <text>3. 感谢您对商家的支持与好评</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPromotionPlatformDetail, logPromotionClick } from '@/api/promotion'

const platformId = ref(null)
const merchantId = ref(null)
const platformName = ref('推广平台')
const platformColor = ref('#1677ff')
const platformDesc = ref('')
const jumpMode = ref('scheme')
const schemeUrl = ref('')
const webUrl = ref('')
const fallbackUrl = ref('')
const miniprogramAppid = ref('')
const miniprogramPath = ref('')

const currentLink = computed(() => {
  return webUrl.value || fallbackUrl.value || schemeUrl.value || ''
})

onLoad((options) => {
  const { id, platformId: pid, merchantId: mid } = options
  platformId.value = id || pid
  merchantId.value = mid

  if (platformId.value) {
    loadPlatformInfo()
    recordClick()
  }
})

const loadPlatformInfo = async () => {
  try {
    const res = await getPromotionPlatformDetail(platformId.value)
    if (res) {
      platformName.value = res.name || '推广平台'
      platformColor.value = res.color || '#1677ff'
      platformDesc.value = res.description || ''
      jumpMode.value = res.jumpMode || 'scheme'
      schemeUrl.value = res.schemeUrl || ''
      webUrl.value = res.webUrl || ''
      fallbackUrl.value = res.fallbackUrl || ''
      miniprogramAppid.value = res.miniprogramAppid || ''
      miniprogramPath.value = res.miniprogramPath || ''
    }
  } catch (e) {
    console.error('加载平台信息失败', e)
    uni.showToast({
      title: '加载平台信息失败',
      icon: 'none'
    })
  }
}

const recordClick = async () => {
  try {
    await logPromotionClick({
      platformId: platformId.value,
      merchantId: merchantId.value
    })
  } catch (e) {
    // 静默失败
    console.error('记录点击失败', e)
  }
}

const handleSchemeJump = () => {
  if (schemeUrl.value) {
    // #ifdef H5
    window.location.href = schemeUrl.value
    // #endif
    // #ifdef MP-WEIXIN
    uni.showModal({
      title: '提示',
      content: '小程序内无法直接唤起APP，请点击"复制链接"后在浏览器中打开',
      showCancel: true,
      confirmText: '复制链接',
      success: (res) => {
        if (res.confirm) {
          copyLink()
        }
      }
    })
    // #endif
  } else {
    uni.showToast({
      title: '跳转链接未配置',
      icon: 'none'
    })
  }
}

const openWebUrl = () => {
  const url = webUrl.value || fallbackUrl.value
  if (!url) {
    uni.showToast({
      title: '链接未配置',
      icon: 'none'
    })
    return
  }
  // #ifdef H5
  window.location.href = url
  // #endif
  // #ifdef MP-WEIXIN
  uni.showModal({
    title: '提示',
    content: '小程序内无法直接打开外部链接，请点击"复制链接"后在浏览器中打开',
    showCancel: true,
    confirmText: '复制链接',
    success: (res) => {
      if (res.confirm) {
        copyLink()
      }
    }
  })
  // #endif
}

const navigateToMiniProgram = () => {
  // #ifdef MP-WEIXIN
  if (miniprogramAppid.value) {
    wx.navigateToMiniProgram({
      appId: miniprogramAppid.value,
      path: miniprogramPath.value,
      success: () => {
        console.log('跳转小程序成功')
      },
      fail: (err) => {
        console.error('跳转小程序失败', err)
        uni.showToast({
          title: '跳转失败，请复制链接',
          icon: 'none'
        })
      }
    })
  } else {
    copyLink()
  }
  // #endif
}

const copyLink = () => {
  const link = currentLink.value
  if (!link) {
    uni.showToast({
      title: '暂无可复制链接',
      icon: 'none'
    })
    return
  }
  uni.setClipboardData({
    data: link,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.promotion-jump {
  min-height: 100vh;
  background: linear-gradient(180deg, $bg-info 0%, $bg-page 30%);
  padding: $spacing-xl $spacing-md;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.jump-header {
  text-align: center;
  margin-bottom: 60rpx;
}

.platform-icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: $border-radius-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto $spacing-md;
}

.icon-text {
  color: $text-white;
  font-size: 56rpx;
  font-weight: bold;
}

.platform-name {
  font-size: $font-size-xl;
  font-weight: bold;
  color: $text-primary;
  display: block;
}

.jump-desc {
  font-size: 26rpx;
  color: $text-secondary;
  margin-top: $spacing-sm;
  display: block;
}

.action-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.action-btn {
  width: 100%;
  height: 96rpx;
  border-radius: $border-radius-full;
  font-size: $font-size-md;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn.primary {
  background: $gradient-primary;
  color: $text-white;
}

.action-btn.secondary {
  background: $bg-card;
  color: $primary-color;
  border: 2rpx solid $primary-color;
}

.link-box {
  background: $bg-card;
  border-radius: $border-radius;
  padding: $spacing-md;
  margin-top: $spacing-sm;
}

.link-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
  display: block;
}

.link-url {
  font-size: 26rpx;
  color: $text-regular;
  word-break: break-all;
  line-height: 1.5;
}

.tips-section {
  width: 100%;
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $shadow-sm;
}

.tip-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: $font-size-md;
  font-weight: bold;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.tip-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: $primary-color;
  color: $text-white;
  font-size: 24rpx;
  font-weight: bold;
  font-style: italic;
}

.tip-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  font-size: 26rpx;
  color: $text-regular;
  line-height: 1.6;
}
</style>
