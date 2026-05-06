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

    <!-- 分类子Tab（详情页隐藏） -->
    <div class="category-tabs" v-show="activeTab === 'browse'">
      <div class="cat-tab" :class="{ active: currentCategory === '' }" @click="currentCategory = ''; loadPosts()">全部</div>
      <div class="cat-tab" v-for="cat in categories" :key="cat.code"
        :class="{ active: currentCategory === cat.code }" @click="currentCategory = cat.code; loadPosts()">
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
            <div class="custom-select" :class="{ open: catDropdownOpen }" @click.stop="catDropdownOpen = !catDropdownOpen" v-click-outside="() => catDropdownOpen = false">
              <div class="cs-value">
                <span :class="{ placeholder: !newPost.categoryCode }">{{ selectedCategoryName || '-- 请选择分类 --' }}</span>
                <i class="ri-arrow-down-s-line cs-arrow"></i>
              </div>
              <div class="cs-dropdown" v-show="catDropdownOpen">
                <div class="cs-option" v-for="cat in categories" :key="cat.code"
                     :class="{ active: newPost.categoryCode === cat.code }"
                     @click.stop="newPost.categoryCode = cat.code; catDropdownOpen = false">
                  {{ cat.name }}
                </div>
              </div>
            </div>
          </div>
          <div class="form-group grow">
            <label><i class="ri-edit-2-line"></i> 帖子标题</label>
            <input v-model="newPost.title" maxlength="100" placeholder="一句话说清主题...">
          </div>
        </div>
        <div class="form-group">
          <label><i class="ri-quill-pen-line"></i> 帖子内容</label>
          <div class="editor-wrap">
            <div class="editor-col">
              <div class="editor-tag" @click="markdownEnabled = !markdownEnabled" style="cursor:pointer" :title="markdownEnabled ? '点击切换为纯文本' : '点击开启 Markdown'">
                <i :class="markdownEnabled ? 'ri-toggle-fill' : 'ri-toggle-line'"></i>
                {{ markdownEnabled ? 'Markdown' : '纯文本' }}
              </div>
              <textarea v-model="newPost.content" placeholder="分享你的技术见解"></textarea>
            </div>
            <div class="editor-col preview-col" v-if="markdownEnabled && newPost.content">
              <div class="editor-tag preview-tag"><i class="ri-eye-line"></i> 实时预览</div>
              <div class="preview-content" v-html="renderMarkdown(newPost.content)"></div>
            </div>
          </div>
        </div>
        <div class="form-footer">
          <span class="form-tip"><i class="ri-information-line"></i> 发布后可在「我的帖子」中查看</span>
          <button class="submit-btn" :disabled="submitting" @click="submitPost"><i class="ri-rocket-2-fill"></i> {{ submitting ? '发布中...' : '发布帖子' }}</button>
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
          <span class="post-category-tag">{{ currentPost.category?.name || '未分类' }}</span>
          <span class="meta-chip"><i class="ri-time-line"></i> {{ formatTime(currentPost.createdAt) }}</span>
          <span class="meta-chip"><i class="ri-eye-line"></i> {{ currentPost.viewCount || 0 }}</span>
        </div>
        <h2 class="detail-title">{{ currentPost.title }}</h2>
        <div class="detail-author" v-if="currentPost.author">
          <span class="author-name">{{ currentPost.author.nickname }}</span>
        </div>
        <div class="title-underline"></div>
        <div v-if="isHtmlContent(currentPost.content)" class="detail-body markdown-body" v-html="currentPost.content"></div>
        <div v-else class="detail-body plain-text" v-text="currentPost.content"></div>
        <div class="detail-images" v-if="currentPost.images && currentPost.images.length">
          <div class="detail-img-grid" :class="{ single: currentPost.images.length === 1 }">
            <img v-for="(img, i) in currentPost.images" :key="i" :src="img" alt="帖子图片" class="detail-img" @click="previewImage(img)">
          </div>
        </div>
        <div class="detail-actions">
          <button class="action-btn like-btn" :class="{ liked: currentPost.liked }" @click="togglePostLike">
            <i class="ri-heart-fill"></i>
            <span class="act-label">{{ currentPost.liked ? '已赞' : '点赞' }}</span>
            <span class="act-count">{{ currentPost.likeCount || 0 }}</span>
          </button>
          <button class="action-btn fav-btn" :class="{ starred: currentPost.collected }" @click="toggleCollect">
            <i :class="currentPost.collected ? 'ri-star-fill' : 'ri-star-line'"></i>
            <span class="act-label">{{ currentPost.collected ? '已收藏' : '收藏' }}</span>
            <span class="act-count">{{ currentPost.collectCount || 0 }}</span>
          </button>
          <button class="action-btn cm-btn" @click="scrollToComment">
            <i class="ri-chat-3-line"></i>
            <span class="act-label">评论</span>
            <span class="act-count">{{ currentPost.commentCount || 0 }}</span>
          </button>
        </div>
      </article>

      <!-- 评论区 -->
      <div class="comments-section" ref="commentsRef">
        <div class="comments-head">
          <span class="comments-title">评论</span>
          <span class="comments-count">{{ currentPost.commentCount || 0 }}</span>
        </div>
        <!-- 评论输入 -->
        <div class="comment-input-area">
          <div class="ci-avatar">U</div>
          <div class="ci-body">
            <textarea v-model="newComment" placeholder="说点什么吧..." rows="1" @keydown.enter.ctrl="submitTopComment" @input="autoResize"></textarea>
            <div class="ci-actions">
              <button class="ci-send" :class="{ active: newComment.trim() }" @click="submitTopComment" :disabled="!newComment.trim()">发布</button>
            </div>
          </div>
        </div>
        <!-- 评论列表 -->
        <div class="comment-list" v-if="comments.length > 0">
          <div class="cmt-item" v-for="comment in comments" :key="comment.commentId">
            <div class="cmt-avatar">{{ (comment.author?.nickname || 'U').slice(0, 1).toUpperCase() }}</div>
            <div class="cmt-content">
              <div class="cmt-user">{{ comment.author?.nickname || '匿名用户' }}</div>
              <div class="cmt-text" v-html="(comment.content || '').replace(/\n/g, '<br>')"></div>
              <div class="cmt-meta">
                <span class="cmt-time">{{ formatTime(comment.createdAt) }}</span>
                <span class="cmt-action" :class="{ liked: comment.liked }" @click="toggleCommentLike(comment)">
                  <i class="ri-thumb-up-line"></i> {{ comment.likeCount || '' }}
                </span>
                <span class="cmt-action" @click="startReply(comment)"><i class="ri-chat-1-line"></i></span>
              </div>
              <!-- 子评论 -->
              <div class="cmt-replies" v-if="comment.replies && comment.replies.length">
                <div class="cmt-reply-item" v-for="reply in flattenReplies(comment.replies)" :key="reply.commentId">
                  <span class="cmt-reply-user">{{ reply.author?.nickname || '匿名用户' }}</span>
                  <span v-if="reply.replyToNickname" class="cmt-reply-to"> 回复 {{ reply.replyToNickname }}：</span>
                  <span class="cmt-reply-text" v-html="(reply.content || '').replace(/\n/g, '<br>')"></span>
                  <div class="cmt-reply-meta">
                    <span class="cmt-time">{{ formatTime(reply.createdAt) }}</span>
                    <span class="cmt-action" :class="{ liked: reply.liked }" @click="toggleCommentLike(reply)">
                      <i class="ri-thumb-up-line"></i> {{ reply.likeCount || '' }}
                    </span>
                    <span class="cmt-action" @click="startReply(reply)"><i class="ri-chat-1-line"></i></span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>暂无评论，快来抢沙发吧</p>
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
        <div class="reply-to">回复 @{{ replyTarget.author?.nickname || '匿名用户' }}</div>
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
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import * as communityApi from '../api/community'
import { useAuth } from '../composables/useAuth'
import { useToast } from '../composables/useToast'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()
const toast = useToast()
const { isLoggedIn } = useAuth()

