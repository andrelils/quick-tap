/**
 * Error Message Localization Utility
 *
 * Provides translation and user-friendly error messages for common API errors
 * Maps technical error codes/messages to customer-facing Chinese messages
 *
 * Features:
 * - Fallback messages for unknown errors
 * - Contextual error messages based on operation type
 * - Support for custom error messages
 * - Error code mapping for standardized responses
 */

// Standard HTTP error messages
const HTTP_ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '您没有权限执行此操作',
  404: '请求的资源不存在',
  409: '操作冲突，请稍后重试',
  410: '请求的资源已删除',
  422: '请求数据验证失败',
  429: '请求过于频繁，请稍后再试',
  500: '服务器内部错误，请稍后重试',
  502: '网关错误，请稍后重试',
  503: '服务暂时不可用，请稍后重试',
  504: '请求超时，请稍后重试'
}

// Business error code messages
const BUSINESS_ERROR_MESSAGES = {
  // Auth errors
  USER_NOT_FOUND: '用户不存在',
  INVALID_PASSWORD: '用户名或密码错误',
  ACCOUNT_DISABLED: '该账号已被禁用',
  TOKEN_INVALID: 'Token 无效或已过期',
  TOKEN_EXPIRED: 'Token 已过期，请重新登录',
  PERMISSION_DENIED: '您没有权限执行此操作',

  // User management errors
  USERNAME_EXISTS: '用户名已存在',
  EMAIL_EXISTS: '邮箱已存在',
  PHONE_EXISTS: '手机号已存在',
  PASSWORD_TOO_SHORT: '密码长度至少 6 个字符',
  OLD_PASSWORD_WRONG: '原密码不正确',

  // Merchant errors
  MERCHANT_NOT_FOUND: '商家不存在',
  MERCHANT_EXISTS: '商家已存在',
  MERCHANT_DISABLED: '该商家已被禁用',
  INVALID_MERCHANT_DATA: '商家信息不完整或有误',

  // Admin errors
  ADMIN_NOT_FOUND: '管理员不存在',
  ADMIN_EXISTS: '管理员已存在',
  CANNOT_DELETE_SUPER_ADMIN: '不能删除超级管理员',
  INVALID_ROLE: '角色不合法',

  // File upload errors
  FILE_TOO_LARGE: '文件大小超过限制（最大 5MB）',
  INVALID_FILE_TYPE: '不支持的文件类型',
  UPLOAD_FAILED: '文件上传失败',
  STORAGE_LIMIT_EXCEEDED: '存储空间已满',

  // General errors
  OPERATION_FAILED: '操作失败',
  SAVE_FAILED: '保存失败，请稍后重试',
  DELETE_FAILED: '删除失败，请稍后重试',
  UPDATE_FAILED: '更新失败，请稍后重试',
  LOAD_FAILED: '加载失败，请稍后重试',
  NETWORK_ERROR: '网络连接失败，请检查网络设置',
  UNKNOWN_ERROR: '发生未知错误，请稍后重试'
}

// Error message patterns (regex-based fallback)
const ERROR_PATTERNS = [
  {
    pattern: /用户名.*存在/i,
    message: '用户名已存在'
  },
  {
    pattern: /密码.*短|密码.*长度/i,
    message: '密码长度不符合要求'
  },
  {
    pattern: /网络|连接|超时/i,
    message: '网络连接失败，请检查网络设置'
  },
  {
    pattern: /权限|允许|禁止/i,
    message: '您没有权限执行此操作'
  },
  {
    pattern: /已过期|过期/i,
    message: '登录已过期，请重新登录'
  }
]

/**
 * Get user-friendly error message from API error response
 * @param {Error|object} error - Error object or response data
 * @param {string} operation - Operation type (create, update, delete, upload, etc.)
 * @returns {string} - User-friendly error message
 */
export function getErrorMessage(error, operation = '') {
  // Handle error object with status code
  if (error?.response?.status) {
    const status = error.response.status
    const data = error.response.data

    // Try business error message first
    if (data?.code) {
      const msg = BUSINESS_ERROR_MESSAGES[data.code]
      if (msg) return msg
    }

    // Try custom message from response
    if (data?.message) {
      return formatErrorMessage(data.message, operation)
    }

    // Fall back to HTTP status message
    return HTTP_ERROR_MESSAGES[status] || HTTP_ERROR_MESSAGES[500]
  }

  // Handle error with code property
  if (error?.code) {
    return BUSINESS_ERROR_MESSAGES[error.code] || error.message || BUSINESS_ERROR_MESSAGES.UNKNOWN_ERROR
  }

  // Handle error message string
  if (typeof error === 'string') {
    return formatErrorMessage(error, operation)
  }

  // Handle error with message property
  if (error?.message) {
    return formatErrorMessage(error.message, operation)
  }

  return BUSINESS_ERROR_MESSAGES.UNKNOWN_ERROR
}

