<template>
  <div class="star-bg"></div>
  <div class="container">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon">🌍</div>
        <div class="header-title">
          <h1>贴吧区</h1>
          <p>地球 · 社区交流中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn">← 返回星域</router-link>
    </header>

    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'browse' }" @click="activeTab = 'browse'">📋 浏览帖子</div>
      <div class="tab" :class="{ active: activeTab === 'post' }" @click="activeTab = 'post'">✏️ 发布帖子</div>
    </div>

    <div class="panel" v-show="activeTab === 'browse'">
      <div class="post-list">
        <div class="post-card" v-for="(post, idx) in sortedPosts" :key="post.id" @click="openPost(post.id)">
          <div class="post-header">
            <div class="post-author">
              <div class="avatar">{{ post.nickname.charAt(0).toUpperCase() }}</div>
              <span>{{ post.nickname }}</span>
            </div>
            <div class="post-meta">{{ formatTime(post.time) }}</div>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-content">{{ post.content.substring(0, 120) }}{{ post.content.length > 120 ? '...' : '' }}</div>
          <div class="post-stats">
            <span>👁 {{ post.views || 0 }}</span>
            <span>💬 {{ (post.comments || []).length }}</span>
          </div>
        </div>
      </div>
      <div class="empty-state" v-if="posts.length === 0">
        <p>暂无帖子，快来发布第一条吧！</p>
      </div>
    </div>

    <div class="panel" v-show="activeTab === 'post'">
      <div class="post-form">
        <h3>✨ 发布新帖</h3>
        <div class="form-group">
          <label>你的昵称</label>
          <input v-model="newPost.nickname" maxlength="20" placeholder="输入昵称...">
        </div>
        <div class="form-group">
          <label>帖子标题</label>
          <input v-model="newPost.title" maxlength="50" placeholder="输入标题...">
        </div>
        <div class="form-group">
          <label>帖子内容</label>
          <textarea v-model="newPost.content" placeholder="分享你的想法..."></textarea>
        </div>
        <button class="submit-btn" @click="submitPost">🚀 发布帖子</button>
      </div>
    </div>

    <div class="detail-view" v-show="detailId !== null">
      <div class="detail-header">
        <button class="back-link" @click="backToList">← 返回列表</button>
      </div>
      <div class="detail-post" v-if="currentPost">
        <div class="post-header" style="margin-bottom:15px">
          <div class="post-author">
            <div class="avatar">{{ currentPost.nickname.charAt(0).toUpperCase() }}</div>
            <div>
              <div style="font-weight:500">{{ currentPost.nickname }}</div>
              <div class="post-meta">{{ formatTime(currentPost.time) }} · 👁 {{ currentPost.views }}</div>
            </div>
          </div>
        </div>
        <h2>{{ currentPost.title }}</h2>
        <div class="detail-body" v-html="currentPost.content.replace(/\n/g, '<br>')"></div>
      </div>
      <div class="comments-section">
        <h3>💬 评论</h3>
        <div class="comment-list">
          <div class="comment-item" v-for="(c, i) in currentPost?.comments || []" :key="i">
            <div class="comment-header">
              <span class="comment-author">{{ c.nickname }}</span>
              <span class="comment-time">{{ formatTime(c.time) }}</span>
            </div>
            <div class="comment-body" v-html="c.body.replace(/\n/g, '<br>')"></div>
          </div>
          <div class="empty-state" v-if="!currentPost?.comments?.length">
            <p>暂无评论，来说两句吧~</p>
          </div>
        </div>
        <div class="comment-form">
          <h4>发表评论</h4>
          <div class="form-group">
            <input v-model="newComment.nickname" maxlength="20" placeholder="昵称...">
          </div>
          <div class="form-group">
            <textarea v-model="newComment.body" placeholder="写下你的评论..." rows="3"></textarea>
          </div>
          <button class="submit-btn" @click="submitComment">📨 发送评论</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const STORAGE_KEY = 'cosmos_tieba_posts'
const activeTab = ref('browse')
const detailId = ref(null)
const posts = ref([])

const newPost = ref({ nickname: '', title: '', content: '' })
const newComment = ref({ nickname: '', body: '' })

const sortedPosts = computed(() =>
  [...posts.value].sort((a, b) => new Date(b.time) - new Date(a.time))
)

const currentPost = computed(() =>
  posts.value.find(p => p.id === detailId.value)
)

