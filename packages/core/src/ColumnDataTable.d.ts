import type { DefineComponent } from 'vue'

declare const ColumnDataTable: DefineComponent<
  {
    serverUrl: string
    token: string
    user: string
    password: string
    systemId: string
    businessId: string
    data: Record<string, unknown>[]
    defaultPresetId?: string
    rowKey?: string
  },
  {
    saveOverrides: () => Promise<void>
    resetColumns: () => void
    refresh: () => Promise<void>
    resolvedColumns: any[]
    rawPreset: any
    selectedPresetId: string
  }
>

export default ColumnDataTable
