<template>
  <div class="merchant-audit-page">
    <div class="page-header">
      <div class="page-title">商家审核</div>
      <div class="page-desc">审核新入驻的商家申请</div>
    </div>
    
    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="商家名称">
          <a-input 
            v-model:value="searchForm.keyword" 
            placeholder="请输入商家名称"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select 
            v-model:value="searchForm.status" 
            placeholder="全部状态"
            style="width: 140px"
            allow-clear
          >
            <a-select-option value="pending">待审核</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
          </a-select>
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
          审核列表
          <a-tag color="warning">待审核 {{ pendingCount }} 家</a-tag>
        </div>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="false"
        :row-key="record => record.id"
        :loading="tableLoading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'shopLogo'">
            <img :src="record.shopLogo || '/vite.svg'" class="shop-logo" />
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleView(record)">查看</a>
              <template v-if="record.status === 'pending'">
                <a-popconfirm
                  title="确定通过该商家的申请吗？"
                  @confirm="handleAudit(record, 'approved')"
                >
                  <a type="link" size="small">通过</a>
                </a-popconfirm>
                <a type="link" size="small" :danger="true" @click="handleReject(record)">
                  驳回
                </a>
              </template>
            </a-space>
          </template>
        </template>
      </a-table>
      
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-size-options="['10', '20', '50']"
          show-size-changer
          show-quick-jumper
          :show-total="(total) => '共 ' + total + ' 条'"
          @change="handlePageChange"
        />
      </div>
    </div>
    
    <a-modal
      v-model:open="detailVisible"
      title="商家详情"
      width="600px"
      :footer="null"
    >
      <div v-if="currentMerchant" class="merchant-detail">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="店铺名称" :span="2">
            {{ currentMerchant.shopName }}
          </a-descriptions-item>
          <a-descriptions-item label="登录账号">
            {{ currentMerchant.account }}
          </a-descriptions-item>
          <a-descriptions-item label="联系电话">
            {{ currentMerchant.phone }}
          </a-descriptions-item>
          <a-descriptions-item label="营业执照">
            <a href="#" @click.prevent>查看图片</a>
          </a-descriptions-item>
          <a-descriptions-item label="法人姓名">
            {{ currentMerchant.legalPerson || '张三' }}
          </a-descriptions-item>
          <a-descriptions-item label="身份证号">
            {{ currentMerchant.idCard || '110101199001011234' }}
          </a-descriptions-item>
          <a-descriptions-item label="店铺地址" :span="2">
            {{ currentMerchant.shopAddress }}
          </a-descriptions-item>
          <a-descriptions-item label="店铺简介" :span="2">
            {{ currentMerchant.shopDesc }}
          </a-descriptions-item>
          <a-descriptions-item label="申请时间" :span="2">
            {{ currentMerchant.createTime }}
          </a-descriptions-item>
        </a-descriptions>
        
        <div class="detail-actions">
          <a-space>
            <a-button @click="detailVisible = false">关闭</a-button>
            <template v-if="currentMerchant.status === 'pending'">
              <a-popconfirm
                title="确定通过该商家的申请吗？"
                @confirm="handleAudit(currentMerchant, 'approved')"
              >
                <a-button type="primary">通过审核</a-button>
              </a-popconfirm>
              <a-button danger @click="handleReject(currentMerchant)">
                驳回申请
              </a-button>
            </template>
          </a-space>
        </div>
      </div>
    </a-modal>
    
    <a-modal
      v-model:open="rejectVisible"
      title="驳回申请"
      @ok="confirmReject"
      ok-text="确认驳回"
      cancel-text="取消"
      :ok-button-props="{ danger: true }"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item label="驳回原因" name="reason">
          <a-textarea
            v-model:value="rejectForm.reason"
            placeholder="请输入驳回原因"
            :rows="4"
            show-count
            :max-length="200"
          />
        </a-form-item>
      </a-form>
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
import { getMerchantList, auditMerchant } from '@/api/merchant'

const tableLoading = ref(false)

const searchForm = reactive({
  keyword: '',
  status: 'pending'
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const pendingCount = ref(0)

const columns = [
  { title: '店铺Logo', dataIndex: 'shopLogo', key: 'shopLogo', width: 110 },
  { title: '店铺名称', dataIndex: 'shopName', key: 'shopName', width: 180 },
  { title: '账号', dataIndex: 'account', key: 'account', width: 140 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '申请时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 200, fixed: 'right' }
]

const dataSource = ref([])
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentMerchant = ref(null)

const rejectForm = reactive({
  reason: ''
})

const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return map[status] || '未知'
}

const getStatusColor = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'error'
  }
  return map[status] || 'default'
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status) params.status = searchForm.status
    const res = await getMerchantList(params)
    const list = res.list || res || []
    dataSource.value = Array.isArray(list) ? list : []
    pagination.total = res.total || 0
    if (Array.isArray(list)) {
      pendingCount.value = list.filter(m => m.status === 'pending').length
    }
  } catch (e) {
    console.error('加载审核列表失败', e)
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
  searchForm.status = 'pending'
  pagination.current = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleView = (record) => {
  currentMerchant.value = record
  detailVisible.value = true
}

const handleAudit = async (record, status) => {
  try {
    await auditMerchant(record.id, status)
    message.success(status === 'approved' ? '审核通过' : '已驳回')
    detailVisible.value = false
    loadData()
  } catch (e) {
    console.error('审核失败', e)
  }
}

const handleReject = (record) => {
  currentMerchant.value = record
  rejectForm.reason = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    message.warning('请输入驳回原因')
    return
  }
  try {
    await auditMerchant(currentMerchant.value.id, 'rejected')
    message.success('已驳回')
    rejectVisible.value = false
    detailVisible.value = false
    loadData()
  } catch (e) {
    console.error('驳回失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.merchant-audit-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1f1f1f;
    margin-bottom: 8px;
  }
  
  .page-desc {
    font-size: 14px;
    color: #8c8c8c;
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
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-logo {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f5f5;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.merchant-detail {
  .detail-actions {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
