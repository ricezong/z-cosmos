<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
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
    <div class="container" :class="{ 'no-padding': activeTab === 'detail' }">
    <div class="tabs" v-show="activeTab !== 'detail'">
      <div class="tab" :class="{ active: activeTab === 'browse' }" @click="activeTab = 'browse'"><i class="ri-file-list-3-line"></i> 浏览帖子</div>
      <div class="tab" :class="{ active: activeTab === 'post' }" @click="activeTab = 'post'"><i class="ri-edit-line"></i> 发布帖子</div>
    </div>

    <!-- 技术分类子Tab（详情页隐藏） -->
    <div class="category-tabs" v-show="activeTab === 'browse'">
      <div class="cat-tab" v-for="cat in categories" :key="cat.id"
        :class="{ active: currentCategory === cat.id }" @click="currentCategory = cat.id">
        {{ cat.name }}
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">

    <!-- 浏览帖子 -->
    <div class="panel" v-show="activeTab === 'browse'">
      <div class="post-list">
        <div class="post-card" v-for="(post, idx) in filteredPosts" :key="post.id" @click="openPost(post.id)">
          <div class="post-header">
            <span class="post-category-tag" :class="post.category">{{ categoryLabel(post.category) }}</span>
            <div class="post-meta">{{ formatTime(post.time) }}</div>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-content" v-html="renderMarkdown(post.content.substring(0, 200))"></div>
          <div class="post-stats">
            <span><i class="ri-eye-line"></i> {{ post.views || 0 }}</span>
            <span><i class="ri-chat-3-line"></i> {{ countAllComments(post.comments) }}</span>
            <span><i class="ri-heart-3-line"></i> {{ post.likes || 0 }}</span>
          </div>
        </div>
      </div>
      <div class="empty-state" v-if="filteredPosts.length === 0">
        <p>暂无{{ currentCategory !== 'all' ? categoryLabel(currentCategory) + '相关' : '' }}帖子，快来发布第一条吧！</p>
      </div>
    </div>

    <!-- 发布帖子 -->
    <div class="panel" v-show="activeTab === 'post'">
      <div class="post-form">
        <div class="form-head">
          <div class="form-head-title">
            <span class="hex-mark">◈</span>
            <h3>发布新帖</h3>
          </div>
          <span class="form-head-sub">分享见解 · 支持 Markdown 排版</span>
        </div>
        <div class="form-row">
          <div class="form-group half">
            <label><i class="ri-price-tag-3-line"></i> 分类</label>
            <select v-model="newPost.category" class="form-select">
              <option v-for="cat in techCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="form-group grow">
            <label><i class="ri-edit-2-line"></i> 帖子标题</label>
            <input v-model="newPost.title" maxlength="100" placeholder="一句话说清主题...">
          </div>
        </div>
        <div class="form-group">
          <label><i class="ri-quill-pen-line"></i> 帖子内容 <span class="md-hint">Markdown</span></label>
          <div class="editor-wrap">
            <div class="editor-col">
              <div class="editor-tag"><i class="ri-code-s-slash-line"></i> Markdown</div>
              <textarea v-model="newPost.content" placeholder="分享你的技术见解...&#10;&#10;支持 Markdown 语法：&#10;# 标题&#10;**粗体** *斜体*&#10;- 列表&#10;`代码`&#10;```代码块```"></textarea>
            </div>
            <div class="editor-col preview-col" v-if="newPost.content">
              <div class="editor-tag preview-tag"><i class="ri-eye-line"></i> 实时预览</div>
              <div class="preview-content" v-html="renderMarkdown(newPost.content)"></div>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label><i class="ri-gallery-upload-line"></i> 上传图片</label>
          <div class="upload-area" @click="triggerUpload" @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onDrop" :class="{ dragover: isDragOver }">
            <div class="upload-hint" v-if="newPostImages.length === 0">
              <i class="ri-image-add-line"></i>
              <span>点击或拖拽图片到此处上传</span>
              <span class="upload-sub">JPG / PNG / GIF · 单张不超过 5MB</span>
            </div>
            <div class="upload-preview" v-else>
              <div class="preview-thumb" v-for="(img, i) in newPostImages" :key="i">
                <img :src="img" alt="预览">
                <button class="remove-img" @click.stop="removeImage(i)">×</button>
              </div>
              <div class="add-more" @click.stop="triggerUpload">
                <i class="ri-add-line"></i>
              </div>
            </div>
          </div>
          <input type="file" ref="fileInputRef" accept="image/*" multiple @change="handleFileSelect" style="display:none">
        </div>
        <div class="form-footer">
          <span class="form-tip"><i class="ri-information-line"></i> 发布后可在「我的帖子」中查看</span>
          <button class="submit-btn" @click="submitPost"><i class="ri-rocket-2-fill"></i> 发布帖子</button>
        </div>
      </div>
    </div>

    <!-- 帖子详情 -->
    <div class="detail-view" v-if="activeTab === 'detail' && currentPost">
      <div class="detail-header">
        <button class="back-link" @click="backToList"><i class="ri-arrow-left-line"></i><span>返回列表</span></button>
      </div>
      <article class="detail-post">
        <span class="detail-decor"></span>
        <div class="detail-meta-row">
          <span class="post-category-tag" :class="currentPost.category">{{ categoryLabel(currentPost.category) }}</span>
          <span class="meta-chip"><i class="ri-time-line"></i> {{ formatTime(currentPost.time) }}</span>
          <span class="meta-chip"><i class="ri-eye-line"></i> {{ currentPost.views }}</span>
        </div>
        <h2 class="detail-title">{{ currentPost.title }}</h2>
        <div class="title-underline"></div>
        <div class="detail-body markdown-body" v-html="renderMarkdown(currentPost.content)"></div>
        <div class="detail-images" v-if="currentPost.images && currentPost.images.length">
          <div class="detail-img-grid" :class="{ single: currentPost.images.length === 1 }">
            <img v-for="(img, i) in currentPost.images" :key="i" :src="img" alt="帖子图片" class="detail-img" @click="previewImage(img)">
          </div>
        </div>
        <div class="detail-actions">
          <button class="action-btn like-btn" :class="{ liked: currentPost.liked }" @click="togglePostLike">
            <i class="ri-heart-fill"></i>
            <span class="act-label">点赞</span>
            <span class="act-count">{{ currentPost.likes || 0 }}</span>
          </button>
          <button class="action-btn fav-btn" :class="{ starred: isFavorite }" @click="toggleFavorite">
            <i :class="isFavorite ? 'ri-star-fill' : 'ri-star-line'"></i>
            <span class="act-label">{{ isFavorite ? '已收藏' : '收藏' }}</span>
          </button>
          <button class="action-btn cm-btn" @click="scrollToComment">
            <i class="ri-chat-3-line"></i>
            <span class="act-label">评论</span>
          </button>
        </div>
      </article>

      <!-- 评论区 -->
      <div class="comments-section" ref="commentsRef">
        <div class="comments-head">
          <h3><i class="ri-chat-smile-2-line"></i> 评论</h3>
          <span class="comments-count">{{ countAllComments(currentPost.comments) }}</span>
        </div>
        <div class="comment-input-box">
          <textarea v-model="newComment.body" placeholder="写下你的评论... (Ctrl+Enter 发送)" rows="2" @keydown.enter.ctrl="submitTopComment"></textarea>
          <button class="comment-send-btn" @click="submitTopComment" title="发送 (Ctrl+Enter)">
            <i class="ri-send-plane-2-fill"></i>
          </button>
        </div>
        <div class="comment-list" v-if="currentPost.comments && currentPost.comments.length">
          <div class="comment-group" v-for="comment in currentPost.comments" :key="comment.id">
            <div class="comment-item parent-comment">
              <div class="c-avatar">{{ (comment.nickname || 'U').slice(0, 1).toUpperCase() }}</div>
              <div class="c-main">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.nickname }}</span>
                  <span class="comment-time">{{ formatTime(comment.time) }}</span>
                </div>
                <div class="comment-body" v-html="comment.body.replace(/\n/g, '<br>')"></div>
                <div class="comment-footer">
                  <span class="comment-like" :class="{ liked: comment.liked }" @click="toggleCommentLike(comment.id)"><i class="ri-heart-fill"></i> {{ comment.likes || 0 }}</span>
                  <span class="comment-reply-btn" @click="startReply(comment)"><i class="ri-reply-line"></i> 回复</span>
                </div>
              </div>
            </div>
            <!-- 子评论平铺在同一框内 -->
            <div class="child-comments" v-if="comment.replies && comment.replies.length">
              <div class="comment-item child-comment" v-for="reply in flattenReplies(comment.replies)" :key="reply.id">
                <div class="c-avatar small">{{ (reply.nickname || 'U').slice(0, 1).toUpperCase() }}</div>
                <div class="c-main">
                  <div class="comment-header">
                    <span class="comment-author">{{ reply.nickname }}</span>
                    <span v-if="reply.replyTo" class="reply-to-tag">回复 @{{ reply.replyTo }}</span>
                    <span class="comment-time">{{ formatTime(reply.time) }}</span>
                  </div>
                  <div class="comment-body" v-html="reply.body.replace(/\n/g, '<br>')"></div>
                  <div class="comment-footer">
                    <span class="comment-like" :class="{ liked: reply.liked }" @click="toggleCommentLike(reply.id)"><i class="ri-heart-fill"></i> {{ reply.likes || 0 }}</span>
                    <span class="comment-reply-btn" @click="startReply(reply)"><i class="ri-reply-line"></i> 回复</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>✦ 暂无评论，来说两句吧</p>
        </div>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->

  <!-- 回复弹窗 -->
  <Teleport to="body">
    <div class="reply-modal-overlay" v-if="replyTarget" @click.self="cancelReply">
      <div class="reply-modal">
        <div class="reply-to">回复 @{{ replyTarget.nickname }}</div>
        <textarea v-model="replyText" placeholder="写下你的回复..." rows="3" ref="replyInputRef"></textarea>
        <div class="reply-modal-btns">
          <button class="submit-btn small" @click="doSubmitReply">发送</button>
          <button class="cancel-btn" @click="cancelReply">取消</button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- 图片预览弹窗 -->
  <Teleport to="body">
    <div class="img-preview-overlay" v-if="previewImg" @click="previewImg = null">
      <img :src="previewImg" class="img-preview-full" @click.stop>
    </div>
  </Teleport>

