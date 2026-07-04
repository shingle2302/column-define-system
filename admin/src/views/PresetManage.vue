<template>
  <div class="page">
    <div class="admin-toolbar">
      <h3>预设方案列表</h3>
      <a-space>
        <a-select v-model:value="filterSystem" placeholder="筛选系统" allowClear style="width: 150px" @change="load">
          <a-select-option v-for="s in systems" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
        </a-select>
        <a-button v-if="auth.role !== 'viewer'" type="primary" @click="onAdd">
          <template #icon><PlusOutlined /></template>
          新增预设
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="admin-card">
      <a-table :columns="columns" :dataSource="list" :pagination="false" :loading="loading" rowKey="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'sysBiz'">
            <span style="font-size: 12px; color: #999">{{ record.systemName }} / {{ record.businessName || '-' }}</span>
          </template>
          <template v-if="column.key === 'constraints'">
            <a-space :size="4" wrap>
              <a-tag v-if="record.minVisibleColumns > 0" color="blue" style="font-size: 11px">min:{{ record.minVisibleColumns }}</a-tag>
              <a-tag v-if="record.maxVisibleColumns > 0" color="purple" style="font-size: 11px">max:{{ record.maxVisibleColumns }}</a-tag>
            </a-space>
          </template>
          <template v-if="column.key === 'action'">
            <a-space v-if="auth.role !== 'viewer'">
              <a @click="onEdit(record)">属性</a>
              <a-popconfirm title="删除预设将同时删除其下所有分组和列定义" @confirm="onDelete(record.id)">
                <a style="color: #ff4d4f">删除</a>
              </a-popconfirm>
            </a-space>
            <span v-else style="color: #ccc">-</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="visible" :title="editing ? '编辑预设属性' : '新增预设'" @ok="save" width="520px">
      <a-form layout="vertical">
        <a-form-item label="预设名称" required>
          <a-input v-model:value="form.name" placeholder="如：标准客户视图" />
        </a-form-item>
        <a-form-item label="所属系统" required>
          <a-select v-model:value="form.systemId" placeholder="选择系统" @change="onSystemChange">
            <a-select-option v-for="s in systems" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="业务模块">
          <a-select v-model:value="form.businessId" placeholder="选择业务（可选）" allowClear :disabled="!form.systemId">
            <a-select-option v-for="b in bizOptions" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" placeholder="预设描述" :rows="2" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="最少可见列">
              <a-input-number v-model:value="form.minVisibleColumns" :min="0" style="width: 100%" placeholder="0=不限制" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最多可见列">
              <a-input-number v-model:value="form.maxVisibleColumns" :min="0" style="width: 100%" placeholder="0=不限制" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="选项">
          <a-space>
            <a-checkbox v-model:checked="form.allowCrossGroup">允许跨分组排序</a-checkbox>
            <a-checkbox v-model:checked="form.isDefault">设为默认方案</a-checkbox>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { api, auth } from '../stores/auth'

const loading = ref(false)
const list = ref<any[]>([])
const systems = ref<any[]>([])
const businesses = ref<any[]>([])
const filterSystem = ref<string | undefined>(undefined)
const visible = ref(false)
const editing = ref<any>(null)
const form = ref({
  name: '', description: '', systemId: '', businessId: null as string | null,
  allowCrossGroup: false, isDefault: false, minVisibleColumns: 0, maxVisibleColumns: 0,
})
const bizOptions = ref<any[]>([])

const columns = [
  { title: '名称', dataIndex: 'name', width: 160 },
  { title: '所属', key: 'sysBiz', width: 180 },
  { title: '约束', key: 'constraints', width: 120 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '操作', key: 'action', width: 130 },
]

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (filterSystem.value) params.set('systemId', filterSystem.value)
    const qs = params.toString() ? '?' + params.toString() : ''
    list.value = await api('/api/admin/presets' + qs)
  } catch (e: any) { message.error('加载失败: ' + e.message) }
  finally { loading.value = false }
}

async function loadRefs() {
  try {
    [systems.value, businesses.value] = await Promise.all([
      api<any[]>('/api/admin/systems'),
      api<any[]>('/api/admin/businesses'),
    ])
  } catch { /* ignore */ }
}

function onSystemChange(systemId: string) {
  bizOptions.value = businesses.value.filter(b => b.systemId === systemId)
  if (!bizOptions.value.find(b => b.id === form.value.businessId)) {
    form.value.businessId = null
  }
}

function onAdd() {
  editing.value = null
  form.value = { name: '', description: '', systemId: filterSystem.value || '', businessId: null, allowCrossGroup: false, isDefault: false, minVisibleColumns: 0, maxVisibleColumns: 0 }
  bizOptions.value = businesses.value.filter(b => b.systemId === form.value.systemId)
  visible.value = true
}
function onEdit(record: any) {
  editing.value = record
  form.value = {
    name: record.name, description: record.description || '',
    systemId: record.systemId, businessId: record.businessId,
    allowCrossGroup: record.allowCrossGroup, isDefault: record.isDefault,
    minVisibleColumns: record.minVisibleColumns || 0, maxVisibleColumns: record.maxVisibleColumns || 0,
  }
  bizOptions.value = businesses.value.filter(b => b.systemId === record.systemId)
  visible.value = true
}
async function save() {
  if (!form.value.name || !form.value.systemId) { message.warning('名称和系统为必填项'); return }
  try {
    if (editing.value) {
      await api(`/api/admin/presets/${editing.value.id}`, { method: 'PUT', body: JSON.stringify(form.value) })
      message.success('已更新')
    } else {
      await api('/api/admin/presets', { method: 'POST', body: JSON.stringify(form.value) })
      message.success('已创建')
    }
    visible.value = false
    await load()
  } catch (e: any) { message.error(e.message) }
}
async function onDelete(id: string) {
  try {
    await api(`/api/admin/presets/${id}`, { method: 'DELETE' })
    message.success('已删除')
    await load()
  } catch (e: any) { message.error(e.message) }
}

onMounted(() => { load(); loadRefs() })
</script>
