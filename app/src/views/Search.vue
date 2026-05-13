<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere purple"></div></div>
        <div class="header-title">
          <h1>搜索区</h1>
          <p>紫微星 · 全站信息聚合</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="search-box">
      <div class="search-input-wrap">
        <input type="text" class="search-input" v-model="query" placeholder="搜索笔记、热点..." @keydown.enter="doSearch">
        <button class="search-btn" @click="doSearch"><i class="ri-search-line"></i> 搜索</button>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <!-- 搜索结果 -->
    <LoadingSpinner v-if="searching" text="搜索中..." />
    <div class="results-list" v-else-if="!searching && searched && searchResults.length > 0">
      <div class="result-item" v-for="(r, i) in searchResults" :key="r.id || i" @click="navigateResult(r)">
        <div class="result-icon">
          <i :class="r.type === 'NOTE' ? 'ri-article-line' : 'ri-fire-line'"></i>
        </div>
        <div class="result-content">
          <div class="result-title" v-html="highlight(r.title)"></div>
          <div class="result-desc" v-html="highlight(r.summary || r.content || '')"></div>
          <div class="result-meta">
            <span class="result-type" :class="r.type === 'NOTE' ? 'note' : 'hot'">
              {{ r.type === 'NOTE' ? '笔记' : '热点' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-if="!searching && searched && resultsCountNum === 0">
      <p><i class="ri-emotion-sad-line"></i> 未找到与 "{{ query }}" 相关的结果</p>
      <p>试试其他关键词？</p>
    </div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->

</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { globalSearch } from '../api/search'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()
const query = ref('')
const searched = ref(false)
const searchResults = ref([])
const resultsCountNum = ref(0)
const searching = ref(false)

function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}
function highlight(text) {
  if (!query.value || !text) return escapeHtml(text || '')
  const q = escapeHtml(query.value)
  const regex = new RegExp('(' + q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi')
  return escapeHtml(text).replace(regex, '<span class="highlight">$1</span>')
}

async function doSearch() {
  const q = query.value.trim()
  searched.value = true
  searching.value = true
  if (!q) { 
    searchResults.value = []
    resultsCountNum.value = 0
    searching.value = false
    return 
  }

  try {
    const data = await globalSearch(q, 1, 50)
    searchResults.value = data?.records || []
    resultsCountNum.value = searchResults.value.length
  } catch (e) {
    console.error('搜索失败:', e)
    searchResults.value = []
    resultsCountNum.value = 0
  } finally {
    searching.value = false
  }
}

function navigateResult(r) {
  if (r.type === 'NOTE') {
    router.push('/notes/' + r.id)
  } else if (r.type === 'HOT') {
    router.push('/hot')
  }
}

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
.container { position: relative; z-index: 1; max-width: 900px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.35); box-shadow: 0 0 15px rgba(144,166,196,0.3); }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }
.empty-state p { margin-bottom: 10px; }
.search-box { border: 1px solid rgba(144,166,196,0.25); border-radius: 24px; padding: 25px; backdrop-filter: blur(10px); }
.search-input-wrap { display: flex; gap: 12px; margin-bottom: 15px; }
.search-input { flex: 1; padding: 14px 22px; border-radius: 30px; background: rgba(10,6,20,0.6); border: 1px solid rgba(144,166,196,0.3); color: #e8eef7; font-size: 1rem; outline: none; font-family: inherit; transition: 0.3s; }
.search-input:focus { border-color: rgba(144,166,196,0.7); box-shadow: 0 0 20px rgba(144,166,196,0.15); }
.search-input::placeholder { color: rgba(144,166,196,0.5); }
.search-btn { padding: 14px 32px; border-radius: 30px; border: none; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: white; cursor: pointer; font-size: 1rem; transition: 0.3s; letter-spacing: 1px; }
.search-btn:hover { box-shadow: 0 0 25px rgba(144,166,196,0.4); transform: translateY(-1px); }
.results-list { display: flex; flex-direction: column; gap: 12px; }
.result-item { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 18px 22px; cursor: pointer; transition: 0.3s; display: flex; gap: 15px; align-items: flex-start; }
.result-item:hover { border-color: rgba(144,166,196,0.5); box-shadow: 0 5px 25px rgba(144,166,196,0.1); transform: translateY(-2px); }
.result-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; flex-shrink: 0; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.3); }
.result-icon i { font-size: 1.4rem; }
.result-content { flex: 1; }
.result-title { font-size: 1.05rem; margin-bottom: 6px; color: #e8eef7; }
.result-desc { font-size: 0.88rem; opacity: 0.7; line-height: 1.5; margin-bottom: 8px; }
.result-meta { display: flex; gap: 15px; font-size: 0.8rem; opacity: 0.5; align-items: center; }
.result-type { display: inline-block; padding: 3px 12px; border-radius: 12px; font-size: 0.75rem; background: rgba(144,166,196,0.2); color: #a8bcd4; border: 1px solid rgba(144,166,196,0.3); }

:deep(.highlight) { background: rgba(144,166,196,0.3); padding: 1px 4px; border-radius: 4px; }

</style>