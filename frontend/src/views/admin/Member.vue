<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-input
            v-model="query"
            placeholder="按昵称/手机号搜索"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增会员</el-button>
      </div>

      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="余额" width="110">
          <template #default="{ row }">¥ {{ Number(row.balance || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="累计消费" width="120">
          <template #default="{ row }">¥ {{ Number(row.totalConsume || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="等级" width="110">
          <template #default="{ row }">{{ MEMBER_LEVEL[row.memberLevel] || row.memberLevel }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="MEMBER_STATUS[row.status]?.type">
              {{ MEMBER_STATUS[row.status]?.text || row.status }}
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
      :title="form.id ? '编辑会员' : '新增会员'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="余额">
          <el-input-number v-model="form.balance" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="form.points" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="累计消费">
          <el-input-number
            v-model="form.totalConsume"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.memberLevel" style="width: 100%">
            <el-option
              v-for="(v, k) in MEMBER_LEVEL"
              :key="k"
              :label="v"
              :value="Number(k)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
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
import { getMemberList, addMember, updateMember, deleteMember } from '@/api/member'
import { MEMBER_LEVEL, MEMBER_STATUS } from '@/utils/constants'

const list = ref([])
const loading = ref(false)
const query = ref('')

const filteredList = computed(() => {
  if (!query.value) return list.value
  const q = query.value
  return list.value.filter(
    (m) => (m.nickname || '').includes(q) || (m.phone || '').includes(q)
  )
})

const load = async () => {
  loading.value = true
  try {
    const res = await getMemberList()
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
  nickname: '',
  phone: '',
  balance: 0,
  points: 0,
  totalConsume: 0,
  memberLevel: 1,
  status: 1
})
const form = reactive(defaultForm())

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const openAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    nickname: row.nickname,
    phone: row.phone,
    balance: Number(row.balance || 0),
    points: row.points,
    totalConsume: Number(row.totalConsume || 0),
    memberLevel: row.memberLevel,
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
        nickname: form.nickname,
        phone: form.phone,
        balance: form.balance,
        points: form.points,
        totalConsume: form.totalConsume,
        memberLevel: form.memberLevel,
        status: form.status
      }
      if (form.id) {
        await updateMember({ ...payload, id: form.id })
        ElMessage.success('修改成功')
      } else {
        await addMember(payload)
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
  ElMessageBox.confirm(`确认删除会员「${row.nickname}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteMember(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>
