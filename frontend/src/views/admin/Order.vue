<template>
  <div class="page-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="提示：后端未提供订单明细接口，此处仅展示订单主表信息；状态流转通过「订单状态」下拉或「结账」按钮完成。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-input
            v-model="query"
            placeholder="按订单号搜索"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
          <el-select v-model="filterStatus" placeholder="按状态筛选" clearable style="width: 160px">
            <el-option
              v-for="(v, k) in ORDER_STATUS"
              :key="k"
              :label="v.text"
              :value="Number(k)"
            />
          </el-select>
        </div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="tableNo" label="桌号" width="90" />
        <el-table-column label="总金额" width="100">
          <template #default="{ row }">¥ {{ Number(row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="实付" width="100">
          <template #default="{ row }">¥ {{ Number(row.payAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="支付方式" width="100">
          <template #default="{ row }">{{ PAY_TYPE[row.payType] || '-' }}</template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="PAY_STATUS[row.payStatus]?.type">
              {{ PAY_STATUS[row.payStatus]?.text || row.payStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="130">
          <template #default="{ row }">
            <el-select
              v-model="row.orderStatus"
              size="small"
              @change="(v) => changeOrderStatus(row, v)"
            >
              <el-option
                v-for="(v, k) in ORDER_STATUS"
                :key="k"
                :label="v.text"
                :value="Number(k)"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.payStatus !== 1"
              link
              type="success"
              @click="handlePay(row)"
            >
              结账
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单明细" width="640px">
      <el-table :data="detailList" border v-loading="detailLoading">
        <el-table-column prop="dishName" label="菜品" min-width="140" />
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥ {{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="110">
          <template #default="{ row }">¥ {{ Number(row.totalPrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="tasteRemark" label="口味备注" min-width="120" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!detailList.length && !detailLoading" description="暂无明细" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOrderList, updateOrder, deleteOrder, getOrderDetail } from '@/api/order'
import { ORDER_STATUS, PAY_STATUS, PAY_TYPE } from '@/utils/constants'

const list = ref([])
const loading = ref(false)
const query = ref('')
const filterStatus = ref(null)
const detailVisible = ref(false)
const detailList = ref([])
const detailLoading = ref(false)

const filteredList = computed(() => {
  let arr = list.value
  if (query.value) {
    arr = arr.filter((o) => (o.orderNo || '').includes(query.value))
  }
  if (filterStatus.value !== null && filterStatus.value !== '') {
    arr = arr.filter((o) => o.orderStatus === filterStatus.value)
  }
  return arr
})

const fmt = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

const load = async () => {
  loading.value = true
  try {
    const res = await getOrderList()
    list.value = res.data || []
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}

const changeOrderStatus = async (row, status) => {
  try {
    await updateOrder({ id: row.id, orderStatus: status })
    ElMessage.success('状态已更新')
    load()
  } catch (e) {
    load()
  }
}

const handlePay = async (row) => {
  try {
    await updateOrder({ id: row.id, payStatus: 1, payType: row.payType || 3 })
    ElMessage.success('结账成功')
    load()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteOrder(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getOrderDetail(row.id)
    detailList.value = res.data || []
  } catch (e) {
  } finally {
    detailLoading.value = false
  }
}

onMounted(load)
</script>
