<template>
  <div class="role-manage-page">
    <div class="page-header">
      <div class="page-title">角色管理</div>
      <div class="page-desc">管理系统角色及权限</div>
    </div>

    <div class="card-wrapper table-card">
      <div class="table-header">
        <div class="table-title">
          角色列表
          <a-tag color="blue">共 {{ dataSource.length }} 个</a-tag>
        </div>
        <div class="table-actions">
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增角色
          </a-button>
        </div>
      </div>

      <a-table :columns="columns" :data-source="dataSource" :pagination="false" :row-key="record => record.id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'permissions'">
            <a-space wrap>
              <a-tag v-for="perm in record.permissions" :key="perm" color="blue">{{ getPermissionLabel(perm) }}</a-tag>
              <span v-if="!record.permissions || record.permissions.length === 0">--</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            {{ record.createdAt ? dayjs(record.createdAt).format('YYYY-MM-DD HH:mm:ss') : '--' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a-popconfirm v-if="!isDefaultRole(record.name)" title="确定要删除该角色吗？" @confirm="handleDelete(record)">
                <a type="link" size="small" :danger="true">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal v-model:open="modalVisible" :title="modalTitle" width="520px" :footer="null" destroy-on-close>
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-form-item label="角色标识" name="name">
          <a-input v-model:value="formData.name" :disabled="isEdit && isDefaultRole(formData.name)" placeholder="如：editor" />
        </a-form-item>
        <a-form-item label="角色名称" name="description">
          <a-input v-model:value="formData.description" placeholder="如：编辑" />
        </a-form-item>
        <a-form-item label="权限">
          <a-checkbox-group v-model:value="formData.permissions" :options="permissionOptions" />
        </a-form-item>
        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">确定</a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getRoleList, createRole, updateRole, deleteRole } from '@/api/role'

const loading = ref(false)
const dataSource = ref([])
const modalVisible = ref(false)
const modalTitle = ref('新增角色')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const formData = reactive({ id: null, name: '', description: '', permissions: [] })

const formRules = {
  name: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  description: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const defaultRoles = ['super_admin', 'admin', 'merchant']
const isDefaultRole = (name) => defaultRoles.includes(name)

const permissionOptions = [
  { label: '仪表盘', value: 'dashboard' },
  { label: '商家管理', value: 'merchant' },
  { label: '设备管理', value: 'device' },
  { label: 'AI创作', value: 'ai' },
  { label: '营销管理', value: 'marketing' },
  { label: '系统设置', value: 'system' }
]

const permissionLabelMap = {
  dashboard: '仪表盘',
  merchant: '商家管理',
  device: '设备管理',
  ai: 'AI创作',
  marketing: '营销管理',
  system: '系统设置'
}

const getPermissionLabel = (key) => {
  return permissionLabelMap[key] || key
}

const columns = [
  { title: '角色标识', dataIndex: 'name', key: 'name', width: 160 },
  { title: '角色名称', dataIndex: 'description', key: 'description', width: 160 },
  { title: '权限', dataIndex: 'permissions', key: 'permissions' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 140, fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ page: 1, pageSize: 100 })
    dataSource.value = res || []
  } catch (e) {
    console.error('加载角色列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增角色'
  Object.assign(formData, { id: null, name: '', description: '', permissions: [] })
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑角色'
  Object.assign(formData, { id: record.id, name: record.name, description: record.description, permissions: record.permissions || [] })
  modalVisible.value = true
}

const handleDelete = async (record) => {
  try { await deleteRole(record.id); message.success('删除成功'); loadData() }
  catch (e) { console.error('删除失败', e) }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (isEdit.value) {
      await updateRole(formData.id, { name: formData.name, description: formData.description, permissions: formData.permissions })
      message.success('编辑成功')
    } else {
      await createRole({ name: formData.name, description: formData.description, permissions: formData.permissions })
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>
.role-manage-page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 20px; font-weight: 600; color: #1f1f1f; margin-bottom: 8px; }
.page-desc { font-size: 14px; color: #8c8c8c; }
.table-card { padding: 20px 24px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; color: #1f1f1f; display: flex; align-items: center; gap: 12px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0; }
</style>
