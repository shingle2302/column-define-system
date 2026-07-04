<template>
  <div class="page">
    <div class="admin-toolbar">
      <h3>业务模块列表</h3>
      <a-space>
        <a-select v-model:value="filterSystem" placeholder="筛选系统" allowClear style="width: 180px" @change="load">
          <a-select-option v-for="s in systems" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
        </a-select>
        <a-button type="primary" @click="onAdd">
          <template #icon><PlusOutlined /></template>
          新增业务
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="admin-card">
      <a-table :columns="columns" :dataSource="list" :pagination="false" :loading="loading" rowKey="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="onEdit(record)">编辑</a>
              <a-popconfirm title="删除业务将同时删除其下所有预设和列定义" @confirm="onDelete(record.id)">
                <a style="color: #ff4d4f">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="visible" :title="editing ? '编辑业务' : '新增业务'" @ok="save" width="450px">
      <a-form layout="vertical">
        <a-form-item label="所属系统" required>
          <a-select v-model:value="form.systemId" placeholder="选择系统">
            <a-select-option v-for="s in systems" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="业务名称" required>
          <a-input v-model:value="form.name" placeholder="如：客户管理" />
        </a-form-item>
        <a-form-item label="业务编码" required>
          <a-input v-model:value="form.code" placeholder="如：customer" :disabled="!!editing" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" placeholder="业务描述" :rows="2" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
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
const systems = ref<any[]>([])
const filterSystem = ref<string | undefined>(undefined)
const visible = ref(false)
const editing = ref<any>(null)
const form = ref({ systemId: '', name: '', code: '', description: '', sortOrder: 0 })

const columns = [
  { title: '名称', dataIndex: 'name' },
  { title: '编码', dataIndex: 'code', width: 120 },
  { title: '所属系统', dataIndex: 'systemName', width: 150 },
  { title: '排序', dataIndex: 'sortOrder', width: 60 },
  { title: '操作', key: 'action', width: 130 },
]

async function load() {
  loading.value = true
  try {
    const params = filterSystem.value ? `?systemId=${filterSystem.value}` : ''
    list.value = await api('/api/admin/businesses' + params)
  } catch (e: any) { message.error('加载失败: ' + e.message) }
  finally { loading.value = false }
}

async function loadSystems() {
  try { systems.value = await api('/api/admin/systems') }
  catch (e: any) { message.error('加载系统失败: ' + e.message) }
}

function onAdd() {
  editing.value = null
  form.value = { systemId: filterSystem.value || '', name: '', code: '', description: '', sortOrder: 0 }
  visible.value = true
}
function onEdit(record: any) {
  editing.value = record
  form.value = { systemId: record.systemId, name: record.name, code: record.code, description: record.description || '', sortOrder: record.sortOrder || 0 }
  visible.value = true
}
async function save() {
  if (!form.value.systemId || !form.value.name || !form.value.code) { message.warning('系统、名称和编码为必填项'); return }
  try {
    if (editing.value) {
      await api(`/api/admin/businesses/${editing.value.id}`, { method: 'PUT', body: JSON.stringify(form.value) })
      message.success('已更新')
    } else {
      await api('/api/admin/businesses', { method: 'POST', body: JSON.stringify(form.value) })
      message.success('已创建')
    }
    visible.value = false
    await load()
  } catch (e: any) { message.error(e.message) }
}
async function onDelete(id: string) {
  try {
    await api(`/api/admin/businesses/${id}`, { method: 'DELETE' })
    message.success('已删除')
    await load()
  } catch (e: any) { message.error(e.message) }
}

onMounted(() => { load(); loadSystems() })
</script>
