<template>
  <div class="about-page">
    <!-- 动态星空背景 -->
    <div class="stars-layer stars-small"></div>
    <div class="stars-layer stars-medium"></div>
    <div class="stars-layer stars-large"></div>
    <div class="nebula"></div>

    <header class="page-fixed about-header">
      <router-link to="/" class="back-btn" title="返回星域">
        <span class="back-orbit"></span>
        <span class="back-core"><i class="ri-arrow-left-line"></i></span>
      </router-link>
      <h1 class="about-title">关于站长</h1>
    </header>

    <main class="about-content">
      <div class="about-card">
        <!-- 顶部扫描线 -->
        <div class="card-scanline"></div>

        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-ring orbit1"></div>
          <div class="avatar-ring orbit2"></div>
          <div class="avatar-wrapper">
            <div class="avatar-glow"></div>
            <img :src="avatar" alt="站长头像" class="avatar" />
          </div>
        </div>

        <!-- 基本信息 -->
        <h1 class="name">{{ name }}</h1>
        <p class="bio">{{ bio }}</p>

        <!-- 技术方向 -->
        <div class="section">
          <h3 class="section-title"><i class="ri-code-s-slash-line"></i> 技术方向</h3>
          <div class="tags">
            <span v-for="tech in techStack" :key="tech" class="tech-tag">{{ tech }}</span>
          </div>
        </div>

        <!-- 友情链接 -->
        <div class="section">
          <h3 class="section-title"><i class="ri-links-line"></i> 友情链接</h3>
          <div class="link-grid">
            <a
                v-for="link in links"
                :key="link.name"
                :href="link.url"
                target="_blank"
                rel="noopener"
                class="link-item"
            >
              <span class="link-icon">{{ link.icon }}</span>
              <span class="link-name">{{ link.name }}</span>
              <span class="link-arrow"><i class="ri-arrow-right-up-line"></i></span>
            </a>
          </div>
        </div>

        <!-- 联系方式 -->
        <div class="section contact-section">
          <h3 class="section-title"><i class="ri-mail-line"></i> 联系方式</h3>
          <div class="contact-info">
            <span class="email"><i class="ri-at-line"></i> {{ email }}</span>
            <span class="note">—— 欢迎技术交流与合作 ——</span>
          </div>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const avatar = ref('https://avatars.githubusercontent.com/u/your-avatar')
const name = ref('站长名称')
const bio = ref('全栈开发者 | 技术爱好者 | 终身学习者')

const techStack = ref([
  'Java',
  'Spring Boot',
  'Vue.js',
  'React',
  'MySQL',
  'Redis',
  'Docker',
  'Linux'
])

const links = ref([
  { name: 'GitHub', url: 'https://github.com/yourusername', icon: '🐙' },
  { name: 'Gitee', url: 'https://gitee.com/yourusername', icon: '🐒' },
  { name: '知乎', url: 'https://zhihu.com/people/yourusername', icon: '💡' },
  { name: '掘金', url: 'https://juejin.cn/user/yourusername', icon: '📝' }
])

const email = ref('contact@example.com')
</script>

