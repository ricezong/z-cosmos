import { get } from './http'

export function listNews(params = {}) {
  return get('/api/news', params)
}

export function getNews(newsId) {
  return get(`/api/news/${encodeURIComponent(newsId)}`)
}

export function topNews(limit = 20) {
  return get('/api/news/top', { limit })
}

export function rankingNews(period = 'day', limit = 20) {
  return get('/api/news/ranking', { period, limit })
}
