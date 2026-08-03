<template>
  <view class="promotion-jump">
    <!-- 加载中 -->
    <view class="state-card" v-if="loading">
      <u-icon name="loading" size="56" color="#1677ff"></u-icon>
      <text class="state-text">正在加载...</text>
    </view>

    <!-- 加载失败 / 无配置 -->
    <view class="state-card" v-else-if="loadError">
      <view class="state-icon">
        <u-icon name="info-circle" size="64" color="#d9d9d9"></u-icon>
      </view>
      <text class="state-title">{{ loadError }}</text>
      <button class="action-btn secondary retry-btn" @tap="retryLoad">重新加载</button>
    </view>

    <!-- 优惠券详情视图 -->
    <template v-else-if="configType === 'coupon'">
      <view class="jump-header">
        <view class="platform-icon coupon-bg">
          <text class="icon-text">¥</text>
        </view>
        <text class="platform-name">{{ platformName }}</text>
        <text class="jump-desc">优惠券详情</text>
      </view>

      <view class="coupon-detail-card" v-if="hasCouponDetail">
        <view class="coupon-detail-value">
          <text class="currency">¥</text>
          <text class="amount">{{ couponValue }}</text>
        </view>
        <view class="coupon-detail-threshold" v-if="couponThreshold > 0">满 {{ couponThreshold }} 元可用</view>
        <view class="coupon-detail-threshold" v-else>无门槛使用</view>
        <view class="coupon-detail-info">
          <view class="info-row">
            <text class="info-label">优惠券名称</text>
            <text class="info-value">{{ platformName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">有效期</text>
            <text class="info-value" v-if="couponValidStart">{{ formatDate(couponValidStart) }} ~ {{ formatDate(couponValidEnd) }}</text>
            <text class="info-value" v-else>长期有效</text>
          </view>
          <view class="info-row">
            <text class="info-label">剩余数量</text>
            <text class="info-value">{{ couponRemainCount }} / {{ couponTotalCount }} 张</text>
          </view>
        </view>
      </view>

      <view class="action-section" v-if="couponLoaded">
        <button class="action-btn primary" @tap="handleClaimCoupon">
          立即领取
        </button>
        <button class="action-btn secondary" @tap="copyCouponName">
          复制券名
        </button>
      </view>

      <view class="tips-section">
        <view class="tip-title">
          <text class="tip-icon">i</text>
          <text>使用说明</text>
        </view>
        <view class="tip-list">
          <text>1. 点击"立即领取"跳转到第三方平台领取优惠券</text>
          <text>2. 跳转后请在第三方平台完成领取流程</text>
          <text>3. 请在有效期内使用，过期作废</text>
        </view>
      </view>
    </template>

    <!-- 推广平台跳转视图 -->
    <template v-else>
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
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPromotionPlatformDetail, logPromotionClick } from '@/api/promotion'

const platformId = ref(null)
const merchantId = ref(null)
const configType = ref('platform') // platform | coupon
const couponLoaded = ref(false)
const hasCouponDetail = ref(false) // 是否有优惠券详细数据（快速跳转路径无详情）
const loading = ref(false)
const loadError = ref('')

const platformName = ref('推广平台')
const platformColor = ref('#1677ff')
const platformDesc = ref('')
const jumpMode = ref('scheme')
const schemeUrl = ref('')
const webUrl = ref('')
const fallbackUrl = ref('')
const miniprogramAppid = ref('')
const miniprogramPath = ref('')

// 优惠券字段
const couponValue = ref(0)
const couponThreshold = ref(0)
const couponTotalCount = ref(0)
const couponRemainCount = ref(0)
const couponValidStart = ref('')
const couponValidEnd = ref('')

const currentLink = computed(() => {
  return webUrl.value || fallbackUrl.value || schemeUrl.value || ''
})

onLoad((options) => {
  const { id, platformId: pid, merchantId: mid, type, name, url } = options
  merchantId.value = mid

  // 兼容旧版：直接传入 url 和 name 的优惠券跳转
  if (type === 'coupon' && url) {
    configType.value = 'coupon'
    platformName.value = name || '领券中心'
    jumpMode.value = 'webview'
    webUrl.value = url
    fallbackUrl.value = url
    couponLoaded.value = true
    hasCouponDetail.value = false
    return
  }

  // 统一通过 id 加载配置详情（支持推广平台和优惠券两种类型）
  platformId.value = id || pid
  if (platformId.value) {
    loadConfigInfo()
    recordClick()
  } else {
    loadError.value = '缺少配置ID，无法加载'
  }
})

const loadConfigInfo = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const res = await getPromotionPlatformDetail(platformId.value)
    if (!res) {
      loadError.value = '配置不存在'
      return
    }

    // 后端返回嵌套结构：{ type, configId, platform: {..., jumpInfo: {...}} }
    // 兼容旧版扁平结构
    const nested = res.platform || res.coupon || res
    const jumpInfo = nested.jumpInfo || {}
    configType.value = res.type || nested.type || 'platform'
    platformName.value = nested.customName || nested.name || nested.couponName || '推广平台'

    if (configType.value === 'coupon') {
      // 优惠券类型
      platformColor.value = '#ff6b6b'
      couponValue.value = Number(nested.couponValue || nested.value || 0)
      couponThreshold.value = Number(nested.couponThreshold || nested.threshold || 0)
      couponTotalCount.value = Number(nested.couponTotalCount || nested.totalCount || 0)
      couponRemainCount.value = Number(nested.couponRemainCount || nested.remainCount || 0)
      couponValidStart.value = nested.couponValidStart || nested.validStart || ''
      couponValidEnd.value = nested.couponValidEnd || nested.validEnd || ''
      // 优惠券可能携带第三方跳转链接
      const couponLink = nested.link || jumpInfo.webUrl || ''
      if (couponLink) {
        webUrl.value = couponLink
        fallbackUrl.value = couponLink
        jumpMode.value = 'webview'
      }
      couponLoaded.value = true
      hasCouponDetail.value = true
    } else {
      // 推广平台类型
      platformColor.value = nested.color || '#1677ff'
      platformDesc.value = nested.description || ''
      jumpMode.value = jumpInfo.jumpMode || nested.jumpMode || 'scheme'
      schemeUrl.value = jumpInfo.scheme || nested.schemeUrl || ''
      webUrl.value = jumpInfo.webUrl || nested.webUrl || ''
      fallbackUrl.value = jumpInfo.fallbackUrl || nested.fallbackUrl || webUrl.value
      miniprogramAppid.value = jumpInfo.miniprogramAppid || nested.miniprogramAppid || ''
      miniprogramPath.value = jumpInfo.miniprogramPath || nested.miniprogramPath || ''
    }
  } catch (e) {
    // request.js 已对业务错误弹出 toast，这里只需设置错误态
    const msg = e?.message || e?.data?.message
    loadError.value = msg || '加载信息失败'
  } finally {
    loading.value = false
  }
}

