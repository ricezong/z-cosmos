<template>
  <div class="notes-page">
    <!-- 顶部标题栏 -->
    <header class="sticky-header">
      <div class="header-inner">
        <div class="header-left">
          <div class="header-icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
          </div>
          <div>
            <h1 class="header-title">技术笔记</h1>
            <p class="header-subtitle">探索 · 记录 · 分享</p>
          </div>
        </div>
        <div class="header-right" @click="$router.push('/search')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          <span class="search-text">搜索</span>
        </div>
      </div>
    </header>

    <div class="content-wrapper">
      <!-- 分类筛选 -->
      <div class="category-filter">
        <button 
          v-for="cat in categories" 
          :key="cat.categoryCode"
          :class="['filter-btn', { active: selectedCategory === cat.categoryCode }]"
          @click="selectCategory(cat.categoryCode)"
        >
          {{ cat.categoryName }}
        </button>
      </div>

      <!-- 笔记网格 -->
      <div class="notes-grid">
        <article 
          v-for="note in notes" 
          :key="note.noteId" 
          class="note-card"
          @click="goToDetail(note.noteId)"
        >
          <div class="card-cover">
            <div class="cover-placeholder">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.3"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
            </div>
            <div class="cover-overlay" />
            <div class="cover-action">
              <span>阅读全文 <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg></span>
            </div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ note.title }}</h3>
            <p class="card-summary">{{ note.shortSummary }}</p>
            <div class="card-tags">
              <span :class="['tag', note.contentType === 0 ? 'tag-original' : 'tag-reprint']">
                {{ note.contentType === 0 ? '原创' : '转载' }}
              </span>
              <span v-if="note.categoryName" class="tag tag-category">{{ note.categoryName }}</span>
              <span v-for="tag in (note.tags || [])" :key="tag" class="tag tag-default">{{ tag }}</span>
            </div>
            <div class="card-footer">
              <div class="card-views">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                <span>{{ formatViewCount(note.viewCount) }} 阅读</span>
              </div>
              <span class="card-date">{{ formatDate(note.createdAt) }}</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="notes-grid skeleton-grid">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <div class="skeleton-cover" />
          <div class="skeleton-body">
            <div class="skeleton-line skeleton-title" />
            <div class="skeleton-line skeleton-text" />
            <div class="skeleton-tags">
              <div class="skeleton-tag" /><div class="skeleton-tag" />
            </div>
          </div>
        </div>
      </div>

      <!-- 无限滚动触发器 -->
      <div ref="loadMoreRef" class="load-more-trigger" />

      <!-- 底部提示 -->
      <div v-if="!hasMore && notes.length > 0" class="bottom-tip">— 已经到底了 —</div>

      <!-- 空状态 -->
      <div v-if="!loading && notes.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" /></svg>
        </div>
        <p class="empty-title">暂无笔记</p>
        <p class="empty-desc">换个分类试试吧</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoteList, getCategories } from '../api/notes'

const router = useRouter()

const categories = ref([{ categoryCode: '', categoryName: '全部' }])
const notes = ref([])
const selectedCategory = ref('')
const page = ref(1)
const size = ref(6)
const loading = ref(false)
const hasMore = ref(true)
const loadMoreRef = ref(null)
let observer = null

function formatViewCount(count) {
  if (!count) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.split('T')[0] || dateStr
}

