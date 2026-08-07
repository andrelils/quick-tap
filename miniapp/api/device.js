import request from '@/utils/request'

export const scanDevice = (deviceId) => {
  return request.post('/device/scan', { deviceId })
}

export const bindDevice = (deviceId, userId) => {
  return request.post('/device/bind', { deviceId, userId })
}

export const getDeviceInfo = (deviceNo) => {
  return request.get(`/device/info/${deviceNo}`)
}

export const checkDeviceInfo = () => {
  return request.get('/device/check')
}
