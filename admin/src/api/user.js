import request from '@/utils/request'

export const getUserList = (params) => {
  return request.get('/admin/user/list', { params })
}

export const createUser = (data) => {
  return request.post('/admin/user', data)
}

export const updateUser = (id, data) => {
  return request.put(`/admin/user/${id}`, data)
}

export const deleteUser = (id) => {
  return request.delete(`/admin/user/${id}`)
}

// status: 1=启用, 0=禁用
export const updateUserStatus = (id, status) => {
  return request.put(`/admin/user/${id}/status`, null, { params: { status } })
}

export const resetUserPassword = (id, password) => {
  return request.put(`/admin/user/${id}/reset-password`, { password })
}
