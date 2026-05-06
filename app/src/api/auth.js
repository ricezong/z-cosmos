import { post, request } from './http'
import { setAuthTokens, clearAuthTokens } from '../composables/useAuth'

export async function loginByPassword({ phone, password }) {
  // 后端同时返回 accessToken（响应体）+ refreshToken（HttpOnly Cookie）
  const tokens = await post('/api/auth/login', { phone, password })
  // 只将 accessToken 存入 localStorage，refreshToken 由后端 Cookie 管理
  setAuthTokens(tokens)
  return tokens
}

export async function logout() {
  try {
    // 后端会：1. 黑名单 accessToken（从 Authorization 头）2. 撤销 Cookie 中的 refreshToken 3. 清除 Cookie
    await request('/api/auth/logout', { method: 'POST' })
  } finally {
    // 清除本地 accessToken
    clearAuthTokens()
  }
}

export async function getOAuthUrl(provider) {
  return post('/api/auth/oauth/url', { provider })
}
