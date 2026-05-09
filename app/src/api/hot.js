import { get } from './http'

/**
 * 获取热点话题列表
 */
export function listHotTopics(params = {}) {
  return get('/api/hot/topics', params)
}

/**
 * 获取热点话题详情
 */
export function getHotTopic(topicId) {
  return get(`/api/hot/topics/${encodeURIComponent(topicId)}`)
}
