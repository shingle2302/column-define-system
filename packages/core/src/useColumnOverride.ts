/**
 * useColumnOverride — 列覆盖管理 composable
 *
 * 管理用户级和角色级的列自定义覆盖。
 * 提供查询、创建、编辑、删除功能。
 *
 * 不依赖 pinia / vue-router，不依赖具体 auth 实现。
 * 调用方需传入符合 ApiClient 接口的 api 函数。
 */

import { ref } from 'vue'
import type { ApiClient } from './types'

// --------------- 类型 ---------------
export interface ColumnOverride {
  id: number
  presetId: string
  presetName?: string
  userId?: string
  userName?: string
  role?: string
  columnId: string
  columnLabel?: string
  columnField?: string
  overrideOrder: number | null
  overrideWidth: number | null
  overrideVisible: boolean | null
  overridePinned: string | null
  updatedAt?: string
}

export interface OverrideFormData {
  presetId: string
  userId?: string
  role?: string
  columnId: string
  overrideOrder: number | null
  overrideWidth: number | null
  overrideVisible: boolean | null
  overridePinned: string | null
}

// --------------- Composable ---------------
/**
 * @param api - 符合 ApiClient 接口的请求函数
 * @param type 'user' = 用户级覆盖, 'role' = 角色级覆盖
 */
export function useColumnOverride(api: ApiClient, type: 'user' | 'role' = 'user') {
  const isUserOverride = type === 'user'

  const overrides = ref<ColumnOverride[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const filterPresetId = ref<string | undefined>(undefined)
  const saving = ref(false)

  const apiPath = isUserOverride ? '/api/admin/overrides' : '/api/admin/role-presets'

  /** 查询覆盖列表 */
  async function fetchOverrides(presetId?: string) {
    loading.value = true
    error.value = null
    const pid = presetId || filterPresetId.value
    try {
      const params = pid ? `?presetId=${encodeURIComponent(pid)}` : ''
      overrides.value = await api<ColumnOverride[]>(`${apiPath}${params}`)
    } catch (e: any) {
      error.value = e.message || '加载覆盖数据失败'
      overrides.value = []
    } finally {
      loading.value = false
    }
  }

  /** 保存（新增或更新） */
  async function saveOverride(data: OverrideFormData): Promise<void> {
    saving.value = true
    error.value = null
    try {
      await api(apiPath, {
        method: 'POST',
        body: JSON.stringify(data),
      })
    } catch (e: any) {
      error.value = e.message || '保存失败'
      throw e
    } finally {
      saving.value = false
    }
  }

  /** 删除 */
  async function deleteOverride(id: number): Promise<void> {
    try {
      await api(`${apiPath}/${id}`, { method: 'DELETE' })
      overrides.value = overrides.value.filter((o: ColumnOverride) => o.id !== id)
    } catch (e: any) {
      error.value = e.message || '删除失败'
      throw e
    }
  }

  /** 将覆盖数据应用到列定义上（返回合并后的列） */
  function applyOverrides(columns: any[]): any[] {
    if (overrides.value.length === 0) return columns

    const overrideMap = new Map<string, ColumnOverride>()
    for (const o of overrides.value) {
      if (o.columnId) overrideMap.set(o.columnId, o)
    }

    return columns.map(col => {
      const override = overrideMap.get(col.id)
      if (!override) return col
      return {
        ...col,
        ...(override.overrideOrder != null ? { sortOrder: override.overrideOrder } : {}),
        ...(override.overrideWidth != null ? { width: override.overrideWidth } : {}),
        ...(override.overrideVisible != null ? { visible: override.overrideVisible } : {}),
        ...(override.overridePinned != null ? { pinned: override.overridePinned } : {}),
      }
    })
  }

  return {
    overrides,
    loading,
    saving,
    error,
    filterPresetId,
    fetchOverrides,
    saveOverride,
    deleteOverride,
    applyOverrides,
  }
}
