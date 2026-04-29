<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere mars"></div></div>
        <div class="header-title">
          <h1>剧场区</h1>
          <p>火星 · 表演艺术中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'stickman' }" @click="activeTab = 'stickman'"><i class="ri-body-scan-line"></i> 火柴人剧场</div>
      <div class="tab" :class="{ active: activeTab === 'pixel' }" @click="activeTab = 'pixel'"><i class="ri-game-line"></i> 像素剧场</div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <div class="panel" v-show="activeTab === 'stickman'">
      <div class="stage">
        <div class="stage-title"><i class="ri-movie-line"></i> 火柴人动画剧场</div>
        <div class="stage-desc">选择场景，观看火柴人的精彩表演</div>
        <div class="placeholder-box">
          <div class="placeholder-icon"><i class="ri-body-scan-line"></i></div>
          <p>功能开发中，敬请期待...</p>
        </div>
      </div>
    </div>

    <div class="panel" v-show="activeTab === 'pixel'">
      <div class="stage">
        <div class="stage-title"><i class="ri-game-line"></i> 像素动画剧场</div>
        <div class="stage-desc">复古像素风格的动画演示</div>
        <div class="placeholder-box">
          <div class="placeholder-icon"><i class="ri-game-line"></i></div>
          <p>功能开发中，敬请期待...</p>
        </div>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  </div><!-- page-layout -->

  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('stickman')

// Back to top
const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; background: #050b1a; color: #ffe0d0; min-height: 100vh; }
.container { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(217,122,92,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #d97a5c, #ffbba0); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(192,92,62,0.2); border: 1px solid rgba(217,122,92,0.4); color: #ffbba0; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(192,92,62,0.4); box-shadow: 0 0 15px rgba(217,122,92,0.3); }
.tabs { display: flex; gap: 10px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(192,92,62,0.15); border: 1px solid rgba(217,122,92,0.25); color: #ffbba0; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(192,92,62,0.35); border-color: rgba(217,122,92,0.6); box-shadow: 0 0 15px rgba(217,122,92,0.2); color: #fff; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.mars { background: radial-gradient(circle at 35% 35%, #d97a5c, #c05c3e 50%, #6b2a1a 100%); box-shadow: 0 0 12px rgba(192,92,62,0.5), inset 0 0 8px rgba(255,255,255,0.12); }
.planet-sphere.mars::after { content: ''; position: absolute; inset: 12% 28% 42% 22%; background: rgba(255,255,255,0.1); border-radius: 50%; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }
.panel { display: block; }
.stage { background: rgba(20,8,8,0.7); border: 1px solid rgba(217,122,92,0.25); border-radius: 20px; padding: 25px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.stage-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 5px; color: #ffbba0; letter-spacing: 2px; }
.stage-desc { font-size: 0.9rem; opacity: 0.7; margin-bottom: 20px; }
.placeholder-box { text-align: center; padding: 60px 20px; opacity: 0.6; }
.placeholder-icon { font-size: 3rem; margin-bottom: 15px; }
.placeholder-box p { font-size: 1rem; color: #ffbba0; }

@media (max-width: 768px) {
  .container { padding: 15px 12px 60px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-sphere { width: 28px; height: 28px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .tabs { gap: 6px; flex-wrap: wrap; }
  .tab { padding: 8px 14px; font-size: 0.85rem; }
  .stage { padding: 18px; }
  .stage-title { font-size: 1.1rem; }
  .placeholder-icon { font-size: 2.5rem; }
}
</style>
