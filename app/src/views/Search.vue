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
        <input type="text" class="search-input" v-model="query" placeholder="搜索帖子、热点..." @keydown.enter="doSearch">
        <button class="search-btn" @click="doSearch"><i class="ri-search-line"></i> 搜索</button>
      </div>
      <div class="filter-tags">
        <div class="filter-tag" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部</div>
        <div class="filter-tag" :class="{ active: filter === 'post' }" @click="filter = 'post'"><i class="ri-earth-line"></i> 社区</div>
        <div class="filter-tag" :class="{ active: filter === 'news' }" @click="filter = 'news'"><i class="ri-fire-line"></i> 热点</div>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <!-- 热门搜索词 -->
    <div class="hot-keywords" v-if="!searched && hotKeywordsList.length > 0">
      <div class="keywords-title"><i class="ri-fire-line"></i> 热门搜索</div>
      <div class="keywords-list">
        <span class="keyword-item" v-for="(kw, i) in hotKeywordsList" :key="i" @click="query = kw.keyword || kw; doSearch()">{{ kw.keyword || kw }}</span>
      </div>
    </div>

    <!-- 输入建议 -->
    <div class="suggestions" v-if="query && !searched && suggestionsList.length > 0">
      <div class="suggestion-item" v-for="(s, i) in suggestionsList" :key="i" @click="query = s; doSearch()">
        <i class="ri-search-line"></i> {{ s }}
      </div>
    </div>

    <div class="results-header">
      <div class="results-count">{{ resultsCount }}</div>
    </div>

    <LoadingSpinner v-if="searching" text="搜索中..." />

    <!-- 全局搜索结果 -->
    <div v-if="!searching && searched && filter === 'all' && globalResults">
      <!-- 社区结果 -->
      <div v-if="globalResults.posts && globalResults.posts.length > 0" class="result-section">
        <div class="section-title"><i class="ri-earth-line"></i> 社区帖子</div>
        <div class="results-list">
            <div class="result-item" v-for="item in globalResults.posts" :key="item.id || item.postId" @click="$router.push('/community/post/' + (item.postId || item.id))">
            <div class="result-icon"><i class="ri-earth-line"></i></div>
            <div class="result-content">
              <div class="result-title" v-html="highlight(item.title)"></div>
              <div class="result-desc" v-html="highlight(item.summary || item.desc || '')"></div>
            </div>
          </div>
        </div>
      </div>
      <!-- 新闻结果 -->
      <div v-if="globalResults.news && globalResults.news.length > 0" class="result-section">
        <div class="section-title"><i class="ri-fire-line"></i> 热点新闻</div>
        <div class="results-list">
          <div class="result-item" v-for="item in globalResults.news" :key="item.id || item.newsId" @click="$router.push('/hot')">
            <div class="result-icon"><i class="ri-fire-line"></i></div>
            <div class="result-content">
              <div class="result-title" v-html="highlight(item.title)"></div>
              <div class="result-desc" v-html="highlight(item.summary || item.desc || '')"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 按类型搜索结果 -->
    <div v-if="!searching && searched && filter !== 'all'" class="results-list">
      <div class="result-item" v-for="(r, i) in typedResults" :key="i" @click="navigateResult(r)">
        <div class="result-icon"><i :class="filter === 'post' ? 'ri-earth-line' : 'ri-fire-line'"></i></div>
        <div class="result-content">
          <div class="result-title" v-html="highlight(r.title)"></div>
          <div class="result-desc" v-html="highlight(r.summary || r.desc || '')"></div>
          <div class="result-meta">
            <span class="result-type" :class="filter">{{ filter === 'post' ? '社区' : '热点' }}</span>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { globalSearch, searchByType, hotKeywords, suggestions } from '../api/search'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()
const query = ref('')
const filter = ref('all')
const searched = ref(false)
const globalResults = ref(null)
const typedResults = ref([])
const hotKeywordsList = ref([])
const suggestionsList = ref([])
const resultsCountNum = ref(0)
const searching = ref(false)

