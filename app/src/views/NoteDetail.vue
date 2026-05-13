<template>
  <div class="note-detail-page">
    <!-- 顶部栏 -->
    <header class="sticky-header">
      <div class="header-inner">
        <button class="back-btn" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>
          返回列表
        </button>
        <h1 v-if="note" class="header-title">{{ note.title }}</h1>
      </div>
    </header>

    <!-- 加载中 -->
    <div v-if="!note" class="loading-state">
      <div class="loading-spinner" />
      <span>加载中...</span>
    </div>

    <!-- 主内容 -->
    <div v-else class="content-wrapper">
      <div class="content-layout">
        <!-- 左侧目录栏（桌面端） -->
        <aside class="sidebar">
          <div class="sidebar-inner">
            <h3 class="sidebar-section-title">目录</h3>
            <nav class="toc-nav">
              <a
                v-for="(heading, index) in tocItems"
                :key="index"
                :href="'#heading-' + index"
                :class="['toc-link', 'toc-level-' + heading.level, { active: activeTocIndex === index }]"
                @click.prevent="scrollToHeading(index)"
              >
                {{ heading.text }}
              </a>
            </nav>
          </div>
        </aside>

        <!-- 主内容区 -->
        <main class="main-content">
          <!-- 标签 -->
          <div class="detail-tags">
            <span :class="['tag', note.contentType === 0 ? 'tag-original' : 'tag-reprint']">
              {{ note.contentType === 0 ? '原创' : '转载' }}
            </span>
            <span v-if="note.categoryName" class="tag tag-category">{{ note.categoryName }}</span>
            <span v-for="tag in (note.tags || [])" :key="tag" class="tag tag-default">{{ tag }}</span>
          </div>

          <!-- 标题 -->
          <h1 class="detail-title">{{ note.title }}</h1>

          <!-- 元信息 -->
          <div class="detail-meta">
            <span class="meta-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
              {{ formatViewCount(note.viewCount) }} 阅读
            </span>
            <span v-if="note.readMinutes" class="meta-item">{{ note.readMinutes }} 分钟阅读</span>
            <span v-if="note.createdAt" class="meta-item">{{ formatDate(note.createdAt) }}</span>
          </div>

          <!-- Markdown 内容 -->
          <div class="detail-content-wrapper">
            <!-- 已解锁或公开笔记 -->
            <div v-if="isUnlocked || note.isLocked !== 1" class="md-body" v-html="renderedContent" />

            <!-- 未解锁笔记 -->
            <div v-else>
              <div class="lock-mask">
                <div class="md-body" v-html="renderedPreview" />
              </div>

              <!-- 锁定提示 -->
              <div class="lock-section">
                <div class="lock-icon-wrap">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                </div>
                <h3 class="lock-title">内容已锁定</h3>
                <p class="lock-desc">扫描二维码并输入口令，即可解锁全站所有文章</p>
                <button class="unlock-btn" @click="showUnlockModal = true">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  阅读全文
                </button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>

    <!-- 移动端目录浮窗 -->
    <button v-if="tocItems.length > 0" class="mobile-toc-btn" @click="showMobileToc = !showMobileToc">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" x2="21" y1="6" y2="6"/><line x1="8" x2="21" y1="12" y2="12"/><line x1="8" x2="21" y1="18" y2="18"/><line x1="3" x2="3.01" y1="6" y2="6"/><line x1="3" x2="3.01" y1="12" y2="12"/><line x1="3" x2="3.01" y1="18" y2="18"/></svg>
    </button>
    <div v-if="showMobileToc" class="mobile-toc-overlay" @click.self="showMobileToc = false">
      <div class="mobile-toc-panel">
        <h3 class="mobile-toc-title">目录</h3>
        <nav class="toc-nav">
          <a
            v-for="(heading, index) in tocItems"
            :key="index"
            :href="'#heading-' + index"
            :class="['toc-link', 'toc-level-' + heading.level]"
            @click.prevent="scrollToHeading(index); showMobileToc = false"
          >
            {{ heading.text }}
          </a>
        </nav>
      </div>
    </div>

    <!-- 解锁弹窗 -->
    <UnlockModal 
      v-if="showUnlockModal" 
      @close="showUnlockModal = false"
      @unlocked="handleUnlocked"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { getNoteDetail } from '../api/notes'
import UnlockModal from '../components/UnlockModal.vue'

