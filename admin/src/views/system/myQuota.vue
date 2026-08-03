<template>
  <div class="my-quota-page">
    <div class="page-header">
      <div class="page-title">当前额度</div>
      <div class="page-desc">查看您的套餐使用情况和购买记录</div>
    </div>

    <div class="current-plan-card card-wrapper" v-if="quotaData.currentPlan">
      <div class="plan-info">
        <div class="plan-badge" :class="getPlanLevelClass(quotaData.currentPlan.planLevel)">
          {{ quotaData.currentPlan.planName }}
        </div>
        <div class="plan-price">¥{{ quotaData.currentPlan.price }} / {{ quotaData.currentPlan.durationMonths }}个月</div>
        <div class="plan-time">
          <span>开通时间：{{ formatTime(quotaData.currentPlan.paidAt) }}</span>
          <span v-if="quotaData.currentPlan.expireAt">到期时间：{{ formatTime(quotaData.currentPlan.expireAt) }}</span>
        </div>
      </div>
      <a-button type="primary" @click="showPlanModal = true">
        <template #icon><ShoppingOutlined /></template>
        升级/续费
      </a-button>
    </div>
    <div class="current-plan-card card-wrapper no-plan" v-else>
      <div class="plan-info">
        <div class="plan-badge free">免费版</div>
        <div class="plan-desc">您当前使用免费版套餐，功能有限</div>
      </div>
      <a-button type="primary" @click="showPlanModal = true">
        <template #icon><CrownOutlined /></template>
        立即升级
      </a-button>
    </div>

    <a-row :gutter="16" class="quota-cards">
      <a-col :xs="24" :sm="12" :md="8" :lg="8">
        <div class="quota-card card-wrapper">
          <div class="quota-icon storage-icon">
            <CloudServerOutlined />
          </div>
          <div class="quota-content">
            <div class="quota-label">存储空间</div>
            <div class="quota-value">{{ formatFileSize(quotaData.storage?.usedBytes || 0) }} / {{ quotaData.storage?.limitMB ? quotaData.storage.limitMB + ' MB' : '不限' }}</div>
            <a-progress
              :percent="storagePercent"
              :stroke-color="storagePercent > 80 ? '#ff4d4f' : '#1677ff'"
              :show-info="false"
              size="small"
            />
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="8">
        <div class="quota-card card-wrapper">
          <div class="quota-icon text-icon">
            <FileTextOutlined />
          </div>
          <div class="quota-content">
            <div class="quota-label">文字生成</div>
            <div class="quota-value">{{ quotaData.aiQuota?.text?.used || 0 }} 次 / {{ quotaData.aiQuota?.text?.total ? quotaData.aiQuota.text.total + ' 次' : '不限' }}</div>
            <a-progress
              :percent="textQuotaPercent"
              :stroke-color="textQuotaPercent > 80 ? '#ff4d4f' : '#52c41a'"
              :show-info="false"
              size="small"
            />
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="8">
        <div class="quota-card card-wrapper">
          <div class="quota-icon image-icon">
            <PictureOutlined />
          </div>
          <div class="quota-content">
            <div class="quota-label">图片生成</div>
            <div class="quota-value">{{ quotaData.aiQuota?.image?.used || 0 }} 次 / {{ quotaData.aiQuota?.image?.total ? quotaData.aiQuota.image.total + ' 次' : '不限' }}</div>
            <a-progress
              :percent="imageQuotaPercent"
              :stroke-color="imageQuotaPercent > 80 ? '#ff4d4f' : '#722ed1'"
              :show-info="false"
              size="small"
            />
          </div>
        </div>
      </a-col>
    </a-row>

    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">
          购买记录
          <a-tag color="blue">共 {{ orderPagination.total }} 条</a-tag>
        </div>
      </div>
      <a-table
        :columns="orderColumns"
        :data-source="orderList"
        :pagination="false"
        :row-key="record => record.id"
        :loading="orderLoading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'planName'">
            <a-tag :color="getPlanTagColor(record.plan_level)">{{ record.plan_name || '-' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'amount'">
            <span class="amount">¥{{ record.amount?.toFixed(2) || '0.00' }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge 
              :status="getOrderStatus(record.status).status" 
              :text="getOrderStatus(record.status).text" 
            />
          </template>
          <template v-else-if="column.key === 'createdAt'">
            <span class="create-time">{{ formatTime(record.created_at) }}</span>
          </template>
        </template>
      </a-table>
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="orderPagination.current"
          v-model:page-size="orderPagination.pageSize"
          :total="orderPagination.total"
          :page-size-options="['10', '20', '50']"
          show-size-changer
          show-quick-jumper
          :show-total="(total) => '共 ' + total + ' 条'"
          @change="loadOrders"
        />
      </div>
    </div>

    <a-modal
      v-model:open="showPlanModal"
      title="选择套餐"
      :footer="null"
      width="720px"
      destroy-on-close
    >
      <div class="plan-list">
        <div 
          v-for="plan in planList" 
          :key="plan.id" 
          class="plan-item card-wrapper"
        >
          <div v-if="plan.recommend" class="recommend-corner">推荐</div>
          <div class="plan-name">{{ plan.name }}</div>
          <div class="plan-price-tag">
            <span class="currency">¥</span>
            <span class="price">{{ plan.price }}</span>
            <span class="duration">/ {{ plan.durationMonths }}个月</span>
          </div>
          <div class="plan-features">
            <div class="feature-item">
              <span class="feature-label">存储空间</span>
              <span class="feature-value">{{ plan.storageLimit }} MB</span>
            </div>
            <div class="feature-item">
              <span class="feature-label">设备数量</span>
              <span class="feature-value">{{ plan.deviceCount }} 台</span>
            </div>
            <div class="feature-item">
              <span class="feature-label">文字生成</span>
              <span class="feature-value">{{ plan.textQuota ? plan.textQuota + ' 次/月' : '不限' }}</span>
            </div>
            <div class="feature-item">
              <span class="feature-label">图片生成</span>
              <span class="feature-value">{{ plan.imageQuota ? plan.imageQuota + ' 次/月' : '不限' }}</span>
            </div>
          </div>
          <a-button 
            type="primary" 
            block
            @click="handlePurchase(plan)"
            :disabled="plan.status !== 1"
          >
            {{ plan.status === 1 ? '立即购买' : '暂不可用' }}
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  CloudServerOutlined,
  FileTextOutlined,
  PictureOutlined,
  ShoppingOutlined,
  CrownOutlined
} from '@ant-design/icons-vue'
import { getMyQuota, getMyOrders } from '@/api/merchant'
import { getPlanList } from '@/api/marketing'
import { formatFileSize } from '@/utils/format'

const quotaData = ref({})
const loading = ref(false)
const orderList = ref([])
const orderLoading = ref(false)
const showPlanModal = ref(false)
const planList = ref([])

const orderPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const orderColumns = [
  { title: '订单号', dataIndex: 'order_no', key: 'orderNo', width: 200 },
  { title: '套餐名称', dataIndex: 'plan_name', key: 'planName', width: 140 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '支付方式', dataIndex: 'pay_type', key: 'payType', width: 120 },
  { title: '购买时间', dataIndex: 'created_at', key: 'createdAt', width: 180 }
]

const storagePercent = computed(() => {
  const used = quotaData.value.storage?.usedBytes || 0
  const total = quotaData.value.storage?.totalBytes || 0
  if (total === 0) return 0
  return Math.min(Math.round((used / total) * 100), 100)
})

const textQuotaPercent = computed(() => {
  const used = quotaData.value.aiQuota?.text?.used || 0
  const total = quotaData.value.aiQuota?.text?.total || 0
  if (total === 0) return 0
  return Math.min(Math.round((used / total) * 100), 100)
})

const imageQuotaPercent = computed(() => {
  const used = quotaData.value.aiQuota?.image?.used || 0
  const total = quotaData.value.aiQuota?.image?.total || 0
  if (total === 0) return 0
  return Math.min(Math.round((used / total) * 100), 100)
})

const loadQuota = async () => {
  loading.value = true
  try {
    const res = await getMyQuota()
    const data = {
      storage: {
        usedBytes: (res?.storage?.used || 0) * 1024 * 1024,
        limitMB: res?.storage?.limit || 0,
        totalBytes: (res?.storage?.limit || 0) * 1024 * 1024
      },
      aiQuota: {
        text: {
          used: res?.aiGeneration?.text?.used || 0,
          total: res?.aiGeneration?.text?.quota || 0
        },
        image: {
          used: res?.aiGeneration?.image?.used || 0,
          total: res?.aiGeneration?.image?.quota || 0
        },
        video: {
          used: res?.aiGeneration?.video?.used || 0,
          total: res?.aiGeneration?.video?.quota || 0
        }
      },
      currentPlan: res?.currentPlan || null
    }
    quotaData.value = data
  } catch (e) {
    console.error('加载额度信息失败', e)
  } finally {
    loading.value = false
  }
}

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const res = await getMyOrders({
      page: orderPagination.current,
      pageSize: orderPagination.pageSize
    })
    orderList.value = res.list || []
    orderPagination.total = res.total || 0
  } catch (e) {
    console.error('加载订单列表失败', e)
  } finally {
    orderLoading.value = false
  }
}

