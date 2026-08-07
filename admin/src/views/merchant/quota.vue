<template>
  <div class="merchant-quota-page">
    <div class="page-header">
      <div class="page-title">额度管理</div>
      <div class="page-desc">管理各商家的存储空间和AI生成额度</div>
    </div>
    
    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="商家名称">
          <a-input 
            v-model:value="searchForm.keyword" 
            placeholder="请输入商家名称"
            style="width: 240px"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
    
    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">
          商家额度列表
          <a-tag color="blue">共 {{ pagination.total }} 家</a-tag>
        </div>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="false"
        :row-key="record => record.id"
        :loading="tableLoading"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'logo'">
            <div class="merchant-logo-wrapper">
              <img v-if="record.logo" :src="getLogoUrl(record.logo)" class="merchant-logo" />
              <div v-else class="merchant-logo-placeholder">{{ record.name?.charAt(0) }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'name'">
            <div class="merchant-name-cell">
              <span class="merchant-name">{{ record.name }}</span>
              <a-tag v-if="record.hasCustomQuota" color="orange" class="custom-tag">自定义</a-tag>
            </div>
          </template>
          <template v-else-if="column.key === 'planName'">
            <a-tag :color="getPlanTagColor(record.planLevel)">{{ record.planName }}</a-tag>
          </template>
          <template v-else-if="column.key === 'storage'">
            <div class="quota-cell">
              <a-progress
                :percent="record.storage.percent"
                :stroke-color="getProgressColor(record.storage.percent)"
                :show-info="false"
                size="small"
              />
              <div class="quota-text">
                {{ formatQuota(record.storage.used, 'MB') }} / 
                <span :class="{ 'unlimited-text': record.storage.unlimited }">
                  {{ record.storage.unlimited ? '不限' : formatQuota(record.storage.limit, 'MB') }}
                </span>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'textQuota'">
            <div class="quota-cell simple">
              <div class="quota-text">
                {{ record.textQuota.used }} 次 / 
                <span :class="{ 'unlimited-text': record.textQuota.unlimited }">
                  {{ record.textQuota.unlimited ? '不限' : `${record.textQuota.total} 次` }}
                </span>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'imageQuota'">
            <div class="quota-cell simple">
              <div class="quota-text">
                {{ record.imageQuota.used }} 次 / 
                <span :class="{ 'unlimited-text': record.imageQuota.unlimited }">
                  {{ record.imageQuota.unlimited ? '不限' : `${record.imageQuota.total} 次` }}
                </span>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'videoQuota'">
            <div class="quota-cell simple">
              <div class="quota-text">
                {{ record.videoQuota.used }} 次 / 
                <span :class="{ 'unlimited-text': record.videoQuota.unlimited }">
                  {{ record.videoQuota.unlimited ? '不限' : `${record.videoQuota.total} 次` }}
                </span>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleAdjust(record)">
              调整额度
            </a-button>
          </template>
        </template>
      </a-table>
      
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-size-options="['10', '20', '50', '100']"
          show-size-changer
          show-quick-jumper
          :show-total="(total) => '共 ' + total + ' 条'"
          @change="handlePageChange"
        />
      </div>
    </div>
    
    <a-modal
      v-model:open="modalVisible"
      title="调整商家额度"
      width="520px"
      :footer="null"
      destroy-on-close
      @ok="handleSubmit"
    >
      <a-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
      >
        <div class="merchant-info-bar">
          <img v-if="currentMerchant?.logo" :src="getLogoUrl(currentMerchant.logo)" class="info-logo" />
          <div v-else class="info-logo-placeholder">{{ currentMerchant?.name?.charAt(0) }}</div>
          <div class="info-text">
            <div class="info-name">{{ currentMerchant?.name }}</div>
            <div class="info-plan">当前套餐：{{ currentMerchant?.planName }}</div>
          </div>
        </div>
        
        <a-divider style="margin: 16px 0" />
        
        <a-form-item label="存储上限 (MB)">
          <a-input-number 
            v-model:value="formData.storageLimit" 
            :min="0"
            style="width: 100%"
            placeholder="0 表示不限，留空则使用套餐默认值"
          />
          <div class="form-tip">当前套餐默认：{{ currentMerchant?.storage?.unlimited ? '不限' : `${currentMerchant?.storage?.limit} MB` }}</div>
        </a-form-item>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="文字生成额度 (次)">
              <a-input-number 
                v-model:value="formData.textQuota" 
                :min="0"
                style="width: 100%"
                placeholder="0 表示不限，留空则使用套餐默认值"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图片生成额度 (次)">
              <a-input-number 
                v-model:value="formData.imageQuota" 
                :min="0"
                style="width: 100%"
                placeholder="0 表示不限，留空则使用套餐默认值"
              />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="视频生成额度 (次)">
          <a-input-number 
            v-model:value="formData.videoQuota" 
            :min="0"
            style="width: 100%"
            placeholder="0 表示不限，留空则使用套餐默认值"
          />
        </a-form-item>
        
        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleConfirm">
            确定调整
          </a-button>
        </div>
      </a-form>
    </a-modal>

    <!-- 二次确认弹窗 -->
    <a-modal
      v-model:open="confirmVisible"
      title="确认额度变更"
      width="440px"
      :footer="null"
      destroy-on-close
    >
      <a-alert
        type="warning"
        show-icon
        message="请确认以下额度变更信息"
        style="margin-bottom: 16px"
      />
      <a-descriptions :column="1" size="small" bordered>
        <a-descriptions-item label="商家名称">{{ currentMerchant?.name }}</a-descriptions-item>
        <a-descriptions-item label="存储上限" v-if="formData.storageLimit !== null">{{ formData.storageLimit }} MB</a-descriptions-item>
        <a-descriptions-item label="文字生成额度" v-if="formData.textQuota !== null">{{ formData.textQuota }} 次</a-descriptions-item>
        <a-descriptions-item label="图片生成额度" v-if="formData.imageQuota !== null">{{ formData.imageQuota }} 次</a-descriptions-item>
        <a-descriptions-item label="视频生成额度" v-if="formData.videoQuota !== null">{{ formData.videoQuota }} 次</a-descriptions-item>
      </a-descriptions>
      <div class="modal-footer">
        <a-button @click="confirmVisible = false">取消</a-button>
        <a-button type="primary" danger :loading="submitting" @click="handleSubmit">
          确认变更
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getMerchantQuotaList, updateMerchantQuota } from '@/api/merchant'

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: 'Logo', dataIndex: 'logo', key: 'logo', width: 80 },
  { title: '商家名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '当前套餐', dataIndex: 'planName', key: 'planName', width: 120 },
  { title: '存储空间', dataIndex: 'storage', key: 'storage', width: 200 },
  { title: '文字生成额度', dataIndex: 'textQuota', key: 'textQuota', width: 160 },
  { title: '图片生成额度', dataIndex: 'imageQuota', key: 'imageQuota', width: 160 },
  { title: '视频生成额度', dataIndex: 'videoQuota', key: 'videoQuota', width: 160 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 100, fixed: 'right' }
]

