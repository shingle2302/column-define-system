<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <TableOutlined />
        </div>
        <h1>列自定义管理系统</h1>
        <p>Column Define System — Admin Console</p>
      </div>

      <a-form layout="vertical" :model="form" class="login-form" @finish="onLogin">
        <a-form-item>
          <a-input v-model:value="form.serverUrl" size="large" placeholder="服务器地址">
            <template #prefix><CloudServerOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="form.username" size="large" placeholder="用户名">
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
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
          <p>测试账号：</p>
          <p>admin / admin123（管理员）</p>
          <p>alice / alice123（用户，CRM系统）</p>
          <p>bob / bob123（用户，HR系统）</p>
          <p>viewer / viewer123（观察者，CRM系统）</p>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TableOutlined, CloudServerOutlined, UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { login, setServerUrl, getServerUrl } from '../stores/auth'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')

const form = ref({
  serverUrl: getServerUrl(),
  username: 'admin',
  password: 'admin123',
})

// 检查是否有上次因 401 被踢回的诊断信息
onMounted(() => {
  const raw = sessionStorage.getItem('cds_redirect_error')
  if (raw) {
    try {
      const err = JSON.parse(raw)
      console.log('[Login] 检测到上次因 401 被重定向:', err)
      errorMsg.value = `[${err.time}] 请求 ${err.path} 返回 401: ${err.message}（角色: ${err.role}, 系统: ${err.systemId}）`
      sessionStorage.removeItem('cds_redirect_error')
    } catch {
      sessionStorage.removeItem('cds_redirect_error')
    }
  }
})

async function onLogin() {
  loading.value = true
  errorMsg.value = ''
  try {
    setServerUrl(form.value.serverUrl)
    await login(form.value.username, form.value.password, form.value.serverUrl)
    router.replace('/')
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea, #1677ff);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26px;
}

.login-header h1 {
  font-size: 22px;
  color: #1f1f1f;
  margin-bottom: 6px;
}

.login-header p {
  font-size: 13px;
  color: #999;
}

.login-error {
  color: #ff4d4f;
  font-size: 13px;
  margin: -8px 0 8px;
}

.login-hint {
  text-align: center;
  color: #bbb;
  font-size: 12px;
}
</style>
