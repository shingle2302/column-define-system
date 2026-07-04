/**
 * @cds/ui — 列自定义 UI 组件库
 *
 * ColumnTable / ColumnPresetPicker / useColumnConfig 已迁移至 @cds/core。
 * 此处仅保留 ColumnConfigurator 存根，并 re-export 核心组件以保持向后兼容。
 */
import { defineComponent, h, type PropType } from 'vue'
import type { ColumnPreset } from '@cds/core'

// Re-export 核心组件
export { ColumnTable, ColumnPresetPicker, useColumnConfig } from '@cds/core'

// ----------------------------------------------------------------
// ColumnConfigurator — 列配置面板（存根）
// ----------------------------------------------------------------
export const ColumnConfigurator = defineComponent({
  name: 'ColumnConfigurator',
  props: {
    preset: { type: Object as PropType<ColumnPreset | null>, default: null },
    resolvedColumns: { type: Array as PropType<any[]>, default: () => [] },
  },
  emits: ['reorder', 'add', 'remove', 'update:width', 'update:pinned', 'reset'],
  setup(props, { emit }) {
    return () => {
      const cols = props.resolvedColumns || []

      return h('div', {
        style: 'background:#fafafa;border:1px solid #f0f0f0;border-radius:8px;padding:12px 16px',
      }, [
        h('div', { style: 'display:flex;align-items:center;gap:8px;flex-wrap:wrap' }, [
          h('strong', { style: 'font-size:13px;margin-right:8px' }, '列配置:'),
          ...cols.map((col: any, idx: number) =>
            h('span', {
              key: col.id,
              style: {
                display: 'inline-flex', alignItems: 'center', gap: '4px',
                padding: '2px 8px', background: '#fff', borderRadius: '4px',
                border: '1px solid #e8e8e8', fontSize: '12px', cursor: 'pointer',
                opacity: col.visible ? 1 : 0.4,
                ...(col.locked ? { borderColor: '#1677ff' } : {}),
              },
              onClick: () => col.visible ? emit('remove', col.id) : emit('add', col.id),
            }, [
              col.locked ? h('span', '🔒 ') : null,
              col.label,
              h('span', { style: 'color:#bbb;font-size:10px' }, col.width + 'px'),
            ]),
          ),
        ]),
        h('div', { style: 'margin-top:8px;display:flex;gap:6px' }, [
          h('button', {
            onClick: () => emit('reset'),
            style: 'padding:2px 10px;border:1px solid #d9d9d9;border-radius:4px;background:#fff;cursor:pointer;font-size:12px',
          }, '恢复默认'),
        ]),
        h('div', { style: 'margin-top:4px;color:#bbb;font-size:11px' }, '拖拽排序/宽度调整请在 admin 控制台使用完整组件'),
      ])
    }
  },
})
