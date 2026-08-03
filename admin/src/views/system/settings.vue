<template>
  <div class="settings-page">
    <div class="page-header">
      <div class="page-title">系统配置</div>
      <div class="page-desc">管理系统域名与URL配置</div>
    </div>
    
    <div class="card-wrapper settings-card">
      <a-form :model="domainSettings" layout="vertical">
        <a-alert type="info" show-icon style="margin-bottom: 16px" message="域名用于生成二维码分享链接、拼接图片相对路径等场景，请确保配置正确" />
        <a-form-item label="网站域名" required>
          <a-input v-model:value="domainSettings.siteUrl" placeholder="如：https://www.example.com" />
          <div class="form-item-tip">不带末尾斜杠的完整 URL，用于生成二维码和拼接资源路径</div>
        </a-form-item>
        <a-form-item label="二维码前缀URL" required>
          <a-input v-model:value="domainSettings.qrcodeUrl" placeholder="如：https://www.example.com/scan" />
          <div class="form-item-tip">生成二维码时的前缀URL，二维码ID将以 ?q=xxx 的形式追加到URL后</div>
        </a-form-item>
        <a-form-item label="设备URL前缀" required>
          <a-input v-model:value="domainSettings.deviceUrl" placeholder="如：https://www.example.com/device" />
          <div class="form-item-tip">设备URL的前缀，设备编号和系统编码将以 ?deviceNo=xxx&code=xxx 的形式追加到URL后</div>
        </a-form-item>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="静态资源前缀">
              <a-input v-model:value="domainSettings.assetPrefix" placeholder="如：https://cdn.example.com" />
              <div class="form-item-tip">CDN 或静态资源域名，图片/视频等资源 URL 前缀</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="API 接口地址">
              <a-input v-model:value="domainSettings.apiUrl" placeholder="如：https://api.example.com" />
              <div class="form-item-tip">后端 API 服务地址</div>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="小程序 AppID">
              <a-input v-model:value="domainSettings.miniappAppId" placeholder="微信小程序 AppID" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="备案号">
              <a-input v-model:value="domainSettings.beianId" placeholder="如：ICP 备案号" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      
      <div class="form-actions">
        <a-space>
          <a-button @click="handleReset">重置</a-button>
          <a-button type="primary" :loading="saving" @click="handleSave">
            保存设置
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getSystemSettings, updateSystemSettings } from '@/api/system'

const saving = ref(false)

const domainSettings = reactive({
  siteUrl: '',
  qrcodeUrl: '',
  deviceUrl: '',
  assetPrefix: '',
  apiUrl: '',
  miniappAppId: '',
  beianId: ''
})

const loadSettings = async () => {
  try {
    const res = await getSystemSettings()
    const data = res || {}
    if (data.domain) {
      Object.assign(domainSettings, data.domain)
    } else {
      Object.assign(domainSettings, {
        siteUrl: data.siteUrl || '',
        qrcodeUrl: data.qrcodeUrl || '',
        deviceUrl: data.deviceUrl || '',
        assetPrefix: data.assetPrefix || '',
        apiUrl: data.apiUrl || '',
        miniappAppId: data.miniappAppId || '',
        beianId: data.beianId || ''
      })
    }
  } catch (e) {
    console.error('加载系统设置失败', e)
  }
}

const handleSave = async () => {
  try {
    saving.value = true
    await updateSystemSettings({
      domain: { ...domainSettings }
    })
    message.success('设置保存成功')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

const handleReset = async () => {
  await loadSettings()
  message.info('已重置为最新配置')
}

onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss" scoped>
.settings-page {
  padding: 24px;
}

.settings-card {
  padding: 20px 24px;
}

.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid $border-color;
  display: flex;
  justify-content: flex-end;
}

.form-item-tip {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
}
</style>