</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'

const route = useRoute()

const STORAGE_KEY = 'cosmos_community_posts'
const PROFILE_KEY = 'cosmos_profile'
const activeTab = ref('browse')
const detailId = ref(null)
const posts = ref([])

// 当前用户身份：从 profile 读取稳定 id（不存在则生成）
function getCurrentUser() {
  let profile = {}
  try { profile = JSON.parse(localStorage.getItem(PROFILE_KEY)) || {} } catch { /* ignore */ }
  if (!profile.id) {
    profile.id = 'u_' + Date.now().toString(36) + Math.random().toString(36).substring(2, 6)
    profile.nickname = profile.nickname || '星际旅人'
    profile.favorites = profile.favorites || []
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile))
  }
  return profile
}
const currentUser = ref(getCurrentUser())

// 分类
const categories = [
  { id: 'all', name: '全部' },
  { id: 'frontend', name: '前端' },
  { id: 'backend', name: '后端' },
  { id: 'mobile', name: '移动端' },
  { id: 'ai', name: '人工智能' },
  { id: 'cloud', name: '云计算' },
  { id: 'database', name: '数据库' },
  { id: 'devops', name: '运维' },
  { id: 'security', name: '安全' },
  { id: 'testing', name: '测试' },
  { id: 'product', name: '产品' },
]
const techCategories = categories.filter(c => c.id !== 'all')
const currentCategory = ref('all')

