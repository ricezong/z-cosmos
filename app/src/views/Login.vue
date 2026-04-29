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
          <button class="login-btn" @click="doLogin">登录</button>

          <div class="divider">
            <span>第三方账号登录</span>
          </div>

          <div class="oauth-grid">
            <button class="oauth-btn" @click="oauthLogin('QQ')">
              <span class="icon" style="color:#12b7f5"><i class="ri-qq-line"></i></span>
            </button>
            <button class="oauth-btn" @click="oauthLogin('GitHub')">
              <span class="icon" style="color:#fff"><i class="ri-github-fill"></i></span>
            </button>
            <button class="oauth-btn" @click="oauthLogin('Gitee')">
              <span class="icon gitee-icon"><svg viewBox="0 0 1024 1024" width="20" height="20"><path d="M512 42.666667C252.793333 42.666667 42.666667 252.793333 42.666667 512s210.126667 469.333333 469.333333 469.333333 469.333333-210.126667 469.333333-469.333333S771.206667 42.666667 512 42.666667z m263.346667 351.466666l-23.04 78.506667c-4.693333 15.786667-21.76 24.746667-37.546667 20.053333L440.32 423.253333c-2.986667-0.853333-5.973333-1.28-8.96-1.28-13.226667 0-25.6 8.533333-29.866667 21.76l-2.133333 7.253334-36.266667 123.733333c-4.693333 15.786667 4.266667 32.853333 20.053334 37.546667l275.2 68.693333c2.986667 0.853333 5.973333 1.28 8.96 1.28 13.226667 0 25.6-8.533333 29.866666-21.76l23.04-78.506667c4.693333-15.786667-4.266667-32.853333-20.053333-37.546666L445.44 476.586667c-2.986667-0.853333-5.973333-1.28-8.96-1.28-13.226667 0-25.6 8.533333-29.866667 21.76" fill="#C71D23"/></svg></span>
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
  min-height: 100vh;
  overflow: hidden;
  background: #050b1a;
  color: #e0e8f0;
  position: relative;
}

.login-wrap {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: rgba(10, 15, 30, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 32px 28px;
  backdrop-filter: blur(16px);
  animation: fadeUp 0.5s ease;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-logo {
  text-align: center;
  margin-bottom: 24px;
}

.login-logo .planet-icon {
  font-size: 48px;
  line-height: 1;
  filter: drop-shadow(0 0 12px rgba(255,255,255,0.5));
  display: block;
  color: #fff;
  margin-bottom: 12px;
}

.login-logo h1 {
  font-size: 1.5rem;
  font-weight: 300;
  letter-spacing: 4px;
  color: #fff;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
  margin-bottom: 4px;
}

.login-logo p {
  font-size: 0.8rem;
  opacity: 0.5;
  color: #c0d0e8;
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
  padding: 12px;
  border-radius: 24px;
  border: none;
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(200,220,255,0.9));
  color: #050b1a;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: 0.3s;
  letter-spacing: 1px;
}

.login-btn:hover {
  box-shadow: 0 0 25px rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
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
