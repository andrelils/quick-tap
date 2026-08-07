<template>
  <div class="merchant-list-page">
    <div class="page-header">
      <div class="page-title">商家列表</div>
      <div class="page-desc">管理平台入驻商家</div>
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
        <a-form-item label="状态">
          <a-select 
            v-model:value="searchForm.status" 
            placeholder="全部状态"
            style="width: 140px"
            allow-clear
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
            <a-select-option :value="2">待审核</a-select-option>
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
          商家列表
          <a-tag color="blue">共 {{ pagination.total }} 家</a-tag>
        </div>
        <div class="table-actions">
          <a-space v-if="selectedRowKeys.length > 0" style="margin-right: 12px">
            <a-button danger :icon="h(LockOutlined)" @click="handleBatchDisable">
              禁用 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button :icon="h(UnlockOutlined)" @click="handleBatchEnable">
              启用 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button danger :icon="h(DeleteOutlined)" @click="handleBatchDelete">
              删除 ({{ selectedRowKeys.length }})
            </a-button>
          </a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增商家
          </a-button>
        </div>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="false"
        :row-key="record => record.id"
        :scroll="{ x: 1300 }"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'logo'">
            <div class="logo-wrapper">
              <img v-if="record.logo" :src="getLogoUrl(record.logo)" class="shop-logo" />
              <div v-else class="shop-logo-placeholder">{{ record.name?.charAt(0) }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'storage'">
            <div class="storage-info" v-if="record.storageInfo">
              <a-progress
                :percent="record.storageInfo.percent"
                :stroke-color="record.storageInfo.percent > 80 ? '#ff4d4f' : '#1677ff'"
                :show-info="false"
                size="small"
              />
              <div class="storage-text">{{ record.storageInfo.used }} / {{ record.storageInfo.total }}</div>
            </div>
            <span v-else class="storage-text">--</span>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            {{ record.createdAt ? dayjs(record.createdAt).format('YYYY-MM-DD HH:mm:ss') : '--' }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge 
              :status="record.status === 1 ? 'success' : 'default'" 
              :text="getStatusText(record.status)" 
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleDetail(record)">详情</a>
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a type="link" size="small" :danger="true" @click="handleDelete(record)">删除</a>
              <a v-if="record.status === 1" type="link" size="small" :danger="true" @click="handleStatus(record)">禁用</a>
              <a v-else-if="record.status === 0" type="link" size="small" @click="handleStatus(record)">启用</a>
            </a-space>
          </template>
        </template>
      </a-table>
      
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-size-options="['10', '20', '50', '100']"
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
      width="720px"
      :footer="null"
      destroy-on-close
    >
      <a-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
      >
        <a-form-item label="商家名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入商家名称" />
        </a-form-item>
        <a-form-item label="商家Logo" name="logo">
          <a-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :max-count="1"
            v-model:file-list="fileList"
            @change="handleUploadChange"
            @remove="handleUploadRemove"
            :before-upload="beforeUpload"
          >
            <div>
              <UploadOutlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item label="首页轮播图（推荐栏图片）" name="bannerImages" extra="最多 5 张，将展示在小程序商家页顶部轮播图">
          <a-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :max-count="5"
            multiple
            v-model:file-list="bannerFileList"
            @change="handleBannerUploadChange"
            @remove="handleBannerRemove"
            :before-upload="beforeUpload"
          >
            <div v-if="bannerFileList.length < 5">
              <UploadOutlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>
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
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老板微信" name="bossWechat" extra="用于小程序“一键加老板微信”工具">
              <a-input v-model:value="formData.bossWechat" placeholder="请输入老板微信号" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="营业时间" name="businessHours">
              <a-input v-model:value="formData.businessHours" placeholder="如 09:00-22:00" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="地址" name="address">
          <a-input v-model:value="formData.address" placeholder="请输入商家地址" />
        </a-form-item>
        <a-form-item label="推荐人编号" name="referrerCode">
          <a-input v-model:value="formData.referrerCode" placeholder="请输入推荐人用户编号（可选，留空则注册时自动绑定首个超管/管理员）" allow-clear />
        </a-form-item>
        <a-form-item label="简介" name="description">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入商家简介"
            :rows="3"
            show-count
            :max-length="500"
          />
        </a-form-item>
        <a-form-item label="店铺图片（介绍页展示）" name="shopImages" extra="最多 9 张，展示在商家介绍Tab">
          <a-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :max-count="9"
            multiple
            v-model:file-list="shopFileList"
            @change="handleShopUploadChange"
            @remove="handleShopRemove"
            :before-upload="beforeUpload"
          >
            <div v-if="shopFileList.length < 9">
              <UploadOutlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">禁用</a-radio>
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
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  UploadOutlined,
  DeleteOutlined,
  LockOutlined,
  UnlockOutlined
} from '@ant-design/icons-vue'
import { getMerchantList, createMerchant, updateMerchant, deleteMerchant, updateMerchantStatus, getMerchantStorage } from '@/api/merchant'
import { usePageStore } from '@/store/page'
import { getErrorMessage, getContextErrorMessage } from '@/utils/errorMessage'
import { isSuccessCode } from '@/utils/request'

const router = useRouter()
const pageStore = usePageStore()

const searchForm = reactive({
  keyword: '',
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: 'Logo', dataIndex: 'logo', key: 'logo', width: 80 },
  { title: '商家名称', dataIndex: 'name', key: 'name', width: 160 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 100 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 130 },
  { title: '地址', dataIndex: 'address', key: 'address', width: 200, ellipsis: true },
  { title: '推荐人', key: 'referrer', width: 160, customRender: ({ record }) => record.referrerCode ? record.referrerCode : '--' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '存储空间', dataIndex: 'storage', key: 'storage', width: 140 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 240, fixed: 'right' }
]

const dataSource = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const modalTitle = ref('新增商家')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)
const selectedRowKeys = ref([])
const selectedRows = ref([])

