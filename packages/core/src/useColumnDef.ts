/**
 * useColumnDef — 列定义查询 composable
 *
 * 从后端拉取预设方案、分组、列定义，并提供可复用的响应式状态。
 *
 * 不依赖 pinia / vue-router，不依赖具体 auth 实现。
 * 调用方需传入符合 ApiClient 接口的 api 函数。
 */

import { ref, computed } from 'vue'
import type { ApiClient } from './types'

// --------------- 类型 ---------------
export interface ColumnDef {
  id: string
  label: string
  field: string
  type: string
  width: number
  minWidth: number | null
  maxWidth: number | null
  sortable: boolean
  filterable: boolean
  resizable: boolean
  visible: boolean
  pinned: string | null
  locked: boolean
  allowHide: boolean
  allowSort: boolean
  allowResize: boolean
  allowPin: boolean
  align: string
  defaultValue: string
  optionsJson: string
  sortOrder: number
}

export interface GroupDef {
  id: string
  label: string
  sortOrder?: number
  collapsed?: boolean
  columns?: ColumnDef[]
}

export interface PresetDef {
  id: string
  name: string
  description?: string
  systemId?: string
  systemName?: string
  businessId?: string
  businessName?: string
  version?: number
  groups?: GroupDef[]
}

export interface SystemDef {
  id: string
  name: string
  code: string
  description?: string
}

// --------------- 工具函数 ---------------
/** 将后端返回的列定义转换为 ColumnPreset 需要的 column 格式 */
export function toPresetColumn(c: ColumnDef): any {
  return {
    id: c.id,
    label: c.label,
    field: c.field,
    type: c.type,
    width: c.width,
    minWidth: c.minWidth,
    maxWidth: c.maxWidth,
    sortable: c.sortable,
    filterable: c.filterable,
    resizable: c.resizable,
    visible: c.visible,
    pinned: c.pinned || undefined,
    locked: c.locked,
    allowHide: c.allowHide,
    allowSort: c.allowSort,
    allowResize: c.allowResize,
    allowPin: c.allowPin,
    align: c.align || 'left',
    defaultValue: c.defaultValue,
    optionsJson: c.optionsJson,
    sortOrder: c.sortOrder,
  }
}

/** 将后端的分组 + 列转换为 ColumnPreset group 格式 */
export function toPresetGroup(g: GroupDef): any {
  return {
    id: g.id,
    label: g.label,
    collapsed: g.collapsed ?? false,
    children: (g.columns || []).map(toPresetColumn),
  }
}

/** 将后端的预设转换为 ColumnPreset 格式 */
export function toColumnPreset(p: PresetDef): any {
  return {
    id: p.id,
    name: p.name,
    description: p.description || '',
    version: p.version || 1,
    groups: (p.groups || []).map(toPresetGroup),
  }
}

// --------------- Composable ---------------
export function useColumnDef(api: ApiClient) {
  // 状态
  const systems = ref<SystemDef[]>([])
  const presets = ref<PresetDef[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 当前选中的预设
  const selectedPresetId = ref<string | null>(null)
  const selectedPreset = computed(() =>
    presets.value.find((p: PresetDef) => p.id === selectedPresetId.value) || null
  )

  /** 加载所有系统 */
  async function fetchSystems() {
    try {
      systems.value = await api<SystemDef[]>('/api/admin/systems')
    } catch {
      systems.value = []
    }
  }

  /** 加载预设列表（可选按 systemId 筛选） */
  async function fetchPresets(systemId?: string) {
    loading.value = true
    error.value = null
    try {
      const params = systemId ? `?systemId=${encodeURIComponent(systemId)}` : ''
      presets.value = await api<PresetDef[]>(`/api/admin/presets${params}`)
    } catch (e: any) {
      error.value = e.message || '加载预设失败'
      presets.value = []
    } finally {
      loading.value = false
    }
  }

  /** 加载某个预设的分组（含列定义） */
  async function fetchGroups(presetId: string) {
    const groups = await api<GroupDef[]>(`/api/admin/presets/${presetId}/groups`)
    // 对每个分组加载列
    const enriched = await Promise.all(groups.map(async (g) => {
      const cols = await api<ColumnDef[]>(`/api/admin/groups/${g.id}/columns`)
      return { ...g, columns: cols }
    }))
    return enriched
  }

  /** 加载完整的预设（分组 + 列，直接返回 ColumnPreset 格式） */
  async function fetchFullPreset(presetId: string) {
    loading.value = true
    error.value = null
    try {
      // 先获取预设元信息
      const presetMeta = await api<PresetDef>(`/api/admin/presets/${presetId}`)
      // 再获取分组和列
      const groups = await fetchGroups(presetId)
      const full: PresetDef = { ...presetMeta, groups }
      return full
    } catch (e: any) {
      error.value = e.message || '加载预设详情失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 一键加载：系统 + 预设，并选中第一个 */
  async function init(systemId?: string) {
    await fetchSystems()
    await fetchPresets(systemId)
    if (presets.value.length > 0 && !selectedPresetId.value) {
      selectedPresetId.value = presets.value[0].id
    }
  }

  return {
    systems,
    presets,
    loading,
    error,
    selectedPresetId,
    selectedPreset,
    fetchSystems,
    fetchPresets,
    fetchGroups,
    fetchFullPreset,
    init,
  }
}
