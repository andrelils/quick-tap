<template>
  <div class="plans-page">
    <div class="page-header">
      <div class="page-title">套餐管理</div>
      <div class="page-desc">管理商家付费套餐和 AI 额度</div>
    </div>
    
    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="套餐名称">
          <a-input 
            v-model:value="searchForm.name" 
            placeholder="请输入套餐名称"
            style="width: 160px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="套餐等级">
          <a-select 
            v-model:value="searchForm.level" 
            placeholder="全部等级"
            style="width: 120px"
            allow-clear
          >
            <a-select-option value="basic">基础版</a-select-option>
            <a-select-option value="pro">专业版</a-select-option>
            <a-select-option value="enterprise">企业版</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select 
            v-model:value="searchForm.status" 
            placeholder="全部状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="1">上架中</a-select-option>
            <a-select-option :value="0">已下架</a-select-option>
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
    
    <div class="plans-overview">
      <a-row :gutter="16">
        <a-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-item">
            <div class="stat-label">在售套餐</div>
            <div class="stat-value">{{ planStats.onSale }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-item">
            <div class="stat-label">付费商家</div>
            <div class="stat-value">{{ planStats.paidMerchant }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-item">
            <div class="stat-label">本月营收</div>
            <div class="stat-value">¥{{ planStats.monthRevenue }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-item">
            <div class="stat-label">待续费</div>
            <div class="stat-value">{{ planStats.renewCount }}</div>
          </div>
        </a-col>
      </a-row>
    </div>
    
    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">套餐列表</div>
        <div class="table-actions">
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增套餐
          </a-button>
        </div>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="false"
        :row-key="record => record.id"
        :loading="tableLoading"
        :scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="plan-name">
              <div class="plan-tag" :class="record.level">
                {{ record.name }}
              </div>
              <span v-if="record.recommend" class="recommend-tag">推荐</span>
            </div>
          </template>
          <template v-else-if="column.key === 'price'">
            <div class="plan-price">
              <span class="price-symbol">¥</span>
              <span class="price-num">{{ record.price }}</span>
              <span class="price-unit">/{{ record.duration }}天</span>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge 
              :status="record.status === 1 ? 'success' : 'default'" 
              :text="record.status === 1 ? '上架中' : '已下架'" 
            />
          </template>
          <template v-else-if="column.key === 'storage_limit'">
            {{ record.storage_limit === 0 ? '不限' : `${record.storage_limit}MB` }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a-popconfirm
                v-if="record.status === 1"
                title="确定要下架该套餐吗？"
                @confirm="handleToggle(record)"
              >
                <a type="link" size="small" :danger="true">下架</a>
              </a-popconfirm>
              <a type="link" size="small" v-else @click="handleToggle(record)">
                上架
              </a>
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
      v-model:open="modalVisible"
      :title="modalTitle"
      width="600px"
      :footer="null"
      destroy-on-close
    >
      <a-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="套餐名称" name="name">
              <a-input v-model:value="formData.name" placeholder="如：基础版、专业版" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="套餐等级" name="level">
              <a-select 
                v-model:value="formData.level" 
                placeholder="请选择等级"
                style="width: 100%"
              >
                <a-select-option value="basic">基础版</a-select-option>
                <a-select-option value="pro">专业版</a-select-option>
                <a-select-option value="enterprise">企业版</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="套餐价格" name="price">
              <a-input-number 
                v-model:value="formData.price" 
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入价格"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="有效期(天)" name="duration">
              <a-input-number 
                v-model:value="formData.duration" 
                :min="1"
                style="width: 100%"
                placeholder="请输入有效期天数"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="文字生成额度" name="textQuota">
              <a-input-number 
                v-model:value="formData.textQuota" 
                :min="0"
                style="width: 100%"
                placeholder="0 表示不限"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图片生成额度" name="imageQuota">
              <a-input-number 
                v-model:value="formData.imageQuota" 
                :min="0"
                style="width: 100%"
                placeholder="0 表示不限"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="设备数量限制" name="deviceLimit">
              <a-input-number 
                v-model:value="formData.deviceLimit" 
                :min="1"
                style="width: 100%"
                placeholder="最多绑定设备数"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="存储空间限制(MB)" name="storageLimit">
              <a-input-number 
                v-model:value="formData.storageLimit" 
                :min="0"
                style="width: 100%"
                placeholder="0 表示不限"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排序" name="sort">
              <a-input-number 
                v-model:value="formData.sort" 
                :min="0"
                style="width: 100%"
                placeholder="数字越小越靠前"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="套餐描述" name="description">
          <a-textarea 
            v-model:value="formData.description" 
            :rows="3"
            placeholder="请输入套餐描述"
          />
        </a-form-item>
        <a-form-item label="是否推荐" name="recommend">
          <a-switch v-model:checked="formData.recommend" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">上架</a-radio>
            <a-radio :value="0">下架</a-radio>
          </a-radio-group>
        </a-form-item>
        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getPlanList, createPlan, updatePlan } from '@/api/marketing'
import { getOverview } from '@/api/statistics'

const tableLoading = ref(false)

const planStats = reactive({
  onSale: 0,
  paidMerchant: 0,
  monthRevenue: 0,
  renewCount: 0
})

const searchForm = reactive({
  name: '',
  level: undefined,
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '套餐名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '价格', dataIndex: 'price', key: 'price', width: 160 },
  { title: '有效期', dataIndex: 'duration', key: 'duration', width: 100 },
  { title: '文字额度', dataIndex: 'textQuota', key: 'textQuota', width: 120 },
  { title: '图片额度', dataIndex: 'imageQuota', key: 'imageQuota', width: 120 },
  { title: '设备限制', dataIndex: 'deviceLimit', key: 'deviceLimit', width: 110 },
  { title: '存储空间', dataIndex: 'storage_limit', key: 'storage_limit', width: 110 },
  { title: '购买人数', dataIndex: 'buyCount', key: 'buyCount', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 160, fixed: 'right' }
]

const dataSource = ref([])
const modalVisible = ref(false)
const modalTitle = ref('新增套餐')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const formData = reactive({
  id: null,
  name: '',
  level: 'basic',
  price: 0,
  duration: 30,
  textQuota: 0,
  imageQuota: 0,
  deviceLimit: 1,
  storageLimit: 0,
  sort: 0,
  description: '',
  recommend: false,
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入套餐价格', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.level !== undefined) params.level = searchForm.level
    if (searchForm.status !== undefined) params.status = searchForm.status
    const res = await getPlanList(params)
    const list = res.list || res || []
    dataSource.value = Array.isArray(list) ? list : []
    pagination.total = res.total || 0
  } catch (e) {
    console.error('加载套餐列表失败', e)
  } finally {
    tableLoading.value = false
  }
}

const handlePageChange = () => {
  loadData()
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.level = undefined
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增套餐'
  Object.assign(formData, {
    id: null,
    name: '',
    level: 'basic',
    price: 0,
    duration: 30,
    textQuota: 0,
    imageQuota: 0,
    deviceLimit: 1,
    storageLimit: 0,
    sort: 0,
    description: '',
    recommend: false,
    status: 1
  })
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑套餐'
  Object.assign(formData, {
    id: record.id,
    name: record.name,
    level: record.level || 'basic',
    price: record.price,
    duration: record.duration || record.duration_months || 1,
    textQuota: record.textQuota || 0,
    imageQuota: record.imageQuota || 0,
    deviceLimit: record.deviceLimit || record.device_count || 1,
    storageLimit: record.storageLimit || record.storage_limit || 0,
    sort: record.sort ?? 0,
    description: record.description || '',
    recommend: !!record.recommend,
    status: record.status ?? 1
  })
  modalVisible.value = true
}

const handleToggle = async (record) => {
  try {
    await updatePlan(record.id, { status: record.status === 1 ? 0 : 1 })
    message.success(record.status === 1 ? '已下架' : '已上架')
    loadData()
  } catch (e) {
    console.error('更新套餐状态失败', e)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updatePlan(formData.id, formData)
      message.success('编辑成功')
    } else {
      await createPlan(formData)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
  } catch (e) {
    console.error('表单验证或提交失败', e)
  } finally {
    submitting.value = false
  }
}

const loadPlanStats = async () => {
  try {
    const res = await getOverview()
    const data = res || {}
    Object.assign(planStats, {
      onSale: data.onSalePlans ?? data.planOnSale ?? 0,
      paidMerchant: data.paidMerchants ?? data.paidMerchant ?? 0,
      monthRevenue: data.monthRevenue ?? 0,
      renewCount: data.renewCount ?? data.pendingRenew ?? 0
    })
  } catch (e) {
    console.error('加载套餐统计失败', e)
  }
}

onMounted(() => {
  loadData()
  loadPlanStats()
})
</script>

<style lang="scss" scoped>
.plans-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: $text-color;
    margin-bottom: 8px;
  }
  
  .page-desc {
    font-size: 14px;
    color: $text-tertiary;
  }
}

.plans-overview {
  margin-bottom: 24px;
}

.stat-item {
  background: $bg-card;
  border-radius: $border-radius;
  padding: 20px;
  box-shadow: $shadow-sm;
  transition: box-shadow 0.3s;
  
  &:hover {
    box-shadow: $shadow-md;
  }
}

.stat-label {
  font-size: 13px;
  color: $text-tertiary;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: $text-color;
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
}

.plan-name {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.plan-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  
  &.basic {
    background: #52c41a;
  }
  
  &.pro {
    background: #1677ff;
  }
  
  &.enterprise {
    background: #722ed1;
  }
}

.recommend-tag {
  font-size: 12px;
  color: #fa8c16;
  background: #fff7e6;
  padding: 2px 6px;
  border-radius: 4px;
}

.plan-price {
  color: #ff4d4f;
  
  .price-symbol {
    font-size: 14px;
  }
  
  .price-num {
    font-size: 20px;
    font-weight: 700;
  }
  
  .price-unit {
    font-size: 12px;
    color: #8c8c8c;
  }
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.ant-table) {
  .ant-table-cell {
    white-space: nowrap;
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
