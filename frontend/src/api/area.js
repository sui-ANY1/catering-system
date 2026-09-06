import request from './request'

export const getAreaList = () => request.get('/area')
export const getArea = (id) => request.get(`/area/${id}`)
export const addArea = (data) => request.post('/area', data)
export const updateArea = (data) => request.put('/area', data)
export const deleteArea = (id) => request.delete(`/area/${id}`)