const newPost = ref({ category: 'frontend', title: '', content: '' })
const newPostImages = ref([])
const newComment = ref({ body: '' })
const commentsRef = ref(null)
const fileInputRef = ref(null)
const isDragOver = ref(false)
const previewImg = ref(null)

// 回复相关
const replyTarget = ref(null)
const replyText = ref('')
const replyInputRef = ref(null)

// 将嵌套回复平铺为列表
function flattenReplies(replies, parentNickname = '') {
  const result = []
  for (const r of replies) {
    result.push({ ...r, replyTo: parentNickname || undefined })
    if (r.replies && r.replies.length) {
      result.push(...flattenReplies(r.replies, r.nickname))
    }
  }
  return result
}

function categoryLabel(id) {
  const cat = categories.find(c => c.id === id)
  return cat ? cat.name : id
}

const filteredPosts = computed(() => {
  let list = [...posts.value].sort((a, b) => new Date(b.time) - new Date(a.time))
  if (currentCategory.value !== 'all') {
    list = list.filter(p => p.category === currentCategory.value)
  }
  return list
})

const currentPost = computed(() =>
  posts.value.find(p => p.id === detailId.value)
)

marked.setOptions({ breaks: true, gfm: true })
function renderMarkdown(text) {
  if (!text) return ''
  try { return marked.parse(text) }
  catch (e) { return text.replace(/\n/g, '<br>') }
}

function countAllComments(comments) {
  if (!comments || !comments.length) return 0
  let count = 0
  function countRec(arr) {
    arr.forEach(c => {
      count++
      if (c.replies && c.replies.length) countRec(c.replies)
    })
  }
  countRec(comments)
  return count
}

