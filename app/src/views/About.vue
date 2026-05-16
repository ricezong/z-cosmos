<template>
  <div class="star-bg"></div>

  <div class="page-layout">
    <!-- 固定顶部栏 -->
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-icon">
            <div class="planet-sphere profile-planet"></div>
          </div>
          <div class="header-title">
            <h1>关于站长</h1>
          </div>
        </div>
        <router-link to="/" class="back-btn">
          <i class="ri-arrow-left-line"></i> 返回星域
        </router-link>
      </header>
    </div>

    <!-- 可滚动内容区 -->
    <div class="page-scroll">
      <div class="container">
        <div class="about-card">
          <!-- 头像区域 - 旋转的黑洞 -->
          <div class="blackhole-section">
            <div class="bh-outer-halo"></div>

            <div class="accretion-disk-container">
              <div class="disk-ring"></div>
              <div class="disk-ring"></div>
              <div class="disk-ring"></div>
              <div class="disk-ring"></div>
            </div>

            <div class="disk-particles">
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
              <span class="disk-particle"></span>
            </div>

            <div class="event-horizon-glow"></div>

            <div class="blackhole-sphere">
              <span class="bh-text">Z</span>
            </div>

            <div class="accretion-particles">
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
              <span class="accretion-particle"></span>
            </div>
          </div>

          <!-- 基本信息 -->
          <h1 class="name">{{ name }}</h1>
          <p class="bio">{{ bio }}</p>

          <!-- 技术方向 -->
          <div class="section">
            <h3 class="section-title"><i class="ri-code-s-slash-line"></i> 技术方向</h3>
            <div class="tech-grid">
              <div v-for="tech in techStack" :key="tech.name" class="tech-card">
                <div class="tech-icon-wrap" :style="{ background: tech.bg }">
                  <i :class="tech.icon"></i>
                </div>
                <div class="tech-info">
                  <span class="tech-name">{{ tech.name }}</span>
                  <div class="tech-bar">
                    <div class="tech-bar-fill" :style="{ width: tech.level + '%', background: tech.color }"></div>
                  </div>
                </div>
              </div>
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
                <div class="link-icon-wrap" :style="{ background: link.bg }">
                  <i :class="link.icon"></i>
                </div>
                <div class="link-body">
                  <span class="link-name">{{ link.name }}</span>
                  <span class="link-desc">{{ link.desc }}</span>
                </div>
                <i class="ri-arrow-right-up-line link-arrow"></i>
              </a>
            </div>
          </div>

          <!-- 联系方式 -->
          <div class="section">
            <h3 class="section-title"><i class="ri-mail-send-line"></i> 联系方式</h3>
            <div class="contact-grid">
              <div class="contact-card">
                <div class="contact-icon-wrap">
                  <i class="ri-at-line"></i>
                </div>
                <div class="contact-body">
                  <span class="contact-label">邮箱</span>
                  <span class="contact-value">{{ email }}</span>
                </div>
              </div>
              <div class="contact-card">
                <div class="contact-icon-wrap">
                  <i class="ri-chat-3-line"></i>
                </div>
                <div class="contact-body">
                  <span class="contact-label">交流</span>
                  <span class="contact-value">欢迎技术交流与合作</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const name = ref('孔明灯')
const bio = ref('全栈开发者 | 技术爱好者 | 终身学习者')

const techStack = ref([
  { name: 'Java', icon: 'ri-code-s-slash-line', bg: 'rgba(255, 120, 50, 0.12)', color: 'rgba(255, 140, 70, 0.8)', level: 90 },
  { name: 'Spring Boot', icon: 'ri-leaf-line', bg: 'rgba(100, 200, 120, 0.12)', color: 'rgba(120, 220, 140, 0.8)', level: 85 },
  { name: 'Vue.js', icon: 'ri-vuejs-line', bg: 'rgba(66, 184, 131, 0.12)', color: 'rgba(66, 184, 131, 0.8)', level: 88 },
  { name: 'React', icon: 'ri-reactjs-line', bg: 'rgba(97, 218, 251, 0.12)', color: 'rgba(97, 218, 251, 0.8)', level: 75 },
  { name: 'MySQL', icon: 'ri-database-2-line', bg: 'rgba(0, 136, 204, 0.12)', color: 'rgba(0, 150, 220, 0.8)', level: 82 },
  { name: 'Redis', icon: 'ri-server-line', bg: 'rgba(220, 60, 50, 0.12)', color: 'rgba(220, 80, 70, 0.8)', level: 78 },
  { name: 'Docker', icon: 'ri-box-3-line', bg: 'rgba(36, 150, 237, 0.12)', color: 'rgba(36, 150, 237, 0.8)', level: 72 },
  { name: 'Linux', icon: 'ri-terminal-box-line', bg: 'rgba(200, 180, 100, 0.12)', color: 'rgba(200, 180, 100, 0.8)', level: 80 },
])

