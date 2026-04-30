<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-sphere"></div>
          <div>
            <h1>技术社区</h1>
            <p>地球 · 技术交流中心</p>
          </div>
        </div>
        <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
      </header>
      <div class="container" v-if="view !== 'detail'">
        <div class="tabs">
          <button :class="{ active: view === 'list' }" @click="view = 'list'">浏览帖子</button>
          <button :class="{ active: view === 'write' }" @click="view = 'write'">发布帖子</button>
        </div>
        <div class="tabs small" v-if="view === 'list'">
          <button v-for="cat in categories" :key="cat.id" :class="{ active: category === cat.id }" @click="setCategory(cat.id)">{{ cat.name }}</button>
        </div>
      </div>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <main class="container">
        <section v-if="view === 'list'" class="stack">
          <article v-for="post in posts" :key="post.id" class="card post-card" @click="openPost(post.id)">
            <div class="meta">
              <span>{{ post.categoryName || post.category }}</span>
              <span>{{ formatTime(post.createdAt) }}</span>
            </div>
            <h2>{{ post.title }}</h2>
            <p>{{ post.summary }}</p>
            <div class="stats">
              <span><i class="ri-eye-line"></i> {{ post.viewCount }}</span>
              <span><i class="ri-chat-3-line"></i> {{ post.commentCount }}</span>
              <span><i class="ri-heart-3-line"></i> {{ post.likeCount }}</span>
            </div>
          </article>
          <div v-if="!posts.length" class="empty">{{ loading ? '正在加载...' : '暂无帖子' }}</div>
        </section>

        <section v-if="view === 'write'" class="card form">
          <label>分类</label>
          <select v-model="form.categoryCode">
            <option v-for="cat in writeCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
          <label>标题</label>
          <input v-model="form.title" maxlength="100" placeholder="写一个清晰的标题">
          <label>内容</label>
          <textarea v-model="form.content" rows="10" placeholder="支持 Markdown / HTML"></textarea>
          <button class="primary" @click="submitPost"><i class="ri-rocket-2-fill"></i> 发布</button>
        </section>

        <section v-if="view === 'detail' && currentPost" class="stack">
          <button class="back-link" @click="backToList"><i class="ri-arrow-left-line"></i> 返回列表</button>
          <article class="card detail">
            <div class="meta">
              <span>{{ currentPost.categoryName || currentPost.category }}</span>
              <span>{{ formatTime(currentPost.createdAt) }}</span>
              <span><i class="ri-eye-line"></i> {{ currentPost.viewCount }}</span>
            </div>
            <h2>{{ currentPost.title }}</h2>
            <div class="body" v-html="renderMarkdown(currentPost.content || currentPost.summary)"></div>
            <div class="actions">
              <button :class="{ active: currentPost.liked }" @click="toggleLike"><i class="ri-heart-fill"></i> {{ currentPost.likeCount || 0 }}</button>
              <button :class="{ active: currentPost.collected }" @click="toggleCollect"><i class="ri-star-fill"></i> {{ currentPost.collected ? '已收藏' : '收藏' }}</button>
            </div>
          </article>
          <section class="card comments">
            <h3>评论</h3>
            <div class="comment-box">
              <textarea v-model="commentText" rows="2" placeholder="写下你的评论"></textarea>
              <button @click="submitComment"><i class="ri-send-plane-2-fill"></i></button>
            </div>
            <div v-for="comment in comments" :key="comment.id" class="comment">
              <strong>{{ comment.nickname }}</strong>
              <span>{{ formatTime(comment.createdAt) }}</span>
              <p>{{ comment.content }}</p>
              <button @click="toggleCommentLike(comment)"><i class="ri-heart-fill"></i> {{ comment.likeCount || 0 }}</button>
            </div>
            <div v-if="!comments.length" class="empty">暂无评论</div>
          </section>
        </section>
      </main>
    </div>
    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { collectPost, createComment, createPost, getPost, likeComment, likePost, listCategories, listComments, listPosts, uncollectPost, unlikeComment, unlikePost } from '../api/community'

const route = useRoute()
const view = ref('list')
const category = ref('all')
const categories = ref([{ id: 'all', name: '全部' }])
const posts = ref([])
const currentPost = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(false)
const form = ref({ categoryCode: 'tech', title: '', content: '' })

const writeCategories = computed(() => categories.value.filter(c => c.id !== 'all'))

const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

function renderMarkdown(text) {
  if (!text) return ''
  try { return marked.parse(text) } catch { return text }
}

