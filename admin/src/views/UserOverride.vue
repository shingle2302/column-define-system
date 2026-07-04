<template>
  <div class="user-override-page">
    <div class="page-header">
      <h2><UserSwitchOutlined /> 用户覆盖管理</h2>
      <p class="page-desc">查看和管理用户对预设列的个性化覆盖（排序、宽度、可见性、固定位置）</p>
    </div>

    <!-- 筛选栏 -->
    <a-card :bordered="false" class="filter-card">
      <a-space>
        <span>预设方案：</span>
        <a-select
          v-model:value="filterPresetId"
          placeholder="全部预设"
          allowClear
          style="width: 260px"
          show-search
          option-filter-prop="label"
          @change="fetchOverrides"
        >
          <a-select-option v-for="p in presetList" :key="p.id" :value="p.id" :label="p.name">
            {{ p.name }}
            <span style="color: #999; font-size: 12px; margin-left: 8px">{{ p.systemName }}</span>
          </a-select-option>
        </a-select>
        <a-button v-if="auth.role !== 'viewer'" type="primary" @click="openAdd">
          <PlusOutlined /> 新增用户覆盖
        </a-button>
        <a-button @click="fetchOverrides" :loading="loading">
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
          <template v-else-if="column.key === 'user'">
            <a-tag>{{ record.userName }}</a-tag>
            <span style="color: #999; font-size: 11px; margin-left: 4px">{{ record.userId }}</span>
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
            <a-switch
              v-if="record.overrideVisible != null"
              :checked="record.overrideVisible"
              disabled
              size="small"
            />
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
              <a-popconfirm title="确定删除此条覆盖？" @confirm="doDelete(record.id)">
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
      :title="editingId != null ? '编辑用户覆盖' : '新增用户覆盖'"
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
            <a-form-item label="用户" required>
              <a-select
                v-model:value="form.userId"
                placeholder="选择用户"
                show-search
                option-filter-prop="label"
                :disabled="auth.role !== 'admin'"
              >
                <a-select-option v-for="u in userList" :key="u.id" :value="u.id" :label="u.username">
                  {{ u.displayName || u.username }} ({{ u.username }})
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
import { UserSwitchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { api, auth } from '../stores/auth'

const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const filterPresetId = ref<string | undefined>(undefined)
const presetList = ref<any[]>([])
const columnList = ref<any[]>([])
const userList = ref<any[]>([])
const dataSource = ref<any[]>([])

const form = ref({
  presetId: '' as string,
  userId: '' as string,
  columnId: '' as string,
  overrideOrder: null as number | null,
  overrideWidth: null as number | null,
  overrideVisible: undefined as boolean | undefined,
  overridePinned: undefined as string | undefined,
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '预设', key: 'preset', width: 140, ellipsis: true },
  { title: '用户', key: 'user', width: 140 },
  { title: '覆盖列', key: 'column', width: 160 },
  { title: '排序', key: 'overrideOrder', width: 60, align: 'center' },
  { title: '宽度', key: 'overrideWidth', width: 80, align: 'center' },
  { title: '可见', key: 'overrideVisible', width: 60, align: 'center' },
  { title: '固定', key: 'overridePinned', width: 80, align: 'center' },
  { title: '更新时间', dataIndex: 'updatedAt', width: 150, ellipsis: true },
  { title: '操作', key: 'action', width: 130, fixed: 'right' },
]

onMounted(async () => {
  await Promise.all([fetchPresets(), fetchUsers(), fetchOverrides()])
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

async function fetchUsers() {
  // 非 admin 用户只能看到自己，不需要请求用户列表
  if (auth.role !== 'admin') {
    userList.value = [{
      id: auth.userId,
      username: auth.username,
      displayName: auth.displayName,
    }]
    return
  }
  try {
    userList.value = await api<any[]>('/api/admin/users')
  } catch { /* ignore */ }
}

async function fetchOverrides() {
  loading.value = true
  try {
    const params = filterPresetId.value ? `?presetId=${filterPresetId.value}` : ''
    const list = await api<any[]>(`/api/admin/overrides${params}`)
    dataSource.value = list
  } catch (e: any) {
    console.error('加载用户覆盖失败:', e)
  } finally {
    loading.value = false
  }
}

async function doDelete(id: number) {
  try {
    await api(`/api/admin/overrides/${id}`, { method: 'DELETE' })
    dataSource.value = dataSource.value.filter(r => r.id !== id)
  } catch { /* ignore */ }
}

function resetForm() {
  form.value = {
    presetId: '', userId: '', columnId: '',
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
  // 非 admin 用户自动填入当前用户
  if (auth.role !== 'admin') {
    form.value.userId = auth.userId
  }
  modalVisible.value = true
}

function openEdit(record: any) {
  resetForm()
  editingId.value = record.id
  form.value.presetId = record.presetId
  form.value.userId = record.userId
  form.value.columnId = record.columnId
  form.value.overrideOrder = record.overrideOrder
  form.value.overrideWidth = record.overrideWidth
  form.value.overrideVisible = record.overrideVisible
  form.value.overridePinned = record.overridePinned
  // 触发列列表加载
  modalVisible.value = true
}

async function handleSave() {
  if (!form.value.presetId || !form.value.userId || !form.value.columnId) return
  saving.value = true
  try {
    await api('/api/admin/overrides', {
      method: 'POST',
      body: JSON.stringify({
        presetId: form.value.presetId,
        userId: form.value.userId,
        columnId: form.value.columnId,
        overrideOrder: form.value.overrideOrder,
        overrideWidth: form.value.overrideWidth,
        overrideVisible: form.value.overrideVisible,
        overridePinned: form.value.overridePinned,
      }),
    })
    modalVisible.value = false
    await fetchOverrides()
  } catch { /* ignore */ } finally {
    saving.value = false
  }
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
