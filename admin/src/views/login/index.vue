<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-left-content">
        <div class="gradient-card">
          <div class="brand-area">
            <div class="brand-logo">
              <LikeOutlined />
            </div>
            <h1 class="brand-title">晓居智能</h1>
            <p class="brand-desc">NFC智能推广 · 一键好评 · AI创作</p>
          </div>
          <div class="feature-list">
            <div class="feature-item">
              <QrcodeOutlined />
              <span>NFC/二维码双模式识别</span>
            </div>
            <div class="feature-item">
              <BulbOutlined />
              <span>AI智能生成评价内容</span>
            </div>
            <div class="feature-item">
              <BarChartOutlined />
              <span>多维度数据统计分析</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="login-right">
      <div class="login-form-wrapper">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">管理您的商家与设备</p>
        
        <a-form
          :model="loginForm"
          :rules="rules"
          ref="formRef"
          @finish="handleLogin"
        >
          <a-form-item name="account">
            <a-input
              v-model:value="loginForm.account"
              size="large"
              placeholder="请输入账号"
            >
              <template #prefix>
                <UserOutlined style="color: rgba(0, 0, 0, 0.25)" />
              </template>
            </a-input>
          </a-form-item>
          
          <a-form-item name="password">
            <a-input-password
              v-model:value="loginForm.password"
              size="large"
              placeholder="请输入密码"
              @pressEnter="handleLogin"
            >
              <template #prefix>
                <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
              </template>
            </a-input-password>
          </a-form-item>
          
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </a-button>
          </a-form-item>
        </a-form>
        <p class="login-tips">请输入您的管理员账号和密码进行登录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  UserOutlined, 
  LockOutlined, 
  LikeOutlined,
  QrcodeOutlined,
  BulbOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    loading.value = true
    
    const res = await userStore.login({
      username: loginForm.account,
      password: loginForm.password
    })

    if (res.token && !userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    }

    message.success('登录成功')

    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (e) {
    console.error('登录失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  background: #f0f5ff;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -200px;
  right: -200px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  position: relative;
  z-index: 1;
}

.login-left-content {
  max-width: 480px;
  width: 100%;
  position: relative;
}

.gradient-card {
  background: linear-gradient(135deg, #1677ff 0%, #69b1ff 50%, #722ed1 100%);
  border-radius: 24px;
  padding: 60px 48px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(22, 119, 255, 0.3);
}

.gradient-card::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.gradient-card::after {
  content: '';
  position: absolute;
  bottom: -20%;
  left: -10%;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.brand-area {
  text-align: center;
  margin-bottom: 48px;
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 14px;
  opacity: 0.85;
  letter-spacing: 2px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.95;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateX(4px);
  }
  
  :deep(.anticon) {
    font-size: 20px;
  }
}

.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.login-form-wrapper {
  width: 100%;
  max-width: 380px;
  background: #fff;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 36px;
}

:deep(.ant-input-affix-wrapper-lg) {
  padding: 8.5px 12px;
  border-radius: 10px;
}

:deep(.ant-input-lg) {
  border-radius: 10px;
}

:deep(.ant-btn-lg) {
  height: 46px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
}

.login-tips {
  text-align: center;
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
  
  .login-form-wrapper {
    max-width: 420px;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 20px;
  }
  
  .login-form-wrapper {
    padding: 32px 24px;
  }
  
  .form-title {
    font-size: 24px;
  }
}
</style>
