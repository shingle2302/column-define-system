<template>
  <a-layout class="admin-layout">
    <!-- 侧边栏 -->
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible :width="220" class="admin-sider">
      <router-link to="/dashboard" class="admin-logo">
        <img v-if="collapsed" src="data:image/svg+xml,..." style="display:none" />
        <TableOutlined class="admin-logo-icon" />
        <span v-if="!collapsed" class="admin-logo-text">列自定义系统</span>
      </router-link>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        theme="dark"
        class="admin-menu"
        @click="onMenuClick"
      >
        <a-menu-item key="dashboard">
          <template #icon><DashboardOutlined /></template>
          <span>仪表盘</span>
        </a-menu-item>

        <!-- 管理员菜单 -->
        <template v-if="auth.role === 'admin'">
          <a-menu-item-group title="管理功能">
            <a-menu-item key="systems">
              <template #icon><AppstoreOutlined /></template>
              <span>系统管理</span>
            </a-menu-item>
            <a-menu-item key="businesses">
              <template #icon><PartitionOutlined /></template>
              <span>业务管理</span>
            </a-menu-item>
            <a-menu-item key="presets">
              <template #icon><GoldOutlined /></template>
              <span>预设管理</span>
            </a-menu-item>
            <a-menu-item key="columns">
              <template #icon><TableOutlined /></template>
              <span>列定义管理</span>
            </a-menu-item>
            <a-menu-item key="overrides">
              <template #icon><UserSwitchOutlined /></template>
              <span>用户覆盖管理</span>
            </a-menu-item>
            <a-menu-item key="role-presets">
              <template #icon><TeamOutlined /></template>
              <span>角色预设管理</span>
            </a-menu-item>
          </a-menu-item-group>

          <a-menu-item-group title="系统管理">
            <a-menu-item key="users">
              <template #icon><UserOutlined /></template>
              <span>用户管理</span>
            </a-menu-item>
            <a-menu-item key="roles">
              <template #icon><TagOutlined /></template>
              <span>角色管理</span>
            </a-menu-item>
          </a-menu-item-group>
        </template>

        <!-- 普通用户菜单 -->
        <template v-if="auth.role === 'user'">
          <a-menu-item-group title="管理功能">
            <a-menu-item key="presets">
              <template #icon><GoldOutlined /></template>
              <span>预设管理</span>
            </a-menu-item>
            <a-menu-item key="columns">
              <template #icon><TableOutlined /></template>
              <span>列定义管理</span>
            </a-menu-item>
            <a-menu-item key="overrides">
              <template #icon><UserSwitchOutlined /></template>
              <span>用户覆盖管理</span>
            </a-menu-item>
            <a-menu-item key="role-presets">
              <template #icon><TeamOutlined /></template>
              <span>角色预设管理</span>
            </a-menu-item>
          </a-menu-item-group>
        </template>

        <!-- 查看者菜单（只读） -->
        <template v-if="auth.role === 'viewer'">
          <a-menu-item-group title="数据查看">
            <a-menu-item key="presets">
              <template #icon><GoldOutlined /></template>
              <span>预设方案</span>
            </a-menu-item>
            <a-menu-item key="overrides">
              <template #icon><UserSwitchOutlined /></template>
              <span>用户覆盖</span>
            </a-menu-item>
            <a-menu-item key="role-presets">
              <template #icon><TeamOutlined /></template>
              <span>角色预设</span>
            </a-menu-item>
          </a-menu-item-group>
        </template>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <!-- 顶部栏 -->
      <a-layout-header class="admin-header">
        <div class="admin-header-left">
          <MenuFoldOutlined v-if="!collapsed" class="admin-trigger" @click="collapsed = true" />
          <MenuUnfoldOutlined v-else class="admin-trigger" @click="collapsed = false" />
          <a-breadcrumb class="admin-breadcrumb">
            <a-breadcrumb-item>首页</a-breadcrumb-item>
            <a-breadcrumb-item>{{ pageTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <a-space size="middle">
          <a-tooltip :title="serverUrl">
            <a-tag color="blue">
              <ApiOutlined />
              {{ displayServerUrl }}
            </a-tag>
          </a-tooltip>
          <a-dropdown>
            <a-space class="admin-user">
              <a-avatar size="small" style="background-color: #1677ff">{{ auth.displayName?.[0] || 'A' }}</a-avatar>
              <span>{{ auth.displayName || auth.username }}</span>
              <DownOutlined />
            </a-space>
            <template #overlay>
              <a-menu @click="onUserMenu">
                <a-menu-item key="logout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="admin-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  TableOutlined, DashboardOutlined, AppstoreOutlined,
  PartitionOutlined, GoldOutlined, ApiOutlined, UserSwitchOutlined, TeamOutlined,
  MenuFoldOutlined, MenuUnfoldOutlined, DownOutlined, LogoutOutlined,
  UserOutlined, TagOutlined,
} from '@ant-design/icons-vue'
import { auth, isLoggedIn, logout, getServerUrl } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const selectedKeys = ref<string[]>(['dashboard'])

const pageTitle = computed(() => (route.meta?.title as string) || '')

const serverUrl = computed(() => getServerUrl())
const displayServerUrl = computed(() => {
  const u = getServerUrl()
  try { return new URL(u).host } catch { return u }
})

// 同步菜单高亮
watch(() => route.path, (path) => {
  const seg = path.split('/').filter(Boolean)[0]
  if (seg) selectedKeys.value = [seg]
}, { immediate: true })

function onMenuClick({ key }: { key: string }) {
  router.push(`/${key}`)
}

function onUserMenu({ key }: { key: string }) {
  if (key === 'logout') {
    logout()
    router.replace('/login')
  }
}

// 未登录跳转
onMounted(() => {
  if (!isLoggedIn()) {
    router.replace('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-sider {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.admin-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  text-decoration: none;
}
.admin-logo-icon {
  font-size: 20px;
  flex-shrink: 0;
}
.admin-logo-text {
  white-space: nowrap;
  overflow: hidden;
}

.admin-menu {
  border-inline-end: none !important;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 24px;
  height: 56px;
  line-height: 56px;
  border-bottom: 1px solid #f0f0f0;
}

.admin-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-trigger {
  font-size: 16px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}
.admin-trigger:hover { color: #1677ff; }

.admin-content {
  padding: 20px 24px;
  min-height: calc(100vh - 56px);
  overflow-y: auto;
}

.admin-user {
  cursor: pointer;
  color: #333;
}
.admin-user:hover { color: #1677ff; }
</style>
