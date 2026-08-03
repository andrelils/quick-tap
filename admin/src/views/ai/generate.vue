<template>
  <div class="ai-generate-page">
    <div class="page-header">
      <div class="page-title">AI 创作</div>
      <div class="page-desc">使用 AI 生成好评文案、宣传图片与视频素材</div>
    </div>
    
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="24" :md="10" :lg="10">
        <div class="card-wrapper form-card">
          <div class="card-title">创作设置</div>
          
          <div class="type-tabs">
            <div 
              v-for="item in typeOptions" 
              :key="item.value"
              class="type-tab"
              :class="{ active: generateType === item.value }"
              @click="generateType = item.value"
            >
              <component :is="item.icon" class="tab-icon" />
              <span class="tab-text">{{ item.label }}</span>
            </div>
          </div>
          
          <a-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            layout="vertical"
          >
            <a-form-item label="所属商家" name="merchantId">
              <a-select 
                v-model:value="formData.merchantId" 
                placeholder="请选择商家"
                style="width: 100%"
                show-search
                :options="merchantOptions"
                @change="handleMerchantChange"
              />
            </a-form-item>

            <a-form-item label="创作模式" name="mode">
              <a-radio-group v-model:value="formData.mode" @change="handleModeChange">
                <a-radio value="new">全新创作</a-radio>
                <a-radio value="secondary">二次创作</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item
              v-if="formData.mode === 'secondary'"
              label="选择语料"
              name="corpusId"
            >
              <a-select
                v-model:value="formData.corpusId"
                placeholder="请选择语料库中的材料"
                style="width: 100%"
                show-search
                :options="corpusOptions"
                :loading="corpusLoading"
                :not-found-content="corpusLoading ? '加载中...' : '暂无可用语料'"
              />
            </a-form-item>
            
            <a-form-item label="提示词" name="prompt">
              <a-textarea 
                v-model:value="formData.prompt" 
                placeholder="请输入创作提示词，描述你想要生成的内容"
                :rows="4"
                show-count
                :max-length="500"
              />
            </a-form-item>

            <template v-if="generateType === 'text'">
              <a-form-item label="文案类型">
                <a-select 
                  v-model:value="formData.textType" 
                  placeholder="请选择文案类型"
                  style="width: 100%"
                >
                  <a-select-option value="review">好评文案</a-select-option>
                  <a-select-option value="promotion">推广文案</a-select-option>
                  <a-select-option value="description">店铺介绍</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="字数">
                <a-slider 
                  v-model:value="formData.length" 
                  :min="50" 
                  :max="500" 
                  :step="10"
                />
                <div class="slider-value">{{ formData.length }} 字</div>
              </a-form-item>
              <a-form-item label="风格">
                <a-radio-group v-model:value="formData.style">
                  <a-radio value="formal">正式</a-radio>
                  <a-radio value="casual">活泼</a-radio>
                  <a-radio value="professional">专业</a-radio>
                  <a-radio value="humorous">幽默</a-radio>
                </a-radio-group>
              </a-form-item>
            </template>
            
            <template v-else-if="generateType === 'image'">
              <a-form-item label="图片类型">
                <a-select 
                  v-model:value="formData.imageType" 
                  placeholder="请选择图片类型"
                  style="width: 100%"
                >
                  <a-select-option value="poster">宣传海报</a-select-option>
                  <a-select-option value="product">产品展示</a-select-option>
                  <a-select-option value="scene">场景配图</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="风格">
                <a-radio-group v-model:value="formData.imageStyle">
                  <a-radio value="realistic">写实</a-radio>
                  <a-radio value="cartoon">卡通</a-radio>
                  <a-radio value="minimalist">简约</a-radio>
                </a-radio-group>
              </a-form-item>
            </template>

            <template v-else-if="generateType === 'video'">
              <a-form-item label="视频类型">
                <a-select 
                  v-model:value="formData.videoType" 
                  placeholder="请选择视频类型"
                  style="width: 100%"
                >
                  <a-select-option value="promo">宣传视频</a-select-option>
                  <a-select-option value="product">产品展示</a-select-option>
                  <a-select-option value="scene">场景视频</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="风格">
                <a-radio-group v-model:value="formData.videoStyle">
                  <a-radio value="realistic">写实</a-radio>
                  <a-radio value="cartoon">卡通</a-radio>
                  <a-radio value="cinematic">电影感</a-radio>
                </a-radio-group>
              </a-form-item>
            </template>
            
            <a-form-item label="生成数量">
              <a-radio-group v-model:value="formData.count">
                <a-radio :value="1">1 条</a-radio>
                <a-radio :value="3">3 条</a-radio>
                <a-radio :value="5">5 条</a-radio>
              </a-radio-group>
            </a-form-item>
            
            <div class="quota-info">
              <span>剩余额度：</span>
              <span class="quota-count">{{ remainingQuota }}</span>
              <span> 次</span>
            </div>
            
            <a-button 
              type="primary" 
              block 
              size="large"
              :loading="generating"
              @click="handleGenerate"
            >
              <template #icon><ThunderboltOutlined /></template>
              立即生成
            </a-button>
          </a-form>
        </div>
      </a-col>
      
      <a-col :xs="24" :sm="24" :md="14" :lg="14">
        <div class="card-wrapper result-card">
          <div class="card-header">
            <div class="card-title">生成结果</div>
            <a-space v-if="results.length > 0">
              <a-button size="small" @click="handleCopyAll">
                <template #icon><CopyOutlined /></template>
                全部复制
              </a-button>
              <a-button size="small" @click="handleSave">
                <template #icon><SaveOutlined /></template>
                保存
              </a-button>
            </a-space>
          </div>
          
          <div v-if="generating" class="loading-box">
            <a-spin size="large" />
            <div class="loading-text">AI 正在创作中，请稍候...</div>
          </div>
          
          <div v-else-if="results.length === 0" class="empty-box">
            <FileTextOutlined class="empty-icon" />
            <div class="empty-text">暂无生成记录</div>
            <div class="empty-desc">设置参数后点击「立即生成」开始创作</div>
          </div>
          
          <div v-else class="result-list">
            <div 
              v-for="(item, index) in results" 
              :key="index"
              class="result-item"
            >
              <div class="result-header">
                <a-tag color="blue">{{ typeLabel(item) }} {{ index + 1 }}</a-tag>
                <a-space>
                  <a-button type="link" size="small" @click="handleCopy(item)">
                    <template #icon><CopyOutlined /></template>
                    复制
                  </a-button>
                  <a-button type="link" size="small" @click="handleRegenerate(index)">
                    <template #icon><ReloadOutlined /></template>
                    重新生成
                  </a-button>
                </a-space>
              </div>
              <div v-if="itemType(item) === 'text'" class="result-content text-content">
                {{ item.content }}
              </div>
              <div v-else-if="itemType(item) === 'image'" class="result-content image-content">
                <img v-if="item.url" :src="item.url" class="result-image" alt="AI 生成图片" />
                <div v-else class="image-placeholder">
                  <PictureOutlined />
                  <span>图片预览</span>
                </div>
              </div>
              <div v-else-if="itemType(item) === 'video'" class="result-content video-content">
                <video
                  v-if="item.url"
                  :src="item.url"
                  controls
                  class="video-player"
                ></video>
                <div v-else class="image-placeholder">
                  <VideoCameraOutlined />
                  <span>视频预览</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ThunderboltOutlined,
  CopyOutlined,
  SaveOutlined,
  ReloadOutlined,
  FileTextOutlined,
  PictureOutlined,
  EditOutlined,
  CameraOutlined,
  VideoCameraOutlined
} from '@ant-design/icons-vue'
import { generateText, generateImage, generateVideo, getCorpusList, getGenerateHistory, createCorpus as saveCorpus } from '@/api/ai'
import { getMerchantList } from '@/api/merchant'

