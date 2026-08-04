<template>
  <div class="device-list-page">
    <div class="page-header">
      <div class="page-title">设备管理</div>
      <div class="page-desc">管理所有晓居智能设备，每套设备同时包含二维码和NFC两种类型</div>
    </div>

    <div class="card-wrapper config-card" v-if="deviceConfig.deviceUrl || deviceConfig.qrcodeUrl">
      <a-descriptions title="设备URL配置" :column="1" size="small" bordered>
        <a-descriptions-item v-if="deviceConfig.qrcodeUrl" label="二维码前缀URL">
          <a-tag color="blue">{{ deviceConfig.qrcodeUrl }}</a-tag>
          <span class="form-item-tip" style="margin-left: 8px">二维码ID将以 ?q=xxx 的形式追加到此URL后</span>
        </a-descriptions-item>
        <a-descriptions-item v-if="deviceConfig.deviceUrl" label="设备URL前缀">
          <a-tag color="blue">{{ deviceConfig.deviceUrl }}</a-tag>
          <span class="form-item-tip" style="margin-left: 8px">设备编号和系统编码将以 ?deviceNo=xxx&code=xxx 的形式追加到此URL后</span>
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="关键词">
          <a-input 
            v-model:value="searchForm.keyword" 
            placeholder="设备编号/名称/系统编码/URL"
            style="width: 240px"
            allow-clear
            @pressEnter="handleSearch"
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
        <a-form-item label="绑定状态">
          <a-select 
            v-model:value="searchForm.bindStatus" 
            placeholder="全部状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="1">已绑定</a-select-option>
            <a-select-option :value="0">未绑定</a-select-option>
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
          设备列表
          <a-tag color="blue">共 {{ pagination.total }} 套</a-tag>
        </div>
        <div class="table-actions">
          <a-space>
            <a-button type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新增设备
            </a-button>
          </a-space>
        </div>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="false"
        :row-key="record => record.setId"
        :loading="tableLoading"
        :scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'setName'">
            <div class="set-name-cell">
              <div class="set-name">{{ record.setName || record.deviceName || '-' }}</div>
              <div class="set-no">systemCode：{{ record.qrcode?.systemCode || record.systemCode || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'qrcode'">
            <div class="device-type-cell" v-if="record.qrcode">
              <div class="type-line">
                <a-tag color="blue">二维码</a-tag>
                <span class="type-code">{{ record.qrcode.systemCode || record.qrcode.deviceNo || '-' }}</span>
              </div>
              <div class="type-url" v-if="record.qrcode.url">{{ record.qrcode.url }}</div>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
          <template v-else-if="column.key === 'nfc'">
            <div class="device-type-cell" v-if="record.nfc">
              <div class="type-line">
                <a-tag color="green">NFC</a-tag>
                <span class="type-code">{{ record.nfc.systemCode || record.nfc.deviceNo || '-' }}</span>
              </div>
              <div class="type-url" v-if="record.nfc.url">{{ record.nfc.url }}</div>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
          <template v-else-if="column.key === 'merchantName'">
            <span v-if="record.merchantName">{{ record.merchantName }}</span>
            <a-tag v-else color="orange">未绑定</a-tag>
          </template>
          <template v-else-if="column.key === 'bindStatus'">
            <a-badge
              :status="record.bindStatus === 1 ? 'success' : 'default'"
              :text="record.bindStatus === 1 ? '已绑定' : '未绑定'"
            />
          </template>
          <template v-else-if="column.key === 'createdAt'">
            <span class="create-time">{{ record.createdAt || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleQrcode(record)">查看二维码</a>
              <a-popconfirm
                title="确定要删除该套设备吗？将同时删除二维码和NFC设备"
                @confirm="handleDelete(record)"
              >
                <a type="link" size="small" :danger="true">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
        
        <template #emptyText>
          <a-empty description="暂无设备数据" />
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
      width="640px"
      :footer="null"
      destroy-on-close
    >
      <a-alert type="info" show-icon style="margin-bottom: 16px">
        <template #message>
          新增设备将同时生成<b>二维码设备</b>和<b>NFC设备</b>，两套设备共享同一设备名称，扫码/碰一碰后均跳转至商家详情页
        </template>
      </a-alert>
      <a-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
      >
        <a-form-item label="设备名称" name="deviceName">
          <a-input v-model:value="formData.deviceName" placeholder="请输入设备名称，如：1号桌台" />
        </a-form-item>
        <div class="auto-generate-bar">
          <a-button type="primary" ghost size="small" @click="autoGenerateAll">
            一键自动生成
          </a-button>
          <span class="auto-generate-tip">自动生成系统编码、设备编号和URL</span>
        </div>
        <a-form-item label="系统编码" name="systemCode">
          <a-input v-model:value="formData.systemCode" placeholder="点击「一键自动生成」自动填写" disabled>
            <template #addonAfter>
              <span v-if="formData.systemCode" class="generated-badge">已生成</span>
            </template>
          </a-input>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="二维码设备URL" name="qrUrl">
              <a-input v-model:value="formData.qrUrl" placeholder="点击「一键自动生成」自动填写" disabled />
              <div class="form-item-tip">格式：{二维码URL前缀}?q={systemCode}&code={systemCode}</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="NFC设备URL" name="nfcUrl">
              <a-input v-model:value="formData.nfcUrl" placeholder="点击「一键自动生成」自动填写" disabled />
              <div class="form-item-tip">格式：{设备URL前缀}?deviceNo={NFC编号}&code={系统编码}</div>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="二维码编号" name="qrDeviceNo">
              <a-input v-model:value="formData.qrDeviceNo" placeholder="点击「一键自动生成」自动填写" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="NFC编号" name="nfcDeviceNo">
              <a-input v-model:value="formData.nfcDeviceNo" placeholder="点击「一键自动生成」自动填写" disabled />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">
            确定新增
          </a-button>
        </div>
      </a-form>
    </a-modal>
    
    <a-modal
      v-model:open="qrcodeVisible"
      title="设备二维码预览"
      width="420px"
      :footer="null"
    >
      <div class="qrcode-content" v-if="currentDevice">
        <div class="qrcode-set-info">
          <div class="set-name">{{ currentDevice.setName || currentDevice.deviceName }}</div>
          <div class="set-no">编号：{{ currentDevice.deviceNo }}</div>
        </div>
        <a-divider style="margin: 16px 0" />
        <div class="qrcode-types">
          <div class="qrcode-type-item" v-if="currentDevice.qrcode">
            <div class="type-header">
              <a-tag color="blue">二维码</a-tag>
              <span class="type-code">{{ currentDevice.qrcode.systemCode || currentDevice.qrcode.deviceNo }}</span>
            </div>
            <div class="qrcode-img">
              <canvas :id="'qrcodeCanvas_' + currentDevice.setId"></canvas>
            </div>
            <div class="type-url">{{ currentDevice.qrcode.url || '-' }}</div>
          </div>
          <div class="qrcode-type-item" v-if="currentDevice.nfc">
            <div class="type-header">
              <a-tag color="green">NFC</a-tag>
              <span class="type-code">{{ currentDevice.nfc.systemCode || currentDevice.nfc.deviceNo }}</span>
            </div>
            <div class="qrcode-img">
              <canvas :id="'nfcCanvas_' + currentDevice.setId"></canvas>
            </div>
            <div class="type-url">{{ currentDevice.nfc.url || '-' }}</div>
          </div>
        </div>
        <div class="qrcode-actions">
          <a-button block @click="handleDownload">
            <template #icon><DownloadOutlined /></template>
            下载二维码
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import QRCode from 'qrcode'
import { getDeviceList, batchCreateDevice, deleteDevice, getQrCodeConfig } from '@/api/device'
import { getMerchantList } from '@/api/merchant'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const appStore = useAppStore()
const userStore = useUserStore()
const tableLoading = ref(false)

const searchForm = reactive({
  keyword: '',
  merchantId: undefined,
  bindStatus: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
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

const columns = [
  { title: '设备名称', dataIndex: 'setName', key: 'setName', width: 180, fixed: 'left' },
  { title: '二维码设备', dataIndex: 'qrcode', key: 'qrcode', width: 240 },
  { title: 'NFC设备', dataIndex: 'nfc', key: 'nfc', width: 240 },
  { title: '所属商家', dataIndex: 'merchantName', key: 'merchantName', width: 140 },
  { title: '绑定状态', dataIndex: 'bindStatus', key: 'bindStatus', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 160, fixed: 'right' }
]

const dataSource = ref([])
const modalVisible = ref(false)
const qrcodeVisible = ref(false)
const modalTitle = ref('新增设备')
const submitting = ref(false)
const formRef = ref()
const currentDevice = ref(null)

const deviceConfig = ref({
  deviceUrl: '',
  qrcodeUrl: ''
})

const loadDeviceConfig = async () => {
  try {
    const res = await getQrCodeConfig()
    if (res) {
      deviceConfig.value.deviceUrl = res.deviceUrl || ''
      deviceConfig.value.qrcodeUrl = res.qrcodeUrl || ''
    }
  } catch (e) {}
}

const formData = reactive({
  deviceName: '',
  systemCode: '',
  qrUrl: '',
  nfcUrl: '',
  qrDeviceNo: '',
  nfcDeviceNo: '',
  status: 1
})

const formRules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

// QR 和 NFC 设备共享的 systemCode，确保成套设备使用相同编码
let sharedSystemCode = ''
const generateSystemCode = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = 'SC'
  for (let i = 0; i < 8; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return code
}

// 一键自动生成：系统编码、二维码编号、NFC编号、设备URL
const autoGenerateAll = async () => {
  try {
    // 生成共享的 systemCode
    sharedSystemCode = generateSystemCode()
    formData.systemCode = sharedSystemCode

    // 生成二维码编号和NFC编号
    formData.qrDeviceNo = 'QR' + Date.now().toString().slice(-8)
    formData.nfcDeviceNo = 'NFC' + Date.now().toString().slice(-8)

    // 获取系统配置的设备URL前缀
    const res = await getQrCodeConfig()
    const qrcodeBase = (res?.qrcodeUrl || '').replace(/\/$/, '')
    const deviceBase = (res?.deviceUrl || '').replace(/\/$/, '')

    // 二维码设备URL：{qrcodeUrl}?q={systemCode}&code={systemCode}
    formData.qrUrl = qrcodeBase
      ? `${qrcodeBase}?q=${sharedSystemCode}&code=${sharedSystemCode}`
      : `?q=${sharedSystemCode}&code=${sharedSystemCode}`
    // NFC设备URL：{deviceUrl}?deviceNo={nfcNo}&code={systemCode}
    formData.nfcUrl = deviceBase
      ? `${deviceBase}?deviceNo=${formData.nfcDeviceNo}&code=${sharedSystemCode}`
      : `?deviceNo=${formData.nfcDeviceNo}&code=${sharedSystemCode}`

    message.success('已自动生成系统编码、设备编号和URL')
  } catch (e) {
    console.error('自动生成失败', e)
    message.error('自动生成失败，请重试')
  }
}

// 将扁平设备列表按设备名称分组为"设备套"（已由后端处理，前端不再分组）
const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      pageNum: pagination.current
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    const globalMerchantId = userStore.isAdmin ? appStore.merchantId : ''
    if (globalMerchantId) {
      params.merchantId = globalMerchantId
    } else if (searchForm.merchantId !== undefined) {
      params.merchantId = searchForm.merchantId
    }
    if (searchForm.bindStatus !== undefined) params.bindStatus = searchForm.bindStatus
    
    const res = await getDeviceList(params)
    // 后端已按 name+systemCode 分组，直接使用
    const list = res.list || res || []
    const sets = Array.isArray(list) ? list : []
    
    // 绑定状态过滤
    let filtered = sets
    if (searchForm.bindStatus !== undefined) {
      filtered = sets.filter(item => item.bindStatus === searchForm.bindStatus)
    }
    
    dataSource.value = filtered
    pagination.total = res.total || filtered.length
  } catch (e) {
    console.error('加载设备列表失败', e)
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
  searchForm.keyword = ''
  searchForm.merchantId = undefined
  searchForm.bindStatus = undefined
  pagination.current = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  modalTitle.value = '新增设备'
  sharedSystemCode = ''
  Object.assign(formData, {
    deviceName: '',
    systemCode: '',
    qrUrl: '',
    nfcUrl: '',
    qrDeviceNo: '',
    nfcDeviceNo: '',
    status: 1
  })
  modalVisible.value = true
}

const handleQrcode = (record) => {
  currentDevice.value = record
  qrcodeVisible.value = true
  nextTick(() => {
    generateQrcodeImages(record)
  })
}

// 生成二维码图片到 canvas
const generateQrcodeImages = (device) => {
  if (device.qrcode) {
    const url = device.qrcode.url || device.qrcode.systemCode || device.qrcode.deviceNo || ''
    const canvas = document.getElementById(`qrcodeCanvas_${device.setId}`)
    if (canvas && url) {
      QRCode.toCanvas(canvas, url, { width: 160, margin: 1 }, (err) => {
        if (err) console.error('生成二维码失败:', err)
      })
    }
  }
  if (device.nfc) {
    const url = device.nfc.url || device.nfc.systemCode || device.nfc.deviceNo || ''
    const canvas = document.getElementById(`nfcCanvas_${device.setId}`)
    if (canvas && url) {
      QRCode.toCanvas(canvas, url, { width: 160, margin: 1 }, (err) => {
        if (err) console.error('生成NFC二维码失败:', err)
      })
    }
  }
}

const handleDelete = async (record) => {
  try {
    // 删除一套设备中的 QR 和 NFC 两条记录
    if (record.qrcode?.id) await deleteDevice(record.qrcode.id)
    if (record.nfc?.id) await deleteDevice(record.nfc.id)
    message.success('删除成功')
    loadData()
  } catch (e) {
    console.error('删除设备失败', e)
    message.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const baseName = formData.deviceName
    
    const devices = [
      {
        deviceNo: formData.qrDeviceNo || ('QR' + Date.now().toString().slice(-8)),
        name: baseName,
        systemCode: sharedSystemCode,
        url: formData.qrUrl,
        type: 'qrcode',
        status: formData.status
      },
      {
        deviceNo: formData.nfcDeviceNo || ('NFC' + Date.now().toString().slice(-8)),
        name: baseName,
        systemCode: sharedSystemCode,
        url: formData.nfcUrl,
        type: 'nfc',
        status: formData.status
      }
    ]
    
    const result = await batchCreateDevice(devices)
    const data = result?.data || result
    const successCount = data?.success || 0
    const failCount = data?.fail || 0
    
    if (failCount > 0) {
      message.warning(`创建完成：${successCount} 个成功，${failCount} 个失败，请查看设备列表确认`)
    } else {
      message.success('设备套创建成功（含二维码和NFC）')
    }
    modalVisible.value = false
    loadData()
  } catch (e) {
    console.error('表单验证或提交失败', e)
    message.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDownload = () => {
  const device = currentDevice.value
  if (!device) return
  const canvases = [
    { el: document.getElementById(`qrcodeCanvas_${device.setId}`), name: `${device.setName || device.deviceNo}_二维码` },
    { el: document.getElementById(`nfcCanvas_${device.setId}`), name: `${device.setName || device.deviceNo}_NFC` }
  ].filter(c => c.el)
  if (canvases.length === 0) {
    message.warning('暂无可下载的二维码')
    return
  }
  canvases.forEach(({ el, name }) => {
    const link = document.createElement('a')
    link.download = `${name}.png`
    link.href = el.toDataURL('image/png')
    link.click()
  })
  message.success('二维码已下载')
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
  loadDeviceConfig()
  if (userStore.isAdmin && appStore.merchantId) {
    searchForm.merchantId = appStore.merchantId
  }
  loadData()
})
</script>

<style lang="scss" scoped>
.device-list-page {
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

.config-card {
  margin-bottom: 16px;
  padding: 16px 24px;
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.set-name-cell {
  .set-name {
    font-weight: 500;
    color: $text-color;
    margin-bottom: 4px;
  }
  
  .set-no {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.device-type-cell {
  .type-line {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
  }
  
  .type-code {
    font-size: 12px;
    color: $text-secondary;
    font-family: monospace;
  }
  
  .type-url {
    font-size: 11px;
    color: $text-tertiary;
    word-break: break-all;
    line-height: 1.4;
  }
}

.empty-text {
  color: $text-tertiary;
}

.create-time {
  font-size: 12px;
  color: $text-secondary;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid $border-color;
}

.auto-generate-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border: 1px dashed #91d5ff;
  border-radius: 10px;
}

.auto-generate-tip {
  font-size: 12px;
  color: $text-tertiary;
}

.generated-badge {
  font-size: 12px;
  color: #52c41a;
}

.qrcode-content {
  text-align: center;
  padding: 8px 0;
}

.qrcode-set-info {
  .set-name {
    font-size: 16px;
    font-weight: 600;
    color: $text-color;
    margin-bottom: 4px;
  }
  
  .set-no {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.qrcode-types {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.qrcode-type-item {
  .type-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 8px;
  }
  
  .type-code {
    font-size: 13px;
    color: $text-secondary;
    font-family: monospace;
  }
}

.qrcode-img {
  width: 180px;
  height: 180px;
  margin: 0 auto 8px;
  background: $bg-card;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  canvas {
    width: 160px;
    height: 160px;
  }
}

.type-url {
  font-size: 11px;
  color: $text-tertiary;
  word-break: break-all;
  padding: 0 12px;
}

.qrcode-actions {
  padding: 16px 20px 0;
}

.form-item-tip {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
}
</style>
