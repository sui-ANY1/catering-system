<template>
  <div class="menu-page">
    <header class="menu-header">
      <div class="brand">🍽️ 扫码点餐</div>
      <div class="header-right">
        <span v-if="currentTable" class="table-badge">
          <el-tag type="warning" effect="dark">桌号：{{ currentTable.tableNo }}</el-tag>
        </span>
        <el-button size="small" @click="goAdmin">管理端</el-button>
      </div>
    </header>

    <!-- 未选桌台：模拟扫码选择桌台 -->
    <div v-if="!tableId" class="table-picker">
      <el-card class="picker-card">
        <div class="picker-icon">📱</div>
        <h3>请选择就餐桌台</h3>
        <p class="muted">模拟扫码进入，选择一张空闲桌台开始点餐</p>
        <div class="picker-actions">
          <el-select v-model="pickedTable" placeholder="选择桌台" style="width: 260px">
            <el-option
              v-for="t in idleTables"
              :key="t.id"
              :label="`${t.tableNo}（${t.capacity}人）`"
              :value="t.id"
            />
          </el-select>
          <el-button type="primary" :disabled="!pickedTable" @click="enterTable">
            进入点餐
          </el-button>
        </div>
        <p class="muted" v-if="!idleTables.length">当前没有空闲桌台，请到管理端先将桌台设为「空闲」</p>
      </el-card>
    </div>

    <!-- 点餐主体 -->
    <div v-else class="menu-body">
      <aside class="category-nav">
        <div
          class="cat-item"
          :class="{ active: activeCat === null }"
          @click="activeCat = null"
        >
          全部
        </div>
        <div
          v-for="c in categories"
          :key="c.id"
          class="cat-item"
          :class="{ active: activeCat === c.id }"
          @click="activeCat = c.id"
        >
          {{ c.name }}
        </div>
      </aside>

      <main class="dish-grid">
        <el-card v-for="d in filteredDishes" :key="d.id" class="dish-card" shadow="hover">
          <div class="dish-img">
            <el-image v-if="d.image" :src="d.image" fit="cover" />
            <div v-else class="dish-img-empty">🍜</div>
          </div>
          <div class="dish-info">
            <div class="dish-name">{{ d.name }}</div>
            <div class="dish-desc">{{ d.description || '暂无描述' }}</div>
            <div class="dish-bottom">
              <span class="dish-price">¥ {{ Number(d.price).toFixed(2) }}</span>
              <div v-if="getQty(d.id) > 0" class="stepper">
                <el-button size="small" circle @click="decrease(d.id)">
                  <el-icon><Minus /></el-icon>
                </el-button>
                <span class="qty">{{ getQty(d.id) }}</span>
                <el-button size="small" circle type="primary" @click="increase(d.id)">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>
              <el-button v-else size="small" type="primary" @click="increase(d.id)">
                加入
              </el-button>
            </div>
          </div>
        </el-card>
        <el-empty
          v-if="!filteredDishes.length"
          description="该分类暂无上架菜品"
          style="grid-column: 1 / -1"
        />
      </main>

      <aside class="cart-panel">
        <div class="cart-header">购物车</div>
        <div class="cart-list">
          <div v-for="item in cart" :key="item.dishId" class="cart-item">
            <div class="cart-item-left">
              <div class="cart-item-name">{{ item.name }}</div>
              <div class="cart-item-controls">
                <el-button size="small" circle @click="decrease(item.dishId)">
                  <el-icon><Minus /></el-icon>
                </el-button>
                <span class="qty">{{ item.quantity }}</span>
                <el-button size="small" circle type="primary" @click="increase(item.dishId)">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="cart-item-right">
              <span class="cart-item-price">
                ¥ {{ (Number(item.price) * (item.quantity || 1)).toFixed(2) }}
              </span>
              <el-button link type="danger" size="small" @click="removeItem(item.dishId)">
                删除
              </el-button>
            </div>
          </div>
          <el-empty v-if="!cart.length" description="购物车为空" :image-size="60" />
        </div>
        <div class="cart-footer">
          <div class="cart-total">
            合计：<span class="total-num">¥ {{ total.toFixed(2) }}</span>
          </div>
          <div class="cart-actions">
            <el-button size="small" :disabled="!cart.length" @click="handleClear">清空</el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="!cart.length"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDishList } from '@/api/dish'
