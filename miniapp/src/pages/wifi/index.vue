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
            <!-- H5：qrcode npm 生成高清图片二维码（浏览器 canvas，保证可扫） -->
            <image v-if="wifiQrImageUrl" :src="wifiQrImageUrl" class="qr-image" mode="aspectFit"></image>
            <!-- 小程序：u-qrcode canvas 二维码 -->
            <u-qrcode
              v-else-if="wifiQrValue"
              ref="pageQrRef"
              :val="wifiQrValue"
              use-root-height-and-width
              :lv="3"
              :quiet-zone="4"
              background="#ffffff"
              foreground="#000000"
              loading-text="二维码生成中"
              @longpressCallback="onQrLongpress"
            ></u-qrcode>
            <!-- 二维码未生成或生成中 -->
            <view v-else class="qr-placeholder">
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
              <u-icon name="file-text" size="16" color="#1677ff"></u-icon>
            </view>
          </view>
          <view class="info-row">
            <text class="info-label">WiFi密码</text>
            <text class="info-value">{{ showPassword ? wifiInfo.password : '********' }}</text>
            <view class="copy-btn" @tap="togglePassword">
              <u-icon :name="showPassword ? 'eye' : 'eye-fill'" size="16" color="#1677ff"></u-icon>
            </view>
          </view>
          <view class="info-row">
            <text class="info-label">加密方式</text>
            <text class="info-value">{{ wifiInfo.encryption || 'WPA2' }}</text>
          </view>
        </view>

        <view class="action-buttons">
          <button class="action-btn primary" @tap="copyPassword">
            <u-icon name="file-text" color="#fff" size="28"></u-icon>
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

      <!-- WiFi二维码模态框（u-modal 用 show prop 控制，v-model 不生效） -->
      <u-modal :show="showQrModal" title="WiFi二维码" :show-cancel-button="true" @confirm="saveQrImage" @cancel="closeQrModal" @update:show="v => showQrModal = v" confirm-text="保存" cancel-text="关闭">
        <view class="qr-modal-content">
          <view class="qr-modal-box">
            <!-- H5：高清图片二维码 -->
            <image v-if="wifiQrImageUrl" :src="wifiQrImageUrl" class="qr-modal-image" mode="aspectFit"></image>
            <!-- 小程序：u-qrcode canvas 二维码 -->
            <u-qrcode
              v-else-if="wifiQrValue"
              ref="modalQrRef"
              :val="wifiQrValue"
              use-root-height-and-width
              :lv="3"
              :quiet-zone="4"
              background="#ffffff"
              foreground="#000000"
              loading-text="二维码生成中"
            ></u-qrcode>
            <view v-else class="qr-placeholder">
              <u-icon name="scan" color="#1677ff" size="80"></u-icon>
              <text class="qr-tip">WiFi二维码</text>
            </view>
          </view>
          <text class="qr-modal-desc">使用手机系统相机或微信扫描可自动连接到该WiFi</text>
          <view class="qr-modal-info">
            <view class="info-item">
              <text class="info-label">WiFi名称：</text>
              <text class="info-value">{{ wifiInfo.ssid }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">加密方式：</text>
              <text class="info-value">{{ wifiInfo.encryption || 'WPA2' }}</text>
            </view>
          </view>
        </view>
      </u-modal>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantWifi } from '@/api/merchant'
import { generateWifiQrContent, validateWifiInfo } from '@/utils/wifiQrcode'
// #ifdef H5
import QRCode from 'qrcode'
// #endif

const wifiInfo = ref(null)
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')
// WiFi QR 码内容（标准 WIFI: 协议字符串）
const wifiQrValue = ref('')
// H5 端高清图片二维码（qrcode npm 生成）
const wifiQrImageUrl = ref('')
const showQrModal = ref(false)
const pageQrRef = ref(null)
const modalQrRef = ref(null)

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
      // 生成二维码内容，各端各自渲染
      await generateQrCode(res)
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

/**
 * 生成 WiFi 二维码内容（标准格式 WIFI:T:WPA;S:SSID;P:密码;;）
 * H5 额外生成高清图片；小程序由 u-qrcode canvas 渲染
 */
