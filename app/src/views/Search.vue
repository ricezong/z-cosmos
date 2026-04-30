<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere purple"></div></div>
        <div class="header-title">
          <h1>搜索区</h1>
          <p>紫微星 · 全站信息聚合</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="search-box">
      <div class="search-input-wrap">
        <input type="text" class="search-input" v-model="query" placeholder="搜索帖子、热点、剧场、工具..." @keydown.enter="doSearch">
        <button class="search-btn" @click="doSearch"><i class="ri-search-line"></i> 搜索</button>
      </div>
      <div class="filter-tags">
        <div class="filter-tag" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部</div>
        <div class="filter-tag" :class="{ active: filter === 'community' }" @click="filter = 'community'"><i class="ri-earth-line"></i> 社区</div>
        <div class="filter-tag" :class="{ active: filter === 'hot' }" @click="filter = 'hot'"><i class="ri-fire-line"></i> 热点</div>
        <div class="filter-tag" :class="{ active: filter === 'theater' }" @click="filter = 'theater'"><i class="ri-movie-line"></i> 剧场</div>
        <div class="filter-tag" :class="{ active: filter === 'tools' }" @click="filter = 'tools'"><i class="ri-tools-line"></i> 工具</div>
      </div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <div class="results-header">
      <div class="results-count">{{ resultsCount }}</div>
    </div>
    <div class="results-list">
      <div class="result-item" v-for="(r, i) in results" :key="i" @click="$router.push(r.url)">
        <div class="result-icon" v-html="r.icon"></div>
        <div class="result-content">
          <div class="result-title" v-html="highlight(r.title)"></div>
          <div class="result-desc" v-html="highlight(r.desc)"></div>
          <div class="result-meta">
            <span class="result-type" :class="r.type">{{ r.typeLabel }}</span>
            <span v-html="r.meta"></span>
          </div>
        </div>
      </div>
    </div>
    <div class="empty-state" v-if="searched && results.length === 0">
      <p><i class="ri-emotion-sad-line"></i> 未找到与 "{{ query }}" 相关的结果</p>
      <p>试试其他关键词？</p>
    </div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->

</template>

<script setup>
import { ref, computed } from 'vue'
import { globalSearch, searchByType } from '../api/search'

const query = ref('')
const filter = ref('all')
const results = ref([])
const searched = ref(false)

const resultsCount = computed(() => {
  if (!searched.value) return '输入关键词开始搜索'
  return `找到 ${results.value.length} 个结果`
})

