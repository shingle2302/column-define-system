/** 列预设组 —— 一组列 */
export interface ColumnGroup {
  id: string
  label: string
  collapsed?: boolean
  children: ColumnDefItem[]
}

/** 单个列定义 */
export interface ColumnDefItem {
  id: string
  label: string
  field: string
  type: string
  width: number
  minWidth?: number | null
  maxWidth?: number | null
  sortable?: boolean
  filterable?: boolean
  resizable?: boolean
  visible: boolean
  pinned?: string | null
  locked?: boolean
  allowHide?: boolean
  allowSort?: boolean
  allowResize?: boolean
  allowPin?: boolean
  align?: string
  defaultValue?: string
  optionsJson?: string
  sortOrder?: number
  order?: number
}

/** 完整的列预设方案 */
export interface ColumnPreset {
  id: string
  name: string
  description?: string
  version?: number
  groups: ColumnGroup[]
}

// --------------- 通用类型 ---------------
export type { ApiClient } from './types'

// --------------- Composables ---------------
export { useColumnDef } from './useColumnDef'
export type { ColumnDef, GroupDef, PresetDef, SystemDef } from './useColumnDef'
export { toPresetColumn, toPresetGroup, toColumnPreset } from './useColumnDef'

export { useColumnOverride } from './useColumnOverride'
export type { ColumnOverride as OverrideItem, OverrideFormData } from './useColumnOverride'

export { usePresetFetcher } from './usePresetFetcher'

// --------------- UI Stubs ---------------
export { ColumnTable, ColumnPresetPicker, useColumnConfig } from './uiStubs'