const remainingQuota = ref(285)
const formRef = ref()

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

const loadQuota = async () => {
  try {
    const res = await getGenerateHistory({ current: 1, pageSize: 1 })
    if (res && res.total !== undefined) {
      remainingQuota.value = Math.max(0, 500 - res.total)
    }
  } catch (e) {
    console.error('加载额度失败', e)
  }
}

const generateType = ref('text')
const generating = ref(false)

const typeOptions = [
  { value: 'text', label: '文字生成', icon: EditOutlined },
  { value: 'image', label: '图片生成', icon: CameraOutlined },
  { value: 'video', label: '视频生成', icon: VideoCameraOutlined }
]

const merchantOptions = ref([])
const corpusOptions = ref([])
const corpusLoading = ref(false)

const formData = reactive({
  merchantId: undefined,
  mode: 'new',
  corpusId: undefined,
  prompt: '',
  textType: 'review',
  length: 150,
  style: 'casual',
  imageType: 'poster',
  imageStyle: 'realistic',
  videoType: 'promo',
  videoStyle: 'realistic',
  count: 3
})

// 二次创作时，语料选择必填的动态校验
const validateCorpus = async (rule, value) => {
  if (formData.mode === 'secondary' && !value) {
    return Promise.reject('二次创作请选择语料')
  }
  return Promise.resolve()
}

const rules = {
  merchantId: [{ required: true, message: '请选择所属商家', trigger: 'change' }],
  mode: [{ required: true, message: '请选择创作模式', trigger: 'change' }],
  corpusId: [{ required: true, validator: validateCorpus, trigger: 'change' }],
  prompt: [{ required: true, message: '请输入提示词', trigger: 'blur' }]
}

const loadCorpusOptions = async () => {
  if (!formData.merchantId) {
    corpusOptions.value = []
    return
  }
  corpusLoading.value = true
  try {
    const res = await getCorpusList({ current: 1, pageSize: 100, merchantId: formData.merchantId })
    const list = res.list || res || []
    corpusOptions.value = (Array.isArray(list) ? list : []).map(c => ({
      label: c.title || c.category || `语料 ${c.id}`,
      value: c.id
    }))
  } catch (e) {
    console.error('加载语料列表失败', e)
    corpusOptions.value = []
  } finally {
    corpusLoading.value = false
  }
}

