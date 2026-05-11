<template>
  <div class="note-detail-page" v-if="note">
    <!-- 笔记头部 -->
    <header class="note-header">
      <div class="meta-row">
        <span class="category">{{ note.categoryName }}</span>
        <span class="type" :class="note.contentType === 0 ? 'original' : 'reprint'">
          {{ note.contentType === 0 ? '原创' : '转载' }}
        </span>
        <span v-if="note.isLocked === 1 && !unlocked" class="lock-hint">🔒 需解锁</span>
      </div>
      <h1 class="title">{{ note.title }}</h1>
      <div class="tags">
        <span v-for="tag in note.tags" :key="tag" class="tag">#{{ tag }}</span>
      </div>
      <div class="info-row">
        <span>{{ note.readMinutes }}分钟阅读</span>
        <span>·</span>
        <span>{{ note.viewCount }}次阅读</span>
      </div>
    </header>

    <!-- 笔记内容 -->
    <article class="note-content" v-html="renderedContent"></article>

    <!-- 解锁提示遮罩 -->
    <div v-if="note.isLocked === 1 && !unlocked" class="unlock-overlay">
      <div class="unlock-modal">
        <h3>🔐 解锁阅读全文</h3>
        <p>发送口令到微信公众号，即可解锁全站内容</p>
        <button @click="showUnlockModal = true" class="unlock-btn">
          获取口令并解锁
        </button>
      </div>
    </div>

    <!-- 解锁弹窗 -->
    <UnlockModal 
      v-if="showUnlockModal" 
      @close="showUnlockModal = false"
      @unlocked="handleUnlocked"
    />

    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">← 返回</button>
  </div>

  <div v-else class="loading">加载中...</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { getNoteDetail } from '../api/notes'
import UnlockModal from '../components/UnlockModal.vue'
import { getDeviceId } from '../composables/useAuth'

const route = useRoute()
const note = ref(null)
const unlocked = ref(false)
const showUnlockModal = ref(false)

// 渲染 Markdown
const renderedContent = computed(() => {
  const content = unlocked.value && note.value?.fullContent 
    ? note.value.fullContent 
    : note.value?.previewContent || ''
  return marked(content)
})

// 加载笔记详情
async function loadNote() {
  try {
    const res = await getNoteDetail(route.params.noteId)
    if (res.code === 0 && res.data) {
      note.value = res.data
      unlocked.value = res.data.unlocked || false
    }
  } catch (e) {
    console.error('加载笔记失败', e)
  }
}

// 处理解锁成功
function handleUnlocked() {
  unlocked.value = true
  showUnlockModal.value = false
  // 重新加载以获取全文
  loadNote()
}

onMounted(() => {
  loadNote()
})
</script>

<style scoped>
.note-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
}

.note-header {
  margin-bottom: 40px;
}

.meta-row {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.category {
  padding: 4px 12px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 4px;
  font-size: 14px;
}

.type {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.type.original {
  background: #f0f0f0;
  color: #666;
}

.type.reprint {
  background: #f5f5f5;
  color: #666;
}

.lock-hint {
  color: #ff9800;
  font-size: 14px;
}

.title {
  font-size: 32px;
  margin: 0 0 20px 0;
  color: #333;
  line-height: 1.4;
}

.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.tag {
  color: #4a90d9;
  font-size: 14px;
}

.info-row {
  color: #999;
  font-size: 14px;
}

.note-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.note-content :deep(h1),
.note-content :deep(h2),
.note-content :deep(h3) {
  margin-top: 30px;
  margin-bottom: 15px;
}

.note-content :deep(p) {
  margin-bottom: 15px;
}

.note-content :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.note-content :deep(pre) {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
}

.unlock-overlay {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  top: 50%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.unlock-modal {
  background: white;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  max-width: 400px;
  width: 90%;
}

.unlock-modal h3 {
  margin: 0 0 15px 0;
  font-size: 20px;
}

.unlock-modal p {
  color: #666;
  margin-bottom: 20px;
}

.unlock-btn {
  background: #4a90d9;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.unlock-btn:hover {
  background: #3a7bc8;
}

.back-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: white;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.loading {
  text-align: center;
  padding: 50px;
  color: #999;
}
</style>
