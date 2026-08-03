<template>
  <div class="qrcode-page">
    <div class="page-header">
      <div class="page-title-wrapper">
        <div class="page-title">二维码管理</div>
        <div class="page-desc">管理二维码生成与绑定</div>
      </div>
      <a-button type="primary" @click="showGenerateModal = true">
        <template #icon><PlusOutlined /></template>
        批量生成二维码
      </a-button>
    </div>

    <div class="card-wrapper config-card" v-if="qrCodeConfig.qrcodeUrl">
      <a-descriptions title="二维码配置" :column="1" size="small" bordered>
        <a-descriptions-item label="前缀URL">
          <a-tag color="blue">{{ qrCodeConfig.qrcodeUrl }}</a-tag>
          <span class="form-item-tip" style="margin-left: 8px">二维码ID将以 ?q=xxx 的形式追加到此URL后</span>
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="searchForm.keyword" placeholder="二维码标识/URL" allow-clear style="width: 220px" />
        </a-form-item>
        <a-form-item label="绑定状态">
          <a-select v-model:value="searchForm.bindStatus" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option :value="''">全部</a-select-option>
            <a-select-option :value="0">未绑定</a-select-option>
            <a-select-option :value="1">已绑定</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="resetSearch">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">二维码列表</div>
      </div>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :row-key="record => record.id"
        :loading="tableLoading"
        :scroll="{ x: 1400 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'qrImage'">
            <img v-if="record.qrImage" :src="record.qrImage" alt="二维码" class="qr-thumb" @click="previewQr(record)" />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'bindStatus'">
            <a-tag :color="record.bindStatus === 1 ? 'green' : 'orange'">
              {{ record.bindStatus === 1 ? '已绑定' : '未绑定' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'merchantName'">
            {{ record.merchantName || '-' }}
          </template>
          <template v-else-if="column.key === 'scanCount'">
            {{ record.scanCount || 0 }}
          </template>
          <template v-else-if="column.key === 'lastScanAt'">
            {{ record.lastScanAt || '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="previewQr(record)">查看</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="showGenerateModal"
      title="批量生成二维码"
      @ok="handleGenerate"
      @cancel="showGenerateModal = false"
      :ok-loading="generating"
    >
      <a-alert type="info" show-icon style="margin-bottom: 16px">
        <template #message>
          <span>二维码将使用配置的前缀URL生成：</span>
          <strong>{{ qrCodeConfig.qrcodeUrl || '未配置' }}</strong>
          <span> + ?q=二维码ID</span>
        </template>
      </a-alert>
      <a-form :model="generateForm" layout="vertical">
        <a-form-item label="生成数量" required>
          <a-input-number v-model:value="generateForm.count" :min="1" :max="100" style="width: 100%" />
          <div class="form-item-tip">每次最多生成 100 个</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="previewVisible"
      title="二维码预览"
      :footer="null"
      width="400px"
    >
      <div class="qr-preview" v-if="previewRecord">
        <img :src="previewRecord.qrImage" alt="二维码大图" style="width: 280px; height: 280px" />
        <div class="qr-info">
          <div>编号：{{ previewRecord.code }}</div>
          <div style="word-break: break-all">URL：{{ previewRecord.url }}</div>
          <div>绑定状态：{{ previewRecord.bindStatus === 1 ? '已绑定' : '未绑定' }}</div>
          <div>关联商家：{{ previewRecord.merchantName || '-' }}</div>
          <div>扫码次数：{{ previewRecord.scanCount || 0 }}</div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getQrCodeHistory, batchGenerateQrCodes, deleteQrCode, getQrCodeConfig } from '@/api/device'

const emit = defineEmits(['refresh'])
const tableLoading = ref(false)
const dataSource = ref([])
const showGenerateModal = ref(false)
const generating = ref(false)
const previewVisible = ref(false)
const previewRecord = ref(null)
const qrCodeConfig = reactive({
  qrcodeUrl: ''
})

const searchForm = reactive({
  keyword: '',
  bindStatus: ''
})

const generateForm = reactive({
  count: 10
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '二维码', dataIndex: 'qrImage', key: 'qrImage', width: 100 },
  { title: '编号', dataIndex: 'code', key: 'code', width: 200, ellipsis: true },
  { title: '跳转URL', dataIndex: 'url', key: 'url', width: 280, ellipsis: true },
  { title: '绑定状态', dataIndex: 'bindStatus', key: 'bindStatus', width: 100 },
  { title: '关联商家', dataIndex: 'merchantName', key: 'merchantName', width: 140 },
  { title: '扫码次数', dataIndex: 'scanCount', key: 'scanCount', width: 100 },
  { title: '最后扫码', dataIndex: 'lastScanAt', key: 'lastScanAt', width: 160 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const loadData = async () => {
  try {
    tableLoading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      bindStatus: searchForm.bindStatus
    }
    const res = await getQrCodeHistory(params)
    dataSource.value = res.list || []
    pagination.total = res.total || 0
  } catch (err) {
    message.error(err.message || '加载失败')
  } finally {
    tableLoading.value = false
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.bindStatus = ''
  pagination.current = 1
  loadData()
}

const handleGenerate = async () => {
  try {
    generating.value = true
    await batchGenerateQrCodes({ count: generateForm.count })
    message.success(`成功生成 ${generateForm.count} 个二维码`)
    showGenerateModal.value = false
    generateForm.count = 10
    loadData()
    emit('refresh')
  } catch (err) {
    message.error(err.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确定删除该二维码吗？',
    content: `编号：${record.code}`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteQrCode(record.id)
        message.success('删除成功')
        loadData()
      } catch (err) {
        message.error(err.message || '删除失败')
      }
    }
  })
}

const previewQr = (record) => {
  previewRecord.value = record
  previewVisible.value = true
}

const loadQrCodeConfig = async () => {
  try {
    const res = await getQrCodeConfig()
    if (res && res.qrcodeUrl) {
      qrCodeConfig.qrcodeUrl = res.qrcodeUrl
    }
  } catch (e) {}
}

onMounted(() => {
  loadData()
  loadQrCodeConfig()
})
</script>

<style lang="scss" scoped>
.qrcode-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.config-card {
  margin-bottom: 16px;
  padding: 16px 24px;
}

.page-title-wrapper {
  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: $text-color;
  }
  
  .page-desc {
    font-size: 14px;
    color: $text-secondary;
    margin-top: 4px;
  }
}

.qr-thumb {
  width: 50px;
  height: 50px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #eee;
  object-fit: cover;
}

.qr-preview {
  text-align: center;

  .qr-info {
    margin-top: 16px;
    text-align: left;
    font-size: 13px;
    color: #666;

    div {
      margin-bottom: 6px;
    }
  }
}

.form-item-tip {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
}
</style>
