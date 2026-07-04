import type { DefineComponent } from 'vue'

declare const ColumnConfigurator: DefineComponent<{
  visible: boolean
  preset: import('./index').ColumnPreset | null
  resolvedColumns: any[]
  presetList: import('./index').ColumnPreset[]
  selectedPresetId?: string
  baseUrl: string
  user: string
  password: string
  business: string
}, {}, any>

export default ColumnConfigurator
