<template>
  <div class="login-panel">
    <div class="login-header">
      <div class="login-logo"><KeyOutlined /></div>
      <h2>登录</h2>
      <p>请输入您的账号信息</p>
    </div>

    <a-form layout="vertical" :model="form" @finish="onLogin">
      <a-form-item label="服务器地址">
        <a-input v-model:value="form.serverUrl" size="large" placeholder="http://localhost:8080">
          <template #prefix><CloudServerOutlined /></template>
        </a-input>
      </a-form-item>
      <a-form-item label="用户名" :rules="[{ required: true, message: '请输入用户名' }]">
        <a-input v-model:value="form.username" size="large" placeholder="用户名">
          <template #prefix><UserOutlined /></template>
        </a-input>
      </a-form-item>
      <a-form-item label="密码" :rules="[{ required: true, message: '请输入密码' }]">
        <a-input-password v-model:value="form.password" size="large" placeholder="密码" @pressEnter="onLogin">
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <p v-if="errorMsg" class="login-error">{{ errorMsg }}</p>

      <a-form-item>
        <a-button type="primary" html-type="submit" size="large" :loading="loading" block>
          {{ loading ? '登录中...' : '登 录' }}
        </a-button>
      </a-form-item>

      <div class="login-hint">
        <p>测试账号：admin / admin123 | alice / alice123</p>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { KeyOutlined, CloudServerOutlined, UserOutlined, LockOutlined } from '@ant-design/icons-vue'

// --------------- 内部 API ---------------
const STORAGE_KEY = 'cds_demo_auth'
const SERVER_KEY = 'cds_demo_server_url'

interface LoginState {
  token: string
  userId: string
  username: string
  displayName: string
  role: string
  systemId: string
}

function loadState(): LoginState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch { /* ignore */ }
  return { token: '', userId: '', username: '', displayName: '', role: '', systemId: '' }
}

function persist(s: LoginState) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(s))
}

const loginState = reactive<LoginState>(loadState())

async function doLogin(serverUrl: string, username: string, password: string) {
  localStorage.setItem(SERVER_KEY, serverUrl)
  const resp = await fetch(`${serverUrl}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  const json = await resp.json()
  if (json.code !== 200) throw new Error(json.message || '登录失败')
  const data = json.data
  const user = data.user || {}
  loginState.token = data.token
  loginState.userId = user.id || ''
  loginState.username = user.username || username
  loginState.displayName = user.displayName || username
  loginState.role = user.role || 'user'
  loginState.systemId = user.systemId || ''
  persist({ ...loginState })
}

/** 获取保存的服务器地址 */
function getServerUrl(): string {
  return localStorage.getItem(SERVER_KEY) || 'http://localhost:8080'
}

// --------------- 组件逻辑 ---------------
const emit = defineEmits<{
  login: [user: { username: string; role: string; token: string; systemId: string; password: string }]
}>()

const loading = ref(false)
const errorMsg = ref('')

const form = ref({
  serverUrl: getServerUrl(),
  username: 'admin',
  password: 'admin123',
})

async function onLogin() {
  loading.value = true
  errorMsg.value = ''
  try {
    await doLogin(form.value.serverUrl, form.value.username, form.value.password)
    emit('login', {
      username: loginState.username,
      role: loginState.role,
      token: loginState.token,
      systemId: loginState.systemId,
      password: form.value.password,
    })
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

/** 暴露给父组件：获取当前 token */
defineExpose({
  get token() { return loginState.token },
  get role() { return loginState.role },
  get userId() { return loginState.userId },
  get systemId() { return loginState.systemId },
  getServerUrl,
})
</script>

<style scoped>
.login-panel { max-width: 500px; margin: 0 auto; }
.login-header { text-align: center; margin-bottom: 24px; }
.login-logo {
  width: 48px; height: 48px; margin: 0 auto 12px;
  background: linear-gradient(135deg, #667eea, #1677ff);
  border-radius: 12px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px;
}
.login-header h2 { font-size: 18px; margin: 0 0 4px; color: #333; }
.login-header p { font-size: 13px; color: #999; margin: 0; }
.login-error { color: #ff4d4f; font-size: 13px; margin: -8px 0 8px; }
.login-hint { text-align: center; color: #bbb; font-size: 12px; }
</style>
