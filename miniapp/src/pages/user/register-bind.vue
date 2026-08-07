<template>
  <view class="register-page">
    <view class="page-header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="page-title">商家入驻</text>
        <text class="page-desc">注册后台账号并绑定设备</text>
      </view>
    </view>

    <view class="form-section">
      <!-- 设备信息卡片 -->
      <view class="device-card">
        <view class="device-icon">
          <view class="icon-device" :style="{ width: '36rpx', height: '36rpx' }"></view>
        </view>
        <view class="device-info">
          <view class="device-row">
            <text class="device-label">设备编码</text>
            <text class="device-value code-text">{{ deviceCode || form.deviceCode || '未识别' }}</text>
          </view>
        </view>
      </view>

      <!-- 手动输入设备信息（无参数时显示） -->
      <view class="form-card" v-if="!deviceCode">
        <view class="section-title">
          <view class="bar"></view>
          <text>设备信息</text>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.deviceCode }">
          <text class="form-label">设备编码 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.deviceCode"
              placeholder="请输入设备编码"
              type="text"
              maxlength="64"
              border="none"
              @blur="validateField('deviceCode')"
              @change="clearError('deviceCode')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.deviceCode">{{ errors.deviceCode }}</text>
        </view>
      </view>

      <view class="form-card">
        <view class="section-title">
          <view class="bar"></view>
          <text>后台账号信息</text>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.username }">
          <text class="form-label">账号 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.username"
              placeholder="请设置后台管理系统账号"
              type="text"
              maxlength="32"
              border="none"
              @blur="validateField('username')"
              @change="clearError('username')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.username">{{ errors.username }}</text>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.password }">
          <text class="form-label">密码 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.password"
              placeholder="请设置登录密码(至少6位)"
              type="password"
              maxlength="32"
              border="none"
              @blur="validateField('password')"
              @change="clearError('password')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.password">{{ errors.password }}</text>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.confirmPassword }">
          <text class="form-label">确认密码 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
              type="password"
              maxlength="32"
              border="none"
              @blur="validateField('confirmPassword')"
              @change="clearError('confirmPassword')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</text>
        </view>

        <view class="section-title">
          <view class="bar"></view>
          <text>店铺信息</text>
        </view>

        <view class="form-item">
          <text class="form-label">用户名 / 昵称</text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.nickname"
              placeholder="请输入昵称(选填,默认同账号)"
              type="text"
              maxlength="32"
              border="none"
            ></u-input>
          </view>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.merchantName }">
          <text class="form-label">店铺名称 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.merchantName"
              placeholder="请输入店铺/商户名称"
              type="text"
              maxlength="50"
              border="none"
              @blur="validateField('merchantName')"
              @change="clearError('merchantName')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.merchantName">{{ errors.merchantName }}</text>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.contactName }">
          <text class="form-label">联系人 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.contactName"
              placeholder="请输入联系人姓名"
              type="text"
              maxlength="20"
              border="none"
              @blur="validateField('contactName')"
              @change="clearError('contactName')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.contactName">{{ errors.contactName }}</text>
        </view>

        <view class="form-item" :class="{ 'has-error': errors.contactPhone }">
          <text class="form-label">联系电话 <text class="required">*</text></text>
          <view class="form-input-wrapper">
            <u-input
              v-model="form.contactPhone"
              placeholder="请输入联系电话"
              type="number"
              maxlength="11"
              border="none"
              @blur="validateField('contactPhone')"
              @change="clearError('contactPhone')"
            ></u-input>
          </view>
          <text class="error-text" v-if="errors.contactPhone">{{ errors.contactPhone }}</text>
        </view>
      </view>

      <view class="tips-card">
        <view class="tips-title">
          <view class="icon-info" :style="{ width: '24rpx', height: '24rpx' }"></view>
          <text>温馨提示</text>
        </view>
        <view class="tips-content">
          <text>1. 账号密码用于登录后台管理系统</text>
          <text>2. 提交后将自动创建商家账号并绑定当前设备</text>
          <text>3. 注册成功后需待管理员审核方可登录后台</text>
        </view>
      </view>

      <view class="submit-section">
        <button
          class="submit-btn"
          :class="{ disabled: !canSubmit || loading }"
          @tap="handleSubmit"
          :loading="loading"
        >
          {{ loading ? '提交中...' : '立即注册' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { registerMerchant } from '@/api/merchant'

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  merchantName: '',
  contactName: '',
  contactPhone: '',
  deviceCode: ''
})

const errors = reactive({})
const deviceCode = ref('')
const loading = ref(false)

const canSubmit = computed(() => {
  const f = form.value
  const hasDevice = deviceCode.value || f.deviceCode
  return !!f.username &&
         !!f.password &&
         !!f.confirmPassword &&
         f.password.length >= 6 &&
         f.password === f.confirmPassword &&
         !!f.merchantName &&
         !!f.contactName &&
         !!f.contactPhone &&
         !!hasDevice
})

onLoad((options) => {
  if (options.code) {
    deviceCode.value = decodeURIComponent(options.code)
  }
})

const clearError = (field) => {
  if (errors[field]) delete errors[field]
}

