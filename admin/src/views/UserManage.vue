<template>
  <div class="user-manage-page">
    <div class="page-header">
      <h2><UserOutlined /> 用户管理</h2>
      <p class="page-desc">管理系统用户及其角色分配</p>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-space>
        <a-button type="primary" @click="openAdd">
          <PlusOutlined /> 新增用户
        </a-button>
        <a-button @click="fetchData" :loading="loading">
          <ReloadOutlined /> 刷新
        </a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :dataSource="dataSource"
        :loading="loading"
        :pagination="{ pageSize: 15, showTotal: (t: number) => `共 ${t} 条` }"
        rowKey="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'role'">
            <a-tag :color="roleColor(record.role)">{{ record.role }}</a-tag>
          </template>
          <template v-else-if="column.key === 'systemId'">
            <span v-if="record.systemId">{{ getSystemName(record.systemId) }}</span>
            <span v-else style="color: #ccc">全部系统</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm title="确定删除此用户？" @confirm="doDelete(record.id)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      title="新增用户"
      @ok="handleSave"
      :confirmLoading="saving"
      width="480px"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item label="用户名" required>
          <a-input v-model:value="form.username" placeholder="登录用户名" />
        </a-form-item>
        <a-form-item label="密码" required>
          <a-input-password v-model:value="form.password" placeholder="登录密码" />
        </a-form-item>
        <a-form-item label="显示名称">
          <a-input v-model:value="form.displayName" placeholder="留空则使用用户名" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="form.role" placeholder="选择角色">
            <a-select-option v-for="r in roleList" :key="r.name" :value="r.name">
              {{ r.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属系统">
          <a-select v-model:value="form.systemId" placeholder="留空表示所有系统" allowClear>
            <a-select-option v-for="s in systemList" :key="s.id" :value="s.id">
              {{ s.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { UserOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { api } from '../stores/auth'

const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const dataSource = ref<any[]>([])
const roleList = ref<any[]>([])
const systemList = ref<any[]>([])

const form = ref({ username: '', password: '', displayName: '', role: '', systemId: '' })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 120, ellipsis: true },
  { title: '用户名', dataIndex: 'username', width: 130 },
  { title: '显示名称', dataIndex: 'displayName', width: 140 },
  { title: '角色', key: 'role', width: 100 },
  { title: '所属系统', key: 'systemId', width: 130 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
]

function getSystemName(sid: string) {
  const s = systemList.value.find((s: any) => s.id === sid)
  return s ? s.name : sid
}

function roleColor(role: string) {
  const map: Record<string, string> = { admin: 'red', user: 'blue', viewer: 'default' }
  return map[role] || 'default'
}

onMounted(async () => {
  await Promise.all([fetchData(), fetchRoles(), fetchSystems()])
})

async function fetchData() {
  loading.value = true
  try {
    dataSource.value = await api<any[]>('/api/admin/users')
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    roleList.value = await api<any[]>('/api/admin/roles')
  } catch {
    roleList.value = []
  }
}

async function fetchSystems() {
  try {
    systemList.value = await api<any[]>('/api/admin/systems')
  } catch {
    systemList.value = []
  }
}

function resetForm() {
  form.value = { username: '', password: '', displayName: '', role: '', systemId: '' }
}

function openAdd() {
  resetForm()
  modalVisible.value = true
}

async function handleSave() {
  if (!form.value.username || !form.value.password) return
  saving.value = true
  try {
    await api('/api/admin/users', {
      method: 'POST',
      body: JSON.stringify(form.value),
    })
    modalVisible.value = false
    await fetchData()
  } catch { /* ignore */ } finally {
    saving.value = false
  }
}

async function doDelete(id: string) {
  try {
    await api(`/api/admin/users/${id}`, { method: 'DELETE' })
    dataSource.value = dataSource.value.filter(r => r.id !== id)
  } catch { /* ignore */ }
}
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; margin: 0; display: flex; align-items: center; gap: 8px; }
.page-desc { color: #999; font-size: 13px; margin: 6px 0 0; }
.filter-card { margin-bottom: 16px; }
.filter-card :deep(.ant-card-body) { padding: 12px 16px; }
</style>