function getPosts() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [] }
  catch { return [] }
}
function savePosts() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(posts.value))
}
function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2, 5)
}
function formatTime(dateStr) {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function submitPost() {
  const { nickname, title, content } = newPost.value
  if (!nickname.trim() || !title.trim() || !content.trim()) { alert('请填写完整信息'); return }
  posts.value.push({
    id: generateId(), nickname, title, content,
    time: new Date().toISOString(), views: 0, comments: []
  })
  savePosts()
  newPost.value = { nickname: '', title: '', content: '' }
  alert('发布成功！')
  activeTab.value = 'browse'
}

function openPost(id) {
  const post = posts.value.find(p => p.id === id)
  if (!post) return
  detailId.value = id
  post.views = (post.views || 0) + 1
  savePosts()
}

function backToList() {
  detailId.value = null
}

function submitComment() {
  const { nickname, body } = newComment.value
  if (!nickname.trim() || !body.trim()) { alert('请填写完整'); return }
  const post = posts.value.find(p => p.id === detailId.value)
  if (!post) return
  if (!post.comments) post.comments = []
  post.comments.push({ nickname, body, time: new Date().toISOString() })
  savePosts()
  newComment.value = { nickname: '', body: '' }
}

onMounted(() => {
  posts.value = getPosts()
  if (posts.value.length === 0) {
    posts.value = [
      {
        id: generateId(), nickname: '星际旅人', title: '欢迎来到地球贴吧区！',
        content: '这里是地球贴吧区，大家可以在这里自由发帖、交流讨论。\n\n支持功能：\n- 发布新帖子\n- 浏览所有帖子\n- 评论互动\n\n快来分享你的想法吧！',
        time: new Date(Date.now() - 86400000).toISOString(), views: 42, comments: [
          { nickname: '火星访客', body: '设计得很棒！蓝色主题很舒服。', time: new Date(Date.now() - 3600000).toISOString() }
        ]
      },
      {
        id: generateId(), nickname: '蓝色星球', title: '今天观测到了流星群',
        content: '今晚在星域北部观测到了大规模流星群，持续了约2小时。\n\n有人也看到了吗？',
        time: new Date(Date.now() - 172800000).toISOString(), views: 28, comments: []
      }
    ]
    savePosts()
  }
})
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; background: #050b1a; color: #e0f0ff; min-height: 100vh; }
.star-bg { position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 0;
  background-image:
    radial-gradient(2px 2px at 20px 30px, rgba(255,255,255,0.6), transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.5), transparent),
    radial-gradient(3px 3px at 90px 40px, rgba(255,255,255,0.7), transparent),
    radial-gradient(2px 2px at 160px 120px, rgba(255,255,255,0.5), transparent),
    radial-gradient(2px 2px at 230px 80px, rgba(255,255,255,0.6), transparent),
    radial-gradient(3px 3px at 300px 60px, rgba(255,255,255,0.7), transparent),
    radial-gradient(2px 2px at 350px 140px, rgba(255,255,255,0.5), transparent),
    radial-gradient(2px 2px at 450px 30px, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 520px 100px, rgba(255,255,255,0.4), transparent),
    radial-gradient(3px 3px at 620px 50px, rgba(255,255,255,0.8), transparent),
    radial-gradient(2px 2px at 700px 160px, rgba(255,255,255,0.5), transparent),
    radial-gradient(2px 2px at 780px 90px, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 850px 30px, rgba(255,255,255,0.4), transparent),
    radial-gradient(3px 3px at 920px 140px, rgba(255,255,255,0.7), transparent),
    radial-gradient(2px 2px at 50px 200px, rgba(255,255,255,0.5), transparent),
    radial-gradient(2px 2px at 150px 280px, rgba(255,255,255,0.6), transparent),
    radial-gradient(3px 3px at 280px 240px, rgba(255,255,255,0.8), transparent),
    radial-gradient(2px 2px at 400px 320px, rgba(255,255,255,0.5), transparent),
    radial-gradient(1px 1px at 500px 260px, rgba(255,255,255,0.4), transparent),
    radial-gradient(2px 2px at 650px 300px, rgba(255,255,255,0.6), transparent),
    radial-gradient(3px 3px at 750px 220px, rgba(255,255,255,0.7), transparent),
    radial-gradient(2px 2px at 880px 280px, rgba(255,255,255,0.5), transparent); }