const hotData = [
  { title: '国产大模型再创新高', desc: '最新评测显示中文理解能力全球第一', source: 'hot', type: 'tech', url: '/hot' },
  { title: '新发现的系外行星可能有生命迹象', desc: '天文学家在宜居带发现类似地球的大气层', source: 'hot', type: 'weibo', url: '/hot' },
  { title: 'SpaceX星舰第四次发射成功', desc: '航天工程师详细解读技术细节和意义', source: 'hot', type: 'zhihu', url: '/hot' },
  { title: '新型固态电池技术取得突破', desc: '充电5分钟续航1000公里成为可能', source: 'hot', type: 'tech', url: '/hot' },
  { title: '沉浸式太空漫步实拍', desc: '国际空间站4K实拍视频火爆全网', source: 'hot', type: 'bilibili', url: '/hot' },
  { title: 'OpenAI发布GPT-5预览版', desc: '多模态能力大幅提升，支持视频理解', source: 'hot', type: 'tech', url: '/hot' },
  { title: '苹果Vision Pro 2代或于明年春季发布', desc: '重量减轻40%，视场角提升至120度', source: 'hot', type: 'tech', url: '/hot' },
  { title: '全球气候峰会在日内瓦召开', desc: '190个国家代表出席，达成碳中和协议', source: 'hot', type: 'weibo', url: '/hot' },
  { title: '自制火箭成功发射并回收', desc: '全程记录视频播放量突破320万', source: 'hot', type: 'bilibili', url: '/hot' },
  { title: 'Rust基金会宣布内核2.0时代', desc: '更多核心模块将使用Rust重写', source: 'hot', type: 'github', url: '/hot' }
]
const theaterData = [
  { title: '火柴人行走动画', desc: '经典火柴人行走循环动画演示', source: 'theater', type: 'stickman', url: '/theater' },
  { title: '火柴人奔跑动画', desc: '快速奔跑姿态的动态演示', source: 'theater', type: 'stickman', url: '/theater' },
  { title: '火柴人跳跃动画', desc: '跳跃、腾空、落地的完整动作', source: 'theater', type: 'stickman', url: '/theater' },
  { title: '火柴人格斗动画', desc: '出拳、闪避、格斗组合动作', source: 'theater', type: 'stickman', url: '/theater' },
  { title: '像素雨效果', desc: '经典黑客帝国风格数字雨动画', source: 'theater', type: 'pixel', url: '/theater' },
  { title: '像素弹跳球', desc: '物理弹跳轨迹与拖尾效果', source: 'theater', type: 'pixel', url: '/theater' },
  { title: '像素贪吃蛇', desc: '复古风格自动寻路贪吃蛇', source: 'theater', type: 'pixel', url: '/theater' },
  { title: '像素星际穿越', desc: '3D伪深度星际航行效果', source: 'theater', type: 'pixel', url: '/theater' }
]
const toolsData = [
  { title: '便签工具', desc: '添加、编辑、保存个人便签', source: 'tools', type: 'notepad', url: '/tools' },
  { title: '涂鸦画板', desc: '自由涂鸦，支持保存为图片', source: 'tools', type: 'draw', url: '/tools' },
  { title: '计算器', desc: '支持加减乘除和连续运算', source: 'tools', type: 'calc', url: '/tools' },
  { title: '计时器', desc: '倒计时工具，支持暂停和重置', source: 'tools', type: 'timer', url: '/tools' },
  { title: '颜色工具', desc: 'RGB调色板，输出HEX/RGB/HSL', source: 'tools', type: 'color', url: '/tools' },
  { title: '文本工具', desc: '字数统计、格式转换、复制', source: 'tools', type: 'text', url: '/tools' }
]
const sourceNames = { weibo: '微博', zhihu: '知乎', bilibili: 'B站', tech: '科技', github: 'GitHub' }

