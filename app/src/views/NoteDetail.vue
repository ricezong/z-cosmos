<template>
  <div class="note-detail-page">
    <!-- 星空背景（复用全局样式） -->
    <div class="star-bg"></div>

    <div class="page-layout">
      <!-- 固定顶部栏 -->
      <div class="page-fixed">
        <header class="header">
          <div class="header-left">
            <div class="planet-icon">
              <div class="planet-sphere earth"></div>
            </div>
            <div class="header-title">
              <h1>笔记详情</h1>
              <p>探索 · 记录 · 分享</p>
            </div>
          </div>
          <button class="back-btn" @click="$router.back()">
            <i class="ri-arrow-left-line"></i> 返回列表
          </button>
        </header>
      </div>

      <!-- 可滚动内容区 -->
      <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
        <div class="container">
          <!-- 加载中 -->
          <LoadingSpinner v-if="!note" text="加载中..." />

          <!-- 主内容 -->
          <div v-else class="content-layout">
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
              <!-- 面包屑 -->
              <div class="detail-header">
                <button class="back-link" @click="$router.push('/notes')">
                  <i class="ri-arrow-left-s-line"></i>
                  <span>笔记列表</span>
                </button>
                <div class="header-separator"></div>
                <div class="header-breadcrumb">
                  <span class="breadcrumb-current">笔记详情</span>
                </div>
              </div>

              <!-- 帖子卡片 -->
              <article class="detail-post">
                <span class="detail-decor"></span>

                <!-- 标签行 -->
                <div class="detail-meta-row">
                  <span :class="['post-tag', note.contentType === 0 ? 'tag-original' : 'tag-reprint']">
                    {{ note.contentType === 0 ? '原创' : '转载' }}
                  </span>
                  <span v-if="note.categoryName" class="post-tag tag-category">{{ note.categoryName }}</span>
                  <span v-for="tag in (note.tags || [])" :key="tag" class="post-tag tag-default">{{ tag }}</span>
                  <span class="meta-chip">
                    <i class="ri-eye-line"></i> {{ formatViewCount(note.viewCount) }} 阅读
                  </span>
                  <span v-if="note.readMinutes" class="meta-chip">
                    <i class="ri-time-line"></i> {{ note.readMinutes }} 分钟
                  </span>
                  <span v-if="note.createdAt" class="meta-chip">
                    <i class="ri-calendar-line"></i> {{ formatDate(note.createdAt) }}
                  </span>
                </div>

                <!-- 标题 -->
                <h1 class="detail-title">{{ note.title }}</h1>
                <div class="title-underline"></div>

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
                        <i class="ri-lock-2-line"></i>
                      </div>
                      <h3 class="lock-title">内容已锁定</h3>
                      <p class="lock-desc">扫描二维码并输入口令，即可解锁全站所有文章</p>
                      <button class="unlock-btn" @click="showUnlockModal = true">
                        <i class="ri-lock-unlock-line"></i> 阅读全文
                      </button>
                    </div>
                  </div>
                </div>
              </article>
            </main>
          </div>
        </div>
      </div>

      <!-- 解锁弹窗 -->
      <Teleport to="body">
        <UnlockModal
            v-if="showUnlockModal"
            @close="showUnlockModal = false"
            @unlocked="handleUnlocked"
        />
      </Teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { getNoteDetail } from '../api/notes'
import UnlockModal from '../components/UnlockModal.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const route = useRoute()
const note = ref(null)
const isUnlocked = ref(false)
const showUnlockModal = ref(false)
const activeTocIndex = ref(0)
let scrollHandler = null

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

// 提取 Markdown 标题生成目录
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

const currentContent = computed(() => {
  if (isUnlocked.value || note.value?.isLocked !== 1) {
    return note.value?.fullContent || note.value?.previewContent || ''
  }
  return note.value?.previewContent || ''
})

const tocItems = computed(() => extractHeadings(currentContent.value))

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