const formData = reactive({
  id: null,
  name: '',
  logo: '',
  bannerImages: [],
  contactName: '',
  contactPhone: '',
  bossWechat: '',
  address: '',
  businessHours: '',
  referrerCode: '',
  description: '',
  shopImages: [],
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确，请输入正确的11位手机号', trigger: 'blur' }
  ]
}

const fileList = ref([])
const bannerFileList = ref([])
const shopFileList = ref([])
// 上传地址：编辑已有商家时带上 merchantId，后端据此校验并累计存储额度
const uploadAction = computed(() => {
  const base = import.meta.env.VITE_API_BASE_URL
  const action = base ? `${base}/admin/upload/image` : '/api/admin/upload/image'
  return formData.id ? `${action}?merchantId=${formData.id}` : action
})

const uploadHeaders = (() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})()

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

const handleUploadChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (isSuccessCode(res)) {
      formData.logo = res.data.url
      if (info.fileList && info.fileList.length > 0) {
        const lastFile = info.fileList[info.fileList.length - 1]
        if (lastFile) {
          lastFile.url = res.data.url
          lastFile.thumbUrl = res.data.url
        }
      }
      message.success('上传成功')
    } else {
      message.error(res?.message || '上传失败')
      fileList.value = fileList.value.filter(f => f.uid !== info.file.uid)
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
    fileList.value = fileList.value.filter(f => f.uid !== info.file.uid)
  }
}

const handleUploadRemove = () => {
  formData.logo = ''
}

// 轮播图上传处理
const handleBannerUploadChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (isSuccessCode(res)) {
      formData.bannerImages = bannerFileList.value
        .filter(f => f.status === 'done' && isSuccessCode(f.response))
        .map(f => f.response.data.url)
      if (info.file.uid === info.fileList[info.fileList.length - 1]?.uid) {
        message.success('上传成功')
      }
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
    .filter(f => f.uid !== file.uid && f.status === 'done' && isSuccessCode(f.response))
    .map(f => f.response.data.url)
  return true
}

