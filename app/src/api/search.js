import { get } from './http'

/**
 * 全局搜索
 */
export function globalSearch(q, page = 1, size = 20) {
  return get('/api/search', { q, page, size })
}
