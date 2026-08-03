<template>
  <div class="merchant-access-page">
    <div class="page-header">
      <div class="page-title">商家权限配置</div>
      <div class="page-desc">配置管理员角色可查看哪些商家的数据</div>
    </div>

    <div class="content-wrapper">
      <div class="left-panel">
        <div class="panel-header">
          <span class="panel-title">管理员列表</span>
          <a-tag color="blue">{{ adminList.length }} 人</a-tag>
        </div>
        <div class="admin-list">
          <div
            v-for="admin in adminList"
            :key="admin.id"
            class="admin-item"
            :class="{ active: selectedAdminId === admin.id }"
            @click="handleSelectAdmin(admin)"
          >
            <a-avatar size="small" style="background-color: #1677ff">
              {{ (admin.nickname || admin.username || 'A').charAt(0) }}
            </a-avatar>
            <div class="admin-info">
              <div class="admin-name">
                {{ admin.nickname || admin.username }}
                <a-tag v-if="admin.role === 'super_admin'" color="red" size="small">超管</a-tag>
                <a-tag v-else color="blue" size="small">管理员</a-tag>
              </div>
              <div class="admin-username">@{{ admin.username }}</div>
            </div>
            <div class="admin-merchant-count">
              {{ admin.merchantAccess?.length || 0 }} 商家
            </div>
          </div>
          <div v-if="adminList.length === 0" class="empty-tip">
            暂无管理员
          </div>
        </div>
      </div>

      <div class="right-panel">
        <template v-if="selectedAdmin">
          <div class="panel-header">
            <div class="admin-detail-header">
              <a-avatar size="default" style="background-color: #1677ff">
                {{ (selectedAdmin.nickname || selectedAdmin.username || 'A').charAt(0) }}
              </a-avatar>
              <div class="admin-detail-info">
                <div class="admin-detail-name">
                  {{ selectedAdmin.nickname || selectedAdmin.username }}
                  <a-tag v-if="selectedAdmin.role === 'super_admin'" color="red">超级管理员</a-tag>
                  <a-tag v-else color="blue">管理员</a-tag>
                </div>
                <div class="admin-detail-username">账号：{{ selectedAdmin.username }}</div>
              </div>
            </div>
          </div>

          <div class="merchant-section">
            <div class="section-header">
              <span class="section-title">可访问商家</span>
              <span class="section-count">已选 {{ selectedMerchantIds.length }} / {{ merchantList.length }}</span>
            </div>

            <div class="search-wrapper">
              <a-input
                v-model:value="searchKeyword"
                placeholder="搜索商家名称"
                allow-clear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>

            <div class="merchant-actions">
              <a-checkbox
                :indeterminate="isIndeterminate"
                v-model:checked="checkAll"
                @change="onCheckAllChange"
              >
                全选
              </a-checkbox>
            </div>

            <div class="merchant-list">
              <a-checkbox-group v-model:value="selectedMerchantIds">
                <div class="merchant-grid">
                  <div
                    v-for="merchant in filteredMerchants"
                    :key="merchant.id"
                    class="merchant-item"
                  >
                    <a-checkbox :value="merchant.id">
                      <div class="merchant-item-content">
                        <div class="merchant-name">{{ merchant.name }}</div>
                        <div v-if="merchant.contact_name" class="merchant-contact">
                          联系人：{{ merchant.contact_name }}
                        </div>
                      </div>
                    </a-checkbox>
                  </div>
                </div>
              </a-checkbox-group>
              <div v-if="filteredMerchants.length === 0" class="empty-tip">
                未找到匹配的商家
              </div>
            </div>
          </div>

          <div class="footer-actions">
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" :loading="saving" @click="handleSave">
              保存配置
            </a-button>
          </div>
        </template>
        <template v-else>
          <div class="empty-state">
            <UserOutlined class="empty-icon" />
            <div class="empty-text">请从左侧选择一个管理员</div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import {
  getAdminMerchantAccessList,
  getAdminMerchantAccess,
  updateAdminMerchantAccess
} from '@/api/system'
import { getMerchantList } from '@/api/merchant'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const adminList = ref([])
const merchantList = ref([])
const selectedAdminId = ref(null)
const selectedAdmin = ref(null)
const selectedMerchantIds = ref([])
const searchKeyword = ref('')

const roleLabelMap = {
  super_admin: '超级管理员',
  admin: '管理员'
}

const filteredMerchants = computed(() => {
  if (!searchKeyword.value) return merchantList.value
  const keyword = searchKeyword.value.toLowerCase()
  return merchantList.value.filter(m =>
    m.name.toLowerCase().includes(keyword) ||
    (m.contact_name && m.contact_name.toLowerCase().includes(keyword))
  )
})

