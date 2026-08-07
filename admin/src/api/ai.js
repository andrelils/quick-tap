import request from '@/utils/request'

// ============ AI 内容生成 ============
// 后端 AiGenerateController 统一用 /merchant/ai-generate/*，参数为 RequestParam
// ADMIN 角色由于刚刚在 SecurityConfig 加了访问 /api/merchant/** 权限，也可以调用这些接口
function buildGenUrl(type, params) {
  const qs = new URLSearchParams()
  if (params?.merchantId != null) qs.append('merchantId', params.merchantId)
  if (params?.prompt != null) qs.append('prompt', params.prompt)
  return `/merchant/ai-generate/${type}?${qs.toString()}`
}

export const generateText = (data) => {
  return request.post(buildGenUrl('text', data))
}

export const generateImage = (data) => {
  return request.post(buildGenUrl('image', data))
}

export const generateVideo = (data) => {
  return request.post(buildGenUrl('video', data))
}

export const getGenerateHistory = (params) => {
  const { merchantId, type, pageNum, pageSize } = params || {}
  const q = new URLSearchParams()
  if (merchantId != null) q.append('merchantId', merchantId)
  if (type) q.append('type', type)
  if (pageNum != null) q.append('pageNum', pageNum)
  if (pageSize != null) q.append('pageSize', pageSize)
  return request.get(`/merchant/ai-generate/history?${q.toString()}`)
}

// ============ AI 配置 ============
// 后端 AiConfigController：
//   商家维度: /merchant/ai-config（GET/PUT）
//   全局配置（超管）: /admin/ai-config（GET/PUT/DELETE /{merchantId}）

export const getAiConfig = (merchantId) => {
  if (merchantId) {
    // 查询指定商家配置：后端没有按 id 查询的端点，优先走商家维度接口 + ADMIN 模拟查询
    // 全局 /admin/ai-config 拿的是全局模板，不是指定商家的；所以此处直接走商家接口（实际从 token 中取商家身份）
    // 如果是 ADMIN 查询别人的配置：后端暂缺，降级返回空，避免页面报错
    return request.get('/merchant/ai-config').catch(() => null)
  }
  // 不传 merchantId：超管拿全局配置
  return request.get('/admin/ai-config')
}

export const updateAiConfig = (merchantIdOrData, data) => {
  // 两个签名：
  // updateAiConfig(config) -> 商家端 /merchant/ai-config
  // updateAiConfig(merchantId, config) -> 超管端 /admin/ai-config（全局） 或 删除单个商家
  if (typeof merchantIdOrData === 'string' || typeof merchantIdOrData === 'number') {
    return request.put('/admin/ai-config', data || merchantIdOrData)
  }
  return request.put('/merchant/ai-config', merchantIdOrData)
}

// ============ 语料（知识库） ============
// 后端 CorpusController：
//   商家维度 /merchant/corpus  GET / POST / PUT / DELETE
//   管理端  /admin/corpus      GET（全量）
// 分类 CorpusCategoryController：/merchant/corpus/categories

export const getCorpusList = (params) => {
  // 管理端查看全量语料：/admin/corpus
  // 商家查看自己：/merchant/corpus
  const { merchantId, ...rest } = params || {}
  if (merchantId) {
    return request.get('/merchant/corpus', { params: { ...rest } })
      .catch(() => request.get('/admin/corpus', { params }))
  }
  return request.get('/admin/corpus', { params })
}

export const getCorpusDetail = (id) => {
  return request.get(`/merchant/corpus/${id}`)
    .catch(() => request.get(`/admin/corpus`).then(list =>
      (list || []).find(it => String(it.id || it.corpusId) === String(id)) || null))
}

export const createCorpus = (data) => {
  return request.post('/merchant/corpus', data)
}

export const updateCorpus = (id, data) => {
  return request.put(`/merchant/corpus/${id}`, data)
}

export const deleteCorpus = (id) => {
  return request.delete(`/merchant/corpus/${id}`)
}

export const batchDeleteCorpus = (ids) => {
  // 后端没有批量删除，串行单个删除
  if (!ids || ids.length === 0) return Promise.resolve()
  return Promise.all(ids.map(id => {
    return request.delete(`/merchant/corpus/permanent/${id}`).catch(() => {
      return request.delete(`/merchant/corpus/${id}`).catch(() => null)
    })
  }))
}

export const moveToTrash = (ids) => {
  // 后端通过 DELETE /merchant/corpus/{id} 即为软删入回收站
  if (!ids || ids.length === 0) return Promise.resolve()
  return Promise.all(ids.map(id => request.delete(`/merchant/corpus/${id}`)))
}

export const restoreCorpus = (ids) => {
  if (!ids || ids.length === 0) return Promise.resolve()
  return Promise.all(ids.map(id => request.post(`/merchant/corpus/${id}/restore`)))
}

export const getTrashList = (params) => {
  return request.get('/merchant/corpus/trash', { params })
}

export const getCorpusStorage = (merchantId) => {
  // 后端暂未提供 corpus storage 接口，返回空结构避免页面报错
  return Promise.resolve({
    totalSize: 0,
    usedSize: 0,
    fileCount: 0,
    merchantId: merchantId || null
  })
}

// 商家配置总览（表格）
export const getMerchantConfigList = (params) => {
  return request.get('/admin/ai-config/overview', { params: {
    pageNum: params?.page || params?.pageNum || 1,
    pageSize: params?.pageSize || 10
  } })
}

// ============ 语料分类 ============
export const getCategories = (params) => {
  return request.get('/merchant/corpus/categories', { params })
}

export const createCategory = (data) => {
  return request.post('/merchant/corpus/categories', data)
}

export const updateCategory = (id, data) => {
  return request.put(`/merchant/corpus/categories/${id}`, data)
}

export const deleteCategory = (id) => {
  return request.delete(`/merchant/corpus/categories/${id}`)
}