function formatTime(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function mapPost(post) {
  return {
    id: post.postId,
    title: post.title || '',
    summary: post.summary || '',
    content: post.content || '',
    category: post.category?.code || 'tech',
    categoryName: post.category?.name,
    viewCount: post.viewCount || 0,
    likeCount: post.likeCount || 0,
    commentCount: post.commentCount || 0,
    collected: Boolean(post.collected),
    liked: Boolean(post.liked),
    createdAt: post.createdAt
  }
}

function mapComment(comment) {
  return {
    id: comment.commentId,
    content: comment.content || '',
    nickname: comment.author?.nickname || '匿名用户',
    likeCount: comment.likeCount || 0,
    liked: Boolean(comment.liked),
    createdAt: comment.createdAt
  }
}

async function loadCategories() {
  const data = await listCategories().catch(() => [])
  if (data.length) {
    categories.value = [{ id: 'all', name: '全部' }, ...data.map(c => ({ id: c.code, name: c.name }))]
    form.value.categoryCode = writeCategories.value[0]?.id || 'tech'
  }
}

async function loadPosts() {
  loading.value = true
  try {
    const data = await listPosts({ categoryCode: category.value === 'all' ? undefined : category.value, page: 1, size: 50, sort: 'latest' })
    posts.value = (data.records || []).map(mapPost)
  } finally {
    loading.value = false
  }
}

async function setCategory(id) {
  category.value = id
  await loadPosts()
}

async function openPost(id) {
  const [detail, commentPage] = await Promise.all([
    getPost(id),
    listComments(id, { page: 1, size: 50 }).catch(() => ({ records: [] }))
  ])
  currentPost.value = mapPost(detail)
  comments.value = (commentPage.records || []).map(mapComment)
  view.value = 'detail'
  scrollToTop()
}

function backToList() {
  currentPost.value = null
  comments.value = []
  view.value = 'list'
}

async function submitPost() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    alert('请填写标题和内容')
    return
  }
  const data = await createPost({ ...form.value, title: form.value.title.trim() })
  form.value.title = ''
  form.value.content = ''
  await loadPosts()
  if (data?.postId) await openPost(data.postId)
}

async function toggleLike() {
  if (!currentPost.value) return
  if (currentPost.value.liked) await unlikePost(currentPost.value.id)
  else await likePost(currentPost.value.id)
  currentPost.value.liked = !currentPost.value.liked
  currentPost.value.likeCount += currentPost.value.liked ? 1 : -1
}

async function toggleCollect() {
  if (!currentPost.value) return
  if (currentPost.value.collected) await uncollectPost(currentPost.value.id)
  else await collectPost(currentPost.value.id)
  currentPost.value.collected = !currentPost.value.collected
}

async function submitComment() {
  const content = commentText.value.trim()
  if (!content || !currentPost.value) return
  await createComment(currentPost.value.id, { content })
  commentText.value = ''
  await openPost(currentPost.value.id)
}

async function toggleCommentLike(comment) {
  if (comment.liked) await unlikeComment(comment.id)
  else await likeComment(comment.id)
  comment.liked = !comment.liked
  comment.likeCount += comment.liked ? 1 : -1
}

onMounted(async () => {
  await loadCategories()
  await loadPosts()
  if (route.query.postId) await openPost(route.query.postId)
})
</script>

<style scoped>
.page-layout { --bt-bg: rgba(120,144,181,0.3); --bt-border: rgba(120,144,181,0.5); --bt-color: #c5d5ea; --bt-hover-bg: rgba(120,144,181,0.5); --bt-shadow: rgba(120,144,181,0.3); }
.container { position: relative; z-index: 1; max-width: 980px; margin: 0 auto; padding: 20px; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #fff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; background: radial-gradient(circle at 35% 35%, #8fc8ff, #4f8fca 55%, #1b3555); box-shadow: 0 0 12px rgba(120,144,181,0.5); }
.back-btn, button { border: 1px solid rgba(144,166,196,0.3); background: rgba(144,166,196,0.12); color: #c5d5ea; cursor: pointer; transition: 0.25s; }
.back-btn { padding: 8px 20px; border-radius: 30px; text-decoration: none; }
.tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.tabs.small button { font-size: 0.82rem; padding: 6px 12px; }
.tabs button, .back-link { padding: 8px 16px; border-radius: 24px; }
button.active, button:hover, .back-btn:hover { background: rgba(144,166,196,0.28); color: #fff; }
.stack { display: flex; flex-direction: column; gap: 12px; }
.card { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 20px; background: rgba(10,18,38,0.35); backdrop-filter: blur(10px); }
.post-card { cursor: pointer; transition: 0.25s; }
.post-card:hover { transform: translateY(-2px); border-color: rgba(144,166,196,0.5); }
.meta, .stats, .actions { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; color: #a8bcd4; font-size: 0.84rem; }
.post-card h2, .detail h2 { color: #fff; margin: 10px 0; }
.post-card p { opacity: 0.75; line-height: 1.6; }
.form { display: flex; flex-direction: column; gap: 10px; }
input, select, textarea { width: 100%; box-sizing: border-box; padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(144,166,196,0.25); background: rgba(5,12,26,0.72); color: #e8eef7; font-family: inherit; }
.primary { width: fit-content; padding: 10px 24px; border-radius: 24px; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: #fff; border: none; }
.detail .body { line-height: 1.9; margin: 18px 0; }
.actions button { border-radius: 24px; padding: 8px 16px; }
.comment-box { display: flex; gap: 10px; margin: 12px 0; }
.comment-box textarea { flex: 1; }
.comment-box button { width: 46px; border-radius: 50%; }
.comment { border-top: 1px solid rgba(144,166,196,0.16); padding: 12px 0; }
.comment strong { color: #c5d5ea; margin-right: 10px; }
.comment span { opacity: 0.55; font-size: 0.82rem; }
.comment p { margin: 8px 0; line-height: 1.6; }
.comment button { border-radius: 16px; padding: 4px 10px; }
.empty { text-align: center; padding: 60px 20px; opacity: 0.55; }
@media (max-width: 768px) { .container { padding: 15px 12px 60px; } .header { flex-direction: column; align-items: flex-start; gap: 12px; } .header h1 { font-size: 1.4rem; } }
</style>
