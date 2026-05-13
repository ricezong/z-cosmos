<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere ice-giant"></div></div>
        <div class="header-title">
          <h1>工具箱</h1>
          <p>冰巨星 · 常用工具与资源</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'tools' }" @click="activeTab = 'tools'"><i class="ri-tools-line"></i> 常用工具</div>
      <div class="tab" :class="{ active: activeTab === 'links' }" @click="activeTab = 'links'"><i class="ri-links-line"></i> 实用链接</div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <div class="panel" v-show="activeTab === 'tools'">
      <div class="stage">
        <div class="stage-title"><i class="ri-tools-line"></i> 常用工具</div>
        <div class="stage-desc">开发、设计、运维常用工具集合</div>
        <div class="placeholder-box">
          <div class="placeholder-icon"><i class="ri-tools-line"></i></div>
          <p>功能开发中，敬请期待...</p>
        </div>
      </div>
    </div>

    <div class="panel" v-show="activeTab === 'links'">
      <div class="stage">
        <div class="stage-title"><i class="ri-links-line"></i> 实用链接</div>
        <div class="stage-desc">常用网站与开发资源</div>
        <div class="placeholder-box">
          <div class="placeholder-icon"><i class="ri-links-line"></i></div>
          <p>功能开发中，敬请期待...</p>
        </div>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('tools')

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
.container { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.4); box-shadow: 0 0 15px rgba(144,166,196,0.3); }
.tabs { display: flex; gap: 10px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(144,166,196,0.15); border: 1px solid rgba(144,166,196,0.25); color: #a8bcd4; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(144,166,196,0.35); border-color: rgba(144,166,196,0.6); box-shadow: 0 0 15px rgba(144,166,196,0.2); color: #fff; }
.panel { display: block; }
.stage { border: 1px solid rgba(144,166,196,0.25); border-radius: 20px; padding: 25px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.stage-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 5px; color: #e8eef7; letter-spacing: 2px; }
.stage-desc { font-size: 0.9rem; opacity: 0.7; margin-bottom: 20px; }
.placeholder-box { text-align: center; padding: 60px 20px; opacity: 0.6; }
.placeholder-icon { font-size: 3rem; margin-bottom: 15px; }
.placeholder-box p { font-size: 1rem; color: #a8bcd4; }
</style>