<template>
  <div class="promotion-detail-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-left">
        <a-button @click="goBack" type="text">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <div class="header-info">
          <span class="page-title">推广配置详情</span>
          <a-tag v-if="detail.type === 'coupon'" color="orange">优惠券</a-tag>
          <a-tag v-else color="blue">推广平台</a-tag>
        </div>
      </div>
      <div class="header-right">
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><SaveOutlined /></template>
          保存
        </a-button>
      </div>
    </div>

    <a-spin :spinning="loading">
      <div class="content-wrapper">
        <!-- 推广平台详情 -->
        <template v-if="detail.type !== 'coupon'">
          <!-- 平台基本信息 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <ShareAltOutlined />
              <span>平台基本信息</span>
            </div>
            <div class="platform-preview">
              <div class="platform-icon" :style="{ background: detail.platformColor || '#1677ff' }">
                {{ (detail.customName || detail.platformName)?.charAt(0) }}
              </div>
              <div class="platform-info">
                <div class="platform-name">{{ detail.customName || detail.platformName }}</div>
                <div class="platform-code" v-if="detail.platformCode">代码：{{ detail.platformCode }}</div>
                <div class="platform-desc">{{ detail.platformDescription }}</div>
              </div>
            </div>
            <a-divider style="margin: 16px 0" />
            <a-descriptions :column="2" size="small">
              <a-descriptions-item label="跳转方式">
                <a-tag :color="jumpModeColor(detail.jumpMode)">{{ jumpModeText(detail.jumpMode) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-switch :checked="detail.status === 1" @change="(v) => handleStatusChange(v)" />
              </a-descriptions-item>
              <a-descriptions-item label="排序">{{ detail.sort }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- 自定义展示 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <EditOutlined />
              <span>展示设置</span>
            </div>
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="自定义展示名称">
                    <a-input v-model:value="editForm.customName" :placeholder="`留空使用默认名称：${detail.platformName || ''}`" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="排序（数字越大越靠前）">
                    <a-input-number v-model:value="editForm.sort" :min="0" :max="9999" style="width: 100%" />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>

          <!-- 参数配置 -->
          <div class="card-wrapper form-card" v-if="detail.requiredParams?.length || detail.optionalParams?.length">
            <div class="card-title">
              <SettingOutlined />
              <span>跳转参数配置</span>
            </div>
            <div v-if="detail.requiredParams?.length" class="params-block">
              <div class="params-block-title">
                <span class="required-mark">*</span> 必填参数
              </div>
              <a-form layout="vertical">
                <a-form-item
                  v-for="param in detail.requiredParams"
                  :key="param.key"
                  :label="param.label || param.key"
                >
                  <a-input v-model:value="editForm.params[param.key]" :placeholder="param.placeholder" />
                </a-form-item>
              </a-form>
            </div>
            <div v-if="detail.optionalParams?.length" class="params-block">
              <div class="params-block-title">可选参数</div>
              <a-form layout="vertical">
                <a-form-item
                  v-for="param in detail.optionalParams"
                  :key="param.key"
                  :label="param.label || param.key"
                >
                  <a-input v-model:value="editForm.params[param.key]" :placeholder="param.placeholder" />
                </a-form-item>
              </a-form>
            </div>
          </div>

          <!-- 跳转预览 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <EyeOutlined />
              <span>跳转预览</span>
            </div>
            <div class="preview-list">
              <div class="preview-item" v-if="previewScheme">
                <div class="preview-label">Scheme URL</div>
                <div class="preview-value">{{ previewScheme }}</div>
              </div>
              <div class="preview-item" v-if="previewWeb">
                <div class="preview-label">H5 链接</div>
                <div class="preview-value">{{ previewWeb }}</div>
              </div>
              <div class="preview-item" v-if="previewFallback">
                <div class="preview-label">兜底链接（分享短链）</div>
                <div class="preview-value">{{ previewFallback }}</div>
              </div>
              <div v-if="!previewScheme && !previewWeb && !previewFallback" class="empty-preview">
                暂无跳转链接，请先填写参数
              </div>
            </div>
          </div>
        </template>

        <!-- 优惠券详情 -->
        <template v-else>
          <!-- 优惠券基本信息 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <GiftOutlined />
              <span>优惠券基本信息</span>
            </div>
            <div class="coupon-preview">
              <div class="coupon-icon-large">
                <span class="coupon-icon-text">¥</span>
              </div>
              <div class="coupon-info">
                <div class="coupon-name">{{ detail.customName || detail.couponName }}</div>
                <div class="coupon-type">{{ couponTypeText(detail.couponType) }}</div>
              </div>
              <div class="coupon-value-block">
                <div class="coupon-value">¥{{ Number(detail.couponValue || 0) }}</div>
                <div class="coupon-threshold" v-if="Number(detail.couponThreshold || 0) > 0">满{{ detail.couponThreshold }}可用</div>
                <div class="coupon-threshold" v-else>无门槛</div>
              </div>
            </div>
            <a-divider style="margin: 16px 0" />
            <a-descriptions :column="2" size="small">
              <a-descriptions-item label="上架状态">
                <a-tag :color="detail.couponStatus === 1 ? 'green' : 'default'">
                  {{ detail.couponStatus === 1 ? '上架' : '下架' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="小程序展示">
                <a-switch :checked="detail.status === 1" @change="(v) => handleStatusChange(v)" />
              </a-descriptions-item>
              <a-descriptions-item label="总数量">{{ detail.couponTotalCount || 0 }}</a-descriptions-item>
              <a-descriptions-item label="剩余数量">{{ detail.couponRemainCount || 0 }}</a-descriptions-item>
              <a-descriptions-item label="有效期起">{{ formatDate(detail.couponValidStart) }}</a-descriptions-item>
              <a-descriptions-item label="有效期止">{{ formatDate(detail.couponValidEnd) }}</a-descriptions-item>
              <a-descriptions-item label="排序">{{ detail.sort }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- 展示设置 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <EditOutlined />
              <span>展示设置</span>
            </div>
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="自定义展示名称">
                    <a-input v-model:value="editForm.customName" :placeholder="`留空使用默认名称：${detail.couponName || ''}`" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="排序（数字越大越靠前）">
                    <a-input-number v-model:value="editForm.sort" :min="0" :max="9999" style="width: 100%" />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>

          <!-- 库存进度 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <BarChartOutlined />
              <span>库存进度</span>
            </div>
            <div class="stock-progress">
              <a-progress
                type="dashboard"
                :percent="couponRemainPercent"
                :stroke-color="couponRemainPercent < 20 ? '#ff4d4f' : '#52c41a'"
              />
              <div class="stock-info">
                <div class="stock-num">
                  剩余 <span class="num">{{ detail.couponRemainCount || 0 }}</span>
                </div>
                <div class="stock-total">
                  总数 <span class="num">{{ detail.couponTotalCount || 0 }}</span>
                </div>
                <div class="stock-used">
                  已发 <span class="num">{{ (detail.couponTotalCount || 0) - (detail.couponRemainCount || 0) }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  SaveOutlined,
  ShareAltOutlined,
  GiftOutlined,
  EditOutlined,
  SettingOutlined,
  EyeOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import {
  getMerchantPromotionConfigDetail,
  updateMerchantPromotionConfig
} from '@/api/marketing'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const detail = ref({})

const editForm = reactive({
  customName: '',
  customIcon: '',
  params: {},
  sort: 0,
  status: 1
})

const previewScheme = computed(() => {
  if (detail.value.type === 'coupon') return ''
  const params = editForm.params || {}
  const template = detail.value.schemeTemplate || ''
  if (!template) return ''
  return fillTemplate(template, params)
})

const previewWeb = computed(() => {
  if (detail.value.type === 'coupon') return ''
  const params = editForm.params || {}
  const template = detail.value.webUrlTemplate || ''
  if (!template) return ''
  return fillTemplate(template, params)
})

const previewFallback = computed(() => {
  if (detail.value.type === 'coupon') return ''
  return editForm.params?.share_url || ''
})

const couponRemainPercent = computed(() => {
  const total = Number(detail.value.couponTotalCount || 0)
  const remain = Number(detail.value.couponRemainCount || 0)
  if (total === 0) return 0
  return Math.round((remain / total) * 100)
})

const fillTemplate = (template, params) => {
  if (!template) return ''
  let result = template
  for (const key of Object.keys(params || {})) {
    const value = params[key]
    if (value !== undefined && value !== null && value !== '') {
      result = result.replace(new RegExp(`\\{${key}\\}`, 'g'), value)
    }
  }
  return result
}

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

const formatDate = (date) => {
  if (!date) return '--'
  const d = new Date(date)
  if (isNaN(d.getTime())) return String(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatDateTime = (date) => {
  if (!date) return '--'
  const d = new Date(date)
  if (isNaN(d.getTime())) return String(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const loadDetail = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await getMerchantPromotionConfigDetail(id)
    if (res) {
      detail.value = res
      editForm.customName = res.customName || ''
      editForm.customIcon = res.customIcon || ''

      // 解析 params（后端可能返回JSON字符串）
      let paramsObj = {}
      if (res.params) {
        if (typeof res.params === 'string') {
          try {
            paramsObj = JSON.parse(res.params)
          } catch (e) {
            paramsObj = {}
          }
        } else {
          paramsObj = res.params
        }
      }
      editForm.params = JSON.parse(JSON.stringify(paramsObj))
      editForm.sort = res.sort || 0
      editForm.status = res.status ?? 1

      // 解析 requiredParams 和 optionalParams（可能是JSON字符串）
      if (typeof res.requiredParams === 'string') {
        try {
          res.requiredParams = JSON.parse(res.requiredParams)
        } catch (e) {
          res.requiredParams = []
        }
      }
      if (typeof res.optionalParams === 'string') {
        try {
          res.optionalParams = JSON.parse(res.optionalParams)
        } catch (e) {
          res.optionalParams = []
        }
      }

      // 确保所有参数key都在editForm.params中初始化
      ;[...(res.requiredParams || []), ...(res.optionalParams || [])].forEach(param => {
        if (param.key && editForm.params[param.key] === undefined) {
          editForm.params[param.key] = ''
        }
      })
    }
  } catch (e) {
    message.error('加载详情失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (checked) => {
  try {
    await updateMerchantPromotionConfig(route.params.id, { status: checked ? 1 : 0 })
    detail.value.status = checked ? 1 : 0
    editForm.status = checked ? 1 : 0
    message.success(checked ? '已开启展示' : '已关闭展示')
  } catch (e) {
    console.error('状态更新失败', e)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const payload = {
      customName: editForm.customName,
      customIcon: editForm.customIcon,
      sort: editForm.sort,
      status: editForm.status
    }
    if (detail.value.type !== 'coupon') {
      // 后端 params 字段为字符串类型，需序列化
      payload.params = JSON.stringify(editForm.params)
    }
    await updateMerchantPromotionConfig(route.params.id, payload)
    message.success('保存成功')
    loadDetail()
  } catch (e) {
    message.error('保存失败')
    console.error(e)
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.promotion-detail-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid $border-color;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: $text-color;
}

.content-wrapper {
  max-width: 900px;
}

.form-card {
  padding: 20px 24px;
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid $border-color;

  .anticon {
    color: $primary-color;
    font-size: 18px;
  }
}

.platform-preview {
  display: flex;
  align-items: center;
  gap: 16px;
}

.platform-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  flex-shrink: 0;
}

.platform-info {
  flex: 1;
  min-width: 0;
}

.platform-name {
  font-size: 18px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 4px;
}

.platform-code {
  font-size: 12px;
  color: $text-tertiary;
  margin-bottom: 4px;
}

.platform-desc {
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.6;
}

.coupon-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border-radius: 10px;
  border: 1px dashed #ffccc7;
}

.coupon-icon-large {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.coupon-icon-text {
  font-size: 28px;
  font-weight: bold;
}

.coupon-info {
  flex: 1;
  min-width: 0;
}

.coupon-name {
  font-size: 18px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 4px;
}

.coupon-type {
  font-size: 12px;
  color: $text-tertiary;
}

.coupon-value-block {
  text-align: right;
}

.coupon-value {
  font-size: 28px;
  font-weight: bold;
  color: #ff4d4f;
  line-height: 1;
}

.coupon-threshold {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 4px;
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

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-item {
  padding: 12px 16px;
  background: #f0f5ff;
  border-radius: 8px;
  border-left: 3px solid $primary-color;
}

.preview-label {
  font-size: 12px;
  color: $text-tertiary;
  margin-bottom: 6px;
  font-weight: 500;
}

.preview-value {
  font-size: 13px;
  color: $text-color;
  word-break: break-all;
  font-family: monospace;
  line-height: 1.6;
}

.empty-preview {
  padding: 24px;
  text-align: center;
  color: $text-tertiary;
  font-size: 14px;
}

.stock-progress {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 0;
}

.stock-info {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .num {
    font-size: 20px;
    font-weight: 600;
    color: $text-color;
    margin-left: 4px;
  }
}

.stock-num .num {
  color: #52c41a;
}

.stock-used .num {
  color: #ff4d4f;
}
</style>
