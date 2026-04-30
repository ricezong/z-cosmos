<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-sphere"></div>
          <div>
            <h1>个人主页</h1>
            <p>太阳 · 你的空间</p>
          </div>
        </div>
        <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
      </header>
      <div class="container" v-if="isLoggedIn">
        <section class="profile-card">
          <div class="avatar">
            <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="avatar">
            <span v-else>{{ (profile.nickname || 'C').slice(0, 1).toUpperCase() }}</span>
          </div>
          <div class="profile-info">
            <h2>{{ profile.nickname }}</h2>
            <p>{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
            <div class="stats">
              <span><i class="ri-file-text-line"></i> {{ myPosts.length }} 帖子</span>
              <span><i class="ri-star-line"></i> {{ favPosts.length }} 收藏</span>
            </div>
          </div>
          <div class="actions">
            <button @click="toggleEdit"><i class="ri-edit-line"></i> 编辑资料</button>
            <button @click="togglePasswordEdit"><i class="ri-lock-line"></i> 修改密码</button>
            <button @click="avatarInput?.click()"><i class="ri-image-edit-line"></i> 上传头像</button>
            <button class="danger" @click="doLogout"><i class="ri-logout-box-r-line"></i> 退出</button>
          </div>
          <input ref="avatarInput" type="file" accept="image/*" hidden @change="uploadAvatarFile">
        </section>
        <div class="tabs" v-if="!showEdit && !showPasswordEdit">
          <button :class="{ active: profileTab === 'posts' }" @click="profileTab = 'posts'">我的帖子</button>
          <button :class="{ active: profileTab === 'favorites' }" @click="profileTab = 'favorites'">我的收藏</button>
        </div>
      </div>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <main class="container" v-if="isLoggedIn">
        <section class="card form" v-if="showEdit">
          <h3>编辑资料</h3>
          <label>昵称</label>
          <input v-model="editForm.nickname" maxlength="20">
          <label>个人简介</label>
          <textarea v-model="editForm.bio" maxlength="200" rows="4"></textarea>
          <button class="primary" @click="saveProfile">保存</button>
        </section>

        <section class="card form" v-if="showPasswordEdit">
          <h3>修改密码</h3>
          <label>当前密码</label>
          <input type="password" v-model="passwordForm.oldPwd">
          <label>新密码</label>
          <input type="password" v-model="passwordForm.newPwd">
          <label>确认新密码</label>
          <input type="password" v-model="passwordForm.confirmPwd">
          <button class="primary" @click="savePassword">修改密码</button>
        </section>

        <section v-if="!showEdit && !showPasswordEdit" class="stack">
          <article class="post-card" v-for="post in visiblePosts" :key="post.id" @click="openPost(post.id)">
            <div class="meta">
              <span>{{ categoryLabel(post.category) }}</span>
              <span>{{ formatTime(post.time) }}</span>
            </div>
            <h2>{{ post.title }}</h2>
            <p>{{ post.content }}</p>
            <div class="stats">
              <span><i class="ri-eye-line"></i> {{ post.views }}</span>
              <span><i class="ri-chat-3-line"></i> {{ countComments(post.comments) }}</span>
              <span><i class="ri-heart-3-line"></i> {{ post.likes || 0 }}</span>
            </div>
          </article>
          <div v-if="!visiblePosts.length" class="empty">{{ profileTab === 'posts' ? '还没有发布过帖子' : '还没有收藏的帖子' }}</div>
        </section>
      </main>

      <main class="container" v-else>
        <section class="login-prompt">
          <i class="ri-lock-line"></i>
          <h2>请先登录</h2>
          <p>登录后即可查看个人主页</p>
          <router-link to="/login" class="login-link">去登录</router-link>
        </section>
      </main>
    </div>
    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { logout as apiLogout } from '../api/auth'
import { isApiLoggedIn } from '../api/http'
import { changePassword as apiChangePassword, getCurrentUser as apiGetCurrentUser, listMyCollections, listMyPosts, updateProfile as apiUpdateProfile, uploadAvatar } from '../api/profile'

const router = useRouter()
const profileTab = ref('posts')
const showEdit = ref(false)
const showPasswordEdit = ref(false)
const isLoggedIn = ref(false)
const profile = ref({ id: '', nickname: '星际旅人', bio: '' })
const editForm = ref({ nickname: '', bio: '' })
const passwordForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' })
const myPosts = ref([])
const favPosts = ref([])
const avatarInput = ref(null)
const visiblePosts = computed(() => profileTab.value === 'posts' ? myPosts.value : favPosts.value)

const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

function mapPost(post) {
  return {
    id: post.postId,
    category: post.category?.code || 'tech',
    title: post.title || '',
    content: post.summary || '',
    time: post.createdAt,
    views: post.viewCount || 0,
    likes: post.likeCount || 0,
    comments: Array.from({ length: post.commentCount || 0 }, (_, i) => ({ id: i }))
  }
}

async function loadProfile() {
  const user = await apiGetCurrentUser()
  profile.value = {
    id: String(user.userId || user.id || ''),
    nickname: user.nickname || 'COSMOS',
    bio: user.bio || '',
    avatarUrl: user.avatarUrl || ''
  }
  editForm.value = { nickname: profile.value.nickname, bio: profile.value.bio }
  const [postsPage, favPage] = await Promise.all([
    listMyPosts({ page: 1, size: 50 }),
    listMyCollections({ page: 1, size: 50 })
  ])
  myPosts.value = (postsPage.records || []).map(mapPost)
  favPosts.value = (favPage.records || []).map(mapPost)
}

function toggleEdit() {
  showEdit.value = !showEdit.value
  if (showEdit.value) showPasswordEdit.value = false
}
function togglePasswordEdit() {
  showPasswordEdit.value = !showPasswordEdit.value
  if (showPasswordEdit.value) showEdit.value = false
}

async function doLogout() {
  await apiLogout()
  router.push('/')
}

async function saveProfile() {
  if (!editForm.value.nickname.trim()) {
    alert('昵称不能为空')
    return
  }
  await apiUpdateProfile({ nickname: editForm.value.nickname.trim(), bio: editForm.value.bio.trim() })
  profile.value.nickname = editForm.value.nickname.trim()
  profile.value.bio = editForm.value.bio.trim()
  showEdit.value = false
}

async function savePassword() {
  const { oldPwd, newPwd, confirmPwd } = passwordForm.value
  if (!oldPwd || !newPwd) {
    alert('请填写密码')
    return
  }
  if (newPwd !== confirmPwd) {
    alert('两次输入的新密码不一致')
    return
  }
  await apiChangePassword({ oldPassword: oldPwd, newPassword: newPwd })
  passwordForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
  showPasswordEdit.value = false
}

async function uploadAvatarFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  try {
    const url = await uploadAvatar(file)
    profile.value.avatarUrl = url
    alert('头像已更新')
  } catch (e) {
    alert(e.message || '头像上传失败')
  }
}

