<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere sun"></div></div>
        <div class="header-title">
          <h1>个人主页</h1>
          <p>星际枢纽 · 你的空间</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>

    <div class="container" v-if="isLoggedIn">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar" :style="avatarStyle">
          <span v-if="!profile.avatar">{{ profile.nickname.charAt(0).toUpperCase() }}</span>
          <i v-else :class="profile.avatar"></i>
        </div>
        <div class="profile-info">
          <h2>{{ profile.nickname }}</h2>
          <p class="profile-bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
          <div class="profile-stats">
            <span><i class="ri-file-text-line"></i> {{ myPosts.length }} 帖子</span>
            <span><i class="ri-heart-3-line"></i> {{ profile.favorites?.length || 0 }} 收藏</span>
          </div>
        </div>
        <div class="action-btns">
          <button v-if="!showEdit && !showPasswordEdit" class="edit-btn" @click="toggleEdit">
            <i class="ri-edit-line"></i> 编辑资料
          </button>
          <button v-if="showEdit" class="edit-btn" @click="toggleEdit">
            <i class="ri-close-line"></i> 取消
          </button>
          <button v-if="!showEdit && !showPasswordEdit" class="edit-btn" @click="togglePasswordEdit">
            <i class="ri-lock-line"></i> 修改密码
          </button>
          <button v-if="showPasswordEdit" class="edit-btn" @click="togglePasswordEdit">
            <i class="ri-close-line"></i> 取消
          </button>
          <button v-if="!showEdit && !showPasswordEdit" class="logout-btn" @click="doLogout">
            <i class="ri-logout-box-r-line"></i> 退出登录
          </button>
        </div>
      </div>

      <!-- 编辑资料面板 -->
      <div class="edit-panel" v-show="showEdit">
        <div class="form-group">
          <label>头像</label>
          <div class="avatar-picker">
            <div class="avatar-option" v-for="opt in avatarOptions" :key="opt.icon"
              :class="{ active: editForm.avatar === opt.icon }"
              @click="editForm.avatar = opt.icon">
              <i :class="opt.icon"></i>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>昵称</label>
          <input v-model="editForm.nickname" maxlength="20" placeholder="输入昵称">
        </div>
        <div class="form-group">
          <label>个人简介</label>
          <textarea v-model="editForm.bio" maxlength="200" placeholder="介绍一下自己..." rows="3"></textarea>
        </div>
        <button class="save-btn" @click="saveProfile"><i class="ri-save-line"></i> 保存修改</button>
      </div>

      <!-- 修改密码面板 -->
      <div class="edit-panel" v-show="showPasswordEdit">
        <h4 class="section-title"><i class="ri-lock-line"></i> 修改密码</h4>
        <div class="form-group">
          <label>当前密码</label>
          <input type="password" v-model="passwordForm.oldPwd" placeholder="输入当前密码">
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" v-model="passwordForm.newPwd" placeholder="输入新密码">
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" v-model="passwordForm.confirmPwd" placeholder="再次输入新密码">
        </div>
        <button class="save-btn" @click="savePassword"><i class="ri-lock-line"></i> 修改密码</button>
      </div>
    </div>

    <!-- Tab切换（编辑模式下隐藏） -->
    <div class="tabs" v-show="!showEdit && !showPasswordEdit">
      <div class="tab" :class="{ active: profileTab === 'posts' }" @click="profileTab = 'posts'"><i class="ri-file-text-line"></i> 我的帖子</div>
      <div class="tab" :class="{ active: profileTab === 'favorites' }" @click="profileTab = 'favorites'"><i class="ri-heart-3-line"></i> 我的收藏</div>
    </div>
    </div><!-- container v-if -->

    <!-- 未登录提示 -->
    <div class="container" v-else>
      <div class="login-prompt">
        <i class="ri-lock-line"></i>
        <h2>请先登录</h2>
        <p>登录后即可查看个人主页</p>
        <router-link to="/login" class="login-link"><i class="ri-login-box-line"></i> 去登录</router-link>
        <router-link to="/" class="back-home-link"><i class="ri-arrow-left-line"></i> 返回首页</router-link>
      </div>
    </div>
    </div><!-- page-fixed -->

    <!-- 滚动区域：帖子/收藏列表 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <!-- 我的帖子（编辑模式下隐藏） -->
    <div class="panel" v-show="profileTab === 'posts' && !showEdit && !showPasswordEdit">
      <div class="post-list">
        <div class="post-card" v-for="post in myPosts" :key="post.id" @click="goToCommunity">
          <div class="post-header">
            <span class="post-category-tag" :class="post.category">{{ categoryLabel(post.category) }}</span>
            <span class="post-time">{{ formatTime(post.time) }}</span>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-desc">{{ post.content.substring(0, 100) }}{{ post.content.length > 100 ? '...' : '' }}</div>
          <div class="post-stats-row">
            <span><i class="ri-eye-line"></i> {{ post.views }}</span>
            <span><i class="ri-chat-3-line"></i> {{ countComments(post.comments) }}</span>
            <span><i class="ri-heart-3-line"></i> {{ post.likes || 0 }}</span>
          </div>
        </div>
      </div>
      <div class="empty-state" v-if="myPosts.length === 0">
        <p>还没有发布过帖子</p>
        <router-link to="/community" class="go-link"><i class="ri-arrow-right-line"></i> 去社区发帖</router-link>
      </div>
    </div>

    <!-- 我的收藏（编辑模式下隐藏） -->
    <div class="panel" v-show="profileTab === 'favorites' && !showEdit && !showPasswordEdit">
      <div class="post-list">
        <div class="post-card" v-for="post in favPosts" :key="post.id" @click="goToCommunity">
          <div class="post-header">
            <span class="post-category-tag" :class="post.category">{{ categoryLabel(post.category) }}</span>
            <span class="post-time">{{ formatTime(post.time) }}</span>
          </div>
          <div class="post-title">{{ post.title }}</div>
          <div class="post-desc">{{ post.content.substring(0, 100) }}{{ post.content.length > 100 ? '...' : '' }}</div>
          <div class="post-stats-row">
            <span><i class="ri-eye-line"></i> {{ post.views }}</span>
            <span><i class="ri-chat-3-line"></i> {{ countComments(post.comments) }}</span>
            <span><i class="ri-heart-3-line"></i> {{ post.likes || 0 }}</span>
          </div>
        </div>
      </div>
      <div class="empty-state" v-if="favPosts.length === 0">
        <p>还没有收藏帖子</p>
        <router-link to="/community" class="go-link"><i class="ri-arrow-right-line"></i> 去社区逛逛</router-link>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->

