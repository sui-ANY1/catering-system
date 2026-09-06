import request from './request'

export const getDishList = () => request.get('/dish')
export const getDish = (id) => request.get(`/dish/${id}`)
export const addDish = (data) => request.post('/dish', data)
export const updateDish = (data) => request.put('/dish', data)
export const deleteDish = (id) => request.delete(`/dish/${id}`)
