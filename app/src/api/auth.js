import { get, post } from './http.js'

/**
 * 请求动态口令
 */
export function requestUnlockCode(data) {
  return post('/api/auth/unlock/request', data)
}

/**
 * 检查解锁状态
 */
export function checkUnlockStatus(params) {
  return get('/api/auth/unlock/status', params)
}