const generateQrCode = async (wifi) => {
  // 验证WiFi信息
  const validation = validateWifiInfo(wifi)
  if (!validation.valid) {
    console.error('WiFi信息验证失败:', validation.error)
    wifiQrValue.value = ''
    wifiQrImageUrl.value = ''
    return
  }

  // 生成WiFi QR码内容
  const qrContent = generateWifiQrContent(
    wifi.ssid,
    wifi.password,
    wifi.encryption || 'WPA2',
    wifi.hidden || false
  )

  console.log('[WiFi QR]', 'Content:', qrContent)
  wifiQrValue.value = qrContent

  // #ifdef H5
  // H5：qrcode npm 生成高清图片（浏览器 canvas 可用，保证可扫码）
  try {
    const dataUrl = await QRCode.toDataURL(qrContent, {
      errorCorrectionLevel: 'H',
      type: 'image/png',
      width: 512,
      margin: 4,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    wifiQrImageUrl.value = dataUrl
    console.log('[WiFi QR]', 'Generated successfully (H5 image)')
  } catch (error) {
    console.error('[WiFi QR] H5 生成失败:', error)
    wifiQrImageUrl.value = ''
  }
  // #endif
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

/**
 * 显示WiFi二维码模态框
 */
const showWifiQr = () => {
  if (!wifiInfo.value?.ssid) {
    uni.showToast({ title: '暂无WiFi信息', icon: 'none' })
    return
  }

  if (!wifiQrValue.value) {
    uni.showToast({ title: '二维码生成中，请稍候...', icon: 'loading' })
    return
  }

  showQrModal.value = true
}

/**
 * 关闭二维码模态框
 */
const closeQrModal = () => {
  showQrModal.value = false
}

/**
 * 从 u-qrcode 画布导出二维码图片（小程序端使用，H5 返回临时文件路径）
 */
const exportQrFile = async () => {
  const qrRef = modalQrRef.value || pageQrRef.value
  if (!qrRef || typeof qrRef.toTempFilePath !== 'function') return ''
  try {
    const res = await new Promise((resolve, reject) => {
      qrRef.toTempFilePath({ success: resolve, fail: reject })
    })
    return res && (res.tempFilePath || res.apFilePath) || ''
  } catch (e) {
    console.error('导出二维码失败', e)
    return ''
  }
}

/**
 * 保存二维码图片（H5 下载，小程序保存到相册）
 */
const saveQrImage = async () => {
  if (!wifiQrValue.value) {
    uni.showToast({ title: '二维码未生成', icon: 'none' })
    return
  }

  try {
    let filePath = ''
    // #ifdef H5
    // H5：直接使用 qrcode npm 生成的 dataURL 图片
    filePath = wifiQrImageUrl.value
    // #endif
    // #ifndef H5
    // 小程序：从 u-qrcode 画布导出
    filePath = await exportQrFile()
    // #endif

    if (!filePath) {
      uni.showToast({ title: '二维码未生成', icon: 'none' })
      return
    }

    // #ifdef H5
    // H5环境下，直接下载
    const link = document.createElement('a')
    link.href = filePath
    link.download = `${wifiInfo.value.ssid}-WiFi.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    uni.showToast({ title: '二维码已下载', icon: 'success' })
    // #endif

    // #ifndef H5
    // 小程序环境下，保存到相册
    uni.saveImageToPhotosAlbum({
      filePath,
      success() {
        uni.showToast({ title: '已保存到相册', icon: 'success' })
      },
      fail() {
        uni.showToast({ title: '保存失败，请检查相册权限', icon: 'none' })
      }
    })
    // #endif
  } catch (error) {
    console.error('保存二维码失败:', error)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

/**
 * 长按页面二维码：保存到相册/下载
 */
const onQrLongpress = () => {
  saveQrImage()
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
  width: 380rpx;
  height: 380rpx;
  margin: 0 auto;
  background: $bg-gray-light;
  border-radius: $border-radius;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.qr-image {
  width: 100%;
  height: 100%;
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

// WiFi二维码模态框样式
.qr-modal-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
}

.qr-modal-box {
  width: 280rpx;
  height: 280rpx;
  border-radius: $border-radius;
  border: 1rpx solid $border-color;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-modal-image {
  width: 100%;
  height: 100%;
  border-radius: $border-radius;
}

.qr-modal-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  text-align: center;
  line-height: 1.6;
}

.qr-modal-info {
  width: 100%;
  background: $bg-gray-light;
  border-radius: $border-radius;
  padding: $spacing-md;
}

.info-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  flex-shrink: 0;
  min-width: 100rpx;
}

.info-value {
  font-size: $font-size-sm;
  color: $text-primary;
  flex: 1;
  text-align: right;
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
  padding: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
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
