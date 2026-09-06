import request from './request'

export const getTableList = () => request.get('/table')
export const getTable = (id) => request.get(`/table/${id}`)
export const addTable = (data) => request.post('/table', data)
export const updateTable = (data) => request.put('/table', data)
export const deleteTable = (id) => request.delete(`/table/${id}`)
