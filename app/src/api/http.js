const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888').replace(/\/$/, '')

const ACCESS_TOKEN_KEY = 'cosmos_access_token'
const REFRESH_TOKEN_KEY = 'cosmos_refresh_token'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAuthTokens(tokens) {
  if (!tokens) return
  if (tokens.accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken)
  if (tokens.refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
  localStorage.setItem('cosmos_logged_in', tokens.accessToken ? 'true' : 'false')
}

export function clearAuthTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem('cosmos_logged_in')
}

export function isApiLoggedIn() {
  return Boolean(getAccessToken()) || localStorage.getItem('cosmos_logged_in') === 'true'
}

export async function request(path, options = {}) {
  const headers = new Headers(options.headers || {})
  const body = options.body

  if (body != null && !(body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getAccessToken()
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
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
