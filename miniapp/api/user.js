import request from '@/utils/request'

export const wechatLogin = (code) => {
  return request.post('/user/auth/wechat-mini', { code })
}

export const registerBind = (data) => {
  return request.post('/user/register-bind', data)
}

export const getUserInfo = () => {
  return request.get('/user/info')
}

export const updateUserInfo = (data) => {
  return request.put('/user/info', data)
}

export const sendSmsCode = (phone) => {
  return request.post('/user/send-sms', { phone })
}
