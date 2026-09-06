import request from './request'

// 登录：返回 Result，data 为 token 字符串
export const login = (data) => request.post('/auth/login', data)

export const logout = () => request.get('/auth/logout')
