import { get, post } from './http'

export function listVideos(params = {}) {
  return get('/api/videos', params)
}

export function getVideo(videoId) {
  return get(`/api/videos/${encodeURIComponent(videoId)}`)
}

export function listEpisodes(videoId) {
  return get(`/api/videos/${encodeURIComponent(videoId)}/episodes`)
}

export function reportPlay(videoId, episodeNumber) {
  return post(`/api/videos/${encodeURIComponent(videoId)}/play`, { episodeNumber })
}