// 店铺图片上传处理
const handleShopUploadChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (isSuccessCode(res)) {
      formData.shopImages = shopFileList.value
        .filter(f => f.status === 'done' && isSuccessCode(f.response))
        .map(f => f.response.data.url)
      if (info.file.uid === info.fileList[info.fileList.length - 1]?.uid) {
        message.success('上传成功')
      }
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
    .filter(f => f.uid !== file.uid && f.status === 'done' && isSuccessCode(f.response))
    .map(f => f.response.data.url)
  return true
}

const getStatusText = (status) => {
  const map = { 0: '禁用', 1: '正常' }
  return map[status] || '未知'
}

const getLogoUrl = (logo) => {
  if (!logo) return ''
  if (logo.startsWith('http://') || logo.startsWith('https://')) return logo
  return `${import.meta.env.VITE_FILE_SERVER_URL || 'http://154.8.138.48:3000'}${logo}`
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status !== undefined ? searchForm.status : ''
    }
    const res = await getMerchantList(params)
    const list = res.list || []
    // Load storage info for each merchant
    const listWithStorage = await Promise.all(list.map(async (item) => {
      try {
        const storageRes = await getMerchantStorage(item.id)
        const storageData = storageRes?.storage || storageRes || {}
        const used = storageData.used || 0
        const total = storageData.limit || storageData.limitMB || 0
        const unlimited = storageData.unlimited === true || !total
        const percent = total > 0 ? Math.round((used / total) * 100) : 0
        item.storageInfo = {
          used: `${used}MB`,
          total: unlimited ? '不限' : `${total}MB`,
          percent: unlimited ? 0 : percent
        }
      } catch (e) {
        item.storageInfo = null
      }
      return item
    }))
    dataSource.value = listWithStorage
    pagination.total = res.total
  } catch (e) {
    console.error('加载数据失败', e)
    message.error(getContextErrorMessage(e, '加载'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

const handlePageChange = () => {
  // Save pagination state when user changes page
  pageStore.setPageState('merchant-list', {
    current: pagination.current,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword,
    status: searchForm.status
  })
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增商家'
  Object.assign(formData, {
    id: null,
    name: '',
    logo: '',
    bannerImages: [],
    contactName: '',
    contactPhone: '',
    bossWechat: '',
    address: '',
    businessHours: '',
    referrerCode: '',
    description: '',
    shopImages: [],
    status: 1
  })
  fileList.value = []
  bannerFileList.value = []
  shopFileList.value = []
  modalVisible.value = true
}

// 解析后端返回的图片字段（兼容 JSON字符串/数组）
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

const handleDetail = (record) => {
  // Save current pagination and filter state before navigating
  pageStore.setPageState('merchant-list', {
    current: pagination.current,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword,
    status: searchForm.status
  })
  router.push(`/merchant/detail/${record.id}`)
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑商家'
  const banners = parseImages(record.bannerImages)
  const shops = parseImages(record.shopImages)
  Object.assign(formData, {
    id: record.id,
    name: record.name,
    logo: record.logo,
    bannerImages: banners,
    contactName: record.contactName,
    contactPhone: record.contactPhone,
    bossWechat: record.bossWechat || '',
    address: record.address,
    businessHours: record.businessHours || '',
    referrerCode: record.referrerCode || '',
    description: record.description,
    shopImages: shops,
    status: record.status
  })
  fileList.value = record.logo ? [{
    uid: '-1',
    name: 'logo.png',
    status: 'done',
    url: getLogoUrl(record.logo)
  }] : []
  bannerFileList.value = banners.map((url, idx) => ({
    uid: `banner-${idx}`,
    name: `banner-${idx}.png`,
    status: 'done',
    url: getLogoUrl(url),
    response: { code: 0, data: { url } }
  }))
  shopFileList.value = shops.map((url, idx) => ({
    uid: `shop-${idx}`,
    name: `shop-${idx}.png`,
    status: 'done',
    url: getLogoUrl(url),
    response: { code: 0, data: { url } }
  }))
  modalVisible.value = true
}

const handleView = (record) => {
  message.info('查看商家详情: ' + record.name)
}

const handleStatus = async (record) => {
  const newStatus = record.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'

  Modal.confirm({
    title: `${actionText}商家`,
    content: h('div', [
      h('p', `确定要${actionText}以下商家吗？`),
      h('div', { style: 'background: #f5f5f5; padding: 12px; border-radius: 4px; margin-top: 12px' }, [
        h('div', { style: 'font-weight: 600; margin-bottom: 8px' }, '商家名称'),
        h('div', record.name)
      ])
    ]),
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: newStatus === 0 },
    onOk: async () => {
      try {
        await updateMerchantStatus(record.id, newStatus)
        message.success(`${actionText}成功`)
        loadData()
      } catch (e) {
        console.error('操作失败', e)
        message.error(getErrorMessage(e) || '操作失败')
      }
    }
  })
}

const handleDelete = async (record) => {
  Modal.confirm({
    title: '删除商家',
    content: h('div', [
      h('p', { style: 'color: #ff4d4f; font-weight: 600; margin-bottom: 12px' }, '⚠️ 确定要删除该商家吗？删除后将无法恢复。'),
      h('div', { style: 'background: #f5f5f5; padding: 12px; border-radius: 4px' }, [
        h('div', { style: 'font-weight: 600; margin-bottom: 8px' }, '商家名称'),
        h('div', { style: 'margin-bottom: 12px' }, record.name),
        h('div', { style: 'font-weight: 600; margin-bottom: 8px' }, '联系电话'),
        h('div', record.contactPhone || '--')
      ])
    ]),
    okText: '确定删除',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        await deleteMerchant(record.id)
        message.success('删除成功')
        loadData()
      } catch (e) {
        console.error('删除失败', e)
        message.error(getErrorMessage(e) || '删除失败')
      }
    }
  })
}

