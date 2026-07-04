import { reactive } from 'vue'

export interface AuthState {
  token: string
  userId: string
  username: string
  displayName: string
  role: string
  systemId: string
}

const STORAGE_KEY = 'cds_admin_auth'

function loadAuth(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch { /* ignore */ }
  return { token: '', userId: '', username: '', displayName: '', role: '', systemId: '' }
}

function saveAuth(state: AuthState) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function clearAuth() {
  localStorage.removeItem(STORAGE_KEY)
}

export const auth = reactive<AuthState>(loadAuth())

export function isLoggedIn(): boolean {
  return !!auth.token
}

export async function login(username: string, password: string, serverUrl: string): Promise<void> {
  const resp = await fetch(`${serverUrl}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  const json = await resp.json()
  if (json.code !== 200) throw new Error(json.message || '登录失败')
  const data = json.data
  const user = data.user || {}
  auth.token = data.token
  auth.userId = user.id || ''
  auth.username = user.username || username
  auth.displayName = user.displayName || username
  auth.role = user.role || 'admin'
  auth.systemId = user.systemId || ''
  saveAuth({ ...auth })
}

export function logout() {
  auth.token = ''
  auth.userId = ''
  auth.username = ''
  auth.displayName = ''
  auth.role = ''
  auth.systemId = ''
  clearAuth()
}

/** 获取后端服务器地址，可通过 localStorage 覆盖 */
export function getServerUrl(): string {
  return localStorage.getItem('cds_server_url') || 'http://localhost:8080'
}

export function setServerUrl(url: string) {
  localStorage.setItem('cds_server_url', url)
}

/** 封装 fetch，自动加 token，401 时自动跳转登录 */
export async function api<T = any>(path: string, options?: RequestInit): Promise<T> {
  const base = getServerUrl()
  const { headers: optHeaders, ...restOpts } = options || {}

  console.log(
    `[api] → ${path} | token=${auth.token ? auth.token.substring(0, 20) + '...' : '(empty)'} | role=${auth.role} | systemId=${auth.systemId}`
  )

  // 构建合并后的 headers，保证 Authorization 不被覆盖
  const baseHeaders: HeadersInit = {
    'Content-Type': 'application/json',
    ...(auth.token ? { Authorization: `Bearer ${auth.token}` } : {}),
  }

  // 如果调用方传了自定义 headers，合并进去（调用方 headers 优先级更高）
  const mergedHeaders = optHeaders
    ? new Headers(baseHeaders)
    : baseHeaders
  if (optHeaders && mergedHeaders instanceof Headers) {
    const extra = new Headers(optHeaders)
    extra.forEach((v, k) => mergedHeaders.set(k, v))
  }

  let resp: Response
  try {
    resp = await fetch(`${base}${path}`, {
      ...restOpts,
      headers: mergedHeaders,
    })
  } catch (e: any) {
    console.error(`[api] fetch failed: ${path}`, e)
    throw new Error('网络请求失败，请检查服务器连接')
  }

  let json: any
  try {
    json = await resp.json()
  } catch {
    console.error(`[api] non-JSON response: ${path} status=${resp.status}`)
    throw new Error(`服务器返回异常 (HTTP ${resp.status})`)
  }

  console.log(`[api] ${path} ← code=${json.code} message="${json.message}"`)

  if (json.code === 401) {
    // 把错误原因存入 sessionStorage，这样页面刷新后 Login 页可以显示
    const errDetail = {
      path,
      code: json.code,
      message: json.message,
      role: auth.role,
      systemId: auth.systemId,
      time: new Date().toISOString(),
    }
    console.error(`[api] 401 Unauthorized!`, errDetail)
    sessionStorage.setItem('cds_redirect_error', JSON.stringify(errDetail))
    logout()
    // 延迟跳转，让上面的日志有时间写入
    setTimeout(() => {
      window.location.href = '/login'
    }, 300)
    throw new Error('登录已过期，请重新登录')
  }
  if (json.code !== 200) throw new Error(json.message || '请求失败')
  return json.data as T
}
