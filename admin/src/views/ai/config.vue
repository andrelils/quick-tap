<template>
  <div class="ai-config-page">
    <div class="page-header">
      <div class="page-title">创作配置</div>
      <div class="page-desc">配置各类型AI创作的默认提示词</div>
    </div>

    <div v-if="userStore.isAdmin" class="card-wrapper merchant-select-card">
      <a-form layout="inline">
        <a-form-item label="选择商家">
          <a-select
            v-model:value="selectedMerchantId"
            placeholder="请选择商家"
            style="width: 240px"
            show-search
            :options="merchantOptions"
            @change="handleMerchantChange"
          />
        </a-form-item>
      </a-form>
    </div>

    <div class="card-wrapper config-card">
      <a-tabs v-model:activeKey="activeTab" class="config-tabs">
        <a-tab-pane key="text" tab="文字提示词">
          <div class="tab-content text-theme">
            <div class="prompt-desc">
              <div class="desc-title">文字提示词使用说明</div>
              <div class="desc-content">
                <p>配置文字创作的默认系统提示词，用于定义AI的角色定位、写作风格和输出规范。</p>
                <ul>
                  <li>可设定AI的角色身份（如：资深文案、营销专家等）</li>
                  <li>可定义输出内容的风格调性（如：正式、活泼、专业等）</li>
                  <li>可添加内容约束条件（如：字数限制、格式要求等）</li>
                  <li>配置后将作为文字创作的默认提示词使用</li>
                </ul>
              </div>
            </div>
            <a-textarea
              v-model:value="textPrompt"
              :rows="10"
              placeholder="请输入文字创作的默认系统提示词..."
              class="prompt-textarea"
            />
            <div class="action-bar">
              <a-button
                type="primary"
                :loading="textSaving"
                @click="handleSaveText"
                class="save-btn text-btn"
              >
                保存文字提示词
              </a-button>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="image" tab="图片提示词">
          <div class="tab-content image-theme">
            <div class="prompt-desc">
              <div class="desc-title">图片提示词使用说明</div>
              <div class="desc-content">
                <p>配置图片创作的默认提示词，用于定义图片的风格基调、视觉元素和质量要求。</p>
                <ul>
                  <li>可设定图片的整体风格（如：写实、卡通、简约等）</li>
                  <li>可定义常用的视觉元素和构图方式</li>
                  <li>可添加画质要求（如：高清、细节丰富等）</li>
                  <li>配置后将作为图片创作的默认提示词使用</li>
                </ul>
              </div>
            </div>
            <a-textarea
              v-model:value="imagePrompt"
              :rows="10"
              placeholder="请输入图片创作的默认系统提示词..."
              class="prompt-textarea"
            />
            <div class="action-bar">
              <a-button
                type="primary"
                :loading="imageSaving"
                @click="handleSaveImage"
                class="save-btn image-btn"
              >
                保存图片提示词
              </a-button>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="video" tab="视频提示词">
          <div class="tab-content video-theme">
            <div class="prompt-desc">
              <div class="desc-title">视频提示词使用说明</div>
              <div class="desc-content">
                <p>配置视频创作的默认提示词，用于定义视频的场景设定、镜头语言和风格基调。</p>
                <ul>
                  <li>可设定视频的整体风格和氛围</li>
                  <li>可定义常用的镜头运动和转场方式</li>
                  <li>可添加时长、节奏等约束条件</li>
                  <li>配置后将作为视频创作的默认提示词使用</li>
                </ul>
              </div>
            </div>
            <a-textarea
              v-model:value="videoPrompt"
              :rows="10"
              placeholder="请输入视频创作的默认系统提示词..."
              class="prompt-textarea"
            />
            <div class="action-bar">
              <a-button
                type="primary"
                :loading="videoSaving"
                @click="handleSaveVideo"
                class="save-btn video-btn"
              >
                保存视频提示词
              </a-button>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getAiConfig, updateAiConfig } from '@/api/ai'
import { getMerchantList } from '@/api/merchant'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const userStore = useUserStore()
const appStore = useAppStore()

const activeTab = ref('text')
const textPrompt = ref('')
const imagePrompt = ref('')
const videoPrompt = ref('')
const textSaving = ref(false)
const imageSaving = ref(false)
const videoSaving = ref(false)

const merchantOptions = ref([])
const selectedMerchantId = ref('')

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

