<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
      <header class="header">
        <div class="header-left">
          <div class="planet-icon"><div class="planet-sphere mars"></div></div>
          <div class="header-title">
            <h1>剧场区</h1>
            <p>火星 · 影像与表演中心</p>
          </div>
        </div>
        <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
      </header>
      <div class="container">
        <div class="tabs">
          <div class="tab" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'"><i class="ri-movie-line"></i> 全部</div>
          <div class="tab" :class="{ active: activeTab === 'movie' }" @click="activeTab = 'movie'"><i class="ri-film-line"></i> 电影</div>
          <div class="tab" :class="{ active: activeTab === 'drama' }" @click="activeTab = 'drama'"><i class="ri-tv-line"></i> 剧集</div>
          <div class="tab" :class="{ active: activeTab === 'anime' }" @click="activeTab = 'anime'"><i class="ri-game-line"></i> 动画</div>
        </div>
      </div>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
      <div class="container">
        <div class="panel">
          <div class="stage">
            <div class="stage-title"><i class="ri-movie-line"></i> 在线剧场</div>
            <div class="stage-desc">数据来自后端视频接口，点击卡片会加载详情并上报播放。</div>
            <div class="video-grid" v-if="filteredVideos.length">
              <div class="video-card" v-for="video in filteredVideos" :key="video.videoId" @click="openVideo(video)">
                <img v-if="video.coverImage" :src="video.coverImage" :alt="video.title">
                <div v-else class="video-cover-fallback"><i class="ri-movie-2-line"></i></div>
                <div class="video-card-body">
                  <h3>{{ video.title }}</h3>
                  <p>{{ video.category || 'video' }} · {{ video.totalEpisodes || 1 }} 集 · {{ video.viewCount || 0 }} 次观看</p>
                  <span v-if="video.rating" class="rating"><i class="ri-star-fill"></i> {{ video.rating }}</span>
                </div>
              </div>
            </div>
            <div class="placeholder-box" v-else>
              <div class="placeholder-icon"><i class="ri-movie-line"></i></div>
              <p>{{ loading ? '正在加载剧场数据...' : '暂无剧场数据' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getVideo, listVideos, reportPlay } from '../api/theater'

const activeTab = ref('all')
const videos = ref([])
const loading = ref(false)

const filteredVideos = computed(() => {
  if (activeTab.value === 'all') return videos.value
  return videos.value.filter(v => v.category === activeTab.value)
})

const showBackTop = ref(false)
const contentRef = ref(null)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

async function loadVideos() {
  loading.value = true
  try {
    const data = await listVideos({ page: 1, size: 30, sort: 'latest' })
    videos.value = data.records || []
  } catch (e) {
    console.warn('Load videos failed', e)
    videos.value = []
  } finally {
    loading.value = false
  }
}

async function openVideo(video) {
  try {
    const detail = await getVideo(video.videoId)
    const first = detail.episodes?.[0]
    if (first) await reportPlay(video.videoId, first.episodeNumber)
    if (first?.playUrl) window.open(first.playUrl, '_blank')
  } catch (e) {
    alert(e.message || '视频加载失败')
  }
}

onMounted(loadVideos)
</script>

<style scoped>
.page-layout {
  --bt-bg: rgba(179,124,128,0.3);
  --bt-border: rgba(179,124,128,0.5);
  --bt-color: #f0c0c6;
  --bt-hover-bg: rgba(179,124,128,0.5);
  --bt-shadow: rgba(179,124,128,0.3);
}
.container { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; padding: 20px; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { line-height: 1; display: flex; align-items: center; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.4); box-shadow: 0 0 15px rgba(144,166,196,0.3); }
.tabs { display: flex; gap: 10px; flex-wrap: wrap; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(144,166,196,0.15); border: 1px solid rgba(144,166,196,0.25); color: #a8bcd4; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(144,166,196,0.35); border-color: rgba(144,166,196,0.6); box-shadow: 0 0 15px rgba(144,166,196,0.2); color: #fff; }
.planet-sphere { width: 36px; height: 36px; border-radius: 50%; position: relative; flex-shrink: 0; }
.planet-sphere.mars { background: radial-gradient(circle at 35% 35%, #c68088, #a86068 50%, #402028 100%); box-shadow: 0 0 12px rgba(175,90,95,0.5), inset 0 0 8px rgba(255,255,255,0.12); }
.planet-sphere.mars::after { content: ''; position: absolute; inset: 12% 28% 42% 22%; background: rgba(255,255,255,0.1); border-radius: 50%; }
.panel { display: block; }
.stage { border: 1px solid rgba(144,166,196,0.25); border-radius: 20px; padding: 25px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.stage-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 5px; color: #e8eef7; letter-spacing: 2px; }
.stage-desc { font-size: 0.9rem; opacity: 0.7; margin-bottom: 20px; }
.video-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(190px, 1fr)); gap: 14px; }
.video-card { border: 1px solid rgba(144,166,196,0.22); border-radius: 14px; overflow: hidden; cursor: pointer; background: rgba(10,18,38,0.5); transition: 0.25s; }
.video-card:hover { transform: translateY(-2px); border-color: rgba(144,166,196,0.5); box-shadow: 0 8px 24px rgba(0,0,0,0.22); }
.video-card img, .video-cover-fallback { width: 100%; aspect-ratio: 16 / 9; object-fit: cover; display: flex; align-items: center; justify-content: center; background: rgba(144,166,196,0.12); color: #c5d5ea; font-size: 2rem; }
.video-card-body { padding: 12px; }
.video-card-body h3 { font-size: 0.96rem; font-weight: 500; color: #fff; margin-bottom: 6px; }
.video-card-body p { font-size: 0.78rem; opacity: 0.65; line-height: 1.5; }
.rating { display: inline-flex; align-items: center; gap: 4px; margin-top: 8px; color: #f0c88a; font-size: 0.78rem; }
.placeholder-box { text-align: center; padding: 60px 20px; opacity: 0.6; }
.placeholder-icon { font-size: 3rem; margin-bottom: 15px; }
.placeholder-box p { font-size: 1rem; color: #a8bcd4; }

@media (max-width: 768px) {
  .container { padding: 15px 12px 60px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-sphere { width: 28px; height: 28px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .tab { padding: 8px 14px; font-size: 0.85rem; }
}
</style>
