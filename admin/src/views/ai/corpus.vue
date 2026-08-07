<template>
  <div class="corpus-page">
    <div class="page-header">
      <div class="page-title">语料管理</div>
      <div class="page-desc">管理 AI 创作使用的语料素材</div>
    </div>
    
    <div class="stats-row">
      <div class="storage-card card-wrapper">
        <div class="storage-header">
          <span class="storage-label">存储空间</span>
          <span class="storage-total">{{ storageInfo.usedText }} / {{ storageInfo.totalText }}</span>
        </div>
        <a-progress
          :percent="storageInfo.percent"
          :stroke-color="storageInfo.percent > 80 ? '#ff4d4f' : '#1677ff'"
          :show-info="false"
          size="small"
          class="storage-progress"
        />
        <div class="storage-types">
          <div class="type-item">
            <span class="type-dot text-dot"></span>
            <span class="type-label">文字</span>
            <span class="type-size">{{ storageInfo.textSize }}</span>
          </div>
          <div class="type-item">
            <span class="type-dot image-dot"></span>
            <span class="type-label">图片</span>
            <span class="type-size">{{ storageInfo.imageSize }}</span>
          </div>
          <div class="type-item">
            <span class="type-dot video-dot"></span>
            <span class="type-label">视频</span>
            <span class="type-size">{{ storageInfo.videoSize }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="所属商家" v-if="userStore.isAdmin">
          <a-select 
            v-model:value="searchForm.merchantId" 
            placeholder="全部商家"
            style="width: 160px"
            allow-clear
            show-search
            :options="merchantOptions"
          />
        </a-form-item>
        <a-form-item label="类型">
          <a-select 
            v-model:value="searchForm.type" 
            placeholder="全部类型"
            style="width: 140px"
            allow-clear
          >
            <a-select-option value="text">文字</a-select-option>
            <a-select-option value="image">图片</a-select-option>
            <a-select-option value="video">视频</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="分类">
          <a-select 
            v-model:value="searchForm.category" 
            placeholder="全部分类"
            style="width: 140px"
            allow-clear
          >
            <a-select-option v-for="cat in categoryOptions" :key="cat.id || cat.value" :value="cat.value || cat.id">
              {{ cat.label || cat.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input 
            v-model:value="searchForm.keyword" 
            placeholder="搜索关键词"
            style="width: 200px"
            allow-clear
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
            <a-button @click="openCategoryModal" v-if="userStore.isAdmin">
              <template #icon><FolderOutlined /></template>
              分类管理
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
    
    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">
          语料列表
          <a-tag color="blue">共 {{ pagination.total }} 条</a-tag>
        </div>
        <div class="table-actions">
          <a-space>
            <a-button @click="handleImport">
              <template #icon><UploadOutlined /></template>
              批量导入
            </a-button>
            <a-button type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新增语料
            </a-button>
          </a-space>
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
          <template v-if="column.key === 'title'">
            <div class="title-text">{{ record.title || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeName(record.type) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'category'">
            <a-tag color="blue">
              {{ getCategoryName(record.category) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'content'">
            <div class="content-preview">{{ record.content || record.fileUrl || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'fileSize'">
            <span class="file-size">{{ formatFileSize(record.fileSize) }}</span>
          </template>
          <template v-else-if="column.key === 'tags'">
            <div class="tags-wrapper">
              <template v-if="getTagList(record.tags).length">
                <a-tag v-for="tag in getTagList(record.tags)" :key="tag" color="default">{{ tag }}</a-tag>
              </template>
              <span v-else>-</span>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge 
              :status="record.status === 1 ? 'success' : 'default'" 
              :text="record.status === 1 ? '启用' : '禁用'" 
            />
          </template>
          <template v-else-if="column.key === 'createTime'">
            <span class="create-time">{{ formatTime(record.createdAt || record.createTime) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a-popconfirm
                title="确定要删除这条语料吗？"
                @confirm="handleDelete(record)"
              >
                <a type="link" size="small" :danger="true">删除</a>
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
        <a-form-item label="类型" name="type">
          <a-radio-group v-model:value="formData.type">
            <a-radio value="text">文字</a-radio>
            <a-radio value="image">图片</a-radio>
            <a-radio value="video">视频</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="标题" name="title">
          <a-input v-model:value="formData.title" placeholder="请输入语料标题" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="语料分类" name="category">
              <a-select 
                v-model:value="formData.category" 
                placeholder="请选择分类"
                style="width: 100%"
              >
                <a-select-option v-for="cat in categoryOptions" :key="cat.id || cat.value" :value="cat.value || cat.id">
                  {{ cat.label || cat.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="适用行业" name="industry">
              <a-select 
                v-model:value="formData.industry" 
                placeholder="请选择行业"
                style="width: 100%"
                allow-clear
              >
                <a-select-option value="food">餐饮美食</a-select-option>
                <a-select-option value="beauty">美容美发</a-select-option>
                <a-select-option value="retail">零售购物</a-select-option>
                <a-select-option value="service">生活服务</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="formData.type === 'text'" label="语料内容" name="content">
          <a-textarea 
            v-model:value="formData.content" 
            placeholder="请输入语料内容"
            :rows="6"
            show-count
            :max-length="500"
          />
        </a-form-item>
        <a-form-item v-else-if="formData.type === 'image'" label="上传图片" name="fileUrl">
          <a-upload
            :action="imageUploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :max-count="1"
            v-model:file-list="imageFileList"
            @change="handleImageUploadChange"
            @remove="handleImageUploadRemove"
            :before-upload="beforeImageUpload"
          >
            <div>
              <UploadOutlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item v-else-if="formData.type === 'video'" label="上传视频" name="fileUrl">
          <a-upload
            :action="fileUploadAction"
            :headers="uploadHeaders"
            :max-count="1"
            v-model:file-list="videoFileList"
            @change="handleVideoUploadChange"
            @remove="handleVideoUploadRemove"
            :before-upload="beforeVideoUpload"
          >
            <a-button>
              <template #icon><UploadOutlined /></template>
              上传视频
            </a-button>
          </a-upload>
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="formData.tags"
            mode="tags"
            placeholder="输入标签后按回车添加"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <div class="form-actions">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </a-button>
        </div>
      </a-form>
    </a-modal>
    
    <a-modal
      v-model:open="categoryModalVisible"
      title="分类管理"
      width="500px"
      :footer="null"
      destroy-on-close
    >
      <div class="category-manage">
        <a-form :model="categoryForm" layout="inline" class="category-form">
          <a-form-item>
            <a-input 
              v-model:value="categoryForm.name" 
              placeholder="请输入分类名称"
              style="width: 180px"
            />
          </a-form-item>
          <a-form-item>
            <a-input-number 
              v-model:value="categoryForm.sortOrder" 
              placeholder="排序"
              :min="0"
              style="width: 100px"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleCategorySubmit" :loading="categorySubmitting">
              {{ categoryEditId ? '保存' : '添加' }}
            </a-button>
            <a-button v-if="categoryEditId" @click="cancelCategoryEdit" style="margin-left: 8px">
              取消
            </a-button>
          </a-form-item>
        </a-form>
        <a-table
          :columns="categoryColumns"
          :data-source="categoryList"
          :pagination="false"
          :row-key="record => record.id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-space size="small">
                <a type="link" size="small" @click="editCategory(record)">编辑</a>
                <a-popconfirm
                  title="确定要删除这个分类吗？"
                  @confirm="deleteCategory(record)"
                >
                  <a type="link" size="small" :danger="true">删除</a>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  UploadOutlined,
  FolderOutlined
} from '@ant-design/icons-vue'
import {
  getCorpusList,
  createCorpus,
  updateCorpus,
  deleteCorpus,
  getCorpusStorage,
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory as apiDeleteCategory
} from '@/api/ai'
import { getMerchantList } from '@/api/merchant'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { formatFileSize } from '@/utils/format'
import { isSuccessCode } from '@/utils/request'

const appStore = useAppStore()
const userStore = useUserStore()
const tableLoading = ref(false)

const searchForm = reactive({
  category: undefined,
  keyword: '',
  merchantId: undefined,
  type: undefined
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

const categoryList = ref([])
const categoryModalVisible = ref(false)
const categoryEditId = ref(null)
const categorySubmitting = ref(false)
const categoryForm = reactive({
  name: '',
  sortOrder: 0
})

const categoryColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '分类名称', dataIndex: 'name', key: 'name' },
  { title: '排序', dataIndex: 'sort_order', key: 'sort_order', width: 100 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 140 }
]

const categoryOptions = computed(() => {
  if (categoryList.value.length > 0) {
    return categoryList.value.map(c => ({
      label: c.name,
      value: String(c.id)
    }))
  }
  return [
    { label: '好评模板', value: 'review' },
    { label: '推广文案', value: 'promotion' },
    { label: '店铺介绍', value: 'description' }
  ]
})

const loadCategories = async () => {
  try {
    const params = {}
    const mid = getCurrentMerchantId()
    if (mid) params.merchantId = mid
    const res = await getCategories(params)
    const list = Array.isArray(res) ? res : (res.list || res.data || [])
    categoryList.value = list
  } catch (e) {
    console.error('加载分类列表失败', e)
  }
}

const openCategoryModal = () => {
  categoryEditId.value = null
  categoryForm.name = ''
  categoryForm.sortOrder = 0
  loadCategories()
  categoryModalVisible.value = true
}

const handleCategorySubmit = async () => {
  if (!categoryForm.name) {
    message.warning('请输入分类名称')
    return
  }
  categorySubmitting.value = true
  try {
    if (categoryEditId.value) {
      await updateCategory(categoryEditId.value, {
        name: categoryForm.name,
        sortOrder: categoryForm.sortOrder
      })
      message.success('更新成功')
    } else {
      const mid = getCurrentMerchantId()
      await createCategory({
        name: categoryForm.name,
        sortOrder: categoryForm.sortOrder,
        merchantId: mid || undefined
      })
      message.success('添加成功')
    }
    categoryForm.name = ''
    categoryForm.sortOrder = 0
    categoryEditId.value = null
    loadCategories()
  } catch (e) {
    console.error('分类操作失败', e)
  } finally {
    categorySubmitting.value = false
  }
}

const editCategory = (record) => {
  categoryEditId.value = record.id
  categoryForm.name = record.name
  categoryForm.sortOrder = record.sort_order || record.sortOrder || 0
}

const cancelCategoryEdit = () => {
  categoryEditId.value = null
  categoryForm.name = ''
  categoryForm.sortOrder = 0
}

const deleteCategory = async (record) => {
  try {
    await apiDeleteCategory(record.id)
    message.success('删除成功')
    loadCategories()
  } catch (e) {
    console.error('删除分类失败', e)
  }
}

const getTagList = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  if (typeof tags === 'string') {
    const t = tags.trim()
    if (!t) return []
    try {
      const parsed = JSON.parse(t)
      if (Array.isArray(parsed)) return parsed
    } catch (e) {
      // 非 JSON，按逗号分隔处理
    }
    return t.split(',').filter(x => x.trim())
  }
  return []
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 160 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 110 },
  { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '文件大小', dataIndex: 'fileSize', key: 'fileSize', width: 120 },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 180 },
  { title: '使用次数', dataIndex: 'viewCount', key: 'useCount', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 140, fixed: 'right' }
]

const dataSource = ref([])
const modalVisible = ref(false)
const modalTitle = ref('新增语料')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const formData = reactive({
  id: null,
  type: 'text',
  title: '',
  category: 'review',
  industry: undefined,
  content: '',
  fileUrl: '',
  fileSize: 0,
  tags: [],
  status: 1
})

const formRules = computed(() => ({
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [
    {
      required: formData.type === 'text',
      message: '请输入语料内容',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (formData.type === 'text' && !value) {
          callback(new Error('请输入语料内容'))
        } else {
          callback()
        }
      }
    }
  ],
  fileUrl: [
    {
      required: formData.type !== 'text',
      message: '请上传文件',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (formData.type !== 'text' && !value) {
          callback(new Error('请上传文件'))
        } else {
          callback()
        }
      }
    }
  ]
}))

const imageFileList = ref([])
const videoFileList = ref([])

const imageUploadAction = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/admin/upload/image`
  : '/api/admin/upload/image'

const fileUploadAction = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL}/admin/upload/file`
  : '/api/admin/upload/file'

const uploadHeaders = (() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})()

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('图片大小不能超过10MB！')
    return false
  }
  return true
}

const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    message.error('只能上传视频文件！')
    return false
  }
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    message.error('视频大小不能超过100MB！')
    return false
  }
  return true
}

const handleImageUploadChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (isSuccessCode(res)) {
      formData.fileUrl = res.data.url
      formData.fileSize = res.data.size || info.file.size || 0
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
      imageFileList.value = imageFileList.value.filter(f => f.uid !== info.file.uid)
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
    imageFileList.value = imageFileList.value.filter(f => f.uid !== info.file.uid)
  }
}

