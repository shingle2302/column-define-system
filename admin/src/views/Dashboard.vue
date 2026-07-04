<template>
  <div class="dashboard">
    <a-row :gutter="16" class="dashboard-stats">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card--blue" :loading="loading">
          <a-statistic title="接入系统" :value="stats.systems" suffix="个">
            <template #prefix><AppstoreOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card--green" :loading="loading">
          <a-statistic title="业务模块" :value="stats.businesses" suffix="个">
            <template #prefix><PartitionOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card--orange" :loading="loading">
          <a-statistic title="预设方案" :value="stats.presets" suffix="套">
            <template #prefix><GoldOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card--purple" :loading="loading">
          <a-statistic title="列定义" :value="stats.columns" suffix="个">
            <template #prefix><TableOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="12">
        <a-card title="系统概览" :bordered="false" class="admin-card" :loading="loading">
          <a-table :columns="sysColumns" :dataSource="systems" :pagination="false" size="small" rowKey="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'active' ? 'green' : 'default'">{{ record.status === 'active' ? '启用' : '停用' }}</a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="最近预设" :bordered="false" class="admin-card" :loading="loading">
          <a-table :columns="presetColumns" :dataSource="presets" :pagination="false" size="small" rowKey="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'sysBiz'">
                <span style="color: #999; font-size: 12px">{{ record.systemName }} / {{ record.businessName || '-' }}</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { AppstoreOutlined, PartitionOutlined, GoldOutlined, TableOutlined } from '@ant-design/icons-vue'
import { api, auth } from '../stores/auth'

const loading = ref(true)
const stats = ref({ systems: 0, businesses: 0, presets: 0, columns: 0 })
const systems = ref<any[]>([])
const presets = ref<any[]>([])

const sysColumns = [
  { title: '名称', dataIndex: 'name' },
  { title: '编码', dataIndex: 'code', width: 100 },
  { title: '状态', key: 'status', width: 80 },
]
const presetColumns = [
  { title: '名称', dataIndex: 'name' },
  { title: '所属', key: 'sysBiz', width: 150 },
]

onMounted(async () => {
  console.log('[Dashboard] auth state:', {
    token: auth.token ? auth.token.substring(0, 20) + '...' : '(empty)',
    username: auth.username,
    role: auth.role,
    systemId: auth.systemId,
  })

  // 分别加载各数据，避免一个失败全部归零
  const loadStats = async () => {
    try {
      const dashStats = await api<any>('/api/admin/stats')
      stats.value = {
        systems: dashStats.systems || 0,
        businesses: dashStats.businesses || 0,
        presets: dashStats.presets || 0,
        columns: dashStats.columns || 0,
      }
    } catch (e: any) {
      console.error('加载仪表盘统计失败:', e)
    }
  }

  const loadSystems = async () => {
    try {
      const sysList = await api<any[]>('/api/admin/systems')
      systems.value = sysList.slice(0, 5)
    } catch (e: any) {
      console.error('加载系统列表失败:', e)
    }
  }

  const loadPresets = async () => {
    try {
      const presetList = await api<any[]>('/api/admin/presets')
      presets.value = presetList.slice(0, 5)
    } catch (e: any) {
      console.error('加载预设列表失败:', e)
    }
  }

  await Promise.allSettled([loadStats(), loadSystems(), loadPresets()])
  loading.value = false
})
</script>

<style scoped>
.dashboard-stats .stat-card {
  border-radius: 10px;
  overflow: hidden;
  border: none;
}
.stat-card :deep(.ant-card-body) { padding: 22px 24px; }
.stat-card--blue   { background: linear-gradient(135deg, #e6f4ff, #bae0ff); }
.stat-card--green  { background: linear-gradient(135deg, #f6ffed, #d9f8be); }
.stat-card--orange { background: linear-gradient(135deg, #fff7e6, #ffd591); }
.stat-card--purple { background: linear-gradient(135deg, #f9f0ff, #d3adf7); }
</style>