const loadPlans = async () => {
  try {
    const res = await getPlanList({ page: 1, pageSize: 50, status: 1 })
    planList.value = (res.list || []).map(p => ({
      ...p,
      durationMonths: p.durationMonths || p.duration_months || 1,
      deviceCount: p.deviceCount || p.device_count || 1,
      textQuota: p.textQuota || p.text_quota || 0,
      imageQuota: p.imageQuota || p.image_quota || 0,
      storageLimit: p.storageLimit || p.storage_limit || 100
    }))
  } catch (e) {
    console.error('加载套餐列表失败', e)
  }
}

const handlePurchase = (plan) => {
  message.info('支付功能开发中，请联系管理员开通')
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })
}

const getPlanLevelClass = (level) => {
  const map = { basic: 'basic', pro: 'pro', enterprise: 'enterprise' }
  return map[level] || 'basic'
}

const getPlanTagColor = (level) => {
  const map = { basic: 'blue', pro: 'purple', enterprise: 'gold' }
  return map[level] || 'default'
}

const getOrderStatus = (status) => {
  const map = {
    0: { status: 'warning', text: '待支付' },
    1: { status: 'success', text: '已支付' },
    2: { status: 'default', text: '已退款' },
    3: { status: 'error', text: '已取消' }
  }
  return map[status] || { status: 'default', text: '未知' }
}