/**
 * Format raw error message to user-friendly message
 * @param {string} rawMessage - Raw error message from API
 * @param {string} operation - Operation type context
 * @returns {string} - Formatted error message
 */
function formatErrorMessage(rawMessage, operation = '') {
  if (!rawMessage) {
    return operation
      ? `${operation}失败，请稍后重试`
      : BUSINESS_ERROR_MESSAGES.OPERATION_FAILED
  }

  // Check if message matches known patterns
  for (const { pattern, message } of ERROR_PATTERNS) {
    if (pattern.test(rawMessage)) {
      return message
    }
  }

  // Return raw message if it's short and looks user-friendly
  if (rawMessage.length < 50 && !rawMessage.includes('Exception') && !rawMessage.includes('Error')) {
    return rawMessage
  }

  // Fallback with operation context
  return operation
    ? `${operation}失败：${rawMessage.substring(0, 30)}...`
    : BUSINESS_ERROR_MESSAGES.OPERATION_FAILED
}

/**
 * Get context-specific error message
 * @param {Error|object} error - Error object
 * @param {string} operation - Operation type (create, update, delete, etc.)
 * @returns {string} - Context-aware error message
 */
export function getContextErrorMessage(error, operation) {
  const baseMessage = getErrorMessage(error, operation)

  // Add operation context to generic messages
  const operationMap = {
    create: '新增',
    add: '添加',
    update: '编辑',
    delete: '删除',
    upload: '上传',
    download: '下载',
    import: '导入',
    export: '导出',
    save: '保存',
    search: '搜索',
    load: '加载',
    enable: '启用',
    disable: '禁用'
  }

  const operationName = operationMap[operation] || operation

  if (baseMessage.includes('失败')) {
    return baseMessage
  }

  return `${operationName}${baseMessage}`
}

/**
 * Parse validation errors from API response
 * @param {object} error - Error response
 * @returns {object} - Validation error map {fieldName: errorMessage}
 */
export function getValidationErrors(error) {
  if (!error?.response?.data) return {}

  const data = error.response.data

  // Handle errors array (validation error list)
  if (Array.isArray(data?.errors)) {
    const errors = {}
    data.errors.forEach(err => {
      if (err.field) {
        errors[err.field] = err.message || '字段验证失败'
      }
    })
    return errors
  }

  // Handle validation object
  if (data?.validationErrors && typeof data.validationErrors === 'object') {
    return data.validationErrors
  }

  return {}
}

/**
 * Convert error to message object for Ant Design message component
 * @param {Error|object} error - Error object
 * @param {string} operation - Operation type
 * @returns {object} - Message config {type: 'error', content: '...'}
 */
export function toMessageConfig(error, operation = '') {
  return {
    type: 'error',
    content: getContextErrorMessage(error, operation)
  }
}

/**
 * Handle API error with logging
 * @param {Error} error - Error object
 * @param {string} context - Error context for logging
 * @param {boolean} logToConsole - Whether to log to console (default: true)
 * @returns {string} - User-friendly error message
 */
export function handleApiError(error, context = '', logToConsole = true) {
  if (logToConsole) {
    console.error(`[${context}] API Error:`, error)
  }

  // Log error details for debugging
  if (error?.response?.data) {
    console.error(`  Status: ${error.response.status}`)
    console.error(`  Data:`, error.response.data)
  }

  return getErrorMessage(error, context)
}

/**
 * Validation error messages mapping
 */
export const VALIDATION_ERROR_MESSAGES = {
  required: '此项为必填项',
  email: '请输入有效的电子邮件地址',
  phone: '请输入有效的手机号',
  url: '请输入有效的网址',
  minLength: '字符数不能少于 {min} 个',
  maxLength: '字符数不能超过 {max} 个',
  min: '不能小于 {min}',
  max: '不能大于 {max}',
  pattern: '格式不符合要求',
  number: '请输入数字',
  integer: '请输入整数',
  decimal: '请输入数字，最多 {decimal} 位小数',
  range: '值必须在 {min} 到 {max} 之间'
}

export default {
  getErrorMessage,
  getContextErrorMessage,
  getValidationErrors,
  toMessageConfig,
  handleApiError,
  VALIDATION_ERROR_MESSAGES
}
