<template>
  <view class="register-page">
    <view class="page-header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="page-title">一键注册绑定</text>
        <text class="page-desc">绑定商家管理后台账号，享受更多服务</text>
      </view>
    </view>

    <view class="form-section">
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">手机号</text>
          <view class="form-input-wrapper">
            <u-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              border="none"
            ></u-input>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="form-input-wrapper code-wrapper">
            <u-input 
              v-model="form.smsCode" 
              placeholder="请输入验证码"
              type="number"
              maxlength="6"
              border="none"
            ></u-input>
            <view 
              class="send-code-btn" 
              :class="{ disabled: countdown > 0 }"
              @tap="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </view>
          </view>
        </view>

        <view class="divider">
          <text class="divider-text">管理后台账号</text>
        </view>

        <view class="form-item">
          <text class="form-label">管理后台账号</text>
          <view class="form-input-wrapper">
            <u-input 
              v-model="form.adminAccount" 
              placeholder="请输入管理后台账号"
              border="none"
            ></u-input>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">管理后台密码</text>
          <view class="form-input-wrapper">
            <u-input 
              v-model="form.adminPassword" 
              placeholder="请输入管理后台密码"
              :type="showPassword ? 'text' : 'password'"
              border="none"
            ></u-input>
            <view class="password-toggle" @tap="showPassword = !showPassword">
              <u-icon :name="showPassword ? 'eye' : 'eye-fill'" size="28" color="#999"></u-icon>
            </view>
          </view>
        </view>
      </view>

      <view class="tips-card">
        <view class="tips-title">
          <u-icon name="info-circle" size="28" color="#1677ff"></u-icon>
          <text>温馨提示</text>
        </view>
        <view class="tips-content">
          <text>1. 绑定后可享受设备管理、数据统计等增值服务</text>
          <text>2. 请确保您拥有管理后台的账号权限</text>
          <text>3. 如有疑问，请联系管理员</text>
        </view>
      </view>

      <view class="submit-section">
        <button 
          class="submit-btn" 
          :class="{ disabled: !canSubmit || loading }"
          @tap="handleSubmit"
          :loading="loading"
        >
          {{ loading ? '提交中...' : '立即注册绑定' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { registerBind, sendSmsCode as sendSms } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const form = ref({
  phone: '',
  smsCode: '',
  adminAccount: '',
  adminPassword: ''
})

const showPassword = ref(false)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const canSubmit = computed(() => {
  return form.value.phone && 
         form.value.smsCode && 
         form.value.adminAccount && 
         form.value.adminPassword
})

const sendSmsCode = () => {
  if (countdown.value > 0) return
  
  if (!form.value.phone) {
    uni.showToast({
      title: '请输入手机号',
      icon: 'none'
    })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    uni.showToast({
      title: '手机号格式不正确',
      icon: 'none'
    })
    return
  }
  
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  sendSms(form.value.phone).then(() => {
    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })
  }).catch(() => {
    clearInterval(timer)
    countdown.value = 0
  })
}

const handleSubmit = async () => {
  if (!canSubmit.value || loading.value) return
  
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    uni.showToast({
      title: '手机号格式不正确',
      icon: 'none'
    })
    return
  }
  
  loading.value = true
  
  try {
    const res = await registerBind({
      phone: form.value.phone,
      smsCode: form.value.smsCode,
      adminAccount: form.value.adminAccount,
      adminPassword: form.value.adminPassword
    })
    
    if (res.token) {
      userStore.setToken(res.token)
      if (res.userInfo) {
        userStore.setUserInfo(res.userInfo)
      }
    }
    
    uni.showToast({
      title: '绑定成功',
      icon: 'success'
    })
    
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }, 1500)
  } catch (e) {
    console.error('注册绑定失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #f5f6fa;
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
  height: 360rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  border-radius: 0 0 60rpx 60rpx;
}

.header-content {
  position: relative;
  padding: 40rpx 32rpx 80rpx;
  color: #fff;
}

.page-title {
  font-size: 44rpx;
  font-weight: bold;
  display: block;
}

.page-desc {
  font-size: 26rpx;
  opacity: 0.85;
  margin-top: 12rpx;
  display: block;
}

.form-section {
  margin-top: -60rpx;
  padding: 0 24rpx 48rpx;
  position: relative;
  z-index: 10;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 8rpx 32rpx;
}

.form-item {
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 26rpx;
  color: #8c8c8c;
  margin-bottom: 16rpx;
  display: block;
}

.form-input-wrapper {
  display: flex;
  align-items: center;
  position: relative;
}

.code-wrapper {
  justify-content: space-between;
}

.send-code-btn {
  font-size: 26rpx;
  color: #1677ff;
  padding-left: 24rpx;
  border-left: 1rpx solid #f0f0f0;
  white-space: nowrap;
}

.send-code-btn.disabled {
  color: #bfbfbf;
}

.password-toggle {
  padding: 0 16rpx;
}

.divider {
  display: flex;
  align-items: center;
  padding: 24rpx 0 8rpx;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1rpx;
  background: #f0f0f0;
}

.divider-text {
  padding: 0 24rpx;
  font-size: 24rpx;
  color: #bfbfbf;
}

.tips-card {
  background: #e6f4ff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-top: 24rpx;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  font-weight: 500;
  color: #1677ff;
  margin-bottom: 16rpx;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  font-size: 24rpx;
  color: #4096ff;
  line-height: 1.6;
}

.submit-section {
  margin-top: 48rpx;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 48rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn.disabled {
  opacity: 0.6;
}
</style>
