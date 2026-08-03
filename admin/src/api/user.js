import request from '@/utils/request'

export const getUserList = (params) => {
  return request.get('/admin/list', { params })
}

export const createUser = (data) => {
  return request.post('/admin', data)
}

export const updateUser = (id, data) => {
  return request.put(`/admin/${id}`, data)
}

export const deleteUser = (id) => {
  return request.delete(`/admin/${id}`)
}

// status: 1=启用, 0=禁用
export const updateUserStatus = (id, status) => {
  const endpoint = status === 1 ? 'enable' : 'disable'
  return request.put(`/admin/${id}/${endpoint}`)
}

export const resetUserPassword = (id, password) => {
  return request.put(`/admin/${id}/reset-password`, { password })
}
