import request from '@/utils/request'

export const getMerchantList = (params) => {
  return request.get('/merchant/list', { params })
}

export const getMerchantDetail = (id) => {
  return request.get(`/merchant/${id}`)
}

export const createMerchant = (data) => {
  return request.post('/merchant', data)
}

export const updateMerchant = (id, data) => {
  return request.put(`/merchant/${id}`, data)
}

export const deleteMerchant = (id) => {
  return request.delete(`/merchant/${id}`)
}

// 审核商户：后端提供 approve（通过）和 reject（拒绝）两个独立接口
// status: 1=通过 0=拒绝 或 'APPROVED'/'REJECTED'
export const auditMerchant = (id, status) => {
  const isPass =
    status === 1 ||
    status === '1' ||
    (typeof status === 'string' && status.toUpperCase() === 'APPROVED') ||
    (typeof status === 'object' &&
      (status.status === 1 ||
        (typeof status.status === 'string' && status.status.toUpperCase() === 'APPROVED')))
  const endpoint = isPass ? 'approve' : 'reject'
  return request.put(`/merchant/${id}/${endpoint}`)
}

// 启用/禁用商户：后端提供 enable / disable 两个独立接口
// status: 1=启用 0=禁用
export const updateMerchantStatus = (id, status) => {
  let s = status
  if (typeof status === 'object' && status.status != null) s = status.status
  const endpoint = Number(s) === 1 ? 'enable' : 'disable'
  return request.put(`/merchant/${id}/${endpoint}`)
}

// 商家存储空间使用情况（目前复用额度接口获取 usage 即可，无则返回空避免报错）
export const getMerchantStorage = (id) => {
  return request.get(`/merchant/merchant-quota/usage`, { params: { merchantId: id } })
    .catch(() => ({ total: 0, used: 0, free: 0 }))
}

// 商户额度管理（管理员视角）
export const getMerchantQuotaList = (params) => {
  // 后端提供 /admin/merchant-quota/list（分页返回各商户额度）
  return request.get('/admin/merchant-quota/list', { params })
}

export const updateMerchantQuota = (id, data) => {
  // 后端：/admin/merchant-quota/{merchantId}/adjust 调整存储与AI生成额度（0 表示不限）
  return request.post(`/admin/merchant-quota/${id}/adjust`, data)
}

// 商家自己的额度 & 订单
export const getMyQuota = (params) => {
  // 后端 MerchantQuotaController: /merchant/merchant-quota/usage
  // 商家自身可不传 merchantId（从 token 取）；管理员可传 merchantId 指定查询
  return request.get('/merchant/merchant-quota/usage', { params })
}

export const getMyOrders = (params) => {
  // 后端 OrderController: /order/merchant/{merchantId} 作为商家维度订单
  // 如未传 merchantId，则走通用 /order/list
  const merchantId = params?.merchantId
  if (merchantId) {
    const { merchantId: _m, ...rest } = params || {}
    return request.get(`/order/merchant/${merchantId}`, { params: rest })
  }
  return request.get('/order/list', { params })
}
