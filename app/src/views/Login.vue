<template>
  <div class="login-page">
    <div class="star-bg"></div>
    <div class="login-wrap">
      <div class="login-card">
        <div class="login-logo">
          <div class="planet-icon"><i class="ri-shield-star-line"></i></div>
          <h1>COSMOS</h1>
          <p>星际漫游 · 登录星域</p>
        </div>

        <!-- 扫码登录 -->
        <div class="qr-panel">
          <div class="qr-box">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="扫码登录" class="qr-img">
            <div v-else class="qr-placeholder">
              <i class="ri-qr-code-line"></i>
              <span>正在获取二维码...</span>
            </div>
            <!-- 状态遮罩 -->
            <div v-if="qrStatus === 'EXPIRED'" class="qr-overlay" @click="generateQr">
              <i class="ri-refresh-line"></i>
              <span>已过期，点击刷新</span>
            </div>
            <div v-if="qrStatus === 'SCANNED'" class="qr-overlay scanned">
              <i class="ri-checkbox-circle-line"></i>
              <span>扫码成功，请确认登录</span>
            </div>
            <div v-if="qrStatus === 'SUCCESS'" class="qr-overlay success">
              <i class="ri-checkbox-circle-fill"></i>
              <span>登录成功</span>
            </div>
            <button v-if="qrStatus !== 'SUCCESS'" class="qr-refresh" @click="generateQr" title="刷新二维码">
              <i class="ri-refresh-line"></i>
            </button>
          </div>
          <div class="qr-hint">请使用手机微信扫码登录</div>
        </div>

        <div class="divider">
          <span>其他方式登录</span>
        </div>

        <div class="oauth-grid">
          <div class="oauth-btn" v-for="item in oauthProviders" :key="item.provider"
               @click="oauthLogin(item.provider)" :style="{ '--hover-color': item.color }">
            <span class="oauth-icon">
              <svg class="brand-icon" viewBox="0 0 1024 1024" v-html="item.svg"></svg>
            </span>
            <span class="oauth-tooltip">{{ item.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOAuthUrl } from '../api/auth'
import { useAuth } from '../composables/useAuth'
import { useToast } from '../composables/useToast'
import { icons } from '../assets/icons'

const router = useRouter()
const toast = useToast()
const { isLoggedIn, authReady, setToken } = useAuth()

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

const qrCodeUrl = ref('')
const qrStatus = ref('') // '' | 'WAIT_SCAN' | 'SCANNED' | 'SUCCESS' | 'EXPIRED'
let sessionId = ''
let pollTimer = null

/** 请求后端生成微信登录二维码 */
async function generateQr() {
  stopPolling()
  qrStatus.value = ''
  qrCodeUrl.value = ''
  sessionId = ''

  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/wechat/qr/generate`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
    })
    const payload = await res.json()
    if (res.ok && payload.code === 200 && payload.data) {
      sessionId = payload.data.sessionId
      qrCodeUrl.value = payload.data.qrCodeUrl
      qrStatus.value = 'WAIT_SCAN'
      startPolling()
    } else {
      toast.error('获取二维码失败')
    }
  } catch {
    toast.error('获取二维码失败，请检查网络')
  }
}

/** 轮询登录状态 */
function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!sessionId) return
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/wechat/qr/status`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId }),
      })
      const payload = await res.json()
      if (res.ok && payload.code === 200 && payload.data) {
        const status = payload.data.status
        qrStatus.value = status

        if (status === 'SUCCESS') {
          stopPolling()
          if (payload.data.accessToken) {
            setToken(payload.data.accessToken)
            toast.success('登录成功！')
            setTimeout(() => router.replace('/'), 800)
          }
        } else if (status === 'EXPIRED') {
          stopPolling()
        }
      }
    } catch {
      // 轮询失败静默处理
    }
  }, 2500)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  generateQr()
})

onUnmounted(() => {
  stopPolling()
})

const oauthProviders = [
  { provider: 'qq', name: 'QQ', color: '#12b7f5', svg: icons.qq },
  { provider: 'github', name: 'GitHub', color: '#ffffff', svg: icons.github },
  { provider: 'gitee', name: 'Gitee', color: '#C71D23', svg: icons.gitee },
]

async function oauthLogin(provider) {
  try {
    const data = await getOAuthUrl(provider)
    if (data && data.authUrl) {
      window.location.href = data.authUrl
    } else {
      toast.error('获取授权链接失败')
    }
  } catch (e) {
    toast.error('获取授权链接失败: ' + (e.message || '请检查配置'))
  }
}

// authReady 后如果是已登录状态，自动跳转首页
watch(authReady, (ready) => {
  if (ready && isLoggedIn.value) {
    router.replace('/')
  }
}, { immediate: true })
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  background:
    radial-gradient(ellipse 90% 60% at 50% 0%, rgba(82, 72, 128, 0.35) 0%, transparent 55%),
    radial-gradient(ellipse 70% 50% at 50% 100%, rgba(120, 90, 150, 0.22) 0%, transparent 60%),
    linear-gradient(180deg, #0d0f24 0%, #08091a 100%);
  color: #e0e8f0;
  position: relative;
}

