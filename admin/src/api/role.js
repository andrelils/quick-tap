import request from '@/utils/request'

// 注意：后端角色路径为 /admin/roles（复数形式）
// 系统中角色为预定义的三个：super_admin / admin / merchant
// 实际"创建/更新/删除角色"走的是管理员分配角色的接口

export const getRoleList = (params) => {
  return request.get('/admin/roles', { params })
}

export const getAllPermissions = () => {
  return request.get('/admin/roles/permissions')
}

export const getAllAdminsWithRoles = () => {
  return request.get('/admin/roles/admins')
}

export const getRolePermissionMatrix = () => {
  return request.get('/admin/roles/matrix')
}

export const assignRole = (adminId, roleId) => {
  return request.post('/admin/roles/assign', null, {
    params: { adminId, roleId }
  })
}

export const getRoleDetail = (roleId) => {
  return request.get(`/admin/roles/${roleId}`)
}

export const getUserPermissions = (userId) => {
  return request.get(`/admin/roles/${userId}/permissions`)
}

export const checkPermission = (userId, permission) => {
  return request.get(`/admin/roles/${userId}/check-permission`, {
    params: { permission }
  })
}

// 角色是预定义的三个 + 数据库中自定义角色
// 新增/编辑/删除走 /admin/roles 真实 CRUD 接口
export const createRole = (data) => {
  return request.post('/admin/roles', data)
}

export const updateRole = (id, data) => {
  // 后端按角色标识(code)更新
  return request.put(`/admin/roles/${id}`, data)
}

export const deleteRole = (id) => {
  return request.delete(`/admin/roles/${id}`)
}
