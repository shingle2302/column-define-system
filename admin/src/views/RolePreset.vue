<template>
  <div class="role-preset-page">
    <div class="page-header">
      <h2><TeamOutlined /> 角色预设管理</h2>
      <p class="page-desc">按角色维度管理列配置覆盖（如 admin 角色看到的列、viewer 角色隐藏的列等），优先级低于用户级覆盖</p>
    </div>

    <!-- 筛选栏 -->
    <a-card :bordered="false" class="filter-card">
      <a-space wrap>
        <span>预设方案：</span>
        <a-select
          v-model:value="filterPresetId"
          placeholder="全部预设"
          allowClear
          style="width: 260px"
          show-search
          option-filter-prop="label"
          @change="fetchData"
        >
          <a-select-option v-for="p in presetList" :key="p.id" :value="p.id" :label="p.name">
            {{ p.name }}
            <span style="color: #999; font-size: 12px; margin-left: 8px">{{ p.systemName }}</span>
          </a-select-option>
        </a-select>
        <a-button v-if="auth.role !== 'viewer'" type="primary" @click="openAdd">
          <PlusOutlined /> 新增角色覆盖
        </a-button>
        <a-button @click="fetchData" :loading="loading">
          <ReloadOutlined /> 刷新
        </a-button>
      </a-space>
    </a-card>

    <!-- 表格 -->
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
          <template v-if="column.key === 'preset'">
            <span>{{ record.presetName }}</span>
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="roleColor(record.role)">{{ record.role }}</a-tag>
          </template>
          <template v-else-if="column.key === 'column'">
            <span style="font-weight: 500">{{ record.columnLabel }}</span>
            <span style="color: #bbb; font-size: 11px; margin-left: 6px">{{ record.columnField }}</span>
          </template>
          <template v-else-if="column.key === 'overrideOrder'">
            <span v-if="record.overrideOrder != null" class="override-val">{{ record.overrideOrder }}</span>
            <span v-else style="color: #ccc">-</span>
          </template>
          <template v-else-if="column.key === 'overrideWidth'">
            <span v-if="record.overrideWidth != null" class="override-val">{{ record.overrideWidth }}px</span>
            <span v-else style="color: #ccc">-</span>
          </template>
          <template v-else-if="column.key === 'overrideVisible'">
            <a-switch v-if="record.overrideVisible != null" :checked="record.overrideVisible" disabled size="small" />
            <span v-else style="color: #ccc">-</span>
          </template>
          <template v-else-if="column.key === 'overridePinned'">
            <a-tag v-if="record.overridePinned === 'left'" color="blue">固定左</a-tag>
            <a-tag v-else-if="record.overridePinned === 'right'" color="green">固定右</a-tag>
            <a-tag v-else-if="record.overridePinned != null" color="default">{{ record.overridePinned }}</a-tag>
            <span v-else style="color: #ccc">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="auth.role !== 'viewer'">
              <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
              <a-popconfirm title="确定删除此条角色覆盖？" @confirm="doDelete(record.id)">
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
            <span v-else style="color: #ccc">-</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingId ? '编辑角色覆盖' : '新增角色覆盖'"
      @ok="handleSave"
      :confirmLoading="saving"
      width="560px"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="预设方案" required>
              <a-select v-model:value="form.presetId" placeholder="选择预设" show-search option-filter-prop="label">
                <a-select-option v-for="p in presetList" :key="p.id" :value="p.id" :label="p.name">
                  {{ p.name }} ({{ p.systemName }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="角色" required>
              <a-select
                v-model:value="form.role"
                placeholder="选择角色"
                show-search
                option-filter-prop="label"
                :disabled="auth.role !== 'admin'"
              >
                <a-select-option v-for="r in roleList" :key="r.name" :value="r.name" :label="r.name">
                  {{ r.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="覆盖列" required>
          <a-select v-model:value="form.columnId" placeholder="选择要覆盖的列" show-search option-filter-prop="label">
            <a-select-option v-for="c in columnList" :key="c.id" :value="c.id" :label="c.label">
              {{ c.label }} ({{ c.field }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="排序">
              <a-input-number v-model:value="form.overrideOrder" placeholder="留空不覆盖" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="宽度(px)">
              <a-input-number v-model:value="form.overrideWidth" placeholder="留空不覆盖" style="width: 100%" :min="50" :max="800" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="可见性">
              <a-select v-model:value="form.overrideVisible" placeholder="留空不覆盖" allowClear>
                <a-select-option :value="true">显示</a-select-option>
                <a-select-option :value="false">隐藏</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="固定位置">
          <a-select v-model:value="form.overridePinned" placeholder="留空不覆盖" allowClear>
            <a-select-option value="left">固定左侧</a-select-option>
            <a-select-option value="right">固定右侧</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { TeamOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { api, auth } from '../stores/auth'

const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const filterPresetId = ref<string | undefined>(undefined)
const presetList = ref<any[]>([])
const columnList = ref<any[]>([])
const roleList = ref<any[]>([])
const dataSource = ref<any[]>([])

const form = ref({
  presetId: '' as string,
  role: '' as string,
  columnId: '' as string,
  overrideOrder: null as number | null,
  overrideWidth: null as number | null,
  overrideVisible: undefined as boolean | undefined,
  overridePinned: undefined as string | undefined,
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '预设', key: 'preset', width: 140, ellipsis: true },
  { title: '角色', key: 'role', width: 80 },
  { title: '覆盖列', key: 'column', width: 160 },
  { title: '排序', key: 'overrideOrder', width: 60, align: 'center' },
  { title: '宽度', key: 'overrideWidth', width: 80, align: 'center' },
  { title: '可见', key: 'overrideVisible', width: 60, align: 'center' },
  { title: '固定', key: 'overridePinned', width: 80, align: 'center' },
  { title: '更新时间', dataIndex: 'updatedAt', width: 150, ellipsis: true },
  { title: '操作', key: 'action', width: 130, fixed: 'right' },
]

function roleColor(role: string) {
  const map: Record<string, string> = { admin: 'red', user: 'blue', viewer: 'default' }
  return map[role] || 'default'
}

onMounted(async () => {
  await Promise.all([fetchPresets(), fetchRoles(), fetchData()])
})

// 当选中预设时，加载该预设的列列表
watch(() => form.value.presetId, async (pid) => {
  if (!pid) { columnList.value = []; return }
  try {
    const groups = await api<any[]>(`/api/admin/presets/${pid}/groups`)
    const cols: any[] = []
    for (const g of groups) {
      const c = await api<any[]>(`/api/admin/groups/${g.id}/columns`)
      cols.push(...c)
    }
    columnList.value = cols
  } catch { columnList.value = [] }
})

async function fetchPresets() {
  try {
    presetList.value = await api<any[]>('/api/admin/presets')
  } catch { /* ignore */ }
}

async function fetchRoles() {
  // 非 admin 用户只能看到自己的角色
  if (auth.role !== 'admin') {
    roleList.value = [{ name: auth.role, description: '' }]
    return
  }
  try {
    roleList.value = await api<any[]>('/api/admin/roles')
  } catch {
    roleList.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = filterPresetId.value ? `?presetId=${filterPresetId.value}` : ''
    dataSource.value = await api<any[]>(`/api/admin/role-presets${params}`)
  } catch (e: any) {
    console.error('加载角色预设失败:', e)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = {
    presetId: '', role: '', columnId: '',
    overrideOrder: null, overrideWidth: null,
    overrideVisible: undefined, overridePinned: undefined,
  }
  editingId.value = null
  columnList.value = []
}

function openAdd() {
  resetForm()
  if (filterPresetId.value) {
    form.value.presetId = filterPresetId.value
  }
  // 非 admin 用户自动填入当前角色
  if (auth.role !== 'admin') {
    form.value.role = auth.role
  }
  modalVisible.value = true
}

function openEdit(record: any) {
  resetForm()
  editingId.value = record.id
  form.value.presetId = record.presetId
  form.value.role = record.role
  form.value.columnId = record.columnId
  form.value.overrideOrder = record.overrideOrder
  form.value.overrideWidth = record.overrideWidth
  form.value.overrideVisible = record.overrideVisible
  form.value.overridePinned = record.overridePinned
  modalVisible.value = true
}

async function handleSave() {
  if (!form.value.presetId || !form.value.role || !form.value.columnId) return
  saving.value = true
  try {
    await api('/api/admin/role-presets', {
      method: 'POST',
      body: JSON.stringify({
        presetId: form.value.presetId,
        role: form.value.role,
        columnId: form.value.columnId,
        overrideOrder: form.value.overrideOrder,
        overrideWidth: form.value.overrideWidth,
        overrideVisible: form.value.overrideVisible,
        overridePinned: form.value.overridePinned,
      }),
    })
    modalVisible.value = false
    await fetchData()
  } catch { /* ignore */ } finally {
    saving.value = false
  }
}

async function doDelete(id: number) {
  try {
    await api(`/api/admin/role-presets/${id}`, { method: 'DELETE' })
    dataSource.value = dataSource.value.filter(r => r.id !== id)
  } catch { /* ignore */ }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}
.page-header h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-desc {
  color: #999;
  font-size: 13px;
  margin: 6px 0 0;
}
.filter-card {
  margin-bottom: 16px;
}
.filter-card :deep(.ant-card-body) {
  padding: 12px 16px;
}
.override-val {
  font-weight: 600;
  color: #1677ff;
}
</style>