<style scoped>
/* ========== 全局基础 ========== */
.about-page {
  min-height: 100vh;
  background: radial-gradient(ellipse at 50% 0%, #111532 0%, #0a0c1d 60%, #050712 100%);
  color: #e0ecff;
  font-family: 'Noto Serif SC', 'Segoe UI', 'Microsoft YaHei', sans-serif;
  position: relative;
  overflow-x: hidden;
}

/* ---------- 动态星空层 ---------- */
.stars-layer {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
  z-index: 0;
}

.stars-small {
  background-image: radial-gradient(1px 1px at 5% 10%, rgba(255,255,255,0.4), transparent),
  radial-gradient(1px 1px at 15% 25%, rgba(255,255,255,0.3), transparent),
  radial-gradient(1px 1px at 25% 8%, rgba(255,255,255,0.35), transparent),
  radial-gradient(1px 1px at 35% 40%, rgba(255,255,255,0.25), transparent),
  radial-gradient(1px 1px at 45% 15%, rgba(255,255,255,0.4), transparent),
  radial-gradient(1px 1px at 55% 35%, rgba(255,255,255,0.3), transparent),
  radial-gradient(1px 1px at 65% 5%, rgba(255,255,255,0.35), transparent),
  radial-gradient(1px 1px at 75% 30%, rgba(255,255,255,0.2), transparent),
  radial-gradient(1px 1px at 85% 12%, rgba(255,255,255,0.4), transparent),
  radial-gradient(1px 1px at 95% 28%, rgba(255,255,255,0.3), transparent);
  animation: twinkle 4s ease-in-out infinite alternate;
}
.stars-medium {
  background-image: radial-gradient(2px 2px at 10% 60%, rgba(210,220,255,0.5), transparent),
  radial-gradient(2px 2px at 30% 70%, rgba(200,210,255,0.4), transparent),
  radial-gradient(2px 2px at 50% 55%, rgba(220,230,255,0.45), transparent),
  radial-gradient(2px 2px at 70% 65%, rgba(200,215,255,0.4), transparent),
  radial-gradient(2px 2px at 90% 50%, rgba(210,220,255,0.5), transparent);
  animation: twinkle 6s ease-in-out infinite alternate-reverse;
}
.stars-large {
  background-image: radial-gradient(3px 3px at 20% 15%, rgba(255,245,200,0.6), transparent),
  radial-gradient(3px 3px at 60% 25%, rgba(240,240,255,0.5), transparent),
  radial-gradient(3px 3px at 80% 80%, rgba(255,225,150,0.55), transparent);
  animation: twinkle 8s ease-in-out infinite alternate;
}
@keyframes twinkle {
  0% { opacity: 0.5; }
  100% { opacity: 1; }
}

/* 星云光晕 */
.nebula {
  position: fixed;
  top: -20%; left: -20%;
  width: 140%; height: 140%;
  background: radial-gradient(circle at 30% 40%, rgba(80, 120, 200, 0.06) 0%, transparent 50%),
  radial-gradient(circle at 70% 60%, rgba(140, 100, 180, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
  animation: nebulaDrift 20s linear infinite;
}
@keyframes nebulaDrift {
  0% { transform: translate(0, 0); }
  100% { transform: translate(20px, 15px); }
}

/* ---------- 头部导航 ---------- */
.about-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 30px;
  position: relative;
  z-index: 10;
}

/* 返回按钮 - 星轨样式 */
.back-btn {
  position: relative;
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(15, 25, 45, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(100, 180, 255, 0.25);
  transition: transform 0.3s, box-shadow 0.3s;
  text-decoration: none;
  flex-shrink: 0;
}
.back-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 0 16px rgba(80, 160, 255, 0.4), 0 0 30px rgba(80, 160, 255, 0.2);
}
.back-orbit {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 1px dashed rgba(197, 213, 234, 0.5);
  animation: orbitSpin 12s linear infinite;
}
@keyframes orbitSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.back-core {
  color: #c8d8ff;
  font-size: 1.2rem;
  z-index: 1;
  transition: color 0.3s;
}
.back-btn:hover .back-core {
  color: #fff;
}

.about-title {
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  font-size: 1.4rem;
  font-weight: 500;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #fff 0%, #c0b0f0 50%, #fff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: titleGlow 3s ease-in-out infinite alternate;
}
@keyframes titleGlow {
  from { filter: drop-shadow(0 0 8px rgba(160, 140, 240, 0.4)); }
  to { filter: drop-shadow(0 0 16px rgba(180, 160, 255, 0.6)); }
}

/* ---------- 主内容 ---------- */
.about-content {
  position: relative;
  z-index: 5;
  max-width: 760px;
  margin: 0 auto;
  padding: 20px 20px 100px;
}

.about-card {
  background: linear-gradient(135deg, rgba(15, 25, 50, 0.55) 0%, rgba(10, 18, 36, 0.65) 100%);
  backdrop-filter: blur(25px) saturate(1.4);
  -webkit-backdrop-filter: blur(25px) saturate(1.4);
  border: 1px solid rgba(120, 180, 255, 0.15);
  border-radius: 28px;
  padding: 50px 40px;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6),
  inset 0 0 0 1px rgba(100, 180, 255, 0.08),
  0 0 80px rgba(60, 120, 200, 0.1);
  position: relative;
  overflow: hidden;
  animation: cardAppear 0.7s cubic-bezier(0.23, 1, 0.32, 1) both;
}
@keyframes cardAppear {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-scanline {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(100, 180, 255, 0.6), transparent);
  opacity: 0.5;
}

/* ---------- 头像区 ---------- */
.avatar-section {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 30px;
}
.avatar-ring {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  border: 1px dashed rgba(180, 200, 255, 0.25);
  animation: ringSpin 20s linear infinite;
}
.orbit2 {
  inset: -18px;
  border-style: solid;
  border-color: rgba(140, 180, 240, 0.15);
  animation-direction: reverse;
  animation-duration: 30s;
}
@keyframes ringSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.avatar-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.avatar-glow {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  width: 160px; height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(160, 140, 240, 0.3) 0%, rgba(80, 100, 180, 0.1) 50%, transparent 70%);
  animation: pulseGlow 3s ease-in-out infinite;
}
@keyframes pulseGlow {
  0%, 100% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgba(180, 160, 255, 0.5);
  object-fit: cover;
  position: relative;
  z-index: 1;
  box-shadow: 0 0 25px rgba(180, 160, 255, 0.3), 0 0 50px rgba(100, 120, 200, 0.2);
  animation: avatarFloat 6s ease-in-out infinite;
}
@keyframes avatarFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* ---------- 名字与简介 ---------- */
.name {
  text-align: center;
  font-size: 2rem;
  font-weight: 600;
  letter-spacing: 4px;
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  background: linear-gradient(135deg, #fff 0%, #d4c0ff 50%, #fff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 12px;
}
.bio {
  text-align: center;
  font-size: 1rem;
  color: rgba(200, 215, 240, 0.8);
  margin: 0 0 40px;
  line-height: 1.6;
}

/* ---------- 通用区块 ---------- */
.section {
  margin-bottom: 36px;
}
.section-title {
  font-size: 1.05rem;
  font-weight: 500;
  letter-spacing: 2px;
  color: rgba(200, 215, 250, 0.9);
  margin: 0 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title i {
  font-size: 1.1rem;
  opacity: 0.8;
}

/* ---------- 技术标签 ---------- */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tech-tag {
  background: linear-gradient(135deg, rgba(80, 150, 240, 0.2) 0%, rgba(120, 130, 220, 0.15) 100%);
  color: rgba(210, 225, 255, 0.9);
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 0.85rem;
  letter-spacing: 1px;
  border: 1px solid rgba(100, 180, 255, 0.2);
  backdrop-filter: blur(6px);
  transition: all 0.3s ease;
  cursor: default;
}
.tech-tag:hover {
  background: linear-gradient(135deg, rgba(90, 170, 255, 0.4) 0%, rgba(140, 150, 240, 0.3) 100%);
  border-color: rgba(120, 200, 255, 0.5);
  box-shadow: 0 0 18px rgba(80, 160, 255, 0.35);
  transform: translateY(-2px);
}

/* ---------- 友情链接 ---------- */
.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}
.link-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  text-decoration: none;
  color: rgba(210, 220, 245, 0.8);
  transition: all 0.3s ease;
  backdrop-filter: blur(6px);
  position: relative;
  overflow: hidden;
}
.link-item::after {
  content: '';
  position: absolute;
  top: 0; left: -100%;
  width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.05), transparent);
  transition: left 0.5s;
}
.link-item:hover::after {
  left: 100%;
}
.link-item:hover {
  background: rgba(100, 150, 220, 0.12);
  border-color: rgba(120, 180, 240, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(80, 160, 255, 0.2);
}
.link-icon {
  font-size: 1.3rem;
}
.link-name {
  font-size: 0.9rem;
  font-weight: 500;
  flex: 1;
}
.link-arrow {
  font-size: 0.8rem;
  opacity: 0;
  transition: all 0.3s;
  color: rgba(180, 200, 255, 0.7);
}
.link-item:hover .link-arrow {
  opacity: 1;
  transform: translate(2px, -2px);
}

/* ---------- 联系方式 ---------- */
.contact-section .section-title {
  margin-bottom: 12px;
}
.contact-info {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}
.email {
  font-size: 1rem;
  color: rgba(200, 215, 240, 0.8);
  display: flex;
  align-items: center;
  gap: 6px;
}
.email i {
  font-size: 1.1rem;
}
.note {
  font-size: 0.85rem;
  color: rgba(180, 195, 220, 0.6);
  letter-spacing: 1px;
}

</style>