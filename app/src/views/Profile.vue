<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域：header + 个人信息卡 + tabs（登录态、非编辑态） -->
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

    <!-- 个人信息卡（固定区）：登录态且非编辑态显示 -->
    <div class="container profile-fixed-container" v-if="isLoggedIn && !showEdit && !showPasswordEdit">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar" :style="avatarStyle">
            <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover">
            <span v-else>{{ (profile.nickname || 'U').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="profile-info">
            <h2>{{ profile.nickname || '星际旅人' }}</h2>
            <p class="profile-bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
            <div class="profile-stats">
              <span><i class="ri-file-text-line"></i> {{ profile.postCount || 0 }} 帖子</span>
              <span><i class="ri-star-line"></i> {{ profile.collectCount || 0 }} 收藏</span>
              <span><i class="ri-heart-3-line"></i> {{ profile.likeCount || 0 }} 获赞</span>
              <span><i class="ri-group-line"></i> {{ profile.followerCount || 0 }} 粉丝</span>
            </div>
          </div>
          <div class="action-btns">
            <button class="edit-btn" @click="toggleEdit">
              <i class="ri-edit-line"></i> 编辑资料
            </button>
            <button class="edit-btn" @click="togglePasswordEdit">
              <i class="ri-lock-line"></i> 修改密码
            </button>
            <button class="logout-btn" @click="doLogout">
              <i class="ri-logout-box-r-line"></i> 退出登录
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="container tabs-container" v-if="isLoggedIn && !showEdit && !showPasswordEdit">
      <div class="tabs">
        <div class="tab" :class="{ active: profileTab === 'posts' }" @click="profileTab = 'posts'; loadMyPosts()"><i class="ri-file-text-line"></i> 我的帖子</div>
        <div class="tab" :class="{ active: profileTab === 'favorites' }" @click="profileTab = 'favorites'; loadMyCollections()"><i class="ri-star-line"></i> 我的收藏</div>
      </div>
    </div>
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container" v-if="isLoggedIn">
      <!-- 编辑资料卡（滚动区） -->
      <div class="profile-card edit-card" v-show="showEdit">
        <div class="edit-card-header">
          <h3><i class="ri-edit-line"></i> 编辑资料</h3>
          <button class="edit-btn" @click="toggleEdit"><i class="ri-close-line"></i> 取消</button>
        </div>
        <div class="edit-panel standalone">
          <div class="form-group">
            <label>头像</label>
            <div class="avatar-upload">
              <div class="avatar-preview" :style="avatarStyle">
                <img v-if="editForm.avatarUrl" :src="editForm.avatarUrl" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover">
                <span v-else>{{ (editForm.nickname || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <label class="avatar-upload-btn">
                <i class="ri-camera-line"></i> 上传头像
                <input type="file" accept="image/*" @change="handleAvatarUpload" style="display:none">
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="editForm.nickname" maxlength="20" placeholder="输入昵称">
          </div>
          <div class="form-group">
            <label>性别</label>
            <select v-model="editForm.gender" class="form-select">
              <option :value="0">未知</option>
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </div>
          <div class="form-group">
            <label>个人简介</label>
            <textarea v-model="editForm.bio" maxlength="200" placeholder="介绍一下自己..." rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>所在地区</label>
            <input v-model="editForm.location" maxlength="50" placeholder="所在城市">
          </div>
          <div class="form-group">
            <label>生日</label>
            <input type="date" v-model="editForm.birthday">
          </div>
          <button class="save-btn" :disabled="saving" @click="saveProfile"><i class="ri-save-line"></i> {{ saving ? '保存中...' : '保存修改' }}</button>
        </div>
      </div>

      <!-- 修改密码卡（滚动区） -->
      <div class="profile-card edit-card" v-show="showPasswordEdit">
        <div class="edit-card-header">
          <h3><i class="ri-lock-line"></i> 修改密码</h3>
          <button class="edit-btn" @click="togglePasswordEdit"><i class="ri-close-line"></i> 取消</button>
        </div>
        <div class="edit-panel standalone">
          <div class="form-group">
            <label>当前密码</label>
            <input type="password" v-model="passwordForm.oldPassword" placeholder="输入当前密码">
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.newPassword" placeholder="输入新密码 (6-32位)">
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input type="password" v-model="passwordForm.confirmPwd" placeholder="再次输入新密码">
          </div>
          <div class="form-error" v-if="passwordError">{{ passwordError }}</div>
          <button class="save-btn" :disabled="savingPwd" @click="savePassword"><i class="ri-lock-line"></i> {{ savingPwd ? '修改中...' : '修改密码' }}</button>
        </div>
      </div>

      <!-- 我的帖子 -->
      <div class="panel" v-show="profileTab === 'posts' && !showEdit && !showPasswordEdit">
        <div class="post-list" v-if="myPosts.length > 0">
          <div class="post-card" v-for="post in myPosts" :key="post.postId" @click="openPost(post.postId)">
            <div class="post-header">
              <span class="post-category-tag">{{ post.category?.name || '未分类' }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            </div>
            <div class="post-title">{{ post.title }}</div>
            <div class="post-desc">{{ post.summary || '' }}</div>
            <div class="post-stats-row">
              <span><i class="ri-eye-line"></i> {{ post.viewCount || 0 }}</span>
              <span><i class="ri-chat-3-line"></i> {{ post.commentCount || 0 }}</span>
              <span><i class="ri-heart-3-line"></i> {{ post.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>还没有发布过帖子</p>
          <router-link to="/community" class="go-link"><i class="ri-arrow-right-line"></i> 去社区发帖</router-link>
        </div>
      </div>

      <!-- 我的收藏 -->
      <div class="panel" v-show="profileTab === 'favorites' && !showEdit && !showPasswordEdit">
        <div class="post-list" v-if="myCollections.length > 0">
          <div class="post-card" v-for="post in myCollections" :key="post.postId" @click="openPost(post.postId)">
            <div class="post-header">
              <span class="post-category-tag">{{ post.category?.name || '未分类' }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            </div>
            <div class="post-title">{{ post.title }}</div>
            <div class="post-desc">{{ post.summary || '' }}</div>
            <div class="post-stats-row">
              <span><i class="ri-eye-line"></i> {{ post.viewCount || 0 }}</span>
              <span><i class="ri-chat-3-line"></i> {{ post.commentCount || 0 }}</span>
              <span><i class="ri-heart-3-line"></i> {{ post.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>还没有收藏的帖子</p>
          <router-link to="/community" class="go-link"><i class="ri-arrow-right-line"></i> 去社区逛逛</router-link>
        </div>
      </div>
    </div><!-- container 登录态 -->

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
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->

</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, updateProfile, changePassword, uploadAvatar, listMyPosts, listMyCollections } from '../api/profile'
import { logout as apiLogout } from '../api/auth'
import { useAuth } from '../composables/useAuth'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'

const router = useRouter()
const toast = useToast()
const { showConfirm } = useConfirm()
const { isLoggedIn } = useAuth()

const profileTab = ref('posts')
const showEdit = ref(false)
const showPasswordEdit = ref(false)
const saving = ref(false)
const savingPwd = ref(false)
const passwordError = ref('')

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

const profile = ref({
  nickname: '',
  bio: '',
  avatarUrl: '',
  gender: 0,
  location: '',
  birthday: '',
  postCount: 0,
  collectCount: 0,
  likeCount: 0,
  followerCount: 0
})

const editForm = ref({
  nickname: '',
  bio: '',
  avatarUrl: '',
  gender: 0,
  location: '',
  birthday: ''
})

const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPwd: '' })

const myPosts = ref([])
const myCollections = ref([])

const avatarStyle = computed(() => {
  if (profile.value.avatarUrl) {
    return {}
  }
  const colors = ['linear-gradient(135deg, #3a7bd5, #5fa3f0)', 'linear-gradient(135deg, #c05c3e, #d97a5c)', 'linear-gradient(135deg, #d4b78c, #b89a6e)', 'linear-gradient(135deg, #6ab0d6, #9fc5e8)', 'linear-gradient(135deg, #8a6db8, #b095e0)']
  const idx = ((profile.value.nickname || 'U').charCodeAt(0)) % colors.length
  return { background: colors[idx] }
})

async function loadProfile() {
  try {
    const data = await getCurrentUser()
    if (data) {
      profile.value = { ...profile.value, ...data }
      editForm.value = {
        nickname: data.nickname || '',
        bio: data.bio || '',
        avatarUrl: data.avatarUrl || '',
        gender: data.gender || 0,
        location: data.location || '',
        birthday: data.birthday || ''
      }
    }
  } catch (e) {
    console.error('加载用户信息失败:', e)
  }
}

async function loadMyPosts(page = 1) {
  try {
    const data = await listMyPosts({ page, size: 20 })
    myPosts.value = data?.records || []
  } catch (e) {
    console.error('加载我的帖子失败:', e)
    myPosts.value = []
  }
}

async function loadMyCollections(page = 1) {
  try {
    const data = await listMyCollections({ page, size: 20 })
    myCollections.value = data?.records || []
  } catch (e) {
    console.error('加载我的收藏失败:', e)
    myCollections.value = []
  }
}

function toggleEdit() {
  showEdit.value = !showEdit.value
  if (showEdit.value) {
    showPasswordEdit.value = false
    editForm.value = {
      nickname: profile.value.nickname || '',
      bio: profile.value.bio || '',
      avatarUrl: profile.value.avatarUrl || '',
      gender: profile.value.gender || 0,
      location: profile.value.location || '',
      birthday: profile.value.birthday || ''
    }
  }
}

function togglePasswordEdit() {
  showPasswordEdit.value = !showPasswordEdit.value
  if (showPasswordEdit.value) showEdit.value = false
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPwd: '' }
  passwordError.value = ''
}

async function doLogout() {
  if (!await showConfirm('退出登录', '确定要退出登录吗？')) return
  try {
    await apiLogout()
  } catch (e) {
    // 即使后端登出失败也要清理本地（apiLogout 内部已有 finally 清理）
  }
  toast.success('已退出登录')
  router.push('/')
}

async function saveProfile() {
  if (!editForm.value.nickname.trim()) { toast.warning('昵称不能为空'); return }
  saving.value = true
  try {
    await updateProfile({
      nickname: editForm.value.nickname.trim(),
      bio: editForm.value.bio?.trim() || '',
      gender: editForm.value.gender,
      location: editForm.value.location?.trim() || '',
      birthday: editForm.value.birthday || null
    })
    // 重新加载
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

async function savePassword() {
  passwordError.value = ''
  const { oldPassword, newPassword, confirmPwd } = passwordForm.value
  if (!oldPassword) { passwordError.value = '请输入当前密码'; return }
  if (!newPassword) { passwordError.value = '请输入新密码'; return }
  if (newPassword.length < 6 || newPassword.length > 32) { passwordError.value = '新密码长度需在6-32位之间'; return }
  if (newPassword !== confirmPwd) { passwordError.value = '两次输入的新密码不一致'; return }

  savingPwd.value = true
  try {
    await changePassword({ oldPassword, newPassword })
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPwd: '' }
    showPasswordEdit.value = false
    toast.success('密码修改成功')
  } catch (e) {
    passwordError.value = e.message || '修改失败，请重试'
  } finally {
    savingPwd.value = false
  }
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
  router.push({ path: '/community', query: { postId } })
}

// isLoggedIn 是响应式的，token 存入 localStorage 后自动变为 true
// 使用 watch 监听，状态确认后自动加载数据
watch(isLoggedIn, async (loggedIn) => {
  if (loggedIn) {
    await Promise.allSettled([loadProfile(), loadMyPosts()])
  }
}, { immediate: true })
</script>

<style scoped>
/* Theme vars for back-to-top */
.page-layout {
  --bt-bg: rgba(204,177,131,0.25);
  --bt-border: rgba(204,177,131,0.45);
  --bt-color: #fff;
  --bt-hover-bg: rgba(204,177,131,0.45);
  --bt-shadow: rgba(204,177,131,0.3);
}
.container { position: relative; z-index: 1; max-width: 800px; margin: 0 auto; padding: 20px 20px; }
.tabs-container { padding-top: 12px; padding-bottom: 12px; }
.profile-fixed-container { padding-top: 14px; padding-bottom: 4px; }
.profile-fixed-container .profile-card { margin-bottom: 0; padding: 18px 22px; }
.profile-fixed-container .profile-header { gap: 16px; }
.profile-fixed-container .avatar { width: 72px; height: 72px; font-size: 1.8rem; }
.profile-fixed-container .profile-info h2 { font-size: 1.2rem; }
.profile-fixed-container .profile-bio { font-size: 0.85rem; margin-top: 4px; }
.profile-fixed-container .profile-stats { margin-top: 8px; gap: 14px; }
.edit-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; padding-bottom: 14px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.edit-card-header h3 { font-size: 1.05rem; font-weight: 500; color: #e8eef7; display: flex; align-items: center; gap: 8px; }
.edit-panel.standalone { margin-top: 0; padding-top: 0; border-top: none; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(255,255,255,0.2); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.25); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.2); box-shadow: 0 0 15px rgba(144,166,196,0.2); }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.sun { background: radial-gradient(circle at 35% 35%, #f6deab, #e7bc6b 40%, #c89a50 70%, #7a5224 100%); box-shadow: 0 0 16px rgba(204,177,131,0.7), 0 0 32px rgba(230,175,95,0.4), 0 0 50px rgba(220,160,80,0.2), inset 0 0 6px rgba(255,255,255,0.2); }
.planet-sphere.sun::after { content: ''; position: absolute; inset: 8% 22% 38% 18%; background: rgba(255,255,255,0.2); border-radius: 50%; }
.tabs { display: flex; gap: 10px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(144,166,196,0.06); border: 1px solid rgba(144,166,196,0.15); color: #a8bcd4; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(144,166,196,0.15); border-color: rgba(144,166,196,0.35); color: #fff; box-shadow: 0 0 15px rgba(144,166,196,0.1); }
.empty-state { text-align: center; padding: 60px 20px; opacity: 0.5; }
.empty-state p { margin-bottom: 12px; }
.go-link { color: #c5d5ea; text-decoration: none; transition: 0.3s; }
.go-link:hover { color: #e8eef7; }

/* Profile Card */
.profile-card { border: 1px solid rgba(144,166,196,0.18); border-radius: 20px; padding: 28px; margin-bottom: 22px; backdrop-filter: blur(10px); box-shadow: 0 8px 28px rgba(0,0,0,0.25); }
.profile-header { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.avatar { width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: bold; color: #fff; flex-shrink: 0; box-shadow: 0 0 20px rgba(144,166,196,0.35); overflow: hidden; }
.profile-info { flex: 1; min-width: 0; }
.profile-info h2 { font-size: 1.3rem; font-weight: 400; color: #fff; margin-bottom: 4px; }
.profile-bio { font-size: 0.85rem; opacity: 0.6; margin-bottom: 8px; }
.profile-stats { display: flex; gap: 16px; font-size: 0.82rem; opacity: 0.7; flex-wrap: wrap; }
.edit-btn { padding: 8px 16px; border-radius: 20px; border: 1px solid rgba(144,166,196,0.25); background: rgba(144,166,196,0.08); color: #c5d5ea; cursor: pointer; font-size: 0.85rem; transition: 0.3s; white-space: nowrap; }
.edit-btn:hover { background: rgba(144,166,196,0.18); }
.action-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.logout-btn { padding: 8px 16px; border-radius: 20px; border: 1px solid rgba(255,80,80,0.3); background: rgba(255,80,80,0.1); color: #ff9999; cursor: pointer; font-size: 0.85rem; transition: 0.3s; white-space: nowrap; }
.logout-btn:hover { background: rgba(255,80,80,0.25); }

/* Avatar Upload */
.avatar-upload { display: flex; align-items: center; gap: 16px; }
.avatar-preview { width: 64px; height: 64px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: bold; color: #fff; flex-shrink: 0; overflow: hidden; box-shadow: 0 0 12px rgba(144,166,196,0.3); }
.avatar-upload-btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: 16px; border: 1px dashed rgba(144,166,196,0.4); background: rgba(144,166,196,0.06); color: #c5d5ea; cursor: pointer; font-size: 0.85rem; transition: 0.3s; }
.avatar-upload-btn:hover { border-color: rgba(144,166,196,0.7); background: rgba(144,166,196,0.15); }

/* Edit Panel */
.edit-panel { margin-top: 20px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); }
.edit-panel .form-group { margin-bottom: 14px; }
.edit-panel label { display: block; margin-bottom: 6px; font-size: 0.85rem; opacity: 0.7; }
.edit-panel input, .edit-panel textarea, .edit-panel select { width: 100%; padding: 10px 14px; border-radius: 10px; background: rgba(18,22,34,0.6); border: 1px solid rgba(144,166,196,0.25); color: #e8eef7; font-family: inherit; font-size: 0.9rem; outline: none; transition: 0.3s; box-sizing: border-box; }
.edit-panel input:focus, .edit-panel textarea:focus { border-color: rgba(144,166,196,0.45); }
.form-select { appearance: none; cursor: pointer; background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'><path fill='%23a8bcd4' d='M6 8 0 0h12z'/></svg>"); background-repeat: no-repeat; background-position: right 14px center; background-size: 10px; }
.form-select option { background: #0f1428; color: #e8eef7; }
.form-error { margin-bottom: 12px; padding: 8px 12px; border-radius: 8px; background: rgba(255,80,80,0.15); border: 1px solid rgba(255,80,80,0.3); color: #ff9999; font-size: 0.82rem; }
.save-btn { padding: 10px 28px; border-radius: 24px; border: none; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: #fff; cursor: pointer; font-size: 0.9rem; transition: 0.3s; font-weight: 500; }
.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.save-btn:hover { box-shadow: 0 0 20px rgba(144,166,196,0.5); }

/* Login Prompt */
.login-prompt { text-align: center; padding: 80px 20px; }
.login-prompt > i { font-size: 4rem; color: rgba(255,255,255,0.3); display: block; margin-bottom: 20px; }
.login-prompt h2 { font-size: 1.5rem; font-weight: 300; margin-bottom: 10px; color: #fff; }
.login-prompt p { opacity: 0.5; margin-bottom: 30px; }
.login-link { display: inline-block; padding: 12px 36px; border-radius: 30px; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: #fff; text-decoration: none; font-size: 1rem; transition: 0.3s; margin-bottom: 15px; font-weight: 500; }
.login-link:hover { box-shadow: 0 0 20px rgba(144,166,196,0.5); }
.back-home-link { display: block; color: rgba(255,255,255,0.5); text-decoration: none; font-size: 0.9rem; transition: 0.3s; }
.back-home-link:hover { color: #fff; }

/* Posts */
.panel { display: block; }
.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-card { background: rgba(22,26,44,0.62); border: 1px solid rgba(144,166,196,0.18); border-radius: 16px; padding: 20px; cursor: pointer; transition: 0.3s; }
.post-card:hover { border-color: rgba(144,166,196,0.3); box-shadow: 0 5px 25px rgba(0,0,0,0.2); transform: translateY(-2px); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.post-category-tag { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 0.75rem; background: rgba(144,166,196,0.15); color: #c5d5ea; border: 1px solid rgba(144,166,196,0.3); }
.post-time { font-size: 0.8rem; opacity: 0.5; }
.post-title { font-size: 1.05rem; margin-bottom: 6px; color: #fff; font-weight: 500; }
.post-desc { font-size: 0.85rem; opacity: 0.6; line-height: 1.5; }
.post-stats-row { display: flex; gap: 16px; margin-top: 10px; font-size: 0.8rem; opacity: 0.5; }

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