const onSelectChange = (keys, rows) => {
  selectedRowKeys.value = keys
  selectedRows.value = rows
}

const handleBatchEnable = () => {
  if (selectedRowKeys.value.length === 0) return

  const disabledCount = selectedRows.value.filter(r => r.status === 0).length
  if (disabledCount === 0) {
    message.warning('所选商家都已启用')
    return
  }

  Modal.confirm({
    title: '批量启用商家',
    content: h('div', [
      h('p', `确定要启用所选 ${disabledCount} 家商家吗？`),
      h('div', { style: 'background: #f5f5f5; padding: 12px; border-radius: 4px; margin-top: 12px; max-height: 200px; overflow-y: auto' }, [
        h('div', { style: 'font-weight: 600; margin-bottom: 8px' }, '待启用商家'),
        ...selectedRows.value
          .filter(r => r.status === 0)
          .map((r, idx) => h('div', { style: 'padding: 4px 0; border-bottom: idx < disabledCount - 1 ? "1px solid #e8e8e8" : "none"' }, r.name))
      ])
    ]),
    okText: '确定启用',
    cancelText: '取消',
    onOk: async () => {
      try {
        const promises = selectedRows.value
          .filter(r => r.status === 0)
          .map(r => updateMerchantStatus(r.id, 1))

        await Promise.all(promises)
        message.success(`批量启用成功，共 ${disabledCount} 家`)
        selectedRowKeys.value = []
        selectedRows.value = []
        loadData()
      } catch (e) {
        console.error('操作失败', e)
        message.error(getErrorMessage(e) || '操作失败')
      }
    }
  })
}

