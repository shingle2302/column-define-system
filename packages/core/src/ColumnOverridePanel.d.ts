import type { DefineComponent } from 'vue'

declare const ColumnOverridePanel: DefineComponent<{
  serverUrl?: string
  token?: string
  role?: string
  userId?: string
}>

export default ColumnOverridePanel
