<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere earth"></div></div>
        <div class="header-title">
          <h1>技术社区</h1>
          <p>地球 · 技术交流中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="list-toolbar">
      <div class="category-tabs">
        <div class="cat-tab" :class="{ active: currentCategory === '' }" @click="currentCategory = ''; loadPosts()">全部</div>
        <div class="cat-tab" v-for="cat in categories" :key="cat.code"
          :class="{ active: currentCategory === cat.code }" @click="currentCategory = cat.code; loadPosts()">
          {{ cat.name }}
        </div>
      </div>
      <button class="write-btn" @click="router.push('/community/new')">
        <i class="ri-edit-line"></i>
        <span>发帖</span>
      </button>
    </div>
    </div>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">

    <LoadingSpinner v-if="pageLoading" text="加载中..." />
    <LoadingSpinner v-else-if="categoryLoading" text="加载帖子中..." />
    <div class="post-list" v-else-if="posts.length > 0">
      <div class="post-card" v-for="post in posts" :key="post.postId" @click="openPost(post.postId)">
        <div class="post-header">
          <span class="post-category-tag">{{ post.category?.name || '未分类' }}</span>
          <div class="post-meta">{{ formatTime(post.createdAt) }}</div>
        </div>
        <div class="post-title">{{ post.title }}</div>
        <div class="post-summary">{{ post.summary || '' }}</div>
        <div class="post-stats">
          <span><i class="ri-eye-line"></i> {{ post.viewCount || 0 }}</span>
          <span><i class="ri-chat-3-line"></i> {{ post.commentCount || 0 }}</span>
          <span><i class="ri-heart-3-line"></i> {{ post.likeCount || 0 }}</span>
        </div>
      </div>
    </div>
    <div class="empty-state" v-else>
      <p>暂无{{ currentCategory ? '该分类' : '' }}帖子，快来发布第一条吧！</p>
      <button class="empty-write-btn" @click="router.push('/community/new')">
        <i class="ri-edit-line"></i> 写一帖
      </button>
    </div>

    </div>
    </div>
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as communityApi from '../api/community'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()

const posts = ref([])
const categories = ref([])
const currentCategory = ref('')
const currentPage = ref(1)
const pageLoading = ref(true)
const categoryLoading = ref(false)

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadCategories() {
  try {
    const data = await communityApi.listCategories()
    categories.value = data || []
  } catch (e) {
    console.error('加载分类失败:', e)
    categories.value = []
  }
}

async function loadPosts(page = 1) {
  try {
    const params = { page, size: 20 }
    if (currentCategory.value) params.categoryCode = currentCategory.value
    const data = await communityApi.listPosts(params)
    posts.value = data?.records || []
    currentPage.value = page
  } catch (e) {
    console.error('加载帖子失败:', e)
    posts.value = []
  }
}

function openPost(postId) {
  router.push(`/community/post/${postId}`)
}

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
  pageLoading.value = true
  await Promise.allSettled([loadCategories(), loadPosts()])
  pageLoading.value = false
})
</script>

<style scoped>
.container { position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.35); box-shadow: 0 0 15px rgba(144,166,196,0.15); }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

/* Toolbar with categories + write button */
.list-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  gap: 16px;
}
.category-tabs {
  display: flex; gap: 6px; flex-wrap: wrap; overflow-x: auto;
  flex: 1; min-width: 0;
}
.cat-tab { padding: 6px 14px; border-radius: 16px; cursor: pointer; background: rgba(144,166,196,0.08); border: 1px solid rgba(144,166,196,0.18); color: #a8bcd4; font-size: 0.82rem; white-space: nowrap; transition: 0.3s; }
.cat-tab.active, .cat-tab:hover { background: rgba(144,166,196,0.3); border-color: rgba(144,166,196,0.5); color: #fff; }

.write-btn {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 7px 18px; border-radius: 20px;
  border: 1px solid rgba(144,166,196,0.4);
  background: rgba(144,166,196,0.12);
  color: #c5d5ea; font-size: 0.85rem;
  cursor: pointer; transition: 0.2s;
  white-space: nowrap; flex-shrink: 0;
  font-family: inherit;
}
.write-btn i { font-size: 0.95rem; }
.write-btn:hover {
  background: linear-gradient(135deg, rgba(120,144,181,0.5), rgba(168,188,212,0.4));
  border-color: rgba(168,188,212,0.6);
  color: #fff;
  box-shadow: 0 2px 12px rgba(144,166,196,0.2);
}

.empty-write-btn {
  display: inline-flex; align-items: center; gap: 6px;
  margin-top: 16px; padding: 10px 28px; border-radius: 24px;
  border: none;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: #fff; font-size: 0.9rem; cursor: pointer;
  transition: 0.25s; font-family: inherit;
}
.empty-write-btn:hover {
  box-shadow: 0 4px 18px rgba(144,166,196,0.4);
  transform: translateY(-1px);
}

/* Post List */
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 20px; cursor: pointer; transition: 0.3s; }
.post-card:hover { border-color: rgba(144,166,196,0.5); box-shadow: 0 5px 25px rgba(144,166,196,0.15); transform: translateY(-2px); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.post-category-tag { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 0.75rem; background: rgba(144,166,196,0.15); color: #c5d5ea; border: 1px solid rgba(144,166,196,0.25); }
.post-meta { font-size: 0.85rem; opacity: 0.6; }
.post-title { font-size: 1.15rem; margin-bottom: 8px; color: #fff; font-weight: 500; }
.post-summary { font-size: 0.9rem; opacity: 0.8; line-height: 1.6; max-height: 80px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
.post-stats { display: flex; gap: 20px; margin-top: 12px; font-size: 0.85rem; opacity: 0.6; }
</style>