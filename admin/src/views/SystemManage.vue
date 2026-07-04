<template>
  <div class="page">
    <div class="admin-toolbar">
      <h3>系统列表</h3>
      <a-space>
        <a-input-search v-model:value="keyword" placeholder="搜索名称或编码..." allow-clear style="width: 220px" @search="load" />
        <a-button type="primary" @click="onAdd">
          <template #icon><PlusOutlined /></template>
          新增系统
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="admin-card">
      <a-table :columns="columns" :dataSource="list" :pagination="false" :loading="loading" rowKey="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'orange'">
              {{ record.status === 'active' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="onEdit(record)">编辑</a>
              <a-popconfirm title="删除系统将同时删除其下所有业务、预设和列定义" @confirm="onDelete(record.id)">
                <a style="color: #ff4d4f">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="visible" :title="editing ? '编辑系统' : '新增系统'" @ok="save" width="450px">
      <a-form layout="vertical">
        <a-form-item label="系统名称" required>
          <a-input v-model:value="form.name" placeholder="如：CRM客户管理系统" />
        </a-form-item>
        <a-form-item label="系统编码" required>
          <a-input v-model:value="form.code" placeholder="如：crm" :disabled="!!editing" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" placeholder="系统描述" :rows="2" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio value="active">启用</a-radio>
            <a-radio value="inactive">停用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { api } from '../stores/auth'

const loading = ref(false)
const list = ref<any[]>([])
const keyword = ref('')
const visible = ref(false)
const editing = ref<any>(null)
const form = ref({ name: '', code: '', description: '', status: 'active' })

const columns = [
  { title: '名称', dataIndex: 'name' },
  { title: '编码', dataIndex: 'code', width: 120 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 130 },
]

async function load() {
  loading.value = true
  try {
    let data = await api<any[]>('/api/admin/systems')
    if (keyword.value) {
      const q = keyword.value.toLowerCase()
      data = data.filter(s => s.name.toLowerCase().includes(q) || s.code.toLowerCase().includes(q))
    }
    list.value = data
  } catch (e: any) { message.error('加载失败: ' + e.message) }
  finally { loading.value = false }
}

function onAdd() {
  editing.value = null
  form.value = { name: '', code: '', description: '', status: 'active' }
  visible.value = true
}
function onEdit(record: any) {
  editing.value = record
  form.value = { name: record.name, code: record.code, description: record.description || '', status: record.status }
  visible.value = true
}
async function save() {
  if (!form.value.name || !form.value.code) { message.warning('名称和编码为必填项'); return }
  try {
    if (editing.value) {
      await api(`/api/admin/systems/${editing.value.id}`, { method: 'PUT', body: JSON.stringify(form.value) })
      message.success('已更新')
    } else {
      await api('/api/admin/systems', { method: 'POST', body: JSON.stringify(form.value) })
      message.success('已创建')
    }
    visible.value = false
    await load()
  } catch (e: any) { message.error(e.message) }
}
async function onDelete(id: string) {
  try {
    await api(`/api/admin/systems/${id}`, { method: 'DELETE' })
    message.success('已删除')
    await load()
  } catch (e: any) { message.error(e.message) }
}

onMounted(load)
</script>
