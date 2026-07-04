<template>
  <div class="column-data-table">
    <!-- 加载中 -->
    <a-card v-if="loading" :bordered="false">
      <div style="text-align:center;padding:60px 0;color:#bbb">
        <a-spin size="large" />
        <p style="margin-top:16px">正在从后端加载列定义...</p>
      </div>
    </a-card>

    <!-- 加载错误 -->
    <a-alert
      v-else-if="error"
      type="error"
      :message="error"
      banner
      style="margin-bottom:16px"
    />

    <!-- 无预设 -->
    <a-empty v-else-if="!presetList.length" description="暂无预设方案，请先在管理后台创建列定义预设" />

    <!-- 主体 -->
    <template v-else>
      <!-- 工具栏 -->
      <div class="column-data-table__toolbar">
        <ColumnPresetPicker :presets="presetList" v-model="selectedPresetId" />
        <a-button @click="configVisible = true" type="primary" ghost size="small">
          <SettingOutlined /> 自定义列
        </a-button>
      </div>

      <!-- 列自定义弹窗 -->
      <ColumnConfigurator
        ref="configuratorRef"
        :visible="configVisible"
        :preset="rawPreset"
        :resolved-columns="resolvedColumns"
        :preset-list="presetList"
        :selected-preset-id="selectedPresetId"
        :base-url="serverUrl"
        :user="user"
        :password="password"
        :business="businessId"
        @cancel="configVisible = false"
        @saved="onSaved"
        @restored="onRestored"
      />

      <!-- 表格 -->
      <a-card :bordered="false">
        <ColumnTable
          :columns="resolvedColumns"
          :data="tableData"
          :sort-by="sortBy"
          :sort-order="sortOrder"
          :row-key="rowKey"
          @sort="onSort"
        >
          <template #header="{ column }">
            <slot name="header" :column="column">
              <strong>{{ column.label }}</strong>
            </slot>
          </template>
          <template #cell="{ column, value, record }">
            <slot name="cell" :column="column" :value="value" :record="record">
              <span>{{ value ?? '-' }}</span>
            </slot>
          </template>
        </ColumnTable>
      </a-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  ColumnTable, ColumnPresetPicker,
  useColumnConfig,
} from './uiStubs'
import type { ColumnPreset } from './index'
import ColumnConfigurator from './ColumnConfigurator.vue'
import { usePresetFetcher } from './usePresetFetcher'
import { SettingOutlined } from '@ant-design/icons-vue'

interface OverrideItem {
  order?: number | null
  width?: number | null
  visible?: boolean | null
  pinned?: string | null
}

interface OverrideResponse {
  presetId: string
  columnOverrides?: Record<string, OverrideItem>
  version?: number
  updatedAt?: number
}

// ---------------------------------------------------------
// Props
// ---------------------------------------------------------
const props = withDefaults(defineProps<{
  serverUrl: string
  token: string
  user: string
  password: string
  systemId: string
  businessId: string
  data: Record<string, unknown>[]
  defaultPresetId?: string
  rowKey?: string
}>(), {
  rowKey: 'id',
  defaultPresetId: '',
})

const emit = defineEmits<{
  'override-saved': []
  'preset-changed': [presetId: string]
}>()

// ---------------------------------------------------------
// 通用 API 请求
// ---------------------------------------------------------
async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (props.token) headers['Authorization'] = `Bearer ${props.token}`

  const resp = await fetch(`${props.serverUrl}${path}`, { ...options, headers })
  if (!resp.ok) {
    const text = await resp.text().catch(() => '')
    throw new Error(`[${resp.status}] ${text || resp.statusText}`)
  }
  const json = await resp.json()
  // 后端统一返回 { code: 200, data: ..., message: ... }
  if (json?.data !== undefined) return json.data as T
  return json as T
}

// ---------------------------------------------------------
// 使用 presetFetcher 拉取预设
// ---------------------------------------------------------
const { presets: presetList, loading, error, fetchByBusiness } = usePresetFetcher(api)

// ---------------------------------------------------------
// 状态
// ---------------------------------------------------------
const selectedPresetId = ref(props.defaultPresetId || '')
const configVisible = ref(false)

// 覆盖（来自后端）
const overridesMap = ref<Map<string, OverrideItem>>(new Map())
const overrideVersion = ref(0)

// 排序
const sortBy = ref('')
const sortOrder = ref<'asc' | 'desc'>('asc')

// 原始预设（未合并覆盖）
const rawPreset = computed<ColumnPreset | null>(() => {
  return presetList.value.find(p => p.id === selectedPresetId.value)
    || presetList.value[0]
    || null
})

