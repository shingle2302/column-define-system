<template>
  <div class="override-panel">
    <div class="panel-header">
      <h3><SwapOutlined /> 覆盖列自定义</h3>
      <a-space>
        <a-radio-group v-model:value="overrideType" button-style="solid" size="small">
          <a-radio-button value="user"><UserOutlined /> 用户级</a-radio-button>
          <a-radio-button value="role"><TeamOutlined /> 角色级</a-radio-button>
        </a-radio-group>
        <a-select
          v-model:value="filterPresetId" placeholder="按预设筛选" allowClear
          style="width: 220px" @change="fetchOverrides"
        >
          <a-select-option v-for="p in presetList" :key="p.id" :value="p.id">{{ p.name }}</a-select-option>
        </a-select>
        <a-button type="primary" @click="openAdd" :disabled="!props.token">
          <PlusOutlined /> 新增覆盖
        </a-button>
        <a-button @click="fetchOverrides" :loading="loading">
          <ReloadOutlined /> 刷新
        </a-button>
      </a-space>
    </div>

    <!-- 覆盖列表 -->
    <a-table
      :columns="columns" :dataSource="overrides" :loading="loading"
      :pagination="{ pageSize: 10, showTotal: (t: number) => `共 ${t} 条` }"
      rowKey="id" size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">
          <a-tag v-if="overrideType === 'user'">{{ record.userName || record.userId }}</a-tag>
          <a-tag v-else :color="roleTagColor(record.role)">{{ record.role }}</a-tag>
        </template>
        <template v-if="column.key === 'column'">
          <span style="font-weight: 500">{{ record.columnLabel }}</span>
          <span style="color: #bbb; font-size: 11px; margin-left: 6px">{{ record.columnField }}</span>
        </template>
        <template v-if="column.key === 'overrideOrder'">
          <span v-if="record.overrideOrder != null" class="override-val">{{ record.overrideOrder }}</span>
          <span v-else style="color: #ccc">-</span>
        </template>
        <template v-if="column.key === 'overrideWidth'">
          <span v-if="record.overrideWidth != null" class="override-val">{{ record.overrideWidth }}px</span>
          <span v-else style="color: #ccc">-</span>
        </template>
        <template v-if="column.key === 'overrideVisible'">
          <a-switch v-if="record.overrideVisible != null" :checked="record.overrideVisible" disabled size="small" />
          <span v-else style="color: #ccc">-</span>
        </template>
        <template v-if="column.key === 'overridePinned'">
          <a-tag v-if="record.overridePinned === 'left'" color="blue">左</a-tag>
          <a-tag v-else-if="record.overridePinned === 'right'" color="green">右</a-tag>
          <span v-else style="color: #ccc">-</span>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除？" @confirm="doDelete(record.id)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingId ? '编辑覆盖' : '新增覆盖'"
      @ok="handleSave" :confirmLoading="saving" width="560px"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="预设方案" required>
              <a-select
                v-model:value="form.presetId" placeholder="选择预设" show-search
                option-filter-prop="label" @change="onPresetChange"
              >
                <a-select-option v-for="p in presetList" :key="p.id" :value="p.id" :label="p.name">
                  {{ p.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="overrideType === 'user' ? '用户' : '角色'" required>
              <a-select
                v-if="overrideType === 'user'"
                v-model:value="form.userId" placeholder="选择用户"
                :disabled="props.role !== 'admin'"
              >
                <a-select-option v-for="u in userList" :key="u.id" :value="u.id">
                  {{ u.displayName || u.username }} ({{ u.username }})
                </a-select-option>
              </a-select>
              <a-select
                v-else v-model:value="form.role" placeholder="选择角色"
                :disabled="props.role !== 'admin'"
              >
                <a-select-option v-for="r in roleList" :key="r.name" :value="r.name">
                  {{ r.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="覆盖列" required>
          <a-select v-model:value="form.columnId" placeholder="选择列" show-search option-filter-prop="label">
            <a-select-option v-for="c in columnList" :key="c.id" :value="c.id" :label="c.label">
              {{ c.label }} ({{ c.field }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="排序">
              <a-input-number v-model:value="form.overrideOrder" placeholder="留空" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="宽度(px)">
              <a-input-number v-model:value="form.overrideWidth" placeholder="留空" style="width: 100%" :min="50" :max="800" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="可见性">
              <a-select v-model:value="form.overrideVisible" placeholder="留空" allowClear>
                <a-select-option :value="true">显示</a-select-option>
                <a-select-option :value="false">隐藏</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="固定位置">
          <a-select v-model:value="form.overridePinned" placeholder="留空" allowClear>
            <a-select-option value="left">左</a-select-option>
            <a-select-option value="right">右</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { SwapOutlined, ReloadOutlined, PlusOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons-vue'

// --------------- Props ---------------
const props = withDefaults(defineProps<{
  serverUrl?: string
  token?: string
  role?: string
  userId?: string
}>(), {
  serverUrl: 'http://localhost:8080',
  token: '',
  role: '',
  userId: '',
})

// --------------- 内部 API ---------------
function authHeaders(): Record<string, string> {
  const h: Record<string, string> = { 'Content-Type': 'application/json' }
  if (props.token) h['Authorization'] = `Bearer ${props.token}`
  return h
}

async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const resp = await fetch(`${props.serverUrl}${path}`, {
    ...options,
    headers: { ...authHeaders(), ...(options?.headers as Record<string, string> || {}) },
  })
  const json = await resp.json()
  if (json.code !== 200) throw new Error(json.message || '请求失败')
  return json.data as T
}

// --------------- 类型 ---------------
interface ColumnOverride {
  id: number; presetId: string; presetName?: string
  userId?: string; userName?: string; role?: string
  columnId: string; columnLabel?: string; columnField?: string
  overrideOrder: number | null; overrideWidth: number | null
  overrideVisible: boolean | null; overridePinned: string | null
  updatedAt?: string
}

interface OverrideFormData {
  presetId: string; userId?: string; role?: string; columnId: string
  overrideOrder: number | null; overrideWidth: number | null
  overrideVisible: boolean | null; overridePinned: string | null
}

// --------------- 状态 ---------------
const overrideType = ref<'user' | 'role'>('user')
const overrides = ref<ColumnOverride[]>([])
const loading = ref(false)
const saving = ref(false)

const presetList = ref<any[]>([])
const columnList = ref<any[]>([])
const userList = ref<any[]>([])
const roleList = ref<any[]>([])

const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const filterPresetId = ref<string | undefined>(undefined)

const form = ref<OverrideFormData>({
  presetId: '', userId: '', role: '', columnId: '',
  overrideOrder: null, overrideWidth: null, overrideVisible: null, overridePinned: null,
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 50 },
  { title: '用户/角色', key: 'target', width: 100 },
  { title: '覆盖列', key: 'column', width: 150 },
  { title: '排序', key: 'overrideOrder', width: 55, align: 'center' as const },
  { title: '宽度', key: 'overrideWidth', width: 70, align: 'center' as const },
  { title: '可见', key: 'overrideVisible', width: 55, align: 'center' as const },
  { title: '固定', key: 'overridePinned', width: 55, align: 'center' as const },
  { title: '操作', key: 'action', width: 120 },
]

// --------------- 数据加载 ---------------
function getApiPath(): string {
  return overrideType.value === 'user' ? '/api/admin/overrides' : '/api/admin/role-presets'
}

async function fetchOverrides() {
  if (!props.token) return
  loading.value = true
  try {
    const params = filterPresetId.value ? `?presetId=${encodeURIComponent(filterPresetId.value)}` : ''
    overrides.value = await api<ColumnOverride[]>(`${getApiPath()}${params}`)
  } catch { overrides.value = [] }
  finally { loading.value = false }
}

async function fetchPresetList() {
  if (!props.token) return
  try { presetList.value = await api<any[]>('/api/admin/presets') }
  catch { presetList.value = [] }
}

async function fetchUserList() {
  if (!props.token) return
  if (props.role !== 'admin') {
    userList.value = [{ id: props.userId, username: '', displayName: '当前用户' }]
    return
  }
  try { userList.value = await api<any[]>('/api/admin/users') }
  catch { userList.value = [] }
}

async function fetchRoleList() {
  if (!props.token) return
  if (props.role !== 'admin') {
    roleList.value = [{ name: props.role }]
    return
  }
  try { roleList.value = await api<any[]>('/api/admin/roles') }
  catch { roleList.value = [] }
}

async function onPresetChange(pid: string) {
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
}

// --------------- 切换覆盖类型 ---------------
watch(overrideType, () => { fetchOverrides() })

// --------------- 增删改 ---------------
function resetForm() {
  form.value = {
    presetId: '', userId: '', role: '', columnId: '',
    overrideOrder: null, overrideWidth: null, overrideVisible: null, overridePinned: null,
  }
  editingId.value = null
  columnList.value = []
}

function openAdd() {
  resetForm()
  if (filterPresetId.value) {
    form.value.presetId = filterPresetId.value
    onPresetChange(filterPresetId.value)
  }
  if (props.role !== 'admin') {
    if (overrideType.value === 'user') form.value.userId = props.userId
    else form.value.role = props.role
  }
  modalVisible.value = true
}

function openEdit(record: ColumnOverride) {
  resetForm()
  editingId.value = record.id
  form.value.presetId = record.presetId
  form.value.userId = record.userId
  form.value.role = record.role
  form.value.columnId = record.columnId
  form.value.overrideOrder = record.overrideOrder
  form.value.overrideWidth = record.overrideWidth
  form.value.overrideVisible = record.overrideVisible
  form.value.overridePinned = record.overridePinned
  onPresetChange(record.presetId)
  modalVisible.value = true
}

async function handleSave() {
  const isUser = overrideType.value === 'user'
  if (!form.value.presetId || !form.value.columnId) return
  if (isUser && !form.value.userId) return
  if (!isUser && !form.value.role) return

  const data: OverrideFormData = {
    presetId: form.value.presetId,
    columnId: form.value.columnId,
    overrideOrder: form.value.overrideOrder,
    overrideWidth: form.value.overrideWidth,
    overrideVisible: form.value.overrideVisible,
    overridePinned: form.value.overridePinned,
  }
  if (isUser) data.userId = form.value.userId!
  else data.role = form.value.role!

  saving.value = true
  try {
    await api(getApiPath(), { method: 'POST', body: JSON.stringify(data) })
    modalVisible.value = false
    await fetchOverrides()
  } catch { /* ignore */ }
  finally { saving.value = false }
}

async function doDelete(id: number) {
  try {
    await api(`${getApiPath()}/${id}`, { method: 'DELETE' })
    overrides.value = overrides.value.filter(o => o.id !== id)
  } catch { /* ignore */ }
}

function roleTagColor(role: string) {
  const map: Record<string, string> = { admin: 'red', user: 'blue', viewer: 'default' }
  return map[role] || 'default'
}

onMounted(async () => {
  await Promise.all([fetchPresetList(), fetchUserList(), fetchRoleList()])
  if (props.token) await fetchOverrides()
})
</script>

<style scoped>
.override-panel { max-width: 960px; margin: 0 auto; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.panel-header h3 { margin: 0; font-size: 16px; display: flex; align-items: center; gap: 6px; }
.override-val { font-weight: 600; color: #1677ff; }
</style>