.login-wrap {
  position: relative;
  z-index: 1;
  min-height: 100%;
  display: flex;
  align-items: safe center;
  justify-content: center;
  padding: 24px 16px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 380px;
  background:
    linear-gradient(155deg, rgba(28, 24, 56, 0.78) 0%, rgba(12, 14, 32, 0.68) 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 36px 30px;
  backdrop-filter: blur(18px);
  box-shadow:
    0 24px 60px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  animation: fadeUp 0.65s var(--ease-out-expo, cubic-bezier(0.19, 1, 0.22, 1));
  position: relative;
}
.login-card::before {
  content: '';
  position: absolute;
  top: 0; left: 20%; right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.35), transparent);
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-logo {
  text-align: center;
  margin-bottom: 28px;
}

.login-logo .planet-icon {
  font-size: 48px;
  line-height: 1;
  text-shadow: 0 0 18px rgba(200, 180, 255, 0.55);
  display: block;
  color: #fff;
  margin-bottom: 14px;
  animation: logoFloat 4.5s ease-in-out infinite;
  will-change: transform;
}
@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.login-logo h1 {
  font-size: 1.6rem;
  font-weight: 500;
  letter-spacing: 8px;
  color: #fff;
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
  text-shadow: 0 0 24px rgba(200, 180, 255, 0.35), 0 0 2px rgba(255,255,255,0.4);
  margin-bottom: 6px;
}

.login-logo p {
  font-size: 0.78rem;
  opacity: 0.55;
  color: #c0d0e8;
  letter-spacing: 3px;
  text-transform: uppercase;
  font-family: var(--font-display, 'Cinzel', serif);
}

/* QR Panel */
.qr-panel {
  text-align: center;
  padding: 8px 0 4px;
}

.qr-box {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  overflow: hidden;
}

.qr-img {
  width: 100%;
  height: 100%;
  background: white;
  padding: 10px;
  display: block;
  object-fit: contain;
  box-sizing: border-box;
}

.qr-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.06);
  border: 1px dashed rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.35);
}
.qr-placeholder i {
  font-size: 2.5rem;
}
.qr-placeholder span {
  font-size: 0.78rem;
}

/* 状态遮罩 */
.qr-overlay {
  position: absolute;
  inset: 0;
  background: rgba(5, 8, 20, 0.85);
  backdrop-filter: blur(4px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: 0.3s;
  z-index: 3;
  border-radius: 14px;
}
.qr-overlay i {
  font-size: 2rem;
}
.qr-overlay span {
  font-size: 0.82rem;
}
.qr-overlay.scanned {
  color: #7dcf8c;
  cursor: default;
}
.qr-overlay.success {
  color: #5ec26e;
  cursor: default;
}

.qr-refresh {
  position: absolute;
  bottom: 6px;
  right: 6px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(18, 16, 36, 0.8);
  backdrop-filter: blur(6px);
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  transition: 0.25s;
  z-index: 4;
}
.qr-refresh:hover {
  background: rgba(18, 16, 36, 0.95);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  transform: rotate(180deg);
}

.qr-hint {
  font-size: 0.85rem;
  opacity: 0.6;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.6);
}

/* Divider */
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0 18px;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.35);
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

/* OAuth Grid */
.oauth-grid {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.oauth-btn {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.oauth-btn:hover {
  border-color: var(--hover-color, rgba(255, 255, 255, 0.3));
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3), 0 0 12px color-mix(in srgb, var(--hover-color, #fff) 25%, transparent);
}

.oauth-icon {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.75);
  transition: color 0.3s;
}

.oauth-btn:hover .oauth-icon {
  color: var(--hover-color, #fff);
}

.brand-icon {
  width: 100%;
  height: 100%;
  display: block;
}

/* Tooltip */
.oauth-tooltip {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%) translateY(4px);
  padding: 5px 12px;
  border-radius: 8px;
  background: rgba(18, 16, 36, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #e8eef7;
  font-size: 0.75rem;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.25s, transform 0.25s;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.oauth-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-top-color: rgba(18, 16, 36, 0.95);
}

.oauth-btn:hover .oauth-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

@media (max-width: 768px) {
  .login-wrap {
    padding: 12px;
    align-items: flex-start;
    padding-top: 40px;
  }
  .login-card {
    padding: 24px 20px;
    border-radius: 16px;
  }
  .login-logo .planet-icon {
    font-size: 38px;
  }
  .login-logo h1 {
    font-size: 1.3rem;
  }
  .qr-box {
    width: 160px;
    height: 160px;
  }
  .oauth-btn {
    width: 40px;
    height: 40px;
  }
  .oauth-icon {
    width: 20px;
    height: 20px;
  }
}
</style>