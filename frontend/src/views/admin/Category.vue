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
        <el-button type="primary" :icon="Plus" @click="openAdd">新增分类</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" min-width="140" />
        <el-table-column label="上级分类" width="140">
          <template #default="{ row }">
            {{ row.parentId ? parentMap[row.parentId] || row.parentId : '顶级分类' }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="CATEGORY_STATUS[row.status]?.type">
              {{ CATEGORY_STATUS[row.status]?.text || row.status }}
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
      :title="form.id ? '编辑分类' : '新增分类'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" placeholder="不选则为顶级分类" style="width: 100%">
            <el-option label="顶级分类" :value="0" />
            <el-option
              v-for="c in parentOptions"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
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
import {
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'
import { CATEGORY_STATUS } from '@/utils/constants'

const list = ref([])
const loading = ref(false)
const query = ref('')

const parentMap = computed(() => {
  const m = {}
  list.value.forEach((c) => (m[c.id] = c.name))
  return m
})

const filteredList = computed(() => {
  if (!query.value) return list.value
  return list.value.filter((c) => (c.name || '').includes(query.value))
})

const load = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
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

const defaultForm = () => ({
  id: null,
  name: '',
  parentId: 0,
  sort: 0,
  status: 1
})
const form = reactive(defaultForm())

// 上级分类候选：排除自身，避免循环
const parentOptions = computed(() =>
  list.value.filter((c) => c.id !== form.id)
)

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const openAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    parentId: row.parentId || 0,
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
      const payload = {
        name: form.name,
        parentId: form.parentId || 0,
        sort: form.sort,
        status: form.status
      }
      if (form.id) {
        await updateCategory({ ...payload, id: form.id })
        ElMessage.success('修改成功')
      } else {
        await addCategory(payload)
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
  ElMessageBox.confirm(`确认删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>
