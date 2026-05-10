<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-icon">
            <div class="planet-sphere earth"></div>
          </div>
          <div class="header-title">
            <h1>技术笔记</h1>
            <p>地球 · 知识库</p>
          </div>
        </div>
        <router-link to="/" class="back-btn">
          <i class="ri-arrow-left-line"></i> 返回星域
        </router-link>
      </header>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <div class="container">
        <LoadingSpinner v-if="loading" text="加载中..." />

        <template v-else-if="note">
          <!-- 面包屑导航 -->
          <div class="detail-header">
            <button class="back-link" @click="router.push('/notes')">
              <i class="ri-arrow-left-s-line"></i>
              <span>笔记列表</span>
            </button>
            <div class="header-separator"></div>
            <div class="header-breadcrumb">
              <span class="breadcrumb-current">笔记详情</span>
            </div>
          </div>

          <!-- 笔记卡片容器 -->
          <article class="detail-post">
            <span class="detail-decor"></span>
            <!-- 元信息行：分类、阅读量、时间 -->
            <div class="detail-meta-row">
              <span v-if="note.category" class="post-category-tag">{{ note.category }}</span>
              <span class="meta-chip"><i class="ri-eye-line"></i> {{ note.viewCount || 0 }}</span>
              <span class="meta-chip"><i class="ri-time-line"></i> {{ formatTime(note.updatedAt) }}</span>
            </div>

            <h2 class="detail-title">{{ note.title }}</h2>
            <div class="title-underline"></div>

            <!-- 封面图 -->
            <div v-if="note.coverImage" class="detail-cover">
              <img :src="note.coverImage" alt="封面" class="cover-image" @click="previewImg = note.coverImage" />
            </div>

            <!-- 预览内容（未解锁） -->
            <div v-if="!isUnlocked && note.isLocked" class="preview-wrapper">
              <div class="detail-body markdown-body" v-html="previewContent"></div>
              <div class="blur-overlay">
                <div class="blur-hint">
                  <i class="ri-lock-line"></i>
                  <p>🔒 剩余内容需要解锁后阅读</p>
                  <button class="unlock-btn" @click="handleUnlock">
                    <i class="ri-key-2-line"></i> 阅读全文
                  </button>
                </div>
              </div>
            </div>

            <!-- 完整内容（已解锁或无需解锁） -->
            <div v-else class="detail-body markdown-body" v-html="fullContent"></div>

            <!-- 标签 -->
            <div v-if="note.tags && note.tags.length" class="note-tags">
              <span v-for="tag in note.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </article>
        </template>

        <div class="empty-state" v-else-if="!loading">
          <p>笔记不存在或已被删除</p>
          <router-link to="/notes" class="go-link">
            <i class="ri-arrow-left-line"></i> 返回笔记列表
          </router-link>
        </div>
      </div>
    </div>

    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop">
      <i class="ri-arrow-up-line"></i>
    </button>
  </div>

  <!-- 解锁弹窗（样式保持不变） -->
  <UnlockModal
      v-model:visible="showUnlockModal"
      :qr-code-url="qrCodeUrl"
      :unlock-code="unlockCode"
      :polling="isPolling"
      @close="stopPolling"
  />

  <!-- 封面图全屏预览 -->
  <Teleport to="body">
    <div class="img-preview-overlay" v-if="previewImg" @click="previewImg = null">
      <img :src="previewImg" class="img-preview-full" @click.stop />
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'
import { getNotePreview, getNoteDetail } from '../api/notes.js'
import UnlockModal from '../components/UnlockModal.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const { requestUnlock, startPolling, isLocallyUnlocked, getDeviceId } = useAuth()

const loading = ref(true)
const note = ref(null)
const isUnlocked = ref(false)
const previewContent = ref('')
const fullContent = ref('')
const showUnlockModal = ref(false)
const unlockCode = ref('')
const qrCodeUrl = ref('/wechat-qr.png')
const isPolling = ref(false)
const previewImg = ref(null)
let stopPollingFn = null

// 回到顶部相关
const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(async () => {
  const noteId = route.params.id
  await loadNote(noteId)
})

onUnmounted(() => {
  if (stopPollingFn) stopPollingFn()
})