const route = useRoute()

const activeTab = ref('browse')
const posts = ref([])
const categories = ref([])
const currentCategory = ref('')
const currentPost = ref(null)
const comments = ref([])
const newPost = ref({ categoryCode: '', title: '', content: '' })
const newComment = ref('')
const commentsRef = ref(null)
const previewImg = ref(null)
const submitting = ref(false)
const currentPage = ref(1)
const pageLoading = ref(true)
const categoryLoading = ref(false)
const markdownEnabled = ref(false)
const catDropdownOpen = ref(false)

const selectedCategoryName = computed(() => {
  const cat = categories.value.find(c => c.code === newPost.value.categoryCode)
  return cat ? cat.name : ''
})

// 点击外部关闭下拉框
const vClickOutside = {
  mounted(el, binding) {
    el._clickOutside = (e) => { if (!el.contains(e.target)) binding.value() }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el._clickOutside)
  }
}

// 回复相关
const replyTarget = ref(null)
const replyText = ref('')
const replyInputRef = ref(null)

/** 自动调整评论输入框高度 */
function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

// 将嵌套回复平铺为列表
function flattenReplies(replies, parentNickname = '') {
  const result = []
  for (const r of replies) {
    result.push({ ...r, replyTo: parentNickname || undefined })
    if (r.replies && r.replies.length) {
      result.push(...flattenReplies(r.replies, r.author?.nickname))
    }
  }
  return result
}