function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}
function highlight(text) {
  if (!query.value) return escapeHtml(text)
  const q = escapeHtml(query.value)
  const regex = new RegExp('(' + q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi')
  return escapeHtml(text).replace(regex, '<span class="highlight">$1</span>')
}

async function doSearch() {
  const q = query.value.trim().toLowerCase()
  searched.value = true
  if (!q) { results.value = []; return }
  try {
    if (filter.value === 'all') {
      const data = await globalSearch(q, 10)
      results.value = [
        ...(data.posts || []).map(toResult),
        ...(data.news || []).map(toResult),
        ...(data.videos || []).map(toResult)
      ]
      return
    }
    const typeMap = { community: 'post', hot: 'news', theater: 'video' }
    if (typeMap[filter.value]) {
      const page = await searchByType(typeMap[filter.value], q, 1, 20)
      results.value = (page.records || []).map(toResult)
      return
    }
  } catch (e) {
    console.warn('API search failed, fallback to local data', e)
  }
  const out = []

  if (filter.value === 'all' || filter.value === 'community') {
    try {
      const posts = JSON.parse(localStorage.getItem('cosmos_community_posts')) || []
      posts.forEach(p => {
        if (p.title.toLowerCase().includes(q) || p.content.toLowerCase().includes(q)) {
          out.push({ title: p.title, desc: p.content.substring(0, 100) + (p.content.length > 100 ? '...' : ''), meta: `<i class="ri-chat-3-line"></i> ${(p.comments || []).length}条评论 · <i class="ri-calendar-line"></i> ${new Date(p.time).toLocaleDateString()}`, type: 'community', typeLabel: '社区', icon: '<i class="ri-earth-line"></i>', url: '/community' })
        }
        ;(p.comments || []).forEach(c => {
          if (c.body.toLowerCase().includes(q) || c.nickname.toLowerCase().includes(q)) {
            out.push({ title: `评论: ${p.title}`, desc: `${c.nickname}: ${c.body.substring(0, 100)}`, meta: `<i class="ri-user-line"></i> ${c.nickname} · <i class="ri-calendar-line"></i> ${new Date(c.time).toLocaleDateString()}`, type: 'community', typeLabel: '社区评论', icon: '<i class="ri-chat-3-line"></i>', url: '/community' })
          }
        })
      })
    } catch { /* ignore */ }
  }
  if (filter.value === 'all' || filter.value === 'hot') {
    hotData.forEach(h => { if (h.title.toLowerCase().includes(q) || h.desc.toLowerCase().includes(q)) { out.push({ ...h, meta: `${sourceNames[h.type] || h.type} · <i class="ri-fire-line"></i> 热搜`, type: 'hot', typeLabel: '热点', icon: '<i class="ri-fire-line"></i>' }) } })
  }
  if (filter.value === 'all' || filter.value === 'theater') {
    theaterData.forEach(t => { if (t.title.toLowerCase().includes(q) || t.desc.toLowerCase().includes(q)) { out.push({ ...t, meta: t.type === 'stickman' ? '<i class="ri-body-scan-line"></i> 火柴人剧场' : '<i class="ri-game-line"></i> 像素剧场', type: 'theater', typeLabel: '剧场', icon: t.type === 'stickman' ? '<i class="ri-body-scan-line"></i>' : '<i class="ri-game-line"></i>' }) } })
  }
  if (filter.value === 'all' || filter.value === 'tools') {
    toolsData.forEach(t => { if (t.title.toLowerCase().includes(q) || t.desc.toLowerCase().includes(q)) { out.push({ ...t, meta: '<i class="ri-tools-line"></i> 自助工具', type: 'tools', typeLabel: '工具', icon: '<i class="ri-tools-line"></i>' }) } })
  }
  results.value = out
}

function toResult(item) {
  const type = item.type === 'post' ? 'community' : item.type === 'news' ? 'hot' : item.type === 'video' ? 'theater' : item.type
  const labels = { community: '绀惧尯', hot: '鐑偣', theater: '鍓у満' }
  const icons = {
    community: '<i class="ri-earth-line"></i>',
    hot: '<i class="ri-fire-line"></i>',
    theater: '<i class="ri-movie-line"></i>'
  }
  const urls = {
    community: `/community?postId=${encodeURIComponent(item.id)}`,
    hot: '/hot',
    theater: '/theater'
  }
  return {
    title: item.title || '',
    desc: item.summary || '',
    meta: `<i class="ri-eye-line"></i> ${item.viewCount || 0}`,
    type,
    typeLabel: labels[type] || type,
    icon: icons[type] || '<i class="ri-search-line"></i>',
    url: urls[type] || '/'
  }
}

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
</script>

<style scoped>
/* Theme vars for back-to-top */
.page-layout {
  --bt-bg: rgba(152,136,183,0.3);
  --bt-border: rgba(152,136,183,0.5);
  --bt-color: #dcc8f4;
  --bt-hover-bg: rgba(152,136,183,0.5);
  --bt-shadow: rgba(152,136,183,0.3);
}
.container { position: relative; z-index: 1; max-width: 900px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.35); box-shadow: 0 0 15px rgba(144,166,196,0.3); }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.purple { background: radial-gradient(circle at 35% 35%, #c4aee3, #a996d0 50%, #513c78 100%); box-shadow: 0 0 12px rgba(152,136,183,0.5), inset 0 0 8px rgba(255,255,255,0.15); }
.planet-sphere.purple::after { content: ''; position: absolute; inset: 10% 25% 40% 20%; background: rgba(255,255,255,0.1); border-radius: 50%; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }
.search-box { border: 1px solid rgba(144,166,196,0.25); border-radius: 24px; padding: 25px; backdrop-filter: blur(10px); }
.search-input-wrap { display: flex; gap: 12px; margin-bottom: 15px; }
.search-input { flex: 1; padding: 14px 22px; border-radius: 30px; background: rgba(10,6,20,0.6); border: 1px solid rgba(144,166,196,0.3); color: #e8eef7; font-size: 1rem; outline: none; font-family: inherit; transition: 0.3s; }
.search-input:focus { border-color: rgba(144,166,196,0.7); box-shadow: 0 0 20px rgba(144,166,196,0.15); }
.search-input::placeholder { color: rgba(144,166,196,0.5); }
.search-btn { padding: 14px 32px; border-radius: 30px; border: none; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: white; cursor: pointer; font-size: 1rem; transition: 0.3s; letter-spacing: 1px; }
.search-btn:hover { box-shadow: 0 0 25px rgba(144,166,196,0.4); transform: translateY(-1px); }
.filter-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.filter-tag { padding: 6px 16px; border-radius: 20px; cursor: pointer; background: rgba(144,166,196,0.1); border: 1px solid rgba(144,166,196,0.2); color: #a8bcd4; font-size: 0.85rem; transition: 0.3s; }
.filter-tag.active, .filter-tag:hover { background: rgba(144,166,196,0.3); border-color: rgba(144,166,196,0.5); color: #fff; }
.results-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.results-count { font-size: 0.9rem; opacity: 0.7; }
.results-list { display: flex; flex-direction: column; gap: 12px; }
.result-item { border: 1px solid rgba(144,166,196,0.2); border-radius: 16px; padding: 18px 22px; cursor: pointer; transition: 0.3s; display: flex; gap: 15px; align-items: flex-start; }
.result-item:hover { border-color: rgba(144,166,196,0.5); box-shadow: 0 5px 25px rgba(144,166,196,0.1); transform: translateY(-2px); }
.result-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; flex-shrink: 0; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.3); }
.result-icon i { font-size: 1.4rem; }
.result-content { flex: 1; }
.result-title { font-size: 1.05rem; margin-bottom: 6px; color: #e8eef7; }
.result-desc { font-size: 0.88rem; opacity: 0.7; line-height: 1.5; margin-bottom: 8px; }
.result-meta { display: flex; gap: 15px; font-size: 0.8rem; opacity: 0.5; align-items: center; }
.result-type { display: inline-block; padding: 3px 12px; border-radius: 12px; font-size: 0.75rem; background: rgba(144,166,196,0.2); color: #a8bcd4; border: 1px solid rgba(144,166,196,0.3); }
.result-type.community { background: rgba(144,166,196,0.2); color: #c9d9ef; border-color: rgba(144,166,196,0.3); }
.result-type.hot { background: rgba(196,142,110,0.2); color: #f0d8b0; border-color: rgba(196,142,110,0.3); }
.result-type.theater { background: rgba(179,124,128,0.2); color: #f0c0c6; border-color: rgba(179,124,128,0.3); }
.result-type.tools { background: rgba(133,168,168,0.2); color: #d4e8ec; border-color: rgba(133,168,168,0.3); }
.empty-state { text-align: center; padding: 60px 20px; opacity: 0.5; }
.empty-state p { margin-bottom: 10px; }

:deep(.highlight) { background: rgba(144,166,196,0.3); padding: 1px 4px; border-radius: 4px; }

@media (max-width: 768px) {
  .container { padding: 15px 12px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-sphere { width: 28px; height: 28px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .search-box { padding: 18px; }
  .search-input-wrap { flex-direction: column; }
  .search-btn { width: 100%; }
  .filter-tags { gap: 6px; }
  .filter-tag { padding: 5px 12px; font-size: 0.8rem; }
  .result-item { padding: 14px; gap: 10px; }
  .result-title { font-size: 0.95rem; }
  .result-desc { font-size: 0.82rem; }
  .result-icon { width: 36px; height: 36px; font-size: 1.1rem; }
}
</style>
