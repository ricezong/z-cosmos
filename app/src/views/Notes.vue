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
      <div class="container">
        <div class="list-toolbar">
          <div class="category-tabs" v-if="categories.length > 0">
            <div
                class="cat-tab"
                :class="{ active: selectedCategory === '' }"
                @click="selectedCategory = ''; loadNotes()"
            >
              全部
            </div>
            <div
                v-for="cat in categories"
                :key="cat.categoryCode"
                class="cat-tab"
                :class="{ active: selectedCategory === cat.categoryCode }"
                @click="selectedCategory = cat.categoryCode; loadNotes()"
            >
              {{ cat.categoryName }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <div class="container">
        <LoadingSpinner v-if="loading" text="加载中..." />

        <div class="notes-grid" v-else-if="notes.length > 0">
          <div
              v-for="note in notes"
              :key="note.noteId"
              class="note-card"
              @click="goToNote(note.noteId)"
          >
            <div class="note-cover-wrapper" v-if="note.coverImage">
              <img :src="note.coverImage" class="note-cover" alt="封面" />
            </div>
            <div class="note-content">
              <h3 class="note-title">{{ note.title }}</h3>
              <p class="note-summary">{{ note.shortSummary }}</p>
              <div class="note-meta">
                <span v-if="note.category" class="note-category-tag">{{ note.category }}</span>
                <span class="meta-chip">
                  <i class="ri-eye-line"></i> {{ note.viewCount || 0 }}
                </span>
                <span class="meta-chip">
                  <i class="ri-time-line"></i> {{ formatTime(note.updatedAt) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="empty-state" v-else-if="!loading">
          <p>暂无笔记</p>
        </div>

        <div class="pagination" v-if="total > pageSize">
          <button :disabled="currentPage === 1" @click="currentPage--; loadNotes()">
            <i class="ri-arrow-left-s-line"></i> 上一页
          </button>
          <span class="page-info">{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
          <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="currentPage++; loadNotes()">
            下一页 <i class="ri-arrow-right-s-line"></i>
          </button>
        </div>
      </div>
    </div>

    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop">
      <i class="ri-arrow-up-line"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotes, getNoteCategories } from '../api/notes.js'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()

const loading = ref(false)
const notes = ref([])
const categories = ref([])
const selectedCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 回到顶部
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
  await loadCategories()
  await loadNotes()
})

async function loadCategories() {
  try {
    const data = await getNoteCategories()
    categories.value = data || []
  } catch (error) {
    console.error('加载笔记类别失败:', error)
  }
}

async function loadNotes() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (selectedCategory.value) {
      params.category = selectedCategory.value
    }
    const pageData = await getNotes(params)
    notes.value = pageData.records || []
    total.value = pageData.total || 0
  } catch (error) {
    console.error('加载笔记列表失败:', error)
  } finally {
    loading.value = false
  }
}

function goToNote(noteId) {
  router.push({ name: 'NoteDetail', params: { id: noteId } })
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

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
  margin-bottom: 5px;
}
.category-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  overflow-x: auto;
}
.cat-tab {
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
.cat-tab.active,
.cat-tab:hover {
  background: rgba(144, 166, 196, 0.3);
  border-color: rgba(144, 166, 196, 0.5);
  color: #fff;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.note-card {
  border: 1px solid rgba(144, 166, 196, 0.2);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.3s;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
}
.note-card:hover {
  border-color: rgba(144, 166, 196, 0.5);
  box-shadow: 0 5px 25px rgba(144, 166, 196, 0.15);
  transform: translateY(-4px);
}

.note-cover-wrapper {
  width: 100%;
  height: 160px;
  overflow: hidden;
  border-bottom: 1px solid rgba(144, 166, 196, 0.2);
}
.note-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}
.note-card:hover .note-cover {
  transform: scale(1.04);
}

.note-content {
  padding: 20px;
}

.note-title {
  font-size: 1.15rem;
  color: #fff;
  font-weight: 500;
  margin: 0 0 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-summary {
  font-size: 0.9rem;
  color: #a8bcd4;
  opacity: 0.8;
  margin: 0 0 15px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 10px;
}

.note-category-tag {
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
  gap: 4px;
  font-size: 0.75rem;
  color: #a8bcd4;
  opacity: 0.7;
}
.meta-chip i {
  font-size: 0.85rem;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  opacity: 0.5;
  color: #a8bcd4;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 20px 0;
}
.pagination button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 18px;
  border-radius: 20px;
  background: rgba(144, 166, 196, 0.12);
  border: 1px solid rgba(144, 166, 196, 0.25);
  color: #c5d5ea;
  cursor: pointer;
  font-size: 0.82rem;
  transition: 0.2s;
}
.pagination button:hover:not(:disabled) {
  background: rgba(144, 166, 196, 0.25);
  border-color: rgba(144, 166, 196, 0.5);
}
.pagination button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.page-info {
  color: #a8bcd4;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}
</style>