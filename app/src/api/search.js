import { get } from './http'

export function globalSearch(q, topN = 10) {
  return get('/api/search', { q, topN })
}

export function searchByType(type, q, page = 1, size = 20) {
  return get('/api/search/type', { type, q, page, size })
}

export function hotKeywords(limit = 10) {
  return get('/api/search/hot-keywords', { limit })
}

export function suggestions(q, limit = 10) {
  return get('/api/search/suggestions', { q, limit })
}