</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const PROFILE_KEY = 'cosmos_profile'
const COMMUNITY_KEY = 'cosmos_community_posts'

const profileTab = ref('posts')
const showEdit = ref(false)
const showPasswordEdit = ref(false)
const isLoggedIn = ref(false)

// Back to top
const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

const avatarOptions = [
  { icon: 'ri-user-star-line' },
  { icon: 'ri-rocket-line' },
  { icon: 'ri-planet-line' },
  { icon: 'ri-spaceship-line' },
  { icon: 'ri-meteor-line' },
  { icon: 'ri-sun-line' },
  { icon: 'ri-moon-line' },
  { icon: 'ri-star-line' },
  { icon: 'ri-shield-star-line' },
  { icon: 'ri-code-s-slash-line' },
  { icon: 'ri-bug-line' },
  { icon: 'ri-terminal-box-line' },
]

const profile = ref({
  nickname: '星际旅人',
  bio: '',
  avatar: 'ri-rocket-line',
  favorites: []
})

const editForm = ref({ nickname: '', bio: '', avatar: 'ri-rocket-line' })
const passwordForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' })

const avatarStyle = computed(() => {
  const colors = ['linear-gradient(135deg, #3a7bd5, #5fa3f0)', 'linear-gradient(135deg, #c05c3e, #d97a5c)', 'linear-gradient(135deg, #d4b78c, #b89a6e)', 'linear-gradient(135deg, #6ab0d6, #9fc5e8)', 'linear-gradient(135deg, #8a6db8, #b095e0)']
  const idx = profile.value.nickname.charCodeAt(0) % colors.length
  return { background: colors[idx] }
})

const categoryNames = { frontend: '前端', backend: '后端', mobile: '移动端', ai: '人工智能', cloud: '云计算', database: '数据库', devops: '运维', security: '安全', testing: '测试', product: '产品' }
function categoryLabel(id) { return categoryNames[id] || id }

function loadProfile() {
  try {
    const saved = JSON.parse(localStorage.getItem(PROFILE_KEY))
    if (saved) profile.value = { ...profile.value, ...saved }
  } catch { /* ignore */ }
}

function saveProfileToStorage() {
  localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value))
}

function toggleEdit() {
  showEdit.value = !showEdit.value
  if (showEdit.value) showPasswordEdit.value = false
}

function togglePasswordEdit() {
  showPasswordEdit.value = !showPasswordEdit.value
  if (showPasswordEdit.value) showEdit.value = false
}

function doLogout() {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('cosmos_logged_in')
    router.push('/')
  }
}

function saveProfile() {
  const { nickname, bio, avatar } = editForm.value
  if (!nickname.trim()) { alert('昵称不能为空'); return }
  profile.value.nickname = nickname.trim()
  profile.value.bio = bio.trim()
  profile.value.avatar = avatar
  saveProfileToStorage()
  showEdit.value = false
  alert('资料已更新')
}

