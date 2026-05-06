const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

// Token 状态统一由 composables/useAuth.js 管理
import {
  getAccessToken,
  setAccessToken,
  clearAuthTokens,
  isApiLoggedIn
} from '../composables/useAuth'

export { getAccessToken, setAccessToken, clearAuthTokens, isApiLoggedIn }

// ========== 401 自动刷新 ==========

let isRefreshing = false
let pendingRequests = []

function onTokenRefreshed(newToken) {
  pendingRequests.forEach(cb => cb(newToken))
  pendingRequests = []
}

/**
 * 尝试通过 HttpOnly Cookie 中的 Refresh Token 刷新 Access Token
 */
async function tryRefreshToken() {
  if (isRefreshing) {
    return new Promise(resolve => {
      pendingRequests.push(token => resolve(token))
    })
  }

  isRefreshing = true
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: 'POST',
      credentials: 'include', // 自动携带 HttpOnly Cookie
      headers: { 'Content-Type': 'application/json' },
    })
    const payload = await response.json()

    if (response.ok && payload.code === 200 && payload.data?.accessToken) {
      const newToken = payload.data.accessToken
      setAccessToken(newToken)
      onTokenRefreshed(newToken)
      return newToken
    }
    // 刷新失败
    return null
  } catch {
    return null
  } finally {
    isRefreshing = false
  }
}

// ========== 请求封装 ==========

export async function request(path, options = {}) {
  const headers = new Headers(options.headers || {})
  const body = options.body

  if (body != null && !(body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  // 自动携带 Access Token
  const token = getAccessToken()
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include', // HttpOnly Cookie 自动携带（用于 refresh）
    ...options,
    headers,
    body: body != null && !(body instanceof FormData) && typeof body !== 'string'
      ? JSON.stringify(body)
      : body,
  })

  let payload = null
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    payload = await response.json()
  } else {
    payload = await response.text()
  }

  // 401 → 尝试通过 Refresh Token Cookie 自动刷新 Access Token
  if (response.status === 401
      && !path.includes('/api/auth/refresh')
      && !path.includes('/api/auth/logout')) {
    const newToken = await tryRefreshToken()
    if (newToken) {
      // 用新 Token 重试原请求
      headers.set('Authorization', `Bearer ${newToken}`)
      const retryResponse = await fetch(`${API_BASE_URL}${path}`, {
        credentials: 'include',
        ...options,
        headers,
        body: body != null && !(body instanceof FormData) && typeof body !== 'string'
          ? JSON.stringify(body)
          : body,
      })
      let retryPayload = null
      const retryContentType = retryResponse.headers.get('content-type') || ''
      if (retryContentType.includes('application/json')) {
        retryPayload = await retryResponse.json()
      } else {
        retryPayload = await retryResponse.text()
      }
      if (!retryResponse.ok) {
        const error = new Error(retryPayload?.msg || retryPayload?.message || retryResponse.statusText)
        error.status = retryResponse.status
        error.payload = retryPayload
        throw error
      }
      if (retryPayload && typeof retryPayload === 'object' && 'code' in retryPayload) {
        if (retryPayload.code !== 200) {
          const error = new Error(retryPayload.msg || 'Request failed')
          error.status = retryPayload.code
          error.payload = retryPayload
          throw error
        }
        return retryPayload.data
      }
      return retryPayload
    }
    // Refresh 也失败 → 清除本地状态，跳转登录页
    clearAuthTokens()
    if (typeof window !== 'undefined' && !window.location.hash.includes('/login')) {
      window.location.hash = '#/login'
    }
    const error = new Error(payload?.msg || payload?.message || '登录已过期，请重新登录')
    error.status = 401
    throw error
  }

  if (!response.ok) {
    const message = payload?.msg || payload?.message || response.statusText || 'Request failed'
    const error = new Error(message)
    error.status = response.status
    error.payload = payload
    throw error
  }

  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code !== 200) {
      const error = new Error(payload.msg || 'Request failed')
      error.status = payload.code
      error.payload = payload
      throw error
    }
    return payload.data
  }

  return payload
}

// ========== 便捷方法 ==========

export function get(path, params) {
  const search = new URLSearchParams()
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value)
  })
  const qs = search.toString()
  return request(qs ? `${path}?${qs}` : path)
}

export function post(path, body) {
  return request(path, { method: 'POST', body })
}

export function put(path, body) {
  return request(path, { method: 'PUT', body })
}

export function del(path) {
  return request(path, { method: 'DELETE' })
}