function scrollToHeading(index) {
  const el = document.getElementById(`heading-${index}`)
  if (el) {
    const headerOffset = 80
    const top = el.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

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

/* ========== 顶部栏 ========== */
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
  transition: 0.3s;
  font-size: 0.9rem;
}
.back-btn:hover {
  background: rgba(144, 166, 196, 0.35);
  box-shadow: 0 0 15px rgba(144, 166, 196, 0.15);
}

/* ========== 内容布局 ========== */
.content-layout {
  display: flex;
  gap: 24px;
}

/* ========== 左侧目录栏 ========== */
.sidebar {
  display: none;
  width: 200px;
  flex-shrink: 0;
}
@media (min-width: 1024px) {
  .sidebar {
    display: block;
  }
}
.sidebar-inner {
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 12px 0;
}
.sidebar-section-title {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(168, 188, 212, 0.5);
  margin: 0 0 8px 8px;
}
.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.toc-link {
  display: block;
  font-size: 0.78rem;
  color: rgba(168, 188, 212, 0.55);
  text-decoration: none;
  padding: 3px 10px;
  border-radius: 6px;
  border-left: 2px solid transparent;
  transition: all 0.2s;
  line-height: 1.4;
}
.toc-link:hover {
  color: #c5d5ea;
  background: rgba(144, 166, 196, 0.06);
}
.toc-link.active {
  color: #c5d5ea;
  border-left-color: #7890b5;
  background: rgba(144, 166, 196, 0.08);
  font-weight: 500;
}
.toc-level-2 {
  padding-left: 20px;
}
.toc-level-3 {
  padding-left: 30px;
  font-size: 0.72rem;
}

/* ========== 主内容区 ========== */
.main-content {
  flex: 1;
  min-width: 0;
}

/* 面包屑 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 0;
  margin-bottom: 22px;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 6px 12px;
  border-radius: 8px;
  background: transparent;
  border: none;
  color: #a8bcd4;
  cursor: pointer;
  font-size: 0.88rem;
  transition: 0.2s;
}
.back-link i {
  font-size: 1.2rem;
  transition: transform 0.2s;
}
.back-link:hover {
  background: rgba(144, 166, 196, 0.1);
  color: #e8eef7;
}
.back-link:hover i {
  transform: translateX(-2px);
}
.header-separator {
  width: 1px;
  height: 16px;
  background: rgba(144, 166, 196, 0.25);
  margin: 0 8px;
  flex-shrink: 0;
}
.header-breadcrumb {
  display: flex;
  align-items: center;
  font-size: 0.88rem;
}
.breadcrumb-current {
  color: #e8eef7;
  font-weight: 500;
}

/* 帖子卡片 */
.detail-post {
  position: relative;
  border: 1px solid rgba(144, 166, 196, 0.22);
  border-radius: 22px;
  padding: 32px 36px;
  backdrop-filter: blur(12px);
  background: rgba(5, 12, 26, 0.3);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  overflow: hidden;
}
.detail-decor {
  position: absolute;
  top: 28px;
  bottom: 28px;
  left: 0;
  width: 3px;
  background: linear-gradient(180deg, transparent, rgba(168, 188, 212, 0.8) 20%, rgba(144, 166, 196, 0.6) 80%, transparent);
  border-radius: 0 3px 3px 0;
}

/* 标签行 */
.detail-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.post-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 0.72rem;
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
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 11px;
  border-radius: 10px;
  font-size: 0.72rem;
  background: rgba(144, 166, 196, 0.08);
  border: 1px solid rgba(144, 166, 196, 0.2);
  color: #a8bcd4;
}
.meta-chip i {
  font-size: 0.85rem;
  opacity: 0.8;
}

