<template>
  <div class="note-detail-page">
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

      <!-- 可滚动区域：三栏布局 -->
      <div class="page-scroll" ref="contentRef">
        <div class="detail-layout">
          <!-- 左侧目录侧边栏 -->
          <aside class="sidebar-left">
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

          <!-- 中央正文容器 -->
          <div class="container">
            <LoadingSpinner v-if="!note" text="加载中..." />

            <template v-else>
              <!-- 帖子卡片 -->
              <article class="detail-post">
                <span class="detail-decor"></span>

                <!-- 封面图 -->
                <img
                    v-if="note.coverImage"
                    :src="note.coverImage"
                    class="detail-cover"
                    alt="封面"
                />

                <!-- 标签行 -->
                <div class="detail-meta-row">
                  <span :class="['post-tag', note.contentType === 0 ? 'tag-original' : 'tag-reprint']">
                    {{ note.contentType === 0 ? '原创' : '转载' }}
                  </span>
                  <span v-if="note.categoryName" class="post-tag tag-category">{{ note.categoryName }}</span>
                  <span v-for="tag in (note.tags || [])" :key="tag" class="post-tag tag-default">{{ tag }}</span>
                  <span v-if="note.authorName" class="meta-chip">
                    <i class="ri-user-line"></i> {{ note.authorName }}
                  </span>
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

                <!-- Markdown 正文（带遮罩/解锁逻辑） -->
                <div class="detail-content-wrapper">
                  <!-- 已解锁或公开笔记 -->
                  <div
                      v-if="isUnlocked || note.isLocked !== 1"
                      class="md-body content-unlocked"
                      v-html="renderedContent"
                  />

                  <!-- 未解锁笔记 -->
                  <div v-else class="locked-content">
                    <div class="md-body" v-html="renderedPreview" />

                    <!-- 渐变遮罩（宽度完全覆盖卡片，消除左右间距） -->
                    <div class="content-mask">
                      <button class="unlock-btn" @click="showUnlockModal = true">
                        <i class="ri-lock-unlock-line"></i>
                        解锁全文
                      </button>
                    </div>
                  </div>
                </div>
              </article>
            </template>
          </div>

          <!-- 右侧占位栏 -->
          <aside class="sidebar-right"></aside>
        </div>
      </div>

      <!-- 解锁弹窗 -->
      <Teleport to="body">
        <div
            v-if="showUnlockModal"
            class="unlock-modal-overlay"
            @click.self="closeUnlockModal"
        >
          <div class="unlock-card">
            <button class="unlock-close" @click="closeUnlockModal">
              <i class="ri-close-line"></i>
            </button>
            <div class="unlock-icon">
              <i class="ri-lock-2-line"></i>
            </div>
            <h3 class="unlock-title">解锁全站文章</h3>
            <p class="unlock-desc">
              扫描下方二维码，关注公众号并输入 <strong>123456</strong> 解锁全站文章
            </p>
            <div class="qr-container">
              <img :src="qrCodeImage" alt="QR Code" />
            </div>
            <div class="unlock-steps">
              <div class="unlock-step">
                <span class="step-num">1</span>
                <span class="step-text">扫码关注公众号</span>
              </div>
              <div class="unlock-step">
                <span class="step-num">2</span>
                <span class="step-text">输入 <strong>123456</strong></span>
              </div>
            </div>
            <button class="unlock-done-btn" @click="confirmUnlock">
              <i class="ri-check-line"></i> 我已完成
            </button>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { getNoteDetail } from '../api/notes'
import LoadingSpinner from '../components/LoadingSpinner.vue'

// 组件 Props
const props = defineProps({
  qrCodeImage: {
    type: String,
    default: 'https://via.placeholder.com/160x160/ffffff/000000?text=QR+Code' // 默认占位图
  }
})

const route = useRoute()
const note = ref(null)
const isUnlocked = ref(false)
const showUnlockModal = ref(false)
const activeTocIndex = ref(0)
let scrollHandler = null

const formatViewCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0] || dateStr
}