const handleMerchantChange = () => {
  formData.corpusId = undefined
  if (formData.mode === 'secondary') {
    loadCorpusOptions()
  }
}

const handleModeChange = () => {
  formData.corpusId = undefined
  if (formData.mode === 'secondary') {
    loadCorpusOptions()
  }
  formRef.value && formRef.value.clearValidate('corpusId')
}

const results = ref([])

const itemType = (item) => item.type || generateType.value

const typeLabel = (item) => {
  const t = itemType(item)
  if (t === 'text') return '文案'
  if (t === 'image') return '图片'
  if (t === 'video') return '视频'
  return '结果'
}

const buildRequestData = (count) => {
  const data = {
    merchantId: formData.merchantId,
    mode: formData.mode,
    corpusId: formData.mode === 'secondary' ? formData.corpusId : undefined,
    prompt: formData.prompt,
    count
  }
  if (generateType.value === 'text') {
    data.textType = formData.textType
    data.length = formData.length
    data.style = formData.style
  } else if (generateType.value === 'image') {
    data.imageType = formData.imageType
    data.imageStyle = formData.imageStyle
  } else if (generateType.value === 'video') {
    data.videoType = formData.videoType
    data.videoStyle = formData.videoStyle
  }
  return data
}

const callGenerate = async (data) => {
  if (generateType.value === 'text') return await generateText(data)
  if (generateType.value === 'image') return await generateImage(data)
  return await generateVideo(data)
}

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  generating.value = true
  results.value = []
  
  try {
    const data = buildRequestData(formData.count)
    const res = await callGenerate(data)
    const list = res.list || res || (Array.isArray(res) ? res : [res])
    results.value = Array.isArray(list) ? list : []
    if (results.value.length > 0 && results.value[0].quota !== undefined) {
      remainingQuota.value = results.value[0].quota
    }
    message.success('生成成功')
  } catch (e) {
    console.error('AI生成失败', e)
  } finally {
    generating.value = false
  }
}

const handleCopy = (item) => {
  const text = item.content || item.url || ''
  if (text) {
    navigator.clipboard.writeText(text)
    message.success('已复制到剪贴板')
  }
}

const handleCopyAll = () => {
  const text = results.value
    .map((r, i) => `${typeLabel(r)}${i + 1}：\n${r.content || r.url || ''}`)
    .join('\n\n')
  navigator.clipboard.writeText(text)
  message.success('已全部复制')
}

const handleSave = async () => {
  try {
    for (const item of results.value) {
      const content = item.content || item.url || ''
      if (!content) continue
      await saveCorpus({
        category: generateType.value === 'text' ? formData.textType : 'description',
        content,
        merchantId: formData.merchantId,
        type: generateType.value
      })
    }
    message.success('已保存到语料库')
  } catch (e) {
    console.error('保存失败', e)
  }
}

const handleRegenerate = async (index) => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  try {
    generating.value = true
    const data = buildRequestData(1)
    const res = await callGenerate(data)
    const list = res.list || res || (Array.isArray(res) ? res : [res])
    const newItem = Array.isArray(list) ? list[0] : null
    if (newItem) {
      results.value.splice(index, 1, newItem)
      message.success(`第 ${index + 1} 条已重新生成`)
    }
  } catch (e) {
    console.error('重新生成失败', e)
  } finally {
    generating.value = false
  }
}

onMounted(() => {
  loadMerchantOptions()
  loadQuota()
})
</script>

<style lang="scss" scoped>
.ai-generate-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1f1f1f;
    margin-bottom: 8px;
  }
  
  .page-desc {
    font-size: 14px;
    color: #8c8c8c;
  }
}

.form-card {
  padding: 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 20px;
}

.type-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.type-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #1677ff;
  }
  
  &.active {
    background: #e6f4ff;
    border-color: #1677ff;
    
    .tab-icon,
    .tab-text {
      color: #1677ff;
    }
  }
}

.tab-icon {
  font-size: 24px;
  color: #8c8c8c;
}

.tab-text {
  font-size: 13px;
  color: #8c8c8c;
}

.slider-value {
  font-size: 12px;
  color: #8c8c8c;
  text-align: right;
}

.quota-info {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 16px;
  text-align: center;
}

.quota-count {
  font-size: 18px;
  font-weight: 600;
  color: #1677ff;
}

.result-card {
  padding: 20px 24px;
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.loading-box,
.empty-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.loading-text {
  margin-top: 16px;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 64px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 13px;
  color: #bfbfbf;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.result-content {
  padding: 16px;
}

.text-content {
  font-size: 14px;
  line-height: 1.8;
  color: #1f1f1f;
}

.image-content,
.video-content {
  display: flex;
  justify-content: center;
}

.result-image {
  max-width: 100%;
  border-radius: 8px;
}

.video-player {
  max-width: 100%;
  border-radius: 8px;
}

.image-placeholder {
  width: 200px;
  height: 200px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 12px;
  gap: 8px;
  
  svg {
    font-size: 48px;
  }
}
</style>
