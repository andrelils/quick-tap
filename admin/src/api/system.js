import request from '@/utils/request'

export const getSystemSettings = () => {
  return request.get('/admin/system/settings')
}

export const updateSystemSettings = (data) => {
  return request.put('/admin/system/settings', data)
}

// 管理员 <-> 商家 权限绑定接口（后端已在 SystemController 补齐）
export const getAdminMerchantAccessList = () => {
  return request.get('/admin/system/admin-merchant-access/list')
}

export const getAdminMerchantAccess = (adminId) => {
  return request.get(`/admin/system/admin-merchant-access/${adminId}`)
}

export const updateAdminMerchantAccess = (adminId, merchantIds) => {
  return request.post(`/admin/system/admin-merchant-access/${adminId}`, { merchantIds })
}
