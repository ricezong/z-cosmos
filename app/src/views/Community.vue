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
        <h3><i class="ri-sparkling-line"></i> 发布新帖</h3>
        <div class="form-group">
          <label>分类</label>
          <select v-model="newPost.category" class="form-select">
            <option v-for="cat in techCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>帖子标题</label>
          <input v-model="newPost.title" maxlength="100" placeholder="输入标题...">
        </div>
        <div class="form-group">
          <label>帖子内容 <span class="md-hint">（支持 Markdown 语法）</span></label>
          <div class="editor-wrap">
            <textarea v-model="newPost.content" placeholder="分享你的技术见解...&#10;&#10;支持 Markdown 语法：&#10;# 标题&#10;**粗体** *斜体*&#10;- 列表&#10;`代码`&#10;```代码块```"></textarea>
            <div class="preview-pane" v-if="newPost.content">
              <div class="preview-label">预览</div>
              <div class="preview-content" v-html="renderMarkdown(newPost.content)"></div>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>上传图片</label>
          <div class="upload-area" @click="triggerUpload" @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onDrop" :class="{ dragover: isDragOver }">
            <div class="upload-hint" v-if="newPostImages.length === 0">
              <i class="ri-image-add-line"></i>
              <span>点击或拖拽图片到此处上传</span>
              <span class="upload-sub">支持 JPG/PNG/GIF，单张不超过 5MB</span>
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
        <button class="submit-btn" @click="submitPost"><i class="ri-rocket-line"></i> 发布帖子</button>
      </div>
    </div>

    <!-- 帖子详情 -->
    <div class="detail-view" v-if="activeTab === 'detail' && currentPost">
      <div class="detail-header">
        <button class="back-link" @click="backToList"><i class="ri-arrow-left-line"></i> 返回列表</button>
      </div>
      <div class="detail-post">
        <div class="detail-meta-row">
          <span class="post-category-tag" :class="currentPost.category">{{ categoryLabel(currentPost.category) }}</span>
          <span class="detail-time">{{ formatTime(currentPost.time) }} · <i class="ri-eye-line"></i> {{ currentPost.views }}</span>
        </div>
        <h2>{{ currentPost.title }}</h2>
        <div class="detail-body markdown-body" v-html="renderMarkdown(currentPost.content)"></div>
        <div class="detail-images" v-if="currentPost.images && currentPost.images.length">
          <div class="detail-img-grid" :class="{ single: currentPost.images.length === 1 }">
            <img v-for="(img, i) in currentPost.images" :key="i" :src="img" alt="帖子图片" class="detail-img" @click="previewImage(img)">
          </div>
        </div>
        <div class="detail-actions">
          <button class="action-btn" :class="{ liked: currentPost.liked }" @click="togglePostLike">
            <i class="ri-heart-fill"></i> {{ currentPost.likes || 0 }}
          </button>
          <button class="action-btn" :class="{ starred: isFavorite }" @click="toggleFavorite">
            <i :class="isFavorite ? 'ri-star-fill' : 'ri-star-line'"></i>
            {{ isFavorite ? '已收藏' : '收藏' }}
          </button>
          <button class="action-btn" @click="scrollToComment"><i class="ri-chat-3-line"></i> 评论</button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section" ref="commentsRef">
        <h3><i class="ri-chat-smile-2-line"></i> 评论 ({{ countAllComments(currentPost.comments) }})</h3>
        <div class="comment-input-box">
          <textarea v-model="newComment.body" placeholder="写下你的评论..." rows="2" @keydown.enter.ctrl="submitTopComment"></textarea>
          <button class="submit-btn small" @click="submitTopComment">发送</button>
        </div>
        <div class="comment-list" v-if="currentPost.comments && currentPost.comments.length">
          <div class="comment-group" v-for="comment in currentPost.comments" :key="comment.id">
            <div class="comment-item parent-comment">
              <div class="comment-header">
                <span class="comment-author">{{ comment.nickname }}</span>
                <span class="comment-time">{{ formatTime(comment.time) }}</span>
              </div>
              <div class="comment-body" v-html="comment.body.replace(/\n/g, '<br>')"></div>
              <div class="comment-footer">
                <span class="comment-like" :class="{ liked: comment.liked }" @click="toggleCommentLike(comment.id)"><i class="ri-heart-fill"></i> {{ comment.likes || 0 }}</span>
                <span class="comment-reply-btn" @click="startReply(comment)">回复</span>
              </div>
            </div>
            <!-- 子评论平铺在同一框内 -->
            <div class="child-comments" v-if="comment.replies && comment.replies.length">
              <div class="comment-item child-comment" v-for="reply in flattenReplies(comment.replies)" :key="reply.id">
                <div class="comment-header">
                  <span class="comment-author">{{ reply.nickname }}</span>
                  <span v-if="reply.replyTo" class="reply-to-tag">回复 @{{ reply.replyTo }}</span>
                  <span class="comment-time">{{ formatTime(reply.time) }}</span>
                </div>
                <div class="comment-body" v-html="reply.body.replace(/\n/g, '<br>')"></div>
                <div class="comment-footer">
                  <span class="comment-like" :class="{ liked: reply.liked }" @click="toggleCommentLike(reply.id)"><i class="ri-heart-fill"></i> {{ reply.likes || 0 }}</span>
                  <span class="comment-reply-btn" @click="startReply(reply)">回复</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>暂无评论，来说两句吧~</p>
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
  --bt-bg: rgba(130,170,225,0.3);
  --bt-border: rgba(130,170,225,0.5);
  --bt-color: #c9d9ef;
  --bt-hover-bg: rgba(130,170,225,0.5);
  --bt-shadow: rgba(130,170,225,0.3);
}
.container { position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(130,170,225,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #92b6e0, #c9d9ef); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(90,135,200,0.2); border: 1px solid rgba(130,170,225,0.4); color: #c9d9ef; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(90,135,200,0.35); box-shadow: 0 0 15px rgba(130,170,225,0.15); }
.tabs { display: flex; gap: 10px; margin-bottom: 20px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(90,135,200,0.12); border: 1px solid rgba(130,170,225,0.25); color: #a0b8d6; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(90,135,200,0.3); border-color: rgba(130,170,225,0.5); color: #fff; }
.container.no-padding { padding: 0; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.earth { background: radial-gradient(circle at 35% 35%, #92b6e0, #5f85c0 50%, #243d68 100%); box-shadow: 0 0 12px rgba(90,135,200,0.5), inset 0 0 8px rgba(255,255,255,0.15); }
.planet-sphere.earth::after { content: ''; position: absolute; inset: 10% 25% 40% 20%; background: rgba(255,255,255,0.12); border-radius: 50%; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

/* Category Tabs */
.category-tabs { display: flex; gap: 6px; flex-wrap: wrap; overflow-x: auto; }
.cat-tab { padding: 6px 14px; border-radius: 16px; cursor: pointer; background: rgba(90,135,200,0.08); border: 1px solid rgba(130,170,225,0.18); color: #a0b8d6; font-size: 0.82rem; white-space: nowrap; transition: 0.3s; }
.cat-tab.active, .cat-tab:hover { background: rgba(90,135,200,0.3); border-color: rgba(130,170,225,0.5); color: #fff; }

.panel { display: block; }

/* Post form */
.post-form { background: rgba(14,20,42,0.62); border: 1px solid rgba(130,170,225,0.25); border-radius: 20px; padding: 28px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.post-form h3 { font-weight: 400; margin-bottom: 18px; color: #92b6e0; letter-spacing: 2px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-size: 0.9rem; opacity: 0.8; }
.md-hint { font-size: 0.75rem; opacity: 0.5; margin-left: 8px; }
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: 12px 15px; border-radius: 12px; background: rgba(5,15,30,0.6); border: 1px solid rgba(130,170,225,0.3); color: #e0f0ff; font-family: inherit; font-size: 0.95rem; outline: none; transition: 0.3s; }
.form-group textarea { min-height: 180px; resize: vertical; font-family: 'Consolas', 'Courier New', monospace; line-height: 1.6; }
.form-select { appearance: none; cursor: pointer; }
.form-group input:focus, .form-group textarea:focus, .form-group select:focus { border-color: rgba(130,170,225,0.7); box-shadow: 0 0 10px rgba(130,170,225,0.15); }

/* Editor */
.editor-wrap { display: flex; gap: 15px; }
.editor-wrap textarea { flex: 1; min-height: 200px; }
.preview-pane { flex: 1; background: rgba(5,15,30,0.6); border: 1px solid rgba(130,170,225,0.2); border-radius: 12px; padding: 15px; overflow-y: auto; max-height: 400px; }
.preview-label { font-size: 0.75rem; opacity: 0.5; margin-bottom: 8px; text-transform: uppercase; }
.preview-content { font-size: 0.9rem; line-height: 1.7; }
.preview-content :deep(h1), .preview-content :deep(h2), .preview-content :deep(h3) { color: #92b6e0; margin: 10px 0; }
.preview-content :deep(code) { background: rgba(130,170,225,0.15); padding: 2px 6px; border-radius: 4px; font-size: 0.85em; }
.preview-content :deep(pre) { background: rgba(5,10,20,0.8); padding: 12px; border-radius: 8px; overflow-x: auto; border: 1px solid rgba(130,170,225,0.2); }
.preview-content :deep(pre code) { background: none; padding: 0; }
.preview-content :deep(blockquote) { border-left: 3px solid #92b6e0; padding-left: 12px; color: #c9d9ef; margin: 10px 0; }

/* Upload */
.upload-area { border: 2px dashed rgba(130,170,225,0.3); border-radius: 14px; padding: 24px; text-align: center; cursor: pointer; transition: 0.3s; min-height: 100px; display: flex; align-items: center; justify-content: center; }
.upload-area:hover, .upload-area.dragover { border-color: rgba(130,170,225,0.6); background: rgba(130,170,225,0.08); }
.upload-hint { display: flex; flex-direction: column; align-items: center; gap: 8px; color: rgba(160,200,255,0.5); }
.upload-hint i { font-size: 2rem; }
.upload-hint span { font-size: 0.9rem; }
.upload-sub { font-size: 0.75rem !important; opacity: 0.6; }
.upload-preview { display: flex; flex-wrap: wrap; gap: 10px; width: 100%; }
.preview-thumb { position: relative; width: 80px; height: 80px; border-radius: 10px; overflow: hidden; border: 1px solid rgba(130,170,225,0.3); }
.preview-thumb img { width: 100%; height: 100%; object-fit: cover; }
.remove-img { position: absolute; top: 2px; right: 2px; width: 20px; height: 20px; border-radius: 50%; background: rgba(255,80,80,0.8); color: white; border: none; cursor: pointer; font-size: 0.8rem; display: flex; align-items: center; justify-content: center; line-height: 1; }
.add-more { width: 80px; height: 80px; border-radius: 10px; border: 2px dashed rgba(130,170,225,0.3); display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 1.5rem; color: rgba(160,200,255,0.5); transition: 0.3s; }
.add-more:hover { border-color: rgba(130,170,225,0.6); color: #c9d9ef; }

/* Detail Images */
.detail-images { margin-top: 15px; }
.detail-img-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.detail-img-grid.single { grid-template-columns: 1fr; max-width: 400px; }
.detail-img { width: 100%; border-radius: 10px; cursor: pointer; transition: 0.3s; border: 1px solid rgba(130,170,225,0.2); }
.detail-img:hover { transform: scale(1.02); box-shadow: 0 4px 15px rgba(0,0,0,0.3); }
.img-preview-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 300; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.img-preview-full { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 8px; }

.submit-btn { padding: 12px 32px; border-radius: 30px; border: none; background: linear-gradient(135deg, #5f85c0, #92b6e0); color: white; cursor: pointer; font-size: 0.95rem; transition: 0.3s; letter-spacing: 1px; }
.submit-btn:hover { box-shadow: 0 0 20px rgba(130,170,225,0.4); transform: translateY(-1px); }
.submit-btn.small { padding: 8px 20px; font-size: 0.85rem; }

/* Post list */
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { background: rgba(10,25,50,0.5); border: 1px solid rgba(130,170,225,0.2); border-radius: 16px; padding: 20px; backdrop-filter: blur(8px); cursor: pointer; transition: 0.3s; }
.post-card:hover { border-color: rgba(130,170,225,0.5); box-shadow: 0 5px 25px rgba(90,135,200,0.15); transform: translateY(-2px); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.post-category-tag { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 0.75rem; background: rgba(130,170,225,0.15); color: #c9d9ef; border: 1px solid rgba(130,170,225,0.25); }
.post-category-tag.frontend { background: rgba(58,180,120,0.2); color: #7ee8a0; border-color: rgba(58,180,120,0.3); }
.post-category-tag.backend { background: rgba(90,135,200,0.2); color: #88bbff; border-color: rgba(90,135,200,0.3); }
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
.post-content :deep(code) { background: rgba(130,170,225,0.12); padding: 1px 4px; border-radius: 3px; font-size: 0.85em; }
.post-stats { display: flex; gap: 20px; margin-top: 12px; font-size: 0.85rem; opacity: 0.6; }

/* Detail */
.detail-view { margin-top: 20px; }
.detail-header { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; }
.back-link { padding: 8px 18px; border-radius: 30px; background: rgba(90,135,200,0.2); border: 1px solid rgba(130,170,225,0.3); color: #c9d9ef; cursor: pointer; font-size: 0.9rem; transition: 0.3s; border: none; }
.back-link:hover { background: rgba(90,135,200,0.35); }
.detail-post { background: rgba(14,20,42,0.62); border: 1px solid rgba(130,170,225,0.25); border-radius: 20px; padding: 28px; margin-bottom: 25px; }
.detail-meta-row { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; }
.detail-time { font-size: 0.85rem; opacity: 0.6; }
.detail-post h2 { font-weight: 400; margin-bottom: 18px; color: #fff; font-size: 1.5rem; }
.detail-body { line-height: 1.9; opacity: 0.9; font-size: 0.95rem; }
.detail-body :deep(h1), .detail-body :deep(h2), .detail-body :deep(h3) { color: #92b6e0; margin: 16px 0 8px; }
.detail-body :deep(code) { background: rgba(130,170,225,0.12); padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
.detail-body :deep(pre) { background: rgba(5,10,20,0.8); padding: 14px; border-radius: 8px; overflow-x: auto; border: 1px solid rgba(130,170,225,0.2); margin: 10px 0; }
.detail-body :deep(pre code) { background: none; padding: 0; }
.detail-body :deep(blockquote) { border-left: 3px solid #92b6e0; padding-left: 14px; color: #c9d9ef; margin: 12px 0; }
.detail-body :deep(ul), .detail-body :deep(ol) { padding-left: 20px; }
.detail-body :deep(a) { color: #92b6e0; }
.detail-actions { display: flex; gap: 15px; margin-top: 20px; padding-top: 18px; border-top: 1px solid rgba(130,170,225,0.2); }
.action-btn { padding: 8px 20px; border-radius: 20px; border: 1px solid rgba(130,170,225,0.3); background: rgba(90,135,200,0.1); color: #c9d9ef; cursor: pointer; transition: 0.3s; font-size: 0.85rem; }
.action-btn:hover { background: rgba(90,135,200,0.25); }
.action-btn.liked { background: rgba(255,80,100,0.2); border-color: rgba(255,80,100,0.4); color: #ff8899; }
.action-btn.starred { background: rgba(255,200,60,0.18); border-color: rgba(255,200,60,0.45); color: #ffd880; }

/* Comments */
.comments-section { margin-top: 10px; }
.comments-section h3 { font-weight: 400; margin-bottom: 15px; color: #92b6e0; letter-spacing: 2px; }
.comment-input-box { display: flex; gap: 10px; margin-bottom: 20px; align-items: flex-end; }
.comment-input-box textarea { flex: 1; padding: 12px 15px; border-radius: 12px; background: rgba(5,15,30,0.6); border: 1px solid rgba(130,170,225,0.3); color: #e0f0ff; font-family: inherit; font-size: 0.9rem; outline: none; resize: vertical; transition: 0.3s; min-height: 50px; }
.comment-input-box textarea:focus { border-color: rgba(130,170,225,0.7); }
.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-group { background: rgba(10,25,50,0.35); border: 1px solid rgba(130,170,225,0.15); border-radius: 14px; padding: 16px; }
.comment-item { padding: 10px 0; }
.comment-item.parent-comment { padding-bottom: 6px; border-bottom: 1px solid rgba(130,170,225,0.08); }
.child-comments { padding-left: 12px; border-left: 2px solid rgba(130,170,225,0.15); margin-top: 6px; }
.child-comments .comment-item { padding: 8px 0; border-bottom: 1px solid rgba(130,170,225,0.06); }
.child-comments .comment-item:last-child { border-bottom: none; }
.comment-header { display: flex; gap: 10px; align-items: center; margin-bottom: 6px; font-size: 0.85rem; flex-wrap: wrap; }
.comment-author { color: #92b6e0; font-weight: 500; }
.reply-to-tag { color: #c9d9ef; font-size: 0.8rem; opacity: 0.7; }
.comment-time { opacity: 0.5; margin-left: auto; }
.comment-body { font-size: 0.9rem; opacity: 0.85; line-height: 1.6; }
.comment-footer { display: flex; gap: 16px; margin-top: 8px; font-size: 0.8rem; }
.comment-like { cursor: pointer; opacity: 0.6; transition: 0.2s; user-select: none; }
.comment-like:hover, .comment-like.liked { opacity: 1; color: #ff8899; }
.comment-reply-btn { cursor: pointer; opacity: 0.5; transition: 0.2s; color: #c9d9ef; }
.comment-reply-btn:hover { opacity: 1; }

/* Reply Modal */
.reply-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(5,8,20,0.85); z-index: 200; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.reply-modal { background: rgba(15,30,55,0.95); border: 1px solid rgba(130,170,225,0.3); border-radius: 18px; padding: 24px; width: 90%; max-width: 480px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); }
.reply-to { font-size: 0.9rem; opacity: 0.7; margin-bottom: 12px; color: #88bbff; }
.reply-modal textarea { width: 100%; padding: 12px 15px; border-radius: 12px; background: rgba(5,15,30,0.6); border: 1px solid rgba(130,170,225,0.3); color: #e0f0ff; font-family: inherit; font-size: 0.9rem; outline: none; resize: vertical; min-height: 80px; }
.reply-modal textarea:focus { border-color: rgba(130,170,225,0.7); }
.reply-modal-btns { display: flex; gap: 10px; margin-top: 14px; justify-content: flex-end; }
.cancel-btn { padding: 8px 20px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.05); color: #a0b8d0; cursor: pointer; transition: 0.3s; font-size: 0.85rem; }
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
