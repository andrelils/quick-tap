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

// 角色是预定义的三个，这里的"创建/更新/删除角色"作为占位，直接返回成功，避免页面报错
export const createRole = (data) => {
  console.warn('当前系统角色为预定义枚举（super_admin/admin/merchant），不支持动态创建')
  return Promise.resolve(data)
}

export const updateRole = (id, data) => {
  console.warn('当前系统角色为预定义枚举（super_admin/admin/merchant），不支持动态修改')
  return Promise.resolve({ id, ...data })
}

export const deleteRole = (id) => {
  console.warn('当前系统角色为预定义枚举（super_admin/admin/merchant），不支持动态删除')
  return Promise.resolve({ id })
}