// 合并覆盖后的预设
const mergedPreset = computed<ColumnPreset | null>(() => {
  if (!rawPreset.value) return null
  if (overridesMap.value.size === 0) return rawPreset.value

  // 深拷贝并应用覆盖
  const copy: ColumnPreset = JSON.parse(JSON.stringify(rawPreset.value))
  for (const group of copy.groups) {
    for (const col of group.children) {
      const ov = overridesMap.value.get(col.id)
      if (!ov) continue
      if (ov.order != null) (col as any).order = ov.order
      if (ov.width != null) col.width = ov.width
      if (ov.visible != null) col.visible = ov.visible
      if (ov.pinned != null) (col as any).pinned = ov.pinned
    }
  }
  return copy
})

const { resolvedColumns, updateColumn, resetToPreset } = useColumnConfig(mergedPreset, null)

// 当前模块的业务数据（由父组件传入）
const tableData = computed(() => props.data)

// ---------------------------------------------------------
// 核心：加载预设 + 覆盖
// ---------------------------------------------------------

/** 从后端拉取指定业务的预设列表 */
async function fetchPresets() {
  if (!props.businessId) return
  await fetchByBusiness(props.businessId)
  // 选择预设：优先用 defaultPresetId，否则选第一个
  if (props.defaultPresetId && presetList.value.find(p => p.id === props.defaultPresetId)) {
    selectedPresetId.value = props.defaultPresetId
  } else if (presetList.value.length > 0 && presetList.value[0]) {
    selectedPresetId.value = presetList.value[0].id
  }
}

/** 从后端加载用户覆盖 */
async function fetchOverrides() {
  if (!props.token || !selectedPresetId.value) {
    overridesMap.value = new Map()
    return
  }
  try {
    const resp = await api<OverrideResponse>(`/api/overrides/${selectedPresetId.value}`)
    const map = new Map<string, OverrideItem>()
    if (resp.columnOverrides) {
      for (const [colId, item] of Object.entries(resp.columnOverrides)) {
        map.set(colId, item)
      }
    }
    overridesMap.value = map
    overrideVersion.value = resp.version || 0
  } catch {
    // 覆盖加载失败不影响预设展示
    overridesMap.value = new Map()
  }
}

/** 加载全部：预设 + 覆盖 */
async function loadAll() {
  await fetchPresets()
  if (props.token && selectedPresetId.value) {
    await fetchOverrides()
  }
}

// 当 businessId 变化时重新加载
watch(() => props.businessId, loadAll, { immediate: true })

// 切换预设时重新加载覆盖
watch(selectedPresetId, async (newId) => {
  if (props.token && newId) {
    await fetchOverrides()
    emit('preset-changed', newId)
  }
})

// ---------------------------------------------------------
// 列配置弹窗回调
// ---------------------------------------------------------

/** 保存成功回调：应用列配置到本地，更新覆盖缓存，关闭弹窗 */
function onSaved(payload: { columns: any[]; targetPresetId: string; columnOverrides: Record<string, OverrideItem> }) {
  for (const col of payload.columns) {
    updateColumn(col.id, {
      visible: col.visible,
      width: col.width,
      pinned: col.pinned || undefined,
      order: col.order,
    } as any)
  }

  if (props.token && payload.targetPresetId) {
    if (payload.targetPresetId !== selectedPresetId.value) {
      selectedPresetId.value = payload.targetPresetId
      fetchOverrides()
    } else {
      overridesMap.value = new Map(Object.entries(payload.columnOverrides))
    }
    emit('override-saved')
  }

  configVisible.value = false
}

/** 恢复成功回调：清除本地覆盖，关闭弹窗 */
async function onRestored(payload: { targetPresetId: string }) {
  if (!props.token || !payload.targetPresetId) return

  overridesMap.value = new Map()
  if (payload.targetPresetId === selectedPresetId.value) {
    resetToPreset()
  } else {
    selectedPresetId.value = payload.targetPresetId
    await fetchOverrides()
  }

  emit('override-saved')
  configVisible.value = false
}

function handleReset() { resetToPreset() }

// ---------------------------------------------------------
// 排序（前端本地排序，供数据展示层使用）
// ---------------------------------------------------------
function onSort(colId: string) {
  if (sortBy.value === colId) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = colId
    sortOrder.value = 'asc'
  }
}

const configuratorRef = ref<InstanceType<typeof ColumnConfigurator>>()

/** 公开的保存方法（无模态，直接保存当前覆盖） */
async function saveOverrides() {
  if (!props.token || !selectedPresetId.value) return
  try {
    const result = await configuratorRef.value!.saveOverrides({
      presetId: selectedPresetId.value,
      columns: resolvedColumns.value,
      overrideVersion: overrideVersion.value || undefined,
    })
    overridesMap.value = new Map(Object.entries(result.columnOverrides))
    emit('override-saved')
  } catch (e: any) {
    throw e
  }
}

// ---------------------------------------------------------
// 暴露
// ---------------------------------------------------------
defineExpose({
  saveOverrides,
  resetColumns: handleReset,
  refresh: loadAll,
  resolvedColumns,
  rawPreset,
  selectedPresetId,
})
</script>

<style scoped>
.column-data-table__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
