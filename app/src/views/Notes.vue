<template>
  <div class="notes-page">
    <!-- 星空背景 -->
    <div class="star-bg"></div>

    <div class="page-layout">
      <!-- 固定顶部栏（不改动） -->
      <div class="page-fixed">
        <header class="header">
          <div class="header-left">
            <div class="planet-icon">
              <div class="planet-sphere earth"></div>
            </div>
            <div class="header-title">
              <h1>技术笔记</h1>
              <p>探索 · 记录 · 分享</p>
            </div>
          </div>
          <router-link to="/" class="back-btn">
            <i class="ri-arrow-left-line"></i> 返回星域
          </router-link>
        </header>

        <!-- 分类筛选 -->
        <div class="container">
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
        </div>
      </div>

      <!-- 可滚动内容区 -->
      <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
        <div class="container">
          <!-- 精选区域（横向滚动，md以上可见） -->
          <div v-if="topNotes.length > 0" class="featured-section hidden md:block">
            <div class="featured-header">
              <h3 class="section-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 6px;">
                  <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
                  <path d="M5 3v4"/><path d="M19 17v4"/><path d="M3 5h4"/><path d="M17 19h4"/>
                </svg>
                本周精选
              </h3>
              <span class="featured-hint">左右滑动查看更多</span>
            </div>
            <div class="featured-scroll">
              <article
                  v-for="note in topNotes"
                  :key="note.noteId"
                  class="note-card featured-card"
                  @click="goToDetail(note.noteId)"
              >
                <div class="card-cover">
                  <div class="cover-text-bg">
                    <span class="cover-title-text">{{ note.title }}</span>
                  </div>
                  <div class="cover-overlay" />
                  <div class="cover-action">
                    <span>
                      阅读全文
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                    </span>
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
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                      <span>{{ formatViewCount(note.viewCount) }} 阅读</span>
                    </div>
                    <span class="card-date">{{ formatDate(note.createdAt) }}</span>
                  </div>
                </div>
              </article>
            </div>
          </div>

          <!-- 全部文章标题 -->
          <h3 v-if="notes.length > 0" class="section-title hidden md:block" style="margin-top: 32px;">全部文章</h3>

          <!-- 笔记网格 -->
          <div class="notes-grid" v-if="notes.length > 0">
            <article
                v-for="note in notes"
                :key="note.noteId"
                class="note-card"
                @click="goToDetail(note.noteId)"
            >
              <div class="card-cover">
                <div class="cover-text-bg">
                  <span class="cover-title-text">{{ note.title }}</span>
                </div>
                <div class="cover-overlay" />
                <div class="cover-action">
                  <span>
                    阅读全文
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                  </span>
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
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                    <span>{{ formatViewCount(note.viewCount) }} 阅读</span>
                  </div>
                  <span class="card-date">{{ formatDate(note.createdAt) }}</span>
                </div>
              </div>
            </article>
          </div>

          <!-- 加载更多指示器（关键新增） -->
          <LoadingSpinner v-if="loading && notes.length > 0" text="加载中..." />

          <!-- 无限滚动触发器 -->
          <div ref="loadMoreRef" class="load-more-trigger" />

          <!-- 底部提示 -->
          <div v-if="!hasMore && notes.length > 0" class="bottom-tip">— 已经到底了 —</div>

          <!-- 加载骨架屏（仅首次加载时显示） -->
          <div v-if="loading && notes.length === 0" class="notes-grid skeleton-grid">
            <div v-for="i in 2" :key="i" class="skeleton-card">
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

          <!-- 空状态 -->
          <div v-if="!loading && notes.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" /></svg>
            </div>
            <p class="empty-title">暂无笔记</p>
            <p class="empty-desc">换个分类试试吧</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoteList, getCategories } from '../api/notes'
import LoadingSpinner from '../components/LoadingSpinner.vue'

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

// 精选笔记：按浏览量降序取前5
const topNotes = computed(() => {
  return [...notes.value].sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0)).slice(0, 5)
})

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

function selectCategory(code) {
  selectedCategory.value = code
  loadNotes(true)
}

function goToDetail(noteId) {
  router.push(`/notes/${noteId}`)
}

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
  setTimeout(setupObserver, 100)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* ========== 页面布局 ========== */
.notes-page {
  height: 100vh;
  overflow: hidden;
}
.container {
  position: relative;
  z-index: 1;
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}

/* ========== 顶部栏（不动） ========== */
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

/* ========== 分类筛选（不动） ========== */
.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0;
}
.filter-btn {
  padding: 6px 14px;
  border-radius: 16px;
  cursor: pointer;
  background: rgba(144, 166, 196, 0.08);
  border: 1px solid rgba(144, 166, 196, 0.18);
  color: #a8bcd4;
  font-size: 0.82rem;
  white-space: nowrap;
  transition: 0.3s;
}
.filter-btn.active,
.filter-btn:hover {
  background: rgba(144, 166, 196, 0.3);
  border-color: rgba(144, 166, 196, 0.5);
  color: #fff;
}

