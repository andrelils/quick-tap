<template>
  <div class="user-manage-page">
    <div class="page-header">
      <div class="page-title">用户管理</div>
      <div class="page-desc">管理系统管理员账号</div>
    </div>

    <div class="card-wrapper search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="账号/昵称">
          <a-input v-model:value="searchForm.keyword" placeholder="请输入账号或昵称" style="width: 200px" allow-clear />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="searchForm.role" placeholder="全部角色" style="width: 140px" allow-clear>
            <a-select-option value="super_admin">超级管理员</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="merchant">商家</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="全部状态" style="width: 120px" allow-clear>
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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
          用户列表
          <a-tag color="blue">共 {{ pagination.total }} 人</a-tag>
        </div>
        <div class="table-actions">
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增用户
          </a-button>
        </div>
      </div>

      <a-table :columns="columns" :data-source="dataSource" :pagination="false" :row-key="record => record.id" :loading="loading">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar :src="record.avatar" :size="32">
              {{ (record.nickname || record.username || '?').charAt(0) }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="roleColorMap[record.role]">{{ roleTextMap[record.role] || record.role }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="record.status === 1 ? 'success' : 'default'" :text="record.status === 1 ? '启用' : '禁用'" />
          </template>
          <template v-else-if="column.key === 'createdAt'">
            {{ record.createdAt ? dayjs(record.createdAt).format('YYYY-MM-DD HH:mm:ss') : '--' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a type="link" size="small" @click="handleEdit(record)">编辑</a>
              <a type="link" size="small" @click="handleResetPassword(record)">重置密码</a>
              <a-popconfirm title="确定要删除该用户吗？" @confirm="handleDelete(record)">
                <a type="link" size="small" :danger="true">删除</a>
              </a-popconfirm>
              <a-popconfirm v-if="record.status === 1" title="确定要禁用该用户吗？" @confirm="handleStatus(record, 0)">
                <a type="link" size="small" :danger="true">禁用</a>
              </a-popconfirm>
              <a-popconfirm v-else title="确定要启用该用户吗？" @confirm="handleStatus(record, 1)">
                <a type="link" size="small">启用</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <div class="pagination-wrapper">
        <a-pagination v-model:current="pagination.current" v-model:page-size="pagination.pageSize" :total="pagination.total"
          :page-size-options="['10', '20', '50']" show-size-changer show-quick-jumper :show-total="(total) => '共 ' + total + ' 条'" @change="handlePageChange" />
      </div>
    </div>

    <a-modal v-model:open="modalVisible" :title="modalTitle" width="520px" :footer="null" destroy-on-close>
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-form-item label="账号" name="username">
          <a-input v-model:value="formData.username" :disabled="isEdit" placeholder="请输入登录账号" />
        </a-form-item>
        <a-form-item v-if="isEdit" label="用户编号">
          <a-input :value="formData.userCode" disabled placeholder="系统自动生成" />
        </a-form-item>
        <a-form-item label="昵称" name="nickname">
          <a-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item v-if="!isEdit" label="密码" name="password">
          <a-input-password v-model:value="formData.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="角色" name="role">
          <a-select v-model:value="formData.role" placeholder="请选择角色">
            <a-select-option value="super_admin">超级管理员</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="merchant">商家</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <div class="modal-footer">
          <a-button @click="modalVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">确定</a-button>
        </div>
      </a-form>
    </a-modal>

    <a-modal v-model:open="pwdModalVisible" title="重置密码" width="440px" :footer="null" destroy-on-close>
      <a-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" layout="vertical">
        <a-form-item label="账号">
          <a-input :value="pwdForm.username" disabled />
        </a-form-item>
        <a-form-item label="新密码" name="password">
          <a-input-password v-model:value="pwdForm.password" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password v-model:value="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
        <div class="modal-footer">
          <a-button @click="pwdModalVisible = false">取消</a-button>
          <a-button type="primary" :loading="pwdSubmitting" @click="handlePwdSubmit">确定</a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { SearchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/user'

const searchForm = reactive({ keyword: '', role: undefined, status: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const loading = ref(false)
const dataSource = ref([])
const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const submitting = ref(false)
const formRef = ref()
const isEdit = ref(false)

const formData = reactive({ id: null, username: '', userCode: '', nickname: '', password: '', role: 'admin', status: 1 })

const formRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleTextMap = { super_admin: '超级管理员', admin: '管理员', merchant: '商家' }
const roleColorMap = { super_admin: 'red', admin: 'blue', merchant: 'green' }

const columns = [
  { title: '头像', dataIndex: 'avatar', key: 'avatar', width: 70 },
  { title: '账号', dataIndex: 'username', key: 'username', width: 140 },
  { title: '用户编号', dataIndex: 'userCode', key: 'userCode', width: 150 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 140 },
  { title: '角色', dataIndex: 'role', key: 'role', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 260, fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.current, pageSize: pagination.pageSize, keyword: searchForm.keyword }
    if (searchForm.role) params.role = searchForm.role
    if (searchForm.status !== undefined) params.status = searchForm.status
    const res = await getUserList(params)
    dataSource.value = res.list || []
    pagination.total = res.total || 0
  } catch (e) {
    console.error('加载用户列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.role = undefined; searchForm.status = undefined; pagination.current = 1; loadData() }
const handlePageChange = () => { loadData() }

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增用户'
  Object.assign(formData, { id: null, username: '', userCode: '', nickname: '', password: '', role: 'admin', status: 1 })
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  Object.assign(formData, { id: record.id, username: record.username, userCode: record.userCode || '', nickname: record.nickname, password: '', role: record.role, status: record.status })
  modalVisible.value = true
}

const handleDelete = async (record) => {
  try { await deleteUser(record.id); message.success('删除成功'); loadData() }
  catch (e) { console.error('删除失败', e) }
}

const handleStatus = async (record, status) => {
  try { await updateUserStatus(record.id, status); message.success(status === 1 ? '启用成功' : '禁用成功'); loadData() }
  catch (e) { console.error('操作失败', e) }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (isEdit.value) {
      const payload = { nickname: formData.nickname, role: formData.role, status: formData.status }
      if (formData.password) payload.password = formData.password
      await updateUser(formData.id, payload)
      message.success('编辑成功')
    } else {
      await createUser({ username: formData.username, nickname: formData.nickname, password: formData.password, role: formData.role, status: formData.status })
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

const pwdModalVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({ id: null, username: '', password: '', confirmPassword: '' })
const pwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleResetPassword = (record) => {
  pwdForm.id = record.id
  pwdForm.username = record.username
  pwdForm.password = ''
  pwdForm.confirmPassword = ''
  pwdModalVisible.value = true
}

const handlePwdSubmit = async () => {
  try {
    await pwdFormRef.value.validate()
    pwdSubmitting.value = true
    await resetUserPassword(pwdForm.id, pwdForm.password)
    message.success('密码重置成功')
    pwdModalVisible.value = false
  } catch (e) {
    if (e?.errorFields) return
    console.error('密码重置失败', e)
  } finally {
    pwdSubmitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.user-manage-page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 20px; font-weight: 600; color: #1f1f1f; margin-bottom: 8px; }
.page-desc { font-size: 14px; color: #8c8c8c; }
.search-card { padding: 20px 24px; margin-bottom: 16px; }
.table-card { padding: 20px 24px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; color: #1f1f1f; display: flex; align-items: center; gap: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0; }
</style>
