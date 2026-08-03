<template>
  <div class="coupons-page">
    <div class="page-header">
      <div class="page-title">优惠券管理</div>
      <div class="page-desc">管理商家优惠券跳转链接，用户点击后跳转至第三方平台领取</div>
    </div>
    
    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
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
        <a-form-item label="状态">
          <a-select 
            v-model:value="searchForm.status" 
            placeholder="全部状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option value="active">进行中</a-select-option>
            <a-select-option value="expired">已过期</a-select-option>
            <a-select-option value="disabled">已停用</a-select-option>
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
          优惠券列表
          <a-tag color="blue">共 {{ pagination.total }} 张</a-tag>
        </div>
        <div class="table-actions">
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增优惠券
          </a-button>
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
          <template v-if="column.key === 'name'">
            <div class="coupon-name">
              <div class="coupon-type-tag" :class="record.type">
                {{ getTypeName(record.type) }}
              </div>
              {{ record.name || record.title }}
            </div>
          </template>
          <template v-else-if="column.key === 'link'">
            <a-tooltip :title="record.link" placement="topLeft">
              <a 
                v-if="record.link" 
                :href="record.link" 
                target="_blank" 
                rel="noopener noreferrer"
                class="link-text"
                @click.stop
              >
                <LinkOutlined />
                {{ record.link }}
              </a>
              <span v-else class="empty-text">未配置</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusName(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a-popconfirm
                v-if="record.status === 'active'"
                title="确定要停用该优惠券吗？"
                @confirm="handleToggle(record)"
              >
                <a type="link" size="small" :danger="true">停用</a>
              </a-popconfirm>
              <a type="link" size="small" v-else @click="handleToggle(record)">
                启用
              </a>
              <a-popconfirm
                title="确定要删除该优惠券吗？"
                @confirm="handleDelete(record)"
              >
                <a type="link" size="small" :danger="true">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
        
        <template #emptyText>
          <a-empty description="暂无优惠券数据" />
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
      width="640px"
      :footer="null"
      destroy-on-close
    >
      <a-alert type="info" show-icon style="margin-bottom: 16px">
        <template #message>
          优惠券主要用于提供<b>第三方平台跳转链接</b>，用户在小程序详情页点击领取后将跳转至该链接对应的平台完成领取
        </template>
      </a-alert>
      <a-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="所属商家" name="merchantId">
              <a-select 
                v-model:value="formData.merchantId" 
                placeholder="请选择商家"
                style="width: 100%"
                show-search
                :options="merchantOptions"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优惠券类型" name="type">
              <a-select 
                v-model:value="formData.type" 
                placeholder="请选择类型"
                style="width: 100%"
              >
                <a-select-option value="discount">折扣券</a-select-option>
                <a-select-option value="cash">代金券</a-select-option>
                <a-select-option value="gift">赠品券</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="优惠券名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入优惠券名称，如：满100减20" />
        </a-form-item>
        <a-form-item label="第三方平台跳转链接" name="link" required>
          <a-input 
            v-model:value="formData.link" 
            placeholder="请输入第三方平台领券链接，如：https://..."
          >
            <template #prefix>
              <LinkOutlined />
            </template>
          </a-input>
          <div class="form-item-tip">用户点击领取优惠券后将跳转到此链接，请确保链接完整且可访问</div>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="有效期开始" name="startTime">
              <a-date-picker 
                v-model:value="formData.startTime" 
                style="width: 100%"
                show-time
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="有效期结束" name="endTime">
              <a-date-picker 
                v-model:value="formData.endTime" 
                style="width: 100%"
                show-time
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="使用说明" name="description">
          <a-textarea 
            v-model:value="formData.description" 
            :rows="3"
            placeholder="请输入使用说明（选填）"
          />
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
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  LinkOutlined
} from '@ant-design/icons-vue'
import { getCouponList, createCoupon, updateCoupon, deleteCoupon } from '@/api/marketing'
import { getMerchantList } from '@/api/merchant'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const appStore = useAppStore()
const userStore = useUserStore()
const tableLoading = ref(false)

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