const checkAll = computed(() => {
  const filteredIds = filteredMerchants.value.map(m => m.id)
  return filteredIds.length > 0 && filteredIds.every(id => selectedMerchantIds.value.includes(id))
})

const isIndeterminate = computed(() => {
  const filteredIds = filteredMerchants.value.map(m => m.id)
  const selectedCount = filteredIds.filter(id => selectedMerchantIds.value.includes(id)).length
  return selectedCount > 0 && selectedCount < filteredIds.length
})

const loadAdminList = async () => {
  loading.value = true
  try {
    const res = await getAdminMerchantAccessList()
    adminList.value = res || []
  } catch (e) {
    console.error('加载管理员列表失败', e)
  } finally {
    loading.value = false
  }
}

const loadMerchantList = async () => {
  try {
    const res = await getMerchantList({ page: 1, pageSize: 1000 })
    merchantList.value = res.list || res || []
  } catch (e) {
    console.error('加载商家列表失败', e)
  }
}

const handleSelectAdmin = async (admin) => {
  selectedAdminId.value = admin.id
  selectedAdmin.value = admin
  try {
    const res = await getAdminMerchantAccess(admin.id)
    selectedMerchantIds.value = res.merchantIds || []
  } catch (e) {
    console.error('加载管理员商家权限失败', e)
    selectedMerchantIds.value = []
  }
}

const onCheckAllChange = (e) => {
  const checked = e.target.checked
  const filteredIds = filteredMerchants.value.map(m => m.id)
  if (checked) {
    const newIds = [...new Set([...selectedMerchantIds.value, ...filteredIds])]
    selectedMerchantIds.value = newIds
  } else {
    selectedMerchantIds.value = selectedMerchantIds.value.filter(
      id => !filteredIds.includes(id)
    )
  }
}

const handleReset = async () => {
  if (selectedAdminId.value) {
    const res = await getAdminMerchantAccess(selectedAdminId.value)
    selectedMerchantIds.value = res.merchantIds || []
    message.info('已重置')
  }
}

const handleSave = async () => {
  if (!selectedAdminId.value) {
    message.warning('请先选择管理员')
    return
  }
  saving.value = true
  try {
    await updateAdminMerchantAccess(selectedAdminId.value, selectedMerchantIds.value)
    message.success('保存成功')
    const admin = adminList.value.find(a => a.id === selectedAdminId.value)
    if (admin) {
      admin.merchantAccess = merchantList.value
        .filter(m => selectedMerchantIds.value.includes(m.id))
        .map(m => ({ id: m.id, name: m.name }))
    }
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadAdminList()
  loadMerchantList()
})
</script>

<style lang="scss" scoped>
.merchant-access-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: $text-tertiary;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);
}

.left-panel {
  width: 320px;
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
}

.right-panel {
  flex: 1;
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-color;
}

.admin-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.admin-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #e6f4ff;
    border: 1px solid #91caff;
  }
}

.admin-info {
  flex: 1;
  min-width: 0;
}

.admin-name {
  font-size: 14px;
  font-weight: 500;
  color: $text-color;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.admin-username {
  font-size: 12px;
  color: $text-tertiary;
}

.admin-merchant-count {
  font-size: 12px;
  color: $text-secondary;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
  flex-shrink: 0;
}

.admin-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-detail-info {
  flex: 1;
}

.admin-detail-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-color;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.admin-detail-username {
  font-size: 13px;
  color: $text-secondary;
}

.merchant-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-color;
}

.section-count {
  font-size: 13px;
  color: $text-secondary;
}

.search-wrapper {
  margin-bottom: 12px;
  flex-shrink: 0;
}

.merchant-actions {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid $border-color;
  flex-shrink: 0;
}

.merchant-list {
  flex: 1;
  overflow-y: auto;
}

.merchant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 8px;
}

.merchant-item {
  padding: 10px 12px;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  transition: all 0.2s;

  &:hover {
    border-color: $primary-color;
    background: #f0f8ff;
  }
}

.merchant-item-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.merchant-name {
  font-size: 14px;
  color: $text-color;
  font-weight: 500;
}

.merchant-contact {
  font-size: 12px;
  color: $text-tertiary;
}

.footer-actions {
  padding: 16px 20px;
  border-top: 1px solid $border-color;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  color: #d9d9d9;
}

.empty-text {
  font-size: 14px;
  color: $text-tertiary;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: $text-tertiary;
  font-size: 14px;
}
</style>
