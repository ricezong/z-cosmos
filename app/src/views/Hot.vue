<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
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
      <div class="source-tab" :class="{ active: currentSort === 'latest' }" @click="currentSort = 'latest'; loadNews()"><i class="ri-time-line"></i> 最新</div>
      <div class="source-tab" :class="{ active: currentSort === 'hot' }" @click="currentSort = 'hot'; loadNews()"><i class="ri-fire-line"></i> 最热</div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <LoadingSpinner v-if="loading" text="加载中..." />
    <div class="hot-list" v-else-if="newsList.length > 0">
      <div class="hot-item" v-for="(item, idx) in newsList" :key="item.newsId" @click="openModal(item)">
        <div class="hot-rank" :class="{ top3: idx < 3 }">{{ idx + 1 }}</div>
        <div class="hot-content">
          <div class="hot-title">{{ item.title }}</div>
          <div class="hot-desc">{{ item.summary || '' }}</div>
          <div class="hot-meta">
            <span v-if="item.category" class="hot-tag">{{ item.category }}</span>
            <span v-if="item.source"><i class="ri-link"></i> {{ item.source }}</span>
            <span><i class="ri-eye-line"></i> {{ item.viewCount || 0 }}</span>
            <span><i class="ri-time-line"></i> {{ formatTime(item.publishedAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="empty-state" v-else><p>{{ loading ? '加载中...' : '暂无热点数据' }}</p></div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->


  <!-- Modal -->
  <div class="modal-overlay" :class="{ active: modalItem }" @click.self="modalItem = null">
    <div class="modal-box" v-if="modalItem">
      <button class="modal-close" @click="modalItem = null">×</button>
      <h2 class="modal-title">{{ modalItem.title }}</h2>
      <div class="modal-source">{{ modalItem.source || '' }} · {{ formatTime(modalItem.publishedAt) }}</div>
      <div class="modal-body">
        <p v-if="modalItem.summary" style="margin-bottom:20px">{{ modalItem.summary }}</p>
        <div v-if="modalItem.content" v-html="modalItem.content"></div>
      </div>
      <div class="modal-stats">
        <span><i class="ri-eye-line"></i> 浏览 {{ modalItem.viewCount || 0 }}</span>
        <span v-if="modalItem.source"><i class="ri-link"></i> {{ modalItem.source }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listNews, getNews } from '../api/hot'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const currentSort = ref('latest')
const newsList = ref([])
const modalItem = ref(null)
const loading = ref(true)

const showBackTop = ref(false)
const contentRef = ref(null)
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
    const data = await listNews({ sort: currentSort.value, page: 1, size: 50 })
    newsList.value = data?.records || []
  } catch (e) {
    console.error('加载新闻失败:', e)
    newsList.value = []
  } finally {
    loading.value = false
  }
}

async function openModal(item) {
  // 加载详情
  try {
    const detail = await getNews(item.newsId)
    modalItem.value = detail || item
  } catch (e) {
    modalItem.value = item
  }
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
.container { position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.15); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.3); box-shadow: 0 0 15px rgba(144,166,196,0.3); }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }
.source-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.source-tab { padding: 8px 16px; border-radius: 20px; cursor: pointer; background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.2); color: #a8bcd4; transition: 0.3s; font-size: 0.85rem; }
.source-tab.active, .source-tab:hover { background: rgba(144,166,196,0.25); border-color: rgba(144,166,196,0.5); color: #fff; }
.hot-list { display: flex; flex-direction: column; gap: 12px; }
.hot-item { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 18px 22px; cursor: pointer; transition: 0.3s; display: flex; gap: 15px; align-items: flex-start; }
.hot-item:hover { border-color: rgba(144,166,196,0.5); box-shadow: 0 5px 25px rgba(144,166,196,0.1); transform: translateY(-2px); }
.hot-rank { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 0.9rem; flex-shrink: 0; background: rgba(144,166,196,0.2); color: #c5d5ea; border: 1px solid rgba(144,166,196,0.3); }
.hot-rank.top3 { background: linear-gradient(135deg, #d8a987, #b59065); color: #1a1510; border: none; box-shadow: 0 0 10px rgba(144,166,196,0.3); }
.hot-content { flex: 1; }
.hot-title { font-size: 1.05rem; margin-bottom: 6px; color: #e8eef7; }
.hot-desc { font-size: 0.88rem; opacity: 0.7; line-height: 1.5; margin-bottom: 8px; }
.hot-meta { display: flex; gap: 15px; font-size: 0.8rem; opacity: 0.5; align-items: center; }
.hot-tag { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; background: rgba(144,166,196,0.15); color: #a8bcd4; border: 1px solid rgba(144,166,196,0.2); }
.modal-overlay { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(10,8,6,0.85); z-index: 100; backdrop-filter: blur(5px); align-items: center; justify-content: center; padding: 20px; }
.modal-overlay.active { display: flex; }
.modal-box { border: 1px solid rgba(144,166,196,0.3); border-radius: 24px; max-width: 600px; width: 100%; max-height: 80vh; overflow-y: auto; padding: 30px; box-shadow: 0 25px 50px rgba(0,0,0,0.5); background: rgba(15,20,35,0.95); }
.modal-close { float: right; width: 36px; height: 36px; border-radius: 50%; background: rgba(144,166,196,0.15); border: 1px solid rgba(144,166,196,0.3); color: #a8bcd4; cursor: pointer; font-size: 1.2rem; display: flex; align-items: center; justify-content: center; transition: 0.3s; }
.modal-close:hover { background: rgba(144,166,196,0.3); }
.modal-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 10px; color: #e8eef7; }
.modal-source { font-size: 0.85rem; opacity: 0.6; margin-bottom: 20px; }
.modal-body { line-height: 1.8; opacity: 0.9; font-size: 0.95rem; }
.modal-stats { display: flex; gap: 20px; margin-top: 25px; padding-top: 20px; border-top: 1px solid rgba(144,166,196,0.2); font-size: 0.9rem; opacity: 0.7; }

</style>