function savePassword() {
  const { oldPwd, newPwd, confirmPwd } = passwordForm.value
  if (!oldPwd) { alert('请输入当前密码'); return }
  if (!newPwd) { alert('请输入新密码'); return }
  if (newPwd !== confirmPwd) { alert('两次输入的新密码不一致'); return }
  const savedPwd = localStorage.getItem('cosmos_password') || ''
  if (savedPwd && oldPwd !== savedPwd) { alert('当前密码错误'); return }
  localStorage.setItem('cosmos_password', newPwd)
  passwordForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
  showPasswordEdit.value = false
  alert('密码修改成功')
}

function getCommunityData() {
  try { return JSON.parse(localStorage.getItem(COMMUNITY_KEY)) || [] }
  catch { return [] }
}

const allPosts = computed(() => getCommunityData())

const myPosts = computed(() => {
  // 在演示模式下，显示所有帖子作为用户的帖子
  return [...allPosts.value].sort((a, b) => new Date(b.time) - new Date(a.time))
})

const favPosts = computed(() => {
  const favIds = profile.value.favorites || []
  return allPosts.value.filter(p => favIds.includes(p.id))
})

function countComments(comments) {
  if (!comments || !comments.length) return 0
  let count = 0
  function countRec(arr) {
    arr.forEach(c => { count++; if (c.replies) countRec(c.replies) })
  }
  countRec(comments)
  return count
}

