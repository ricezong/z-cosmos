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

        <div class="login-tabs">
          <div class="login-tab" :class="{ active: tab === 'account' }" @click="tab = 'account'">账号登录</div>
          <div class="login-tab" :class="{ active: tab === 'qr' }" @click="tab = 'qr'">扫码登录</div>
        </div>

        <!-- 账号登录 -->
        <div class="login-panel" v-show="tab === 'account'">
          <div class="form-group">
            <label>手机号</label>
            <input type="tel" v-model="loginForm.phone" placeholder="请输入手机号" maxlength="11">
          </div>
          <div class="form-group">
            <label>密码</label>
            <input type="password" v-model="loginForm.pwd" placeholder="请输入密码">
          </div>
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.remember">
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link" @click.prevent="alert('请联系管理员重置密码')">忘记密码?</a>
          </div>
          <button class="login-btn" @click="doLogin">
            <span class="btn-shine"></span>
            <span class="btn-content">
              <span class="btn-bracket left">⟨</span>
              <span class="btn-text">登 录 星 域</span>
              <i class="ri-arrow-right-up-line btn-arrow"></i>
              <span class="btn-bracket right">⟩</span>
            </span>
          </button>

          <div class="divider">
            <span>第三方账号登录</span>
          </div>

          <div class="oauth-grid">
            <button class="oauth-btn" @click="oauthLogin('QQ')">
              <span class="icon" style="color:#12b7f5"><svg class="brand-icon" aria-hidden="true"><use href="/icons.svg#qq-icon"/></svg></span>
            </button>
            <button class="oauth-btn" @click="oauthLogin('GitHub')">
              <span class="icon" style="color:#fff"><svg class="brand-icon" aria-hidden="true"><use href="/icons.svg#github-icon"/></svg></span>
            </button>
            <button class="oauth-btn" @click="oauthLogin('Gitee')">
              <span class="icon" style="color:#C71D23"><svg class="brand-icon" aria-hidden="true"><use href="/icons.svg#gitee-icon"/></svg></span>
            </button>
          </div>
        </div>

        <!-- 扫码登录 -->
        <div class="login-panel" v-show="tab === 'qr'">
          <div class="qr-panel">
            <div class="qr-box">
              <div class="qr-pattern"></div>
              <div class="qr-extra"></div>
            </div>
            <div class="qr-hint">
              请使用手机微信扫码登录<br>
              <span style="opacity:0.5;font-size:0.75rem">二维码每 5 分钟自动刷新</span>
            </div>
            <button class="qr-refresh" @click="refreshQR">刷新二维码</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const tab = ref('account')
const loginForm = ref({ phone: '', pwd: '', remember: false })

function doLogin() {
  const { phone, pwd } = loginForm.value
  if (!phone.trim() || !pwd.trim()) { alert('请输入手机号和密码'); return }
  localStorage.setItem('cosmos_logged_in', 'true')
  if (loginForm.value.remember) localStorage.setItem('cosmos_phone', phone)
  alert('欢迎回来！\n\n（演示模式）')
  router.push('/')
}

function oauthLogin(platform) {
  alert('即将通过 ' + platform + ' 授权登录...\n\n（演示模式）')
}

function refreshQR() {
  const box = document.querySelector('.qr-box')
  if (box) { box.style.opacity = '0.3'; setTimeout(() => { box.style.opacity = '1' }, 300) }
}

onMounted(() => {
  const saved = localStorage.getItem('cosmos_phone')
  if (saved) { loginForm.value.phone = saved; loginForm.value.remember = true }
})
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
/* 卡片顶部极淡光软 */
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
  /* 使用 text-shadow 代替 drop-shadow，避免帧帧重新栅格化 */
  text-shadow: 0 0 18px rgba(200, 180, 255, 0.55);
  display: block;
  color: #fff;
  margin-bottom: 14px;
  animation: logoFloat 4.5s ease-in-out infinite;
  will-change: transform;
}
/* 仅动 transform，移除 filter 动画；光晕呼吸交给子元素的 opacity */
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

.login-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 4px;
}

.login-tab {
  flex: 1;
  text-align: center;
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.85rem;
  transition: 0.3s;
  border: none;
  background: transparent;
}

.login-tab.active {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.08);
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

.form-group input {
  width: 100%;
  padding: 11px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 0.9rem;
  outline: none;
  font-family: inherit;
  transition: 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.1);
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 0.8rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
}

.remember-me input {
  width: 14px;
  height: 14px;
  accent-color: #fff;
}

.forgot-link {
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  transition: 0.3s;
}

.forgot-link:hover {
  color: #fff;
}

.login-btn {
  width: 100%;
  padding: 14px 18px;
  border-radius: 14px;
  border: 1px solid rgba(168,188,212,0.4);
  background:
    linear-gradient(135deg, rgba(120,144,181,0.22) 0%, rgba(168,188,212,0.12) 50%, rgba(120,144,181,0.22) 100%),
    linear-gradient(180deg, rgba(22,26,48,0.92) 0%, rgba(14,18,36,0.95) 100%);
  color: #e8eef7;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  letter-spacing: 4px;
  position: relative;
  overflow: hidden;
  transition: 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.12),
    inset 0 -1px 0 rgba(0,0,0,0.25),
    0 4px 16px rgba(0,0,0,0.3);
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
}

