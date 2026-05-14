<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- ==================== 固定顶部区域（保留原样式，含 padding 的 container） ==================== -->
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-icon"><div class="planet-sphere jupiter"></div></div>
          <div class="header-title">
            <h1>热点区</h1>
            <p>木星 · 每日热点汇聚</p>
          </div>
        </div>
        <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
      </header>
      <div class="container">
        <div class="source-tabs">
          <div class="source-tab" :class="{ active: currentSort === 'latest' }" @click="currentSort = 'latest'; loadNews()">
            <i class="ri-time-line"></i> 最新
          </div>
          <div class="source-tab" :class="{ active: currentSort === 'hot' }" @click="currentSort = 'hot'; loadNews()">
            <i class="ri-fire-line"></i> 最热
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 滚动区域（三栏布局，左右栏脱离 container） ==================== -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <LoadingSpinner v-if="loading" text="加载中..." />
      <div class="scroll-inner" v-else-if="newsList.length > 0">
        <!-- 左侧占位栏（直接贴左，无 container） -->
        <aside class="sidebar-placeholder">
          <div class="placeholder-card">
            <i class="ri-planet-line placeholder-icon"></i>
            <span class="placeholder-text">热点分类</span>
          </div>
        </aside>

        <!-- 中间新闻列表（使用独立的 list-wrapper，无 padding） -->
        <div class="list-wrapper">
          <div class="news-list-panel">
            <div class="list-header">
              <h3 class="list-header-title">新闻列表</h3>
            </div>
            <div class="minimal-list">
              <div
                  v-for="(item, idx) in newsList"
                  :key="item.topicId"
                  class="list-item"
                  :class="{ active: (hoveredNews || selectedNews)?.topicId === item.topicId }"
                  @mouseenter="hoveredNews = item"
                  @mouseleave="hoveredNews = null"
                  @click="selectNews(item)"
              >
                <div class="item-index" :class="{ top3: idx < 3 }">
                  {{ String(idx + 1).padStart(2, '0') }}
                </div>
                <div class="item-body">
                  <h4 class="item-title">{{ item.title }}</h4>
                  <p class="item-desc">{{ item.summary || '暂无摘要' }}</p>
                  <div class="item-meta">
                    <span v-if="item.category" class="item-tag">{{ item.category }}</span>
                    <span class="meta-stat"><i class="ri-eye-line"></i> {{ item.viewCount || 0 }}</span>
                    <span class="meta-stat"><i class="ri-time-line"></i> {{ formatTime(item.publishedAt) }}</span>
                  </div>
                </div>
                <div class="item-arrow">
                  <i class="ri-arrow-right-s-line"></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧详情面板（直接贴右，无 container） -->
        <aside class="preview-panel">
          <div class="detail-card" v-if="activeNews">
            <div class="detail-img-placeholder">
              <i class="ri-news-line detail-placeholder-icon"></i>
            </div>
            <div class="detail-content">
              <div class="detail-tags">
                <span v-if="activeNews.category" class="detail-tag">{{ activeNews.category }}</span>
              </div>
              <h3 class="detail-title">{{ activeNews.title }}</h3>
              <div class="detail-summary">{{ activeNews.summary || '暂无摘要' }}</div>
              <div class="detail-body" v-if="activeNews.content" v-html="activeNews.content"></div>
              <div class="detail-meta">
                <span><i class="ri-eye-line"></i> {{ activeNews.viewCount || 0 }}</span>
                <span>{{ formatTime(activeNews.publishedAt) }}</span>
              </div>
              <button class="detail-origin-btn" @click="goToOriginal(activeNews)">
                阅读原文 <i class="ri-arrow-right-line"></i>
              </button>
            </div>
          </div>
          <div class="detail-empty" v-else>
            <i class="ri-mouse-line empty-icon"></i>
            <p>点击新闻查看详情</p>
          </div>
        </aside>
      </div>
      <div class="empty-state" v-else-if="!loading">
        <p>暂无热点数据</p>
      </div>
    </div>

    <!-- 回到顶部 -->
    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop">
      <i class="ri-arrow-up-line"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listHotTopics, getHotTopic } from '../api/hot'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const currentSort = ref('latest')
const newsList = ref([])
const loading = ref(true)
const hoveredNews = ref(null)
const selectedNews = ref(null)
const showBackTop = ref(false)
const contentRef = ref(null)

// 右侧面板显示逻辑：悬停优先，否则显示已选中新闻，默认第一条
const activeNews = computed(() => {
  return hoveredNews.value || selectedNews.value || (newsList.value.length > 0 ? newsList.value[0] : null)
})

