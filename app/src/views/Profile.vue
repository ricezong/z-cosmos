<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon cosmic">
          <div class="cosmic-halo"></div>
          <div class="cosmic-corona"></div>
          <div class="planet-sphere profile-planet"></div>
        </div>
        <div class="header-title">
          <h1>个人主页</h1>
          <p>星际枢纽 · 你的空间</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>

    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn" class="login-prompt-fixed">
      <div class="prompt-icon"><i class="ri-lock-line"></i></div>
      <h2>请先登录</h2>
      <p>登录后即可查看个人主页</p>
      <router-link to="/login" class="login-link"><i class="ri-login-box-line"></i> 去登录</router-link>
      <router-link to="/" class="back-home-link"><i class="ri-arrow-left-line"></i> 返回首页</router-link>
    </div>

    <!-- 个人信息卡（固定区） -->
    <div class="profile-hero-wrap" v-if="isLoggedIn && !showEdit">
    <div class="profile-hero">
        <div class="hero-bg"></div>
        <div class="hero-content">
          <div class="avatar-wrap">
            <div class="avatar" :style="avatarStyle">
              <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="avatar" class="avatar-img">
              <span v-else class="avatar-text">{{ (profile.nickname || 'U').charAt(0).toUpperCase() }}</span>
            </div>
            <div class="avatar-badge" v-if="profile.gender === 1"><i class="ri-men-line"></i></div>
            <div class="avatar-badge female" v-else-if="profile.gender === 2"><i class="ri-women-line"></i></div>
          </div>
          <div class="hero-info">
            <h2 class="hero-name">{{ profile.nickname || '星际旅人' }}</h2>
            <p class="hero-bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
            <div class="hero-location" v-if="profile.location">
              <i class="ri-map-pin-line"></i>
              <span>{{ profile.location }}</span>
            </div>
          </div>
          <div class="hero-actions">
            <button class="action-edit" @click="toggleEdit"><i class="ri-edit-line"></i> 编辑资料</button>
            <button class="action-logout" @click="doLogout"><i class="ri-logout-box-r-line"></i></button>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-item" @click="profileTab = 'posts'; loadMyPosts()">
            <div class="stat-num">{{ profile.postCount || 0 }}</div>
            <div class="stat-label">帖子</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">{{ profile.likeCount || 0 }}</div>
            <div class="stat-label">获赞</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">{{ profile.collectCount || 0 }}</div>
            <div class="stat-label">收藏</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">{{ profile.followerCount || 0 }}</div>
            <div class="stat-label">粉丝</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs（固定区） -->
    <div class="content-tabs" v-if="isLoggedIn && !showEdit">
      <div class="content-tab" :class="{ active: profileTab === 'posts' }" @click="profileTab = 'posts'; loadMyPosts()">
        <i class="ri-file-text-line"></i> 我的帖子
      </div>
      <div class="content-tab" :class="{ active: profileTab === 'favorites' }" @click="profileTab = 'favorites'; loadMyCollections()">
        <i class="ri-star-line"></i> 我的收藏
      </div>
    </div>
    </div><!-- page-fixed -->

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="profile-container">

    <template v-if="isLoggedIn">
      <!-- 编辑资料面板 -->
      <div class="edit-panel" v-if="showEdit">
        <div class="edit-card">
          <div class="edit-card-header">
            <h3><i class="ri-edit-line"></i> 编辑资料</h3>
            <button class="edit-close" @click="toggleEdit"><i class="ri-close-line"></i></button>
          </div>
          <div class="edit-form">
            <div class="form-field">
              <label class="field-label">头像</label>
              <div class="avatar-upload">
                <div class="upload-avatar" :style="avatarStyle">
                  <img v-if="editForm.avatarUrl" :src="editForm.avatarUrl" alt="avatar" class="avatar-img">
                  <span v-else class="avatar-text">{{ (editForm.nickname || 'U').charAt(0).toUpperCase() }}</span>
                </div>
                <label class="upload-btn">
                  <i class="ri-camera-line"></i> 更换头像
                  <input type="file" accept="image/*" @change="handleAvatarUpload" style="display:none">
                </label>
              </div>
            </div>
            <div class="form-field">
              <label class="field-label">昵称</label>
              <input v-model="editForm.nickname" maxlength="20" class="field-input" placeholder="输入昵称">
            </div>
            <div class="form-field">
              <label class="field-label">性别</label>
              <div class="gender-group">
                <button class="gender-btn" :class="{ active: editForm.gender === 0 }" @click="editForm.gender = 0">未知</button>
                <button class="gender-btn" :class="{ active: editForm.gender === 1 }" @click="editForm.gender = 1"><i class="ri-men-line"></i> 男</button>
                <button class="gender-btn" :class="{ active: editForm.gender === 2 }" @click="editForm.gender = 2"><i class="ri-women-line"></i> 女</button>
              </div>
            </div>
            <div class="form-field">
              <label class="field-label">个人简介</label>
              <textarea v-model="editForm.bio" maxlength="200" class="field-textarea" placeholder="介绍一下自己..." rows="3"></textarea>
              <div class="field-counter">{{ (editForm.bio || '').length }}/200</div>
            </div>
            <div class="form-field">
              <label class="field-label">所在地区</label>
              <input v-model="editForm.location" maxlength="50" class="field-input" placeholder="所在城市">
            </div>
            <div class="form-field">
              <label class="field-label">生日</label>
              <input type="date" v-model="editForm.birthday" class="field-input" ref="birthdayInput">
            </div>
            <div class="form-actions">
              <button class="btn-cancel" @click="toggleEdit">取消</button>
              <button class="btn-save" :disabled="saving" @click="saveProfile">
                <i class="ri-save-line"></i> {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 我的帖子 -->
      <div class="content-panel" v-if="!showEdit && profileTab === 'posts'">
        <LoadingSpinner v-if="postsLoading" text="加载中..." />
        <div class="post-list" v-else-if="myPosts.length > 0">
          <div class="post-card" v-for="post in myPosts" :key="post.postId" @click="openPost(post.postId)">
            <div class="post-card-main">
              <div class="post-card-title">{{ post.title }}</div>
              <div class="post-card-desc">{{ post.summary || '' }}</div>
              <div class="post-card-meta">
                <span class="post-tag">{{ post.category?.name || '未分类' }}</span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-card-stats">
              <span><i class="ri-eye-line"></i> {{ post.viewCount || 0 }}</span>
              <span><i class="ri-chat-3-line"></i> {{ post.commentCount || 0 }}</span>
              <span><i class="ri-heart-3-line"></i> {{ post.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <div class="empty-icon"><i class="ri-file-text-line"></i></div>
          <p>还没有发布过帖子</p>
          <router-link to="/community/new" class="empty-action"><i class="ri-edit-line"></i> 去发帖</router-link>
        </div>
      </div>

      <!-- 我的收藏 -->
      <div class="content-panel" v-if="!showEdit && profileTab === 'favorites'">
        <LoadingSpinner v-if="collectionsLoading" text="加载中..." />
        <div class="post-list" v-else-if="myCollections.length > 0">
          <div class="post-card" v-for="post in myCollections" :key="post.postId" @click="openPost(post.postId)">
            <div class="post-card-main">
              <div class="post-card-title">{{ post.title }}</div>
              <div class="post-card-desc">{{ post.summary || '' }}</div>
              <div class="post-card-meta">
                <span class="post-tag">{{ post.category?.name || '未分类' }}</span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-card-stats">
              <span><i class="ri-eye-line"></i> {{ post.viewCount || 0 }}</span>
              <span><i class="ri-chat-3-line"></i> {{ post.commentCount || 0 }}</span>
              <span><i class="ri-heart-3-line"></i> {{ post.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <div class="empty-icon"><i class="ri-star-line"></i></div>
          <p>还没有收藏的帖子</p>
          <router-link to="/community" class="empty-action"><i class="ri-arrow-right-line"></i> 去社区逛逛</router-link>
        </div>
      </div>
    </template>

    </div>
    </div>
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, updateProfile, uploadAvatar, listMyPosts, listMyCollections } from '../api/profile'
import { logout as apiLogout } from '../api/auth'
import { useAuth } from '../composables/useAuth'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()
const toast = useToast()
const { showConfirm } = useConfirm()
const { isLoggedIn } = useAuth()

const profileTab = ref('posts')
const showEdit = ref(false)
const saving = ref(false)
const birthdayInput = ref(null)

const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

const profile = ref({
  nickname: '', bio: '', avatarUrl: '', gender: 0,
  location: '', birthday: '', postCount: 0,
  collectCount: 0, likeCount: 0, followerCount: 0
})

const editForm = ref({
  nickname: '', bio: '', avatarUrl: '', gender: 0, location: '', birthday: ''
})

const myPosts = ref([])
const myCollections = ref([])
const postsLoading = ref(false)
const collectionsLoading = ref(false)

const avatarStyle = computed(() => {
  if (profile.value.avatarUrl) return {}
  const colors = [
    'linear-gradient(135deg, #7c5ce0, #a78bfa)',
    'linear-gradient(135deg, #3b82f6, #60a5fa)',
    'linear-gradient(135deg, #ec4899, #f472b6)',
    'linear-gradient(135deg, #14b8a6, #5eead4)',
    'linear-gradient(135deg, #f59e0b, #fbbf24)'
  ]
  const idx = ((profile.value.nickname || 'U').charCodeAt(0)) % colors.length
  return { background: colors[idx] }
})

async function loadProfile() {
  try {
    const data = await getCurrentUser()
    if (data) {
      profile.value = { ...profile.value, ...data }
      editForm.value = {
        nickname: data.nickname || '', bio: data.bio || '', avatarUrl: data.avatarUrl || '',
        gender: data.gender || 0, location: data.location || '', birthday: data.birthday || ''
      }
    }
  } catch (e) {
    console.error('加载用户信息失败:', e)
  }
}

async function loadMyPosts(page = 1) {
  postsLoading.value = true
  try {
    const data = await listMyPosts({ page, size: 20 })
    myPosts.value = data?.records || []
  } catch (e) {
    console.error('加载我的帖子失败:', e)
    myPosts.value = []
  } finally {
    postsLoading.value = false
  }
}

async function loadMyCollections(page = 1) {
  collectionsLoading.value = true
  try {
    const data = await listMyCollections({ page, size: 20 })
    myCollections.value = data?.records || []
  } catch (e) {
    console.error('加载我的收藏失败:', e)
    myCollections.value = []
  } finally {
    collectionsLoading.value = false
  }
}

function toggleEdit() {
  showEdit.value = !showEdit.value
  if (showEdit.value) {
    editForm.value = {
      nickname: profile.value.nickname || '', bio: profile.value.bio || '',
      avatarUrl: profile.value.avatarUrl || '', gender: profile.value.gender || 0,
      location: profile.value.location || '', birthday: profile.value.birthday || ''
    }
  }
}

async function doLogout() {
  if (!await showConfirm('退出登录', '确定要退出登录吗？')) return
  try { await apiLogout() } catch (e) {}
  toast.success('已退出登录')
  router.push('/')
}

async function saveProfile() {
  if (!editForm.value.nickname.trim()) { toast.warning('昵称不能为空'); return }
  saving.value = true
  try {
    await updateProfile({
      nickname: editForm.value.nickname.trim(), bio: editForm.value.bio?.trim() || '',
      gender: editForm.value.gender, location: editForm.value.location?.trim() || '',
      birthday: editForm.value.birthday || null
    })
    await loadProfile()
    showEdit.value = false
    toast.success('资料已更新')
  } catch (e) {
    toast.error('更新失败: ' + (e.message || '请重试'))
  } finally {
    saving.value = false
  }
}

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { toast.warning('图片不能超过5MB'); return }
  if (!file.type.startsWith('image/')) { toast.warning('请选择图片文件'); return }
  try {
    const url = await uploadAvatar(file)
    editForm.value.avatarUrl = url
    profile.value.avatarUrl = url
  } catch (e) {
    toast.error('头像上传失败: ' + (e.message || '请重试'))
  }
  e.target.value = ''
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

function openPost(postId) {
  router.push(`/community/post/${postId}`)
}

watch(isLoggedIn, async (loggedIn) => {
  if (loggedIn) await Promise.allSettled([loadProfile(), loadMyPosts()])
}, { immediate: true })
</script>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(255,255,255,0.2); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.25); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.2); box-shadow: 0 0 15px rgba(144,166,196,0.2); }


.profile-container { position: relative; z-index: 1; max-width: 800px; margin: 0 auto; padding: 20px 20px; }

/* Profile Hero Card */
.profile-hero-wrap, .content-tabs {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px 20px 0;
}
.profile-hero {
  border: 1px solid rgba(144,166,196,0.15);
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 20px;
  background: rgba(15,22,44,0.5);
  backdrop-filter: blur(12px);
  position: relative;
}
.hero-bg {
  position: absolute; top: 0; left: 0; right: 0; height: 100px;
  background: linear-gradient(135deg, rgba(120,100,180,0.3) 0%, rgba(100,140,200,0.2) 50%, rgba(140,120,200,0.15) 100%);
}
.hero-content {
  position: relative; display: flex; align-items: flex-start; gap: 18px;
  padding: 24px 24px 0;
}
.avatar-wrap { position: relative; flex-shrink: 0; }
.avatar {
  width: 80px; height: 80px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; font-weight: bold; color: #fff;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
  border: 3px solid rgba(15,22,44,0.8);
  overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-text { text-shadow: 0 2px 8px rgba(0,0,0,0.3); }
.avatar-badge {
  position: absolute; bottom: 2px; right: 2px;
  width: 22px; height: 22px; border-radius: 50%;
  background: #3b82f6; display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: #fff; border: 2px solid rgba(15,22,44,0.9);
}
.avatar-badge.female { background: #ec4899; }
.hero-info { flex: 1; min-width: 0; padding-top: 4px; }
.hero-name { font-size: 1.3rem; font-weight: 600; color: #fff; margin-bottom: 6px; }
.hero-bio { font-size: 0.85rem; color: rgba(168,188,212,0.7); line-height: 1.5; margin-bottom: 6px; }
.hero-location { display: inline-flex; align-items: center; gap: 4px; font-size: 0.78rem; color: rgba(168,188,212,0.5); }
.hero-location i { font-size: 0.85rem; }
.hero-actions { display: flex; gap: 8px; flex-shrink: 0; padding-top: 4px; }
.action-edit {
  padding: 7px 16px; border-radius: 8px;
  border: 1px solid rgba(200,180,255,0.3);
  background: rgba(200,180,255,0.1);
  color: rgba(220,200,255,0.9); cursor: pointer;
  font-size: 0.82rem; transition: 0.2s;
  font-family: inherit;
}
.action-edit:hover { background: rgba(200,180,255,0.2); border-color: rgba(200,180,255,0.5); }
.action-logout {
  width: 34px; height: 34px; border-radius: 8px;
  border: 1px solid rgba(200,180,255,0.2);
  background: rgba(200,180,255,0.05);
  color: rgba(200,180,255,0.6); cursor: pointer;
  font-size: 1rem; display: flex; align-items: center; justify-content: center;
  transition: 0.2s;
}
.action-logout:hover { background: var(--status-error-glow); color: rgba(205,105,135,0.9); border-color: var(--status-error-border); }

.hero-stats {
  position: relative; display: flex; align-items: center; justify-content: center;
  padding: 12px 24px 16px; gap: 0;
  border-top: 1px solid rgba(144,166,196,0.08);
}
.stat-item {
  flex: 1; text-align: center; cursor: pointer;
  padding: 2px 0; transition: 0.2s;
}
.stat-item:hover { opacity: 0.8; }
.stat-num { font-size: 1.1rem; font-weight: 600; color: #fff; margin-bottom: 1px; }
.stat-label { font-size: 0.7rem; color: rgba(168,188,212,0.5); letter-spacing: 0.5px; }
.stat-divider { width: 1px; height: 24px; background: rgba(144,166,196,0.1); }

/* Edit Panel */
.edit-panel { margin-bottom: 20px; }
.edit-card {
  border: 1px solid rgba(144,166,196,0.15);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(12px);
}
.edit-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(144,166,196,0.08);
}
.edit-card-header h3 { font-size: 0.95rem; font-weight: 500; color: #e8eef7; display: flex; align-items: center; gap: 8px; }
.edit-card-header h3 i { font-size: 1.05rem; opacity: 0.7; }
.edit-close {
  width: 32px; height: 32px; border-radius: 8px;
  border: none; background: rgba(144,166,196,0.08);
  color: #a8bcd4; cursor: pointer; font-size: 1.1rem;
  display: flex; align-items: center; justify-content: center;
  transition: 0.2s;
}
.edit-close:hover { background: rgba(144,166,196,0.15); color: #e8eef7; }
.edit-form { padding: 20px 24px; }
.form-field { margin-bottom: 18px; }
.field-label { display: block; margin-bottom: 8px; font-size: 0.82rem; color: #a8bcd4; letter-spacing: 0.3px; }
.field-input {
  width: 100%; padding: 10px 14px; border-radius: 10px;
  background: rgba(5,12,26,0.5); border: 1px solid rgba(144,166,196,0.18);
  color: #e8eef7; font-family: inherit; font-size: 0.9rem;
  outline: none; transition: 0.2s; box-sizing: border-box;
}
.field-input:focus { border-color: rgba(144,166,196,0.4); box-shadow: 0 0 0 2px rgba(144,166,196,0.08); }
.field-textarea {
  width: 100%; padding: 10px 14px; border-radius: 10px;
  background: rgba(5,12,26,0.5); border: 1px solid rgba(144,166,196,0.18);
  color: #e8eef7; font-family: inherit; font-size: 0.9rem;
  outline: none; resize: vertical; min-height: 80px;
  transition: 0.2s; box-sizing: border-box;
}
.field-textarea:focus { border-color: rgba(144,166,196,0.4); box-shadow: 0 0 0 2px rgba(144,166,196,0.08); }
.field-counter { text-align: right; font-size: 0.7rem; color: rgba(168,188,212,0.3); margin-top: 4px; }

.avatar-upload { display: flex; align-items: center; gap: 16px; }
.upload-avatar {
  width: 64px; height: 64px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; font-weight: bold; color: #fff;
  overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.2);
}
.upload-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px;
  border: 1px dashed rgba(144,166,196,0.3);
  background: rgba(144,166,196,0.04);
  color: #a8bcd4; cursor: pointer;
  font-size: 0.82rem; transition: 0.2s;
}
.upload-btn:hover { border-color: rgba(144,166,196,0.5); background: rgba(144,166,196,0.08); color: #e8eef7; }

.gender-group { display: flex; gap: 8px; }
.gender-btn {
  padding: 7px 16px; border-radius: 8px;
  border: 1px solid rgba(144,166,196,0.18);
  background: rgba(144,166,196,0.04);
  color: #a8bcd4; cursor: pointer;
  font-size: 0.82rem; transition: 0.2s;
  font-family: inherit; display: inline-flex; align-items: center; gap: 4px;
}
.gender-btn:hover { background: rgba(144,166,196,0.1); }
.gender-btn.active {
  background: rgba(144,166,196,0.15);
  border-color: rgba(144,166,196,0.4);
  color: #e8eef7;
}

.form-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 24px; padding-top: 18px; border-top: 1px solid rgba(144,166,196,0.08); }
.btn-cancel {
  padding: 9px 20px; border-radius: 10px;
  border: 1px solid rgba(144,166,196,0.18);
  background: transparent; color: #a8bcd4;
  cursor: pointer; font-size: 0.85rem; transition: 0.2s;
  font-family: inherit;
}
.btn-cancel:hover { background: rgba(144,166,196,0.08); }
.btn-save {
  padding: 9px 24px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #7890b5, #a8bcd4);
  color: #fff; cursor: pointer; font-size: 0.85rem;
  transition: 0.25s; font-family: inherit;
  display: inline-flex; align-items: center; gap: 6px;
}
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-save:not(:disabled):hover { box-shadow: 0 4px 16px rgba(144,166,196,0.35); transform: translateY(-1px); }

/* Content Tabs */
.content-tabs {
  display: flex; gap: 0;
  border-bottom: 1px solid rgba(144,166,196,0.1);
  padding: 0 20px;
}
.content-tab {
  padding: 12px 20px; cursor: pointer;
  font-size: 0.88rem; color: rgba(168,188,212,0.5);
  border-bottom: 2px solid transparent;
  transition: 0.2s;
  display: inline-flex; align-items: center; gap: 6px;
}
.content-tab:hover { color: #e8eef7; }
.content-tab.active {
  color: #e8eef7;
  border-bottom-color: rgba(168,188,212,0.6);
}

/* Post List */
.content-panel { min-height: 200px; }
.post-list { display: flex; flex-direction: column; gap: 10px; }
.post-card {
  display: flex; align-items: center; gap: 16px;
  border: 1px solid rgba(144,166,196,0.1);
  border-radius: 12px; padding: 16px 18px;
  cursor: pointer; transition: 0.2s;
}
.post-card:hover { border-color: rgba(144,166,196,0.25); background: rgba(144,166,196,0.03); }
.post-card-main { flex: 1; min-width: 0; }
.post-card-title { font-size: 0.95rem; color: #e8eef7; font-weight: 500; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.post-card-desc { font-size: 0.78rem; color: rgba(168,188,212,0.45); margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.post-card-meta { display: flex; align-items: center; gap: 10px; }
.post-tag {
  padding: 2px 8px; border-radius: 6px;
  font-size: 0.68rem; background: rgba(144,166,196,0.08);
  color: rgba(168,188,212,0.5); border: 1px solid rgba(144,166,196,0.12);
}
.post-time { font-size: 0.7rem; color: rgba(168,188,212,0.3); }
.post-card-stats { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }
.post-card-stats span { font-size: 0.72rem; color: rgba(168,188,212,0.35); display: flex; align-items: center; gap: 4px; white-space: nowrap; }
.post-card-stats i { font-size: 0.78rem; }

/* Empty State */
.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 2.5rem; color: rgba(168,188,212,0.15); margin-bottom: 16px; }
.empty-state p { color: rgba(168,188,212,0.35); font-size: 0.88rem; margin-bottom: 16px; }
.empty-action {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 20px; border-radius: 10px;
  background: rgba(144,166,196,0.08);
  border: 1px solid rgba(144,166,196,0.15);
  color: #a8bcd4; text-decoration: none;
  font-size: 0.82rem; transition: 0.2s;
}
.empty-action:hover { background: rgba(144,166,196,0.15); color: #e8eef7; }

/* Login Prompt */
.login-prompt-fixed { text-align: center; padding: 60px 20px; }
.login-prompt-fixed .prompt-icon,
.login-prompt .prompt-icon { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 24px; display: flex; align-items: center; justify-content: center; background: rgba(180,160,240,0.06); border: 1px solid rgba(180,160,240,0.12); }
.login-prompt-fixed .prompt-icon i,
.login-prompt .prompt-icon i { font-size: 2rem; color: rgba(180,160,240,0.35); }
.login-prompt-fixed h2,
.login-prompt h2 { font-size: 1.3rem; font-weight: 500; margin-bottom: 8px; color: #e8eef7; }
.login-prompt-fixed p,
.login-prompt p { color: rgba(239,236,251,0.4); margin-bottom: 28px; font-size: 0.88rem; }
.login-prompt { text-align: center; padding: 80px 20px; }
.prompt-icon { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 24px; display: flex; align-items: center; justify-content: center; background: rgba(180,160,240,0.06); border: 1px solid rgba(180,160,240,0.12); }
.prompt-icon i { font-size: 2rem; color: rgba(180,160,240,0.35); }
.login-prompt h2 { font-size: 1.3rem; font-weight: 500; margin-bottom: 8px; color: #e8eef7; }
.login-prompt p { color: rgba(239,236,251,0.4); margin-bottom: 28px; font-size: 0.88rem; }
.login-link { display: inline-block; padding: 10px 32px; border-radius: 12px; background: linear-gradient(135deg, rgba(120,100,200,0.7), rgba(180,160,240,0.8)); color: #fff; text-decoration: none; font-size: 0.92rem; transition: 0.3s; margin-bottom: 14px; }
.login-link:hover { box-shadow: 0 4px 18px rgba(180,160,240,0.35); filter: brightness(1.1); }
.back-home-link { display: block; color: rgba(239,236,251,0.4); text-decoration: none; font-size: 0.85rem; transition: 0.3s; }
.back-home-link:hover { color: var(--ink-100); }
</style>