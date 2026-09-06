<template>
  <div class="page-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="密码会在后端自动进行 BCrypt 加密存储；编辑时留空则保持不变。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-input
            v-model="query"
            placeholder="按用户名/姓名搜索"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增用户</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">{{ ROLE_MAP[row.roleId] || row.roleId }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="USER_STATUS[row.status]?.type">
              {{ USER_STATUS[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
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
      :title="form.id ? '编辑用户' : '新增用户'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" style="width: 100%">
            <el-option
              v-for="(v, k) in ROLE_MAP"
              :key="k"
              :label="v"
              :value="Number(k)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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
import { getUserList, addUser, updateUser, deleteUser } from '@/api/user'
import { USER_STATUS } from '@/utils/constants'

// 后端未提供角色表，此处为约定映射，可按需调整
const ROLE_MAP = {
  1: '管理员',
  2: '收银员',
  3: '厨师'
}

const list = ref([])
const loading = ref(false)
const query = ref('')

const filteredList = computed(() => {
  if (!query.value) return list.value
  const q = query.value
  return list.value.filter(
    (u) => (u.username || '').includes(q) || (u.realName || '').includes(q)
  )
})

const fmt = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

const load = async () => {
  loading.value = true
  try {
    const res = await getUserList()
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
  username: '',
  password: '',
  realName: '',
  phone: '',
  roleId: 2,
  status: 1
})
const form = reactive(defaultForm())

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const openAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName || '',
    phone: row.phone || '',
    roleId: row.roleId,
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
        username: form.username,
        realName: form.realName,
        phone: form.phone,
        roleId: form.roleId,
        status: form.status
      }
      if (form.id) {
        await updateUser({ ...payload, id: form.id })
        ElMessage.success('修改成功')
      } else {
        await addUser({ ...payload, password: form.password })
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
  ElMessageBox.confirm(`确认删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>