function getPosts() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [] }
  catch { return [] }
}
function savePosts() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(posts.value))
}
function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substring(2, 7)
}
function formatTime(dateStr) {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function submitPost() {
  const { category, title, content } = newPost.value
  if (!title.trim() || !content.trim()) { alert('请填写标题和内容'); return }
  // 图片总量校验，避免 localStorage 溢出
  const totalSize = newPostImages.value.reduce((s, url) => s + url.length, 0)
  if (totalSize > 8 * 1024 * 1024) { alert('图片总量过大，请减少或压缩后再发布'); return }
  posts.value.push({
    id: generateId(), category, title, content,
    authorId: currentUser.value.id,
    authorNickname: currentUser.value.nickname,
    time: new Date().toISOString(), views: 0, likes: 0, liked: false,
    comments: [],
    images: [...newPostImages.value]
  })
  savePosts()
  newPost.value = { category: 'frontend', title: '', content: '' }
  newPostImages.value = []
  alert('发布成功！')
  activeTab.value = 'browse'
}

function triggerUpload() {
  fileInputRef.value?.click()
}

function handleFileSelect(e) {
  const files = e.target.files
  if (files) processFiles(files)
  e.target.value = ''
}

function onDragOver() { isDragOver.value = true }
function onDragLeave() { isDragOver.value = false }
function onDrop(e) {
  isDragOver.value = false
  const files = e.dataTransfer.files
  if (files) processFiles(files)
}

function processFiles(files) {
  Array.from(files).forEach(file => {
    if (!file.type.startsWith('image/')) return
    if (file.size > 5 * 1024 * 1024) { alert(`${file.name} 超过 5MB 限制`); return }
    const reader = new FileReader()
    reader.onload = (e) => {
      newPostImages.value.push(e.target.result)
    }
    reader.readAsDataURL(file)
  })
}

function removeImage(index) {
  newPostImages.value.splice(index, 1)
}

function previewImage(src) {
  previewImg.value = src
}

// Sticky header & back to top
const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

function openPost(id) {
  const post = posts.value.find(p => p.id === id)
  if (!post) return
  detailId.value = id
  post.views = (post.views || 0) + 1
  savePosts()
  activeTab.value = 'detail'
  nextTick(() => { if (contentRef.value) contentRef.value.scrollTo({ top: 0, behavior: 'smooth' }) })
}

function backToList() {
  detailId.value = null
  activeTab.value = 'browse'
}

function togglePostLike() {
  const post = currentPost.value
  if (!post) return
  post.liked = !post.liked
  post.likes = (post.likes || 0) + (post.liked ? 1 : -1)
  savePosts()
}

// 收藏相关
const isFavorite = computed(() => {
  if (!currentPost.value) return false
  return (currentUser.value.favorites || []).includes(currentPost.value.id)
})
function toggleFavorite() {
  const post = currentPost.value
  if (!post) return
  const favs = currentUser.value.favorites || []
  const idx = favs.indexOf(post.id)
  if (idx >= 0) favs.splice(idx, 1)
  else favs.push(post.id)
  currentUser.value.favorites = favs
  localStorage.setItem(PROFILE_KEY, JSON.stringify(currentUser.value))
}

function scrollToComment() {
  commentsRef.value?.scrollIntoView({ behavior: 'smooth' })
}

// 顶级评论
function submitTopComment() {
  const body = newComment.value.body.trim()
  if (!body) return
  const post = currentPost.value
  if (!post) return
  if (!post.comments) post.comments = []
  post.comments.push({
    id: generateId(), nickname: '匿名用户', body, time: new Date().toISOString(),
    likes: 0, liked: false, replies: []
  })
  savePosts()
  newComment.value.body = ''
}

// 回复
function startReply(comment) {
  replyTarget.value = comment
  replyText.value = ''
  nextTick(() => replyInputRef.value?.focus())
}
function cancelReply() {
  replyTarget.value = null
  replyText.value = ''
}
function doSubmitReply() {
  const body = replyText.value.trim()
  if (!body || !replyTarget.value) return
  const post = currentPost.value
  if (!post) return

  function addReply(comments, targetId) {
    for (const c of comments) {
      if (c.id === targetId) {
        if (!c.replies) c.replies = []
        c.replies.push({
          id: generateId(), nickname: '匿名用户', body, time: new Date().toISOString(),
          likes: 0, liked: false, replies: []
        })
        return true
      }
      if (c.replies && c.replies.length) {
        if (addReply(c.replies, targetId)) return true
      }
    }
    return false
  }
  addReply(post.comments, replyTarget.value.id)
  savePosts()
  cancelReply()
}

function toggleCommentLike(commentId) {
  const post = currentPost.value
  if (!post) return
  function toggle(comments) {
    for (const c of comments) {
      if (c.id === commentId) {
        c.liked = !c.liked
        c.likes = (c.likes || 0) + (c.liked ? 1 : -1)
        return true
      }
      if (c.replies && c.replies.length) {
        if (toggle(c.replies)) return true
      }
    }
    return false
  }
  toggle(post.comments)
  savePosts()
}

function openPostFromRoute() {
  const pid = route.query.postId
  if (pid && posts.value.find(p => p.id === pid)) {
    openPost(pid)
  }
}

onMounted(() => {
  posts.value = getPosts()
  if (posts.value.length === 0) {
    posts.value = [
      {
        id: generateId(), category: 'frontend',
        title: '欢迎来到技术社区！',
        content: '这里是技术社区，大家可以在这里自由交流技术话题。\n\n## 支持的功能\n\n- 发布技术帖子\n- Markdown 格式编辑\n- 分类浏览\n- 评论互动\n- 点赞功能\n\n> 快来分享你的技术见解吧！',
        time: new Date(Date.now() - 86400000).toISOString(), views: 42, likes: 5, liked: false,
        comments: [
          { id: generateId(), nickname: '前端爱好者', body: '社区设计得很棒！期待更多技术讨论。', time: new Date(Date.now() - 3600000).toISOString(), likes: 3, liked: false, replies: [] }
        ]
      },
      {
        id: generateId(), category: 'ai',
        title: 'DeepSeek 模型使用体验分享',
        content: '最近在使用 DeepSeek 模型进行开发，体验非常好。\n\n## 优点\n\n1. **响应速度快** - 推理速度令人满意\n2. **中文理解强** - 对中文语境理解深刻\n3. **成本低** - 相比其他模型有价格优势\n\n## 使用场景\n\n- 代码生成与补全\n- 文档撰写\n- 智能问答\n\n大家有什么使用心得？欢迎交流！',
        time: new Date(Date.now() - 172800000).toISOString(), views: 28, likes: 8, liked: false,
        comments: []
      },
      {
        id: generateId(), category: 'backend',
        title: 'Spring Boot 3.4 新特性一览',
        content: 'Spring Boot 3.4 带来了许多令人兴奋的新特性。\n\n### 主要更新\n\n- **虚拟线程支持** - 正式支持 Java 21 虚拟线程\n- **改进的 AOT 编译** - 更快的启动时间\n- **新的 Actuator 端点** - 更好的可观测性\n\n```java\n@RestController\npublic class HelloController {\n    @GetMapping("/hello")\n    public String hello() {\n        return "Hello, COSMOS!";\n    }\n}\n```\n\n大家升级了吗？',
        time: new Date(Date.now() - 259200000).toISOString(), views: 56, likes: 12, liked: false,
        comments: [
          { id: generateId(), nickname: 'Java开发者', body: '虚拟线程确实好用，性能提升明显！', time: new Date(Date.now() - 7200000).toISOString(), likes: 2, liked: false, replies: [
            { id: generateId(), nickname: '后端老司机', body: '同意，我们项目已经在用了', time: new Date(Date.now() - 3600000).toISOString(), likes: 1, liked: false, replies: [] }
          ]}
        ]
      }
    ]
    savePosts()
  }
  openPostFromRoute()
})
</script>

<style scoped>
/* Theme vars for back-to-top */
.page-layout {
  --bt-bg: rgba(144,166,196,0.3);
  --bt-border: rgba(144,166,196,0.5);
  --bt-color: #c9d9ef;
  --bt-hover-bg: rgba(144,166,196,0.5);
  --bt-shadow: rgba(144,166,196,0.3);
}
.container { position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.35); box-shadow: 0 0 15px rgba(144,166,196,0.15); }
.tabs { display: flex; gap: 10px; margin-bottom: 20px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(144,166,196,0.12); border: 1px solid rgba(144,166,196,0.25); color: #a8bcd4; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(144,166,196,0.3); border-color: rgba(144,166,196,0.5); color: #fff; }
.container.no-padding { padding: 0; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.earth { background: radial-gradient(circle at 35% 35%, #a8bcd4, #7890b5 50%, #243d68 100%); box-shadow: 0 0 12px rgba(90,135,200,0.5), inset 0 0 8px rgba(255,255,255,0.15); }
.planet-sphere.earth::after { content: ''; position: absolute; inset: 10% 25% 40% 20%; background: rgba(255,255,255,0.12); border-radius: 50%; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

/* Category Tabs */
.category-tabs { display: flex; gap: 6px; flex-wrap: wrap; overflow-x: auto; }
.cat-tab { padding: 6px 14px; border-radius: 16px; cursor: pointer; background: rgba(144,166,196,0.08); border: 1px solid rgba(144,166,196,0.18); color: #a8bcd4; font-size: 0.82rem; white-space: nowrap; transition: 0.3s; }
.cat-tab.active, .cat-tab:hover { background: rgba(144,166,196,0.3); border-color: rgba(144,166,196,0.5); color: #fff; }

.panel { display: block; }

/* ===== Post form ===== */
.post-form {
  position: relative;
  border: 1px solid rgba(144,166,196,0.22);
  border-radius: 22px;
  padding: 28px 30px 26px;
  margin-bottom: 25px;
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 50px rgba(0,0,0,0.35), inset 0 1px 0 rgba(255,255,255,0.05);
  overflow: hidden;
}
.post-form::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent, rgba(144,166,196,0.7), rgba(168,188,212,0.6), rgba(144,166,196,0.7), transparent);
  opacity: 0.9;
}
.form-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 22px; padding-bottom: 16px;
  border-bottom: 1px dashed rgba(144,166,196,0.2);
  flex-wrap: wrap; gap: 8px;
}
.form-head-title { display: flex; align-items: center; gap: 12px; }
.hex-mark { color: #c5d5ea; font-size: 1.1rem; text-shadow: 0 0 12px rgba(168,188,212,0.6); }
.form-head h3 { font-weight: 500; font-size: 1.1rem; letter-spacing: 4px; color: #e8eef7; margin: 0; font-family: var(--font-display, serif); }
.form-head-sub { font-size: 0.75rem; opacity: 0.55; letter-spacing: 1.5px; color: #a8bcd4; text-transform: uppercase; }

.form-row { display: flex; gap: 14px; margin-bottom: 16px; flex-wrap: wrap; }
.form-group { margin-bottom: 16px; }
.form-group.half { flex: 0 0 200px; margin-bottom: 0; }
.form-group.grow { flex: 1; min-width: 220px; margin-bottom: 0; }
.form-group label {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 8px; font-size: 0.85rem;
  opacity: 0.9; color: #c5d5ea; letter-spacing: 0.5px;
}
.form-group label i { color: #a8bcd4; font-size: 1rem; }
.md-hint {
  font-size: 0.68rem; opacity: 0.65; margin-left: auto;
  letter-spacing: 1.5px; padding: 2px 8px; border-radius: 8px;
  background: rgba(144,166,196,0.12); border: 1px solid rgba(144,166,196,0.22);
  color: #a8bcd4; text-transform: uppercase;
}
.form-group input, .form-group textarea {
  width: 100%; padding: 12px 16px; border-radius: 12px;
  background: rgba(5,12,26,0.7);
  border: 1px solid rgba(144,166,196,0.25);
  color: #e8eef7; font-family: inherit; font-size: 0.95rem;
  outline: none; transition: border-color 0.25s, box-shadow 0.25s;
}
.form-group textarea { resize: vertical; line-height: 1.7; }
.form-group input:focus, .form-group textarea:focus {
  border-color: rgba(144,166,196,0.6);
  box-shadow: 0 0 0 3px rgba(144,166,196,0.12);
}
/* Custom select with SVG chevron */
.form-select {
  width: 100%; padding: 12px 40px 12px 16px; border-radius: 12px;
  background-color: rgba(5,12,26,0.7);
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'><path fill='%23a8bcd4' d='M6 8 0 0h12z'/></svg>");
  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 10px;
  border: 1px solid rgba(144,166,196,0.25);
  color: #e8eef7; font-family: inherit; font-size: 0.95rem;
  outline: none; cursor: pointer; appearance: none;
  transition: 0.25s;
}
.form-select:focus {
  border-color: rgba(144,166,196,0.6);
  box-shadow: 0 0 0 3px rgba(144,166,196,0.12);
}
.form-select option { background: #0f1428; color: #e8eef7; padding: 8px; }

/* ===== Editor ===== */
.editor-wrap { display: flex; gap: 14px; align-items: stretch; }
.editor-col { flex: 1; position: relative; display: flex; flex-direction: column; min-width: 0; }
.editor-col textarea {
  flex: 1; min-height: 260px;
  padding: 38px 16px 14px;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 0.88rem; line-height: 1.75;
}
.editor-tag {
  position: absolute; top: 10px; left: 12px;
  font-size: 0.65rem; letter-spacing: 2px; text-transform: uppercase;
  padding: 4px 10px; border-radius: 6px;
  background: rgba(144,166,196,0.18);
  color: #c5d5ea; border: 1px solid rgba(144,166,196,0.28);
  display: inline-flex; align-items: center; gap: 5px;
  z-index: 2;
}
.editor-tag i { font-size: 0.85rem; }
.preview-tag { background: rgba(168,188,212,0.16); color: #c5d5ea; border-color: rgba(168,188,212,0.3); }
.preview-col {
  background: rgba(5,12,26,0.6);
  border: 1px solid rgba(144,166,196,0.2);
  border-radius: 12px;
  padding: 38px 16px 14px;
  overflow-y: auto;
  max-height: 400px;
  min-height: 260px;
}
.preview-content { font-size: 0.9rem; line-height: 1.75; }
.preview-content :deep(h1), .preview-content :deep(h2), .preview-content :deep(h3) { color: #c5d5ea; margin: 10px 0; font-weight: 500; }
.preview-content :deep(code) { background: rgba(144,166,196,0.15); padding: 2px 6px; border-radius: 4px; font-size: 0.85em; font-family: 'JetBrains Mono', 'Consolas', monospace; }
.preview-content :deep(pre) { background: rgba(5,10,20,0.8); padding: 12px; border-radius: 8px; overflow-x: auto; border: 1px solid rgba(144,166,196,0.2); }
.preview-content :deep(pre code) { background: none; padding: 0; }
.preview-content :deep(blockquote) { border-left: 3px solid #c5d5ea; padding-left: 12px; color: #c5d5ea; margin: 10px 0; }

/* ===== Upload ===== */
.upload-area {
  border: 2px dashed rgba(144,166,196,0.28);
  border-radius: 16px; padding: 28px;
  text-align: center; cursor: pointer; transition: 0.3s;
  min-height: 120px;
  display: flex; align-items: center; justify-content: center;
  background: radial-gradient(ellipse at center, rgba(144,166,196,0.05) 0%, transparent 70%);
}
.upload-area:hover, .upload-area.dragover {
  border-color: rgba(144,166,196,0.6);
  background: rgba(144,166,196,0.08);
  transform: translateY(-1px);
}
.upload-hint { display: flex; flex-direction: column; align-items: center; gap: 6px; color: rgba(180,210,240,0.6); }
.upload-hint i { font-size: 2.4rem; color: rgba(180,210,240,0.55); }
.upload-hint span { font-size: 0.88rem; color: #c5d5ea; }
.upload-sub { font-size: 0.72rem !important; opacity: 0.55; letter-spacing: 1px; }
.upload-preview { display: flex; flex-wrap: wrap; gap: 10px; width: 100%; }
.preview-thumb { position: relative; width: 84px; height: 84px; border-radius: 12px; overflow: hidden; border: 1px solid rgba(144,166,196,0.3); }
.preview-thumb img { width: 100%; height: 100%; object-fit: cover; }
.remove-img { position: absolute; top: 3px; right: 3px; width: 20px; height: 20px; border-radius: 50%; background: rgba(255,80,80,0.85); color: white; border: none; cursor: pointer; font-size: 0.85rem; display: flex; align-items: center; justify-content: center; line-height: 1; }
.add-more { width: 84px; height: 84px; border-radius: 12px; border: 2px dashed rgba(144,166,196,0.3); display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 1.6rem; color: rgba(180,210,240,0.55); transition: 0.3s; }
.add-more:hover { border-color: rgba(144,166,196,0.6); color: #c5d5ea; background: rgba(144,166,196,0.06); }

/* Form footer */
.form-footer {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 22px; padding-top: 18px;
  border-top: 1px dashed rgba(144,166,196,0.18);
  flex-wrap: wrap; gap: 12px;
}
.form-tip {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 0.78rem; opacity: 0.55; color: #c5d5ea;
}

/* Detail Images */
.detail-images { margin-top: 15px; }
.detail-img-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.detail-img-grid.single { grid-template-columns: 1fr; max-width: 400px; }
.detail-img { width: 100%; border-radius: 10px; cursor: pointer; transition: 0.3s; border: 1px solid rgba(144,166,196,0.2); }
.detail-img:hover { transform: scale(1.02); box-shadow: 0 4px 15px rgba(0,0,0,0.3); }
.img-preview-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 300; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.img-preview-full { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 8px; }

.submit-btn {
  padding: 12px 34px; border-radius: 30px; border: none;
  background: linear-gradient(135deg, #7890b5 0%, #a8bcd4 100%);
  color: white; cursor: pointer; font-size: 0.92rem;
  letter-spacing: 2px; font-weight: 500;
  box-shadow: 0 6px 20px rgba(144,166,196,0.35), inset 0 1px 0 rgba(255,255,255,0.2);
  transition: transform 0.25s, box-shadow 0.25s;
  display: inline-flex; align-items: center; gap: 8px;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(144,166,196,0.5), inset 0 1px 0 rgba(255,255,255,0.3);
}
.submit-btn.small { padding: 8px 20px; font-size: 0.85rem; letter-spacing: 1px; }

/* Post list */
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 20px; cursor: pointer; transition: 0.3s; }
.post-card:hover { border-color: rgba(144,166,196,0.5); box-shadow: 0 5px 25px rgba(144,166,196,0.15); transform: translateY(-2px); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.post-category-tag { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 0.75rem; background: rgba(144,166,196,0.15); color: #c5d5ea; border: 1px solid rgba(144,166,196,0.25); }
.post-category-tag.frontend { background: rgba(58,180,120,0.2); color: #7ee8a0; border-color: rgba(58,180,120,0.3); }
.post-category-tag.backend { background: rgba(58,123,213,0.2); color: #88bbff; border-color: rgba(58,123,213,0.3); }
.post-category-tag.mobile { background: rgba(180,120,58,0.2); color: #f0c080; border-color: rgba(180,120,58,0.3); }
.post-category-tag.ai { background: rgba(138,109,184,0.2); color: #c8b5e8; border-color: rgba(138,109,184,0.3); }
.post-category-tag.cloud { background: rgba(106,176,214,0.2); color: #a0d8f0; border-color: rgba(106,176,214,0.3); }
.post-category-tag.database { background: rgba(212,140,80,0.2); color: #f0c090; border-color: rgba(212,140,80,0.3); }
.post-category-tag.devops { background: rgba(192,92,62,0.2); color: #ffbba0; border-color: rgba(192,92,62,0.3); }
.post-category-tag.security { background: rgba(180,60,60,0.2); color: #ff9999; border-color: rgba(180,60,60,0.3); }
.post-category-tag.testing { background: rgba(60,160,100,0.2); color: #99ffbb; border-color: rgba(60,160,100,0.3); }
.post-category-tag.product { background: rgba(180,140,60,0.2); color: #f0d080; border-color: rgba(180,140,60,0.3); }
.post-meta { font-size: 0.85rem; opacity: 0.6; }
.post-title { font-size: 1.15rem; margin-bottom: 8px; color: #fff; font-weight: 500; }
.post-content { font-size: 0.9rem; opacity: 0.8; line-height: 1.6; max-height: 80px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
.post-content :deep(h1), .post-content :deep(h2), .post-content :deep(h3) { font-size: 1em; margin: 2px 0; }
.post-content :deep(code) { background: rgba(144,166,196,0.12); padding: 1px 4px; border-radius: 3px; font-size: 0.85em; }
.post-stats { display: flex; gap: 20px; margin-top: 12px; font-size: 0.85rem; opacity: 0.6; }

/* ===== Detail ===== */
.detail-view { margin-top: 20px; }
.detail-header { display: flex; align-items: center; gap: 15px; margin-bottom: 22px; }
.back-link {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 22px; border-radius: 30px;
  background: rgba(144,166,196,0.16);
  border: 1px solid rgba(144,166,196,0.3);
  color: #c5d5ea; cursor: pointer;
  font-size: 0.88rem; transition: 0.3s; letter-spacing: 0.5px;
}
.back-link i { transition: transform 0.3s; }
.back-link:hover {
  background: rgba(144,166,196,0.28);
  border-color: rgba(144,166,196,0.5);
  padding-left: 18px; padding-right: 26px;
}
.back-link:hover i { transform: translateX(-3px); }

.detail-post {
  position: relative;
  border: 1px solid rgba(144,166,196,0.22);
  border-radius: 22px;
  padding: 32px 36px;
  margin-bottom: 25px;
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 50px rgba(0,0,0,0.35), inset 0 1px 0 rgba(255,255,255,0.05);
  overflow: hidden;
}
.detail-decor {
  position: absolute; top: 28px; bottom: 28px; left: 0; width: 3px;
  background: linear-gradient(180deg, transparent, rgba(168,188,212,0.8) 20%, rgba(144,166,196,0.6) 80%, transparent);
  border-radius: 0 3px 3px 0;
}
.detail-meta-row { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.meta-chip {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 4px 11px; border-radius: 10px; font-size: 0.75rem;
  background: rgba(144,166,196,0.08);
  border: 1px solid rgba(144,166,196,0.2);
  color: #a8bcd4;
}
.meta-chip i { font-size: 0.85rem; opacity: 0.8; }
.detail-title {
  font-family: var(--font-display, serif);
  font-weight: 500; font-size: 1.75rem; color: #fff;
  letter-spacing: 2px; line-height: 1.35;
  margin-bottom: 10px;
  text-shadow: 0 2px 20px rgba(144,166,196,0.18);
}
.title-underline {
  width: 56px; height: 2px;
  background: linear-gradient(90deg, #c5d5ea 0%, #a8bcd4 60%, transparent 100%);
  border-radius: 2px; margin-bottom: 22px;
}
.detail-body { line-height: 1.95; opacity: 0.92; font-size: 0.98rem; color: #e8eef7; }
.detail-body :deep(p) { margin-bottom: 14px; }
.detail-body :deep(h1), .detail-body :deep(h2), .detail-body :deep(h3) { color: #c5d5ea; margin: 20px 0 10px; font-weight: 500; letter-spacing: 1px; }
.detail-body :deep(code) { background: rgba(144,166,196,0.14); padding: 2px 7px; border-radius: 5px; font-size: 0.88em; color: #e8eef7; font-family: 'JetBrains Mono', 'Consolas', monospace; border: 1px solid rgba(144,166,196,0.18); }
.detail-body :deep(pre) { background: rgba(5,10,22,0.85); padding: 16px; border-radius: 10px; overflow-x: auto; border: 1px solid rgba(144,166,196,0.2); margin: 14px 0; box-shadow: inset 0 2px 8px rgba(0,0,0,0.25); }
.detail-body :deep(pre code) { background: none; padding: 0; border: none; }
.detail-body :deep(blockquote) { border-left: 3px solid #c5d5ea; padding: 4px 16px; color: #c5d5ea; margin: 14px 0; background: rgba(168,188,212,0.06); border-radius: 0 8px 8px 0; }
.detail-body :deep(ul), .detail-body :deep(ol) { padding-left: 22px; }
.detail-body :deep(a) { color: #c5d5ea; }

.detail-actions { display: flex; gap: 10px; margin-top: 26px; padding-top: 20px; border-top: 1px dashed rgba(144,166,196,0.2); flex-wrap: wrap; }
.action-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 9px 18px; border-radius: 24px;
  border: 1px solid rgba(144,166,196,0.28);
  background: rgba(144,166,196,0.08);
  color: #c5d5ea; cursor: pointer;
  font-size: 0.85rem; transition: 0.25s;
}
.action-btn i { font-size: 1rem; }
.action-btn .act-count {
  padding: 1px 8px; border-radius: 8px; font-size: 0.72rem;
  background: rgba(144,166,196,0.18); color: #e8eef7;
  min-width: 20px; text-align: center; font-family: 'JetBrains Mono', monospace;
}
.action-btn:hover { background: rgba(144,166,196,0.22); border-color: rgba(144,166,196,0.5); transform: translateY(-1px); }
.action-btn.liked { background: rgba(255,120,140,0.15); border-color: rgba(255,120,140,0.4); color: #ff9caa; }
.action-btn.liked .act-count { background: rgba(255,120,140,0.25); color: #ffb4be; }
.action-btn.starred { background: rgba(240,200,120,0.12); border-color: rgba(240,200,120,0.45); color: #f0c88a; }

/* ===== Comments ===== */
.comments-section { margin-top: 10px; }
.comments-head { display: flex; align-items: baseline; gap: 12px; margin-bottom: 18px; padding-bottom: 12px; border-bottom: 1px dashed rgba(144,166,196,0.2); }
.comments-head h3 { font-weight: 500; color: #e8eef7; letter-spacing: 3px; font-size: 1.05rem; display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-display, serif); }
.comments-head h3 i { color: #a8bcd4; }
.comments-count { padding: 2px 10px; border-radius: 10px; background: rgba(144,166,196,0.16); color: #a8bcd4; font-size: 0.78rem; border: 1px solid rgba(144,166,196,0.25); font-family: 'JetBrains Mono', monospace; }

.comment-input-box { position: relative; margin-bottom: 24px; }
.comment-input-box textarea {
  width: 100%; padding: 14px 60px 14px 18px; border-radius: 14px;
  background: rgba(5,12,26,0.7);
  border: 1px solid rgba(144,166,196,0.25);
  color: #e8eef7; font-family: inherit; font-size: 0.9rem;
  outline: none; resize: vertical; min-height: 64px;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.comment-input-box textarea:focus { border-color: rgba(144,166,196,0.6); box-shadow: 0 0 0 3px rgba(144,166,196,0.12); }
.comment-send-btn {
  position: absolute; right: 10px; bottom: 10px;
  width: 40px; height: 40px; border-radius: 50%;
  border: none; cursor: pointer;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: white; font-size: 1.05rem;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.25s, box-shadow 0.25s;
  box-shadow: 0 4px 12px rgba(144,166,196,0.35);
}
.comment-send-btn:hover { transform: translateY(-2px) scale(1.05); box-shadow: 0 6px 18px rgba(144,166,196,0.5); }

.comment-list { display: flex; flex-direction: column; gap: 14px; }
.comment-group {
  background: rgba(10,18,38,0.5);
  border: 1px solid rgba(144,166,196,0.14);
  border-radius: 16px; padding: 16px 20px;
  transition: border-color 0.25s, background 0.25s;
}
.comment-group:hover { border-color: rgba(144,166,196,0.3); background: rgba(10,18,38,0.62); }
.comment-item { display: flex; gap: 12px; padding: 6px 0; }
.comment-item.parent-comment { padding-bottom: 10px; }
.c-avatar {
  flex-shrink: 0; width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #7890b5, #c5d5ea);
  color: white; font-weight: 500; font-size: 0.95rem;
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-display, serif);
  box-shadow: 0 2px 8px rgba(144,166,196,0.3), inset 0 1px 0 rgba(255,255,255,0.18);
}
.c-avatar.small { width: 28px; height: 28px; font-size: 0.78rem; background: linear-gradient(135deg, #7890b5, #a8bcd4); }
.c-main { flex: 1; min-width: 0; }
.child-comments { padding-left: 14px; margin-top: 4px; border-left: 2px solid rgba(144,166,196,0.18); display: flex; flex-direction: column; gap: 2px; }
.child-comments .comment-item { padding: 6px 0; }
.comment-header { display: flex; gap: 10px; align-items: center; margin-bottom: 4px; font-size: 0.82rem; flex-wrap: wrap; }
.comment-author { color: #a8bcd4; font-weight: 500; }
.reply-to-tag { color: #a8bcd4; font-size: 0.72rem; padding: 1px 7px; border-radius: 7px; background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.2); }
.comment-time { opacity: 0.5; margin-left: auto; font-size: 0.75rem; font-family: 'JetBrains Mono', monospace; }
.comment-body { font-size: 0.9rem; opacity: 0.9; line-height: 1.7; color: #e8eef7; word-break: break-word; }
.comment-footer { display: flex; gap: 8px; margin-top: 8px; font-size: 0.78rem; }
.comment-like, .comment-reply-btn {
  display: inline-flex; align-items: center; gap: 4px;
  cursor: pointer; opacity: 0.55; transition: 0.2s;
  user-select: none; padding: 4px 10px; border-radius: 10px;
}
.comment-like:hover { opacity: 1; color: #ff9caa; background: rgba(255,120,140,0.1); }
.comment-like.liked { opacity: 1; color: #ff9caa; }
.comment-reply-btn { color: #a8bcd4; }
.comment-reply-btn:hover { opacity: 1; background: rgba(144,166,196,0.14); color: #c5d5ea; }

/* Reply Modal */
.reply-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(5,8,20,0.85); z-index: 200; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.reply-modal { background: rgba(15,30,55,0.95); border: 1px solid rgba(144,166,196,0.3); border-radius: 18px; padding: 24px; width: 90%; max-width: 480px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); }
.reply-to { font-size: 0.9rem; opacity: 0.7; margin-bottom: 12px; color: #88bbff; }
.reply-modal textarea { width: 100%; padding: 12px 15px; border-radius: 12px; background: rgba(5,15,30,0.6); border: 1px solid rgba(144,166,196,0.3); color: #e8eef7; font-family: inherit; font-size: 0.9rem; outline: none; resize: vertical; min-height: 80px; }
.reply-modal textarea:focus { border-color: rgba(144,166,196,0.7); }
.reply-modal-btns { display: flex; gap: 10px; margin-top: 14px; justify-content: flex-end; }
.cancel-btn { padding: 8px 20px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.05); color: #a8bcd4; cursor: pointer; transition: 0.3s; font-size: 0.85rem; }
.cancel-btn:hover { background: rgba(255,255,255,0.1); }

.empty-state { text-align: center; padding: 60px 20px; opacity: 0.5; }

@media (max-width: 768px) {
  .container { padding: 0px 20px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-sphere { width: 28px; height: 28px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .tabs { gap: 6px; flex-wrap: wrap; }
  .tab { padding: 8px 14px; font-size: 0.85rem; }
  .category-tabs { gap: 4px; }
  .cat-tab { padding: 5px 10px; font-size: 0.75rem; }
  .post-form { padding: 18px; }
  .editor-wrap { flex-direction: column; }
  .preview-pane { max-height: 250px; }
  .post-card { padding: 15px; }
  .post-title { font-size: 1rem; }
  .submit-btn { width: 100%; }
  .detail-post { padding: 18px; }
  .detail-post h2 { font-size: 1.2rem; }
  .child-comments { padding-left: 8px; }
  .reply-modal { width: 95%; padding: 16px; }
}
</style>
