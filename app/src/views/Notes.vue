<template>
  <div class="notes-page">
    <!-- 分类筛选 -->
    <div class="category-filter">
      <button 
        :class="{ active: selectedCategory === '' }" 
        @click="selectCategory('')"
      >
        全部
      </button>
      <button 
        v-for="cat in categories" 
        :key="cat.categoryCode"
        :class="{ active: selectedCategory === cat.categoryCode }"
        @click="selectCategory(cat.categoryCode)"
      >
        {{ cat.categoryName }}
      </button>
    </div>

    <!-- 笔记列表 -->
    <div class="notes-list">
      <div 
        v-for="note in notes" 
        :key="note.noteId" 
        class="note-card"
        @click="goToDetail(note.noteId)"
      >
        <div class="note-header">
          <span class="note-type" :class="note.contentType === 0 ? 'original' : 'reprint'">
            {{ note.contentType === 0 ? '原创' : '转载' }}
          </span>
          <span class="category-tag">{{ note.categoryName }}</span>
        </div>
        <h3 class="note-title">{{ note.title }}</h3>
        <p class="note-summary">{{ note.shortSummary }}</p>
        <div class="note-footer">
          <div class="tags">
            <span v-for="tag in note.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>
          <div class="meta">
            <span>{{ note.readMinutes }}分钟阅读</span>
            <span v-if="note.isLocked === 1" class="locked">🔒 需解锁</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-if="!hasMore && notes.length > 0" class="no-more">没有更多了</div>

    <!-- 空状态 -->
    <div v-if="!loading && notes.length === 0" class="empty">
      <p>暂无笔记</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoteList, getCategories } from '../api/notes'

const router = useRouter()

const categories = ref([])
const notes = ref([])
const selectedCategory = ref('')
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const hasMore = ref(true)

// 加载分类
async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 0) {
      categories.value = res.data || []
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
    
    const res = await getNoteList(page.value, size.value, selectedCategory.value)
    if (res.code === 0 && res.data) {
      const newNotes = res.data.records || []
      notes.value = reset ? newNotes : [...notes.value, ...newNotes]
      hasMore.value = notes.value.length < res.data.total
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
  router.push(`/note/${noteId}`)
}

// 触底加载
function handleScroll() {
  const scrollTop = window.scrollY
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadNotes()
  }
}

onMounted(() => {
  loadCategories()
  loadNotes(true)
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.notes-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.category-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.category-filter button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.category-filter button.active {
  background: #4a90d9;
  color: white;
  border-color: #4a90d9;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.note-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.note-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.note-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.note-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.note-type.original {
  background: #e3f2fd;
  color: #1976d2;
}

.note-type.reprint {
  background: #f5f5f5;
  color: #666;
}

.category-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.note-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.note-summary {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
}

.meta .locked {
  color: #ff9800;
}

.loading, .no-more, .empty {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style>
