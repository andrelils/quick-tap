<template>
  <div class="platforms-page">
    <div class="page-header">
      <div class="page-title">推广平台总配置</div>
      <div class="page-desc">配置各第三方推广平台的跳转方式与参数模板，商家可基于此填入自己的参数后展示在小程序中供用户点击跳转</div>
    </div>

    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">
          平台列表
          <a-tag color="blue">共 {{ dataSource.length }} 个</a-tag>
        </div>
        <div class="table-actions">
          <a-input-search v-model:value="searchKeyword" placeholder="搜索平台名称/代码" style="width: 240px" allow-clear @search="loadData" />
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增平台
          </a-button>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-key="record => record.id"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="platform-cell">
              <div class="platform-icon" :style="{ background: record.color }">
                {{ record.name?.charAt(0) }}
              </div>
              <div>
                <div class="platform-name-text">{{ record.name }}</div>
                <div class="platform-code-text">{{ record.code }}</div>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'jumpMode'">
            <a-tag :color="jumpModeColor(record.jumpMode)">{{ jumpModeText(record.jumpMode) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'schemeTemplate'">
            <a-tooltip :title="record.schemeTemplate">
              <span class="text-ellipsis">{{ record.schemeTemplate || '--' }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'webUrlTemplate'">
            <a-tooltip :title="record.webUrlTemplate">
              <span class="text-ellipsis">{{ record.webUrlTemplate || '--' }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'requiredParams'">
            <a-space wrap>
              <a-tag v-for="p in record.requiredParams" :key="p.key" color="orange">{{ p.label }}（{{ p.key }}）</a-tag>
              <span v-if="!record.requiredParams || record.requiredParams.length === 0" class="text-tertiary">无</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-switch :checked="record.status === 1" @change="(v) => handleStatusChange(record, v)" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleEditParams(record)">参数设置</a>
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a-popconfirm title="确定要删除该平台吗？" @confirm="handleDelete(record)">
                <a type="link" size="small" :danger="true">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 平台编辑/新增弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="720px"
      :footer="null"
      destroy-on-close
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="平台代码" name="code">
              <a-input v-model:value="formData.code" :disabled="isEdit" placeholder="如 douyin / xiaohongshu / meituan" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="平台名称" name="name">
              <a-input v-model:value="formData.name" placeholder="如 抖音" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="品牌色">
              <a-input v-model:value="formData.color" placeholder="#fe2c55">
                <template #prefix>
                  <div class="color-preview" :style="{ background: formData.color || '#1677ff' }"></div>
                </template>
              </a-input>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图标URL">
              <a-input v-model:value="formData.icon" placeholder="可留空，前端用首字母兜底" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="描述">
          <a-input v-model:value="formData.description" placeholder="一句话描述平台用途" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="跳转方式" name="jumpMode">
              <a-select v-model:value="formData.jumpMode">
                <a-select-option value="scheme">URL Scheme（唤起APP）</a-select-option>
                <a-select-option value="webview">H5链接（浏览器打开）</a-select-option>
                <a-select-option value="miniprogram">小程序跳转</a-select-option>
                <a-select-option value="copy">复制链接</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序">
              <a-input-number v-model:value="formData.sort" :min="0" :max="9999" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="状态">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">停用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="URL Scheme 模板">
          <a-input v-model:value="formData.schemeTemplate" placeholder="如 snssdk1128://goods/store?sec_shop_id={sec_shop_id}（{param}会被商家填写的参数替换）" />
        </a-form-item>
        <a-form-item label="H5 链接模板">
          <a-input v-model:value="formData.webUrlTemplate" placeholder="如 https://www.dianping.com/shop/{shop_id}/review" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="小程序 AppID">
              <a-input v-model:value="formData.miniprogramAppid" placeholder="跳转方式为小程序时填写" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="小程序路径模板">
              <a-input v-model:value="formData.miniprogramPathTemplate" placeholder="如 pages/shop/index?id={sec_shop_id}" />
            </a-form-item>
          </a-col>
        </a-row>
        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">保存</a-button>
        </div>
      </a-form>
    </a-modal>

    <!-- 参数定义编辑弹窗 -->
    <a-modal
      v-model:open="paramsModalVisible"
      :title="`${currentPlatform?.name} - 参数定义`"
      width="720px"
      :footer="null"
      destroy-on-close
    >
      <div class="params-section">
        <div class="params-section-header">
          <div class="params-section-title">必填参数</div>
          <a-button size="small" type="dashed" @click="addRequiredParam">
            <template #icon><PlusOutlined /></template>
            添加必填参数
          </a-button>
        </div>
        <div v-if="formData.requiredParams && formData.requiredParams.length > 0" class="param-list">
          <div v-for="(param, idx) in formData.requiredParams" :key="'r'+idx" class="param-row">
            <a-input v-model:value="param.key" placeholder="参数key（英文）" style="width: 25%" />
            <a-input v-model:value="param.label" placeholder="参数名称（中文）" style="width: 25%" />
            <a-input v-model:value="param.placeholder" placeholder="输入提示语" style="flex: 1" />
            <a-button :danger="true" size="small" @click="removeRequiredParam(idx)">
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </div>
        </div>
        <a-empty v-else description="暂无必填参数" />
      </div>

      <div class="params-section">
        <div class="params-section-header">
          <div class="params-section-title">可选参数</div>
          <a-button size="small" type="dashed" @click="addOptionalParam">
            <template #icon><PlusOutlined /></template>
            添加可选参数
          </a-button>
        </div>
        <div v-if="formData.optionalParams && formData.optionalParams.length > 0" class="param-list">
          <div v-for="(param, idx) in formData.optionalParams" :key="'o'+idx" class="param-row">
            <a-input v-model:value="param.key" placeholder="参数key（英文）" style="width: 25%" />
            <a-input v-model:value="param.label" placeholder="参数名称（中文）" style="width: 25%" />
            <a-input v-model:value="param.placeholder" placeholder="输入提示语" style="flex: 1" />
            <a-button :danger="true" size="small" @click="removeOptionalParam(idx)">
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </div>
        </div>
        <a-empty v-else description="暂无可选参数" />
      </div>

      <div class="params-tip">
        <a-alert
          type="info"
          show-icon
          message="模板中的 {param} 占位符会被商家填写的参数值替换，如 {sec_shop_id} 会被商家填写的 sec_shop_id 值替换"
        />
      </div>

      <div class="modal-footer">
        <a-button @click="paramsModalVisible = false">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="handleParamsSubmit">保存参数定义</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import {
  getPlatformList,
  createPlatform,
  updatePlatform,
  deletePlatform
} from '@/api/marketing'

const loading = ref(false)
const dataSource = ref([])
const searchKeyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (t) => `共 ${t} 条`
})

const modalVisible = ref(false)
const modalTitle = ref('新增平台')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const paramsModalVisible = ref(false)
const currentPlatform = ref(null)

const defaultFormData = () => ({
  id: null,
  code: '',
  name: '',
  icon: '',
  color: '#1677ff',
  description: '',
  jumpMode: 'scheme',
  schemeTemplate: '',
  webUrlTemplate: '',
  miniprogramAppid: '',
  miniprogramPathTemplate: '',
  requiredParams: [],
  optionalParams: [],
  sort: 0,
  status: 1
})

const formData = reactive(defaultFormData())

const formRules = {
  code: [{ required: true, message: '请输入平台代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入平台名称', trigger: 'blur' }],
  jumpMode: [{ required: true, message: '请选择跳转方式', trigger: 'change' }]
}

const columns = [
  { title: '平台', dataIndex: 'name', key: 'name', width: 200, fixed: 'left' },
  { title: '跳转方式', dataIndex: 'jumpMode', key: 'jumpMode', width: 140 },
  { title: 'URL Scheme', dataIndex: 'schemeTemplate', key: 'schemeTemplate', width: 260, ellipsis: true },
  { title: 'H5链接', dataIndex: 'webUrlTemplate', key: 'webUrlTemplate', width: 260, ellipsis: true },
  { title: '必填参数', dataIndex: 'requiredParams', key: 'requiredParams', width: 220 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

const jumpModeText = (mode) => {
  const map = { scheme: 'URL Scheme', webview: 'H5链接', miniprogram: '小程序', copy: '复制链接' }
  return map[mode] || mode
}

const jumpModeColor = (mode) => {
  const map = { scheme: 'blue', webview: 'green', miniprogram: 'purple', copy: 'orange' }
  return map[mode] || 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPlatformList({
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value
    })
    const list = (res?.list || []).map(item => ({
      ...item,
      sort: item.sortOrder,
      status: item.enabled ? 1 : 0,
      icon: item.iconUrl,
      requiredParams: typeof item.requiredParams === 'string' ? JSON.parse(item.requiredParams || '[]') : (item.requiredParams || []),
      optionalParams: typeof item.optionalParams === 'string' ? JSON.parse(item.optionalParams || '[]') : (item.optionalParams || [])
    }))
    dataSource.value = list
    pagination.total = res?.total || 0
  } catch (e) {
    console.error('加载平台列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const resetForm = () => {
  Object.assign(formData, defaultFormData())
  formData.requiredParams = []
  formData.optionalParams = []
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增平台'
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑平台'
  resetForm()
  Object.assign(formData, {
    id: record.id,
    code: record.code,
    name: record.name,
    icon: record.icon,
    color: record.color,
    description: record.description,
    jumpMode: record.jumpMode,
    schemeTemplate: record.schemeTemplate,
    webUrlTemplate: record.webUrlTemplate,
    miniprogramAppid: record.miniprogramAppid,
    miniprogramPathTemplate: record.miniprogramPathTemplate,
    requiredParams: JSON.parse(JSON.stringify(record.requiredParams || [])),
    optionalParams: JSON.parse(JSON.stringify(record.optionalParams || [])),
    sort: record.sort,
    status: record.status
  })
  modalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    const f = JSON.parse(JSON.stringify(formData))
    // 字段名对齐后端 PromotionPlatformDTO 契约，requiredParams/optionalParams 为 JSON 字符串
    const body = {
      code: f.code,
      name: f.name,
      description: f.description,
      iconUrl: f.icon,
      color: f.color,
      jumpMode: f.jumpMode,
      schemeTemplate: f.schemeTemplate,
      webUrlTemplate: f.webUrlTemplate,
      miniprogramAppid: f.miniprogramAppid,
      miniprogramPathTemplate: f.miniprogramPathTemplate,
      sortOrder: f.sort,
      requiredParams: JSON.stringify(f.requiredParams || []),
      optionalParams: JSON.stringify(f.optionalParams || [])
    }
    if (isEdit.value) {
      await updatePlatform(f.id, body)
      message.success('更新成功')
    } else {
      await createPlatform(body)
      message.success('创建成功')
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
    await deletePlatform(record.id)
    message.success('删除成功')
    loadData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

const handleStatusChange = async (record, checked) => {
  try {
    await updatePlatform(record.id, { enabled: checked })
    record.status = checked ? 1 : 0
    message.success(checked ? '已启用' : '已停用')
  } catch (e) {
    console.error('状态更新失败', e)
  }
}

const handleEditParams = (record) => {
  currentPlatform.value = record
  resetForm()
  Object.assign(formData, {
    id: record.id,
    requiredParams: JSON.parse(JSON.stringify(record.requiredParams || [])),
    optionalParams: JSON.parse(JSON.stringify(record.optionalParams || []))
  })
  paramsModalVisible.value = true
}

const addRequiredParam = () => {
  if (!formData.requiredParams) formData.requiredParams = []
  formData.requiredParams.push({ key: '', label: '', placeholder: '', required: 1 })
}

const removeRequiredParam = (idx) => {
  formData.requiredParams.splice(idx, 1)
}

const addOptionalParam = () => {
  if (!formData.optionalParams) formData.optionalParams = []
  formData.optionalParams.push({ key: '', label: '', placeholder: '', required: 0 })
}

const removeOptionalParam = (idx) => {
  formData.optionalParams.splice(idx, 1)
}

const handleParamsSubmit = async () => {
  // 校验
  for (const p of [...(formData.requiredParams || []), ...(formData.optionalParams || [])]) {
    if (!p.key || !p.label) {
      message.warning('参数 key 和名称不能为空')
      return
    }
  }
  try {
    submitting.value = true
    await updatePlatform(formData.id, {
      requiredParams: JSON.stringify(formData.requiredParams || []),
      optionalParams: JSON.stringify(formData.optionalParams || [])
    })
    message.success('参数定义保存成功')
    paramsModalVisible.value = false
    loadData()
  } catch (e) {
    console.error('保存参数失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.platforms-page {
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
  align-items: center;
  margin-bottom: 16px;

  .table-title {
    font-size: 16px;
    font-weight: 600;
    color: $text-color;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .table-actions {
    display: flex;
    gap: 12px;
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

.platform-name-text {
  font-size: 14px;
  font-weight: 500;
  color: $text-color;
}

.platform-code-text {
  font-size: 12px;
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

.text-tertiary {
  color: $text-tertiary;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.color-preview {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  display: inline-block;
}

.params-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.params-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.params-section-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-color;
}

.param-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.params-tip {
  margin-bottom: 16px;
}
</style>
