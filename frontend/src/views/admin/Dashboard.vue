<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="26"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <span class="card-title">今日概览</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="总营业额（已下单金额合计）">
          ¥ {{ totalSales.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="订单总数">{{ orderCount }}</el-descriptions-item>
        <el-descriptions-item label="桌台总数">{{ tableCount }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDishList } from '@/api/dish'
import { getCategoryList } from '@/api/category'
import { getTableList } from '@/api/table'
import { getOrderList } from '@/api/order'
import { getMemberList } from '@/api/member'

const dishCount = ref(0)
const categoryCount = ref(0)
const tableCount = ref(0)
const orderCount = ref(0)
const memberCount = ref(0)
const totalSales = ref(0)

const stats = computed(() => [
  { title: '菜品总数', value: dishCount.value, icon: 'Food', color: '#409eff' },
  { title: '分类总数', value: categoryCount.value, icon: 'Menu', color: '#67c23a' },
  { title: '桌台总数', value: tableCount.value, icon: 'Grid', color: '#e6a23c' },
  { title: '订单总数', value: orderCount.value, icon: 'Document', color: '#f56c6c' }
])

const load = async () => {
  try {
    const [dish, category, table, order, member] = await Promise.all([
      getDishList(),
      getCategoryList(),
      getTableList(),
      getOrderList(),
      getMemberList()
    ])
    dishCount.value = dish.data?.length || 0
    categoryCount.value = category.data?.length || 0
    tableCount.value = table.data?.length || 0
    orderCount.value = order.data?.length || 0
    memberCount.value = member.data?.length || 0

    totalSales.value = (order.data || []).reduce(
      (sum, o) => sum + Number(o.payAmount || 0),
      0
    )
  } catch (e) {
    // 错误已在拦截器提示
  }
}

onMounted(load)
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
}

.stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

.stat-title {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
