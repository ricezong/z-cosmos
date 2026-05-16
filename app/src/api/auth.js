import { get, post } from './http.js'
import { getDeviceId } from '../utils/device.js'

/**
 * 请求动态口令（后端生成6位混合口令）
 */
export function requestUnlockCode(moduleType = 'NOTE') {
  return post('/api/auth/unlock/request', {
    deviceId: getDeviceId(),
    moduleType
  })
}

/**
 * 轮询检查解锁状态
 */
export function checkUnlockStatus(moduleType = 'NOTE') {
  return get('/api/auth/unlock/status', {
    deviceId: getDeviceId(),
    moduleType
  })
}

