import { post } from './http.js'

/**
 * 验证解锁口令
 */
export function validateUnlockCode(data) {
  return post('/api/auth/unlock/validate', data)
}