const extractHeadings = (md) => {
  if (!md) return []
  const lines = md.split('\n')
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

const renderMarkdownWithAnchors = (markdown) => {
  if (!markdown) return ''
  let headingIndex = 0
  let currentSectionDepth = 0
  const sectionStack = []

  const m = marked.use({
    renderer: {
      heading({ text, depth }) {
        const id = `heading-${headingIndex++}`
        let closeTags = ''
        // 关闭比当前更深或同级的 section
        while (sectionStack.length > 0 && sectionStack[sectionStack.length - 1] >= depth) {
          closeTags += '</div>'
          sectionStack.pop()
        }
        // 开启新 section
        sectionStack.push(depth)
        return `${closeTags}<div class="md-section md-section-${depth}"><h${depth} id="${id}">${text}</h${depth}>\n`
      }
    }
  })
  let result = m.parse(markdown)
  // 关闭所有未关闭的 section
  while (sectionStack.length > 0) {
    result += '</div>'
    sectionStack.pop()
  }
  return result
}

const renderedContent = computed(() => {
  const content = note.value?.fullContent || note.value?.previewContent || ''
  return renderMarkdownWithAnchors(content)
})

const renderedPreview = computed(() => {
  const content = note.value?.previewContent || ''
  return renderMarkdownWithAnchors(content)
})

const scrollToHeading = (index) => {
  const el = document.getElementById(`heading-${index}`)
  if (el) {
    const headerOffset = 80
    const top = el.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

const setupScrollSpy = () => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
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

const loadNote = async () => {
  try {
    const data = await getNoteDetail(route.params.id)
    if (data) {
      note.value = data
      const unlockedIds = JSON.parse(localStorage.getItem('unlockedNotes') || '[]')
      isUnlocked.value = data.isLocked !== 1 || unlockedIds.includes(data.id)
      await nextTick()
      setupScrollSpy()
    }
  } catch (e) {
    console.error('加载笔记失败', e)
  }
}

const confirmUnlock = () => {
  if (note.value) {
    const unlockedIds = JSON.parse(localStorage.getItem('unlockedNotes') || '[]')
    if (!unlockedIds.includes(note.value.id)) {
      unlockedIds.push(note.value.id)
      localStorage.setItem('unlockedNotes', JSON.stringify(unlockedIds))
    }
    isUnlocked.value = true
    showUnlockModal.value = false
  }
}

const closeUnlockModal = () => {
  showUnlockModal.value = false
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

/* ========== 三栏布局 ========== */
.page-scroll {
  overflow-y: auto;
  height: calc(100vh - 80px);
}

.detail-layout {
  display: flex;
  justify-content: center;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 24px;
}

/* 左侧侧边栏 */
.sidebar-left {
  width: 220px;
  flex-shrink: 0;
  display: none;
}
@media (min-width: 1024px) {
  .sidebar-left {
    display: block;
  }
}
.sidebar-inner {
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  border-right: 1px solid rgba(144, 166, 196, 0.15);
  padding: 18px 16px;
  backdrop-filter: blur(8px);
}
.sidebar-section-title {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(168, 188, 212, 0.45);
  margin: 0 0 12px 8px;
}
.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.toc-link {
  display: block;
  font-size: 0.8rem;
  color: rgba(168, 188, 212, 0.6);
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 0 8px 8px 0;
  border-left: 3px solid transparent;
  transition: all 0.25s;
  line-height: 1.4;
}
.toc-link:hover {
  color: #e8eef7;
  background: rgba(144, 166, 196, 0.08);
  border-left-color: rgba(168, 188, 212, 0.3);
}
.toc-link.active {
  color: #ffffff;
  background: rgba(144, 166, 196, 0.15);
  border-left-color: #a8bcd4;
  font-weight: 500;
}
.toc-level-2 {
  padding-left: 24px;
  font-size: 0.76rem;
}
.toc-level-3 {
  padding-left: 36px;
  font-size: 0.73rem;
  color: rgba(168, 188, 212, 0.5);
}

/* 右侧占位栏 */
.sidebar-right {
  width: 220px;
  flex-shrink: 0;
  display: none;
}
@media (min-width: 1024px) {
  .sidebar-right {
    display: block;
  }
}

/* ========== 中央容器 ========== */
.container {
  flex: 1;
  min-width: 0;
  max-width: 960px;
  padding: 0;
}

/* 帖子卡片 */
.detail-post {
  position: relative;
  padding: 32px 36px;
  backdrop-filter: blur(12px);
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

/* 封面图 */
.detail-cover {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  border-radius: 12px;
  margin-bottom: 24px;
  border: 1px solid rgba(144, 166, 196, 0.15);
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
.md-body :deep(h1) { scroll-margin-top: 80px; }
.md-body :deep(h2) { scroll-margin-top: 80px; }
.md-body :deep(h3) { scroll-margin-top: 80px; }

/* 标题层级间距与样式 */
.md-body :deep(h1) {
  font-size: 1.5rem;
  margin: 2rem 0 1rem;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid rgba(144, 166, 196, 0.15);
  color: #fff;
}
.md-body :deep(h2) {
  font-size: 1.25rem;
  margin: 1.8rem 0 0.8rem;
  padding-bottom: 0.35rem;
  border-bottom: 1px solid rgba(144, 166, 196, 0.1);
  color: rgba(232, 238, 247, 0.95);
}
.md-body :deep(h3) {
  font-size: 1.1rem;
  margin: 1.5rem 0 0.6rem;
  color: rgba(232, 238, 247, 0.9);
}

/* 标题 section 层级缩进 — h2 内容左缩进 16px，h3 内容左缩进 28px */
.md-body :deep(.md-section-2) {
  padding-left: 16px;
  border-left: 2px solid rgba(144, 166, 196, 0.08);
  margin-left: 0;
}
.md-body :deep(.md-section-3) {
  padding-left: 28px;
  border-left: 2px solid rgba(144, 166, 196, 0.06);
  margin-left: 0;
}
.md-body :deep(.md-section-2 .md-section-3) {
  padding-left: 36px;
}

/* 列表样式 */
.md-body :deep(ul),
.md-body :deep(ol) {
  padding-left: 1.5em;
  margin: 0.6rem 0;
}
.md-body :deep(li) {
  margin: 0.3rem 0;
}

/* 代码块 */
.md-body :deep(pre) {
  background: rgba(10, 14, 28, 0.7);
  border: 1px solid rgba(144, 166, 196, 0.12);
  border-radius: 10px;
  padding: 16px 20px;
  margin: 1rem 0;
  overflow-x: auto;
  font-size: 0.85rem;
}
.md-body :deep(code) {
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
  font-size: 0.85em;
}
.md-body :deep(:not(pre) > code) {
  background: rgba(144, 166, 196, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  color: #c5d5ea;
}

/* 引用块 */
.md-body :deep(blockquote) {
  border-left: 3px solid rgba(168, 188, 212, 0.3);
  padding: 8px 16px;
  margin: 1rem 0;
  background: rgba(144, 166, 196, 0.04);
  border-radius: 0 8px 8px 0;
  color: rgba(168, 188, 212, 0.75);
}

/* 表格 */
.md-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
  font-size: 0.88rem;
}
.md-body :deep(th),
.md-body :deep(td) {
  border: 1px solid rgba(144, 166, 196, 0.15);
  padding: 8px 12px;
  text-align: left;
}
.md-body :deep(th) {
  background: rgba(144, 166, 196, 0.08);
  color: #c5d5ea;
  font-weight: 600;
}

/* 链接 */
.md-body :deep(a) {
  color: #78a0c5;
  text-decoration: underline;
  text-underline-offset: 3px;
}
.md-body :deep(a:hover) {
  color: #a8bcd4;
}

/* 水平线 */
.md-body :deep(hr) {
  border: none;
  border-top: 1px solid rgba(144, 166, 196, 0.15);
  margin: 2rem 0;
}

/* 图片 */
.md-body :deep(img) {
  max-width: 100%;
  border-radius: 10px;
  margin: 1rem 0;
  border: 1px solid rgba(144, 166, 196, 0.1);
}

/* 已解锁状态 */
.content-unlocked {
  position: relative;
}

/* 未解锁容器 */
.locked-content {
  position: relative;
}

/* ========== 渐变遮罩（与背景自然融合） ========== */
.content-mask {
  position: absolute;
  bottom: 0;
  left: -36px;
  right: -36px;
  height: 280px;
  background: linear-gradient(
      to bottom,
      transparent 0%,
      rgba(13, 15, 36, 0.15) 15%,
      rgba(13, 15, 36, 0.4) 30%,
      rgba(10, 12, 29, 0.65) 50%,
      rgba(10, 12, 29, 0.85) 70%,
      rgba(7, 8, 26, 0.96) 100%
  );
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding-bottom: 32px;
  pointer-events: none;
}

.content-mask .unlock-btn {
  pointer-events: auto;
}

.unlock-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 36px;
  border-radius: 14px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 18px rgba(120, 144, 181, 0.35);
  transition: all 0.3s;
}
.unlock-btn:hover {
  box-shadow: 0 6px 25px rgba(120, 144, 181, 0.5);
  transform: translateY(-1px);
}

/* ========== 解锁弹窗 ========== */
.unlock-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  overflow-y: auto;
  padding: 20px 0;
}
.unlock-card {
  background: rgba(15, 20, 35, 0.95);
  border: 1px solid rgba(144, 166, 196, 0.25);
  border-radius: 20px;
  padding: 36px 32px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
  position: relative;
  margin: auto;
}
.unlock-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(144, 166, 196, 0.1);
  border: 1px solid rgba(144, 166, 196, 0.2);
  color: #a8bcd4;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: 0.3s;
}
.unlock-close:hover {
  background: rgba(144, 166, 196, 0.25);
  color: #fff;
}
.unlock-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  font-size: 1.8rem;
  color: #fff;
}
.unlock-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 10px;
}
.unlock-desc {
  font-size: 0.88rem;
  color: rgba(168, 188, 212, 0.75);
  margin-bottom: 20px;
  line-height: 1.6;
}
.unlock-desc strong {
  color: #e8eef7;
  font-weight: 600;
  background: rgba(144, 166, 196, 0.12);
  padding: 1px 6px;
  border-radius: 4px;
}
.qr-container {
  background: white;
  padding: 14px;
  border-radius: 12px;
  display: inline-block;
  margin-bottom: 20px;
}
.qr-container img {
  width: 140px;
  height: 140px;
  display: block;
}

/* 步骤说明 */
.unlock-steps {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 22px;
  text-align: left;
}
.unlock-step {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(144, 166, 196, 0.04);
  border: 1px solid rgba(144, 166, 196, 0.08);
}
.step-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(120, 144, 181, 0.2);
  border: 1px solid rgba(120, 144, 181, 0.35);
  color: #c5d5ea;
  font-size: 0.72rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.step-text {
  font-size: 0.82rem;
  color: rgba(168, 188, 212, 0.8);
  line-height: 1.4;
}
.step-text strong {
  color: #e8eef7;
  font-weight: 600;
}

/* 完成按钮 */
.unlock-done-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.unlock-done-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 18px rgba(120, 144, 181, 0.35);
}
</style>