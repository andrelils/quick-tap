// 获取API基础URL
// dev 环境（NODE_ENV=development）走本地后端；生产环境（uni build）走线上服务
const BASE_URL = process.env.NODE_ENV === 'production'
  ? 'http://154.8.138.48:3000/api/miniapp'
  : 'http://localhost:8222/api/miniapp'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const url = BASE_URL + options.url
    const token = uni.getStorageSync('token') || ''

    const header = {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.header
    }

    uni.request({
      url: url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data
          // 后端成功 code 为 "1000"；兼容 0 / "0" / 1000
          const code = data && data.code
          if (code === 0 || code === '0' || code === '1000' || code === 1000) {
            resolve(data.data)
          } else {
            uni.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(data)
          }
        } else {
          reject(res)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export default {
  get: (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data }),
  put: (url, data) => request({ url, method: 'PUT', data }),
  delete: (url, data) => request({ url, method: 'DELETE', data })
}
