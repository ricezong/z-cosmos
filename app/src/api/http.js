const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

// 全局认证：基于device_id

/**
 * 获取device_id
 */
function getDeviceId() {
  let deviceId = localStorage.getItem('device_id')
  if (!deviceId) {
    deviceId = crypto.randomUUID()
    localStorage.setItem('device_id', deviceId)
  }
  return deviceId
}

export { getDeviceId }

// ========== 请求封装 ==========

export async function request(path, options = {}) {
  const headers = new Headers(options.headers || {})
  const body = options.body

  if (body != null && !(body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  // 添加device_id到请求头（用于认证追踪）
  const deviceId = getDeviceId()
  if (!headers.has('X-Device-ID')) {
    headers.set('X-Device-ID', deviceId)
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