<template>
  <div class="page">
    <div class="admin-toolbar">
      <h3>列定义管理</h3>
      <p style="color: #999; font-size: 13px">选择一个预设方案，管理其分组和列定义</p>
    </div>

    <!-- 步骤 1: 选择预设 -->
    <a-card :bordered="false" class="admin-card" title="选择预设方案">
      <a-space size="large">
        <a-select v-model:value="filterSystem" placeholder="按系统筛选" allowClear style="width: 180px" @change="loadPresets">
          <a-select-option v-for="s in systems" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
        </a-select>
        <a-select v-model:value="selectedPresetId" placeholder="选择预设方案" style="width: 260px" @change="loadGroups" :loading="presetLoading">
          <a-select-option v-for="p in presets" :key="p.id" :value="p.id">
            {{ p.name }} <span style="color:#999;font-size:11px">({{ p.systemName }})</span>
          </a-select-option>
        </a-select>
      </a-space>
    </a-card>

    <!-- 步骤 2: 分组 + 列 -->
    <template v-if="selectedPresetId">
      <div v-for="group in groups" :key="group.id" class="column-group-block">
        <div class="column-group-header">
          <span class="column-group-label">
            <FolderOutlined /> {{ group.label }}
          </span>
          <a-space v-if="auth.role !== 'viewer'">
            <a-button type="primary" size="small" @click="onAddColumn(group)">
              <PlusOutlined /> 加列
            </a-button>
            <a-button size="small" @click="onEditGroup(group)">
              <EditOutlined />
            </a-button>
            <a-popconfirm title="删除分组将同时删除其下所有列定义" @confirm="onDeleteGroup(group.id)">
              <a-button size="small" danger>
                <DeleteOutlined />
              </a-button>
            </a-popconfirm>
          </a-space>
        </div>

        <a-table
          :columns="colColumns"
          :dataSource="group.columns || []"
          :pagination="false"
          rowKey="id"
          size="small"
          :loading="groupLoading"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'visible'">
              <a-tag :color="record.visible ? 'green' : 'default'">{{ record.visible ? '显示' : '隐藏' }}</a-tag>
            </template>
            <template v-if="column.key === 'constraints'">
              <a-space :size="2" wrap>
                <a-tag v-if="record.locked" color="red" style="font-size: 10px">锁</a-tag>
                <a-tag v-if="record.allowHide === false" color="default" style="font-size: 10px">禁止隐藏</a-tag>
                <a-tag v-if="record.allowSort === false" color="default" style="font-size: 10px">禁止排序</a-tag>
              </a-space>
            </template>
            <template v-if="column.key === 'action'">
              <a-space v-if="auth.role !== 'viewer'">
                <a @click="onEditColumn(group, record)">编辑</a>
                <a-popconfirm title="确认删除此列定义？" @confirm="onDeleteColumn(record.id)">
                  <a style="color: #ff4d4f">删除</a>
                </a-popconfirm>
              </a-space>
              <span v-else style="color: #ccc">-</span>
            </template>
          </template>
        </a-table>
      </div>

      <a-button v-if="auth.role !== 'viewer'" type="dashed" block style="margin-top: 16px" @click="onAddGroup">
        <PlusOutlined /> 新增分组
      </a-button>
    </template>

    <a-empty v-else description="请先选择一个预设方案" style="margin-top: 60px" />

    <!-- 分组编辑弹窗 -->
    <a-modal v-model:visible="groupVisible" :title="groupEditing ? '编辑分组' : '新增分组'" @ok="saveGroup" width="400px">
      <a-form layout="vertical">
        <a-form-item label="分组标签" required>
          <a-input v-model:value="groupForm.label" placeholder="如：基本信息" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="groupForm.sortOrder" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="默认折叠">
          <a-switch v-model:checked="groupForm.collapsed" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 列定义弹窗 -->
    <a-modal v-model:visible="colVisible" :title="colEditing ? '编辑列定义' : '新增列定义'" @ok="saveColumn" width="680px">
      <!-- 复用 AdminPanel 中已有的列编辑表单，但这里简化以保持独立 -->
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="列标签" required>
              <a-input v-model:value="colForm.label" placeholder="如：客户名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="字段名" required>
              <a-input v-model:value="colForm.field" placeholder="如：name" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="数据类型">
              <a-select v-model:value="colForm.type">
                <a-select-option value="text">text</a-select-option>
                <a-select-option value="number">number</a-select-option>
                <a-select-option value="date">date</a-select-option>
                <a-select-option value="datetime">datetime</a-select-option>
                <a-select-option value="boolean">boolean</a-select-option>
                <a-select-option value="select">select</a-select-option>
                <a-select-option value="tag">tag</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="4"><a-form-item label="宽度"><a-input-number v-model:value="colForm.width" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="4"><a-form-item label="最小宽"><a-input-number v-model:value="colForm.minWidth" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="4"><a-form-item label="最大宽"><a-input-number v-model:value="colForm.maxWidth" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="4"><a-form-item label="排序"><a-input-number v-model:value="colForm.sortOrder" :min="0" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-divider style="margin: 8px 0">显示</a-divider>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="默认可见"><a-switch v-model:checked="colForm.visible" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="固定列">
              <a-select v-model:value="colForm.pinned" placeholder="无" allowClear>
                <a-select-option value="left">左侧</a-select-option>
                <a-select-option value="right">右侧</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="对齐">
              <a-select v-model:value="colForm.align">
                <a-select-option value="left">居左</a-select-option>
                <a-select-option value="center">居中</a-select-option>
                <a-select-option value="right">居右</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="排序"><a-switch v-model:checked="colForm.sortable" size="small" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="筛选"><a-switch v-model:checked="colForm.filterable" size="small" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="调宽"><a-switch v-model:checked="colForm.resizable" size="small" /></a-form-item></a-col>
        </a-row>
        <a-divider style="margin: 8px 0">约束</a-divider>
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="锁定"><a-switch v-model:checked="colForm.locked" size="small" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="允许隐藏"><a-switch v-model:checked="colForm.allowHide" size="small" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="允许排序"><a-switch v-model:checked="colForm.allowSort" size="small" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="允许固定"><a-switch v-model:checked="colForm.allowPin" size="small" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="默认值">
          <a-input v-model:value="colForm.defaultValue" placeholder="列默认值" />
        </a-form-item>
        <a-form-item label="选项(JSON)">
          <a-textarea v-model:value="colForm.optionsJson" placeholder='[{"label":"VIP","value":"vip"}]' :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, FolderOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { api, auth } from '../stores/auth'