const mdOpts = { breaks: true, gfm: true }
function renderMarkdown(text) {
  if (!text) return ''
  try { return marked.parse(text, mdOpts) }
  catch (e) { return text.replace(/\n/g, '<br>') }
}

/** 检测内容是否为 HTML（用于详情页区分渲染方式） */
function isHtmlContent(content) {
  if (!content) return false
  return /<[a-z][\s\S]*>/i.test(content)
}

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

// 加载分类
async function loadCategories() {
  try {
    const data = await communityApi.listCategories()
    categories.value = data || []
    if (categories.value.length > 0 && !newPost.value.categoryCode) {
      newPost.value.categoryCode = categories.value[0].code
    }
  } catch (e) {
    console.error('加载分类失败:', e)
    categories.value = []
  }
}

// 加载帖子列表
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

// 发布帖子
async function submitPost() {
  const { categoryCode, title, content } = newPost.value
  if (!title.trim()) { toast.warning('请填写标题'); return }
  if (!content.trim()) { toast.warning('请填写内容'); return }
  if (!categoryCode) { toast.warning('请选择分类'); return }

  submitting.value = true
  try {
    // 根据模式处理内容：Markdown 模式转换为 HTML，纯文本模式保留原始内容
    const finalContent = markdownEnabled.value
      ? renderMarkdown(content.trim())
      : content.trim()
    await communityApi.createPost({ categoryCode, title: title.trim(), content: finalContent })
    newPost.value = { categoryCode: categories.value[0]?.code || '', title: '', content: '' }
    toast.success('发布成功！')
    activeTab.value = 'browse'
    await loadPosts()
  } catch (e) {
    toast.error('发布失败: ' + (e.message || '请重试'))
  } finally {
    submitting.value = false
  }
}

// 打开帖子详情
async function openPost(postId) {
  try {
    const data = await communityApi.getPost(postId)
    if (data) {
      currentPost.value = data
      activeTab.value = 'detail'
      nextTick(() => { if (contentRef.value) contentRef.value.scrollTo({ top: 0, behavior: 'smooth' }) })
      await loadComments(postId)
    }
  } catch (e) {
    toast.error('加载帖子详情失败: ' + (e.message || '请重试'))
  }
}

// 加载评论
async function loadComments(postId) {
  try {
    const data = await communityApi.listComments(postId, { page: 1, size: 50 })
    comments.value = data?.records || []
  } catch (e) {
    console.error('加载评论失败:', e)
    comments.value = []
  }
}

function backToList() {
  currentPost.value = null
  activeTab.value = 'browse'
}

// 点赞帖子
async function togglePostLike() {
  if (!isLoggedIn.value) { toast.warning('请先登录'); router.push('/login'); return }
  const post = currentPost.value
  if (!post) return
  try {
    if (post.liked) {
      await communityApi.unlikePost(post.postId)
      post.liked = false
      post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
    } else {
      await communityApi.likePost(post.postId)
      post.liked = true
      post.likeCount = (post.likeCount || 0) + 1
    }
  } catch (e) {
    console.error('操作失败:', e)
  }
}

// 收藏帖子
async function toggleCollect() {
  if (!isLoggedIn.value) { toast.warning('请先登录'); router.push('/login'); return }
  const post = currentPost.value
  if (!post) return
  try {
    if (post.collected) {
      await communityApi.uncollectPost(post.postId)
      post.collected = false
      post.collectCount = Math.max(0, (post.collectCount || 0) - 1)
    } else {
      await communityApi.collectPost(post.postId)
      post.collected = true
      post.collectCount = (post.collectCount || 0) + 1
    }
  } catch (e) {
    console.error('操作失败:', e)
  }
}

function scrollToComment() {
  commentsRef.value?.scrollIntoView({ behavior: 'smooth' })
}

