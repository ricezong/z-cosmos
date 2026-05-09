<template>
  <div class="note-detail">
    <LoadingSpinner v-if="loading" />
    
    <template v-else-if="note">
      <!-- 笔记头部 -->
      <div class="note-header">
        <h1>{{ note.title }}</h1>
        <div class="note-meta">
          <span class="category">{{ note.category }}</span>
          <span class="views">{{ note.viewCount }} 人已读</span>
          <span class="time">{{ formatTime(note.updatedAt) }}</span>
        </div>
      </div>
      
      <!-- 封面图 -->
      <img v-if="note.coverImage" :src="note.coverImage" class="cover-image" />
      
      <!-- 预览内容（未解锁） -->
      <div v-if="!isUnlocked && note.isLocked" class="preview-content">
        <div v-html="previewContent"></div>
        <div class="blur-overlay">
          <div class="blur-hint">
            <p>🔒 剩余内容需要解锁后阅读</p>
            <button class="unlock-btn" @click="handleUnlock">
              阅读全文
            </button>
          </div>
        </div>
      </div>
      
      <!-- 完整内容（已解锁或无需解锁） -->
      <div v-else class="full-content" v-html="fullContent"></div>
      
      <!-- 标签 -->
      <div v-if="note.tags && note.tags.length" class="note-tags">
        <span v-for="tag in note.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </template>
    
    <!-- 解锁弹窗 -->
    <UnlockModal
      v-model:visible="showUnlockModal"
      :qr-code-url="qrCodeUrl"
      :unlock-code="unlockCode"
      :polling="isPolling"
      @close="stopPolling"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'
import { getNotePreview, getNoteDetail } from '../api/notes.js'
import UnlockModal from '../components/UnlockModal.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const route = useRoute()
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
let stopPollingFn = null

onMounted(async () => {
  const noteId = route.params.id
  await loadNote(noteId)
})

onUnmounted(() => {
  if (stopPollingFn) {
    stopPollingFn()
  }
})

async function loadNote(noteId) {
  loading.value = true
  try {
    // 检查全局解锁状态
    if (isLocallyUnlocked('NOTE')) {
      isUnlocked.value = true
      const noteData = await getNoteDetail(noteId, getDeviceId())
      note.value = noteData
      fullContent.value = noteData.content
    } else {
      const previewData = await getNotePreview(noteId)
      note.value = previewData.note
      previewContent.value = previewData.previewContent
      
      // 如果笔记不需要解锁
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
    
    // 启动轮询
    stopPollingFn = startPolling('NOTE', () => {
      isUnlocked.value = true
      isPolling.value = false
      showUnlockModal.value = false
      // 重新加载当前笔记（获取完整内容）
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
.note-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 100vh;
}

.note-header {
  margin-bottom: 30px;
}

.note-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 15px;
  line-height: 1.4;
}

.note-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
  align-items: center;
}

.category {
  background: #e6f7ff;
  color: #1890ff;
  padding: 4px 12px;
  border-radius: 4px;
}

.cover-image {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 30px;
}

.preview-content {
  position: relative;
  margin-bottom: 30px;
}

.full-content,
.preview-content {
  line-height: 1.8;
  color: #333;
  font-size: 16px;
}

.full-content :deep(h1),
.full-content :deep(h2),
.full-content :deep(h3) {
  margin: 30px 0 15px;
  color: #333;
}

.full-content :deep(p) {
  margin-bottom: 15px;
}

.full-content :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.full-content :deep(pre) {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
}

.blur-overlay {
  position: relative;
  margin-top: -100px;
  padding-top: 100px;
  background: linear-gradient(to bottom, transparent, white 50%);
}

.blur-hint {
  text-align: center;
  padding: 40px 20px;
}

.blur-hint p {
  color: #999;
  margin-bottom: 20px;
  font-size: 14px;
}

.unlock-btn {
  padding: 12px 40px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.unlock-btn:hover {
  background: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.tag {
  background: #f5f5f5;
  color: #666;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
