<template>
  <div class="notes-page">
    <div class="container">
      <h1 class="page-title">技术笔记</h1>
      
      <!-- 分类过滤 -->
      <div class="category-filter" v-if="categories.length > 0">
        <button 
          v-for="cat in categories" 
          :key="cat"
          :class="['category-btn', { active: selectedCategory === cat }]"
          @click="selectedCategory = cat; loadNotes()"
        >
          {{ cat }}
        </button>
      </div>
      
      <!-- 笔记列表 -->
      <div class="notes-grid">
        <LoadingSpinner v-if="loading" />
        <div 
          v-for="note in notes" 
          :key="note.noteId"
          class="note-card"
          @click="goToNote(note.noteId)"
        >
          <img v-if="note.coverImage" :src="note.coverImage" class="note-cover" />
          <div class="note-content">
            <h3 class="note-title">{{ note.title }}</h3>
            <p class="note-summary">{{ note.shortSummary }}</p>
            <div class="note-meta">
              <span class="category">{{ note.category }}</span>
              <span class="views">{{ note.viewCount }} 人已读</span>
              <span class="time">{{ formatTime(note.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && notes.length === 0" class="empty-state">
        <p>暂无笔记</p>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <button 
          :disabled="currentPage === 1" 
          @click="currentPage--; loadNotes()"
        >
          上一页
        </button>
        <span>{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button 
          :disabled="currentPage >= Math.ceil(total / pageSize)" 
          @click="currentPage++; loadNotes()"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotes } from '../api/notes.js'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()

const loading = ref(false)
const notes = ref([])
const categories = ref([])
const selectedCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(async () => {
  await loadNotes()
})

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
    
    // 提取分类
    if (categories.value.length === 0) {
      const categorySet = new Set(notes.value.map(n => n.category).filter(Boolean))
      categories.value = Array.from(categorySet)
    }
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
.notes-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.category-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

.category-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.category-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.category-btn.active {
  background: #1890ff;
  color: white;
  border-color: #1890ff;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.note-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.note-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.note-content {
  padding: 20px;
}

.note-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-summary {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.category {
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
}

.pagination button {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination button:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination span {
  color: #666;
}
</style>
