import { del, get, post, put, request } from './http'

export function listCategories() {
  return get('/api/categories')
}

export function listPosts(params = {}) {
  return get('/api/posts', params)
}

export function getPost(postId) {
  return get(`/api/posts/${encodeURIComponent(postId)}`)
}

export function createPost(payload) {
  return post('/api/posts', payload)
}

export function updatePost(postId, payload) {
  return put(`/api/posts/${encodeURIComponent(postId)}`, payload)
}

export function deletePost(postId) {
  return del(`/api/posts/${encodeURIComponent(postId)}`)
}

export function likePost(postId) {
  return post(`/api/posts/${encodeURIComponent(postId)}/like`)
}

export function unlikePost(postId) {
  return del(`/api/posts/${encodeURIComponent(postId)}/like`)
}

export function collectPost(postId) {
  return post(`/api/posts/${encodeURIComponent(postId)}/collect`)
}

export function uncollectPost(postId) {
  return del(`/api/posts/${encodeURIComponent(postId)}/collect`)
}

export function listComments(postId, params = {}) {
  return get(`/api/posts/${encodeURIComponent(postId)}/comments`, params)
}

export function createComment(postId, payload) {
  return post(`/api/posts/${encodeURIComponent(postId)}/comments`, payload)
}

export function likeComment(commentId) {
  return post(`/api/comments/${encodeURIComponent(commentId)}/like`)
}

export function unlikeComment(commentId) {
  return del(`/api/comments/${encodeURIComponent(commentId)}/like`)
}

export function uploadForm(path, formData) {
  return request(path, { method: 'POST', body: formData })
}
