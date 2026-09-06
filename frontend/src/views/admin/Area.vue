<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-input
            v-model="query"
            placeholder="按名称搜索"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增区域</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="区域名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="AREA_STATUS[row.status]?.type">
              {{ AREA_STATUS[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑区域' : '新增区域'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如 大厅 / 包间" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { getAreaList, addArea, updateArea, deleteArea } from '@/api/area'
import { AREA_STATUS } from '@/utils/constants'

const list = ref([])
const loading = ref(false)
const query = ref('')

const filteredList = computed(() => {
  if (!query.value) return list.value
  return list.value.filter((a) => (a.name || '').includes(query.value))
})

const load = async () => {
  loading.value = true
  try {
    const res = await getAreaList()
    list.value = res.data || []
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}

const formRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)

const defaultForm = () => ({ id: null, name: '', sort: 0, status: 1 })
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入区域名称', trigger: 'blur' }]
}

const openAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = { name: form.name, sort: form.sort, status: form.status }
      if (form.id) {
        await updateArea({ ...payload, id: form.id })
        ElMessage.success('修改成功')
      } else {
        await addArea(payload)
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

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除区域「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteArea(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>
