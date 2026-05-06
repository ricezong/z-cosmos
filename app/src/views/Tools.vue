<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon ice-giant-wrap"><div class="planet-ring"></div><div class="planet-sphere ice-giant"></div></div>
        <div class="header-title">
          <h1>自助区</h1>
          <p>冰巨星 · 工具集合中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'mcp' }" @click="activeTab = 'mcp'"><i class="ri-server-line"></i> MCP 服务</div>
      <div class="tab" :class="{ active: activeTab === 'skill' }" @click="activeTab = 'skill'"><i class="ri-magic-line"></i> SKILL 技能</div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">

    <div class="panel" v-show="activeTab === 'mcp'">
      <div class="stage">
        <div class="stage-title"><i class="ri-server-line"></i> MCP 服务</div>
        <div class="stage-desc">模型上下文协议 · 为 AI 提供标准化工具与数据连接</div>
        <div class="placeholder-box">
          <div class="placeholder-icon"><i class="ri-server-line"></i></div>
          <p>功能搭建中，敬请期待...</p>
        </div>
      </div>
    </div>

    <div class="panel" v-show="activeTab === 'skill'">
      <div class="stage">
        <div class="stage-title"><i class="ri-magic-line"></i> SKILL 技能</div>
        <div class="stage-desc">自定义技能集 · 探索与组合实用能力</div>
        <div class="placeholder-box">
          <div class="placeholder-icon"><i class="ri-magic-line"></i></div>
          <p>功能搭建中，敬请期待...</p>
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
import {ref} from 'vue'

const activeTab = ref('mcp')

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
/* Theme vars for back-to-top */
.page-layout {
  --bt-bg: rgba(133,168,168,0.3);
  --bt-border: rgba(133,168,168,0.5);
  --bt-color: #d4e8ec;
  --bt-hover-bg: rgba(133,168,168,0.5);
  --bt-shadow: rgba(133,168,168,0.3);
}
.container { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; justify-content: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.35); box-shadow: 0 0 15px rgba(144,166,196,0.3); }
/* Planet sphere with ring */
.ice-giant-wrap { position: relative; display: flex; align-items: center; justify-content: center; }
.planet-ring { position: absolute; width: 50px; height: 14px; border-radius: 50%; border: 2px solid rgba(170,200,210,0.5); transform: rotate(-15deg); top: 50%; left: 50%; margin-left: -25px; margin-top: -7px; z-index: 0; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; z-index: 1; }
.planet-sphere.ice-giant { background: radial-gradient(circle at 35% 35%, #a8d0d8, #8cc0cc 50%, #345260 100%); box-shadow: 0 0 12px rgba(133,168,168,0.5), inset 0 0 8px rgba(255,255,255,0.15); }
.planet-sphere.ice-giant::after { content: ''; position: absolute; inset: 10% 25% 40% 20%; background: rgba(255,255,255,0.12); border-radius: 50%; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }
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

@media (max-width: 768px) {
  .container { padding: 0px 20px; }
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