/* 标题 */
.detail-title {
  font-weight: 500;
  font-size: 1.75rem;
  color: #fff;
  letter-spacing: 2px;
  line-height: 1.35;
  margin-bottom: 10px;
  text-shadow: 0 2px 20px rgba(144, 166, 196, 0.18);
}
.title-underline {
  width: 56px;
  height: 2px;
  background: linear-gradient(90deg, #c5d5ea 0%, #a8bcd4 60%, transparent 100%);
  border-radius: 2px;
  margin-bottom: 22px;
}

/* ========== Markdown 内容 ========== */
.detail-content-wrapper {
  position: relative;
}
.md-body {
  line-height: 1.9;
  opacity: 0.92;
  font-size: 0.95rem;
  color: #e8eef7;
}
.md-body :deep(h1) {
  font-size: 1.6rem;
  font-weight: 500;
  color: #c5d5ea;
  margin: 1.5rem 0 0.8rem;
  scroll-margin-top: 80px;
}
.md-body :deep(h2) {
  font-size: 1.25rem;
  font-weight: 500;
  color: #c5d5ea;
  margin: 1.3rem 0 0.6rem;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid rgba(144, 166, 196, 0.2);
  scroll-margin-top: 80px;
}
.md-body :deep(h3) {
  font-size: 1.05rem;
  font-weight: 500;
  color: #a8bcd4;
  margin: 1.1rem 0 0.5rem;
  scroll-margin-top: 80px;
}
.md-body :deep(p) {
  margin-bottom: 12px;
}
.md-body :deep(code) {
  background: rgba(144, 166, 196, 0.12);
  padding: 2px 7px;
  border-radius: 5px;
  font-size: 0.85em;
  color: #e8eef7;
  border: 1px solid rgba(144, 166, 196, 0.18);
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}
.md-body :deep(pre) {
  background: rgba(5, 10, 22, 0.85);
  padding: 16px;
  border-radius: 10px;
  overflow-x: auto;
  border: 1px solid rgba(144, 166, 196, 0.2);
  margin: 14px 0;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.25);
}
.md-body :deep(pre code) {
  background: none;
  padding: 0;
  border: none;
}
.md-body :deep(blockquote) {
  border-left: 3px solid #c5d5ea;
  padding: 4px 16px;
  color: #c5d5ea;
  margin: 14px 0;
  background: rgba(168, 188, 212, 0.06);
  border-radius: 0 8px 8px 0;
}
.md-body :deep(ul),
.md-body :deep(ol) {
  padding-left: 22px;
}
.md-body :deep(a) {
  color: #c5d5ea;
}
.md-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}
.md-body :deep(th),
.md-body :deep(td) {
  border: 1px solid rgba(144, 166, 196, 0.2);
  padding: 6px 10px;
  text-align: left;
}
.md-body :deep(th) {
  background: rgba(144, 166, 196, 0.08);
}
.md-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}
.md-body :deep(hr) {
  border: none;
  border-top: 1px solid rgba(144, 166, 196, 0.2);
  margin: 1.2rem 0;
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
  height: 280px;
  background: linear-gradient(to bottom, rgba(5, 8, 20, 0), rgba(5, 8, 20, 0.7) 40%, rgba(5, 8, 20, 0.95));
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
  padding: 48px 0;
  margin-top: 12px;
}
.lock-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(168, 188, 212, 0.15), rgba(120, 144, 181, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  box-shadow: 0 4px 20px rgba(120, 144, 181, 0.15);
  color: #a8bcd4;
  font-size: 1.5rem;
  animation: pulseGlow 2s infinite;
}
@keyframes pulseGlow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(120, 144, 181, 0.3); }
  50% { box-shadow: 0 0 0 8px rgba(120, 144, 181, 0); }
}
.lock-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #e8eef7;
  margin: 0 0 6px;
}
.lock-desc {
  font-size: 0.85rem;
  color: rgba(168, 188, 212, 0.6);
  margin: 0 0 20px;
  text-align: center;
  max-width: 400px;
}
.unlock-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 32px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(120, 144, 181, 0.25);
  transition: all 0.3s;
}
.unlock-btn:hover {
  box-shadow: 0 6px 20px rgba(120, 144, 181, 0.4);
  transform: translateY(-1px);
}

</style>