import { getCategoryList } from '@/api/category'
import { getTableList } from '@/api/table'
import { getCart, addToCart, decreaseCart, removeCart, clearCart } from '@/api/cart'
import { submitOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

const tableId = ref(Number(route.query.tableId) || null)
const pickedTable = ref(null)
const tables = ref([])
const dishes = ref([])
const categories = ref([])
const cart = ref([])
const activeCat = ref(null)
const submitting = ref(false)

const idleTables = computed(() => tables.value.filter((t) => t.status === 0))
const currentTable = computed(() => tables.value.find((t) => t.id === tableId.value))

const filteredDishes = computed(() => {
  const arr = dishes.value.filter((d) => d.status === 1)
  if (activeCat.value === null) return arr
  return arr.filter((d) => d.categoryId === activeCat.value)
})

const total = computed(() =>
  cart.value.reduce((s, i) => s + Number(i.price || 0) * (i.quantity || 1), 0)
)

const getQty = (dishId) => {
  const item = cart.value.find((i) => i.dishId === dishId)
  return item ? item.quantity : 0
}

const loadBase = async () => {
  const [dishRes, catRes, tableRes] = await Promise.all([
    getDishList(),
    getCategoryList(),
    getTableList()
  ])
  dishes.value = dishRes.data || []
  categories.value = catRes.data || []
  tables.value = tableRes.data || []
}

const loadCart = async () => {
  if (!tableId.value) return
  const res = await getCart(tableId.value)
  cart.value = res.data || []
}

const enterTable = async () => {
  tableId.value = pickedTable.value
  router.replace({ query: { tableId: tableId.value } })
  await loadCart()
}

const increase = async (dishId) => {
  try {
    await addToCart(tableId.value, dishId)
    await loadCart()
  } catch (e) {}
}

const decrease = async (dishId) => {
  try {
    await decreaseCart(tableId.value, dishId)
    await loadCart()
  } catch (e) {}
}

const removeItem = async (dishId) => {
  try {
    await removeCart(tableId.value, dishId)
    await loadCart()
  } catch (e) {}
}

const handleClear = () => {
  ElMessageBox.confirm('确认清空购物车吗？', '提示', { type: 'warning' })
    .then(async () => {
      await clearCart(tableId.value)
      await loadCart()
      ElMessage.success('购物车已清空')
    })
    .catch(() => {})
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await submitOrder(tableId.value)
    ElMessageBox.alert(
      `下单成功！订单号：${res.data}`,
      '下单成功',
      { type: 'success', confirmButtonText: '好的' }
    )
    await loadCart()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

const goAdmin = () => router.push('/dashboard')

onMounted(async () => {
  try {
    await loadBase()
    if (tableId.value) await loadCart()
  } catch (e) {
    // 错误已在拦截器提示
  }
})
</script>

<style scoped>
.menu-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.menu-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.brand {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 选桌台 */
.table-picker {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.picker-card {
  width: 460px;
  text-align: center;
  padding: 20px;
}

.picker-icon {
  font-size: 48px;
}

.picker-card h3 {
  margin: 8px 0;
}

.picker-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin: 16px 0;
}

.muted {
  color: #909399;
  font-size: 13px;
}

/* 主体三栏 */
.menu-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.category-nav {
  width: 160px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  padding: 8px 0;
  flex-shrink: 0;
}

.cat-item {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.cat-item.active {
  color: #409eff;
  font-weight: 600;
  background: #ecf5ff;
  border-right: 3px solid #409eff;
}

.dish-grid {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  align-content: start;
}

.dish-card :deep(.el-card__body) {
  padding: 0;
}

.dish-img {
  height: 140px;
  background: #f0f2f5;
}

.dish-img .el-image {
  width: 100%;
  height: 100%;
}

.dish-img-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.dish-info {
  padding: 12px;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.dish-desc {
  font-size: 12px;
  color: #909399;
  margin: 6px 0;
  min-height: 32px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 32px;
}

.dish-price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 16px;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.qty {
  min-width: 20px;
  text-align: center;
  font-weight: 600;
}

/* 购物车 */
.cart-panel {
  width: 300px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.cart-header {
  padding: 14px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e4e7ed;
}

.cart-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
  gap: 8px;
}

.cart-item-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
}

.cart-item-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.cart-item-right {
  text-align: right;
}

.cart-item-price {
  color: #f56c6c;
  font-weight: 600;
  display: block;
  margin-bottom: 4px;
}

.cart-footer {
  border-top: 1px solid #e4e7ed;
  padding: 14px 16px;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin-bottom: 12px;
}

.total-num {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 700;
}

.cart-actions {
  display: flex;
  gap: 8px;
}

.cart-actions .el-button {
  flex: 1;
}
</style>