const route = useRoute()
const note = ref(null)
const isUnlocked = ref(false)
const showUnlockModal = ref(false)
const showMobileToc = ref(false)
const activeTocIndex = ref(0)
let scrollHandler = null

// 格式化阅读数
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

// 从 Markdown 中提取标题生成目录
function extractHeadings(markdown) {
  if (!markdown) return []
  const lines = markdown.split('\n')
  const headings = []
  let inCodeBlock = false
  for (const line of lines) {
    if (line.trim().startsWith('```')) {
      inCodeBlock = !inCodeBlock
      continue
    }
    if (inCodeBlock) continue
    const match = line.match(/^(#{1,3})\s+(.+)/)
    if (match) {
      headings.push({
        level: match[1].length,
        text: match[2].replace(/[*`]/g, '').trim()
      })
    }
  }
  return headings
}

// 当前内容
const currentContent = computed(() => {
  if (isUnlocked.value || note.value?.isLocked !== 1) {
    return note.value?.fullContent || note.value?.previewContent || ''
  }
  return note.value?.previewContent || ''
})

// 目录项
const tocItems = computed(() => extractHeadings(currentContent.value))

// 配置 marked 并生成带锚点的 HTML
const renderedContent = computed(() => {
  const content = note.value?.fullContent || note.value?.previewContent || ''
  return renderMarkdownWithAnchors(content)
})

const renderedPreview = computed(() => {
  const content = note.value?.previewContent || ''
  return renderMarkdownWithAnchors(content)
})

function renderMarkdownWithAnchors(markdown) {
  if (!markdown) return ''
  let headingIndex = 0
  const renderer = new marked.Renderer()
  renderer.heading = function (text, level) {
    const id = `heading-${headingIndex++}`
    return `<h${level} id="${id}">${text}</h${level}>`
  }
  return marked(markdown, { renderer })
}

// 滚动到指定标题
function scrollToHeading(index) {
  const el = document.getElementById(`heading-${index}`)
  if (el) {
    const headerOffset = 80
    const top = el.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

// 监听滚动，高亮当前目录项
function setupScrollSpy() {
  scrollHandler = () => {
    const headings = document.querySelectorAll('.md-body h1, .md-body h2, .md-body h3')
    if (!headings.length) return
    let current = 0
    for (let i = 0; i < headings.length; i++) {
      const rect = headings[i].getBoundingClientRect()
      if (rect.top <= 100) current = i
    }
    activeTocIndex.value = current
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
}

// 加载笔记详情
async function loadNote() {
  try {
    const data = await getNoteDetail(route.params.id)
    if (data) {
      note.value = data
      isUnlocked.value = data.unlocked || false
      await nextTick()
      setupScrollSpy()
    }
  } catch (e) {
    console.error('加载笔记失败', e)
  }
}

// 处理解锁成功
function handleUnlocked() {
  isUnlocked.value = true
  showUnlockModal.value = false
  loadNote()
}

onMounted(() => {
  loadNote()
})

onUnmounted(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
})
</script>

<style scoped>
/* ========== 页面基础 ========== */
.note-detail-page {
  min-height: 100vh;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 50%, #f8fafc 100%);
}

/* ========== 顶部栏 ========== */
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
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  color: #64748b;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s;
  white-space: nowrap;
}
.back-btn:hover { color: #0f172a; background: #f1f5f9; }
.header-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: #0f172a;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 加载状态 ========== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  gap: 16px;
  color: #94a3b8;
  font-size: 0.875rem;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e2e8f0;
  border-top-color: #10b981;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== 内容区域 ========== */
.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}
.content-layout {
  display: flex;
  gap: 32px;
}

/* ========== 左侧目录栏 ========== */
.sidebar {
  display: none;
  width: 224px;
  flex-shrink: 0;
}
@media (min-width: 1024px) { .sidebar { display: block; } }
.sidebar-inner {
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.sidebar-section-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 12px;
}
.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.toc-link {
  display: block;
  font-size: 0.8125rem;
  color: #64748b;
  text-decoration: none;
  padding: 4px 12px;
  border-radius: 6px;
  border-left: 2px solid transparent;
  transition: all 0.2s;
  line-height: 1.5;
}
.toc-link:hover {
  color: #0f172a;
  background: #f1f5f9;
}
.toc-link.active {
  color: #059669;
  border-left-color: #10b981;
  background: #f0fdf4;
  font-weight: 500;
}
.toc-level-2 { padding-left: 24px; }
.toc-level-3 { padding-left: 36px; font-size: 0.75rem; }

/* ========== 主内容区 ========== */
.main-content {
  flex: 1;
  min-width: 0;
}

/* 标签 */
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

/* 标题 */
.detail-title {
  font-size: 1.875rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 16px;
  line-height: 1.4;
}

/* 元信息 */
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  font-size: 0.875rem;
  color: #94a3b8;
  margin-bottom: 32px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== Markdown 内容 ========== */
.detail-content-wrapper {
  position: relative;
}
.md-body {
  font-size: 15px;
  line-height: 1.8;
  color: #334155;
}
.md-body :deep(h1) {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 1.5rem 0 1rem;
  color: #0f172a;
  scroll-margin-top: 80px;
}
.md-body :deep(h2) {
  font-size: 1.35rem;
  font-weight: 600;
  margin: 1.5rem 0 0.75rem;
  color: #1e293b;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e2e8f0;
  scroll-margin-top: 80px;
}
.md-body :deep(h3) {
  font-size: 1.15rem;
  font-weight: 600;
  margin: 1.25rem 0 0.5rem;
  color: #334155;
  scroll-margin-top: 80px;
}
.md-body :deep(p) {
  margin: 0.75rem 0;
}
.md-body :deep(ul),
.md-body :deep(ol) {
  margin: 0.75rem 0;
  padding-left: 1.5rem;
}
.md-body :deep(li) {
  margin: 0.25rem 0;
}
.md-body :deep(strong) {
  font-weight: 600;
  color: #0f172a;
}
.md-body :deep(a) {
  color: #10b981;
  text-decoration: underline;
}
.md-body :deep(blockquote) {
  border-left: 3px solid #10b981;
  padding: 0.5rem 1rem;
  margin: 1rem 0;
  background: #f0fdf4;
  border-radius: 0 0.5rem 0.5rem 0;
}
.md-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
}
.md-body :deep(th),
.md-body :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 0.5rem 0.75rem;
  text-align: left;
}
.md-body :deep(th) {
  background: #f1f5f9;
  font-weight: 600;
}
.md-body :deep(pre) {
  background: #282c34;
  border-radius: 0.75rem;
  padding: 1rem;
  margin: 1rem 0;
  overflow-x: auto;
}
.md-body :deep(code) {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.875rem;
}
.md-body :deep(:not(pre) > code) {
  background: #f1f5f9;
  padding: 0.15rem 0.4rem;
  border-radius: 0.25rem;
  color: #e11d48;
  font-size: 0.85em;
}
.md-body :deep(img) {
  max-width: 100%;
  border-radius: 0.75rem;
  margin: 1rem 0;
}
.md-body :deep(hr) {
  border: none;
  border-top: 1px solid #e2e8f0;
  margin: 1.5rem 0;
}

/* ========== 锁定遮罩 ========== */
.lock-mask {
  position: relative;
}
.lock-mask::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 320px;
  background: linear-gradient(to bottom, rgba(248, 250, 252, 0), rgba(248, 250, 252, 0.6) 30%, rgba(248, 250, 252, 0.95));
  pointer-events: none;
}

/* ========== 锁定提示区 ========== */
.lock-section {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 0;
  margin-top: 16px;
}
.lock-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d1fae5, #ccfbf1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.2);
  animation: pulseGlow 2s infinite;
  color: #059669;
}
@keyframes pulseGlow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
}
.lock-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 8px;
}
.lock-desc {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0 0 24px;
  text-align: center;
  max-width: 400px;
}
.unlock-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 32px;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #10b981, #14b8a6);
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
  transition: all 0.3s;
}
.unlock-btn:hover {
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
  transform: translateY(-1px);
}

/* ========== 移动端目录按钮 ========== */
.mobile-toc-btn {
  display: flex;
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 40;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #0f172a;
  color: #fff;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.3);
  transition: all 0.2s;
}
.mobile-toc-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.4);
}
@media (min-width: 1024px) { .mobile-toc-btn { display: none; } }

/* ========== 移动端目录面板 ========== */
.mobile-toc-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.mobile-toc-panel {
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 24px;
  width: 100%;
  max-height: 60vh;
  overflow-y: auto;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
}
.mobile-toc-title {
  font-size: 1rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 16px;
}
@media (min-width: 1024px) { .mobile-toc-overlay { display: none; } }
</style>