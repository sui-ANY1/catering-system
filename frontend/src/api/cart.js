import request from './request'

// 加入购物车（同菜品数量 +1）
export const addToCart = (tableId, dishId) =>
  request.post('/cart/add', null, { params: { tableId, dishId } })

// 减少数量（减到 0 则移除）
export const decreaseCart = (tableId, dishId) =>
  request.post('/cart/decrease', null, { params: { tableId, dishId } })

// 移除单个菜品
export const removeCart = (tableId, dishId) =>
  request.delete('/cart/remove', { params: { tableId, dishId } })

// 查看购物车
export const getCart = (tableId) =>
  request.get('/cart/list', { params: { tableId } })

// 清空购物车
export const clearCart = (tableId) =>
  request.delete('/cart/clear', { params: { tableId } })