const systems = ref<any[]>([])
const filterSystem = ref<string | undefined>(undefined)
const presets = ref<any[]>([])
const presetLoading = ref(false)
const selectedPresetId = ref<string | null>(null)
const groups = ref<any[]>([])
const groupLoading = ref(false)

const colColumns = [
  { title: '标签', dataIndex: 'label', width: 110 },
  { title: '字段', dataIndex: 'field', width: 100 },
  { title: '类型', dataIndex: 'type', width: 70 },
  { title: '对齐', dataIndex: 'align', width: 60 },
  { title: '宽', dataIndex: 'width', width: 50 },
  { title: '可见', key: 'visible', width: 60 },
  { title: '约束', key: 'constraints', width: 150 },
  { title: '操作', key: 'action', width: 120 },
]

async function loadPresets() {
  presetLoading.value = true
  try {
    const params = new URLSearchParams()
    if (filterSystem.value) params.set('systemId', filterSystem.value)
    presets.value = await api('/api/admin/presets' + (params.toString() ? '?' + params.toString() : ''))
  } catch (e: any) { message.error('加载预设失败: ' + e.message) }
  finally { presetLoading.value = false }
}

async function loadGroups() {
  if (!selectedPresetId.value) return
  groupLoading.value = true
  try {
    const groupList = await api<any[]>(`/api/admin/presets/${selectedPresetId.value}/groups`)
    const enriched = await Promise.all(groupList.map(async g => {
      const cols = await api<any[]>(`/api/admin/groups/${g.id}/columns`)
      return { ...g, columns: cols }
    }))
    groups.value = enriched
  } catch (e: any) { message.error('加载分组失败: ' + e.message); groups.value = [] }
  finally { groupLoading.value = false }
}

