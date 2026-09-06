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
          <el-select
            v-model="filterCategory"
            placeholder="按分类筛选"
            clearable
            style="width: 160px"
          >
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增菜品</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              fit="cover"
              style="width: 48px; height: 48px; border-radius: 6px"
              :preview-src-list="[row.image]"
              preview-teleported
            />
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            {{ categoryMap[row.categoryId] || row.categoryId }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥ {{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="DISH_STATUS[row.status]?.type">
              {{ DISH_STATUS[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-tag :type="DISH_RECOMMEND[row.isRecommend]?.type" effect="plain">
              {{ DISH_RECOMMEND[row.isRecommend]?.text || row.isRecommend }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑菜品' : '新增菜品'"
      width="560px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="form.image" placeholder="图片 URL（可选）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="推荐">
          <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
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
import { getDishList, addDish, updateDish, deleteDish } from '@/api/dish'
import { getCategoryList } from '@/api/category'
import { DISH_STATUS, DISH_RECOMMEND } from '@/utils/constants'

const list = ref([])
const categories = ref([])
const loading = ref(false)
const query = ref('')
const filterCategory = ref(null)

const categoryMap = computed(() => {
  const m = {}
  categories.value.forEach((c) => (m[c.id] = c.name))
  return m
})

const filteredList = computed(() => {
  let arr = list.value
  if (query.value) {
    arr = arr.filter((d) => (d.name || '').includes(query.value))
  }
  if (filterCategory.value !== null && filterCategory.value !== '') {
    arr = arr.filter((d) => d.categoryId === filterCategory.value)
  }
  return arr
})

const load = async () => {
  loading.value = true
  try {
    const [dishRes, catRes] = await Promise.all([
      getDishList(),
      getCategoryList()
    ])
    list.value = dishRes.data || []
    categories.value = catRes.data || []
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
  categoryId: null,
  price: 0,
  image: '',
  description: '',
  status: 1,
  stock: 0,
  isRecommend: 0,
  sort: 0
})
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const openAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId,
    price: Number(row.price),
    image: row.image || '',
    description: row.description || '',
    status: row.status,
    stock: row.stock,
    isRecommend: row.isRecommend,
    sort: row.sort
  })
  dialogVisible.value = true
}

const buildPayload = () => ({
  name: form.name,
  categoryId: form.categoryId,
  price: form.price,
  image: form.image || null,
  description: form.description || null,
  status: form.status,
  stock: form.stock,
  isRecommend: form.isRecommend,
  sort: form.sort
})

const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await updateDish({ ...buildPayload(), id: form.id })
        ElMessage.success('修改成功')
      } else {
        await addDish(buildPayload())
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      load()
    } catch (e) {
      // 错误已在拦截器提示
    } finally {
      saving.value = false
    }
  })
}

const toggleStatus = async (row) => {
  try {
    await updateDish({ id: row.id, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('操作成功')
    load()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除菜品「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteDish(row.id)
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