// 加载分类
async function loadCategories() {
  try {
    const data = await getCategories()
    if (data && data.length) {
      categories.value = [{ categoryCode: '', categoryName: '全部' }, ...data]
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 加载笔记列表
async function loadNotes(reset = false) {
  if (loading.value || (!reset && !hasMore.value)) return
  
  loading.value = true
  try {
    if (reset) {
      page.value = 1
      notes.value = []
    }
    
    const pageData = await getNoteList(page.value, size.value, selectedCategory.value)
    if (pageData) {
      const newNotes = pageData.records || []
      notes.value = reset ? newNotes : [...notes.value, ...newNotes]
      hasMore.value = notes.value.length < pageData.total
      page.value++
    }
  } catch (e) {
    console.error('加载笔记失败', e)
  } finally {
    loading.value = false
  }
}

// 选择分类
function selectCategory(code) {
  selectedCategory.value = code
  loadNotes(true)
}

// 跳转详情
function goToDetail(noteId) {
  router.push(`/notes/${noteId}`)
}

// 使用 IntersectionObserver 实现无限滚动
function setupObserver() {
  if (!loadMoreRef.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        loadNotes()
      }
    },
    { threshold: 0.1 }
  )
  observer.observe(loadMoreRef.value)
}

onMounted(() => {
  loadCategories()
  loadNotes(true)
  // 延迟设置 observer，确保 DOM 已渲染
  setTimeout(setupObserver, 100)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* ========== 页面基础 ========== */
.notes-page {
  min-height: 100vh;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 50%, #f8fafc 100%);
}

/* ========== 顶部标题栏 ========== */
.sticky-header {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #34d399, #14b8a6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}
.header-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}
.header-subtitle {
  font-size: 0.75rem;
  color: #94a3b8;
  margin: -2px 0 0;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}
.header-right:hover { color: #0f172a; }
.search-text { display: none; }
@media (min-width: 640px) { .search-text { display: inline; } }

/* ========== 内容区域 ========== */
.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ========== 分类筛选 ========== */
.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 32px;
}
.filter-btn {
  padding: 8px 20px;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  background: #f1f5f9;
  color: #475569;
}
.filter-btn:hover { background: #e2e8f0; }
.filter-btn.active {
  background: #0f172a;
  color: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.2);
}

/* ========== 笔记网格 ========== */
.notes-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}
@media (min-width: 640px) { .notes-grid { grid-template-columns: repeat(2, 1fr); } }
@media (min-width: 1024px) { .notes-grid { grid-template-columns: repeat(3, 1fr); } }

/* ========== 笔记卡片 ========== */
.note-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  animation: slideUp 0.3s ease-out both;
}
.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(148, 163, 184, 0.15);
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 卡片封面 */
.card-cover {
  position: relative;
  height: 176px;
  overflow: hidden;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}
.note-card:hover .cover-overlay { opacity: 1; }
.cover-action {
  position: absolute;
  bottom: 12px;
  right: 12px;
  opacity: 0;
  transform: translateX(8px);
  transition: all 0.3s;
}
.note-card:hover .cover-action {
  opacity: 1;
  transform: translateX(0);
}
.cover-action span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.9);
  color: #334155;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(8px);
}

/* 卡片内容 */
.card-body {
  padding: 20px;
}
.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}
.note-card:hover .card-title { color: #059669; }
.card-summary {
  font-size: 0.875rem;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 标签 */
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}
.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}
.tag-original { background: #d1fae5; color: #047857; }
.tag-reprint { background: #fef3c7; color: #b45309; }
.tag-category { background: #dbeafe; color: #1d4ed8; }
.tag-default { background: #f1f5f9; color: #475569; }

/* 卡片底部 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}
.card-views {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: #94a3b8;
}
.card-date {
  font-size: 0.75rem;
  color: #94a3b8;
}

/* ========== 骨架屏 ========== */
.skeleton-grid { margin-top: 24px; }
.skeleton-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
}
.skeleton-cover {
  height: 176px;
  background: #f1f5f9;
  animation: pulse 1.5s ease-in-out infinite;
}
.skeleton-body { padding: 20px; }
.skeleton-line {
  height: 16px;
  border-radius: 4px;
  background: #f1f5f9;
  animation: pulse 1.5s ease-in-out infinite;
}
.skeleton-title { width: 75%; margin-bottom: 12px; }
.skeleton-text { width: 100%; margin-bottom: 16px; }
.skeleton-tags { display: flex; gap: 8px; }
.skeleton-tag {
  width: 48px;
  height: 20px;
  border-radius: 9999px;
  background: #f1f5f9;
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ========== 无限滚动触发器 ========== */
.load-more-trigger {
  height: 40px;
  margin-top: 16px;
}

/* ========== 底部提示 ========== */
.bottom-tip {
  text-align: center;
  padding: 32px 0;
  font-size: 0.875rem;
  color: #94a3b8;
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 80px 0;
}
.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cbd5e1;
}
.empty-title {
  font-size: 1.125rem;
  font-weight: 500;
  color: #475569;
  margin: 0 0 4px;
}
.empty-desc {
  font-size: 0.875rem;
  color: #94a3b8;
  margin: 0;
}
</style>