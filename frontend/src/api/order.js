import request from './request'

export const getOrderList = () => request.get('/order')
export const getOrder = (id) => request.get(`/order/${id}`)
export const addOrder = (data) => request.post('/order', data)
export const updateOrder = (data) => request.put('/order', data)
export const deleteOrder = (id) => request.delete(`/order/${id}`)

// 提交订单：根据桌台购物车生成订单，返回订单号
export const submitOrder = (tableId) =>
  request.post('/order/submit', null, { params: { tableId } })

// 查询订单明细
export const getOrderDetail = (orderId) => request.get(`/orderDetail/${orderId}`)
