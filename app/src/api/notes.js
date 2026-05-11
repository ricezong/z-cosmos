import http from './http'

/**
 * 获取笔记列表（分页）
 */
export function getNoteList(page = 1, size = 10, categoryCode = '') {
  return http.get('/api/notes/list', {
    params: { page, size, categoryCode }
  })
}

/**
 * 获取笔记详情
 */
export function getNoteDetail(noteId) {
  return http.get(`/api/notes/${noteId}`)
}

/**
 * 获取分类列表
 */
export function getCategories() {
  return http.get('/api/notes/categories')
}
