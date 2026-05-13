import { post } from './http.js'

/**
 * 验证解锁口令
 */
export function validateUnlockCode(deviceId, unlockCode) {
  return post('/api/auth/unlock/validate', { deviceId, unlockCode })
}
