# 列自定义管理系统 — 设计文档

日期：2026-07-04

## 概述

企业级低代码平台的列自定义管理系统，提供列元数据定义 + 运行时列配置双重能力。管理员配置列模板，用户可在此基础上拖拽自定义。

## 技术栈

- **核心层**: TypeScript (零 UI 依赖)
- **UI 层**: Vue 3 + TypeScript
- **构建**: pnpm monorepo (workspaces)
- **测试**: vitest + @vue/test-utils + Playwright

## 架构

```
┌─────────────────────────────────────────────────────────┐
│                    @cds/ui (Vue 3)                      │
│  ColumnConfigurator | ColumnTable | ColumnPresetPicker  │
│  composables: useColumnConfig, useDragSort              │
├─────────────────────────────────────────────────────────┤
│                    @cds/core (TS)                       │
│  types: ColumnSchema, ColumnPreset, ColumnGroup         │
│  merge: mergeColumnConfig (模板 + 用户覆盖)              │
│  validate: schema 校验 + 运行时校验                      │
├─────────────────────────────────────────────────────────┤
│                   @cds/storage (TS)                     │
│  ColumnStorageAdapter interface                         │
│  LocalStorageAdapter | ApiStorageAdapter                │
│  OfflineSyncManager                                     │
└─────────────────────────────────────────────────────────┘
```

## 核心数据模型

### ColumnSchema

```
ColumnSchema
├── id: string                    ← 列唯一标识
├── label: string                 ← 显示名称
├── field: string                 ← 数据字段路径（支持嵌套）
├── type: ColumnType              ← text | number | date | select | boolean | custom
├── width: number                 ← 默认宽度 (px)
├── sortable: boolean             ← 可排序
├── filterable: boolean           ← 可筛选
├── resizable: boolean            ← 可调整宽度
├── visible: boolean              ← 默认可见
├── pinned: 'left' | 'right' | false  ← 固定列
├── locked: boolean               ← 管理员锁定（用户不可修改）
├── validation: ColumnValidation[] ← 校验规则
│   ├── type: required | pattern | range | custom
│   ├── message: string
│   └── params: Record<string, unknown>
├── options?: SelectOption[]      ← type=select 时的选项
├── defaultValue?: unknown        ← 默认值
├── meta: Record<string, unknown> ← 扩展点
```

### ColumnGroup

列分组，支持嵌套。

```
ColumnGroup
├── id: string
├── label: string
├── children: (ColumnGroup | ColumnSchema)[]
├── collapsed?: boolean           ← 默认折叠状态
```

### ColumnPreset

管理员配置的完整列方案。

```
ColumnPreset
├── id: string
├── name: string
├── description?: string
├── groups: ColumnGroup[]
├── allowCrossGroup?: boolean     ← 是否允许用户跨组移列
├── version: number
└── createdAt / updatedAt
```

### ColumnPresetOverride

用户在模板基础上的增量覆盖。

```
ColumnPresetOverride
├── presetId: string
├── columnOverrides: Record<string, Partial<ColumnOverride>>
│   ColumnOverride: { order?, width?, visible?, pinned? }
├── version: number
└── updatedAt: number
```

## 模板合并算法

### 规则

1. **增量合并**: 以 `column.id` 为 key，用户覆盖模板的同名字段
2. **分组继承**: 列排序在分组内生效，默认不可跨组
3. **受保护字段**: `type`, `field`, `validation` 不可被用户覆盖
4. **冻结列**: `locked: true` 的列用户不可隐藏或移除
5. **新增列**: 用户不可新增模板中不存在的列

### 输出

```
mergeColumnConfig(preset, userOverride): ResolvedColumn[]
```

ResolvedColumn 是合并后的扁平列数组，下游组件直接使用。

## 存储层

### ColumnStorageAdapter 接口

```typescript
interface ColumnStorageAdapter {
  load(presetId: string, userId: string): Promise<ColumnPresetOverride | null>
  save(presetId: string, userId: string, override: ColumnPresetOverride): Promise<void>
  listPresets(): Promise<ColumnPreset[]>
  getPreset(id: string): Promise<ColumnPreset | null>
}
```

### 内置实现

- **LocalStorageAdapter**: 键名 `cds:{presetId}:{userId}`，离线快速读写
- **ApiStorageAdapter**: 通过 REST API，需要注入 httpClient

### 同步策略

在线时优先 ApiAdapter + 写入 LocalAdapter 做缓存；离线时使用 LocalAdapter；联机后推送增量。

## UI 组件

### ColumnConfigurator

列配置面板，左右两栏布局。

- **Props**: `modelValue` (ColumnPresetOverride), `preset` (ColumnPreset)
- **Emits**: `update:modelValue`
- **功能**: 可用/已选列列表、拖拽排序、宽度输入、固定选项、搜索过滤、重置

### ColumnTable

渲染列头，接收 `ResolvedColumn[]`。

- 支持列宽拖拽调整
- 支持排序触发
- 不绑定具体表格实现（slot / render prop 接入 el-table 等）

### ColumnPresetPicker

预设方案切换器。

- 管理员的多个预设方案列表
- 用户一键切换

## 错误处理

| 场景 | 策略 |
|------|------|
| 后端加载预设失败 | 降级到 localStorage 缓存 |
| 缓存也不存在 | 使用内嵌默认预设（hardcoded fallback） |
| 并发写冲突 | last-write-wins + version 字段检测 |
| 超过 50 列 | ColumnConfigurator 启用虚拟滚动 |

## 测试策略

| 包 | 测试类型 | 覆盖目标 |
|----|----------|----------|
| @cds/core | 单元测试 (vitest) | 核心类型、合并算法、校验逻辑，覆盖率 > 95% |
| @cds/storage | 单元 + 集成测试 | Adapter 接口契约、localStorage mock、离线/在线切换 |
| @cds/ui | 组件测试 (VTU + vitest) | 组件渲染、事件、v-model 绑定 |
| @cds/ui | E2E (Playwright) | 拖拽完整流程、跨组件联调 |

## 项目结构

```
column-define-system/
├── packages/
│   ├── core/
│   │   ├── src/
│   │   │   ├── types/         → ColumnSchema, ColumnPreset, ColumnGroup
│   │   │   ├── merge/         → mergeColumnConfig
│   │   │   ├── validate/      → schema + runtime validation
│   │   │   └── utils/         → 序列化、深比较
│   │   └── __tests__/
│   ├── ui/
│   │   ├── src/
│   │   │   ├── components/    → ColumnConfigurator, ColumnTable, ColumnPresetPicker
│   │   │   ├── composables/   → useColumnConfig, useDragSort
│   │   │   └── styles/
│   │   └── __tests__/
│   └── storage/
│       ├── src/
│       │   ├── adapters/      → LocalStorageAdapter, ApiStorageAdapter
│       │   └── sync/          → OfflineSyncManager
│       └── __tests__/
├── demo/                      → Vite dev playground
├── docs/
│   └── plans/
└── package.json               → pnpm workspaces
```