const links = ref([
  { name: 'GitHub', url: 'https://github.com/ricezong', icon: 'ri-github-line', desc: '开源项目', bg: 'rgba(255, 255, 255, 0.08)' },
  { name: 'Gitee', url: 'https://gitee.com/kongming-latern', icon: 'ri-git-branch-line', desc: '国内代码托管', bg: 'rgba(200, 60, 50, 0.1)' }
])

const email = ref('gz.kong@qq.com')
</script>

<style scoped>
/* ========== 顶部栏 ========== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(144, 166, 196, 0.3);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.header-title h1 {
  font-size: 1.6rem;
  font-weight: 300;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #ffffff, #c5d5ea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}
.header-title p {
  font-size: 0.85rem;
  color: rgba(168, 188, 212, 0.7);
  margin: 2px 0 0;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border-radius: 30px;
  background: rgba(144, 166, 196, 0.2);
  border: 1px solid rgba(144, 166, 196, 0.4);
  color: #c5d5ea;
  cursor: pointer;
  text-decoration: none;
  transition: 0.3s;
  font-size: 0.9rem;
}
.back-btn:hover {
  background: rgba(144, 166, 196, 0.35);
  box-shadow: 0 0 15px rgba(144, 166, 196, 0.15);
}

/* ========== 主内容 ========== */
.container {
  position: relative;
  z-index: 1;
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}

.about-card {
  position: relative;
  overflow: hidden;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 🕳️ 旋转的黑洞头像区 ========== */
.blackhole-section {
  text-align: center;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 260px;
  overflow: visible;
  margin-bottom: 10px;
}

.bh-outer-halo {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  width: 220px; height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(140, 100, 220, 0.15) 0%, rgba(100, 70, 180, 0.08) 35%, rgba(60, 30, 120, 0.03) 65%, transparent 100%);
  pointer-events: none;
  animation: outerHaloPulse 3.5s ease-in-out infinite;
  z-index: 0;
}
@keyframes outerHaloPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.15); opacity: 0.9; }
}

.accretion-disk-container {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  width: 180px; height: 180px;
  pointer-events: none;
  z-index: 2;
  perspective: 300px;
  transform-style: preserve-3d;
}

.disk-ring {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%) rotateX(65deg) rotateZ(var(--ring-angle, 0deg));
  border-radius: 50%;
  border: 3px solid transparent;
  animation: diskSpin var(--spin-dur, 3.5s) linear infinite;
  pointer-events: none;
  box-shadow: 0 0 12px rgba(180, 130, 255, 0.25);
}
.disk-ring:nth-child(1) {
  width: 155px; height: 155px;
  border-top-color: rgba(180, 130, 255, 0.55);
  border-right-color: rgba(140, 100, 220, 0.4);
  border-bottom-color: rgba(100, 60, 180, 0.2);
  --spin-dur: 4s; --ring-angle: 15deg;
}
.disk-ring:nth-child(2) {
  width: 145px; height: 145px;
  border-top-color: rgba(200, 150, 255, 0.45);
  border-left-color: rgba(160, 110, 240, 0.3);
  border-bottom-color: rgba(120, 80, 200, 0.2);
  --spin-dur: 5.2s; --ring-angle: -25deg;
  animation-direction: reverse;
}
.disk-ring:nth-child(3) {
  width: 135px; height: 135px;
  border-right-color: rgba(190, 140, 255, 0.5);
  border-bottom-color: rgba(130, 90, 220, 0.35);
  border-left-color: rgba(150, 100, 230, 0.25);
  --spin-dur: 3.8s; --ring-angle: 40deg;
}
.disk-ring:nth-child(4) {
  width: 125px; height: 125px;
  border-top-color: rgba(170, 120, 255, 0.4);
  border-right-color: rgba(140, 100, 230, 0.3);
  --spin-dur: 4.6s; --ring-angle: -10deg;
  animation-direction: reverse;
}
@keyframes diskSpin {
  from { transform: translate(-50%, -50%) rotateX(65deg) rotateZ(var(--ring-angle, 0deg)) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotateX(65deg) rotateZ(var(--ring-angle, 0deg)) rotate(360deg); }
}

