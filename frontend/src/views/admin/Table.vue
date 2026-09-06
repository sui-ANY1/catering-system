<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-input
            v-model="query"
            placeholder="按桌号搜索"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
          <el-select v-model="filterArea" placeholder="按区域筛选" clearable style="width: 160px">
            <el-option v-for="a in areas" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增桌台</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="tableNo" label="桌号" width="120" />
        <el-table-column label="区域" width="120">
          <template #default="{ row }">{{ areaMap[row.areaId] || row.areaId }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="人数" width="90" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" @change="(v) => changeStatus(row, v)">
              <el-option :value="0" label="空闲" />
              <el-option :value="1" label="占用" />
              <el-option :value="2" label="维修" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="二维码" width="120">
          <template #default="{ row }">
            <el-link v-if="row.qrCodeUrl" type="primary" :href="row.qrCodeUrl" target="_blank">
              查看
            </el-link>
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑桌台' : '新增桌台'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="桌号" prop="tableNo">
          <el-input v-model="form.tableNo" placeholder="如 A01" />
        </el-form-item>
        <el-form-item label="区域" prop="areaId">
          <el-select v-model="form.areaId" placeholder="选择区域" style="width: 100%">
            <el-option v-for="a in areas" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="form.capacity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">空闲</el-radio>
            <el-radio :value="1">占用</el-radio>
            <el-radio :value="2">维修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="二维码">
          <el-input v-model="form.qrCodeUrl" placeholder="二维码图片地址（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getTableList, addTable, updateTable, deleteTable } from '@/api/table'
import { getAreaList } from '@/api/area'

const list = ref([])
const areas = ref([])
const loading = ref(false)
const query = ref('')
const filterArea = ref(null)

const areaMap = computed(() => {
  const m = {}
  areas.value.forEach((a) => (m[a.id] = a.name))
  return m
})

const filteredList = computed(() => {
  let arr = list.value
  if (query.value) {
    arr = arr.filter((t) => (t.tableNo || '').includes(query.value))
  }
  if (filterArea.value !== null && filterArea.value !== '') {
    arr = arr.filter((t) => t.areaId === filterArea.value)
  }
  return arr
})

const load = async () => {
  loading.value = true
  try {
    const [tableRes, areaRes] = await Promise.all([getTableList(), getAreaList()])
    list.value = tableRes.data || []
    areas.value = areaRes.data || []
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}

const formRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)

const defaultForm = () => ({
  id: null,
  tableNo: '',
  areaId: null,
  capacity: 4,
  status: 0,
  qrCodeUrl: ''
})
const form = reactive(defaultForm())

const rules = {
  tableNo: [{ required: true, message: '请输入桌号', trigger: 'blur' }],
  areaId: [{ required: true, message: '请选择区域', trigger: 'change' }]
}

const openAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    tableNo: row.tableNo,
    areaId: row.areaId,
    capacity: row.capacity,
    status: row.status,
    qrCodeUrl: row.qrCodeUrl || ''
  })
  dialogVisible.value = true
}

const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        tableNo: form.tableNo,
        areaId: form.areaId,
        capacity: form.capacity,
        status: form.status,
        qrCodeUrl: form.qrCodeUrl || null
      }
      if (form.id) {
        await updateTable({ ...payload, id: form.id })
        ElMessage.success('修改成功')
      } else {
        await addTable(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      load()
    } catch (e) {
    } finally {
      saving.value = false
    }
  })
}

const changeStatus = async (row, status) => {
  try {
    await updateTable({ id: row.id, status })
    ElMessage.success('状态已更新')
    load()
  } catch (e) {
    load()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除桌台「${row.tableNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteTable(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.muted {
  color: #c0c4cc;
}
</style>