function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadNews() {
  loading.value = true
  try {
    const data = await listHotTopics({ page: 1, size: 50 })
    newsList.value = data?.records || []
    if (newsList.value.length > 0 && !selectedNews.value) {
      selectedNews.value = newsList.value[0]
    }
  } catch (e) {
    console.error('加载热点失败:', e)
    newsList.value = []
  } finally {
    loading.value = false
  }
}

async function selectNews(item) {
  selectedNews.value = item
  try {
    const detail = await getHotTopic(item.topicId)
    const updated = { ...item, ...detail }
    const idx = newsList.value.findIndex(n => n.topicId === item.topicId)
    if (idx !== -1) {
      newsList.value[idx] = updated
    }
    selectedNews.value = updated
  } catch (e) {
    console.error('获取详情失败', e)
  }
}

function goToOriginal(item) {
  if (!item?.url) return
  window.open(item.url, '_blank')
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
/* ========== 全局背景 ========== */
.star-bg {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: radial-gradient(ellipse at bottom, #1b2735 0%, #090a0f 100%);
  z-index: -1;
}
.page-layout {
  position: relative; z-index: 1; min-height: 100vh;
  display: flex; flex-direction: column;
}

/* ========== 顶部固定区域（含 container 带 padding） ========== */
.page-fixed {
  flex-shrink: 0;
}
.header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(144,166,196,0.3);
  backdrop-filter: blur(10px);
}
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 {
  font-size: 1.8rem; font-weight: 300; letter-spacing: 4px;
  background: linear-gradient(135deg, #ffffff, #c5d5ea);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  margin: 0;
}
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; color: #a8bcd4; }
.back-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 20px; border-radius: 30px;
  background: rgba(144,166,196,0.15); border: 1px solid rgba(144,166,196,0.4);
  color: #c5d5ea; cursor: pointer; text-decoration: none;
  transition: 0.3s; font-size: 0.9rem;
}
.back-btn:hover {
  background: rgba(144,166,196,0.3); box-shadow: 0 0 15px rgba(144,166,196,0.3);
}

/* 固定区域使用的 container（带 padding） */
.container {
  position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px;
}

.source-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.source-tab {
  padding: 8px 16px; border-radius: 20px; cursor: pointer;
  background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.2);
  color: #a8bcd4; transition: 0.3s; font-size: 0.85rem;
  display: flex; align-items: center; gap: 6px;
}
.source-tab.active, .source-tab:hover {
  background: rgba(144,166,196,0.25); border-color: rgba(144,166,196,0.5); color: #fff;
}

/* ========== 滚动区域及三栏布局 ========== */
.page-scroll {
  flex: 1; overflow-y: auto; scroll-behavior: smooth;
}
.scroll-inner {
  display: flex; justify-content: center;
  align-items: flex-start;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 24px;
}