.disk-particles {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%) rotateX(65deg) rotateZ(30deg);
  width: 170px; height: 170px;
  pointer-events: none;
  z-index: 3;
  animation: diskSpin 2.8s linear infinite reverse;
  border-radius: 50%;
}
.disk-particle {
  position: absolute;
  width: 4px; height: 4px;
  background: rgba(220, 180, 255, 0.8);
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(200, 150, 255, 0.8), 0 0 14px rgba(160, 100, 255, 0.5);
}
.disk-particle:nth-child(1) { top: 2%; left: 50%; }
.disk-particle:nth-child(2) { top: 12%; left: 75%; }
.disk-particle:nth-child(3) { top: 45%; left: 90%; }
.disk-particle:nth-child(4) { top: 80%; left: 70%; }
.disk-particle:nth-child(5) { top: 92%; left: 40%; }
.disk-particle:nth-child(6) { top: 70%; left: 15%; }
.disk-particle:nth-child(7) { top: 30%; left: 8%; }
.disk-particle:nth-child(8) { top: 5%; left: 25%; }

.event-horizon-glow {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  width: 90px; height: 90px;
  border-radius: 50%;
  background: transparent;
  border: 2.5px solid rgba(200, 160, 255, 0.6);
  box-shadow: 0 0 10px rgba(180, 120, 255, 0.7), 0 0 25px rgba(140, 90, 230, 0.45), inset 0 0 10px rgba(200, 160, 255, 0.2);
  pointer-events: none;
  z-index: 5;
  animation: horizonGlow 2s ease-in-out infinite alternate;
}
@keyframes horizonGlow {
  0% { box-shadow: 0 0 8px rgba(180, 120, 255, 0.6), 0 0 20px rgba(140, 90, 230, 0.35), inset 0 0 8px rgba(200, 160, 255, 0.2); }
  100% { box-shadow: 0 0 16px rgba(200, 150, 255, 0.9), 0 0 35px rgba(160, 110, 250, 0.6), inset 0 0 14px rgba(220, 180, 255, 0.35); }
}

.blackhole-sphere {
  position: relative;
  width: 78px; height: 78px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 30%, #1a1030, #08030f 50%, #000000 100%);
  z-index: 4;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.9), inset 0 0 20px rgba(0, 0, 0, 0.8), inset 0 -5px 10px rgba(40, 20, 60, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  animation: blackholePulse 3s ease-in-out infinite;
}
@keyframes blackholePulse {
  0%, 100% { box-shadow: 0 0 12px rgba(0, 0, 0, 0.9), inset 0 0 18px rgba(0, 0, 0, 0.8), inset 0 -4px 8px rgba(40, 20, 60, 0.3); }
  50% { box-shadow: 0 0 22px rgba(20, 10, 40, 0.9), inset 0 0 25px rgba(0, 0, 0, 0.9), inset 0 -6px 12px rgba(60, 30, 80, 0.35); }
}

.blackhole-sphere::before {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent, rgba(180, 140, 255, 0.15) 20%, rgba(140, 100, 240, 0.1) 40%, transparent 60%, rgba(160, 120, 255, 0.12) 80%, transparent 100%);
  animation: lensSpin 5s linear infinite;
  pointer-events: none;
  z-index: -1;
  filter: blur(4px);
}
@keyframes lensSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.blackhole-sphere::after {
  content: '';
  position: absolute;
  top: 15%; left: 20%;
  width: 18%; height: 14%;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(200, 180, 255, 0.4), transparent 70%);
  pointer-events: none;
  z-index: 2;
}

.bh-text {
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
  font-size: 2.2rem;
  font-weight: 700;
  color: rgba(210, 190, 255, 0.85);
  text-shadow: 0 0 10px rgba(180, 130, 255, 0.8), 0 0 25px rgba(140, 100, 240, 0.5), 0 0 45px rgba(100, 70, 200, 0.3);
  position: relative;
  z-index: 3;
  animation: textFlicker 2.5s ease-in-out infinite alternate;
}
@keyframes textFlicker {
  0% { text-shadow: 0 0 8px rgba(180, 130, 255, 0.7), 0 0 20px rgba(140, 100, 240, 0.4), 0 0 35px rgba(100, 70, 200, 0.25); }
  100% { text-shadow: 0 0 14px rgba(200, 160, 255, 0.95), 0 0 32px rgba(160, 120, 255, 0.65), 0 0 55px rgba(120, 90, 230, 0.45); }
}

