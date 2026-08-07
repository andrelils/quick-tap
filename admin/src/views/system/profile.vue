<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="page-title">个人中心</div>
      <div class="page-desc">管理您的个人信息</div>
    </div>
    
    <a-row :gutter="24">
      <a-col :xs="24" :sm="24" :md="8" :lg="8">
        <div class="card-wrapper profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <a-avatar :size="100" :src="userInfo.avatar || '/vite.svg'" />
              <a-upload
                ref="avatarUploadRef"
                :action="`${apiBaseUrl}/user/avatar`"
                :headers="uploadHeaders"
                :show-upload-list="false"
                @change="handleAvatarChange"
                style="display: none"
              >
                <template #default>
                  <input type="file" />
                </template>
              </a-upload>
              <div class="avatar-clickable" @dblclick="triggerAvatarUpload" style="position: absolute; width: 100px; height: 100px; cursor: pointer; top: 0; left: 0;"></div>
            </div>
            <div class="user-name">{{ userInfo.username }}</div>
          <div class="user-role">{{ roleTextMap[userInfo.role] || userInfo.role }}</div>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.loginDays || 128 }}</div>
              <div class="stat-label">登录天数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.operationCount || 2580 }}</div>
              <div class="stat-label">操作次数</div>
            </div>
          </div>
        </div>
      </a-col>
      
      <a-col :xs="24" :sm="24" :md="16" :lg="16">
        <div class="card-wrapper content-card">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="basic" tab="基本信息">
              <a-form
                :model="formData"
                :rules="formRules"
                ref="formRef"
                layout="vertical"
              >
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="用户名" name="username">
                      <a-input v-model:value="formData.username" disabled />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="昵称" name="nickname">
                      <a-input v-model:value="formData.nickname" placeholder="请输入昵称" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="手机号" name="phone">
                      <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="邮箱" name="email">
                      <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item label="备注">
                  <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注" />
                </a-form-item>
                <div class="form-actions">
                  <a-button type="primary" :loading="saving" @click="handleSave">
                    保存修改
                  </a-button>
                </div>
              </a-form>
            </a-tab-pane>
            
            <a-tab-pane key="password" tab="修改密码">
              <a-form
                :model="passwordForm"
                :rules="passwordRules"
                ref="passwordFormRef"
                layout="vertical"
              >
                <a-form-item label="原密码" name="oldPassword">
                  <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
                </a-form-item>
                <a-form-item label="新密码" name="newPassword">
                  <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
                </a-form-item>
                <a-form-item label="确认新密码" name="confirmPassword">
                  <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
                </a-form-item>
                <div class="form-actions">
                  <a-button type="primary" :loading="passwordSaving" @click="handleChangePassword">
                    确认修改
                  </a-button>
                </div>
              </a-form>
            </a-tab-pane>
            
            <a-tab-pane key="logs" tab="登录日志">
              <a-table
                :columns="logColumns"
                :data-source="logList"
                :pagination="false"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-badge 
                      :status="record.status === 'success' ? 'success' : 'error'" 
                      :text="record.status === 'success' ? '成功' : '失败'" 
                    />
                  </template>
                </template>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getUserInfo, updatePassword, updateUserInfo } from '@/api/auth'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// API配置
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8222/api'
const token = localStorage.getItem('token') || ''
const uploadHeaders = {
  'Authorization': `Bearer ${token}`
}

const activeTab = ref('basic')
const saving = ref(false)
const passwordSaving = ref(false)
const avatarUploading = ref(false)
const formRef = ref()
const passwordFormRef = ref()
const avatarUploadRef = ref()

const userInfo = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  role: '',
  avatar: '',
  loginDays: 0,
  operationCount: 0
})

const roleTextMap = { super_admin: '超级管理员', admin: '管理员', merchant: '商家' }

const formData = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  remark: ''
})

const formRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value) => {
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const logColumns = [
  { title: '登录时间', dataIndex: 'time', key: 'time', width: 180 },
  { title: 'IP地址', dataIndex: 'ip', key: 'ip', width: 140 },
  { title: '登录地点', dataIndex: 'location', key: 'location', width: 160 },
  { title: '设备', dataIndex: 'device', key: 'device', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const logList = ref([])

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    const data = res || {}
    Object.assign(userInfo, {
      username: data.username || data.account || '',
      nickname: data.nickname || data.name || '',
      phone: data.phone || data.mobile || '',
      email: data.email || '',
      role: data.role || '',
      avatar: data.avatar || '',
      loginDays: data.loginDays || 0,
      operationCount: data.operationCount || 0
    })
    Object.assign(formData, {
      username: data.username || data.account || '',
      nickname: data.nickname || data.name || '',
      phone: data.phone || data.mobile || '',
      email: data.email || '',
      remark: data.remark || ''
    })
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const loadLogList = async () => {
  logList.value = []
}

const triggerAvatarUpload = () => {
  if (avatarUploadRef.value?.$el) {
    const input = avatarUploadRef.value.$el.querySelector('input[type=file]')
    if (input) {
      input.click()
    }
  }
}

const handleAvatarChange = async (info) => {
  const { file } = info
  if (file.status === 'done') {
    const response = file.response
    if (response?.data?.url) {
      const avatarUrl = response.data.url
      // 将新头像URL保存到数据库
      try {
        await updateUserInfo({
          avatar: avatarUrl,
          nickname: formData.nickname,
          phone: formData.phone,
          email: formData.email,
          remark: formData.remark
        })
        // 更新本地状态
        userInfo.avatar = avatarUrl
        // 刷新用户信息确保同步
        await userStore.fetchUserInfo?.()
        message.success('头像上传成功')
      } catch (e) {
        console.error('保存头像失败', e)
        message.error('头像保存失败，请重试')
      }
    } else if (response?.message) {
      message.error(response.message)
    }
  } else if (file.status === 'error') {
    message.error('头像上传失败，请重试')
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    await updateUserInfo({
      nickname: formData.nickname,
      phone: formData.phone,
      email: formData.email,
      remark: formData.remark
    })
    await userStore.fetchUserInfo?.()
    message.success('保存成功')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordSaving.value = true
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    console.error('修改密码失败', e)
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadLogList()
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 24px;
}

.profile-card {
  padding: 24px;
  text-align: center;
}

.content-card {
  padding: 20px 24px;
}

.avatar-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid $border-color;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: $text-color;
  margin-top: 16px;
  margin-bottom: 4px;
}

.user-role {
  font-size: 13px;
  color: $text-tertiary;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: $primary-color;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: $text-tertiary;
}

:deep(.ant-tabs-content-holder) {
  padding-top: 8px;
}

.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid $border-color;
  display: flex;
  justify-content: flex-end;
}
</style>
