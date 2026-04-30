<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <!-- 固定区域 -->
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere jupiter"></div></div>
        <div class="header-title">
          <h1>热点区</h1>
          <p>木星 · 每日热点汇聚</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    <div class="container">
    <div class="date-bar">
      <div class="date-btn" v-for="d in dates" :key="d.str"
        :class="{ active: currentDate === d.str }" @click="currentDate = d.str">
        <span class="day-label">{{ d.isToday ? '今天' : d.day }}</span>
        <span class="date-label">{{ d.date }}</span>
      </div>
    </div>

    <div class="source-tabs">
      <div class="source-tab" :class="{ active: currentSource === 'all' }" @click="currentSource = 'all'">全部</div>
      <div class="source-tab" :class="{ active: currentSource === 'weibo' }" @click="currentSource = 'weibo'"><i class="ri-fire-line"></i> 微博</div>
      <div class="source-tab" :class="{ active: currentSource === 'zhihu' }" @click="currentSource = 'zhihu'"><i class="ri-lightbulb-line"></i> 知乎</div>
      <div class="source-tab" :class="{ active: currentSource === 'bilibili' }" @click="currentSource = 'bilibili'"><i class="ri-video-line"></i> B站</div>
      <div class="source-tab" :class="{ active: currentSource === 'tech' }" @click="currentSource = 'tech'"><i class="ri-computer-line"></i> 科技</div>
      <div class="source-tab" :class="{ active: currentSource === 'github' }" @click="currentSource = 'github'"><i class="ri-github-line"></i> GitHub</div>
    </div>
    </div><!-- container -->
    </div><!-- page-fixed -->

    <!-- 滚动区域 -->
    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="container">
    <div class="hot-list">
      <div class="hot-item" v-for="(item, idx) in filteredItems" :key="idx" @click="openModal(item)">
        <div class="hot-rank" :class="{ top3: idx < 3 }">{{ idx + 1 }}</div>
        <div class="hot-content">
          <div class="hot-title">{{ item.title }}</div>
          <div class="hot-desc">{{ item.desc }}</div>
          <div class="hot-meta">
            <span class="hot-tag" :class="{ hot: item.heat === '沸', new: item.heat === '新' }">{{ item.heat }}</span>
            <span>{{ item.srcName }}</span>
            <span><i class="ri-eye-line"></i> {{ item.views }}</span>
            <span><i class="ri-chat-3-line"></i> {{ item.comments }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="empty-state" v-if="filteredItems.length === 0"><p>当日暂无热点数据</p></div>
    </div><!-- container -->
    </div><!-- page-scroll -->
  <!-- 回到顶部 -->
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div><!-- page-layout -->


  <!-- Modal -->
  <div class="modal-overlay" :class="{ active: modalItem }" @click.self="modalItem = null">
    <div class="modal-box" v-if="modalItem">
      <button class="modal-close" @click="modalItem = null">×</button>
      <h2 class="modal-title">{{ modalItem.title }}</h2>
      <div class="modal-source">{{ modalItem.srcName }} · {{ currentDate }}</div>
      <div class="modal-body">
        <p style="margin-bottom:20px">{{ modalItem.desc }}</p>
        <p>这里是关于「{{ modalItem.title }}」的详细内容。该话题目前在{{ modalItem.srcName }}平台热度为「{{ modalItem.heat }}」，已产生{{ modalItem.views }}次浏览和{{ modalItem.comments }}条讨论。</p>
        <p style="margin-top:15px">相关内容持续更新中，点击来源链接可查看原始页面获取最新进展。</p>
      </div>
      <div class="modal-stats">
        <span><i class="ri-eye-line"></i> 浏览 {{ modalItem.views }}</span>
        <span><i class="ri-chat-3-line"></i> 讨论 {{ modalItem.comments }}</span>
        <span><i class="ri-fire-line"></i> 热度 {{ modalItem.heat }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const sourceNames = { weibo: '微博热搜', zhihu: '知乎热榜', bilibili: 'B站热门', tech: '科技资讯', github: 'GitHub Trending' }
const today = new Date()
const rawDates = Array.from({ length: 7 }, (_, i) => { const d = new Date(today); d.setDate(d.getDate() - i); return d })
const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const dates = rawDates.map(d => ({
  str: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`,
  day: days[d.getDay()], date: `${d.getMonth() + 1}/${d.getDate()}`, isToday: d.toDateString() === today.toDateString()
}))

const currentDate = ref(dates[0].str)
const currentSource = ref('all')
const modalItem = ref(null)

const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

const templates = {
  weibo: [
    { title: '新发现的系外行星可能有生命迹象', desc: '天文学家在宜居带发现类似地球的大气层成分', heat: '沸', views: '2.1亿', comments: '18.5万' },
    { title: '国产大模型再创新高，性能超越国际水平', desc: '最新评测显示中文理解能力全球第一', heat: '热', views: '1.8亿', comments: '12.3万' },
    { title: '某知名导演宣布执导科幻巨制', desc: '投资规模创华语电影史新高', heat: '热', views: '9800万', comments: '8.2万' },
    { title: '全球气候峰会在日内瓦召开', desc: '190个国家代表出席，达成新的碳中和协议', heat: '新', views: '7500万', comments: '5.1万' },
    { title: '新型固态电池技术取得突破', desc: '充电5分钟续航1000公里成为可能', heat: '沸', views: '1.5亿', comments: '9.8万' },
    { title: '人工智能生成内容版权新规出台', desc: '明确AI创作内容的归属权和使用规范', heat: '热', views: '6200万', comments: '4.5万' }
  ],
  zhihu: [
    { title: '如何评价最新的量子计算机突破？', desc: '255个回答 · 量子物理话题优秀回答者参与讨论', heat: '热', views: '45万', comments: '255' },
    { title: '未来十年最值得学习的技能是什么？', desc: '892个回答 · 涵盖AI、生物技术、能源等领域', heat: '沸', views: '120万', comments: '892' },
    { title: 'SpaceX星舰第四次发射成功的意义', desc: '156个回答 · 航天工程师详细解读技术细节', heat: '热', views: '68万', comments: '156' },
    { title: '为什么现在的年轻人更向往太空探索？', desc: '432个回答 · 从心理学和社会学角度分析', heat: '新', views: '38万', comments: '432' },
    { title: '脑机接口技术的最新进展和伦理问题', desc: '321个回答 · 神经科学家和伦理学家深度探讨', heat: '热', views: '52万', comments: '321' }
  ],
  bilibili: [
    { title: '【4K】沉浸式太空漫步 · 国际空间站实拍', desc: '播放量 385万 · 弹幕 2.4万 · UP主: 星际视界', heat: '热', views: '385万', comments: '1.8万' },
    { title: '我用AI重建了整个火星表面 · 3D渲染', desc: '播放量 256万 · 弹幕 1.9万 · UP主: 像素火星', heat: '沸', views: '256万', comments: '2.1万' },
    { title: '科普：如果太阳突然熄灭，人类能撑多久？', desc: '播放量 178万 · 弹幕 1.2万 · UP主: 宇宙漫游指南', heat: '热', views: '178万', comments: '9800' },
    { title: '【手书】星际穿越 · 不要温和地走进那个良夜', desc: '播放量 145万 · 弹幕 9800 · UP主: 星空绘师', heat: '新', views: '145万', comments: '7500' },
    { title: '自制火箭成功发射并回收！全程记录', desc: '播放量 320万 · 弹幕 2.8万 · UP主: 硬核玩家', heat: '沸', views: '320万', comments: '3.2万' }
  ],
  tech: [
    { title: 'OpenAI发布GPT-5预览版，多模态能力大幅提升', desc: '支持文本、图像、音频、视频统一处理', heat: '沸', views: '88万', comments: '4500' },
    { title: '苹果Vision Pro 2代或于明年春季发布', desc: '重量减轻40%，视场角提升至120度', heat: '热', views: '42万', comments: '2800' },
    { title: 'Linux Kernel 6.9正式发布，新增AI调度器', desc: '专为机器学习工作负载优化的进程调度', heat: '新', views: '18万', comments: '1200' },
    { title: '谷歌DeepMind蛋白质预测准确率达95%', desc: '新药研发周期有望从10年缩短至2年', heat: '热', views: '35万', comments: '2100' },
    { title: 'Rust基金会宣布进入Linux内核2.0时代', desc: '更多核心模块将使用Rust重写', heat: '热', views: '28万', comments: '3400' }
  ],
  github: [
    { title: 'starship / starship', desc: 'The minimal, blazing-fast, and infinitely customizable prompt for any shell! ⭐ 42.3k', heat: '热', views: '12万', comments: '890' },
    { title: 'microsoft / generative-ai-for-beginners', desc: '21 Lessons, Get Started Building with AI ⭐ 58.1k', heat: '沸', views: '25万', comments: '1200' },
    { title: 'Developer-Y / cs-video-courses', desc: 'List of Computer Science courses with video lectures. ⭐ 65.2k', heat: '热', views: '18万', comments: '950' },
    { title: 'rust-lang / rust', desc: 'Empowering everyone to build reliable and efficient software. ⭐ 92.1k', heat: '热', views: '30万', comments: '2100' },
    { title: 'oven-sh / bun', desc: 'Incredibly fast JavaScript runtime, bundler, test runner ⭐ 72.5k', heat: '新', views: '15万', comments: '780' }
  ]
}

function hashStr(s) {
  let h = 0
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0
  return h
}
function generateNews(dateStr, source) {
  const items = templates[source] || []
  const seed = dateStr.split('-').join('') + source
  const shuffled = [...items].sort((a, b) => (hashStr(a.title + seed) % 1000) - (hashStr(b.title + seed) % 1000))
  const count = 4 + (hashStr(seed) % 2)
  return shuffled.slice(0, count)
}

function getAllData() {
  const data = {}
  dates.forEach(d => {
    data[d.str] = {}
    Object.keys(sourceNames).forEach(src => { data[d.str][src] = generateNews(d.str, src) })
  })
  return data
}

const allData = getAllData()

const filteredItems = computed(() => {
  const data = allData[currentDate.value]
  let items = []
  if (currentSource.value === 'all') {
    Object.keys(data).forEach(src => {
      data[src].forEach((item, i) => { items.push({ ...item, source: src, rank: i + 1, srcName: sourceNames[src] }) })
    })
    const heatOrder = { '沸': 3, '热': 2, '新': 1 }
    items.sort((a, b) => (heatOrder[b.heat] || 0) - (heatOrder[a.heat] || 0))
  } else {
    items = data[currentSource.value].map((item, i) => ({ ...item, source: currentSource.value, rank: i + 1, srcName: sourceNames[currentSource.value] }))
  }
  return items
})

function openModal(item) { modalItem.value = item }
</script>

<style scoped>
/* Theme vars for back-to-top */
.page-layout {
  --bt-bg: rgba(220,175,125,0.3);
  --bt-border: rgba(220,175,125,0.5);
  --bt-color: #f0d8b0;
  --bt-hover-bg: rgba(220,175,125,0.5);
  --bt-shadow: rgba(220,175,125,0.3);
}
.container { position: relative; z-index: 1; max-width: 960px; margin: 0 auto; padding: 20px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(220,175,125,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #d8a987, #f0d8b0); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(220,175,125,0.15); border: 1px solid rgba(220,175,125,0.4); color: #f0d8b0; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(220,175,125,0.3); box-shadow: 0 0 15px rgba(220,175,125,0.3); }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.jupiter { background: radial-gradient(circle at 35% 35%, #e8c699, #d8a987 40%, #b59065 70%, #4a3620 100%); box-shadow: 0 0 12px rgba(220,175,125,0.5), inset 0 0 8px rgba(255,255,255,0.1); overflow: hidden; }
.planet-sphere.jupiter::before { content: ''; position: absolute; inset: 0; background: repeating-linear-gradient(0deg, transparent, transparent 4px, rgba(139,100,60,0.35) 4px, rgba(139,100,60,0.35) 6px); border-radius: 50%; }
.planet-sphere.jupiter::after { content: ''; position: absolute; inset: 10% 25% 40% 20%; background: rgba(255,255,255,0.1); border-radius: 50%; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }
.date-bar { display: flex; gap: 8px; margin-bottom: 20px; overflow-x: auto; padding-bottom: 5px; }
.date-btn { padding: 10px 18px; border-radius: 30px; cursor: pointer; white-space: nowrap; background: rgba(220,175,125,0.1); border: 1px solid rgba(220,175,125,0.2); color: #d8a987; transition: 0.3s; font-size: 0.9rem; }
.date-btn.active, .date-btn:hover { background: rgba(220,175,125,0.3); border-color: rgba(220,175,125,0.5); box-shadow: 0 0 15px rgba(220,175,125,0.15); color: #fff; }
.date-btn .day-label { font-size: 0.75rem; opacity: 0.7; display: block; text-align: center; }
.date-btn .date-label { font-weight: 500; }
.source-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.source-tab { padding: 8px 16px; border-radius: 20px; cursor: pointer; background: rgba(220,175,125,0.1); border: 1px solid rgba(220,175,125,0.2); color: #d8a987; transition: 0.3s; font-size: 0.85rem; }
.source-tab.active, .source-tab:hover { background: rgba(220,175,125,0.25); border-color: rgba(220,175,125,0.5); color: #fff; }
.hot-list { display: flex; flex-direction: column; gap: 12px; }
.hot-item { background: rgba(22,18,36,0.62); border: 1px solid rgba(220,175,125,0.2); border-radius: 16px; padding: 18px 22px; backdrop-filter: blur(8px); cursor: pointer; transition: 0.3s; display: flex; gap: 15px; align-items: flex-start; }
.hot-item:hover { border-color: rgba(220,175,125,0.5); box-shadow: 0 5px 25px rgba(220,175,125,0.1); transform: translateY(-2px); }
.hot-rank { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 0.9rem; flex-shrink: 0; background: rgba(220,175,125,0.2); color: #d8a987; border: 1px solid rgba(220,175,125,0.3); }
.hot-rank.top3 { background: linear-gradient(135deg, #d8a987, #b59065); color: #1a1510; border: none; box-shadow: 0 0 10px rgba(220,175,125,0.3); }
.hot-content { flex: 1; }
.hot-title { font-size: 1.05rem; margin-bottom: 6px; color: #f0e5d5; }
.hot-desc { font-size: 0.88rem; opacity: 0.7; line-height: 1.5; margin-bottom: 8px; }
.hot-meta { display: flex; gap: 15px; font-size: 0.8rem; opacity: 0.5; align-items: center; }
.hot-tag { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; background: rgba(220,175,125,0.15); color: #d8a987; border: 1px solid rgba(220,175,125,0.2); }
.hot-tag.hot { background: rgba(192,60,60,0.2); color: #ff9999; border-color: rgba(192,60,60,0.3); }
.hot-tag.new { background: rgba(60,160,100,0.2); color: #99ffbb; border-color: rgba(60,160,100,0.3); }
.modal-overlay { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(10,8,6,0.85); z-index: 100; backdrop-filter: blur(5px); align-items: center; justify-content: center; padding: 20px; }
.modal-overlay.active { display: flex; }
.modal-box { background: linear-gradient(135deg, #252018, #1e1812); border: 1px solid rgba(220,175,125,0.3); border-radius: 24px; max-width: 600px; width: 100%; max-height: 80vh; overflow-y: auto; padding: 30px; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
.modal-close { float: right; width: 36px; height: 36px; border-radius: 50%; background: rgba(220,175,125,0.15); border: 1px solid rgba(220,175,125,0.3); color: #d8a987; cursor: pointer; font-size: 1.2rem; display: flex; align-items: center; justify-content: center; transition: 0.3s; }
.modal-close:hover { background: rgba(220,175,125,0.3); }
.modal-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 10px; color: #f0d8b0; }
.modal-source { font-size: 0.85rem; opacity: 0.6; margin-bottom: 20px; }
.modal-body { line-height: 1.8; opacity: 0.9; font-size: 0.95rem; }
.modal-stats { display: flex; gap: 20px; margin-top: 25px; padding-top: 20px; border-top: 1px solid rgba(220,175,125,0.2); font-size: 0.9rem; opacity: 0.7; }

@media (max-width: 768px) {
  .container { padding: 0px 20px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-sphere { width: 28px; height: 28px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .date-bar { gap: 6px; }
  .date-btn { padding: 8px 12px; font-size: 0.8rem; }
  .source-tabs { gap: 6px; }
  .source-tab { padding: 6px 12px; font-size: 0.8rem; }
  .hot-item { padding: 14px; gap: 10px; }
  .hot-title { font-size: 0.95rem; }
  .hot-desc { font-size: 0.82rem; }
  .modal-box { padding: 20px; border-radius: 18px; }
  .modal-title { font-size: 1.1rem; }
}
</style>
