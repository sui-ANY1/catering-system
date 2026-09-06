import request from './request'

export const getCategoryList = () => request.get('/category')
export const getCategory = (id) => request.get(`/category/${id}`)
export const addCategory = (data) => request.post('/category', data)
export const updateCategory = (data) => request.put('/category', data)
export const deleteCategory = (id) => request.delete(`/category/${id}`)
