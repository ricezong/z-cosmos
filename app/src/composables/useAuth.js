import { ref, computed } from 'vue'

/**
 * 全局认证状态（单一数据源 + Vue 响应式）
 *
 * 双 Token 策略：
 *   - Access Token: 存 localStorage + 内存响应式 ref，请求头 Authorization: Bearer <token>
 *   - Refresh Token: 后端 HttpOnly Cookie 管理，前端不接触
 */
const token = ref(localStorage.getItem('cosmos_access_token'))
const authReady = ref(false)

// 监听 localStorage 变化（跨标签页同步）
if (typeof window !== 'undefined') {
  window.addEventListener('storage', (e) => {
    if (e.key === 'cosmos_access_token') {
      token.value = e.newValue
    }
  })
}

// ---- 底层 API（供 http.js / auth.js 等非 Vue 上下文调用）----

export function getAccessToken() {
  return token.value || localStorage.getItem('cosmos_access_token')
}

export function setAccessToken(val) {
  token.value = val
  if (val) {
    localStorage.setItem('cosmos_access_token', val)
  } else {
    localStorage.removeItem('cosmos_access_token')
  }
}

/**
 * 登录成功后设置 Token
 * 只存 accessToken 到 localStorage，refreshToken 由后端 HttpOnly Cookie 管理
 */
export function setAuthTokens(tokens) {
  if (!tokens) return
  const accessToken = tokens.accessToken || tokens.access_token
  if (accessToken) {
    token.value = accessToken
    localStorage.setItem('cosmos_access_token', accessToken)
  }
}

/**
 * 清除本地认证状态（登出或 Token 失效时调用）
 * 不清除 HttpOnly Cookie（由后端 /logout 端点负责）
 */
export function clearAuthTokens() {
  token.value = null
  localStorage.removeItem('cosmos_access_token')
}

export function isApiLoggedIn() {
  return !!token.value || !!localStorage.getItem('cosmos_access_token')
}

// ---- Vue Composable ----

export function useAuth() {
  const isLoggedIn = computed(() => !!token.value)

  return {
    isLoggedIn,
    authReady,
    token: computed(() => token.value),
    setToken(val) {
      setAccessToken(val)
    },
    clearToken() {
      clearAuthTokens()
    },
    markReady() {
      authReady.value = true
    },
  }
}