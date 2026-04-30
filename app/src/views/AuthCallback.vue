<template>
  <div class="callback-page">
    <div class="star-bg"></div>
    <section class="callback-card">
      <i class="ri-loader-4-line"></i>
      <h1>{{ title }}</h1>
      <p>{{ message }}</p>
      <router-link to="/" class="home-link">返回星域</router-link>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const title = ref('正在完成登录')
const message = ref('请稍候，正在同步第三方登录状态。')

onMounted(() => {
  if (route.query.success === 'true') {
    localStorage.setItem('cosmos_logged_in', 'true')
    if (route.query.userId) {
      localStorage.setItem('cosmos_profile', JSON.stringify({
        id: String(route.query.userId),
        nickname: 'COSMOS',
        favorites: []
      }))
    }
    title.value = '登录成功'
    message.value = '欢迎回来，正在返回首页。'
    setTimeout(() => router.replace('/'), 900)
    return
  }

  title.value = '登录失败'
  message.value = route.query.msg || route.query.error || '第三方登录未完成。'
})
</script>

<style scoped>
.callback-page { min-height: 100%; display: flex; align-items: center; justify-content: center; padding: 24px; color: #e8eef7; background: #08091a; }
.callback-card { position: relative; z-index: 1; width: min(420px, 100%); padding: 34px 28px; border-radius: 20px; text-align: center; background: rgba(16,22,42,0.78); border: 1px solid rgba(144,166,196,0.24); backdrop-filter: blur(16px); }
.callback-card i { display: block; font-size: 2.6rem; color: #c5d5ea; margin-bottom: 16px; animation: spin 1.2s linear infinite; }
.callback-card h1 { font-size: 1.35rem; font-weight: 500; margin-bottom: 8px; }
.callback-card p { opacity: 0.72; line-height: 1.6; margin-bottom: 22px; }
.home-link { display: inline-flex; padding: 10px 24px; border-radius: 24px; color: #fff; text-decoration: none; background: linear-gradient(135deg, #7890b5, #a8bcd4); }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
