<template>
  <a-modal
    :open="visible"
    title="列自定义"
    width="800px"
    :mask-closable="false"
    @cancel="$emit('cancel')"
  >
    <div class="configurator">
      <!-- 快捷操作 -->
      <div class="configurator__quick">
        <a-space>
          <a-button size="small" @click="showAll">全部显示</a-button>
          <a-button size="small" @click="hideAllOptional">隐藏可选列</a-button>
          <a-button size="small" @click="resetDraft">
            <ReloadOutlined /> 恢复默认
          </a-button>
        </a-space>
      </div>

      <!-- 按分组展示所有列 -->
      <div v-for="group of draftGroups" :key="group.id" class="configurator__group">
        <div class="configurator__group-header">
          <span class="configurator__group-label">{{ group.label }}</span>
          <span class="configurator__group-count">{{ group.children.length }} 列</span>
        </div>
        <div class="configurator__list">
          <div
            v-for="col of group.children"
            :key="col.id"
            class="configurator__item"
            :class="{
              'configurator__item--hidden': !col.visible,
              'configurator__item--locked': col.locked,
            }"
          >
            <!-- 可见性开关 -->
            <a-switch
              size="small"
              :checked="col.visible"
              :disabled="col.locked"
              @change="(val: boolean) => setCol(col.id, { visible: val })"
            />

            <!-- 列标签 -->
            <span class="configurator__col-label">
              <LockOutlined v-if="col.locked" style="font-size:11px;margin-right:2px" />
              {{ col.label }}
            </span>

            <!-- 字段名 -->
            <span class="configurator__col-field">{{ col.field }}</span>

            <!-- 宽度 -->
            <a-input-number
              size="small"
              :value="col.width"
              :min="col.minWidth || 40"
              :max="col.maxWidth || 600"
              :step="10"
              style="width: 80px"
              @change="(val: number | null) => val != null && setCol(col.id, { width: val })"
            />
            <span class="configurator__unit">px</span>

            <!-- 固定列 -->
            <a-radio-group
              size="small"
              :value="col.pinned || 'none'"
              :disabled="col.locked || col.allowPin === false || !col.visible"
              button-style="solid"
              @change="(e: any) => setCol(col.id, { pinned: e.target.value === 'none' ? false : e.target.value })"
            >
              <a-radio-button value="none">无</a-radio-button>
              <a-radio-button value="left">左</a-radio-button>
              <a-radio-button value="right">右</a-radio-button>
            </a-radio-group>

            <!-- 锁定标识 -->
            <a-tag v-if="col.locked" color="blue" style="font-size:10px">锁定</a-tag>

            <!-- 约束标签 -->
            <span class="configurator__constraints">
              <a-tag v-if="col.allowHide === false" style="font-size:10px">不可隐藏</a-tag>
              <a-tag v-if="col.allowSort === false" style="font-size:10px">不可排序</a-tag>
              <a-tag v-if="col.allowPin === false" style="font-size:10px">不可固定</a-tag>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底栏：方案选择 + 恢复 + 取消/保存 -->
    <template #footer>
      <div class="configurator__footer">
        <div class="configurator__footer-left">
          <span class="configurator__footer-label">保存到</span>
          <a-select
            v-model:value="saveTargetPresetId"
            style="width: 180px"
            size="small"
          >
            <a-select-option
              v-for="p in presetList"
              :key="p.id"
              :value="p.id"
            >
              {{ p.name }}
            </a-select-option>
          </a-select>
          <a-popconfirm
            title="确定恢复？"
            :description="'将清除「' + getViewingPresetName() + '」的所有自定义列配置，恢复到预设默认值。'"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleRestore"
          >
            <a-button size="small" danger>
              <ReloadOutlined /> 恢复
            </a-button>
          </a-popconfirm>
          <span v-if="errorMsg" class="configurator__error">{{ errorMsg }}</span>
        </div>
        <div class="configurator__footer-right">
          <a-button @click="$emit('cancel')">取消</a-button>
          <a-button type="primary" :loading="saving" @click="handleSave">保存列设置</a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { ColumnPreset } from './index'
import { ReloadOutlined, LockOutlined } from '@ant-design/icons-vue'