let searchTimer = null

const resultsCount = computed(() => {
  if (!searched.value) return '输入关键词开始搜索'
  return `找到 ${resultsCountNum.value} 个结果`
})

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
  suggestionsList.value = []
  if (!q) { globalResults.value = null; typedResults.value = []; resultsCountNum.value = 0; searching.value = false; return }

  try {
    if (filter.value === 'all') {
      const data = await globalSearch(q, 10)
      globalResults.value = data || {}
      let count = 0
      if (data?.posts) count += data.posts.length
      if (data?.news) count += data.news.length
      if (data?.videos) count += data.videos.length
      if (data?.users) count += data.users.length
      resultsCountNum.value = count
    } else {
      const data = await searchByType(filter.value, q, 1, 20)
      typedResults.value = data?.records || []
      resultsCountNum.value = typedResults.value.length
    }
  } catch (e) {
    console.error('搜索失败:', e)
    globalResults.value = null
    typedResults.value = []
    resultsCountNum.value = 0
  } finally {
    searching.value = false
  }
}

function navigateResult(r) {
  if (filter.value === 'post') {
    router.push('/community/post/' + (r.postId || r.id))
  } else if (filter.value === 'news') {
    router.push('/hot')
  }
}

// 输入建议
watch(query, (val) => {
  if (searchTimer) clearTimeout(searchTimer)
  if (!val.trim()) { suggestionsList.value = []; return }
  searchTimer = setTimeout(async () => {
    try {
      const data = await suggestions(val.trim(), 8)
      suggestionsList.value = data || []
    } catch (e) {
      suggestionsList.value = []
    }
  }, 300)
})

async function loadHotKeywords() {
  try {
    const data = await hotKeywords(10)
    hotKeywordsList.value = data || []
  } catch (e) {
    hotKeywordsList.value = []
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

onMounted(() => {
  loadHotKeywords()
})
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
.filter-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.filter-tag { padding: 6px 16px; border-radius: 20px; cursor: pointer; background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.2); color: #a8bcd4; font-size: 0.85rem; transition: 0.3s; }
.filter-tag.active, .filter-tag:hover { background: rgba(144,166,196,0.3); border-color: rgba(144,166,196,0.5); color: #fff; }

/* Hot Keywords */
.hot-keywords { margin-bottom: 20px; }
.keywords-title { font-size: 0.9rem; color: #a8bcd4; margin-bottom: 10px; display: flex; align-items: center; gap: 6px; }
.keywords-list { display: flex; flex-wrap: wrap; gap: 8px; }
.keyword-item { padding: 6px 14px; border-radius: 16px; background: rgba(144,166,196,0.08); border: 1px solid rgba(144,166,196,0.18); color: #a8bcd4; font-size: 0.82rem; cursor: pointer; transition: 0.3s; }
.keyword-item:hover { background: rgba(144,166,196,0.2); border-color: rgba(144,166,196,0.4); color: #fff; }

/* Suggestions */
.suggestions { margin-bottom: 16px; }
.suggestion-item { padding: 8px 16px; border-radius: 10px; cursor: pointer; color: #a8bcd4; font-size: 0.88rem; transition: 0.2s; display: flex; align-items: center; gap: 8px; }
.suggestion-item:hover { background: rgba(144,166,196,0.15); color: #fff; }
.suggestion-item i { font-size: 0.85rem; opacity: 0.5; }

/* Results */
.result-section { margin-bottom: 24px; }
.section-title { font-size: 0.95rem; color: #c5d5ea; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; letter-spacing: 1px; }
.results-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.results-count { font-size: 0.9rem; opacity: 0.7; }
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
.result-type.post { background: rgba(144,166,196,0.2); color: #c9d9ef; border-color: rgba(144,166,196,0.3); }
.result-type.news { background: rgba(196,142,110,0.2); color: #f0d8b0; border-color: rgba(196,142,110,0.3); }

:deep(.highlight) { background: rgba(144,166,196,0.3); padding: 1px 4px; border-radius: 4px; }

</style>