const retryLoad = () => {
  if (platformId.value) {
    loadConfigInfo()
  }
}

const recordClick = async () => {
  // /promotion/log 后端已支持匿名访问，可安全调用
  try {
    await logPromotionClick({
      platformId: platformId.value,
      merchantId: merchantId.value
    })
  } catch (e) {
    // 静默失败，不影响用户使用
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

// 优惠券操作：跳转到第三方平台领取
const handleClaimCoupon = () => {
  const link = webUrl.value || fallbackUrl.value
  if (!link) {
    uni.showToast({ title: '暂未配置领取链接', icon: 'none' })
    return
  }
  // #ifdef H5
  window.location.href = link
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

const copyCouponName = () => {
  uni.setClipboardData({
    data: platformName.value,
    success: () => {
      uni.showToast({ title: '券名已复制', icon: 'success' })
    }
  })
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
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

.state-card {
  background: $bg-card;
  border-radius: $border-radius-xl;
  padding: 120rpx $spacing-xl;
  text-align: center;
  box-shadow: $shadow-sm;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 60rpx;
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

.retry-btn {
  margin-top: $spacing-lg;
  width: auto;
  padding: 0 $spacing-xl;
  height: 72rpx;
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

/* 优惠券详情样式 */
.coupon-bg {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%) !important;
}

.coupon-detail-card {
  width: 100%;
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-xl $spacing-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-sm;
  text-align: center;
}

.coupon-detail-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  color: #ff4d4f;
  margin-bottom: $spacing-sm;

  .currency {
    font-size: 36rpx;
    font-weight: bold;
  }

  .amount {
    font-size: 96rpx;
    font-weight: bold;
    line-height: 1;
  }
}

.coupon-detail-threshold {
  font-size: 26rpx;
  color: $text-secondary;
  margin-bottom: $spacing-lg;
}

.coupon-detail-info {
  text-align: left;
  padding-top: $spacing-md;
  border-top: 2rpx dashed $border-color;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  font-size: 26rpx;

  .info-label {
    color: $text-secondary;
  }

  .info-value {
    color: $text-primary;
    font-weight: 500;
    text-align: right;
  }
}
</style>
