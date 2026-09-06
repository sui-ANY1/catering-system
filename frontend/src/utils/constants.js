// 前后端约定的状态字典
// 后端未在文档中显式定义部分枚举，此处按餐饮业务惯例约定，可直接调整

// 桌台状态 dining_table.status
export const TABLE_STATUS = {
  0: { text: '空闲', type: 'success' },
  1: { text: '占用', type: 'warning' },
  2: { text: '维修', type: 'danger' }
}

// 菜品状态 dish.status
export const DISH_STATUS = {
  0: { text: '下架', type: 'info' },
  1: { text: '上架', type: 'success' }
}

// 菜品是否推荐 dish.isRecommend
export const DISH_RECOMMEND = {
  0: { text: '否', type: 'info' },
  1: { text: '是', type: 'warning' }
}

// 分类状态 dish_category.status
export const CATEGORY_STATUS = {
  0: { text: '停用', type: 'info' },
  1: { text: '启用', type: 'success' }
}

// 区域状态 table_area.status
export const AREA_STATUS = {
  0: { text: '停用', type: 'info' },
  1: { text: '启用', type: 'success' }
}

// 订单状态 orders.orderStatus（下单接口默认置为 1）
export const ORDER_STATUS = {
  1: { text: '待接单', type: 'warning' },
  2: { text: '制作中', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

// 支付状态 orders.payStatus
export const PAY_STATUS = {
  0: { text: '未支付', type: 'warning' },
  1: { text: '已支付', type: 'success' }
}

// 支付方式 orders.payType
export const PAY_TYPE = {
  1: '微信',
  2: '支付宝',
  3: '现金',
  4: '会员余额'
}

// 会员等级 member.memberLevel
export const MEMBER_LEVEL = {
  1: '普通会员',
  2: '银卡会员',
  3: '金卡会员',
  4: '钻石会员'
}

// 会员状态 member.status
export const MEMBER_STATUS = {
  0: { text: '禁用', type: 'info' },
  1: { text: '正常', type: 'success' }
}

// 系统用户状态 sys_user.status
export const USER_STATUS = {
  0: { text: '禁用', type: 'info' },
  1: { text: '启用', type: 'success' }
}
