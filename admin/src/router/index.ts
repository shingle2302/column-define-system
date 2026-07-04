import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DashboardOutlined' },
      },
      {
        path: 'systems',
        name: 'Systems',
        component: () => import('../views/SystemManage.vue'),
        meta: { title: '系统管理', icon: 'AppstoreOutlined' },
      },
      {
        path: 'businesses',
        name: 'Businesses',
        component: () => import('../views/BusinessManage.vue'),
        meta: { title: '业务管理', icon: 'PartitionOutlined' },
      },
      {
        path: 'presets',
        name: 'Presets',
        component: () => import('../views/PresetManage.vue'),
        meta: { title: '预设管理', icon: 'GoldOutlined' },
      },
      {
        path: 'columns',
        name: 'Columns',
        component: () => import('../views/ColumnManage.vue'),
        meta: { title: '列定义管理', icon: 'TableOutlined' },
      },
      {
        path: 'overrides',
        name: 'Overrides',
        component: () => import('../views/UserOverride.vue'),
        meta: { title: '用户覆盖管理', icon: 'UserSwitchOutlined' },
      },
      {
        path: 'role-presets',
        name: 'RolePresets',
        component: () => import('../views/RolePreset.vue'),
        meta: { title: '角色预设管理', icon: 'TeamOutlined' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/UserManage.vue'),
        meta: { title: '用户管理', icon: 'UserOutlined' },
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('../views/RoleManage.vue'),
        meta: { title: '角色管理', icon: 'TagOutlined' },
      },
    ],
  },
]
