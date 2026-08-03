<template>
  <div class="orders-page">
    <div class="page-header">
      <div class="page-title">订单管理</div>
      <div class="page-desc">查看和管理商家购买订单</div>
    </div>
    
    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="订单号">
          <a-input 
            v-model:value="searchForm.orderNo" 
            placeholder="请输入订单号"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="所属商家">
          <a-select 
            v-model:value="searchForm.merchantId" 
            placeholder="全部商家"
            style="width: 160px"
            allow-clear
            show-search
            :options="merchantOptions"
          />
        </a-form-item>
        <a-form-item label="订单状态">
          <a-select 
            v-model:value="searchForm.status" 
            placeholder="全部状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option value="pending">待支付</a-select-option>
            <a-select-option value="paid">已支付</a-select-option>
            <a-select-option value="refunded">已退款</a-select-option>
            <a-select-option value="cancelled">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="下单时间">
          <a-range-picker 
            v-model:value="searchForm.dateRange" 
            style="width: 260px"
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
          订单列表
          <a-tag color="blue">共 {{ pagination.total }} 条</a-tag>
        </div>
        <div class="table-actions">
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出
          </a-button>
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
          <template v-if="column.key === 'amount'">
            <span class="amount">¥{{ record.amount }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusName(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleDetail(record)">详情</a>
              <a-popconfirm
                v-if="record.status === 'paid'"
                title="确定要给该订单退款吗？"
                @confirm="handleRefund(record)"
              >
                <a type="link" size="small" :danger="true">退款</a>
              </a-popconfirm>
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
    
    <a-drawer
      v-model:open="detailVisible"
      title="订单详情"
      width="480px"
    >
      <div v-if="currentOrder" class="order-detail">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="订单号">
            {{ currentOrder.orderNo }}
          </a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getStatusColor(currentOrder.status)">
              {{ getStatusName(currentOrder.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="商家名称">
            {{ currentOrder.merchantName }}
          </a-descriptions-item>
          <a-descriptions-item label="套餐名称">
            {{ currentOrder.planName }}
          </a-descriptions-item>
          <a-descriptions-item label="订单金额">
            <span class="amount">¥{{ currentOrder.amount }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="支付方式">
            {{ currentOrder.payType === 'wechat' ? '微信支付' : '支付宝' }}
          </a-descriptions-item>
          <a-descriptions-item label="下单时间">
            {{ currentOrder.createTime }}
          </a-descriptions-item>
          <a-descriptions-item label="支付时间" v-if="currentOrder.payTime">
            {{ currentOrder.payTime }}
          </a-descriptions-item>
          <a-descriptions-item label="有效期">
            {{ currentOrder.validTime }}
          </a-descriptions-item>
        </a-descriptions>
        
        <div class="detail-actions">
          <a-space>
            <a-button @click="detailVisible = false">关闭</a-button>
            <a-popconfirm
              v-if="currentOrder.status === 'paid'"
              title="确定要给该订单退款吗？"
              @confirm="handleRefund(currentOrder)"
            >
              <a-button danger>退款</a-button>
            </a-popconfirm>
          </a-space>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { getOrderList, refundOrder } from '@/api/marketing'
import { getMerchantList } from '@/api/merchant'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { watch } from 'vue'

const appStore = useAppStore()
const userStore = useUserStore()
const tableLoading = ref(false)

const searchForm = reactive({
  orderNo: '',
  merchantId: undefined,
  status: undefined,
  dateRange: []
})

const merchantOptions = ref([])

const loadMerchantOptions = async () => {
  try {
    const res = await getMerchantList({ current: 1, pageSize: 100 })
    const list = res.list || res || []
    merchantOptions.value = (Array.isArray(list) ? list : []).map(m => ({
      label: m.shopName || m.name || m.label,
      value: m.id
    }))
  } catch (e) {
    console.error('加载商家列表失败', e)
  }
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
  { title: '商家名称', dataIndex: 'merchantName', key: 'merchantName', width: 160 },
  { title: '套餐名称', dataIndex: 'planName', key: 'planName', width: 160 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 110 },
  { title: '支付方式', dataIndex: 'payType', key: 'payType', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '下单时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 140, fixed: 'right' }
]

const dataSource = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

const getStatusName = (status) => {
  const map = {
    pending: '待支付',
    paid: '已支付',
    refunded: '已退款',
    cancelled: '已取消'
  }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = {
    pending: 'warning',
    paid: 'success',
    refunded: 'default',
    cancelled: 'error'
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
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo
    // Use global merchant filter if admin has selected one
    const globalMerchantId = userStore.isAdmin ? appStore.merchantId : ''
    if (globalMerchantId) {
      params.merchantId = globalMerchantId
    } else if (searchForm.merchantId !== undefined) {
      params.merchantId = searchForm.merchantId
    }
    if (searchForm.status !== undefined) params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await getOrderList(params)
    const list = res.list || res || []
    dataSource.value = Array.isArray(list) ? list : []
    pagination.total = res.total || 0
  } catch (e) {
    console.error('加载订单列表失败', e)
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.merchantId = undefined
  searchForm.status = undefined
  searchForm.dateRange = []
  pagination.current = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleExport = () => {
  message.success('导出成功')
}

const handleDetail = (record) => {
  currentOrder.value = record
  detailVisible.value = true
}

const handleRefund = async (record) => {
  try {
    await refundOrder(record.id)
    message.success('退款成功')
    detailVisible.value = false
    loadData()
  } catch (e) {
    console.error('退款失败', e)
  }
}

// Watch global merchant change and auto refresh
watch(() => appStore.merchantId, (newVal) => {
  if (userStore.isAdmin) {
    searchForm.merchantId = newVal || undefined
    pagination.current = 1
    loadData()
  }
})

onMounted(() => {
  loadMerchantOptions()
  if (userStore.isAdmin && appStore.merchantId) {
    searchForm.merchantId = appStore.merchantId
  }
  loadData()
})
</script>

<style lang="scss" scoped>
.orders-page {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.amount {
  font-weight: 600;
  color: #ff4d4f;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  .detail-actions {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
