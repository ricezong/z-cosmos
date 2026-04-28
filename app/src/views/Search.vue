<template>
  <div class="star-bg"></div>
  <div class="container">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon">🟣</div>
        <div class="header-title">
          <h1>搜索区</h1>
          <p>紫微星 · 全站信息聚合</p>
        </div>
      </div>
      <router-link to="/" class="back-btn">← 返回星域</router-link>
    </header>

    <div class="search-box">
      <div class="search-input-wrap">
        <input type="text" class="search-input" v-model="query" placeholder="搜索帖子、热点、剧场、工具..." @keydown.enter="doSearch">
        <button class="search-btn" @click="doSearch">🔍 搜索</button>
      </div>
      <div class="filter-tags">
        <div class="filter-tag" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部</div>
        <div class="filter-tag" :class="{ active: filter === 'tieba' }" @click="filter = 'tieba'">📝 贴吧</div>
        <div class="filter-tag" :class="{ active: filter === 'hot' }" @click="filter = 'hot'">🔥 热点</div>
        <div class="filter-tag" :class="{ active: filter === 'theater' }" @click="filter = 'theater'">🎭 剧场</div>
        <div class="filter-tag" :class="{ active: filter === 'tools' }" @click="filter = 'tools'">🛠️ 工具</div>
      </div>
    </div>

    <div class="results-header">
      <div class="results-count">{{ resultsCount }}</div>
    </div>
    <div class="results-list">
      <div class="result-item" v-for="(r, i) in results" :key="i" @click="$router.push(r.url)">
        <div class="result-icon">{{ r.icon }}</div>
        <div class="result-content">
          <div class="result-title" v-html="highlight(r.title)"></div>
          <div class="result-desc" v-html="highlight(r.desc)"></div>
          <div class="result-meta">
            <span class="result-type" :class="r.type">{{ r.typeLabel }}</span>
            <span>{{ r.meta }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="empty-state" v-if="searched && results.length === 0">
      <p>😕 未找到与 "{{ query }}" 相关的结果</p>
      <p>试试其他关键词？</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

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

function doSearch() {
  const q = query.value.trim().toLowerCase()
  searched.value = true
  if (!q) { results.value = []; return }
  const out = []

  if (filter.value === 'all' || filter.value === 'tieba') {
    try {
      const tieba = JSON.parse(localStorage.getItem('cosmos_tieba_posts')) || []
      tieba.forEach(p => {
        if (p.title.toLowerCase().includes(q) || p.content.toLowerCase().includes(q) || p.nickname.toLowerCase().includes(q)) {
          out.push({ title: p.title, desc: p.content.substring(0, 100) + (p.content.length > 100 ? '...' : ''), meta: `👤 ${p.nickname} · 💬 ${(p.comments || []).length}条评论 · 📅 ${new Date(p.time).toLocaleDateString()}`, type: 'tieba', typeLabel: '贴吧', icon: '📝', url: '/tieba' })
        }
        ;(p.comments || []).forEach(c => {
          if (c.body.toLowerCase().includes(q) || c.nickname.toLowerCase().includes(q)) {
            out.push({ title: `评论: ${p.title}`, desc: `${c.nickname}: ${c.body.substring(0, 100)}`, meta: `👤 ${c.nickname} · 📅 ${new Date(c.time).toLocaleDateString()}`, type: 'tieba', typeLabel: '贴吧评论', icon: '💬', url: '/tieba' })
          }
        })
      })
    } catch { /* ignore */ }
  }
  if (filter.value === 'all' || filter.value === 'hot') {
    hotData.forEach(h => { if (h.title.toLowerCase().includes(q) || h.desc.toLowerCase().includes(q)) { out.push({ ...h, meta: `${sourceNames[h.type] || h.type} · 🔥 热搜`, type: 'hot', typeLabel: '热点', icon: '🔥' }) } })
  }
  if (filter.value === 'all' || filter.value === 'theater') {
    theaterData.forEach(t => { if (t.title.toLowerCase().includes(q) || t.desc.toLowerCase().includes(q)) { out.push({ ...t, meta: t.type === 'stickman' ? '🎭 火柴人剧场' : '👾 像素剧场', type: 'theater', typeLabel: '剧场', icon: t.type === 'stickman' ? '🎭' : '👾' }) } })
  }
  if (filter.value === 'all' || filter.value === 'tools') {
    toolsData.forEach(t => { if (t.title.toLowerCase().includes(q) || t.desc.toLowerCase().includes(q)) { out.push({ ...t, meta: '🛠️ 自助工具', type: 'tools', typeLabel: '工具', icon: '🛠️' }) } })
  }
  results.value = out
}
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; background: #050b1a; color: #e8d5ff; min-height: 100vh; }
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
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid rgba(138,109,184,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #8a6db8, #b095e0); box-shadow: 0 0 20px rgba(138,109,184,0.4); display: flex; align-items: center; justify-content: center; font-size: 24px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #b095e0, #e0d0ff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(138,109,184,0.2); border: 1px solid rgba(138,109,184,0.4); color: #e0d0ff; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(138,109,184,0.35); box-shadow: 0 0 15px rgba(138,109,184,0.3); }
.search-box { background: rgba(20,12,35,0.6); border: 1px solid rgba(138,109,184,0.25); border-radius: 24px; padding: 25px; margin-bottom: 30px; backdrop-filter: blur(10px); }
.search-input-wrap { display: flex; gap: 12px; margin-bottom: 15px; }
.search-input { flex: 1; padding: 14px 22px; border-radius: 30px; background: rgba(10,6,20,0.6); border: 1px solid rgba(138,109,184,0.3); color: #e8d5ff; font-size: 1rem; outline: none; font-family: inherit; transition: 0.3s; }
.search-input:focus { border-color: rgba(138,109,184,0.7); box-shadow: 0 0 20px rgba(138,109,184,0.15); }
.search-input::placeholder { color: rgba(138,109,184,0.5); }
.search-btn { padding: 14px 32px; border-radius: 30px; border: none; background: linear-gradient(135deg, #8a6db8, #b095e0); color: white; cursor: pointer; font-size: 1rem; transition: 0.3s; letter-spacing: 1px; }
.search-btn:hover { box-shadow: 0 0 25px rgba(138,109,184,0.4); transform: translateY(-1px); }
.filter-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.filter-tag { padding: 6px 16px; border-radius: 20px; cursor: pointer; background: rgba(138,109,184,0.1); border: 1px solid rgba(138,109,184,0.2); color: #c8b5e8; font-size: 0.85rem; transition: 0.3s; }
.filter-tag.active, .filter-tag:hover { background: rgba(138,109,184,0.3); border-color: rgba(138,109,184,0.5); color: #fff; }
.results-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.results-count { font-size: 0.9rem; opacity: 0.7; }
.results-list { display: flex; flex-direction: column; gap: 12px; }
.result-item { background: rgba(20,12,35,0.5); border: 1px solid rgba(138,109,184,0.2); border-radius: 16px; padding: 18px 22px; backdrop-filter: blur(8px); cursor: pointer; transition: 0.3s; display: flex; gap: 15px; align-items: flex-start; }
.result-item:hover { border-color: rgba(138,109,184,0.5); box-shadow: 0 5px 25px rgba(138,109,184,0.1); transform: translateY(-2px); }
.result-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; flex-shrink: 0; background: rgba(138,109,184,0.2); border: 1px solid rgba(138,109,184,0.3); }
.result-content { flex: 1; }
.result-title { font-size: 1.05rem; margin-bottom: 6px; color: #f0e5ff; }
.result-desc { font-size: 0.88rem; opacity: 0.7; line-height: 1.5; margin-bottom: 8px; }
.result-meta { display: flex; gap: 15px; font-size: 0.8rem; opacity: 0.5; align-items: center; }
.result-type { display: inline-block; padding: 3px 12px; border-radius: 12px; font-size: 0.75rem; background: rgba(138,109,184,0.2); color: #c8b5e8; border: 1px solid rgba(138,109,184,0.3); }
.result-type.tieba { background: rgba(58,123,213,0.2); color: #a8d0ff; border-color: rgba(58,123,213,0.3); }
.result-type.hot { background: rgba(212,183,140,0.2); color: #f0e0c0; border-color: rgba(212,183,140,0.3); }
.result-type.theater { background: rgba(192,92,62,0.2); color: #ffbba0; border-color: rgba(192,92,62,0.3); }
.result-type.tools { background: rgba(106,176,214,0.2); color: #c8e0f5; border-color: rgba(106,176,214,0.3); }
.empty-state { text-align: center; padding: 60px 20px; opacity: 0.5; }
.empty-state p { margin-bottom: 10px; }

:deep(.highlight) { background: rgba(138,109,184,0.3); padding: 1px 4px; border-radius: 4px; }

@media (max-width: 768px) {
  .container { padding: 15px 12px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-icon { width: 38px; height: 38px; font-size: 18px; }
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