/* 顶部细金属光带 */
.login-btn::before {
  content: '';
  position: absolute;
  top: 0; left: 12%; right: 12%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.55), transparent);
  pointer-events: none;
}

/* 底部雾青辉 */
.login-btn::after {
  content: '';
  position: absolute;
  bottom: 0; left: 20%; right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(168,188,212,0.5), transparent);
  pointer-events: none;
}

/* hover 扫光 */
.btn-shine {
  position: absolute;
  top: 0; left: -60%;
  width: 40%; height: 100%;
  background: linear-gradient(100deg, transparent 30%, rgba(255,255,255,0.22) 50%, transparent 70%);
  transform: skewX(-20deg);
  pointer-events: none;
  transition: left 0.75s ease;
}

.login-btn:hover .btn-shine { left: 120%; }

.login-btn:hover {
  border-color: rgba(168,188,212,0.7);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.18),
    inset 0 -1px 0 rgba(0,0,0,0.25),
    0 8px 24px rgba(120,144,181,0.35),
    0 0 32px rgba(168,188,212,0.2);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow:
    inset 0 2px 4px rgba(0,0,0,0.3),
    0 4px 12px rgba(0,0,0,0.2);
}

.btn-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.btn-bracket {
  font-size: 1.15em;
  color: rgba(168,188,212,0.7);
  font-weight: 300;
  transition: 0.35s ease;
}

.login-btn:hover .btn-bracket.left {
  transform: translateX(-4px);
  color: #c5d5ea;
  text-shadow: 0 0 8px rgba(168,188,212,0.6);
}

.login-btn:hover .btn-bracket.right {
  transform: translateX(4px);
  color: #c5d5ea;
  text-shadow: 0 0 8px rgba(168,188,212,0.6);
}

.btn-text {
  background: linear-gradient(135deg, #ffffff 0%, #c5d5ea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.btn-arrow {
  font-size: 1rem;
  color: #c5d5ea;
  transition: transform 0.35s ease;
}

.login-btn:hover .btn-arrow {
  transform: translate(3px, -3px);
}

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 18px 0;
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

.oauth-grid {
  display: flex;
  flex-direction: row;
  gap: 10px;
}

.oauth-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 4px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: 0.3s;
  text-decoration: none;
}

.oauth-btn:hover {
  border-color: rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.oauth-btn .icon {
  font-size: 1.4rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.oauth-btn .brand-icon {
  width: 1em;
  height: 1em;
  display: block;
  fill: currentColor;
}

.oauth-btn .icon svg {
  display: inline-block;
  vertical-align: -0.15em;
}

.qr-panel {
  text-align: center;
  padding: 8px 0;
}

.qr-box {
  width: 170px;
  height: 170px;
  margin: 0 auto 16px;
  background: white;
  border-radius: 14px;
  padding: 10px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-pattern {
  width: 100%;
  height: 100%;
  background:
    repeating-linear-gradient(0deg, #1a1a1a 0, #1a1a1a 4px, transparent 4px, transparent 8px),
    repeating-linear-gradient(90deg, #1a1a1a 0, #1a1a1a 4px, transparent 4px, transparent 8px);
  background-size: 8px 8px;
  border-radius: 6px;
}

.qr-pattern::before {
  content: '';
  position: absolute;
  width: 26px;
  height: 26px;
  border: 3px solid #1a1a1a;
  border-radius: 3px;
  top: 16px;
  left: 16px;
}

.qr-pattern::after {
  content: '';
  position: absolute;
  width: 26px;
  height: 26px;
  border: 3px solid #1a1a1a;
  border-radius: 3px;
  bottom: 16px;
  right: 16px;
}

.qr-extra {
  position: absolute;
  width: 26px;
  height: 26px;
  border: 3px solid #1a1a1a;
  border-radius: 3px;
  top: 16px;
  right: 16px;
}

.qr-hint {
  font-size: 0.85rem;
  opacity: 0.6;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.6);
}

.qr-refresh {
  margin-top: 12px;
  padding: 7px 18px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 0.8rem;
  transition: 0.3s;
}

.qr-refresh:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

@media (max-width: 768px) {
  .login-wrap {
    padding: 12px;
    align-items: flex-start;
    padding-top: 40px;
  }
  .login-card {
    padding: 20px 16px;
    border-radius: 16px;
  }
  .login-logo .planet-icon {
    font-size: 38px;
  }
  .login-logo h1 {
    font-size: 1.3rem;
  }
  .login-tab {
    font-size: 0.8rem;
    padding: 7px;
  }
  .form-group input {
    padding: 10px 12px;
    font-size: 0.85rem;
  }
  .oauth-btn {
    padding: 10px 2px;
  }
  .oauth-btn .icon {
    font-size: 1.2rem;
  }
  .qr-box {
    width: 150px;
    height: 150px;
  }
}
</style>
