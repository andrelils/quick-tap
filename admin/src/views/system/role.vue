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
            {{ formatCreatedAt(record.createdAt) }}
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
          <a-input v-model:value="formData.name" :disabled="isEdit" placeholder="如：editor（小写字母/数字/下划线）" />
        </a-form-item>
        <a-form-item label="角色名称" name="description">
          <a-input v-model:value="formData.description" placeholder="如：编辑" />
        </a-form-item>
        <a-form-item label="权限（含菜单与操作权限）">
          <div class="perm-groups">
            <a-checkbox-group v-model:value="formData.permissions" class="perm-group-all">
              <div v-for="group in permissionGroups" :key="group.resource" class="perm-group">
                <div class="perm-group-title">{{ groupLabel(group.resource) }}</div>
                <div class="perm-checks">
                  <a-checkbox v-for="p in group.items" :key="p.code" :value="p.code">{{ p.description || p.code }}</a-checkbox>
                </div>
              </div>
            </a-checkbox-group>
          </div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getRoleList, getAllPermissions, createRole, updateRole, deleteRole } from '@/api/role'

const loading = ref(false)
const dataSource = ref([])
const modalVisible = ref(false)
const modalTitle = ref('新增角色')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const formData = reactive({ id: null, name: '', description: '', permissions: [] })

// 权限数据（来自后端 permissions 表）
const allPermissions = ref([])
const permissionGroups = computed(() => {
  const groups = []
  const map = {}
  allPermissions.value.forEach(p => {
    const res = p.resource || 'other'
    if (!map[res]) {
      map[res] = { resource: res, items: [] }
      groups.push(map[res])
    }
    map[res].items.push(p)
  })
  return groups
})

const resourceLabels = {
  dashboard: '仪表盘',
  merchant: '商家管理',
  device: '设备管理',
  ai: 'AI创作',
  marketing: '营销管理',
  system: '系统设置',
  other: '其他'
}
const groupLabel = (res) => resourceLabels[res] || res

// 权限 code → 中文名
const permNameMap = computed(() => {
  const m = {}
  allPermissions.value.forEach(p => { m[p.code] = p.description || p.code })
  return m
})
const getPermissionLabel = (code) => permNameMap.value[code] || code

const formRules = {
  name: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  description: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const defaultRoles = ['super_admin', 'admin', 'merchant']
const isDefaultRole = (name) => defaultRoles.includes(name)

const columns = [
  { title: '角色标识', dataIndex: 'name', key: 'name', width: 160 },
  { title: '角色名称', dataIndex: 'description', key: 'description', width: 160 },
  { title: '权限', dataIndex: 'permissions', key: 'permissions' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 140, fixed: 'right' }
]

// 创建时间：日期格式化为标准时间；非日期（如"系统内置"）原样显示
const formatCreatedAt = (val) => {
  if (!val) return '--'
  if (typeof val === 'string' && !/^\d{4}-\d{2}-\d{2}/.test(val)) return val
  return dayjs(val).format('YYYY-MM-DD HH:mm:ss')
}

const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    allPermissions.value = Array.isArray(res) ? res : (res?.list || [])
  } catch (e) {
    console.error('加载权限列表失败', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ page: 1, pageSize: 100 })
    const list = Array.isArray(res) ? res : (res?.list || [])
    dataSource.value = list.map(r => ({
      id: r.id,
      name: r.name || r.id,
      description: r.description || '--',
      permissions: r.permissions || [],
      createdAt: r.createdAt
    }))
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
  try { await deleteRole(record.name); message.success('删除成功'); loadData() }
  catch (e) { console.error('删除失败', e) }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (isEdit.value) {
      await updateRole(formData.name, { name: formData.description, permissions: formData.permissions })
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

onMounted(() => { loadPermissions(); loadData() })
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
.perm-groups { width: 100%; max-height: 320px; overflow-y: auto; border: 1px solid #f0f0f0; border-radius: 8px; padding: 12px; }
.perm-group { margin-bottom: 14px; }
.perm-group:last-child { margin-bottom: 0; }
.perm-group-title { font-weight: 600; font-size: 13px; color: #1f1f1f; margin-bottom: 8px; padding-bottom: 6px; border-bottom: 1px dashed #e8e8e8; }
.perm-checks { display: flex; flex-wrap: wrap; gap: 4px 16px; }
</style>
