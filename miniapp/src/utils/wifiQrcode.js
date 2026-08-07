/**
 * WiFi二维码生成工具
 * 生成可被系统扫码识别的WiFi连接二维码
 *
 * WiFi QR码格式: WIFI:T:WPA;S:SSID;P:PASSWORD;;
 *
 * 参考标准:
 * - T: 加密方式 (WPA/WEP/nopass)
 * - S: 网络名称 (SSID)
 * - P: 密码
 * - H: 隐藏网络 (true/false)
 */

/**
 * 生成WiFi QR码内容
 * @param {string} ssid - WiFi名称
 * @param {string} password - WiFi密码
 * @param {string} encryption - 加密方式 (WPA/WEP/nopass) 默认WPA
 * @param {boolean} hidden - 是否隐藏网络 (默认false)
 * @returns {string} WiFi QR码内容
 */
export function generateWifiQrContent(ssid, password, encryption = 'WPA', hidden = false) {
  if (!ssid) {
    throw new Error('SSID不能为空')
  }

  // 对特殊字符进行转义
  const escapedSsid = escapeWifiString(ssid)
  const escapedPassword = password ? escapeWifiString(password) : ''

  // 确保加密方式有效
  const validEncryption = ['WPA', 'WEP', 'nopass'].includes(encryption) ? encryption : 'WPA'

  // 构建WiFi QR码
  let qrContent = `WIFI:T:${validEncryption};S:${escapedSsid}`

  // 只有在有密码时才添加密码
  if (escapedPassword) {
    qrContent += `;P:${escapedPassword}`
  }

  // 添加隐藏网络标志（可选）
  if (hidden) {
    qrContent += ';H:true'
  }

  // 最后必须以;;结尾
  qrContent += ';;'

  return qrContent
}

/**
 * 对WiFi字符串进行转义
 * WiFi QR码格式要求对特殊字符进行转义
 * @param {string} str
 * @returns {string}
 */
function escapeWifiString(str) {
  if (!str) return ''

  return str
    .replace(/\\/g, '\\\\')  // \ 需要转义为 \\
    .replace(/;/g, '\\;')    // ; 需要转义为 \;
    .replace(/"/g, '\\"')    // " 需要转义为 \"
    .replace(/:/g, '\\:')    // : 需要转义为 \:
    .replace(/,/g, '\\,')    // , 需要转义为 \,
}

/**
 * 验证WiFi信息
 * @param {object} wifiInfo
 * @returns {object} { valid: boolean, error: string }
 */
export function validateWifiInfo(wifiInfo) {
  if (!wifiInfo) {
    return { valid: false, error: 'WiFi信息不能为空' }
  }

  if (!wifiInfo.ssid) {
    return { valid: false, error: 'SSID不能为空' }
  }

  if (wifiInfo.ssid.length > 32) {
    return { valid: false, error: 'SSID长度不能超过32个字符' }
  }

  if (wifiInfo.password && wifiInfo.password.length > 63) {
    return { valid: false, error: '密码长度不能超过63个字符' }
  }

  return { valid: true }
}

/**
 * 获取推荐的WiFi QR码生成参数
 * @param {object} wifiInfo
 * @returns {object}
 */
export function getWifiQrOptions(wifiInfo) {
  const encryption = wifiInfo.encryption || 'WPA'
  const hidden = wifiInfo.hidden || false

  return {
    ssid: wifiInfo.ssid,
    password: wifiInfo.password || '',
    encryption,
    hidden
  }
}

export default {
  generateWifiQrContent,
  validateWifiInfo,
  getWifiQrOptions,
  escapeWifiString
}
