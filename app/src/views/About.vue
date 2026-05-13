<template>
  <div class="about-page">
    <div class="star-bg"></div>
    <header class="page-fixed about-header">
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
      <h1 class="about-title">关于站长</h1>
    </header>

    <main class="about-content">
      <div class="about-card">
        <!-- 头像 -->
        <div class="avatar-wrapper">
          <div class="avatar-glow"></div>
          <img :src="avatar" alt="站长头像" class="avatar" />
        </div>

        <!-- 基本信息 -->
        <h1 class="name">{{ name }}</h1>
        <p class="bio">{{ bio }}</p>

        <!-- 技术方向 -->
        <div class="section">
          <h3 class="section-title"><i class="ri-code-s-slash-line"></i> 技术方向</h3>
          <div class="tags">
            <span v-for="tech in techStack" :key="tech" class="tech-tag">
              {{ tech }}
            </span>
          </div>
        </div>

        <!-- 链接 -->
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
            </a>
          </div>
        </div>

        <!-- 联系信息 -->
        <div class="section">
          <h3 class="section-title"><i class="ri-mail-line"></i> 联系方式</h3>
          <p class="email">邮箱：{{ email }}</p>
          <p class="note">欢迎技术交流与合作</p>
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
.about-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #0d0f24 0%, #0a0c1d 45%, #07081a 100%);
  color: var(--ink-100, #efecfb);
  font-family: var(--font-body, 'Noto Serif SC', 'Segoe UI', 'Microsoft YaHei', sans-serif);
  position: relative;
  overflow-x: hidden;
}

.star-bg {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
  z-index: 0;
  background-image:
    radial-gradient(1.5px 1.5px at 20px 30px, rgba(210,220,255,0.45), transparent),
    radial-gradient(2px 2px at 90px 40px, rgba(220,225,255,0.55), transparent),
    radial-gradient(1.5px 1.5px at 200px 120px, rgba(200,215,255,0.4), transparent),
    radial-gradient(2px 2px at 300px 60px, rgba(230,235,255,0.55), transparent),
    radial-gradient(1px 1px at 450px 100px, rgba(220,225,255,0.35), transparent),
    radial-gradient(2.5px 2.5px at 620px 50px, rgba(240,240,255,0.6), transparent),
    radial-gradient(2px 2px at 780px 90px, rgba(255,225,185,0.45), transparent),
    radial-gradient(1.5px 1.5px at 920px 140px, rgba(235,235,255,0.5), transparent),
    radial-gradient(2px 2px at 150px 280px, rgba(255,220,180,0.45), transparent),
    radial-gradient(2.5px 2.5px at 400px 320px, rgba(200,215,255,0.55), transparent),
    radial-gradient(2px 2px at 650px 300px, rgba(255,225,190,0.45), transparent),
    radial-gradient(1.5px 1.5px at 880px 280px, rgba(210,220,255,0.4), transparent);
}

.about-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 30px;
  position: relative;
  z-index: 10;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--ink-200, rgba(239,236,251,0.72));
  text-decoration: none;
  font-size: 0.9rem;
  padding: 8px 18px;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.1);
  background: rgba(255,255,255,0.04);
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
}
.back-btn:hover {
  color: #fff;
  border-color: rgba(180,160,255,0.35);
  background: rgba(180,160,255,0.12);
  box-shadow: 0 0 16px rgba(180,160,255,0.2);
}

.about-title {
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
  font-size: 1.3rem;
  font-weight: 500;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #fff 0%, #c8b8ff 50%, #fff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.about-content {
  position: relative;
  z-index: 5;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 20px 100px;
}

.about-card {
  background: linear-gradient(135deg, rgba(15, 28, 52, 0.65) 0%, rgba(12, 20, 38, 0.75) 100%);
  backdrop-filter: blur(20px) saturate(1.2);
  -webkit-backdrop-filter: blur(20px) saturate(1.2);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  padding: 50px 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(100, 180, 255, 0.08) inset;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s cubic-bezier(0.19, 1, 0.22, 1) both;
}
.about-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(100, 180, 255, 0.35) 50%, transparent 100%);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.avatar-wrapper {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  display: inline-block;
  width: 100%;
}

.avatar-glow {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  width: 180px; height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(180, 160, 255, 0.25) 0%, rgba(160, 140, 220, 0.1) 40%, transparent 70%);
  pointer-events: none;
  animation: glowPulse 3.5s ease-in-out infinite;
}
@keyframes glowPulse {
  0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.08); }
}

.avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 3px solid rgba(180, 160, 255, 0.4);
  object-fit: cover;
  position: relative;
  z-index: 1;
  box-shadow: 0 0 20px rgba(180, 160, 255, 0.3), 0 0 40px rgba(180, 160, 255, 0.15);
}