// ---- Group ----
const groupVisible = ref(false)
const groupForm = ref({ label: '', sortOrder: 0, collapsed: false })
const groupEditing = ref<any>(null)
function onAddGroup() {
  groupEditing.value = null
  groupForm.value = { label: '', sortOrder: groups.value.length, collapsed: false }
  groupVisible.value = true
}
function onEditGroup(r: any) {
  groupEditing.value = r
  groupForm.value = { label: r.label, sortOrder: r.sortOrder || 0, collapsed: r.collapsed }
  groupVisible.value = true
}
async function saveGroup() {
  if (!groupForm.value.label) { message.warning('标签不能为空'); return }
  try {
    if (groupEditing.value) {
      await api(`/api/admin/groups/${groupEditing.value.id}`, { method: 'PUT', body: JSON.stringify(groupForm.value) })
    } else {
      await api(`/api/admin/presets/${selectedPresetId.value}/groups`, { method: 'POST', body: JSON.stringify(groupForm.value) })
    }
    groupVisible.value = false
    await loadGroups()
  } catch (e: any) { message.error(e.message) }
}
async function onDeleteGroup(id: string) {
  await api(`/api/admin/groups/${id}`, { method: 'DELETE' })
  message.success('分组已删除')
  await loadGroups()
}

// ---- Column ----
const colVisible = ref(false)
const colForm = ref<any>({})
const colEditing = ref<any>(null)
const colCurrentGroupId = ref<string | null>(null)
function makeColDefault() {
  return { label: '', field: '', type: 'text', width: 150, minWidth: null, maxWidth: null,
    sortable: false, filterable: false, resizable: true, visible: true, pinned: null,
    locked: false, allowHide: true, allowSort: true, allowResize: true, allowPin: true, align: 'left',
    defaultValue: '', optionsJson: '', sortOrder: 0 }
}
function onAddColumn(group: any) {
  colEditing.value = null
  colCurrentGroupId.value = group.id
  colForm.value = { ...makeColDefault(), sortOrder: (group.columns?.length || 0) }
  colVisible.value = true
}
function onEditColumn(_group: any, record: any) {
  colEditing.value = record
  colCurrentGroupId.value = _group.id
  colForm.value = { ...makeColDefault(), ...record, optionsJson: record.optionsJson || '', defaultValue: record.defaultValue || '' }
  colVisible.value = true
}
async function saveColumn() {
  if (!colForm.value.label || !colForm.value.field) { message.warning('标签和字段名为必填'); return }
  const body = { ...colForm.value }
  if (body.minWidth === 0) body.minWidth = null
  if (body.maxWidth === 0) body.maxWidth = null
  try {
    if (colEditing.value) {
      await api(`/api/admin/columns/${colEditing.value.id}`, { method: 'PUT', body: JSON.stringify(body) })
    } else {
      await api(`/api/admin/groups/${colCurrentGroupId.value}/columns`, { method: 'POST', body: JSON.stringify(body) })
    }
    colVisible.value = false
    await loadGroups()
  } catch (e: any) { message.error(e.message) }
}
async function onDeleteColumn(id: string) {
  await api(`/api/admin/columns/${id}`, { method: 'DELETE' })
  message.success('列定义已删除')
  await loadGroups()
}

onMounted(async () => {
  try { systems.value = await api<any[]>('/api/admin/systems') } catch { /* ok */ }
  await loadPresets()
})
</script>

<style scoped>
.column-group-block {
  margin-top: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}
.column-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.column-group-label {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}
.column-group-block :deep(.ant-table-thead > tr > th) { padding: 6px 8px !important; font-size: 12px; }
.column-group-block :deep(.ant-table-tbody > tr > td)  { padding: 6px 8px !important; font-size: 12px; }
</style>