const handleImageUploadRemove = () => {
  formData.fileUrl = ''
  formData.fileSize = 0
}

const handleVideoUploadChange = (info) => {
  if (info.file.status === 'done') {
    const res = info.file.response
    if (isSuccessCode(res)) {
      formData.fileUrl = res.data.url
      formData.fileSize = res.data.size || info.file.size || 0
      message.success('上传成功')
    } else {
      message.error(res?.message || '上传失败')
      videoFileList.value = videoFileList.value.filter(f => f.uid !== info.file.uid)
    }
  } else if (info.file.status === 'error') {
    message.error('上传失败')
    videoFileList.value = videoFileList.value.filter(f => f.uid !== info.file.uid)
  }
}

const handleVideoUploadRemove = () => {
  formData.fileUrl = ''
  formData.fileSize = 0
}

const getTypeName = (type) => {
  const map = { text: '文字', image: '图片', video: '视频' }
  return map[type] || type
}

const formatTime = (value) => {
  if (!value) return '-'
  const d = new Date(value)
  if (isNaN(d.getTime())) return value
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const getTypeColor = (type) => {
  const map = { text: 'blue', image: 'green', video: 'orange' }
  return map[type] || 'default'
}

const getCategoryName = (category) => {
  if (categoryList.value.length > 0) {
    const cat = categoryList.value.find(c => String(c.id) === String(category))
    if (cat) return cat.name
  }
  const map = { review: '好评模板', promotion: '推广文案', description: '店铺介绍' }
  return map[category] || category || '-'
}

const storageData = ref({
  usedBytes: 0,
  totalBytes: 100 * 1024 * 1024,
  textBytes: 0,
  imageBytes: 0,
  videoBytes: 0
})

const storageInfo = computed(() => {
  const total = storageData.value.totalBytes || 100 * 1024 * 1024
  const used = storageData.value.usedBytes || 0
  const percent = total > 0 ? Math.round((used / total) * 100) : 0
  return {
    usedText: formatFileSize(used),
    totalText: formatFileSize(total),
    percent: Math.min(percent, 100),
    textSize: formatFileSize(storageData.value.textBytes || 0),
    imageSize: formatFileSize(storageData.value.imageBytes || 0),
    videoSize: formatFileSize(storageData.value.videoBytes || 0)
  }
})

const getCurrentMerchantId = () => {
  if (userStore.isAdmin) {
    return appStore.merchantId || searchForm.merchantId || ''
  }
  return userStore.userInfo?.merchantId || ''
}

const loadStorageInfo = async () => {
  try {
    const merchantId = getCurrentMerchantId()
    if (!merchantId) {
      storageData.value = {
        usedBytes: 0,
        totalBytes: 100 * 1024 * 1024,
        textBytes: 0,
        imageBytes: 0,
        videoBytes: 0
      }
      return
    }
    const res = await getCorpusStorage(merchantId)
    if (res) {
      storageData.value = {
        usedBytes: res.usedBytes || res.used || 0,
        totalBytes: res.totalBytes || res.total || res.limitBytes || 100 * 1024 * 1024,
        textBytes: res.textBytes || res.textSize || 0,
        imageBytes: res.imageBytes || res.imageSize || 0,
        videoBytes: res.videoBytes || res.videoSize || 0
      }
    }
  } catch (e) {
    console.error('加载存储空间信息失败', e)
    storageData.value = {
      usedBytes: 0,
      totalBytes: 100 * 1024 * 1024,
      textBytes: 0,
      imageBytes: 0,
      videoBytes: 0
    }
  }
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
    if (searchForm.type !== undefined) params.type = searchForm.type
    if (searchForm.category !== undefined) params.category = searchForm.category
    if (searchForm.keyword) params.keyword = searchForm.keyword
    const res = await getCorpusList(params)
    const list = res.list || res || []
    dataSource.value = Array.isArray(list) ? list : []
    pagination.total = res.total || 0
  } catch (e) {
    console.error('加载语料列表失败', e)
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
  loadStorageInfo()
}

const handleReset = () => {
  searchForm.category = undefined
  searchForm.keyword = ''
  searchForm.merchantId = undefined
  searchForm.type = undefined
  pagination.current = 1
  loadData()
  loadStorageInfo()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增语料'
  Object.assign(formData, {
    id: null,
    type: 'text',
    title: '',
    category: 'review',
    industry: undefined,
    content: '',
    fileUrl: '',
    fileSize: 0,
    tags: [],
    status: 1
  })
  imageFileList.value = []
  videoFileList.value = []
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑语料'
  Object.assign(formData, {
    id: record.id,
    type: record.type || 'text',
    title: record.title || '',
    category: record.category,
    industry: record.industry,
    content: record.content || '',
    fileUrl: record.fileUrl || '',
    fileSize: record.fileSize || 0,
    tags: record.tags,
    status: record.status
  })
  imageFileList.value = []
  videoFileList.value = []
  if (record.type === 'image' && record.fileUrl) {
    imageFileList.value = [{
      uid: '-1',
      name: 'image',
      status: 'done',
      url: record.fileUrl,
      thumbUrl: record.fileUrl
    }]
  }
  if (record.type === 'video' && record.fileUrl) {
    videoFileList.value = [{
      uid: '-1',
      name: 'video',
      status: 'done',
      url: record.fileUrl
    }]
  }
  modalVisible.value = true
}

const handleDelete = async (record) => {
  try {
    await deleteCorpus(record.id)
    message.success('删除成功')
    loadData()
    loadStorageInfo()
  } catch (e) {
    console.error('删除语料失败', e)
  }
}

const handleImport = () => {
  message.info('批量导入功能开发中')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = { ...formData }
    // 后端 tags 为 JSON 字符串，前端表单是数组，需序列化
    if (Array.isArray(submitData.tags)) {
      submitData.tags = JSON.stringify(submitData.tags)
    }
    
    if (isEdit.value) {
      await updateCorpus(formData.id, submitData)
      message.success('编辑成功')
    } else {
      await createCorpus(submitData)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
    loadStorageInfo()
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
    loadStorageInfo()
    loadCategories()
  }
})

