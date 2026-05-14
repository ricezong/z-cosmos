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
          <!-- 头像区域 - 孔明灯 -->
          <div class="lantern-section">
            <div class="lantern-glow"></div>
            <div class="lantern">
              <div class="lantern-flame"></div>
              <div class="lantern-body">
                <span class="lantern-text">Z</span>
              </div>
              <div class="lantern-tail"></div>
            </div>
            <div class="lantern-sparks">
              <span class="spark"></span>
              <span class="spark"></span>
              <span class="spark"></span>
              <span class="spark"></span>
              <span class="spark"></span>
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
  { name: 'GitHub', url: 'https://github.com/yourusername', icon: 'ri-github-line', desc: '开源项目', bg: 'rgba(255, 255, 255, 0.08)' },
  { name: 'Gitee', url: 'https://gitee.com/yourusername', icon: 'ri-git-branch-line', desc: '国内代码托管', bg: 'rgba(200, 60, 50, 0.1)' },
  { name: '知乎', url: 'https://zhihu.com/people/yourusername', icon: 'ri-chat-quote-line', desc: '技术问答', bg: 'rgba(66, 133, 244, 0.1)' },
  { name: '掘金', url: 'https://juejin.cn/user/yourusername', icon: 'ri-quill-pen-line', desc: '技术博客', bg: 'rgba(66, 165, 245, 0.1)' },
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

/* ========== 孔明灯头像区 ========== */
.lantern-section {
  text-align: center;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 160px;
}

.lantern-glow {
  position: absolute;
  left: 50%; top: 45%;
  transform: translate(-50%, -50%);
  width: 140px; height: 140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 160, 60, 0.15) 0%, rgba(255, 120, 40, 0.08) 40%, transparent 70%);
  pointer-events: none;
  animation: lanternGlow 4s ease-in-out infinite;
}
@keyframes lanternGlow {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.15); }
}

.lantern {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: lanternFloat 5s ease-in-out infinite;
}
@keyframes lanternFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.lantern-flame {
  width: 8px;
  height: 12px;
  background: radial-gradient(ellipse at 50% 80%, #ffcc44, #ff8800 60%, transparent 100%);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: flameFlicker 0.6s ease-in-out infinite alternate;
  position: absolute;
  top: -6px;
  z-index: 2;
}
@keyframes flameFlicker {
  0% { transform: scale(1) rotate(-3deg); opacity: 0.9; }
  100% { transform: scale(1.15) rotate(3deg); opacity: 1; }
}

.lantern-body {
  width: 72px;
  height: 88px;
  background: radial-gradient(ellipse at 40% 30%, rgba(255, 180, 80, 0.9), rgba(220, 80, 30, 0.85) 60%, rgba(180, 50, 20, 0.8));
  border-radius: 50% 50% 45% 45% / 55% 55% 45% 45%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 0 20px rgba(255, 140, 40, 0.4),
    0 0 50px rgba(255, 100, 20, 0.2),
    inset 0 -8px 20px rgba(180, 50, 10, 0.3),
    inset 0 5px 15px rgba(255, 220, 150, 0.3);
  position: relative;
  overflow: hidden;
}
.lantern-body::before {
  content: '';
  position: absolute;
  top: 8%; left: 15%; right: 55%; bottom: 40%;
  background: radial-gradient(ellipse, rgba(255, 240, 200, 0.35), transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}
.lantern-body::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border: 1px solid rgba(255, 200, 100, 0.25);
  border-radius: inherit;
  pointer-events: none;
}

.lantern-text {
  font-family: 'Cinzel', serif;
  font-size: 2rem;
  font-weight: 600;
  color: rgba(255, 240, 200, 0.95);
  text-shadow: 0 0 10px rgba(255, 200, 100, 0.6), 0 0 20px rgba(255, 160, 60, 0.3);
  position: relative;
  z-index: 1;
}

.lantern-tail {
  width: 30px;
  height: 20px;
  background: linear-gradient(180deg, rgba(200, 70, 20, 0.7), rgba(160, 50, 10, 0.4));
  border-radius: 0 0 50% 50% / 0 0 100% 100%;
  margin-top: -2px;
}

.lantern-sparks {
  position: absolute;
  left: 50%; top: 25%;
  width: 100px; height: 100px;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.spark {
  position: absolute;
  width: 3px;
  height: 3px;
  background: rgba(255, 200, 80, 0.8);
  border-radius: 50%;
  animation: sparkRise 3s ease-out infinite;
  box-shadow: 0 0 4px rgba(255, 180, 60, 0.6);
}
.spark:nth-child(1) { left: 45%; animation-delay: 0s; }
.spark:nth-child(2) { left: 55%; animation-delay: 0.6s; }
.spark:nth-child(3) { left: 40%; animation-delay: 1.2s; }
.spark:nth-child(4) { left: 60%; animation-delay: 1.8s; }
.spark:nth-child(5) { left: 50%; animation-delay: 2.4s; }
@keyframes sparkRise {
  0% { top: 50%; opacity: 0; transform: scale(0.5); }
  20% { opacity: 1; }
  100% { top: 0%; opacity: 0; transform: scale(0); }
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