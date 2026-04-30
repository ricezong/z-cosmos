import { clearAuthTokens, post, request, setAuthTokens } from './http'

export async function loginByPassword({ phone, password }) {
  const tokens = await post('/api/auth/login', { phone, password })
  setAuthTokens(tokens)
  return tokens
}

export async function logout(refreshToken) {
  try {
    await request('/api/auth/logout', {
      method: 'POST',
      body: refreshToken ? { refreshToken } : undefined,
    })
  } finally {
    clearAuthTokens()
  }
}

export async function getOAuthUrl(provider) {
  return post('/api/auth/oauth/url', { provider })
}
