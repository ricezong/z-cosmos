<template>
  <div class="login-page">
    <div class="star-bg"></div>
    <main class="login-wrap">
      <section class="login-card">
        <div class="login-logo">
          <div class="planet-icon"><i class="ri-shield-star-line"></i></div>
          <h1>COSMOS</h1>
          <p>登录星域</p>
        </div>

        <div class="login-tabs">
          <button class="login-tab" :class="{ active: tab === 'account' }" @click="tab = 'account'">账号登录</button>
          <button class="login-tab" :class="{ active: tab === 'oauth' }" @click="tab = 'oauth'">第三方登录</button>
        </div>

        <div class="login-panel" v-if="tab === 'account'">
          <label>手机号</label>
          <input type="tel" v-model="loginForm.phone" placeholder="请输入手机号" maxlength="11">
          <label>密码</label>
          <input type="password" v-model="loginForm.pwd" placeholder="请输入密码">
          <label class="remember-me">
            <input type="checkbox" v-model="loginForm.remember">
            <span>记住账号</span>
          </label>
          <button class="login-btn" @click="doApiLogin" :disabled="loading">
            {{ loading ? '登录中...' : '登录星域' }}
            <i class="ri-arrow-right-up-line"></i>
          </button>
        </div>

        <div class="login-panel" v-else>
          <div class="oauth-grid">
            <button class="oauth-btn" @click="doOAuthLogin('QQ')"><svg class="brand-icon"><use href="/icons.svg#qq-icon"/></svg></button>
            <button class="oauth-btn" @click="doOAuthLogin('GitHub')"><svg class="brand-icon"><use href="/icons.svg#github-icon"/></svg></button>
            <button class="oauth-btn" @click="doOAuthLogin('Gitee')"><svg class="brand-icon"><use href="/icons.svg#gitee-icon"/></svg></button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOAuthUrl, loginByPassword } from '../api/auth'

const router = useRouter()
const tab = ref('account')
const loginForm = ref({ phone: '', pwd: '', remember: false })
const loading = ref(false)

async function doApiLogin() {
  const { phone, pwd } = loginForm.value
  if (!phone.trim() || !pwd.trim()) {
    alert('请输入手机号和密码')
    return
  }
  loading.value = true
  try {
    const resp = await loginByPassword({ phone: phone.trim(), password: pwd })
    if (loginForm.value.remember) localStorage.setItem('cosmos_phone', phone.trim())
    localStorage.setItem('cosmos_profile', JSON.stringify({
      id: String(resp.userId || ''),
      nickname: resp.nickname || 'COSMOS',
      favorites: []
    }))
    router.push('/')
  } catch (e) {
    alert(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function doOAuthLogin(platform) {
  try {
    const resp = await getOAuthUrl(platform.toLowerCase())
    const url = typeof resp === 'string' ? resp : (resp?.url || resp?.authUrl || resp?.authorizeUrl)
    if (!url) throw new Error('OAuth auth url missing')
    window.location.href = url
  } catch (e) {
    alert(e.message || `OAuth login failed: ${platform}`)
  }
}

onMounted(() => {
  const saved = localStorage.getItem('cosmos_phone')
  if (saved) {
    loginForm.value.phone = saved
    loginForm.value.remember = true
  }
})
</script>

<style scoped>
.login-page { width: 100%; height: 100%; overflow: auto; background: linear-gradient(180deg, #0d0f24, #08091a); color: #e0e8f0; position: relative; }
.login-wrap { min-height: 100%; display: flex; align-items: center; justify-content: center; padding: 24px 16px; box-sizing: border-box; position: relative; z-index: 1; }
.login-card { width: 100%; max-width: 380px; padding: 34px 28px; border-radius: 22px; background: linear-gradient(155deg, rgba(28,24,56,0.78), rgba(12,14,32,0.7)); border: 1px solid rgba(255,255,255,0.1); backdrop-filter: blur(18px); box-shadow: 0 24px 60px rgba(0,0,0,0.5); }
.login-logo { text-align: center; margin-bottom: 26px; }
.planet-icon { font-size: 46px; color: #fff; margin-bottom: 12px; text-shadow: 0 0 18px rgba(200,180,255,0.55); }
.login-logo h1 { font-size: 1.6rem; font-weight: 500; letter-spacing: 8px; color: #fff; margin-bottom: 6px; }
.login-logo p { font-size: 0.82rem; opacity: 0.58; letter-spacing: 3px; }
.login-tabs { display: flex; gap: 6px; margin-bottom: 20px; padding: 4px; border-radius: 12px; background: rgba(255,255,255,0.05); }
.login-tab { flex: 1; padding: 9px; border-radius: 10px; border: none; background: transparent; color: rgba(255,255,255,0.56); cursor: pointer; }
.login-tab.active { background: rgba(255,255,255,0.12); color: #fff; }
.login-panel { display: flex; flex-direction: column; gap: 10px; }
label { font-size: 0.82rem; opacity: 0.72; }
input { width: 100%; box-sizing: border-box; padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.15); background: rgba(255,255,255,0.06); color: #fff; outline: none; }
input:focus { border-color: rgba(255,255,255,0.42); }
.remember-me { display: inline-flex; align-items: center; gap: 8px; margin: 2px 0 8px; }
.remember-me input { width: auto; }
.login-btn { padding: 13px 18px; border-radius: 14px; border: 1px solid rgba(168,188,212,0.42); background: linear-gradient(135deg, rgba(120,144,181,0.35), rgba(168,188,212,0.18)); color: #e8eef7; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; gap: 8px; letter-spacing: 3px; }
.login-btn:disabled { opacity: 0.6; cursor: wait; }
.oauth-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.oauth-btn { display: flex; align-items: center; justify-content: center; padding: 14px 4px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.12); background: rgba(255,255,255,0.06); color: #fff; cursor: pointer; }
.brand-icon { width: 1.5rem; height: 1.5rem; fill: currentColor; }
@media (max-width: 768px) { .login-wrap { align-items: flex-start; padding-top: 40px; } .login-card { padding: 24px 18px; } }
</style>