.container { position: relative; z-index: 1; max-width: 900px; margin: 0 auto; padding: 30px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid rgba(90,163,240,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #3a7bd5, #5fa3f0); box-shadow: 0 0 20px rgba(90,163,240,0.4); display: flex; align-items: center; justify-content: center; font-size: 24px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #5fa3f0, #a8d0ff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(58,123,213,0.2); border: 1px solid rgba(90,163,240,0.4); color: #a8d0ff; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(58,123,213,0.4); box-shadow: 0 0 15px rgba(90,163,240,0.3); }
.tabs { display: flex; gap: 10px; margin-bottom: 25px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(58,123,213,0.15); border: 1px solid rgba(90,163,240,0.25); color: #a8d0ff; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(58,123,213,0.35); border-color: rgba(90,163,240,0.6); box-shadow: 0 0 15px rgba(90,163,240,0.2); color: #fff; }
.panel { display: block; }
.post-form { background: rgba(10,25,50,0.6); border: 1px solid rgba(90,163,240,0.25); border-radius: 20px; padding: 25px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.post-form h3 { font-weight: 400; margin-bottom: 15px; color: #5fa3f0; letter-spacing: 2px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 6px; font-size: 0.9rem; opacity: 0.8; }
.form-group input, .form-group textarea { width: 100%; padding: 12px 15px; border-radius: 12px; background: rgba(5,15,30,0.6); border: 1px solid rgba(90,163,240,0.3); color: #e0f0ff; font-family: inherit; font-size: 0.95rem; outline: none; transition: 0.3s; }
.form-group input:focus, .form-group textarea:focus { border-color: rgba(90,163,240,0.7); box-shadow: 0 0 10px rgba(90,163,240,0.15); }
.form-group textarea { min-height: 120px; resize: vertical; }
.submit-btn { padding: 12px 32px; border-radius: 30px; border: none; background: linear-gradient(135deg, #3a7bd5, #5fa3f0); color: white; cursor: pointer; font-size: 0.95rem; transition: 0.3s; letter-spacing: 1px; }
.submit-btn:hover { box-shadow: 0 0 20px rgba(90,163,240,0.4); transform: translateY(-1px); }
.post-list { display: flex; flex-direction: column; gap: 15px; }
.post-card { background: rgba(10,25,50,0.5); border: 1px solid rgba(90,163,240,0.2); border-radius: 16px; padding: 20px; backdrop-filter: blur(8px); cursor: pointer; transition: 0.3s; }
.post-card:hover { border-color: rgba(90,163,240,0.5); box-shadow: 0 5px 25px rgba(58,123,213,0.15); transform: translateY(-2px); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.post-author { display: flex; align-items: center; gap: 10px; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #3a7bd5, #5fa3f0); display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: bold; }
.post-meta { font-size: 0.85rem; opacity: 0.6; }
.post-title { font-size: 1.15rem; margin-bottom: 8px; color: #fff; }
.post-content { font-size: 0.95rem; opacity: 0.85; line-height: 1.6; }
.post-stats { display: flex; gap: 20px; margin-top: 12px; font-size: 0.85rem; opacity: 0.6; }
.detail-view { margin-top: 20px; }
.detail-header { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; }
.back-link { padding: 8px 18px; border-radius: 30px; background: rgba(58,123,213,0.2); border: 1px solid rgba(90,163,240,0.3); color: #a8d0ff; cursor: pointer; font-size: 0.9rem; transition: 0.3s; }
.back-link:hover { background: rgba(58,123,213,0.35); }
.detail-post { background: rgba(10,25,50,0.6); border: 1px solid rgba(90,163,240,0.25); border-radius: 20px; padding: 25px; margin-bottom: 25px; }
.detail-post h2 { font-weight: 400; margin-bottom: 15px; color: #fff; }
.detail-body { line-height: 1.8; opacity: 0.9; }
.comments-section h3 { font-weight: 400; margin-bottom: 15px; color: #5fa3f0; letter-spacing: 2px; }
.comment-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px; }
.comment-item { background: rgba(10,25,50,0.4); border: 1px solid rgba(90,163,240,0.15); border-radius: 14px; padding: 15px; }
.comment-header { display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 8px; }
.comment-author { color: #5fa3f0; font-weight: 500; }
.comment-time { opacity: 0.5; }
.comment-body { font-size: 0.95rem; opacity: 0.85; line-height: 1.5; }
.comment-form { background: rgba(10,25,50,0.5); border: 1px solid rgba(90,163,240,0.2); border-radius: 16px; padding: 20px; }
.comment-form h4 { font-weight: 400; margin-bottom: 12px; color: #a8d0ff; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

@media (max-width: 768px) {
  .container { padding: 15px 12px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-icon { width: 38px; height: 38px; font-size: 18px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .tabs { gap: 6px; flex-wrap: wrap; }
  .tab { padding: 8px 14px; font-size: 0.85rem; }
  .post-form { padding: 18px; }
  .post-card { padding: 15px; }
  .post-title { font-size: 1rem; }
  .post-header { flex-direction: column; align-items: flex-start; gap: 6px; }
  .submit-btn { width: 100%; }
  .detail-post { padding: 18px; }
  .comment-form { padding: 15px; }
}
</style>
