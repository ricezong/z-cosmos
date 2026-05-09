import { get } from './http.js'

/**
 * 获取笔记列表
 */
export function getNotes(params) {
  return get('/api/notes', params)
}

/**
 * 获取笔记详情（需传递deviceId验证解锁状态）
 */
export function getNoteDetail(id, deviceId) {
  const params = {}
  if (deviceId) params.deviceId = deviceId
  return get(`/api/notes/${id}`, params)
}

/**
 * 获取笔记预览（根据preview_ratio动态生成）
 */
export function getNotePreview(id) {
  return get(`/api/notes/${id}/preview`)
}
