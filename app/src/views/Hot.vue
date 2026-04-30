<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-icon"><div class="planet-sphere jupiter"></div></div>
          <div class="header-title">
            <h1>热点区</h1>
            <p>木星 · 每日热点聚合</p>
          </div>
        </div>
        <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
      </header>
      <div class="container">
        <div class="source-tabs">
          <button class="source-tab" :class="{ active: currentSource === 'all' }" @click="setSource('all')">全部</button>
          <button class="source-tab" v-for="source in sources" :key="source.id" :class="{ active: currentSource === source.id }" @click="setSource(source.id)">{{ source.name }}</button>
        </div>
      </div>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <div class="container">
        <div class="hot-list" v-if="items.length">
          <article class="hot-item" v-for="(item, idx) in items" :key="item.newsId || idx" @click="openModal(item)">
            <div class="hot-rank" :class="{ top3: idx < 3 }">{{ idx + 1 }}</div>
            <div class="hot-content">
              <h2 class="hot-title">{{ item.title }}</h2>
              <p class="hot-desc">{{ item.desc }}</p>
              <div class="hot-meta">
                <span class="hot-tag">{{ item.category || 'news' }}</span>
                <span>{{ item.srcName }}</span>
                <span><i class="ri-eye-line"></i> {{ item.views }}</span>
                <span><i class="ri-fire-line"></i> {{ item.hotScore || 0 }}</span>
              </div>
            </div>
          </article>
        </div>
        <div class="empty-state" v-else>{{ loading ? '正在加载热点...' : '暂无热点数据' }}</div>
      </div>
    </div>
    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>

  <div class="modal-overlay" :class="{ active: modalItem }" @click.self="modalItem = null">
    <div class="modal-box" v-if="modalItem">
      <button class="modal-close" @click="modalItem = null">×</button>
      <h2 class="modal-title">{{ modalItem.title }}</h2>
      <div class="modal-source">{{ modalItem.srcName }}</div>
      <div class="modal-body" v-html="modalItem.content || modalItem.desc"></div>
      <div class="modal-stats">
        <span><i class="ri-eye-line"></i> {{ modalItem.views }}</span>
        <span><i class="ri-fire-line"></i> {{ modalItem.hotScore || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getNews, listNews } from '../api/hot'

const sources = [
  { id: 'general', name: '综合' },
  { id: 'tech', name: '科技' },
  { id: 'weibo', name: '微博' },
  { id: 'zhihu', name: '知乎' },
  { id: 'bilibili', name: 'B站' },
  { id: 'github', name: 'GitHub' },
]
const currentSource = ref('all')
const items = ref([])
const loading = ref(false)
const modalItem = ref(null)

const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

function mapNews(item) {
  return {
    newsId: item.newsId,
    title: item.title || '',
    desc: item.summary || '',
    content: item.content,
    category: item.category || 'general',
    srcName: item.source || item.category || 'News',
    views: item.viewCount || 0,
    hotScore: Math.round(item.hotScore || 0)
  }
}

async function loadNews() {
  loading.value = true
  try {
    const data = await listNews({
      category: currentSource.value === 'all' ? undefined : currentSource.value,
      sort: 'hot',
      page: 1,
      size: 30
    })
    items.value = (data.records || []).map(mapNews)
  } catch (e) {
    console.warn('Load news failed', e)
    items.value = []
  } finally {
    loading.value = false
  }
}

function setSource(source) {
  currentSource.value = source
  loadNews()
}

async function openModal(item) {
  modalItem.value = item
  if (!item.newsId) return
  try {
    const detail = await getNews(item.newsId)
    modalItem.value = { ...item, ...mapNews(detail) }
  } catch (e) {
    console.warn('Load news detail failed', e)
  }
}

onMounted(loadNews)
</script>

<style scoped>
.page-layout { --bt-bg: rgba(196,142,110,0.3); --bt-border: rgba(196,142,110,0.5); --bt-color: #f0d8b0; --bt-hover-bg: rgba(196,142,110,0.5); --bt-shadow: rgba(196,142,110,0.3); }
.container { position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn, .source-tab { border-radius: 30px; border: 1px solid rgba(144,166,196,0.35); color: #c5d5ea; background: rgba(144,166,196,0.12); text-decoration: none; cursor: pointer; transition: 0.25s; }
.back-btn { padding: 8px 20px; }
.source-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.source-tab { padding: 8px 16px; }
.source-tab.active, .source-tab:hover, .back-btn:hover { background: rgba(144,166,196,0.3); color: #fff; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0; background: radial-gradient(circle at 35% 35%, #e8c699, #d8a987 40%, #b59065 70%, #4a3620 100%); box-shadow: 0 0 12px rgba(196,142,110,0.5); }
.hot-list { display: flex; flex-direction: column; gap: 12px; }
.hot-item { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 18px 22px; cursor: pointer; transition: 0.3s; display: flex; gap: 15px; }
.hot-item:hover { border-color: rgba(144,166,196,0.5); transform: translateY(-2px); }
.hot-rank { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: rgba(144,166,196,0.2); color: #c5d5ea; flex-shrink: 0; }
.hot-rank.top3 { background: linear-gradient(135deg, #d8a987, #b59065); color: #1a1510; }
.hot-content { flex: 1; }
.hot-title { font-size: 1.05rem; margin-bottom: 6px; color: #e8eef7; }
.hot-desc { font-size: 0.88rem; opacity: 0.72; line-height: 1.5; margin-bottom: 8px; }
.hot-meta { display: flex; gap: 14px; font-size: 0.8rem; opacity: 0.65; align-items: center; flex-wrap: wrap; }
.hot-tag { padding: 3px 10px; border-radius: 12px; background: rgba(144,166,196,0.15); color: #a8bcd4; }
.empty-state { text-align: center; padding: 60px 20px; opacity: 0.55; }
.modal-overlay { display: none; position: fixed; inset: 0; background: rgba(10,8,6,0.85); z-index: 100; backdrop-filter: blur(5px); align-items: center; justify-content: center; padding: 20px; }
.modal-overlay.active { display: flex; }
.modal-box { border: 1px solid rgba(144,166,196,0.3); border-radius: 24px; max-width: 640px; width: 100%; max-height: 80vh; overflow-y: auto; padding: 30px; background: rgba(16,18,32,0.96); box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
.modal-close { float: right; width: 36px; height: 36px; border-radius: 50%; background: rgba(144,166,196,0.15); border: 1px solid rgba(144,166,196,0.3); color: #a8bcd4; cursor: pointer; font-size: 1.2rem; }
.modal-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 10px; color: #e8eef7; }
.modal-source { font-size: 0.85rem; opacity: 0.6; margin-bottom: 20px; }
.modal-body { line-height: 1.8; opacity: 0.9; font-size: 0.95rem; }
.modal-stats { display: flex; gap: 20px; margin-top: 25px; padding-top: 20px; border-top: 1px solid rgba(144,166,196,0.2); font-size: 0.9rem; opacity: 0.7; }
@media (max-width: 768px) { .container { padding: 15px 12px 60px; } .header { flex-direction: column; align-items: flex-start; gap: 12px; } .header-title h1 { font-size: 1.4rem; } .hot-item { padding: 14px; } }
</style>