.name {
  text-align: center;
  font-size: 2rem;
  font-weight: 600;
  letter-spacing: 4px;
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
  color: #fff;
  margin: 0 0 12px;
  background: linear-gradient(135deg, #fff 0%, #d8c8ff 50%, #fff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.bio {
  text-align: center;
  font-size: 1rem;
  color: var(--ink-200, rgba(239,236,251,0.72));
  margin: 0 0 40px;
  line-height: 1.6;
  letter-spacing: 1px;
}

.section {
  margin-bottom: 36px;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 500;
  letter-spacing: 2px;
  color: rgba(200, 210, 255, 0.9);
  margin: 0 0 18px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title i {
  font-size: 1.15rem;
  opacity: 0.8;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tech-tag {
  background: linear-gradient(135deg, rgba(70, 150, 255, 0.25) 0%, rgba(100, 130, 220, 0.18) 100%);
  color: rgba(200, 220, 255, 0.9);
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 0.85rem;
  letter-spacing: 1px;
  border: 1px solid rgba(100, 180, 255, 0.15);
  backdrop-filter: blur(4px);
  transition: all 0.3s ease;
}
.tech-tag:hover {
  background: linear-gradient(135deg, rgba(80, 170, 255, 0.45) 0%, rgba(120, 150, 240, 0.35) 100%);
  border-color: rgba(100, 180, 255, 0.35);
  box-shadow: 0 2px 12px rgba(80, 160, 255, 0.2);
}

.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  text-decoration: none;
  color: var(--ink-200, rgba(239,236,251,0.72));
  transition: all 0.3s ease;
  backdrop-filter: blur(6px);
}
.link-item:hover {
  background: rgba(180, 160, 255, 0.12);
  border-color: rgba(180, 160, 255, 0.35);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(180, 160, 255, 0.2);
}

.link-icon {
  font-size: 1.3rem;
}

.link-name {
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 1px;
}

.contact {
  text-align: center;
}

.email {
  font-size: 1rem;
  color: var(--ink-200, rgba(239,236,251,0.72));
  margin: 10px 0;
}

.note {
  font-size: 0.85rem;
  color: var(--ink-300, rgba(239,236,251,0.5));
  margin: 0;
}

/* ========== 宇宙备案栏（与 Home 一致） ========== */
.cosmic-footer {
  position: fixed; bottom: 8px; left: 50%; transform: translateX(-50%); z-index: 25;
  padding: 6px 20px 8px; display: flex; align-items: center; justify-content: center; pointer-events: none;
}
.footer-glow {
  position: absolute; inset: -8px -20px;
  background: radial-gradient(ellipse at center, rgba(80, 140, 220, 0.12) 0%, rgba(40, 70, 120, 0.06) 35%, transparent 70%);
  border-radius: 16px; pointer-events: none; animation: footerGlow 4s ease-in-out infinite;
}
@keyframes footerGlow { 0%, 100% { opacity: 0.6; transform: scale(1); } 50% { opacity: 1; transform: scale(1.03); } }
.footer-content {
  position: relative; display: flex; align-items: center; gap: 12px; padding: 4px 16px;
  background: linear-gradient(135deg, rgba(15, 28, 52, 0.65) 0%, rgba(12, 20, 38, 0.75) 100%);
  backdrop-filter: blur(12px) saturate(1.1); -webkit-backdrop-filter: blur(12px) saturate(1.1);
  border: 1px solid rgba(100, 180, 255, 0.15); border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  pointer-events: auto; font-size: 0.75rem; letter-spacing: 0.5px; color: rgba(180, 200, 230, 0.85);
  transition: all 0.3s ease;
}
.cosmic-footer:hover .footer-content { border-color: rgba(120, 190, 255, 0.35); box-shadow: 0 6px 28px rgba(40, 100, 180, 0.25), inset 0 1px 0 rgba(255, 255, 255, 0.12); }
.footer-link { display: inline-flex; align-items: center; gap: 4px; color: rgba(200, 220, 255, 0.9); text-decoration: none; transition: all 0.25s ease; padding: 2px 6px; border-radius: 6px; }
.footer-link i { font-size: 0.85rem; opacity: 0.9; transition: transform 0.3s ease; }
.footer-link:hover { color: #ffffff; background: rgba(100, 160, 240, 0.15); text-shadow: 0 0 12px rgba(120, 180, 255, 0.6); }
.footer-link:hover i { transform: scale(1.15); }
.footer-divider { width: 1px; height: 14px; background: linear-gradient(to bottom, transparent, rgba(100, 180, 255, 0.4), transparent); opacity: 0.6; }
.footer-copyright { display: inline-flex; align-items: center; gap: 4px; opacity: 0.75; }
.footer-copyright i { font-size: 0.8rem; }

@media (max-width: 768px) {
  .about-content {
    padding: 16px 14px 100px;
  }
  .about-card {
    padding: 32px 20px;
  }
  .name {
    font-size: 1.5rem;
  }
  .link-grid {
    grid-template-columns: 1fr;
  }
}
</style>