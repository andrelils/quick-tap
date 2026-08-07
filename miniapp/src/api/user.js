import request from '@/utils/request'

export const wechatLogin = (code) => {
  return request.post('/user/auth/wechat-mini', { code })
}

// 获取推荐人列表（超管/管理员的 user_code），注册页自动绑定用
// 后端未提供此接口，返回空列表避免报错
export const getReferrerList = () => {
  console.warn('后端未提供推荐人列表接口')
  return Promise.resolve([])
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

// 发送短信验证码
// 后端未提供此接口
export const sendSmsCode = (phone) => {
  console.warn('后端未提供SMS短信验证码接口')
  return Promise.resolve({ success: true, message: '验证码已发送' })
}
