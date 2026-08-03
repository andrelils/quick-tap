import request from '@/utils/request'

export const wechatLogin = (code) => {
  return request.post('/user/auth/wechat-mini', { code })
}

// 获取推荐人列表（超管/管理员的 user_code），注册页自动绑定用
export const getReferrerList = () => {
  return request.get('/user/referrer/list')
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
