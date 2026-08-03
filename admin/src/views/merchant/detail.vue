<template>
  <div class="merchant-detail-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-left">
        <a-button v-if="!isMerchant" @click="goBack" type="text">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <div class="header-info">
          <span class="page-title">{{ isMerchant ? '我的商家' : '商家详情管理' }}</span>
          <a-tag v-if="merchantData.status === 1" color="success">正常</a-tag>
          <a-tag v-else color="default">禁用</a-tag>
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
        <!-- 左侧：商家信息预览 -->
        <div class="preview-section">
          <div class="preview-card">
            <div class="preview-header">
              <img v-if="formData.logo" :src="getLogoUrl(formData.logo)" class="preview-logo" />
              <div v-else class="preview-logo-placeholder">{{ formData.name?.charAt(0) || '?' }}</div>
              <div class="preview-meta">
                <div class="preview-name">{{ formData.name || '未命名商家' }}</div>
                <div class="preview-sub">{{ formData.address || '暂无地址' }}</div>
                <div class="preview-sub" v-if="formData.businessHours">{{ formData.businessHours }}</div>
              </div>
            </div>
            <a-divider style="margin: 12px 0" />
            <div class="preview-item">
              <span class="preview-label">联系人</span>
              <span class="preview-value">{{ formData.contactName || '--' }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">联系电话</span>
              <span class="preview-value">{{ formData.contactPhone || '--' }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">老板微信</span>
              <span class="preview-value">{{ formData.bossWechat || '--' }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">WiFi</span>
              <span class="preview-value">{{ formData.wifiName || '--' }} / {{ formData.wifiPassword || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧：表单编辑 -->
        <div class="form-section">
          <!-- 基本信息 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <ShopOutlined />
              <span>基本信息</span>
            </div>
            <a-form :model="formData" layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="商家名称" name="name" :rules="[{ required: true, message: '请输入商家名称' }]">
                    <a-input v-model:value="formData.name" placeholder="请输入商家名称" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="营业时间" name="businessHours">
                    <a-input v-model:value="formData.businessHours" placeholder="如 09:00-22:00" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="商家地址" name="address">
                <a-input v-model:value="formData.address" placeholder="请输入商家地址" />
              </a-form-item>
              <a-form-item label="商家简介" name="description">
                <a-textarea v-model:value="formData.description" placeholder="请输入商家简介" :rows="3" show-count :max-length="500" />
              </a-form-item>
            </a-form>
          </div>

          <!-- 联系方式 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <PhoneOutlined />
              <span>联系方式</span>
            </div>
            <a-form :model="formData" layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="联系人" name="contactName">
                    <a-input v-model:value="formData.contactName" placeholder="请输入联系人姓名" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="联系电话" name="contactPhone">
                    <a-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="老板微信" name="bossWechat" extra="用于小程序一键加老板微信工具">
                <a-input v-model:value="formData.bossWechat" placeholder="请输入老板微信号" allow-clear />
              </a-form-item>
            </a-form>
          </div>

          <!-- WiFi 配置 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <WifiOutlined />
              <span>WiFi 配置</span>
            </div>
            <a-form :model="formData" layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="WiFi 名称" name="wifiName">
                    <a-input v-model:value="formData.wifiName" placeholder="请输入 WiFi SSID" allow-clear />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="WiFi 密码" name="wifiPassword">
                    <a-input v-model:value="formData.wifiPassword" placeholder="请输入 WiFi 密码" allow-clear />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>

          <!-- 图片管理 -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <PictureOutlined />
              <span>图片管理</span>
            </div>
            <a-form :model="formData" layout="vertical">
              <a-form-item label="商家 Logo" name="logo">
                <a-upload
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  list-type="picture-card"
                  :max-count="1"
                  v-model:file-list="logoFileList"
                  @change="handleLogoChange"
                  @remove="handleLogoRemove"
                  :before-upload="beforeUpload"
                >
                  <div>
                    <UploadOutlined />
                    <div style="margin-top: 8px">上传 Logo</div>
                  </div>
                </a-upload>
              </a-form-item>
              <a-form-item label="首页轮播图" name="bannerImages" extra="最多 5 张，展示在小程序商家页顶部轮播图">
                <a-upload
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  list-type="picture-card"
                  :max-count="5"
                  multiple
                  v-model:file-list="bannerFileList"
                  @change="handleBannerChange"
                  @remove="handleBannerRemove"
                  :before-upload="beforeUpload"
                >
                  <div v-if="bannerFileList.length < 5">
                    <UploadOutlined />
                    <div style="margin-top: 8px">上传</div>
                  </div>
                </a-upload>
              </a-form-item>
              <a-form-item label="店铺图片" name="shopImages" extra="最多 9 张，展示在商家介绍页">
                <a-upload
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  list-type="picture-card"
                  :max-count="9"
                  multiple
                  v-model:file-list="shopFileList"
                  @change="handleShopChange"
                  @remove="handleShopRemove"
                  :before-upload="beforeUpload"
                >
                  <div v-if="shopFileList.length < 9">
                    <UploadOutlined />
                    <div style="margin-top: 8px">上传</div>
                  </div>
                </a-upload>
              </a-form-item>
            </a-form>
          </div>

          <!-- 推广配置（展示开关） -->
          <div class="card-wrapper form-card">
            <div class="card-title">
              <ShareAltOutlined />
              <span>推广配置</span>
              <a-tag color="blue" size="small">{{ promotionConfigs.length }} 项</a-tag>
              <a-button type="link" size="small" style="margin-left: auto" @click="goToPromotionManage">
                前往管理
                <template #icon><ArrowRightOutlined /></template>
              </a-button>
            </div>
            <a-spin :spinning="promotionLoading">
              <a-empty
                v-if="promotionConfigs.length === 0"
                description="暂未配置任何推广平台或优惠券"
                :image="emptyImage"
              >
                <a-button type="primary" size="small" @click="goToPromotionManage">
                  <template #icon><PlusOutlined /></template>
                  立即配置
                </a-button>
              </a-empty>
              <div v-else class="promotion-list">
                <!-- 推广平台 -->
                <div
                  v-for="item in platformConfigs"
                  :key="'p-' + item.id"
                  class="promotion-item"
                >
                  <div class="promotion-icon" :style="{ background: item.platformColor || '#1677ff' }">
                    {{ (item.customName || item.platformName)?.charAt(0) }}
                  </div>
                  <div class="promotion-info">
                    <div class="promotion-name">{{ item.customName || item.platformName }}</div>
                    <div class="promotion-desc">
                      <a-tag color="blue" size="small">推广平台</a-tag>
                      <span class="promotion-jump">{{ jumpModeText(item.jumpMode) }}</span>
                    </div>
                  </div>
                  <div class="promotion-actions">
                    <a-switch
                      :checked="item.status === 1"
                      size="small"
                      @change="(v) => handlePromotionStatusChange(item, v)"
                    />
                    <a-button type="link" size="small" @click="goToPromotionDetail(item.id)">
                      详情
                    </a-button>
                  </div>
                </div>
                <!-- 优惠券 -->
                <div
                  v-for="item in couponConfigs"
                  :key="'c-' + item.id"
                  class="promotion-item"
                >
                  <div class="promotion-icon coupon-icon-bg">
                    <span class="coupon-icon-text">¥</span>
                  </div>
                  <div class="promotion-info">
                    <div class="promotion-name">{{ item.customName || item.couponName }}</div>
                    <div class="promotion-desc">
                      <a-tag color="orange" size="small">优惠券</a-tag>
                      <span class="promotion-jump">¥{{ Number(item.couponValue || 0) }} · 剩{{ item.couponRemainCount || 0 }}张</span>
                    </div>
                  </div>
                  <div class="promotion-actions">
                    <a-switch
                      :checked="item.status === 1"
                      size="small"
                      @change="(v) => handlePromotionStatusChange(item, v)"
                    />
                    <a-button type="link" size="small" @click="goToPromotionDetail(item.id)">
                      详情
                    </a-button>
                  </div>
                </div>
              </div>
            </a-spin>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  SaveOutlined,
  ShopOutlined,
  PhoneOutlined,
  WifiOutlined,
  PictureOutlined,
  UploadOutlined,
  ShareAltOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'
import { getMerchantDetail, updateMerchant } from '@/api/merchant'
import {
  getMerchantPromotionConfigs,
  updateMerchantPromotionConfig
} from '@/api/marketing'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const merchantData = ref({})

// 是否为商家角色（只能编辑自己的商家信息，且不能修改管理字段）
const isMerchant = computed(() => userStore.isMerchant)

const formData = reactive({
  name: '',
  logo: '',
  bannerImages: [],
  shopImages: [],
  contactName: '',
  contactPhone: '',
  bossWechat: '',
  address: '',
  businessHours: '',
  wifiName: '',
  wifiPassword: '',
  description: ''
})

const logoFileList = ref([])
const bannerFileList = ref([])
const shopFileList = ref([])

const uploadAction = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/admin/upload/image`
  : '/api/admin/upload/image'

const uploadHeaders = (() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})()

const getLogoUrl = (logo) => {
  if (!logo) return ''
  if (logo.startsWith('http://') || logo.startsWith('https://')) return logo
  return `${import.meta.env.VITE_FILE_SERVER_URL || 'http://154.8.138.48:3000'}${logo}`
}

const parseImages = (v) => {
  if (!v) return []
  if (Array.isArray(v)) return v
  try {
    const p = JSON.parse(v)
    return Array.isArray(p) ? p : []
  } catch {
    return []
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB！')
    return false
  }
  return true
}

// Logo 上传
const handleLogoChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (res && res.code === 0 && res.data) {
      formData.logo = res.data.url
      message.success('Logo 上传成功')
    } else {
      message.error(res?.message || '上传失败')
      logoFileList.value = logoFileList.value.filter(f => f.uid !== info.file.uid)
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
    logoFileList.value = logoFileList.value.filter(f => f.uid !== info.file.uid)
  }
}
const handleLogoRemove = () => {
  formData.logo = ''
}

// 轮播图上传
const handleBannerChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (res && res.code === 0 && res.data) {
      formData.bannerImages = bannerFileList.value
        .filter(f => f.status === 'done' && f.response?.code === 0)
        .map(f => f.response.data.url)
    } else {
      message.error(res?.message || '上传失败')
      bannerFileList.value = bannerFileList.value.filter(f => f.uid !== info.file.uid)
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
    bannerFileList.value = bannerFileList.value.filter(f => f.uid !== info.file.uid)
  }
}
const handleBannerRemove = (file) => {
  formData.bannerImages = bannerFileList.value
    .filter(f => f.uid !== file.uid && f.status === 'done' && f.response?.code === 0)
    .map(f => f.response.data.url)
  return true
}

// 店铺图片上传
const handleShopChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (res && res.code === 0 && res.data) {
      formData.shopImages = shopFileList.value
        .filter(f => f.status === 'done' && f.response?.code === 0)
        .map(f => f.response.data.url)
    } else {
      message.error(res?.message || '上传失败')
      shopFileList.value = shopFileList.value.filter(f => f.uid !== info.file.uid)
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
    shopFileList.value = shopFileList.value.filter(f => f.uid !== info.file.uid)
  }
}
const handleShopRemove = (file) => {
  formData.shopImages = shopFileList.value
    .filter(f => f.uid !== file.uid && f.status === 'done' && f.response?.code === 0)
    .map(f => f.response.data.url)
  return true
}

const loadData = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await getMerchantDetail(id)
    merchantData.value = res || {}
    const banners = parseImages(res.banner_images)
    const shops = parseImages(res.shop_images)
    Object.assign(formData, {
      name: res.name || '',
      logo: res.logo || '',
      bannerImages: banners,
      shopImages: shops,
      contactName: res.contact_name || '',
      contactPhone: res.contact_phone || '',
      bossWechat: res.boss_wechat || '',
      address: res.address || '',
      businessHours: res.business_hours || '',
      wifiName: res.wifi_name || '',
      wifiPassword: res.wifi_password || '',
      description: res.description || ''
    })
    logoFileList.value = res.logo ? [{
      uid: '-1', name: 'logo.png', status: 'done', url: getLogoUrl(res.logo)
    }] : []
    bannerFileList.value = banners.map((url, idx) => ({
      uid: `banner-${idx}`, name: `banner-${idx}.png`, status: 'done',
      url: getLogoUrl(url), response: { code: 0, data: { url } }
    }))
    shopFileList.value = shops.map((url, idx) => ({
      uid: `shop-${idx}`, name: `shop-${idx}.png`, status: 'done',
      url: getLogoUrl(url), response: { code: 0, data: { url } }
    }))
  } catch (e) {
    message.error('加载商家信息失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formData.name) {
    message.warning('请输入商家名称')
    return
  }
  saving.value = true
  try {
    await updateMerchant(route.params.id, {
      name: formData.name,
      logo: formData.logo,
      bannerImages: formData.bannerImages || [],
      shopImages: formData.shopImages || [],
      contactName: formData.contactName,
      contactPhone: formData.contactPhone,
      bossWechat: formData.bossWechat || '',
      address: formData.address,
      businessHours: formData.businessHours || '',
      wifiName: formData.wifiName || '',
      wifiPassword: formData.wifiPassword || '',
      description: formData.description
    })
    message.success('保存成功')
    loadData()
  } catch (e) {
    message.error('保存失败')
    console.error(e)
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  // 商家角色无权访问商家列表，返回仪表盘
  if (isMerchant.value) {
    router.push('/dashboard')
  } else {
    router.push('/merchant/list')
  }
}

// ============ 推广配置 ============
const emptyImage = Empty.PRESENTED_IMAGE_SIMPLE
const promotionLoading = ref(false)
const promotionConfigs = ref([])

const platformConfigs = computed(() => promotionConfigs.value.filter(item => item.type !== 'coupon'))
const couponConfigs = computed(() => promotionConfigs.value.filter(item => item.type === 'coupon'))

const jumpModeText = (mode) => {
  const map = { scheme: 'URL Scheme', webview: 'H5链接', miniprogram: '小程序', copy: '复制链接' }
  return map[mode] || mode || '--'
}

const loadPromotionConfigs = async () => {
  const id = route.params.id
  if (!id) return
  promotionLoading.value = true
  try {
    const res = await getMerchantPromotionConfigs(id)
    promotionConfigs.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载推广配置失败', e)
    promotionConfigs.value = []
  } finally {
    promotionLoading.value = false
  }
}

const handlePromotionStatusChange = async (item, checked) => {
  try {
    await updateMerchantPromotionConfig(item.id, { status: checked ? 1 : 0 })
    item.status = checked ? 1 : 0
    message.success(checked ? '已开启展示' : '已关闭展示')
  } catch (e) {
    console.error('状态更新失败', e)
  }
}

const goToPromotionManage = () => {
  router.push('/marketing/merchant-promotion')
}

const goToPromotionDetail = (id) => {
  router.push(`/marketing/promotion-detail/${id}`)
}

onMounted(() => {
  loadData()
  loadPromotionConfigs()
})
</script>

<style lang="scss" scoped>
.merchant-detail-page {
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
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.preview-section {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 24px;
}

.form-section {
  flex: 1;
  min-width: 0;
}

.preview-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.preview-header {
  display: flex;
  gap: 12px;
  align-items: center;
}

.preview-logo {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  background: #f5f5f5;
}

.preview-logo-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 600;
  color: $primary-color;
}

.preview-meta {
  flex: 1;
  min-width: 0;
}

.preview-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-sub {
  font-size: 12px;
  color: $text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.preview-label {
  color: $text-tertiary;
  flex-shrink: 0;
}

.preview-value {
  color: $text-color;
  font-weight: 500;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
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

.promotion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.promotion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.2s;

  &:hover {
    background: #f0f5ff;
  }
}

.promotion-icon {
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

.coupon-icon-bg {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.coupon-icon-text {
  font-size: 18px;
  font-weight: bold;
}

.promotion-info {
  flex: 1;
  min-width: 0;
}

.promotion-name {
  font-size: 14px;
  font-weight: 500;
  color: $text-color;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.promotion-desc {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: $text-tertiary;
}

.promotion-jump {
  color: $text-secondary;
}

.promotion-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
</style>
