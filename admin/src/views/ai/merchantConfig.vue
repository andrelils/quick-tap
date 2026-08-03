<template>
  <div class="merchant-config-page">
    <div class="page-header">
      <div class="page-title">商家配置总览</div>
      <div class="page-desc">查看所有商家的AI创作配置和语料使用情况</div>
    </div>

    <div class="card-wrapper search-card">
      <a-form layout="inline">
        <a-form-item label="商家名称">
          <a-input
            v-model:value="searchKeyword"
            placeholder="请输入商家名称"
            style="width: 200px"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="配置状态">
          <a-select
            v-model:value="statusFilter"
            placeholder="全部"
            style="width: 150px"
            allow-clear
            @change="handleSearch"
          >
            <a-select-option value="configured">已配置</a-select-option>
            <a-select-option value="unconfigured">未配置</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <div class="card-wrapper table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-key="record => record.id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'merchant'">
            <div class="merchant-cell">
              <a-avatar :src="record.logo" size="small" shape="square">
                {{ record.name?.charAt(0) || '商' }}
              </a-avatar>
              <span class="merchant-name">{{ record.name }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'configStatus'">
            <a-tag :color="record.hasConfig ? 'green' : 'default'">
              {{ record.hasConfig ? '已配置' : '未配置' }}
            </a-tag>
            <div v-if="record.hasConfig" class="model-info">
              <span class="model-item">文: {{ record.config?.textModel || '-' }}</span>
              <span class="model-item">图: {{ record.config?.imageModel || '-' }}</span>
              <span v-if="record.config?.videoModel" class="model-item">视: {{ record.config.videoModel }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'textPrompt'">
            <span v-if="record.config?.textPrompt" class="prompt-text" @click="showPromptDetail(record, 'text')">
              {{ truncateText(record.config.textPrompt, 20) }}
              <span class="view-more">查看</span>
            </span>
            <span v-else class="empty-text">-</span>
          </template>
          <template v-else-if="column.key === 'imagePrompt'">
            <span v-if="record.config?.imagePrompt" class="prompt-text" @click="showPromptDetail(record, 'image')">
              {{ truncateText(record.config.imagePrompt, 20) }}
              <span class="view-more">查看</span>
            </span>
            <span v-else class="empty-text">-</span>
          </template>
          <template v-else-if="column.key === 'videoPrompt'">
            <span v-if="record.config?.videoPrompt" class="prompt-text" @click="showPromptDetail(record, 'video')">
              {{ truncateText(record.config.videoPrompt, 20) }}
              <span class="view-more">查看</span>
            </span>
            <span v-else class="empty-text">-</span>
          </template>
          <template v-else-if="column.key === 'corpusCount'">
            <div class="corpus-count">
              <span class="count-item text-count">文: {{ record.corpusStats.textCount }}</span>
              <span class="count-item image-count">图: {{ record.corpusStats.imageCount }}</span>
              <span class="count-item video-count">视: {{ record.corpusStats.videoCount }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'storage'">
            <span class="storage-text">{{ formatSize(record.corpusStats.totalSize) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="showDetail(record)">查看详情</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="detailVisible"
      :title="`${currentMerchant?.name || ''} - 配置详情`"
      :width="720"
      :footer="null"
      class="detail-modal"
    >
      <div v-if="currentMerchant" class="detail-content">
        <div class="detail-section">
          <div class="section-title">商家信息</div>
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="商家名称">{{ currentMerchant.name }}</a-descriptions-item>
            <a-descriptions-item label="商家状态">
              <a-tag :color="currentMerchant.merchantStatus === 1 ? 'green' : 'red'">
                {{ currentMerchant.merchantStatus === 1 ? '正常' : '停用' }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">AI配置状态</div>
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="配置状态">
              <a-tag :color="currentMerchant.hasConfig ? 'green' : 'default'">
                {{ currentMerchant.hasConfig ? '已配置' : '未配置' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="文字模型">
              {{ currentMerchant.config?.textModel || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片模型">
              {{ currentMerchant.config?.imageModel || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="视频模型">
              {{ currentMerchant.config?.videoModel || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">提示词配置</div>
          <div class="prompt-section">
            <div class="prompt-label">文字提示词</div>
            <div class="prompt-content">
              {{ currentMerchant.config?.textPrompt || '未配置' }}
            </div>
          </div>
          <div class="prompt-section">
            <div class="prompt-label">图片提示词</div>
            <div class="prompt-content">
              {{ currentMerchant.config?.imagePrompt || '未配置' }}
            </div>
          </div>
          <div class="prompt-section">
            <div class="prompt-label">视频提示词</div>
            <div class="prompt-content">
              {{ currentMerchant.config?.videoPrompt || '未配置' }}
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">语料统计</div>
          <a-row :gutter="16">
            <a-col :span="8">
              <div class="stat-card text-stat">
                <div class="stat-icon">
                  <FileTextOutlined />
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ currentMerchant.corpusStats.textCount }}</div>
                  <div class="stat-label">文字语料</div>
                </div>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="stat-card image-stat">
                <div class="stat-icon">
                  <PictureOutlined />
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ currentMerchant.corpusStats.imageCount }}</div>
                  <div class="stat-label">图片语料</div>
                </div>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="stat-card video-stat">
                <div class="stat-icon">
                  <VideoCameraOutlined />
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ currentMerchant.corpusStats.videoCount }}</div>
                  <div class="stat-label">视频语料</div>
                </div>
              </div>
            </a-col>
          </a-row>
          <div class="storage-total">
            <span class="storage-label">存储空间占用：</span>
            <span class="storage-value">{{ formatSize(currentMerchant.corpusStats.totalSize) }}</span>
          </div>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="promptModalVisible"
      :title="promptModalTitle"
      :width="600"
      :footer="null"
    >
      <div class="prompt-modal-content">
        {{ promptModalContent }}
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchantConfigList } from '@/api/ai'
import { useUserStore } from '@/store/user'
import { FileTextOutlined, PictureOutlined, VideoCameraOutlined } from '@ant-design/icons-vue'

const userStore = useUserStore()

const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const dataSource = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
})

const detailVisible = ref(false)
const currentMerchant = ref(null)

const promptModalVisible = ref(false)
const promptModalTitle = ref('')
const promptModalContent = ref('')

const columns = [
  {
    title: '商家名称',
    key: 'merchant',
    width: 200,
    fixed: 'left'
  },
  {
    title: 'AI配置状态',
    key: 'configStatus',
    width: 200
  },
  {
    title: '文字提示词',
    key: 'textPrompt',
    width: 180,
    ellipsis: true
  },
  {
    title: '图片提示词',
    key: 'imagePrompt',
    width: 180,
    ellipsis: true
  },
  {
    title: '视频提示词',
    key: 'videoPrompt',
    width: 180,
    ellipsis: true
  },
  {
    title: '语料数量',
    key: 'corpusCount',
    width: 200
  },
  {
    title: '存储空间',
    key: 'storage',
    width: 120
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right'
  }
]

const loadData = async () => {
  if (!userStore.isAdmin) {
    return
  }
  loading.value = true
  try {
    const res = await getMerchantConfigList({
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value,
      status: statusFilter.value
    })
    dataSource.value = res.list || []
    pagination.value.total = res.total || 0
  } catch (e) {
    console.error('加载商家配置列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  loadData()
}

const handleReset = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  pagination.value.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const truncateText = (text, len) => {
  if (!text) return ''
  if (text.length <= len) return text
  return text.substring(0, len) + '...'
}

const formatSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const showDetail = (record) => {
  currentMerchant.value = record
  detailVisible.value = true
}

const showPromptDetail = (record, type) => {
  const titleMap = {
    text: '文字提示词',
    image: '图片提示词',
    video: '视频提示词'
  }
  const contentMap = {
    text: record.config?.textPrompt || '',
    image: record.config?.imagePrompt || '',
    video: record.config?.videoPrompt || ''
  }
  promptModalTitle.value = titleMap[type]
  promptModalContent.value = contentMap[type]
  promptModalVisible.value = true
}

onMounted(() => {
  if (userStore.isAdmin) {
    loadData()
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.merchant-config-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: $text-color;
  line-height: 1.4;
}

.page-desc {
  font-size: 14px;
  color: $text-tertiary;
  margin-top: 6px;
}

.card-wrapper {
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  padding: 20px;
  margin-bottom: 16px;
}

.search-card {
  padding: 16px 20px;
}

.table-card {
  padding: 0;
  overflow: hidden;

  :deep(.ant-table) {
    .ant-table-thead > tr > th {
      background: #fafafa;
      font-weight: 600;
    }
  }
}

.merchant-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.merchant-name {
  font-size: 14px;
  color: $text-color;
  font-weight: 500;
}

.model-info {
  margin-top: 4px;
  font-size: 12px;
  color: $text-tertiary;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.model-item {
  white-space: nowrap;
}

.prompt-text {
  color: $text-secondary;
  cursor: pointer;
  font-size: 13px;

  &:hover {
    color: $primary-color;
  }
}

.view-more {
  color: $primary-color;
  margin-left: 4px;
}

.empty-text {
  color: $text-tertiary;
}

.corpus-count {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.count-item {
  white-space: nowrap;
}

.text-count {
  color: $primary-color;
}

.image-count {
  color: $success-color;
}

.video-count {
  color: #722ed1;
}

.storage-text {
  color: $text-color;
  font-weight: 500;
}

.detail-modal {
  :deep(.ant-modal-body) {
    max-height: 70vh;
    overflow-y: auto;
  }
}

.detail-content {
  .detail-section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: $text-color;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid $primary-color;
  }
}

.prompt-section {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.prompt-label {
  font-size: 13px;
  color: $text-secondary;
  margin-bottom: 6px;
  font-weight: 500;
}

.prompt-content {
  background: $bg-body;
  border-radius: $border-radius-sm;
  padding: 12px 16px;
  font-size: 13px;
  color: $text-color;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: $border-radius-sm;
  background: $bg-body;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.text-stat .stat-icon {
  background: rgba(22, 119, 255, 0.1);
  color: $primary-color;
}

.image-stat .stat-icon {
  background: rgba(82, 196, 26, 0.1);
  color: $success-color;
}

.video-stat .stat-icon {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: $text-color;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
}

.storage-total {
  margin-top: 16px;
  padding: 12px 16px;
  background: rgba(22, 119, 255, 0.04);
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.storage-label {
  font-size: 13px;
  color: $text-secondary;
}

.storage-value {
  font-size: 16px;
  font-weight: 600;
  color: $primary-color;
}

.prompt-modal-content {
  font-size: 14px;
  line-height: 1.8;
  color: $text-color;
  white-space: pre-wrap;
  word-break: break-all;
  padding: 8px 0;
}
</style>