async function loadNote(noteId) {
  loading.value = true
  try {
    if (isLocallyUnlocked('NOTE')) {
      isUnlocked.value = true
      const noteData = await getNoteDetail(noteId, getDeviceId())
      note.value = noteData
      fullContent.value = noteData.content
    } else {
      const previewData = await getNotePreview(noteId)
      note.value = previewData.note
      previewContent.value = previewData.previewContent
      if (!previewData.note.isLocked) {
        isUnlocked.value = true
        const noteData = await getNoteDetail(noteId, getDeviceId())
        fullContent.value = noteData.content
      }
    }
  } catch (error) {
    console.error('加载笔记失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleUnlock() {
  try {
    const result = await requestUnlock('NOTE')
    unlockCode.value = result.code
    showUnlockModal.value = true
    isPolling.value = true
    stopPollingFn = startPolling('NOTE', () => {
      isUnlocked.value = true
      isPolling.value = false
      showUnlockModal.value = false
      loadNote(route.params.id)
    })
  } catch (error) {
    console.error('请求解锁失败:', error)
  }
}

function stopPolling() {
  if (stopPollingFn) {
    stopPollingFn()
    isPolling.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>

<style scoped>
/* 复用帖子详情全局布局与组件样式 */
.container {
  position: relative;
  z-index: 1;
  max-width: 960px;
  margin: 0 auto;
  padding: 20px 20px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(144, 166, 196, 0.3);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.header-title h1 {
  font-size: 1.8rem;
  font-weight: 300;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #ffffff, #c5d5ea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header-title p {
  font-size: 0.85rem;
  opacity: 0.7;
  margin-top: 2px;
}
.back-btn {
  padding: 8px 20px;
  border-radius: 30px;
  background: rgba(144, 166, 196, 0.2);
  border: 1px solid rgba(144, 166, 196, 0.4);
  color: #c5d5ea;
  cursor: pointer;
  text-decoration: none;
  transition: 0.3s;
  font-size: 0.9rem;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.back-btn:hover {
  background: rgba(144, 166, 196, 0.35);
  box-shadow: 0 0 15px rgba(144, 166, 196, 0.15);
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
  letter-spacing: 0.3px;
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
  gap: 6px;
  font-size: 0.88rem;
}
.breadcrumb-current {
  color: #e8eef7;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* 主卡片 */
.detail-post {
  position: relative;
  border: 1px solid rgba(144, 166, 196, 0.22);
  border-radius: 22px;
  padding: 32px 36px;
  margin-bottom: 25px;
  backdrop-filter: blur(12px);
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
.detail-meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.post-category-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 0.75rem;
  background: rgba(144, 166, 196, 0.15);
  color: #c5d5ea;
  border: 1px solid rgba(144, 166, 196, 0.25);
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 11px;
  border-radius: 10px;
  font-size: 0.75rem;
  background: rgba(144, 166, 196, 0.08);
  border: 1px solid rgba(144, 166, 196, 0.2);
  color: #a8bcd4;
}
.meta-chip i {
  font-size: 0.85rem;
  opacity: 0.8;
}
.detail-title {
  font-family: var(--font-display, serif);
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

/* 封面图 */
.detail-cover {
  margin-bottom: 22px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(144, 166, 196, 0.2);
}
.cover-image {
  width: 100%;
  display: block;
  cursor: pointer;
  transition: transform 0.3s;
}
.cover-image:hover {
  transform: scale(1.02);
}

/* 内容区域 */
.detail-body {
  line-height: 1.95;
  opacity: 0.92;
  font-size: 0.98rem;
  color: #e8eef7;
}
.detail-body :deep(p) {
  margin-bottom: 14px;
}
.detail-body :deep(h1),
.detail-body :deep(h2),
.detail-body :deep(h3) {
  color: #c5d5ea;
  margin: 20px 0 10px;
  font-weight: 500;
  letter-spacing: 1px;
}
.detail-body :deep(code) {
  background: rgba(144, 166, 196, 0.14);
  padding: 2px 7px;
  border-radius: 5px;
  font-size: 0.88em;
  color: #e8eef7;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  border: 1px solid rgba(144, 166, 196, 0.18);
}
.detail-body :deep(pre) {
  background: rgba(5, 10, 22, 0.85);
  padding: 16px;
  border-radius: 10px;
  overflow-x: auto;
  border: 1px solid rgba(144, 166, 196, 0.2);
  margin: 14px 0;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.25);
}
.detail-body :deep(pre code) {
  background: none;
  padding: 0;
  border: none;
}
.detail-body :deep(blockquote) {
  border-left: 3px solid #c5d5ea;
  padding: 4px 16px;
  color: #c5d5ea;
  margin: 14px 0;
  background: rgba(168, 188, 212, 0.06);
  border-radius: 0 8px 8px 0;
}
.detail-body :deep(ul),
.detail-body :deep(ol) {
  padding-left: 22px;
}
.detail-body :deep(a) {
  color: #c5d5ea;
}

/* 解锁覆盖层 */
.preview-wrapper {
  position: relative;
}
.blur-overlay {
  position: relative;
  margin-top: -60px;
  padding-top: 60px;
}
.blur-hint {
  text-align: center;
  padding: 50px 20px;
  background: linear-gradient(to bottom, rgba(10, 20, 40, 0.7), rgba(10, 20, 40, 0.95));
  border-radius: 16px;
  margin: 0 -8px;
  border: 1px solid rgba(144, 166, 196, 0.2);
  backdrop-filter: blur(8px);
}
.blur-hint i {
  font-size: 2rem;
  color: #c5d5ea;
  margin-bottom: 12px;
  display: block;
}
.blur-hint p {
  color: #a8bcd4;
  margin-bottom: 20px;
  font-size: 1rem;
}
.unlock-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 28px;
  border-radius: 24px;
  border: none;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: #fff;
  font-size: 0.95rem;
  cursor: pointer;
  transition: 0.25s;
  font-family: inherit;
}
.unlock-btn:hover {
  box-shadow: 0 4px 18px rgba(144, 166, 196, 0.4);
  transform: translateY(-1px);
}

/* 标签 */
.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed rgba(144, 166, 196, 0.2);
}
.tag {
  background: rgba(144, 166, 196, 0.1);
  color: #a8bcd4;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.78rem;
  border: 1px solid rgba(144, 166, 196, 0.2);
  transition: 0.2s;
}
.tag:hover {
  background: rgba(144, 166, 196, 0.2);
  border-color: rgba(144, 166, 196, 0.4);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  opacity: 0.5;
  color: #a8bcd4;
}
.go-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #c5d5ea;
  text-decoration: none;
  margin-top: 12px;
  transition: 0.2s;
}
.go-link:hover {
  color: #e8eef7;
}

/* 全屏图片预览 */
.img-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.img-preview-full {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}
</style>