.accretion-particles {
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%, -50%);
  width: 150px; height: 150px;
  pointer-events: none;
  z-index: 6;
}
.accretion-particle {
  position: absolute;
  width: 2px; height: 2px;
  background: rgba(220, 190, 255, 0.9);
  border-radius: 50%;
  box-shadow: 0 0 5px rgba(200, 150, 255, 0.8);
  animation: spiralIn var(--duration, 2.5s) ease-in infinite;
  animation-delay: var(--delay, 0s);
  left: 50%; top: 50%;
  transform-origin: center center;
}
.accretion-particle:nth-child(1) { --duration: 2.2s; --delay: 0s; }
.accretion-particle:nth-child(2) { --duration: 2.8s; --delay: 0.4s; }
.accretion-particle:nth-child(3) { --duration: 2.5s; --delay: 0.8s; }
.accretion-particle:nth-child(4) { --duration: 3s; --delay: 1.2s; }
.accretion-particle:nth-child(5) { --duration: 2.6s; --delay: 1.6s; }
.accretion-particle:nth-child(6) { --duration: 2.3s; --delay: 0.2s; }
.accretion-particle:nth-child(7) { --duration: 2.9s; --delay: 0.6s; }
.accretion-particle:nth-child(8) { --duration: 2.4s; --delay: 1s; }
.accretion-particle:nth-child(9) { --duration: 2.7s; --delay: 1.4s; }
.accretion-particle:nth-child(10) { --duration: 2.1s; --delay: 1.8s; }
@keyframes spiralIn {
  0% { transform: translate(-50%, -50%) rotate(0deg) translateX(60px) scale(1); opacity: 0.2; }
  30% { opacity: 1; transform: translate(-50%, -50%) rotate(120deg) translateX(45px) scale(1.2); }
  70% { opacity: 0.8; transform: translate(-50%, -50%) rotate(240deg) translateX(20px) scale(0.7); }
  100% { transform: translate(-50%, -50%) rotate(360deg) translateX(0px) scale(0); opacity: 0; }
}

/* ========== 名字与简介 ========== */
.name {
  text-align: center;
  font-size: 1.8rem;
  font-weight: 600;
  letter-spacing: 4px;
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
  background: linear-gradient(135deg, #fff 0%, #d4c0ff 50%, #fff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 10px;
}
.bio {
  text-align: center;
  font-size: 0.95rem;
  color: rgba(168, 188, 212, 0.8);
  margin: 0 0 36px;
  line-height: 1.6;
}

/* ========== 通用区块 ========== */
.section {
  margin-bottom: 32px;
}
.section-title {
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 2px;
  color: rgba(200, 215, 250, 0.9);
  margin: 0 0 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title i {
  font-size: 1.05rem;
  opacity: 0.8;
}

/* ========== 技术方向 ========== */
.tech-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
@media (min-width: 640px) {
  .tech-grid { grid-template-columns: repeat(4, 1fr); }
}
.tech-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid rgba(144, 166, 196, 0.12);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
  transition: all 0.3s ease;
}
.tech-card:hover {
  border-color: rgba(144, 166, 196, 0.3);
  background: rgba(144, 166, 196, 0.05);
  transform: translateY(-1px);
}
.tech-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.1rem;
  color: #e8eef7;
}
.tech-info {
  flex: 1;
  min-width: 0;
}
.tech-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: #c5d5ea;
  display: block;
  margin-bottom: 5px;
}
.tech-bar {
  width: 100%;
  height: 3px;
  background: rgba(144, 166, 196, 0.1);
  border-radius: 2px;
  overflow: hidden;
}
.tech-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.8s cubic-bezier(0.19, 1, 0.22, 1);
}

/* ========== 友情链接 ========== */
.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}
.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(144, 166, 196, 0.12);
  border-radius: 14px;
  text-decoration: none;
  color: rgba(168, 188, 212, 0.85);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}
.link-item:hover {
  background: rgba(144, 166, 196, 0.08);
  border-color: rgba(144, 166, 196, 0.3);
  color: #e8eef7;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(144, 166, 196, 0.08);
}
.link-icon-wrap {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.15rem;
  color: #e8eef7;
}
.link-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.link-name {
  font-size: 0.88rem;
  font-weight: 500;
  color: #c5d5ea;
}
.link-desc {
  font-size: 0.72rem;
  color: rgba(144, 166, 196, 0.5);
}
.link-arrow {
  font-size: 0.85rem;
  opacity: 0;
  transition: all 0.3s;
  color: rgba(168, 188, 212, 0.6);
  flex-shrink: 0;
}
.link-item:hover .link-arrow {
  opacity: 1;
  transform: translate(2px, -2px);
}

/* ========== 联系方式 ========== */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}
.contact-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(144, 166, 196, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.02);
  transition: all 0.3s ease;
}
.contact-card:hover {
  border-color: rgba(144, 166, 196, 0.25);
  background: rgba(144, 166, 196, 0.05);
}
.contact-icon-wrap {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(144, 166, 196, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.1rem;
  color: #a8bcd4;
}
.contact-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.contact-label {
  font-size: 0.72rem;
  color: rgba(144, 166, 196, 0.5);
}
.contact-value {
  font-size: 0.88rem;
  font-weight: 500;
  color: #c5d5ea;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>