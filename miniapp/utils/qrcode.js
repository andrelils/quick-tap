/**
 * WiFi二维码生成工具
 * 生成标准的WiFi QR Code格式字符串
 */

/**
 * 生成WiFi二维码值
 * @param {Object} wifiInfo - WiFi信息对象
 * @param {string} wifiInfo.ssid - WiFi网络名称
 * @param {string} wifiInfo.password - WiFi密码
 * @param {string} [wifiInfo.encryption='WPA'] - 加密方式 (WPA, WEP, nopass等)
 * @returns {string} WiFi QR Code格式的字符串
 *
 * 格式说明：WIFI:T:加密类型;S:网络名;P:密码;;
 * 例如：WIFI:T:WPA;S:MyNetwork;P:MyPassword;;
 */
export function generateWiFiQrCodeValue(wifiInfo) {
  if (!wifiInfo || !wifiInfo.ssid || !wifiInfo.password) {
    throw new Error('WiFi信息不完整，需要ssid和password')
  }

  const encryptionType = (wifiInfo.encryption || 'WPA').toUpperCase()
  const ssid = wifiInfo.ssid
  const password = wifiInfo.password

  // WiFi QR Code标准格式：WIFI:T:加密类型;S:网络名;P:密码;;
  return `WIFI:T:${encryptionType};S:${ssid};P:${password};;`
}

/**
 * 验证WiFi信息
 * @param {Object} wifiInfo - WiFi信息对象
 * @returns {boolean} 是否有效
 */
export function validateWiFiInfo(wifiInfo) {
  if (!wifiInfo) return false
  if (!wifiInfo.ssid || typeof wifiInfo.ssid !== 'string') return false
  if (!wifiInfo.password || typeof wifiInfo.password !== 'string') return false
  return true
}

/**
 * 获取加密方式的可读名称
 * @param {string} encryptionType - 加密类型代码
 * @returns {string} 可读的加密方式名称
 */
export function getEncryptionName(encryptionType) {
  const encryptionMap = {
    'WPA': 'WPA/WPA2',
    'WEP': 'WEP',
    'NOPASS': '开放网络',
    'OPEN': '开放网络'
  }
  return encryptionMap[(encryptionType || '').toUpperCase()] || encryptionType || 'WPA'
}
