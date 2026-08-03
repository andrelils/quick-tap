<template>
  <div class="merchant-promotion-page">
    <div class="page-header">
      <div class="page-title">我的推广平台</div>
      <div class="page-desc">
        统一管理您的推广平台与优惠券展示配置，配置后小程序中将展示对应入口供用户点击使用。
        <span v-if="userStore.isSuperAdmin" class="page-tip">（超管视图：{{ currentMerchantName || '请先在右上角选择商家' }}）</span>
      </div>
    </div>

    <div v-if="!currentMerchantId" class="empty-card">
      <a-empty description="请先在右上角选择商家后再配置推广平台" />
    </div>

    <template v-else>
      <div class="card-wrapper table-card">
        <div class="table-header">
          <div class="table-title">
            <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" class="type-tabs">
              <a-tab-pane key="platform">
                <template #tab>
                  <span>
                    <ShareAltOutlined />
                    推广平台
                    <a-tag color="blue" size="small">{{ platformList.length }}</a-tag>
                  </span>
                </template>
              </a-tab-pane>
              <a-tab-pane key="coupon">
                <template #tab>
                  <span>
                    <GiftOutlined />
                    优惠券
                    <a-tag color="orange" size="small">{{ couponList.length }}</a-tag>
                  </span>
                </template>
              </a-tab-pane>
            </a-tabs>
          </div>
          <div class="table-actions">
            <a-button type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              {{ activeTab === 'coupon' ? '配置优惠券' : '配置新平台' }}
            </a-button>
          </div>
        </div>

        <!-- 推广平台表格 -->
        <a-table
          v-if="activeTab === 'platform'"
          :columns="platformColumns"
          :data-source="platformList"
          :loading="loading"
          :pagination="false"
          :row-key="record => record.id"
          :scroll="{ x: 1100 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'platformName'">
              <div class="platform-cell">
                <div class="platform-icon" :style="{ background: record.platformColor }">
                  {{ (record.customName || record.platformName)?.charAt(0) }}
                </div>
                <div>
                  <div class="platform-name-text">{{ record.customName || record.platformName }}</div>
                  <div class="platform-desc-text">{{ record.platformDescription }}</div>
                </div>
              </div>
            </template>
            <template v-else-if="column.key === 'jumpMode'">
              <a-tag :color="jumpModeColor(record.jumpMode)">{{ jumpModeText(record.jumpMode) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'params'">
              <div class="params-cell">
                <div v-for="(value, key) in record.params" :key="key" class="param-item">
                  <span class="param-key">{{ key }}:</span>
                  <span class="param-value">{{ value || '--' }}</span>
                </div>
                <span v-if="!record.params || Object.keys(record.params).length === 0" class="text-tertiary">未配置</span>
              </div>
            </template>
            <template v-else-if="column.key === 'preview'">
              <a-tooltip :title="buildPreviewUrl(record)">
                <span class="text-ellipsis">{{ buildPreviewUrl(record) || '未生成' }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-switch :checked="record.status === 1" @change="(v) => handleStatusChange(record, v)" />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space size="small">
                <a type="link" size="small" @click="handleDetail(record)">详情</a>
                <a type="link" size="small" @click="handleEdit(record)">编辑</a>
                <a-popconfirm title="确定要删除该配置吗？" @confirm="handleDelete(record)">
                  <a type="link" size="small" :danger="true">删除</a>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>

        <!-- 优惠券表格 -->
        <a-table
          v-else
          :columns="couponColumns"
          :data-source="couponList"
          :loading="loading"
          :pagination="false"
          :row-key="record => record.id"
          :scroll="{ x: 1000 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'couponName'">
              <div class="platform-cell">
                <div class="coupon-icon" :class="couponTypeClass(record.couponType)">
                  <span class="coupon-icon-text">¥</span>
                </div>
                <div>
                  <div class="platform-name-text">{{ record.customName || record.couponName }}</div>
                  <div class="platform-desc-text">{{ couponTypeText(record.couponType) }}</div>
                </div>
              </div>
            </template>
            <template v-else-if="column.key === 'couponValue'">
              <div class="coupon-value-cell">
                <span class="value-amount">¥{{ Number(record.couponValue || 0) }}</span>
                <span class="value-threshold" v-if="Number(record.couponThreshold || 0) > 0">满{{ record.couponThreshold }}可用</span>
                <span class="value-threshold" v-else>无门槛</span>
              </div>
            </template>
            <template v-else-if="column.key === 'couponCount'">
              <div class="count-cell">
                <div>剩余 <span class="count-num">{{ record.couponRemainCount || 0 }}</span> / 总 <span class="count-num">{{ record.couponTotalCount || 0 }}</span></div>
                <a-progress
                  :percent="couponRemainPercent(record)"
                  size="small"
                  :stroke-color="couponRemainPercent(record) < 20 ? '#ff4d4f' : '#52c41a'"
                />
              </div>
            </template>
            <template v-else-if="column.key === 'couponValid'">
              <div class="valid-cell">
                <div>{{ formatDate(record.couponValidStart) }}</div>
                <div>~</div>
                <div>{{ formatDate(record.couponValidEnd) }}</div>
              </div>
            </template>
            <template v-else-if="column.key === 'couponStatus'">
              <a-tag :color="record.couponStatus === 1 ? 'green' : 'default'">
                {{ record.couponStatus === 1 ? '上架' : '下架' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-switch :checked="record.status === 1" @change="(v) => handleStatusChange(record, v)" />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space size="small">
                <a type="link" size="small" @click="handleDetail(record)">详情</a>
                <a type="link" size="small" @click="handleEdit(record)">编辑</a>
                <a-popconfirm title="确定要删除该配置吗？" @confirm="handleDelete(record)">
                  <a type="link" size="small" :danger="true">删除</a>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </template>

    <!-- 配置弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="640px"
      :footer="null"
      destroy-on-close
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <!-- 推广平台选择 -->
        <template v-if="formData.type === 'platform'">
          <a-form-item label="选择推广平台" name="platformId">
            <a-select
              v-model:value="formData.platformId"
              placeholder="请选择要配置的平台"
              :disabled="isEdit"
              show-search
              :filter-option="filterPlatform"
              @change="handlePlatformChange"
            >
              <a-select-option v-for="p in availablePlatforms" :key="p.id" :value="p.id">
                <div class="platform-option">
                  <span class="platform-option-dot" :style="{ background: p.color }"></span>
                  {{ p.name }}（{{ p.code }}）
                </div>
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-alert
            v-if="selectedPlatform"
            type="info"
            show-icon
            :message="selectedPlatform.description"
            style="margin-bottom: 16px"
          />

          <a-form-item label="自定义展示名称（可选）">
            <a-input v-model:value="formData.customName" :placeholder="`留空使用默认名称：${selectedPlatform?.name || ''}`" />
          </a-form-item>

          <div v-if="formData.requiredParams && formData.requiredParams.length > 0" class="params-block">
            <div class="params-block-title">
              <span class="required-mark">*</span> 必填参数
            </div>
            <a-form-item
              v-for="param in formData.requiredParams"
              :key="param.key"
              :label="param.label"
              :name="['params', param.key]"
              :rules="[{ required: true, message: `${param.label}不能为空`, trigger: 'blur' }]"
            >
              <a-input v-model:value="formData.params[param.key]" :placeholder="param.placeholder" />
            </a-form-item>
          </div>

          <div v-if="formData.optionalParams && formData.optionalParams.length > 0" class="params-block">
            <div class="params-block-title">可选参数</div>
            <a-form-item
              v-for="param in formData.optionalParams"
              :key="param.key"
              :label="param.label"
              :name="['params', param.key]"
            >
              <a-input v-model:value="formData.params[param.key]" :placeholder="param.placeholder" />
            </a-form-item>
          </div>

          <div v-if="previewUrl" class="preview-block">
            <div class="preview-title">配置后预览：</div>
            <div class="preview-url">{{ previewUrl }}</div>
          </div>
        </template>

        <!-- 优惠券选择 -->
        <template v-else>
          <a-form-item label="选择优惠券" name="couponId">
            <a-select
              v-model:value="formData.couponId"
              placeholder="请选择要展示的优惠券"
              :disabled="isEdit"
              show-search
              :filter-option="filterCoupon"
            >
              <a-select-option v-for="c in availableCoupons" :key="c.id" :value="c.id">
                <div class="coupon-option">
                  <span class="coupon-option-value">¥{{ Number(c.value || 0) }}</span>
                  <span class="coupon-option-name">{{ c.name }}</span>
                  <span class="coupon-option-count">剩{{ c.remainCount || 0 }}张</span>
                </div>
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-alert
            v-if="selectedCoupon"
            type="info"
            show-icon
            style="margin-bottom: 16px"
        >
            <template #message>
              <div class="coupon-info-alert">
                <div><strong>{{ selectedCoupon.name }}</strong></div>
                <div>面值 ¥{{ Number(selectedCoupon.value || 0) }} · {{ Number(selectedCoupon.threshold || 0) > 0 ? `满${selectedCoupon.threshold}可用` : '无门槛' }}</div>
                <div>有效期：{{ formatDate(selectedCoupon.validStart) }} ~ {{ formatDate(selectedCoupon.validEnd) }}</div>
              </div>
            </template>
          </a-alert>

          <a-form-item label="自定义展示名称（可选）">
            <a-input v-model:value="formData.customName" :placeholder="`留空使用默认名称：${selectedCoupon?.name || ''}`" />
          </a-form-item>
        </template>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排序（数字越大越靠前）">
              <a-input-number v-model:value="formData.sort" :min="0" :max="9999" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">保存</a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ShareAltOutlined,
  GiftOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import {
  getMerchantPromotionConfigs,
  getAvailablePlatforms,
  getAvailableCoupons,
  upsertMerchantPromotionConfig,
  updateMerchantPromotionConfig,
  deleteMerchantPromotionConfig
} from '@/api/marketing'

const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()

const loading = ref(false)
const activeTab = ref('platform')
const allDataSource = ref([])
const availablePlatforms = ref([])
const availableCoupons = ref([])

const modalVisible = ref(false)
const modalTitle = ref('配置推广平台')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const currentMerchantId = computed(() => {
  if (userStore.isMerchant) {
    return userStore.userInfo?.merchantId || userStore.userInfo?.merchant_id
  }
  return appStore.merchantId
})

const currentMerchantName = computed(() => appStore.currentMerchant?.name || '')

const platformList = computed(() => allDataSource.value.filter(item => item.type !== 'coupon'))
const couponList = computed(() => allDataSource.value.filter(item => item.type === 'coupon'))

const selectedPlatform = computed(() => {
  return availablePlatforms.value.find(p => p.id === formData.platformId) || null
})

const selectedCoupon = computed(() => {
  return availableCoupons.value.find(c => c.id === formData.couponId) || null
})

const previewUrl = computed(() => {
  if (!selectedPlatform.value) return ''
  const params = formData.params || {}
  const platform = selectedPlatform.value
  let url = ''
  if (platform.jumpMode === 'scheme') {
    url = platform.schemeTemplate
  } else if (platform.jumpMode === 'webview') {
    url = platform.webUrlTemplate
  }
  if (!url) return ''
  let result = url
  for (const key of Object.keys(params)) {
    const value = params[key]
    if (value) {
      result = result.replace(new RegExp(`\\{${key}\\}`, 'g'), value)
    }
  }
  return result
})

const defaultFormData = () => ({
  type: 'platform',
  platformId: null,
  couponId: null,
  customName: '',
  customIcon: '',
  params: {},
  requiredParams: [],
  optionalParams: [],
  sort: 0,
  status: 1
})

const formData = reactive(defaultFormData())

const formRules = computed(() => {
  if (formData.type === 'coupon') {
    return {
      couponId: [{ required: true, message: '请选择优惠券', trigger: 'change' }]
    }
  }
  return {
    platformId: [{ required: true, message: '请选择推广平台', trigger: 'change' }]
  }
})

const platformColumns = [
  { title: '平台', key: 'platformName', width: 240, fixed: 'left' },
  { title: '跳转方式', key: 'jumpMode', width: 120 },
  { title: '已填参数', key: 'params', width: 280 },
  { title: '跳转预览', key: 'preview', width: 280, ellipsis: true },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '展示', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

const couponColumns = [
  { title: '优惠券', key: 'couponName', width: 240, fixed: 'left' },
  { title: '面值', key: 'couponValue', width: 140 },
  { title: '库存', key: 'couponCount', width: 200 },
  { title: '有效期', key: 'couponValid', width: 200 },
  { title: '上架状态', key: 'couponStatus', width: 100 },
  { title: '展示', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

const jumpModeText = (mode) => {
  const map = { scheme: 'URL Scheme', webview: 'H5链接', miniprogram: '小程序', copy: '复制链接' }
  return map[mode] || mode
}

const jumpModeColor = (mode) => {
  const map = { scheme: 'blue', webview: 'green', miniprogram: 'purple', copy: 'orange' }
  return map[mode] || 'default'
}

const couponTypeText = (type) => {
  const map = { amount: '满减券', discount: '折扣券', gift: '礼品券' }
  return map[type] || type || '优惠券'
}

const couponTypeClass = (type) => {
  const map = { amount: 'type-amount', discount: 'type-discount', gift: 'type-gift' }
  return map[type] || 'type-amount'
}

const couponRemainPercent = (record) => {
  const total = Number(record.couponTotalCount || 0)
  const remain = Number(record.couponRemainCount || 0)
  if (total === 0) return 0
  return Math.round((remain / total) * 100)
}

const formatDate = (date) => {
  if (!date) return '--'
  const d = new Date(date)
  if (isNaN(d.getTime())) return String(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const filterPlatform = (input, option) => {
  const text = option.children?.[0]?.children
  if (typeof text === 'string') {
    return text.toLowerCase().includes(input.toLowerCase())
  }
  return false
}

const filterCoupon = (input, option) => {
  const name = option.children?.[0]?.children?.[1]?.children
  if (typeof name === 'string') {
    return name.toLowerCase().includes(input.toLowerCase())
  }
  return false
}

const loadData = async () => {
  if (!currentMerchantId.value) {
    allDataSource.value = []
    return
  }
  loading.value = true
  try {
    const res = await getMerchantPromotionConfigs(currentMerchantId.value)
    allDataSource.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载商家推广配置失败', e)
    allDataSource.value = []
  } finally {
    loading.value = false
  }
}

const loadAvailablePlatforms = async () => {
  try {
    const res = await getAvailablePlatforms()
    availablePlatforms.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载可用平台失败', e)
    availablePlatforms.value = []
  }
}

const loadAvailableCoupons = async () => {
  if (!currentMerchantId.value) {
    availableCoupons.value = []
    return
  }
  try {
    const res = await getAvailableCoupons(currentMerchantId.value)
    availableCoupons.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载可用优惠券失败', e)
    availableCoupons.value = []
  }
}

const buildPreviewUrl = (record) => {
  if (!record) return ''
  const params = record.params || {}
  let template = ''
  if (record.jumpMode === 'scheme') {
    template = record.schemeTemplate
  } else if (record.jumpMode === 'webview') {
    template = record.webUrlTemplate
  }
  if (!template) return params.share_url || ''
  let result = template
  for (const key of Object.keys(params)) {
    const value = params[key]
    if (value) {
      result = result.replace(new RegExp(`\\{${key}\\}`, 'g'), value)
    }
  }
  return result
}

const resetForm = () => {
  Object.assign(formData, defaultFormData())
  formData.params = {}
  formData.requiredParams = []
  formData.optionalParams = []
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  resetForm()
  formData.type = activeTab.value
  if (activeTab.value === 'platform') {
    modalTitle.value = '配置推广平台'
    if (availablePlatforms.value.length === 0) {
      loadAvailablePlatforms()
    }
  } else {
    modalTitle.value = '配置优惠券展示'
    if (availableCoupons.value.length === 0) {
      loadAvailableCoupons()
    }
  }
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  editId.value = record.id
  resetForm()
  formData.type = record.type || 'platform'
  if (formData.type === 'platform') {
    modalTitle.value = '编辑推广平台配置'
    if (availablePlatforms.value.length === 0) {
      loadAvailablePlatforms()
    }
    Object.assign(formData, {
      platformId: record.platformId,
      customName: record.customName || '',
      customIcon: record.customIcon || '',
      params: JSON.parse(JSON.stringify(record.params || {})),
      requiredParams: JSON.parse(JSON.stringify(record.requiredParams || [])),
      optionalParams: JSON.parse(JSON.stringify(record.optionalParams || [])),
      sort: record.sort,
      status: record.status
    })
  } else {
    modalTitle.value = '编辑优惠券展示配置'
    if (availableCoupons.value.length === 0) {
      loadAvailableCoupons()
    }
    Object.assign(formData, {
      couponId: record.couponId,
      customName: record.customName || '',
      sort: record.sort,
      status: record.status
    })
  }
  modalVisible.value = true
}

const handleDetail = (record) => {
  router.push(`/marketing/promotion-detail/${record.id}`)
}

const handleTabChange = (key) => {
  if (key === 'coupon' && availableCoupons.value.length === 0) {
    loadAvailableCoupons()
  }
}

const handlePlatformChange = (platformId) => {
  const platform = availablePlatforms.value.find(p => p.id === platformId)
  if (platform) {
    formData.requiredParams = JSON.parse(JSON.stringify(platform.requiredParams || []))
    formData.optionalParams = JSON.parse(JSON.stringify(platform.optionalParams || []))
    formData.params = formData.params || {}
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      merchantId: currentMerchantId.value,
      type: formData.type,
      params: formData.params,
      customName: formData.customName,
      customIcon: formData.customIcon,
      sort: formData.sort,
      status: formData.status
    }
    if (formData.type === 'coupon') {
      payload.couponId = formData.couponId
      delete payload.params
    } else {
      payload.platformId = formData.platformId
    }
    if (isEdit.value) {
      await upsertMerchantPromotionConfig(payload)
      message.success('更新成功')
    } else {
      await upsertMerchantPromotionConfig(payload)
      message.success(formData.type === 'coupon' ? '配置成功' : '配置成功')
    }
    modalVisible.value = false
    loadData()
  } catch (e) {
    if (e?.errorFields) return
    console.error('保存失败', e)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (record) => {
  try {
    await deleteMerchantPromotionConfig(record.id)
    message.success('删除成功')
    loadData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

const handleStatusChange = async (record, checked) => {
  try {
    await updateMerchantPromotionConfig(record.id, { status: checked ? 1 : 0 })
    record.status = checked ? 1 : 0
    message.success(checked ? '已开启展示' : '已关闭展示')
  } catch (e) {
    console.error('状态更新失败', e)
  }
}

watch(currentMerchantId, (newId) => {
  if (newId) {
    loadData()
    loadAvailableCoupons()
  }
})

onMounted(() => {
  loadAvailablePlatforms()
  loadAvailableCoupons()
  loadData()
})
</script>

<style lang="scss" scoped>
.merchant-promotion-page {
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
    line-height: 1.6;
  }

  .page-tip {
    color: $primary-color;
    margin-left: 8px;
  }
}

.empty-card {
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  padding: 60px 24px;
}

.card-wrapper {
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  padding: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;

  .table-title {
    flex: 1;
  }
}

.type-tabs {
  :deep(.ant-tabs-nav) {
    margin-bottom: 0;
  }

  :deep(.ant-tag) {
    margin-left: 8px;
    font-size: 12px;
    line-height: 16px;
    padding: 0 6px;
  }
}

.platform-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.platform-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.coupon-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;

  &.type-amount {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  }

  &.type-discount {
    background: linear-gradient(135deg, #feca57 0%, #ff9f43 100%);
  }

  &.type-gift {
    background: linear-gradient(135deg, #54a0ff 0%, #2e86de 100%);
  }
}

.coupon-icon-text {
  font-size: 18px;
}

.platform-name-text {
  font-size: 14px;
  font-weight: 500;
  color: $text-color;
}

.platform-desc-text {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 2px;
}

.coupon-value-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .value-amount {
    font-size: 18px;
    font-weight: 600;
    color: #ff4d4f;
  }

  .value-threshold {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.count-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .count-num {
    font-weight: 600;
    color: $text-color;
  }
}

.valid-cell {
  font-size: 12px;
  color: $text-secondary;
  line-height: 1.6;
}

.params-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.param-item {
  font-size: 12px;

  .param-key {
    color: $text-tertiary;
    margin-right: 4px;
  }

  .param-value {
    color: $text-secondary;
    word-break: break-all;
  }
}

.text-tertiary {
  color: $text-tertiary;
}

.text-ellipsis {
  display: inline-block;
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: $text-secondary;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.platform-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.platform-option-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.coupon-option {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;

  .coupon-option-value {
    color: #ff4d4f;
    font-weight: 600;
    min-width: 60px;
  }

  .coupon-option-name {
    flex: 1;
    color: $text-color;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .coupon-option-count {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.coupon-info-alert {
  font-size: 13px;
  line-height: 1.8;

  > div {
    margin-top: 2px;
  }
}

.params-block {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.params-block-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 12px;
}

.required-mark {
  color: $error-color;
  margin-right: 4px;
}

.preview-block {
  margin: 16px 0;
  padding: 12px;
  background: #f0f5ff;
  border-radius: 8px;
  border-left: 3px solid $primary-color;
}

.preview-title {
  font-size: 12px;
  color: $text-tertiary;
  margin-bottom: 4px;
}

.preview-url {
  font-size: 13px;
  color: $text-color;
  word-break: break-all;
  font-family: monospace;
}
</style>