/* ========== 精选区域 ========== */
.featured-section {
  margin-bottom: 8px;
}
.featured-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.featured-header .section-title {
  display: flex;
  align-items: center;
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
}
.featured-hint {
  font-size: 0.75rem;
  color: rgba(168, 188, 212, 0.4);
}
.featured-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}
.featured-scroll::-webkit-scrollbar {
  height: 4px;
}
.featured-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.featured-scroll::-webkit-scrollbar-thumb {
  background: rgba(144, 166, 196, 0.2);
  border-radius: 2px;
}
.featured-card {
  flex: 0 0 260px;
  scroll-snap-align: start;
}

/* ========== 全部文章标题 ========== */
.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
}

/* ========== 笔记网格 ========== */
.notes-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}
@media (min-width: 640px) {
  .notes-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ========== 笔记卡片 ========== */
.note-card {
  border: 1px solid rgba(144, 166, 196, 0.2);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s, border-color 0.3s;
  background: rgba(5, 12, 26, 0.4);
  backdrop-filter: blur(8px);
}
.note-card:hover {
  border-color: rgba(144, 166, 196, 0.5);
  box-shadow: 0 8px 30px rgba(144, 166, 196, 0.2);
  transform: translateY(-4px);
}

/* 封面 */
.card-cover {
  position: relative;
  height: 140px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(120, 144, 181, 0.15), rgba(168, 188, 212, 0.08));
}
/* 文字占位封面 */
.cover-text-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: linear-gradient(135deg, rgba(120, 144, 181, 0.35), rgba(168, 188, 212, 0.1));
  transition: transform 0.5s ease;
}
.note-card:hover .cover-text-bg {
  transform: scale(1.05);
}
.cover-title-text {
  font-size: 0.95rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(5, 8, 20, 0.5), transparent);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}
.note-card:hover .cover-overlay {
  opacity: 1;
}
.cover-action {
  position: absolute;
  bottom: 10px;
  right: 10px;
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
  padding: 5px 10px;
  border-radius: 8px;
  font-size: 0.72rem;
  font-weight: 500;
  background: rgba(15, 30, 55, 0.8);
  color: #c5d5ea;
  border: 1px solid rgba(144, 166, 196, 0.3);
  backdrop-filter: blur(4px);
}

/* 卡片内容 */
.card-body {
  padding: 16px;
}
.card-title {
  font-size: 1.05rem;
  font-weight: 500;
  color: #fff;
  margin: 0 0 8px;
  transition: color 0.2s;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.note-card:hover .card-title {
  color: #c5d5ea;
}
.card-summary {
  font-size: 0.85rem;
  color: rgba(168, 188, 212, 0.8);
  line-height: 1.6;
  margin: 0 0 12px;
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
  margin-bottom: 12px;
}
.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 9999px;
  font-size: 0.72rem;
  font-weight: 500;
  border: 1px solid transparent;
}
.tag-original {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
  border-color: rgba(16, 185, 129, 0.25);
}
.tag-reprint {
  background: rgba(245, 158, 11, 0.12);
  color: #fbbf24;
  border-color: rgba(245, 158, 11, 0.25);
}
.tag-category {
  background: rgba(59, 130, 246, 0.12);
  color: #60a5fa;
  border-color: rgba(59, 130, 246, 0.25);
}
.tag-default {
  background: rgba(144, 166, 196, 0.08);
  color: #a8bcd4;
  border-color: rgba(144, 166, 196, 0.18);
}

/* 卡片底部 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid rgba(144, 166, 196, 0.1);
}
.card-views,
.card-date {
  font-size: 0.75rem;
  color: rgba(168, 188, 212, 0.45);
}
.card-views {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 骨架屏 ========== */
.skeleton-card {
  border: 1px solid rgba(144, 166, 196, 0.2);
  border-radius: 16px;
  overflow: hidden;
  background: rgba(5, 12, 26, 0.4);
}
.skeleton-cover {
  height: 140px;
  background: rgba(144, 166, 196, 0.05);
  animation: pulse 1.5s ease-in-out infinite;
}
.skeleton-body {
  padding: 16px;
}
.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background: rgba(144, 166, 196, 0.08);
  animation: pulse 1.5s ease-in-out infinite;
}
.skeleton-title {
  width: 70%;
  margin-bottom: 10px;
}
.skeleton-text {
  width: 100%;
  margin-bottom: 12px;
}
.skeleton-tags {
  display: flex;
  gap: 6px;
}
.skeleton-tag {
  width: 40px;
  height: 18px;
  border-radius: 9999px;
  background: rgba(144, 166, 196, 0.08);
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 50px 20px;
  opacity: 0.5;
}
.empty-icon {
  color: rgba(168, 188, 212, 0.3);
  margin-bottom: 12px;
}
.empty-title {
  font-size: 1rem;
  color: rgba(168, 188, 212, 0.5);
  margin: 0 0 4px;
}
.empty-desc {
  font-size: 0.85rem;
  color: rgba(168, 188, 212, 0.35);
  margin: 0;
}

/* ========== 其他 ========== */
.load-more-trigger {
  height: 40px;
}
.bottom-tip {
  text-align: center;
  padding: 24px 0;
  font-size: 0.8rem;
  color: rgba(168, 188, 212, 0.35);
}
</style>