// 发评论
async function submitTopComment() {
  if (!isLoggedIn.value) { toast.warning('请先登录'); router.push('/login'); return }
  const body = newComment.value.trim()
  if (!body) return
  try {
    await communityApi.createComment(currentPost.value.postId, { content: body })
    newComment.value = ''
    await loadComments(currentPost.value.postId)
    // 更新评论数
    if (currentPost.value) currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
  } catch (e) {
    toast.error('评论失败: ' + (e.message || '请重试'))
  }
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
async function doSubmitReply() {
  if (!isLoggedIn.value) { toast.warning('请先登录'); router.push('/login'); return }
  const body = replyText.value.trim()
  if (!body || !replyTarget.value) return
  try {
    await communityApi.createComment(currentPost.value.postId, {
      content: body,
      parentId: replyTarget.value.parentId || replyTarget.value.commentId,
      replyToUserId: replyTarget.value.author?.userId
    })
    cancelReply()
    await loadComments(currentPost.value.postId)
    if (currentPost.value) currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
  } catch (e) {
    toast.error('回复失败: ' + (e.message || '请重试'))
  }
}

// 评论点赞
async function toggleCommentLike(comment) {
  if (!isLoggedIn.value) { toast.warning('请先登录'); router.push('/login'); return }
  try {
    if (comment.liked) {
      await communityApi.unlikeComment(comment.commentId)
      comment.liked = false
      comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
    } else {
      await communityApi.likeComment(comment.commentId)
      comment.liked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    }
  } catch (e) {
    console.error('评论点赞失败:', e)
  }
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

onMounted(async () => {
  const pid = route.query.postId
  if (pid) {
    // 有 postId 参数：直接进入详情页，不加载列表
    pageLoading.value = false
    await loadCategories()
    await openPost(pid)
  } else {
    // 正常浏览模式：加载分类和帖子列表
    pageLoading.value = true
    await Promise.allSettled([loadCategories(), loadPosts()])
    pageLoading.value = false
  }
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
.md-toggle {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.72rem;
  opacity: 0.7;
  margin-left: 10px;
  padding: 3px 10px;
  border-radius: 12px;
  background: rgba(144,166,196,0.1);
  border: 1px solid rgba(144,166,196,0.2);
  color: #a8bcd4;
  cursor: pointer;
  transition: 0.3s;
  user-select: none;
}
.md-toggle:hover { opacity: 1; }
.md-toggle.active {
  background: rgba(120,144,181,0.25);
  border-color: rgba(120,144,181,0.5);
  color: #c5d5ea;
  opacity: 1;
}
.md-toggle i { font-size: 0.9rem; }
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
  outline: none; transition: border-color 0.25s, box-shadow 0.25s; box-sizing: border-box;
}
.form-group textarea { resize: vertical; line-height: 1.7; }
.form-group input:focus, .form-group textarea:focus {
  border-color: rgba(144,166,196,0.6);
  box-shadow: 0 0 0 3px rgba(144,166,196,0.12);
}
/* ===== Custom Select ===== */
.custom-select {
  position: relative;
  width: 100%;
  cursor: pointer;
  user-select: none;
}
.cs-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 14px;
  background: rgba(5,12,26,0.5);
  border: 1.5px solid rgba(144,166,196,0.2);
  color: #e8eef7;
  font-size: 0.92rem;
  transition: border-color 0.25s, box-shadow 0.25s, background-color 0.25s;
}
.cs-value .placeholder { color: rgba(168,188,212,0.45); }
.cs-arrow {
  font-size: 1rem;
  color: #a8bcd4;
  transition: transform 0.25s;
}
.custom-select.open .cs-value {
  border-color: rgba(144,166,196,0.6);
  box-shadow: 0 0 0 3px rgba(144,166,196,0.1);
  background-color: rgba(5,12,26,0.7);
}
.custom-select.open .cs-arrow {
  transform: rotate(180deg);
}
.custom-select:hover .cs-value {
  border-color: rgba(144,166,196,0.4);
  background-color: rgba(5,12,26,0.65);
}
.cs-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: rgba(12, 16, 34, 0.96);
  border: 1.5px solid rgba(144,166,196,0.25);
  border-radius: 14px;
  padding: 6px;
  z-index: 50;
  backdrop-filter: blur(16px);
  box-shadow: 0 12px 36px rgba(0,0,0,0.4);
  max-height: 200px;
  overflow-y: auto;
}
.cs-option {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.9rem;
  color: #c5d5ea;
  transition: background 0.15s, color 0.15s;
}
.cs-option:hover {
  background: rgba(144,166,196,0.15);
  color: #fff;
}
.cs-option.active {
  background: rgba(144,166,196,0.22);
  color: #fff;
  font-weight: 500;
}

/* ===== Editor ===== */
.editor-wrap { display: flex; gap: 14px; align-items: stretch; min-height: 260px; }
.editor-col { flex: 1; position: relative; display: flex; flex-direction: column; min-width: 0; }
.editor-col textarea {
  flex: 1; height: 100%; min-height: 220px;
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
  flex: 1;
  align-self: stretch;
}
.preview-content { font-size: 0.9rem; line-height: 1.75; }
.preview-content :deep(h1), .preview-content :deep(h2), .preview-content :deep(h3) { color: #c5d5ea; margin: 10px 0; font-weight: 500; }
.preview-content :deep(code) { background: rgba(144,166,196,0.15); padding: 2px 6px; border-radius: 4px; font-size: 0.85em; font-family: 'JetBrains Mono', 'Consolas', monospace; }
.preview-content :deep(pre) { background: rgba(5,10,20,0.8); padding: 12px; border-radius: 8px; overflow-x: auto; border: 1px solid rgba(144,166,196,0.2); }
.preview-content :deep(pre code) { background: none; padding: 0; }
.preview-content :deep(blockquote) { border-left: 3px solid #c5d5ea; padding-left: 12px; color: #c5d5ea; margin: 10px 0; }
.preview-content :deep(ul), .preview-content :deep(ol) { padding-left: 24px; margin: 8px 0; }
.preview-content :deep(li) { margin-bottom: 4px; line-height: 1.6; }

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

.submit-btn {
  padding: 12px 34px; border-radius: 30px; border: none;
  background: linear-gradient(135deg, #7890b5 0%, #a8bcd4 100%);
  color: white; cursor: pointer; font-size: 0.92rem;
  letter-spacing: 2px; font-weight: 500;
  box-shadow: 0 6px 20px rgba(144,166,196,0.35), inset 0 1px 0 rgba(255,255,255,0.2);
  transition: transform 0.25s, box-shadow 0.25s;
  display: inline-flex; align-items: center; gap: 8px;
}
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
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
.post-meta { font-size: 0.85rem; opacity: 0.6; }
.post-title { font-size: 1.15rem; margin-bottom: 8px; color: #fff; font-weight: 500; }
.post-summary { font-size: 0.9rem; opacity: 0.8; line-height: 1.6; max-height: 80px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
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
.detail-author { margin-bottom: 4px; font-size: 0.85rem; opacity: 0.6; }
.author-name { color: #a8bcd4; }
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
.detail-body.plain-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  line-height: 1.95;
}

/* Detail Images */
.detail-images { margin-top: 15px; }
.detail-img-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.detail-img-grid.single { grid-template-columns: 1fr; max-width: 400px; }
.detail-img { width: 100%; border-radius: 10px; cursor: pointer; transition: 0.3s; border: 1px solid rgba(144,166,196,0.2); }
.detail-img:hover { transform: scale(1.02); box-shadow: 0 4px 15px rgba(0,0,0,0.3); }
.img-preview-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 300; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.img-preview-full { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 8px; }

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
.comments-section { margin-top: 10px; padding: 0 4px; }
.comments-head { display: flex; align-items: baseline; gap: 10px; margin-bottom: 16px; }
.comments-title { font-size: 1rem; font-weight: 500; color: #e8eef7; letter-spacing: 1px; }
.comments-count { font-size: 0.78rem; color: #a8bcd4; opacity: 0.6; }

/* 评论输入区 */
.comment-input-area { display: flex; gap: 12px; margin-bottom: 24px; align-items: flex-start; }
.ci-avatar {
  flex-shrink: 0; width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #7890b5, #c5d5ea);
  color: white; font-weight: 500; font-size: 0.85rem;
  display: flex; align-items: center; justify-content: center;
}
.ci-body { flex: 1; min-width: 0; }
.ci-body textarea {
  width: 100%; padding: 10px 14px; border-radius: 10px;
  background: rgba(5,12,26,0.5);
  border: 1px solid rgba(144,166,196,0.18);
  color: #e8eef7; font-family: inherit; font-size: 0.88rem;
  outline: none; resize: none; min-height: 36px; max-height: 120px;
  transition: border-color 0.25s, box-shadow 0.25s; box-sizing: border-box;
  line-height: 1.5;
}
.ci-body textarea:focus { border-color: rgba(144,166,196,0.5); box-shadow: 0 0 0 2px rgba(144,166,196,0.08); }
.ci-actions { display: flex; justify-content: flex-end; margin-top: 8px; }
.ci-send {
  padding: 6px 20px; border-radius: 18px; border: none;
  background: rgba(144,166,196,0.15); color: rgba(255,255,255,0.35);
  cursor: not-allowed; font-size: 0.82rem; letter-spacing: 1px;
  transition: 0.25s;
}
.ci-send.active {
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: #fff; cursor: pointer;
  box-shadow: 0 3px 10px rgba(144,166,196,0.3);
}
.ci-send.active:hover { box-shadow: 0 5px 16px rgba(144,166,196,0.45); transform: translateY(-1px); }

/* 评论列表 */
.comment-list { display: flex; flex-direction: column; }
.cmt-item {
  display: flex; gap: 12px; padding: 16px 0;
  border-bottom: 1px solid rgba(144,166,196,0.08);
}
.cmt-item:last-child { border-bottom: none; }
.cmt-avatar {
  flex-shrink: 0; width: 34px; height: 34px; border-radius: 50%;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: white; font-weight: 500; font-size: 0.82rem;
  display: flex; align-items: center; justify-content: center;
}
.cmt-content { flex: 1; min-width: 0; }
.cmt-user { font-size: 0.82rem; color: #8a9bb8; margin-bottom: 4px; font-weight: 500; }
.cmt-text { font-size: 0.9rem; color: #e8eef7; line-height: 1.65; word-break: break-word; }
.cmt-meta { display: flex; align-items: center; gap: 16px; margin-top: 8px; }
.cmt-time { font-size: 0.72rem; color: rgba(168,188,212,0.45); }
.cmt-action {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 0.75rem; color: rgba(168,188,212,0.45);
  cursor: pointer; transition: 0.2s; user-select: none;
}
.cmt-action:hover { color: #c5d5ea; }
.cmt-action.liked { color: #ff9caa; }

/* 子评论 */
.cmt-replies {
  margin-top: 10px; padding: 10px 14px;
  background: rgba(144,166,196,0.04);
  border-radius: 10px;
}
.cmt-reply-item { padding: 6px 0; font-size: 0.85rem; line-height: 1.6; }
.cmt-reply-item:not(:last-child) { border-bottom: 1px solid rgba(144,166,196,0.06); padding-bottom: 8px; margin-bottom: 2px; }
.cmt-reply-user { color: #8a9bb8; font-weight: 500; font-size: 0.82rem; }
.cmt-reply-to { color: rgba(168,188,212,0.5); font-size: 0.82rem; }
.cmt-reply-text { color: #e8eef7; }
.cmt-reply-meta { display: flex; align-items: center; gap: 14px; margin-top: 4px; }

/* Reply Modal */
.reply-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(5,8,20,0.85); z-index: 200; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.reply-modal { background: rgba(15,30,55,0.95); border: 1px solid rgba(144,166,196,0.3); border-radius: 18px; padding: 24px; width: 90%; max-width: 480px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); }
.reply-to { font-size: 0.9rem; opacity: 0.7; margin-bottom: 12px; color: #88bbff; }
.reply-modal textarea { width: 100%; padding: 12px 15px; border-radius: 12px; background: rgba(5,15,30,0.6); border: 1px solid rgba(144,166,196,0.3); color: #e8eef7; font-family: inherit; font-size: 0.9rem; outline: none; resize: vertical; min-height: 80px; box-sizing: border-box; }
.reply-modal textarea:focus { border-color: rgba(144,166,196,0.7); }
.reply-modal-btns { display: flex; gap: 10px; margin-top: 14px; justify-content: flex-end; }
.cancel-btn { padding: 8px 20px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.05); color: #a8bcd4; cursor: pointer; transition: 0.3s; font-size: 0.85rem; }
.cancel-btn:hover { background: rgba(255,255,255,0.1); }

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
  .preview-col { max-height: 250px; }
  .post-card { padding: 15px; }
  .post-title { font-size: 1rem; }
  .submit-btn { width: 100%; }
  .detail-post { padding: 18px; }
  .detail-post h2 { font-size: 1.2rem; }
  .child-comments { padding-left: 8px; }
  .reply-modal { width: 95%; padding: 16px; }
}
</style>