const props = defineProps<{
  visible: boolean
  preset: ColumnPreset | null
  resolvedColumns: any[]
  presetList: ColumnPreset[]
  selectedPresetId?: string
  /** 后端地址，例如 http://localhost:8080 */
  baseUrl: string
  /** 用户名 */
  user: string
  /** 密码 */
  password: string
  /** 业务标识 */
  business: string
}>()

const emit = defineEmits<{
  cancel: []
  saved: [payload: { columns: any[]; targetPresetId: string; columnOverrides: Record<string, any> }]
  restored: [payload: { targetPresetId: string }]
}>()

// -------------------------------------------------------
// 草稿状态
// -------------------------------------------------------

interface DraftCol {
  id: string; label: string; field: string; type: string
  width: number; minWidth?: number | null; maxWidth?: number | null
  visible: boolean; pinned?: string | false
  locked?: boolean
  allowHide?: boolean; allowSort?: boolean; allowPin?: boolean
  align?: string
}

interface DraftGroup {
  id: string; label: string
  children: DraftCol[]
}

const draftGroups = ref<DraftGroup[]>([])
const saveTargetPresetId = ref('')
const saving = ref(false)
const errorMsg = ref('')

// -------------------------------------------------------
// API 请求（独立获取 token）
// -------------------------------------------------------
const localToken = ref('')

