<template>
  <div class="col-def-viewer">
    <div class="viewer-header">
      <h3><TableOutlined /> 查看列定义</h3>
      <a-space>
        <a-select
          v-model:value="filterSystemCode"
          placeholder="按系统筛选"
          allowClear
          style="width: 200px"
          @change="onSystemChange"
        >
          <a-select-option v-for="s in systems" :key="s.code" :value="s.code">
            {{ s.name }}
          </a-select-option>
        </a-select>
        <a-button type="primary" @click="refresh" :loading="loading">
          <ReloadOutlined /> 刷新
        </a-button>
      </a-space>
    </div>

    <!-- 预设选择 -->
    <div class="preset-list" v-if="presets.length > 0">
      <a-card
        v-for="preset in presets" :key="preset.id" :bordered="false"
        :class="['preset-card', { active: selectedPresetId === preset.id }]"
        @click="selectPreset(preset.id)" hoverable
      >
        <template #title>
          <span class="preset-name">{{ preset.name }}</span>
          <a-tag style="margin-left: 8px" color="purple">{{ preset.systemId || '-' }}</a-tag>
        </template>
        <template #extra>
          <a-button size="small" type="link" @click.stop="selectPreset(preset.id)">查看分组 →</a-button>
        </template>
        <p class="preset-desc">{{ preset.description || '无描述' }}</p>
      </a-card>
    </div>

    <a-spin v-else-if="loading" style="display: block; text-align: center; padding: 40px" />
    <a-empty v-else description="暂未加载预设方案，请点击刷新" style="padding: 40px" />

    <!-- 分组与列详情 -->
    <template v-if="currentPreset">
      <a-divider />
      <div class="detail-header">
        <h4><FolderOutlined /> {{ currentPreset.name }} — 分组与列</h4>
        <a-tag>v{{ currentPreset.version || 1 }}</a-tag>
      </div>

      <div v-for="group in groups" :key="group.id" class="group-block">
        <div class="group-header">
          <span class="group-label"><FolderOpenOutlined /> {{ group.label }}</span>
          <span class="group-count">{{ group.children?.length || 0 }} 列</span>
        </div>

        <a-table
          v-if="group.children?.length"
          :columns="colColumns" :dataSource="group.children"
          :pagination="false" rowKey="id" size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'visible'">
              <a-tag :color="record.visible ? 'green' : 'default'">
                {{ record.visible ? '显示' : '隐藏' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'pinned'">
              <a-tag v-if="record.pinned === 'left'" color="blue">左固定</a-tag>
              <a-tag v-else-if="record.pinned === 'right'" color="green">右固定</a-tag>
              <span v-else style="color: #ccc">-</span>
            </template>
            <template v-if="column.key === 'constraints'">
              <a-space :size="2" wrap>
                <a-tag v-if="record.locked" color="red" style="font-size: 10px">锁定</a-tag>
                <a-tag v-if="record.sortable" color="blue" style="font-size: 10px">可排序</a-tag>
              </a-space>
            </template>
          </template>
        </a-table>
        <a-empty v-else description="该分组下无列" :image="false" style="padding: 16px" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TableOutlined, ReloadOutlined, FolderOutlined, FolderOpenOutlined } from '@ant-design/icons-vue'

// --------------- Props ---------------
const props = withDefaults(defineProps<{
  serverUrl?: string
  token?: string
}>(), {
  serverUrl: 'http://localhost:8080',
  token: '',
})

// --------------- 内部 API ---------------
function authHeaders(): Record<string, string> {
  const h: Record<string, string> = { 'Content-Type': 'application/json' }
  if (props.token) h['Authorization'] = `Bearer ${props.token}`
  return h
}

async function api<T>(path: string): Promise<T> {
  const resp = await fetch(`${props.serverUrl}${path}`, { headers: authHeaders() })
  const json = await resp.json()
  if (json.code !== 200) throw new Error(json.message || '请求失败')
  return json.data as T
}

// --------------- 类型 ---------------
interface SystemInfo { id: string; name: string; code: string; description?: string; businesses?: any[] }
interface PresetMeta { id: string; name: string; description?: string; systemId?: string; version?: number }
interface BackendColumn {
  id: string; label: string; field: string; type: string; width: number
  sortable: boolean; filterable: boolean; resizable: boolean
  visible: boolean; pinned: string | null; locked: boolean
  allowHide: boolean; allowSort: boolean; allowResize: boolean; allowPin: boolean
  align: string; defaultValue: any; options?: any[]
}
interface BackendGroup { id: string; label: string; collapsed: boolean; children: BackendColumn[] }
interface FullPreset extends PresetMeta { groups: BackendGroup[]; businessId?: string }

// --------------- 状态 ---------------
const systems = ref<SystemInfo[]>([])
const presets = ref<PresetMeta[]>([])
const loading = ref(false)
const filterSystemCode = ref<string | undefined>(undefined)
const selectedPresetId = ref<string | null>(null)
const currentPreset = ref<PresetMeta | null>(null)
const groups = ref<BackendGroup[]>([])

const colColumns = [
  { title: '标签', dataIndex: 'label', width: 100 },
  { title: '字段', dataIndex: 'field', width: 100 },
  { title: '类型', dataIndex: 'type', width: 70 },
  { title: '宽度', dataIndex: 'width', width: 60 },
  { title: '可见', key: 'visible', width: 60 },
  { title: '固定', key: 'pinned', width: 80 },
  { title: '对齐', dataIndex: 'align', width: 60 },
  { title: '约束', key: 'constraints', width: 100 },
]

// --------------- 数据加载 ---------------
async function fetchSystems() {
  try {
    systems.value = await api<SystemInfo[]>('/api/systems')
  } catch { systems.value = [] }
}

async function fetchPresets(systemCode?: string) {
  loading.value = true
  try {
    if (systemCode) {
      presets.value = await api<PresetMeta[]>(`/api/systems/${systemCode}/presets`)
    } else {
      presets.value = await api<PresetMeta[]>('/api/presets')
    }
  } catch { presets.value = [] }
  finally { loading.value = false }
}

async function selectPreset(presetId: string) {
  selectedPresetId.value = presetId
  try {
    const full = await api<FullPreset>(`/api/presets/${presetId}`)
    currentPreset.value = full
    groups.value = full.groups || []
  } catch {
    currentPreset.value = null
    groups.value = []
  }
}

async function refresh() {
  await Promise.all([fetchSystems(), fetchPresets(filterSystemCode.value)])
}

async function onSystemChange() {
  selectedPresetId.value = null
  currentPreset.value = null
  groups.value = []
  await fetchPresets(filterSystemCode.value)
}

onMounted(() => {
  refresh()
})
</script>

<style scoped>
.col-def-viewer { max-width: 960px; margin: 0 auto; }
.viewer-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.viewer-header h3 { margin: 0; font-size: 16px; display: flex; align-items: center; gap: 6px; }
.preset-list { display: flex; flex-direction: column; gap: 8px; }
.preset-card { border-radius: 8px; cursor: pointer; transition: box-shadow 0.2s, border-color 0.2s; border: 2px solid transparent; }
.preset-card.active { border-color: #1677ff; box-shadow: 0 0 0 2px rgba(22,119,255,0.12); }
.preset-name { font-weight: 600; }
.preset-desc { color: #999; font-size: 12px; margin: 0; }
.detail-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.detail-header h4 { margin: 0; font-size: 15px; display: flex; align-items: center; gap: 6px; }
.group-block { margin-bottom: 16px; border: 1px solid #f0f0f0; border-radius: 8px; overflow: hidden; }
.group-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; background: #fafafa; border-bottom: 1px solid #f0f0f0; }
.group-label { font-weight: 600; font-size: 13px; display: flex; align-items: center; gap: 6px; }
.group-count { font-size: 12px; color: #999; }
</style>
