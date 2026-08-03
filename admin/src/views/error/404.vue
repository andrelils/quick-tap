<template>
  <div class="error-404-page">
    <div class="error-container">
      <div class="error-code">404</div>
      <div class="error-title">页面不存在</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被移除</div>
      <div class="error-actions">
        <a-space>
          <a-button @click="goBack">
            <template #icon><ArrowLeftOutlined /></template>
            返回上一页
          </a-button>
          <a-button type="primary" @click="goHome">
            <template #icon><HomeOutlined /></template>
            返回首页
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeftOutlined, HomeOutlined } from '@ant-design/icons-vue'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.error-404-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.error-container {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #1677ff;
  line-height: 1;
  margin-bottom: 24px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 12px;
}

.error-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  justify-content: center;
}
</style>