onMounted(() => {
  loadQuota()
  loadOrders()
  loadPlans()
})
</script>

<style lang="scss" scoped>
.my-quota-page {
  padding: 24px;
}

.current-plan-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;

  &.no-plan {
    background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
    color: $text-color;
  }

  .plan-info {
    .plan-badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 500;
      background: rgba(255, 255, 255, 0.25);
      color: #fff;
      margin-bottom: 12px;

      &.free {
        background: #e6f7ff;
        color: $primary-color;
      }

      &.pro {
        background: rgba(255, 255, 255, 0.3);
      }

      &.enterprise {
        background: rgba(255, 215, 0, 0.3);
      }
    }

    .plan-price {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 8px;
    }

    .plan-desc {
      font-size: 14px;
      color: $text-secondary;
    }

    .plan-time {
      display: flex;
      gap: 24px;
      font-size: 13px;
      opacity: 0.9;
    }
  }
}

.quota-cards {
  margin-bottom: 20px;
}

.quota-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-bottom: 16px;

  .quota-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;

    &.storage-icon {
      background: #e6f7ff;
      color: $primary-color;
    }

    &.text-icon {
      background: #f6ffed;
      color: $success-color;
    }

    &.image-icon {
      background: #f9f0ff;
      color: #722ed1;
    }
  }

  .quota-content {
    flex: 1;
    min-width: 0;

    .quota-label {
      font-size: 13px;
      color: $text-tertiary;
      margin-bottom: 4px;
    }

    .quota-value {
      font-size: 16px;
      font-weight: 600;
      color: $text-color;
      margin-bottom: 8px;
    }
  }
}

.amount {
  color: $error-color;
  font-weight: 600;
}

.create-time {
  color: $text-secondary;
  font-size: 13px;
}

.plan-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.plan-item {
  position: relative;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid $border-color;
  border-radius: $border-radius;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
    border-color: $primary-color;
  }

  .recommend-corner {
    position: absolute;
    top: 0;
    right: 0;
    background: $primary-color;
    color: #fff;
    font-size: 12px;
    padding: 2px 10px;
    border-radius: 0 $border-radius 0 8px;
  }

  .plan-name {
    font-size: 16px;
    font-weight: 600;
    color: $text-color;
    margin-bottom: 12px;
  }

  .plan-price-tag {
    margin-bottom: 16px;

    .currency {
      font-size: 14px;
      color: $error-color;
      vertical-align: top;
    }

    .price {
      font-size: 28px;
      font-weight: 600;
      color: $error-color;
    }

    .duration {
      font-size: 13px;
      color: $text-tertiary;
    }
  }

  .plan-features {
    margin-bottom: 16px;
    text-align: left;

    .feature-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid $border-color;
      font-size: 13px;

      &:last-child {
        border-bottom: none;
      }

      .feature-label {
        color: $text-tertiary;
      }

      .feature-value {
        color: $text-color;
        font-weight: 500;
      }
    }
  }
}
</style>