const searchForm = reactive({
  merchantId: undefined,
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '优惠券名称', dataIndex: 'name', key: 'name', width: 220 },
  { title: '跳转链接', dataIndex: 'link', key: 'link', width: 320, ellipsis: true },
  { title: '所属商家', dataIndex: 'merchantName', key: 'merchantName', width: 140 },
  { title: '有效期', dataIndex: 'validTime', key: 'validTime', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 180, fixed: 'right' }
]

const dataSource = ref([])
const modalVisible = ref(false)
const modalTitle = ref('新增优惠券')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const formData = reactive({
  id: null,
  merchantId: undefined,
  type: 'cash',
  name: '',
  value: 10,
  threshold: 0,
  totalCount: 100,
  limitPerUser: 1,
  startTime: null,
  endTime: null,
  description: '',
  link: ''
})

const formRules = {
  merchantId: [{ required: true, message: '请选择商家', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  link: [{ required: true, message: '请输入第三方平台跳转链接', trigger: 'blur' }]
}

const getTypeName = (type) => {
  const map = { discount: '折扣', cash: '代金', gift: '赠品' }
  return map[type] || type
}

const getStatusName = (status) => {
  const map = { active: '进行中', expired: '已过期', disabled: '已停用' }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = { active: 'success', expired: 'default', disabled: 'error' }
  return map[status] || 'default'
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize
    }
    const globalMerchantId = userStore.isAdmin ? appStore.merchantId : ''
    if (globalMerchantId) {
      params.merchantId = globalMerchantId
    } else if (searchForm.merchantId !== undefined) {
      params.merchantId = searchForm.merchantId
    }
    if (searchForm.status !== undefined) params.status = searchForm.status
    const res = await getCouponList(params)
    const list = res.list || res || []
    dataSource.value = Array.isArray(list) ? list : []
    pagination.total = res.total || 0
  } catch (e) {
    console.error('加载优惠券列表失败', e)
    dataSource.value = []
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.merchantId = undefined
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增优惠券'
  Object.assign(formData, {
    id: null,
    merchantId: undefined,
    type: 'cash',
    name: '',
    value: 10,
    threshold: 0,
    totalCount: 100,
    limitPerUser: 1,
    startTime: null,
    endTime: null,
    description: '',
    link: ''
  })
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑优惠券'
  Object.assign(formData, {
    id: record.id,
    merchantId: record.merchantId,
    type: record.type,
    name: record.name || record.title,
    value: record.value || record.amount,
    threshold: record.threshold || record.minAmount || 0,
    totalCount: record.totalCount,
    limitPerUser: 1,
    startTime: null,
    endTime: null,
    description: record.description || '',
    link: record.link || ''
  })
  modalVisible.value = true
}

const handleToggle = async (record) => {
  try {
    await updateCoupon(record.id, { status: record.status === 'active' ? 'disabled' : 'active' })
    message.success(record.status === 'active' ? '已停用' : '已启用')
    loadData()
  } catch (e) {
    console.error('更新优惠券状态失败', e)
  }
}

const handleDelete = async (record) => {
  try {
    await deleteCoupon(record.id)
    message.success('删除成功')
    loadData()
  } catch (e) {
    console.error('删除优惠券失败', e)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 构造提交数据，重点携带跳转链接
    const payload = {
      merchantId: formData.merchantId,
      type: formData.type,
      title: formData.name,
      name: formData.name,
      amount: formData.value,
      minAmount: formData.threshold,
      totalCount: formData.totalCount,
      startTime: formData.startTime,
      endTime: formData.endTime,
      link: formData.link
    }
    
    if (isEdit.value) {
      await updateCoupon(formData.id, payload)
      message.success('编辑成功')
    } else {
      await createCoupon(payload)
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
.coupons-page {
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

.coupon-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coupon-type-tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  color: #fff;
  
  &.discount {
    background: #52c41a;
  }
  
  &.cash {
    background: #ff4d4f;
  }
  
  &.gift {
    background: #faad14;
  }
}

.link-text {
  display: inline-block;
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: $primary-color;
  font-size: 12px;
  
  &:hover {
    text-decoration: underline;
  }
}

.empty-text {
  color: $text-tertiary;
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid $border-color;
}

.form-item-tip {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
}
</style>
