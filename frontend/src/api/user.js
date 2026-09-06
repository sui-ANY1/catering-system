import request from './request'

export const getUserList = () => request.get('/user')
export const getUser = (id) => request.get(`/user/${id}`)
export const addUser = (data) => request.post('/user', data)
export const updateUser = (data) => request.put('/user', data)
export const deleteUser = (id) => request.delete(`/user/${id}`)
