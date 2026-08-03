import request from '@/utils/request'

export const login = (data) => {
  return request.post('/admin/auth/login', data)
}

export const getUserInfo = () => {
  return request.get('/admin/user/info')
}

export const logout = () => {
  return request.post('/admin/auth/logout')
}

export const updatePassword = (data) => {
  return request.put('/admin/user/password', data)
}

export const updateUserInfo = (data) => {
  return request.put('/admin/user/info', data)
}