const loadConfig = async () => {
  try {
    const merchantId = userStore.isAdmin ? selectedMerchantId.value : ''
    const res = await getAiConfig(merchantId)
    const data = res || {}
    textPrompt.value = data.textPrompt || ''
    imagePrompt.value = data.imagePrompt || ''
    videoPrompt.value = data.videoPrompt || ''
  } catch (e) {
    console.error('加载 AI 配置失败', e)
  }
}

const handleSaveText = async () => {
  try {
    textSaving.value = true
    const merchantId = userStore.isAdmin ? selectedMerchantId.value : ''
    const payload = { textPrompt: textPrompt.value }
    if (merchantId) {
      await updateAiConfig(merchantId, payload)
    } else {
      await updateAiConfig(payload)
    }
    message.success('文字提示词保存成功')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    textSaving.value = false
  }
}

const handleSaveImage = async () => {
  try {
    imageSaving.value = true
    const merchantId = userStore.isAdmin ? selectedMerchantId.value : ''
    const payload = { imagePrompt: imagePrompt.value }
    if (merchantId) {
      await updateAiConfig(merchantId, payload)
    } else {
      await updateAiConfig(payload)
    }
    message.success('图片提示词保存成功')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    imageSaving.value = false
  }
}

const handleSaveVideo = async () => {
  try {
    videoSaving.value = true
    const merchantId = userStore.isAdmin ? selectedMerchantId.value : ''
    const payload = { videoPrompt: videoPrompt.value }
    if (merchantId) {
      await updateAiConfig(merchantId, payload)
    } else {
      await updateAiConfig(payload)
    }
    message.success('视频提示词保存成功')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    videoSaving.value = false
  }
}

const handleMerchantChange = () => {
  loadConfig()
}

onMounted(() => {
  if (userStore.isAdmin) {
    loadMerchantOptions().then(() => {
      if (appStore.merchantId) {
        selectedMerchantId.value = appStore.merchantId
      } else if (merchantOptions.value.length > 0) {
        selectedMerchantId.value = merchantOptions.value[0].value
      }
      loadConfig()
    })
  } else {
    loadConfig()
  }
})

// 监听全局商家切换，自动同步并重新加载
watch(() => appStore.merchantId, (newVal) => {
  if (userStore.isAdmin) {
    selectedMerchantId.value = newVal || ''
    loadConfig()
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.ai-config-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: $text-color;
  line-height: 1.4;
}

.page-desc {
  font-size: 14px;
  color: $text-tertiary;
  margin-top: 6px;
}

.card-wrapper {
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  padding: 20px;
  margin-bottom: 16px;
}

.merchant-select-card {
  padding: 16px 20px;
}

.config-card {
  padding: 0;
  overflow: hidden;
}

.config-tabs {
  :deep(.ant-tabs-nav) {
    padding: 0 20px;
    margin: 0;
    border-bottom: 1px solid $border-color;
  }

  :deep(.ant-tabs-tab) {
    padding: 16px 20px;
    font-size: 15px;
  }
}

.tab-content {
  padding: 24px 20px;
}

.prompt-desc {
  border-radius: $border-radius-sm;
  padding: 16px 20px;
  margin-bottom: 20px;
  border-left: 4px solid;
  background: $bg-body;
}

.text-theme .prompt-desc {
  border-left-color: $primary-color;
  background: rgba(22, 119, 255, 0.04);
}

.image-theme .prompt-desc {
  border-left-color: $success-color;
  background: rgba(82, 196, 26, 0.04);
}

.video-theme .prompt-desc {
  border-left-color: #722ed1;
  background: rgba(114, 46, 209, 0.04);
}

.desc-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 10px;
}

.desc-content {
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.7;

  p {
    margin: 0 0 8px 0;
  }

  ul {
    margin: 0;
    padding-left: 20px;

    li {
      margin-bottom: 4px;
    }
  }
}

.prompt-textarea {
  font-size: 14px;
  line-height: 1.6;
  border-radius: $border-radius-sm;
}

.action-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  height: 40px;
  padding: 0 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
}

.text-btn {
  background: $primary-color;
  border-color: $primary-color;

  &:hover {
    background: lighten($primary-color, 5%);
    border-color: lighten($primary-color, 5%);
  }
}

.image-btn {
  background: $success-color;
  border-color: $success-color;

  &:hover {
    background: lighten($success-color, 5%);
    border-color: lighten($success-color, 5%);
  }
}

.video-btn {
  background: #722ed1;
  border-color: #722ed1;

  &:hover {
    background: lighten(#722ed1, 5%);
    border-color: lighten(#722ed1, 5%);
  }
}
</style>