/* ========== 左侧占位栏 ========== */
.sidebar-placeholder {
  width: 220px; flex-shrink: 0;
  position: sticky; top: 20px;
}
.placeholder-card {
  border: 1px solid rgba(144,166,196,0.2); border-radius: 16px;
  padding: 30px 15px; text-align: center;
  background: rgba(144,166,196,0.03); backdrop-filter: blur(5px);
}
.placeholder-icon { font-size: 2rem; color: rgba(144,166,196,0.3); }
.placeholder-text { display: block; margin-top: 10px; font-size: 0.8rem; opacity: 0.4; color: #a8bcd4; }

/* ========== 中间新闻列表容器（无 padding，独立类名） ========== */
.list-wrapper {
  flex: 1; min-width: 0;
  max-width: 960px;          /* 限制列表最大宽度，与内容区对齐 */
  margin: 0 auto;            /* 在 flex 容器内自动居中 */
}
.news-list-panel {
  border: 1px solid rgba(144,166,196,0.2); border-radius: 16px;
  background: rgba(15,20,35,0.6); overflow: hidden;
  backdrop-filter: blur(10px);
}
.list-header {
  padding: 12px 20px; border-bottom: 1px solid rgba(144,166,196,0.15);
}
.list-header-title {
  font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 2px;
  opacity: 0.5; color: #a8bcd4; margin: 0;
}
.minimal-list { display: flex; flex-direction: column; }
.list-item {
  display: flex; align-items: flex-start; padding: 16px 20px;
  border-bottom: 1px solid rgba(144,166,196,0.08);
  cursor: pointer; transition: all 0.2s ease;
}
.list-item:hover { background: rgba(144,166,196,0.05); }
.list-item.active { background: rgba(144,166,196,0.08); }
.item-index {
  width: 32px; font-size: 0.75rem; font-family: monospace;
  color: rgba(144,166,196,0.35); flex-shrink: 0; margin-top: 2px; transition: color 0.2s;
}
.list-item.active .item-index,
.list-item:hover .item-index { color: #d8a987; }
.item-index.top3 { color: #d8a987; }
.item-body { flex: 1; margin-left: 12px; overflow: hidden; }
.item-title {
  font-size: 0.95rem; margin: 0 0 4px; color: #e8eef7; transition: color 0.2s;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.list-item.active .item-title,
.list-item:hover .item-title { color: #d8a987; }
.item-desc {
  font-size: 0.8rem; opacity: 0.55; margin: 0 0 8px; color: #a8bcd4;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.item-meta { display: flex; gap: 12px; font-size: 0.7rem; opacity: 0.4; align-items: center; }
.item-tag {
  display: inline-block; padding: 2px 8px; border-radius: 10px;
  background: rgba(144,166,196,0.12); color: #a8bcd4;
  border: 1px solid rgba(144,166,196,0.15); font-size: 0.7rem;
}
.meta-stat { display: flex; align-items: center; gap: 3px; }
.item-arrow {
  width: 20px; display: flex; align-items: center; justify-content: center;
  opacity: 0; transform: translateX(-5px); transition: all 0.2s ease;
  color: #d8a987; font-size: 1.2rem;
}
.list-item:hover .item-arrow,
.list-item.active .item-arrow { opacity: 1; transform: translateX(0); }

/* ========== 右侧详情面板 ========== */
.preview-panel {
  width: 220px; flex-shrink: 0;
  position: sticky; top: 20px;
}
.detail-card {
  border: 1px solid rgba(144,166,196,0.2); border-radius: 16px;
  overflow: hidden; background: rgba(15,20,35,0.85); backdrop-filter: blur(10px);
  max-height: calc(100vh - 150px);
  display: flex; flex-direction: column;
}
.detail-img-placeholder {
  height: 120px;
  background: linear-gradient(135deg, rgba(144,166,196,0.15), rgba(20,25,45,0.8));
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.detail-placeholder-icon { font-size: 2rem; color: rgba(144,166,196,0.3); }
.detail-content {
  padding: 16px; overflow-y: auto; flex: 1;
}
.detail-tags { margin-bottom: 6px; }
.detail-tag {
  display: inline-block; padding: 2px 8px; border-radius: 10px;
  font-size: 0.65rem; background: rgba(144,166,196,0.12); color: #a8bcd4;
  border: 1px solid rgba(144,166,196,0.15);
}
.detail-title {
  font-size: 0.9rem; font-weight: 500; color: #e8eef7; margin: 0 0 8px; line-height: 1.4;
}
.detail-summary {
  font-size: 0.78rem; opacity: 0.7; margin-bottom: 10px; line-height: 1.5; color: #a8bcd4;
}
.detail-body {
  font-size: 0.75rem; opacity: 0.6; line-height: 1.6; margin-bottom: 12px;
  max-height: 200px; overflow-y: auto; padding-right: 4px;
}
.detail-body :deep(p) { margin-bottom: 6px; }
.detail-meta {
  display: flex; justify-content: space-between; font-size: 0.7rem; opacity: 0.4;
  margin-bottom: 12px;
}
.detail-origin-btn {
  width: 100%; padding: 7px 0; border-radius: 20px;
  background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.3);
  color: #c5d5ea; cursor: pointer; font-size: 0.78rem;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  transition: all 0.2s;
}
.detail-origin-btn:hover {
  background: rgba(144,166,196,0.25); border-color: rgba(144,166,196,0.5);
}
.detail-empty {
  border: 1px solid rgba(144,166,196,0.15); border-radius: 16px;
  padding: 40px 20px; text-align: center; background: rgba(15,20,35,0.4);
}
.empty-icon { font-size: 2rem; opacity: 0.2; color: #a8bcd4; }
.detail-empty p { margin-top: 12px; font-size: 0.8rem; opacity: 0.4; color: #a8bcd4; }

/* ========== 空状态 ========== */
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; color: #a8bcd4; }

/* ========== 响应式：移动端隐藏左右栏 ========== */
@media (max-width: 768px) {
  .scroll-inner { flex-direction: column; padding: 0; gap: 0; }
  .sidebar-placeholder,
  .preview-panel { display: none; }
  .list-wrapper { max-width: none; margin: 0; }
  .news-list-panel { border: none; border-radius: 0; background: transparent; }
  .list-item { padding: 14px 0; border-bottom: 1px solid rgba(144,166,196,0.1); }
  .item-arrow { display: none; }
}

/* ========== 回到顶部按钮 ========== */
.back-to-top {
  position: fixed; bottom: 30px; right: 30px;
  width: 40px; height: 40px; border-radius: 50%;
  background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.3);
  color: #a8bcd4; cursor: pointer; display: flex;
  align-items: center; justify-content: center; transition: 0.3s; z-index: 50;
}
.back-to-top:hover { background: rgba(144,166,196,0.4); }
</style>