const validateField = (field) => {
  const f = form.value
  switch (field) {
    case 'deviceCode':
      if (!deviceCode.value && !f.deviceCode) {
        errors.deviceCode = '请输入设备编码'
      } else {
        clearError('deviceCode')
      }
      break
    case 'username':
      if (!f.username) {
        errors.username = '请输入账号'
      } else if (f.username.length < 3) {
        errors.username = '账号至少3个字符'
      } else {
        clearError('username')
      }
      break
    case 'password':
      if (!f.password) {
        errors.password = '请输入密码'
      } else if (f.password.length < 6) {
        errors.password = '密码至少6位'
      } else {
        clearError('password')
      }
      if (f.confirmPassword && f.confirmPassword !== f.password) {
        errors.confirmPassword = '两次密码不一致'
      }
      break
    case 'confirmPassword':
      if (!f.confirmPassword) {
        errors.confirmPassword = '请再次输入密码'
      } else if (f.confirmPassword !== f.password) {
        errors.confirmPassword = '两次密码不一致'
      } else {
        clearError('confirmPassword')
      }
      break
    case 'merchantName':
      if (!f.merchantName) {
        errors.merchantName = '请输入店铺名称'
      } else {
        clearError('merchantName')
      }
      break
    case 'contactName':
      if (!f.contactName) {
        errors.contactName = '请输入联系人姓名'
      } else {
        clearError('contactName')
      }
      break
    case 'contactPhone':
      if (!f.contactPhone) {
        errors.contactPhone = '请输入联系电话'
      } else if (!/^1[3-9]\d{9}$/.test(f.contactPhone)) {
        errors.contactPhone = '手机号格式不正确'
      } else {
        clearError('contactPhone')
      }
      break
  }
}

const validateAll = () => {
  const fields = ['username', 'password', 'confirmPassword', 'merchantName', 'contactName', 'contactPhone']
  if (!deviceCode.value) fields.push('deviceCode')
  fields.forEach(f => validateField(f))
  return Object.keys(errors).length === 0
}

const handleSubmit = async () => {
  if (loading.value) return

  if (!validateAll()) {
    uni.showToast({ title: '请完善必填信息', icon: 'none' })
    return
  }

  const finalCode = deviceCode.value || form.value.deviceCode

  loading.value = true
  try {
    await registerMerchant({
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname,
      merchantName: form.value.merchantName,
      contactName: form.value.contactName,
      contactPhone: form.value.contactPhone,
      code: finalCode
    })

    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      // 【修改】注册成功后，直接返回应用首页，而不是进入商家详情
      // 在首页会重新检查该设备的绑定情况
      uni.redirectTo({ url: '/pages/index/index' })
    }, 1500)
  } catch (e) {
    console.error('注册失败', e)
    uni.showToast({
      title: e?.message || '注册失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.register-page {
  min-height: 100vh;
  background: $bg-page;
}

.page-header {
  position: relative;
  padding-top: 88rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: $header-height;
  background: $gradient-primary;
  border-radius: $header-radius;
}

.header-content {
  position: relative;
  padding: $spacing-lg $spacing-md $spacing-xl;
  color: $text-white;
}

.page-title {
  font-size: $font-size-xl;
  font-weight: bold;
  display: block;
}

.page-desc {
  font-size: $font-size-sm;
  opacity: 0.85;
  margin-top: $spacing-sm;
  display: block;
}

.form-section {
  margin-top: -30rpx;
  padding: 0 $spacing-md $spacing-xl;
  position: relative;
  z-index: $z-card;
}

.device-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-sm;
  border-left: 8rpx solid $primary-color;
}

.device-icon {
  width: 80rpx;
  height: 80rpx;
  background: $bg-info;
  border-radius: $border-radius;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-device {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%231677ff'%3E%3Cpath d='M3 3h8v8H3zm2 2v4h4V5H5zm8-2h8v8h-8zm2 2v4h4V5h-4zM3 13h8v8H3zm2 2v4h4v-4H5zm16-2h-6v2h2v6h2v-6h2v-2zm-4-2h2v2h-2zm0-4h2v2h-2z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  display: inline-block;
}

.device-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  min-width: 0;
}

.device-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.device-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  flex-shrink: 0;
}

.device-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 500;
}

.code-text {
  font-family: monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-card {
  background: $bg-card;
  border-radius: $border-radius-lg;
  padding: $spacing-md $spacing-lg;
  box-shadow: $shadow-sm;
  margin-bottom: $spacing-lg;
}

.section-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md 0 $spacing-sm;
  font-size: $font-size-md;
  font-weight: bold;
  color: $text-primary;

  .bar {
    width: 6rpx;
    height: 28rpx;
    background: $primary-color;
    border-radius: 4rpx;
  }
}

.form-item {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
  display: block;

  .required {
    color: $error-color;
    margin-left: 4rpx;
  }
}

.form-item.has-error {
  .form-input-wrapper {
    border-bottom: 2rpx solid $error-color;
    padding-bottom: 4rpx;
  }
}

.error-text {
  display: block;
  font-size: $font-size-xs;
  color: $error-color;
  margin-top: $spacing-xs;
  line-height: 1.4;
}

.form-input-wrapper {
  display: flex;
  align-items: center;
}

.tips-card {
  background: $bg-info;
  border-radius: $border-radius;
  padding: $spacing-md;
  margin-top: $spacing-md;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $primary-color;
  margin-bottom: $spacing-sm;
}

.icon-info {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%231677ff'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  display: inline-block;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $primary-color-light;
  line-height: 1.6;
}

.submit-section {
  margin-top: $spacing-xl;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: $gradient-primary;
  color: $text-white;
  font-size: $font-size-lg;
  font-weight: bold;
  border-radius: $border-radius-full;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.submit-btn.disabled {
  opacity: 0.6;
}
</style>