onMounted(() => {
  loadMerchantOptions()
  loadCategories()
  if (userStore.isAdmin && appStore.merchantId) {
    searchForm.merchantId = appStore.merchantId
  }
  loadData()
  loadStorageInfo()
})
</script>

<style lang="scss" scoped>
.corpus-page {
  padding: 24px;
}

.search-card {
  padding: 16px 20px;
  margin-bottom: 16px;
  
  :deep(.ant-form-inline) {
    display: flex;
    flex-wrap: wrap;
    row-gap: 12px;
    column-gap: 0;
    align-items: flex-start;
  }
  
  :deep(.ant-form-item) {
    margin-bottom: 0;
    margin-right: 16px;
  }
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.storage-card {
  width: 360px;
  padding: 18px 22px;
  
  .storage-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .storage-label {
      font-size: 14px;
      font-weight: 500;
      color: $text-color;
    }
    
    .storage-total {
      font-size: 13px;
      color: $text-secondary;
    }
  }
  
  .storage-progress {
    margin-bottom: 12px;
  }
  
  .storage-types {
    display: flex;
    gap: 20px;
    
    .type-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: $text-secondary;
      
      .type-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }
      
      .text-dot {
        background: #1677ff;
      }
      
      .image-dot {
        background: #52c41a;
      }
      
      .video-dot {
        background: #faad14;
      }
      
      .type-label {
        color: $text-tertiary;
      }
      
      .type-size {
        font-weight: 500;
        color: $text-secondary;
      }
    }
  }
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  max-width: 200px;
}

.create-time {
  color: $text-secondary;
  font-size: 13px;
}

.category-manage {
  .category-form {
    margin-bottom: 16px;
  }
}
</style>