const dataSource = ref([])
const tableLoading = ref(false)
const modalVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentMerchant = ref(null)

const formData = reactive({
  storageLimit: null,
  textQuota: null,
  imageQuota: null,
  videoQuota: null
})

const formRules = {}

const getPlanTagColor = (level) => {
  const colorMap = {
    basic: 'green',
    pro: 'blue',
    enterprise: 'purple'
  }
  return colorMap[level] || 'default'
}

const getLogoUrl = (logo) => {
  if (!logo) return ''
  if (logo.startsWith('http://') || logo.startsWith('https://')) return logo
  return `${import.meta.env.VITE_FILE_SERVER_URL || 'http://154.8.138.48:3000'}${logo}`
}

const getProgressColor = (percent) => {
  if (percent >= 90) return '#ff4d4f'
  if (percent >= 70) return '#faad14'
  return '#1677ff'
}

const formatQuota = (value, unit) => {
  if (value >= 1024) {
    return `${(value / 1024).toFixed(2)} GB`
  }
  return `${value} ${unit}`
}

const loadData = async () => {
  try {
    tableLoading.value = true
    const res = await getMerchantQuotaList({
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined
    })
    const list = res?.list || []
    dataSource.value = list.map(item => ({
      ...item,
      storage: item.storage || { limit: 0, used: 0, unlimited: true, percent: 0 },
      textQuota: item.textQuota || { total: 0, used: 0, unlimited: false },
      imageQuota: item.imageQuota || { total: 0, used: 0, unlimited: false },
      videoQuota: item.videoQuota || { total: 0, used: 0, unlimited: false }
    }))
    pagination.total = res?.total || 0
  } catch (e) {
    console.error('加载额度列表失败', e)
    message.error('加载失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.current = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleAdjust = (record) => {
  currentMerchant.value = record
  formData.storageLimit = null
  formData.textQuota = null
  formData.imageQuota = null
  formData.videoQuota = null
  modalVisible.value = true
}

const confirmVisible = ref(false)

const handleConfirm = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  // 检查是否有任何变更
  const hasChange = formData.storageLimit !== null || formData.textQuota !== null ||
    formData.imageQuota !== null || formData.videoQuota !== null
  if (!hasChange) {
    message.warning('请至少修改一项额度')
    return
  }
  modalVisible.value = false
  confirmVisible.value = true
}

const handleSubmit = async () => {
  try {
    submitting.value = true
    const payload = {}
    if (formData.storageLimit !== null) {
      payload.storageLimit = formData.storageLimit
    }
    if (formData.textQuota !== null) {
      payload.textQuota = formData.textQuota
    }
    if (formData.imageQuota !== null) {
      payload.imageQuota = formData.imageQuota
    }
    if (formData.videoQuota !== null) {
      payload.videoQuota = formData.videoQuota
    }
    await updateMerchantQuota(currentMerchant.value.id, payload)
    message.success('额度调整成功')
    confirmVisible.value = false
    loadData()
  } catch (e) {
    console.error('额度调整失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.merchant-quota-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: $text-color;
    margin-bottom: 8px;
  }
  
  .page-desc {
    font-size: 14px;
    color: $text-tertiary;
  }
}

.search-card {
  padding: 20px 24px;
  margin-bottom: 16px;
}

.table-card {
  padding: 20px 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-color;
  display: flex;
  align-items: center;
  gap: 12px;
}

.merchant-logo-wrapper {
  display: flex;
  align-items: center;
}

.merchant-logo {
  width: 40px;
  height: 40px;
  border-radius: $border-radius-sm;
  object-fit: cover;
  background: #f5f5f5;
}

.merchant-logo-placeholder {
  width: 40px;
  height: 40px;
  border-radius: $border-radius-sm;
  background: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: $primary-color;
}

.merchant-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.merchant-name {
  font-weight: 500;
  color: $text-color;
}

.custom-tag {
  font-size: 11px;
}

.quota-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  
  &.simple {
    gap: 0;
  }
}

.quota-text {
  font-size: 12px;
  color: $text-secondary;
}

.unlimited-text {
  color: $success-color;
  font-weight: 500;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.merchant-info-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: $border-radius-sm;
}

.info-logo {
  width: 48px;
  height: 48px;
  border-radius: $border-radius-sm;
  object-fit: cover;
}

.info-logo-placeholder {
  width: 48px;
  height: 48px;
  border-radius: $border-radius-sm;
  background: #e6f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  color: $primary-color;
}

.info-text {
  flex: 1;
}

.info-name {
  font-size: 15px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 4px;
}

.info-plan {
  font-size: 13px;
  color: $text-tertiary;
}

.form-tip {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid $border-color;
}
</style>
