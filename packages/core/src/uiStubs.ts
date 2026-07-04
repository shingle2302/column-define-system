/**
 * UI Stubs — 轻量级表格/预设选择器/列配置 composable
 *
 * 提供 minima 组件定义，供 ColumnDataTable 直接使用。
 * 这些 stub 不需要 ant-design-vue 等重型依赖。
 */
import { defineComponent, h, type PropType, ref, computed, watch } from 'vue'
import type { ColumnPreset, ColumnDefItem } from './index'

// ----------------------------------------------------------------
// ColumnPresetPicker — 预设方案下拉选择器
// ----------------------------------------------------------------
export const ColumnPresetPicker = defineComponent({
  name: 'ColumnPresetPicker',
  props: {
    presets: { type: Array as PropType<ColumnPreset[]>, required: true },
    modelValue: { type: String, default: '' },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () => h('div', { class: 'cds-preset-picker' }, [
      h('span', { style: 'margin-right:8px;font-size:13px;color:#666' }, '方案:'),
      h('select', {
        value: props.modelValue,
        onChange: (e: Event) => emit('update:modelValue', (e.target as HTMLSelectElement).value),
        style: 'padding:4px 8px;border:1px solid #d9d9d9;border-radius:4px;font-size:13px;min-width:160px',
      }, props.presets.map(p => h('option', { key: p.id, value: p.id }, p.name))),
    ])
  },
})

// ----------------------------------------------------------------
// ColumnTable — 列自定义表格
// ----------------------------------------------------------------
export const ColumnTable = defineComponent({
  name: 'ColumnTable',
  props: {
    columns: { type: Array as PropType<any[]>, default: () => [] },
    data: { type: Array as PropType<Record<string, unknown>[]>, default: () => [] },
    sortBy: { type: String, default: '' },
    sortOrder: { type: String as PropType<'asc' | 'desc'>, default: 'asc' },
    rowKey: { type: String, default: 'id' },
  },
  emits: ['sort'],
  setup(props, { emit, slots }) {
    return () => {
      const visibleCols = (props.columns || []).filter((c: any) => c.visible !== false)
      return h('table', { style: 'width:100%;border-collapse:collapse;font-size:13px' }, [
        h('thead', [
          h('tr', visibleCols.map((col: any) =>
            h('th', {
              key: col.id,
              onClick: () => emit('sort', col.id),
              style: {
                padding: '10px 12px', borderBottom: '2px solid #f0f0f0',
                textAlign: col.align || 'left', cursor: col.sortable ? 'pointer' : 'default',
                fontWeight: 600, background: '#fafafa', whiteSpace: 'nowrap',
                minWidth: col.width ? col.width + 'px' : 'auto',
              },
            }, slots.header ? slots.header({ column: col }) : col.label),
          )),
        ]),
        h('tbody', (props.data || []).map((row: any) =>
          h('tr', { key: row[props.rowKey] || row.id }, visibleCols.map((col: any) =>
            h('td', {
              key: col.id,
              style: { padding: '10px 12px', borderBottom: '1px solid #f5f5f5', textAlign: col.align || 'left' },
            }, slots.cell
              ? slots.cell({ column: col, value: row[col.field], record: row })
              : String(row[col.field] ?? '-'),
            ),
          )),
        )),
      ])
    }
  },
})

// ----------------------------------------------------------------
// useColumnConfig — 列配置 composable
// ----------------------------------------------------------------
export function useColumnConfig(preset: any, _overrides: any) {
  const columns = ref<ColumnDefItem[]>([])

  function syncFromPreset() {
    if (!preset?.value) { columns.value = []; return }
    const flat: ColumnDefItem[] = []
    for (const group of preset.value.groups) {
      for (const child of group.children) {
        flat.push({ ...child })
      }
    }
    columns.value = flat
  }

  watch(() => preset?.value, syncFromPreset, { immediate: true, deep: true })

  const resolvedColumns = computed(() => columns.value)

  function updateColumn(id: string, patch: Partial<ColumnDefItem>) {
    const idx = columns.value.findIndex(c => c.id === id)
    if (idx >= 0) {
      columns.value[idx] = { ...columns.value[idx], ...patch }
    }
  }

  function resetToPreset() {
    syncFromPreset()
  }

  return { resolvedColumns, updateColumn, resetToPreset }
}
