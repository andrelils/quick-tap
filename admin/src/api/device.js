import request from '@/utils/request'
import { getSystemSettings } from '@/api/system'

// 设备管理（统一设备：二维码 + NFC 成套）
export const getDeviceList = (params) => {
  return request.get('/device/list', { params })
}

export const getDeviceDetail = (id) => {
  return request.get(`/device/${id}`)
}

export const createDevice = (data) => {
  return request.post('/device', data)
}

export const batchCreateDevice = (data) => {
  return request.post('/device/batch', data)
}

export const updateDevice = (id, data) => {
  return request.put(`/device/${id}`, data)
}

export const deleteDevice = (id) => {
  return request.delete(`/device/${id}`)
}

export const batchEnableDevices = (deviceIds) => {
  return request.put('/device/batch/enable', deviceIds)
}

export const batchDisableDevices = (deviceIds) => {
  return request.put('/device/batch/disable', deviceIds)
}

export const batchDeleteDevices = (deviceIds) => {
  return request.delete('/device/batch', { data: deviceIds })
}

// 二维码生成与查询
export const generateQrCode = (data) => {
  return request.post('/admin/qrcode/generate', data)
}

export const batchGenerateNfcQr = (data) => {
  return request.post('/admin/qrcode/batch', data)
}

export const getQrCodeHistory = (params) => {
  return request.get('/admin/qrcode/list', { params })
}

export const batchGenerateQrCodes = (data) => {
  return request.post('/admin/qrcode/batch', data)
}

export const deleteQrCode = (id) => {
  return request.delete(`/admin/qrcode/${id}`)
}

export const bindQrCodeToMerchant = (data) => {
  return request.post('/admin/qrcode/bind', data)
}

// 从系统配置获取设备URL前缀（替代原有的Mock实现）
export const getQrCodeConfig = () => {
  return getSystemSettings().then(res => ({
    deviceUrl: res?.deviceUrl || '',
    qrcodeUrl: res?.qrcodeUrl || '',
    size: 240,
    margin: 2,
    errorCorrectionLevel: 'M',
    downloadFormat: 'png'
  })).catch(() => ({
    deviceUrl: '',
    qrcodeUrl: '',
    size: 240,
    margin: 2,
    errorCorrectionLevel: 'M',
    downloadFormat: 'png'
  }))
}
