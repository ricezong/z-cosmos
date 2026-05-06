<template>
  <router-view />
  <ToastContainer />
  <ConfirmModal />
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuth } from './composables/useAuth'
import ToastContainer from './components/ToastContainer.vue'
import ConfirmModal from './components/ConfirmModal.vue'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

const { setToken, markReady } = useAuth()

onMounted(async () => {
  // 1. OAuth 回调：从 URL 参数中提取 accessToken
  const urlParams = new URLSearchParams(window.location.search)
  const oauthToken = urlParams.get('token')
  if (oauthToken) {
    setToken(oauthToken)
    // 清除 URL 中的参数
    window.history.replaceState({}, '', window.location.pathname + window.location.hash)
    markReady()
    return
  }

  // 2. 页面刷新：通过 HttpOnly Cookie 中的 Refresh Token 自动恢复 Access Token
  //    如果 localStorage 有 accessToken 则直接可用，但尝试刷新以延长会话
  const existingToken = localStorage.getItem('cosmos_access_token')
  if (!existingToken) {
    // 没有 accessToken，尝试通过 Cookie 刷新
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include', // 自动携带 HttpOnly Cookie
        headers: { 'Content-Type': 'application/json' },
      })
      const payload = await response.json()
      if (response.ok && payload.code === 200 && payload.data?.accessToken) {
        setToken(payload.data.accessToken)
      }
    } catch {
      // Refresh Token 不存在或已过期，用户需要重新登录
    }
  }

  // 3. 标记认证初始化完成
  markReady()
})
</script>