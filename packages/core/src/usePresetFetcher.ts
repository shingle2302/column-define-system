/**
 * usePresetFetcher — 按业务拉取列预设方案
 *
 * 封装 fetchByBusiness 方法，统一后端数据 → ColumnPreset 的转换。
 * 不依赖具体 auth 实现，通过 ApiClient 解耦。
 */

import { ref, computed } from 'vue'
import type { ColumnPreset } from './index'
import { toPresetColumn } from './useColumnDef'
import type { ApiClient } from './types'

// --------------- 后端原始数据结构 ---------------
interface BackendPreset {
  id: string; name: string; description?: string
  systemId?: string; businessId?: string
  groups?: { id: string; label: string; collapsed?: boolean; children?: any[] }[]
  version?: number
}

/** 将后端分组转换为 ColumnPreset group */
function convertGroup(g: { id: string; label: string; collapsed?: boolean; children?: any[] }): any {
  return {
    id: g.id,
    label: g.label,
    collapsed: g.collapsed ?? false,
    children: (g.children || []).map(toPresetColumn),
  }
}

// --------------- Composable ---------------
export function usePresetFetcher(api: ApiClient) {
  const presets = ref<ColumnPreset[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const defaultPresetId = computed(() => presets.value[0]?.id || '')

  /** 按 businessId 拉取预设方案列表 */
  async function fetchByBusiness(businessId: string) {
    loading.value = true
    error.value = null
    try {
      const raw = await api<BackendPreset[]>(`/api/businesses/${businessId}/presets`)
      presets.value = raw.map(p => ({
        id: p.id,
        name: p.name,
        description: p.description || '',
        version: p.version || 1,
        groups: (p.groups || []).map(convertGroup),
      }) as ColumnPreset)
    } catch (e: any) {
      error.value = e.message || '加载预设方案失败'
      presets.value = []
    } finally {
      loading.value = false
    }
  }

  return { presets, loading, error, defaultPresetId, fetchByBusiness }
}