const handleBatchDisable = () => {
  if (selectedRowKeys.value.length === 0) return

  const enabledCount = selectedRows.value.filter(r => r.status === 1).length
  if (enabledCount === 0) {
    message.warning('所选商家都已禁用')
    return
  }

  Modal.confirm({
    title: '批量禁用商家',
    content: h('div', [
      h('p', { style: 'color: #ff4d4f; font-weight: 600' }, `⚠️ 确定要禁用所选 ${enabledCount} 家商家吗？`),
      h('div', { style: 'background: #f5f5f5; padding: 12px; border-radius: 4px; margin-top: 12px; max-height: 200px; overflow-y: auto' }, [
        h('div', { style: 'font-weight: 600; margin-bottom: 8px' }, '待禁用商家'),
        ...selectedRows.value
          .filter(r => r.status === 1)
          .map((r, idx) => h('div', { style: 'padding: 4px 0; border-bottom: idx < enabledCount - 1 ? "1px solid #e8e8e8" : "none"' }, r.name))
      ])
    ]),
    okText: '确定禁用',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const promises = selectedRows.value
          .filter(r => r.status === 1)
          .map(r => updateMerchantStatus(r.id, 0))

        await Promise.all(promises)
        message.success(`批量禁用成功，共 ${enabledCount} 家`)
        selectedRowKeys.value = []
        selectedRows.value = []
        loadData()
      } catch (e) {
        console.error('操作失败', e)
        message.error(getErrorMessage(e) || '操作失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) return

  Modal.confirm({
    title: '批量删除商家',
    content: h('div', [
      h('p', { style: 'color: #ff4d4f; font-weight: 600; margin-bottom: 12px' }, `⚠️ 确定要删除所选 ${selectedRowKeys.value.length} 家商家吗？删除后将无法恢复。`),
      h('div', { style: 'background: #f5f5f5; padding: 12px; border-radius: 4px; max-height: 200px; overflow-y: auto' }, [
        h('div', { style: 'font-weight: 600; margin-bottom: 8px' }, '待删除商家'),
        ...selectedRows.value.map((r, idx) => h('div', { style: 'padding: 4px 0; border-bottom: idx < selectedRows.value.length - 1 ? "1px solid #e8e8e8" : "none"' }, r.name))
      ])
    ]),
    okText: '确定删除',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const promises = selectedRows.value.map(r => deleteMerchant(r.id))
        await Promise.all(promises)
        message.success(`批量删除成功，共 ${selectedRowKeys.value.length} 家`)
        selectedRowKeys.value = []
        selectedRows.value = []
        loadData()
      } catch (e) {
        console.error('操作失败', e)
        message.error(getErrorMessage(e) || '删除失败')
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const payload = {
      name: formData.name,
      logo: formData.logo,
      bannerImages: JSON.stringify(formData.bannerImages || []),
      contactName: formData.contactName,
      contactPhone: formData.contactPhone,
      bossWechat: formData.bossWechat || '',
      address: formData.address,
      businessHours: formData.businessHours || '',
      referrerCode: formData.referrerCode || '',
      description: formData.description,
      shopImages: JSON.stringify(formData.shopImages || []),
      status: formData.status
    }

    if (isEdit.value) {
      await updateMerchant(formData.id, payload)
      message.success('编辑成功')
    } else {
      await createMerchant(payload)
      message.success('新增成功')
    }

    modalVisible.value = false
    loadData()
    // 通知顶部"切换商家视图"下拉刷新商家列表
    window.dispatchEvent(new CustomEvent('merchant-changed'))
  } catch (e) {
    console.error('表单提交失败', e)
    if (e.response?.status === 422 || e.response?.data?.validationErrors) {
      // Handle validation errors
      message.error('请检查表单信息是否完整')
    } else {
      message.error(getErrorMessage(e) || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // Try to restore pagination and filter state from previous visit
  const savedState = pageStore.getPageState('merchant-list')
  if (savedState) {
    // Restore pagination
    pagination.current = savedState.current || 1
    pagination.pageSize = savedState.pageSize || 10

    // Restore search filters
    searchForm.keyword = savedState.keyword || ''
    searchForm.status = savedState.status !== undefined ? savedState.status : undefined

    // Mark that we're returning to this page
    pageStore.setLastVisitedPage('merchant-list')
  }

  loadData()
})
</script>

<style lang="scss" scoped>
.merchant-list-page {
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

.table-actions {
  display: flex;
  gap: 8px;
}

.shop-logo {
  width: 40px;
  height: 40px;
  border-radius: $border-radius-sm;
  object-fit: cover;
  background: #f5f5f5;
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

.storage-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.storage-text {
  font-size: 12px;
  color: $text-tertiary;
}

.shop-logo-placeholder {
  width: 40px;
  height: 40px;
  border-radius: $border-radius-sm;
  background: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: $primary-color;
}
</style>
