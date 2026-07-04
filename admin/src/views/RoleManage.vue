<template>
  <div class="role-manage-page">
    <div class="page-header">
      <h2><TagOutlined /> 角色管理</h2>
      <p class="page-desc">管理系统中可用的角色（如 admin、user、viewer）</p>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-space>
        <a-button type="primary" @click="openAdd">
          <PlusOutlined /> 新增角色
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
          <template v-if="column.key === 'name'">
            <a-tag :color="roleColor(record.name)">{{ record.name }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm title="确定删除此角色？" @confirm="doDelete(record.id)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      title="新增角色"
      @ok="handleSave"
      :confirmLoading="saving"
      width="400px"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="form.name" placeholder="如：admin / editor / viewer" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" placeholder="角色说明" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TagOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { api } from '../stores/auth'

const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const dataSource = ref<any[]>([])

const form = ref({ name: '', description: '' })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '角色名', key: 'name', width: 120 },
  { title: '描述', dataIndex: 'description', width: 300 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
]

function roleColor(role: string) {
  const map: Record<string, string> = { admin: 'red', user: 'blue', viewer: 'default' }
  return map[role] || 'geekblue'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    dataSource.value = await api<any[]>('/api/admin/roles')
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = { name: '', description: '' }
}

function openAdd() {
  resetForm()
  modalVisible.value = true
}

async function handleSave() {
  if (!form.value.name) return
  saving.value = true
  try {
    await api('/api/admin/roles', {
      method: 'POST',
      body: JSON.stringify(form.value),
    })
    modalVisible.value = false
    await fetchData()
  } catch { /* ignore */ } finally {
    saving.value = false
  }
}

async function doDelete(id: number) {
  try {
    await api(`/api/admin/roles/${id}`, { method: 'DELETE' })
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
