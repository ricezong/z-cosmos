import { get, post, put } from './http'

export function getCurrentUser() {
  return get('/api/user/me')
}

export function updateProfile(payload) {
  return put('/api/user/profile', payload)
}

export function changePassword(payload) {
  return put('/api/user/password', payload)
}

export function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  return post('/api/user/avatar', form)
}

export function listMyPosts(params = {}) {
  return get('/api/user/posts', params)
}

export function listMyCollections(params = {}) {
  return get('/api/user/collections', params)
}