const categoryNames = { tech: '技术', life: '生活', film: '影视', travel: '旅行', gaming: '游戏', art: '艺术' }
function categoryLabel(id) { return categoryNames[id] || id }
function countComments(comments) { return comments?.length || 0 }
function formatTime(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
function openPost(postId) {
  router.push({ path: '/community', query: { postId } })
}

onMounted(async () => {
  isLoggedIn.value = isApiLoggedIn()
  if (!isLoggedIn.value) return
  try {
    await loadProfile()
  } catch (e) {
    console.warn('Load profile failed', e)
  }
})
</script>

<style scoped>
.page-layout { --bt-bg: rgba(204,177,131,0.25); --bt-border: rgba(204,177,131,0.45); --bt-color: #fff; --bt-hover-bg: rgba(204,177,131,0.45); --bt-shadow: rgba(204,177,131,0.3); }
.container { position: relative; z-index: 1; max-width: 860px; margin: 0 auto; padding: 20px; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid rgba(255,255,255,0.2); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #fff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; background: radial-gradient(circle at 35% 35%, #f6deab, #e7bc6b 40%, #7a5224); box-shadow: 0 0 22px rgba(230,175,95,0.45); }
.back-btn, button { border: 1px solid rgba(144,166,196,0.25); background: rgba(144,166,196,0.08); color: #c5d5ea; cursor: pointer; transition: 0.25s; }
.back-btn { padding: 8px 20px; border-radius: 30px; text-decoration: none; }
button { padding: 8px 16px; border-radius: 20px; }
button.active, button:hover, .back-btn:hover { background: rgba(144,166,196,0.18); color: #fff; }
.danger { border-color: rgba(255,80,80,0.35); color: #ff9999; }
.profile-card, .card, .post-card { border: 1px solid rgba(144,166,196,0.18); border-radius: 18px; padding: 22px; background: rgba(12,18,34,0.45); backdrop-filter: blur(10px); }
.profile-card { display: flex; align-items: center; gap: 18px; flex-wrap: wrap; }
.avatar { width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #d4b78c, #b89a6e); color: #fff; font-size: 1.8rem; }
.avatar img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; display: block; }
.profile-info { flex: 1; min-width: 220px; }
.profile-info h2 { color: #fff; margin-bottom: 4px; }
.profile-info p { opacity: 0.65; }
.actions, .stats, .tabs { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
.stats { margin-top: 8px; opacity: 0.72; font-size: 0.86rem; }
.tabs { margin-top: 14px; }
.stack { display: flex; flex-direction: column; gap: 12px; }
.post-card { cursor: pointer; transition: 0.25s; }
.post-card:hover { transform: translateY(-2px); border-color: rgba(144,166,196,0.35); }
.post-card h2 { color: #fff; margin: 8px 0; }
.post-card p { opacity: 0.68; line-height: 1.55; }
.meta { display: flex; justify-content: space-between; gap: 12px; color: #a8bcd4; font-size: 0.82rem; }
.form { display: flex; flex-direction: column; gap: 10px; }
input, textarea { width: 100%; box-sizing: border-box; padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(144,166,196,0.25); background: rgba(5,12,26,0.72); color: #e8eef7; font-family: inherit; }
.primary { width: fit-content; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: #fff; border: none; }
.empty, .login-prompt { text-align: center; padding: 70px 20px; opacity: 0.7; }
.login-prompt > i { font-size: 3rem; display: block; margin-bottom: 16px; }
.login-link { display: inline-block; margin-top: 18px; padding: 10px 28px; border-radius: 24px; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: #fff; text-decoration: none; }
@media (max-width: 768px) { .container { padding: 15px 12px 60px; } .header { flex-direction: column; align-items: flex-start; gap: 12px; } .header h1 { font-size: 1.4rem; } }
</style>
