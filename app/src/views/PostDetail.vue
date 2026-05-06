<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere earth"></div></div>
        <div class="header-title">
          <h1>帖子详情</h1>
          <p>地球 · 技术交流中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">

    <LoadingSpinner v-if="loading" text="加载中..." />

    <template v-else-if="currentPost">
      <div class="detail-header">
        <button class="back-link" @click="router.push('/community')">
          <i class="ri-arrow-left-s-line"></i>
          <span>社区</span>
        </button>
        <div class="header-separator"></div>
        <div class="header-breadcrumb">
          <span class="breadcrumb-current">帖子详情</span>
        </div>
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

      <div class="comments-section" ref="commentsRef">
        <div class="comments-head">
          <span class="comments-title">评论</span>
          <span class="comments-count">{{ currentPost.commentCount || 0 }}</span>
        </div>
        <div class="comment-input-area">
          <div class="ci-avatar">U</div>
          <div class="ci-body">
            <textarea v-model="newComment" placeholder="说点什么吧..." rows="1" @keydown.enter.ctrl="submitTopComment" @input="autoResize"></textarea>
            <div class="ci-actions">
              <button class="ci-send" :class="{ active: newComment.trim() }" @click="submitTopComment" :disabled="!newComment.trim()">发布</button>
            </div>
          </div>
        </div>
        <div class="comments-divider"></div>
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
    </template>

    <div class="empty-state" v-else>
      <p>帖子不存在或已被删除</p>
      <router-link to="/community" class="go-link"><i class="ri-arrow-left-line"></i> 返回社区</router-link>
    </div>

    </div>
    </div>
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>

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

  <Teleport to="body">
    <div class="img-preview-overlay" v-if="previewImg" @click="previewImg = null">
      <img :src="previewImg" class="img-preview-full" @click.stop>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as communityApi from '../api/community'
import { useAuth } from '../composables/useAuth'
import { useToast } from '../composables/useToast'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const props = defineProps({ id: String })
const route = useRoute()
const router = useRouter()
const toast = useToast()
const { isLoggedIn } = useAuth()

const loading = ref(true)
const currentPost = ref(null)
const comments = ref([])
const newComment = ref('')
const commentsRef = ref(null)
const previewImg = ref(null)
const contentRef = ref(null)

const replyTarget = ref(null)
const replyText = ref('')
const replyInputRef = ref(null)

function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

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

async function loadPost() {
  const postId = props.id || route.params.id
  if (!postId) return
  loading.value = true
  try {
    const data = await communityApi.getPost(postId)
    if (data) {
      currentPost.value = data
      await loadComments(postId)
    }
  } catch (e) {
    toast.error('加载帖子详情失败: ' + (e.message || '请重试'))
  } finally {
    loading.value = false
  }
}

async function loadComments(postId) {
  try {
    const data = await communityApi.listComments(postId, { page: 1, size: 50 })
    comments.value = data?.records || []
  } catch (e) {
    console.error('加载评论失败:', e)
    comments.value = []
  }
}

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

async function submitTopComment() {
  if (!isLoggedIn.value) { toast.warning('请先登录'); router.push('/login'); return }
  const body = newComment.value.trim()
  if (!body) return
  try {
    await communityApi.createComment(currentPost.value.postId, { content: body })
    newComment.value = ''
    await loadComments(currentPost.value.postId)
    if (currentPost.value) currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
  } catch (e) {
    toast.error('评论失败: ' + (e.message || '请重试'))
  }
}

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

const showBackTop = ref(false)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadPost()
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
.go-link { color: #c5d5ea; text-decoration: none; transition: 0.3s; margin-top: 12px; display: inline-flex; align-items: center; gap: 6px; }
.go-link:hover { color: #e8eef7; }

.detail-header { display: flex; align-items: center; gap: 0; margin-bottom: 22px; }
.back-link {
  display: inline-flex; align-items: center; gap: 2px;
  padding: 6px 12px; border-radius: 8px;
  background: transparent;
  border: none;
  color: #a8bcd4; cursor: pointer;
  font-size: 0.88rem; transition: 0.2s; letter-spacing: 0.3px;
}
.back-link i { font-size: 1.2rem; transition: transform 0.2s; }
.back-link:hover {
  background: rgba(144,166,196,0.1);
  color: #e8eef7;
}
.back-link:hover i { transform: translateX(-2px); }
.header-separator {
  width: 1px; height: 16px;
  background: rgba(144,166,196,0.25);
  margin: 0 8px;
  flex-shrink: 0;
}
.header-breadcrumb {
  display: flex; align-items: center; gap: 6px;
  font-size: 0.88rem;
}
.breadcrumb-current {
  color: #e8eef7;
  font-weight: 500;
  letter-spacing: 0.3px;
}

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
.post-category-tag { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 0.75rem; background: rgba(144,166,196,0.15); color: #c5d5ea; border: 1px solid rgba(144,166,196,0.25); }
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

.comments-section { margin-top: 10px; padding: 0 4px; }
.comments-divider {
  height: 1px;
  margin: 20px 0 24px;
  background: linear-gradient(90deg, transparent 0%, rgba(144,166,196,0.2) 15%, rgba(144,166,196,0.35) 50%, rgba(144,166,196,0.2) 85%, transparent 100%);
}
.comments-head { display: flex; align-items: baseline; gap: 10px; margin-bottom: 16px; }
.comments-title { font-size: 1rem; font-weight: 500; color: #e8eef7; letter-spacing: 1px; }
.comments-count { font-size: 0.78rem; color: #a8bcd4; opacity: 0.6; }

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

.reply-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(5,8,20,0.85); z-index: 200; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.reply-modal { background: rgba(15,30,55,0.95); border: 1px solid rgba(144,166,196,0.3); border-radius: 18px; padding: 24px; width: 90%; max-width: 480px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); }
.reply-to { font-size: 0.9rem; opacity: 0.7; margin-bottom: 12px; color: #88bbff; }
.reply-modal textarea {
  width: 100%; padding: 12px; border-radius: 10px;
  background: rgba(5,12,26,0.6); border: 1px solid rgba(144,166,196,0.25);
  color: #e8eef7; font-family: inherit; font-size: 0.9rem;
  outline: none; resize: none;
}
.reply-modal textarea:focus { border-color: rgba(144,166,196,0.5); }
.reply-modal-btns { display: flex; gap: 10px; justify-content: flex-end; margin-top: 14px; }
.cancel-btn {
  padding: 8px 18px; border-radius: 18px;
  background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.25);
  color: #a8bcd4; cursor: pointer; font-size: 0.85rem;
}
.cancel-btn:hover { background: rgba(144,166,196,0.2); }
.submit-btn {
  padding: 8px 20px; border-radius: 18px; border: none;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: #fff; cursor: pointer; font-size: 0.85rem; transition: 0.3s;
}
.submit-btn:hover { box-shadow: 0 4px 14px rgba(144,166,196,0.4); }
</style>