/** 独立调用登录接口获取 token */
async function getToken(): Promise<string> {
  if (localToken.value) return localToken.value
  const resp = await fetch(`${props.baseUrl}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: props.user, password: props.password }),
  })
  const json = await resp.json()
  if (json.code !== 200) throw new Error(json.message || '获取token失败')
  localToken.value = json.data.token
  return localToken.value
}

async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const token = await getToken()
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const resp = await fetch(`${props.baseUrl}${path}`, { ...options, headers })
  if (!resp.ok) {
    const text = await resp.text().catch(() => '')
    throw new Error(`[${resp.status}] ${text || resp.statusText}`)
  }
  const json = await resp.json()
  if (json?.data !== undefined) return json.data as T
  return json as T
}

function buildDraft() {
  if (!props.preset) { draftGroups.value = []; return }
  const resMap = new Map<string, any>(props.resolvedColumns.map((c: any) => [c.id, c]))

  draftGroups.value = props.preset.groups.map(group => ({
    id: group.id,
    label: group.label,
    children: group.children.map(col => {
      const resolved = resMap.get(col.id)
      const c = resolved || col
      return {
        id: c.id, label: c.label, field: c.field, type: c.type,
        width: c.width ?? 120, minWidth: c.minWidth ?? null, maxWidth: c.maxWidth ?? null,
        visible: c.visible ?? true, pinned: (c.pinned as any) || false,
        locked: c.locked ?? false,
        allowHide: c.allowHide ?? true, allowSort: c.allowSort ?? true, allowPin: c.allowPin ?? true,
        align: c.align || 'left',
      }
    }),
  }))

  // 默认保存到当前选中的预设
  saveTargetPresetId.value = props.selectedPresetId || props.presetList[0]?.id || ''
}

// 弹窗打开时初始化草稿
watch(() => props.visible, (v) => {
  if (v) buildDraft()
})

function setCol(id: string, patch: Partial<DraftCol>) {
  for (const g of draftGroups.value) {
    const idx = g.children.findIndex(c => c.id === id)
    if (idx >= 0) {
      g.children[idx] = { ...g.children[idx], ...patch } as DraftCol
      break
    }
  }
}

function getFlatDraft(): any[] {
  const flat: any[] = []
  let order = 0
  for (const g of draftGroups.value) {
    for (const c of g.children) {
      flat.push({ ...c, order })
      order++
    }
  }
  return flat
}

function getViewingPresetName(): string {
  return props.presetList.find((p: ColumnPreset) => p.id === (props.selectedPresetId || props.presetList[0]?.id))?.name || '该方案'
}

function buildOverridesFromDraft(columns: any[]): Record<string, any> {
  const columnOverrides: Record<string, any> = {}
  for (const col of columns) {
    const item: any = {}
    if (col.order != null) item.order = col.order
    if (col.width != null) item.width = col.width
    if (col.visible != null) item.visible = col.visible
    if (col.pinned) item.pinned = col.pinned
    columnOverrides[col.id] = item
  }
  return columnOverrides
}

async function handleSave() {
  const columns = getFlatDraft()
  const targetPresetId = saveTargetPresetId.value
  saving.value = true
  errorMsg.value = ''
  try {
    const columnOverrides = buildOverridesFromDraft(columns)

    if (targetPresetId) {
      const body = { presetId: targetPresetId, columnOverrides }
      await api(`/api/overrides/${targetPresetId}`, { method: 'PUT', body: JSON.stringify(body) })
    }

    emit('saved', { columns, targetPresetId, columnOverrides })
  } catch (e: any) {
    errorMsg.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function handleRestore() {
  const targetPresetId = props.selectedPresetId || props.presetList[0]?.id || ''
  saving.value = true
  errorMsg.value = ''
  try {
    if (targetPresetId) {
      const body = { presetId: targetPresetId, columnOverrides: {} }
      await api(`/api/overrides/${targetPresetId}`, { method: 'PUT', body: JSON.stringify(body) })
    }

    emit('restored', { targetPresetId })
  } catch (e: any) {
    errorMsg.value = e.message || '恢复失败'
  } finally {
    saving.value = false
  }
}

function resetDraft() {
  buildDraft()
}

function showAll() {
  for (const g of draftGroups.value) {
    for (const c of g.children) {
      if (!c.visible) c.visible = true
    }
  }
}

function hideAllOptional() {
  for (const g of draftGroups.value) {
    for (const c of g.children) {
      if (c.visible && !c.locked && c.allowHide !== false) {
        c.visible = false
      }
    }
  }
}

// -------------------------------------------------------
// 暴露：编程式保存/恢复（非模态，父组件通过 ref 调用）
// -------------------------------------------------------

async function saveOverrides(params: {
  presetId: string
  columns: any[]
  overrideVersion?: number
}): Promise<{ columnOverrides: Record<string, any> }> {
  if (!params.presetId) throw new Error('未选择方案')

  const columnOverrides = buildOverridesFromDraft(params.columns)
  const body: Record<string, any> = { presetId: params.presetId, columnOverrides }
  if (params.overrideVersion) body.version = params.overrideVersion

  await api(`/api/overrides/${params.presetId}`, { method: 'PUT', body: JSON.stringify(body) })
  return { columnOverrides }
}

async function restoreOverrides(presetId: string): Promise<void> {
  if (!presetId) return
  const body = { presetId, columnOverrides: {} }
  await api(`/api/overrides/${presetId}`, { method: 'PUT', body: JSON.stringify(body) })
}

defineExpose({ saveOverrides, restoreOverrides })
</script>

<style scoped>
.configurator {
  max-height: 50vh;
  overflow-y: auto;
}

.configurator__quick {
  margin-bottom: 12px;
}

/* 分组 */
.configurator__group {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 12px;
  overflow: hidden;
}
.configurator__group:last-child {
  margin-bottom: 0;
}

.configurator__group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  background: #f5f5f5;
  border-bottom: 1px solid #f0f0f0;
}

.configurator__group-label {
  font-weight: 500;
  font-size: 13px;
}

.configurator__group-count {
  font-size: 11px;
  color: #999;
}

/* 列行 */
.configurator__list {
  padding: 4px 0;
}

.configurator__item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  transition: background 0.15s;
}
.configurator__item:hover {
  background: #f0f5ff;
}
.configurator__item--hidden {
  opacity: 0.4;
}
.configurator__item--hidden:hover {
  opacity: 1;
}

.configurator__col-label {
  font-size: 13px;
  font-weight: 500;
  min-width: 90px;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.configurator__col-field {
  font-size: 11px;
  color: #999;
  font-family: monospace;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.configurator__unit {
  font-size: 11px;
  color: #999;
}

.configurator__constraints {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

/* 底栏 */
.configurator__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.configurator__footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.configurator__footer-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.configurator__footer-right {
  display: flex;
  gap: 8px;
}

.configurator__error {
  font-size: 12px;
  color: #ff4d4f;
  margin-left: 8px;
}
</style>