function formatTime(dateStr) {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function goToCommunity() {
  router.push('/community')
}

onMounted(() => {
  isLoggedIn.value = localStorage.getItem('cosmos_logged_in') === 'true'
  loadProfile()
  editForm.value = { nickname: profile.value.nickname, bio: profile.value.bio, avatar: profile.value.avatar || 'ri-rocket-line' }
  passwordForm.value = { oldPwd: '', newPwd: '', confirmPwd: '' }
})
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; background: #050b1a; color: #e0f0ff; min-height: 100vh; }
/* Theme vars for back-to-top */
.page-layout {
  --bt-bg: rgba(255,200,60,0.25);
  --bt-border: rgba(255,200,60,0.45);
  --bt-color: #fff;
  --bt-hover-bg: rgba(255,200,60,0.45);
  --bt-shadow: rgba(255,200,60,0.3);
}
.container { position: relative; z-index: 1; max-width: 800px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(255,255,255,0.2); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #ffe8a0); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(255,200,60,0.1); border: 1px solid rgba(255,200,60,0.25); color: #ffd080; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(255,200,60,0.2); box-shadow: 0 0 15px rgba(255,200,60,0.2); }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.sun { background: radial-gradient(circle at 35% 35%, #ffe680, #ffc040 40%, #e89820 70%, #a06010 100%); box-shadow: 0 0 16px rgba(255,200,60,0.7), 0 0 32px rgba(255,180,40,0.4), 0 0 50px rgba(255,160,20,0.2), inset 0 0 6px rgba(255,255,255,0.2); }
.planet-sphere.sun::after { content: ''; position: absolute; inset: 8% 22% 38% 18%; background: rgba(255,255,255,0.2); border-radius: 50%; }
.tabs { display: flex; gap: 10px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(255,200,60,0.06); border: 1px solid rgba(255,200,60,0.15); color: #c8a870; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(255,200,60,0.15); border-color: rgba(255,200,60,0.35); color: #ffe8a0; box-shadow: 0 0 15px rgba(255,200,60,0.1); }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

/* Profile Card */
.profile-card { background: rgba(35,20,8,0.6); border: 1px solid rgba(255,200,60,0.18); border-radius: 20px; padding: 28px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.profile-header { display: flex; align-items: center; gap: 20px; }
.avatar { width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: bold; color: #fff; flex-shrink: 0; box-shadow: 0 0 20px rgba(255,200,60,0.35); }
.avatar i { font-size: 30px; }
.profile-info { flex: 1; }
.profile-info h2 { font-size: 1.3rem; font-weight: 400; color: #fff; margin-bottom: 4px; }
.profile-bio { font-size: 0.85rem; opacity: 0.6; margin-bottom: 8px; }
.profile-stats { display: flex; gap: 20px; font-size: 0.85rem; opacity: 0.7; }
.edit-btn { padding: 8px 16px; border-radius: 20px; border: 1px solid rgba(255,200,60,0.25); background: rgba(255,200,60,0.08); color: #ffd080; cursor: pointer; font-size: 0.85rem; transition: 0.3s; white-space: nowrap; }
.edit-btn:hover { background: rgba(255,200,60,0.18); }
.action-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.logout-btn { padding: 8px 16px; border-radius: 20px; border: 1px solid rgba(255,80,80,0.3); background: rgba(255,80,80,0.1); color: #ff9999; cursor: pointer; font-size: 0.85rem; transition: 0.3s; white-space: nowrap; }
.logout-btn:hover { background: rgba(255,80,80,0.25); }
.section-title { font-weight: 400; margin-bottom: 16px; color: #ffe8a0; letter-spacing: 2px; font-size: 1.05rem; }

/* Avatar Picker */
.avatar-picker { display: flex; flex-wrap: wrap; gap: 10px; }
.avatar-option { width: 44px; height: 44px; border-radius: 50%; background: rgba(255,200,60,0.06); border: 1px solid rgba(255,200,60,0.15); display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 20px; color: #c8a870; transition: 0.3s; }
.avatar-option:hover { background: rgba(255,200,60,0.15); }
.avatar-option.active { border-color: #ffc040; background: rgba(255,192,64,0.2); box-shadow: 0 0 10px rgba(255,192,64,0.3); color: #fff; }

/* Edit Panel */
.edit-panel { margin-top: 20px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); }
.edit-panel .form-group { margin-bottom: 14px; }
.edit-panel label { display: block; margin-bottom: 6px; font-size: 0.85rem; opacity: 0.7; }
.edit-panel input, .edit-panel textarea { width: 100%; padding: 10px 14px; border-radius: 10px; background: rgba(25,14,5,0.6); border: 1px solid rgba(255,255,255,0.2); color: #e0f0ff; font-family: inherit; font-size: 0.9rem; outline: none; transition: 0.3s; }
.edit-panel input:focus, .edit-panel textarea:focus { border-color: rgba(255,200,60,0.45); }
.save-btn { padding: 10px 28px; border-radius: 24px; border: none; background: linear-gradient(135deg, #e89820, #ffc040); color: #1a0800; cursor: pointer; font-size: 0.9rem; transition: 0.3s; font-weight: 500; }
.save-btn:hover { box-shadow: 0 0 20px rgba(255,192,64,0.5); }

/* Login Prompt */
.login-prompt { text-align: center; padding: 120px 20px; }
.login-prompt > i { font-size: 4rem; color: rgba(255,255,255,0.3); display: block; margin-bottom: 20px; }
.login-prompt h2 { font-size: 1.5rem; font-weight: 300; margin-bottom: 10px; color: #fff; }
.login-prompt p { opacity: 0.5; margin-bottom: 30px; }
.login-link { display: inline-block; padding: 12px 36px; border-radius: 30px; background: linear-gradient(135deg, #e89820, #ffc040); color: #1a0800; text-decoration: none; font-size: 1rem; transition: 0.3s; margin-bottom: 15px; font-weight: 500; }
.login-link:hover { box-shadow: 0 0 20px rgba(255,192,64,0.5); }
.back-home-link { display: block; color: rgba(255,255,255,0.5); text-decoration: none; font-size: 0.9rem; transition: 0.3s; }
.back-home-link:hover { color: #fff; }

/* Posts */
.panel { display: block; }
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { background: rgba(35,20,8,0.5); border: 1px solid rgba(255,200,60,0.1); border-radius: 16px; padding: 20px; backdrop-filter: blur(8px); cursor: pointer; transition: 0.3s; }
.post-card:hover { border-color: rgba(255,200,60,0.3); box-shadow: 0 5px 25px rgba(0,0,0,0.2); transform: translateY(-2px); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.post-category-tag { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 0.75rem; background: rgba(255,192,64,0.12); color: #ffd080; border: 1px solid rgba(255,192,64,0.22); text-transform: capitalize; }
.post-category-tag.frontend { background: rgba(58,180,120,0.2); color: #7ee8a0; border-color: rgba(58,180,120,0.3); }
.post-category-tag.backend { background: rgba(58,123,213,0.2); color: #88bbff; border-color: rgba(58,123,213,0.3); }
.post-category-tag.ai { background: rgba(138,109,184,0.2); color: #c8b5e8; border-color: rgba(138,109,184,0.3); }
.post-time { font-size: 0.8rem; opacity: 0.5; }
.post-title { font-size: 1.05rem; margin-bottom: 6px; color: #fff; font-weight: 500; }
.post-desc { font-size: 0.85rem; opacity: 0.6; line-height: 1.5; }
.post-stats-row { display: flex; gap: 16px; margin-top: 10px; font-size: 0.8rem; opacity: 0.5; }

.empty-state { text-align: center; padding: 60px 20px; opacity: 0.5; }
.empty-state p { margin-bottom: 12px; }
.go-link { color: #ffc040; text-decoration: none; transition: 0.3s; }
.go-link:hover { color: #ffe8a0; }

@media (max-width: 768px) {
  .container { padding: 15px 12px 60px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; }
  .planet-sphere { width: 28px; height: 28px; }
  .profile-header { flex-wrap: wrap; }
  .avatar { width: 52px; height: 52px; font-size: 22px; }
  .tabs { gap: 6px; flex-wrap: wrap; }
  .tab { padding: 8px 14px; font-size: 0.85rem; }
}
</style>
