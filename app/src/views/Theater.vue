<template>
  <div class="star-bg"></div>
  <div class="container">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon">🔴</div>
        <div class="header-title">
          <h1>剧场区</h1>
          <p>火星 · 表演艺术中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn">← 返回星域</router-link>
    </header>

    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'stickman' }" @click="activeTab = 'stickman'">🎭 火柴人剧场</div>
      <div class="tab" :class="{ active: activeTab === 'pixel' }" @click="activeTab = 'pixel'">👾 像素剧场</div>
    </div>

    <div class="panel" v-show="activeTab === 'stickman'">
      <div class="stage">
        <div class="stage-title">🎬 火柴人动画剧场</div>
        <div class="stage-desc">选择场景，观看火柴人的精彩表演</div>
        <div class="canvas-wrapper">
          <canvas ref="stickCanvas" class="theater-canvas"></canvas>
        </div>
        <div class="controls">
          <select v-model="stickScene" class="ctrl-select">
            <option value="walk">🚶 行走</option>
            <option value="run">🏃 奔跑</option>
            <option value="jump">🦘 跳跃</option>
            <option value="dance">💃 舞蹈</option>
            <option value="fight">🥋 格斗</option>
            <option value="wave">👋 挥手</option>
          </select>
          <button class="ctrl-btn" @click="playStickman">▶ 播放</button>
          <button class="ctrl-btn secondary" @click="pauseStickman">⏸ 暂停</button>
          <button class="ctrl-btn secondary" @click="resetStickman">↺ 重置</button>
        </div>
        <div class="status-bar">
          <span>状态: {{ stickStatus }}</span>
          <span>帧: {{ stickFrame }}</span>
        </div>
      </div>
    </div>

    <div class="panel" v-show="activeTab === 'pixel'">
      <div class="stage">
        <div class="stage-title">🕹️ 像素动画剧场</div>
        <div class="stage-desc">复古像素风格的动画演示</div>
        <div class="canvas-wrapper">
          <canvas ref="pixelCanvas" class="theater-canvas"></canvas>
        </div>
        <div class="controls">
          <select v-model="pixelScene" class="ctrl-select">
            <option value="matrix">🌧️ 像素雨</option>
            <option value="bounce">🏀 弹跳球</option>
            <option value="snake">🐍 贪吃蛇</option>
            <option value="starfield">🌌 星际穿越</option>
            <option value="fire">🔥 火焰效果</option>
            <option value="wave">🌊 波浪</option>
          </select>
          <button class="ctrl-btn" @click="playPixel">▶ 播放</button>
          <button class="ctrl-btn secondary" @click="pausePixel">⏸ 暂停</button>
          <button class="ctrl-btn secondary" @click="resetPixel">↺ 重置</button>
        </div>
        <div class="status-bar">
          <span>状态: {{ pixelStatus }}</span>
          <span>帧: {{ pixelFrame }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const activeTab = ref('stickman')
const stickCanvas = ref(null)
const pixelCanvas = ref(null)

// Stickman
const stickScene = ref('walk')
const stickFrame = ref(0)
const stickStatus = ref('准备就绪')
let stickPlaying = false
let stickAnimId = null

function resizeStick() {
  if (!stickCanvas.value) return
  const rect = stickCanvas.value.parentElement.getBoundingClientRect()
  stickCanvas.value.width = rect.width
  stickCanvas.value.height = 400
}

function drawStickman(ctx, x, y, scale, pose) {
  ctx.strokeStyle = '#ffccaa'
  ctx.lineWidth = 3 * scale
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  const headY = y - 50 * scale
  const bodyTop = headY + 15 * scale
  const bodyBot = bodyTop + 40 * scale
  const shoulderY = bodyTop + 5 * scale
  const hipY = bodyBot - 5 * scale
  ctx.beginPath()
  ctx.arc(x, headY, 12 * scale, 0, Math.PI * 2)
  ctx.stroke()
  ctx.fillStyle = '#ffccaa'
  ctx.beginPath(); ctx.arc(x - 4 * scale, headY - 2 * scale, 2 * scale, 0, Math.PI * 2); ctx.fill()
  ctx.beginPath(); ctx.arc(x + 4 * scale, headY - 2 * scale, 2 * scale, 0, Math.PI * 2); ctx.fill()
  ctx.beginPath(); ctx.moveTo(x, bodyTop); ctx.lineTo(x, bodyBot); ctx.stroke()
  const armLen = 25 * scale
  ctx.beginPath(); ctx.moveTo(x, shoulderY); ctx.lineTo(x + Math.cos(pose.la) * armLen, shoulderY + Math.sin(pose.la) * armLen); ctx.stroke()
  ctx.beginPath(); ctx.moveTo(x, shoulderY); ctx.lineTo(x + Math.cos(pose.ra) * armLen, shoulderY + Math.sin(pose.ra) * armLen); ctx.stroke()
  const legLen = 30 * scale
  ctx.beginPath(); ctx.moveTo(x, hipY); ctx.lineTo(x + Math.cos(pose.ll) * legLen, hipY + Math.sin(pose.ll) * legLen); ctx.stroke()
  ctx.beginPath(); ctx.moveTo(x, hipY); ctx.lineTo(x + Math.cos(pose.rl) * legLen, hipY + Math.sin(pose.rl) * legLen); ctx.stroke()
}

function getStickmanPose(scene, frame) {
  const t = frame * 0.05
  switch (scene) {
    case 'walk': return { la: Math.sin(t) * 0.8 - 0.2, ra: Math.sin(t + Math.PI) * 0.8 - 0.2, ll: Math.sin(t + Math.PI) * 0.6 + 0.3, rl: Math.sin(t) * 0.6 + 0.3 }
    case 'run': return { la: Math.sin(t * 2) * 1.2 - 0.5, ra: Math.sin(t * 2 + Math.PI) * 1.2 - 0.5, ll: Math.sin(t * 2 + Math.PI) * 1.0 + 0.5, rl: Math.sin(t * 2) * 1.0 + 0.5 }
    case 'jump': { const p = (frame % 60) / 60; const j = Math.sin(p * Math.PI) * 2; return { la: p < 0.3 ? -2.5 : (p > 0.7 ? -0.5 : -1.0), ra: p < 0.3 ? -2.5 : (p > 0.7 ? -0.5 : -1.0), ll: p < 0.5 ? -0.5 : 0.8, rl: p < 0.5 ? -0.5 : 0.8, offsetY: -j * 30 } }
    case 'dance': return { la: Math.sin(t * 1.5) * 1.5 + 0.5, ra: Math.cos(t * 1.5) * 1.5 + 0.5, ll: Math.cos(t * 1.5) * 0.8 + 0.5, rl: Math.sin(t * 1.5) * 0.8 + 0.5 }
    case 'fight': { const punch = (frame % 40) < 10; return { la: punch ? -0.2 : -0.8, ra: punch ? -2.8 : -0.8, ll: 0.5, rl: 0.8 } }
    case 'wave': return { la: Math.sin(t * 0.5) * 0.3 - 0.3, ra: Math.sin(t * 2) * 0.5 - 2.5, ll: 0.3, rl: 0.5 }
    default: return { la: -0.5, ra: -0.5, ll: 0.3, rl: 0.3 }
  }
}

function animateStickman() {
  if (!stickPlaying) return
  stickFrame.value++
  const ctx = stickCanvas.value.getContext('2d')
  const w = stickCanvas.value.width, h = stickCanvas.value.height
  ctx.clearRect(0, 0, w, h)
  ctx.strokeStyle = 'rgba(217,122,92,0.3)'
  ctx.lineWidth = 2
  ctx.beginPath(); ctx.moveTo(0, h - 80); ctx.lineTo(w, h - 80); ctx.stroke()
  for (let i = 0; i < w; i += 80) { ctx.beginPath(); ctx.moveTo(i, h - 80); ctx.lineTo(i, h - 70); ctx.stroke() }
  const pose = getStickmanPose(stickScene.value, stickFrame.value)
  let x = w / 2, y = h - 80
  if (stickScene.value === 'walk' || stickScene.value === 'run') x = (stickFrame.value * 3) % (w + 100) - 50
  if (pose.offsetY) y += pose.offsetY
  ctx.fillStyle = 'rgba(217,122,92,0.15)'
  ctx.beginPath(); ctx.ellipse(x, h - 78, 20, 5, 0, 0, Math.PI * 2); ctx.fill()
  drawStickman(ctx, x, y, 1.2, pose)
  stickAnimId = requestAnimationFrame(animateStickman)
}

function playStickman() { if (stickPlaying) return; stickPlaying = true; stickStatus.value = '播放中'; animateStickman() }
function pauseStickman() { stickPlaying = false; cancelAnimationFrame(stickAnimId); stickStatus.value = '已暂停' }
function resetStickman() { stickPlaying = false; cancelAnimationFrame(stickAnimId); stickFrame.value = 0; stickCanvas.value?.getContext('2d').clearRect(0, 0, stickCanvas.value.width, stickCanvas.value.height); stickStatus.value = '准备就绪' }
watch(stickScene, () => { resetStickman(); setTimeout(playStickman, 200) })

// Pixel
const pixelScene = ref('matrix')
const pixelFrame = ref(0)
const pixelStatus = ref('准备就绪')
let pixelPlaying = false
let pixelAnimId = null
let matrixDrops = [], bounceBall = {}, snake = {}, stars = [], firePixels = []

function resizePixel() {
  if (!pixelCanvas.value) return
  const rect = pixelCanvas.value.parentElement.getBoundingClientRect()
  pixelCanvas.value.width = rect.width
  pixelCanvas.value.height = 400
}

function initPixel() {
  const w = pixelCanvas.value?.width || 900
  if (pixelScene.value === 'matrix') { const cols = Math.floor(w / 14); matrixDrops = Array(cols).fill(0).map(() => Math.random() * -50) }
  if (pixelScene.value === 'bounce') bounceBall = { x: 50, y: 50, vx: 4, vy: 3, r: 12 }
  if (pixelScene.value === 'snake') snake = { body: [{ x: 10, y: 10 }, { x: 9, y: 10 }, { x: 8, y: 10 }], dir: 'right', food: { x: 15, y: 15 } }
  if (pixelScene.value === 'starfield') stars = Array(100).fill(0).map(() => ({ x: Math.random() * w, y: Math.random() * 400, z: Math.random() * 2 + 0.5, speed: Math.random() * 3 + 1 }))
  if (pixelScene.value === 'fire') { const cw = Math.floor(w / 4); firePixels = Array(cw).fill(0) }
}

function animatePixel() {
  if (!pixelPlaying) return
  pixelFrame.value++
  const ctx = pixelCanvas.value.getContext('2d')
  const w = pixelCanvas.value.width, h = pixelCanvas.value.height
  const scene = pixelScene.value

  if (scene === 'matrix') {
    ctx.fillStyle = 'rgba(10,5,5,0.15)'; ctx.fillRect(0, 0, w, h)
    const cols = Math.floor(w / 14); ctx.font = '14px monospace'
    for (let i = 0; i < cols; i++) {
      const char = String.fromCharCode(0x30A0 + Math.random() * 96)
      const y = matrixDrops[i] * 14
      ctx.fillStyle = `rgba(217, ${122 + Math.random() * 80}, ${92 + Math.random() * 60}, ${Math.random()})`
      ctx.fillText(char, i * 14, y); matrixDrops[i]++
      if (y > h && Math.random() > 0.975) matrixDrops[i] = 0
    }
  } else if (scene === 'bounce') {
    ctx.fillStyle = '#0a0505'; ctx.fillRect(0, 0, w, h)
    bounceBall.x += bounceBall.vx; bounceBall.y += bounceBall.vy
    if (bounceBall.x - bounceBall.r < 0 || bounceBall.x + bounceBall.r > w) bounceBall.vx *= -1
    if (bounceBall.y - bounceBall.r < 0 || bounceBall.y + bounceBall.r > h) bounceBall.vy *= -1
    ctx.fillStyle = '#d97a5c'; ctx.shadowColor = '#d97a5c'; ctx.shadowBlur = 15
    ctx.beginPath(); ctx.arc(bounceBall.x, bounceBall.y, bounceBall.r, 0, Math.PI * 2); ctx.fill(); ctx.shadowBlur = 0
    ctx.fillStyle = 'rgba(217,122,92,0.3)'
    ctx.beginPath(); ctx.arc(bounceBall.x - bounceBall.vx * 2, bounceBall.y - bounceBall.vy * 2, bounceBall.r * 0.7, 0, Math.PI * 2); ctx.fill()
  } else if (scene === 'snake') {
    if (pixelFrame.value % 8 === 0) {
      const head = { ...snake.body[0] }
      if (snake.dir === 'right') head.x++; if (snake.dir === 'left') head.x--
      if (snake.dir === 'up') head.y--; if (snake.dir === 'down') head.y++
      snake.body.unshift(head)
      if (head.x === snake.food.x && head.y === snake.food.y) { snake.food = { x: Math.floor(Math.random() * 20), y: Math.floor(Math.random() * 20) } }
      else snake.body.pop()
      if (head.x >= 20) snake.dir = 'down'; if (head.y >= 20) snake.dir = 'left'; if (head.x <= 0) snake.dir = 'up'; if (head.y <= 0) snake.dir = 'right'
    }
    ctx.fillStyle = '#0a0505'; ctx.fillRect(0, 0, w, h)
    const cs = Math.min(w, h) / 22, ox = (w - cs * 22) / 2, oy = (h - cs * 22) / 2
    ctx.strokeStyle = 'rgba(217,122,92,0.1)'; ctx.lineWidth = 1
    for (let i = 0; i <= 22; i++) { ctx.beginPath(); ctx.moveTo(ox + i * cs, oy); ctx.lineTo(ox + i * cs, oy + 22 * cs); ctx.stroke(); ctx.beginPath(); ctx.moveTo(ox, oy + i * cs); ctx.lineTo(ox + 22 * cs, oy + i * cs); ctx.stroke() }
    ctx.fillStyle = '#d97a5c'
    snake.body.forEach(seg => { ctx.fillRect(ox + seg.x * cs + 1, oy + seg.y * cs + 1, cs - 2, cs - 2) })
    ctx.fillStyle = '#ffccaa'
    ctx.fillRect(ox + snake.food.x * cs + 1, oy + snake.food.y * cs + 1, cs - 2, cs - 2)
  } else if (scene === 'starfield') {
    ctx.fillStyle = 'rgba(10,5,5,0.3)'; ctx.fillRect(0, 0, w, h)
    stars.forEach(star => { star.x -= star.speed; if (star.x < 0) { star.x = w; star.y = Math.random() * h } const s = star.z; ctx.fillStyle = `rgba(255, ${200 + Math.random() * 55}, ${180 + Math.random() * 40}, ${Math.min(1, star.z / 2)})`; ctx.fillRect(star.x, star.y, s * 3, s) })
  } else if (scene === 'fire') {
    ctx.fillStyle = '#0a0505'; ctx.fillRect(0, 0, w, h)
    const cols = Math.floor(w / 4)
    if (pixelFrame.value % 2 === 0) { for (let i = 0; i < cols; i++) { firePixels[i] = Math.max(0, (firePixels[i] || 0) - Math.random() * 15); if (Math.random() > 0.85) firePixels[i] = 100 + Math.random() * 100 } }
    for (let i = 0; i < cols; i++) { const height = firePixels[i] || 0; if (height > 0) { ctx.fillStyle = `hsl(${10 + Math.random() * 20}, ${80 + Math.random() * 20}%, ${30 + height / 3}%)`; ctx.fillRect(i * 4, h - height, 3, height) } }
  } else if (scene === 'wave') {
    ctx.fillStyle = '#0a0505'; ctx.fillRect(0, 0, w, h)
    ctx.strokeStyle = '#d97a5c'; ctx.lineWidth = 2
    for (let y = 0; y < h; y += 8) { ctx.beginPath(); for (let x = 0; x < w; x += 4) { const wy = y + Math.sin((x + pixelFrame.value * 3) * 0.02) * 15 + Math.sin((x + pixelFrame.value * 2) * 0.01) * 10; if (x === 0) ctx.moveTo(x, wy); else ctx.lineTo(x, wy) } ctx.globalAlpha = 0.3 + (y / h) * 0.5; ctx.stroke() }
    ctx.globalAlpha = 1
  }
  pixelAnimId = requestAnimationFrame(animatePixel)
}

function playPixel() { if (pixelPlaying) return; pixelPlaying = true; pixelStatus.value = '播放中'; animatePixel() }
function pausePixel() { pixelPlaying = false; cancelAnimationFrame(pixelAnimId); pixelStatus.value = '已暂停' }
function resetPixel() { pixelPlaying = false; cancelAnimationFrame(pixelAnimId); pixelFrame.value = 0; pixelCanvas.value?.getContext('2d').clearRect(0, 0, pixelCanvas.value.width, pixelCanvas.value.height); pixelStatus.value = '准备就绪'; initPixel() }
watch(pixelScene, () => { resetPixel(); setTimeout(playPixel, 200) })

const onResize = () => { resizeStick(); resizePixel() }

onMounted(() => {
  resizeStick(); resizePixel()
  initPixel()
  window.addEventListener('resize', onResize)
  setTimeout(playStickman, 500)
})
onUnmounted(() => { cancelAnimationFrame(stickAnimId); cancelAnimationFrame(pixelAnimId); window.removeEventListener('resize', onResize) })
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; background: #050b1a; color: #ffe0d0; min-height: 100vh; }
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
.container { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; padding: 30px 20px; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid rgba(217,122,92,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #c05c3e, #d97a5c); box-shadow: 0 0 20px rgba(217,122,92,0.4); display: flex; align-items: center; justify-content: center; font-size: 24px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #d97a5c, #ffbba0); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(192,92,62,0.2); border: 1px solid rgba(217,122,92,0.4); color: #ffbba0; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(192,92,62,0.4); box-shadow: 0 0 15px rgba(217,122,92,0.3); }
.tabs { display: flex; gap: 10px; margin-bottom: 25px; }
.tab { padding: 10px 24px; border-radius: 30px; cursor: pointer; background: rgba(192,92,62,0.15); border: 1px solid rgba(217,122,92,0.25); color: #ffbba0; transition: 0.3s; font-size: 0.95rem; }
.tab.active, .tab:hover { background: rgba(192,92,62,0.35); border-color: rgba(217,122,92,0.6); box-shadow: 0 0 15px rgba(217,122,92,0.2); color: #fff; }
.panel { display: block; }
.stage { background: rgba(20,8,8,0.7); border: 1px solid rgba(217,122,92,0.25); border-radius: 20px; padding: 25px; margin-bottom: 25px; backdrop-filter: blur(10px); }
.stage-title { font-size: 1.3rem; font-weight: 400; margin-bottom: 5px; color: #ffbba0; letter-spacing: 2px; }
.stage-desc { font-size: 0.9rem; opacity: 0.7; margin-bottom: 20px; }
.canvas-wrapper { background: #0a0505; border-radius: 16px; border: 2px solid rgba(217,122,92,0.3); overflow: hidden; position: relative; box-shadow: inset 0 0 40px rgba(192,92,62,0.1); }
.theater-canvas { width: 100%; height: 400px; display: block; background: #0a0505; }
.controls { display: flex; gap: 12px; margin-top: 20px; flex-wrap: wrap; align-items: center; }
.ctrl-btn { padding: 10px 22px; border-radius: 30px; border: none; background: linear-gradient(135deg, #c05c3e, #d97a5c); color: white; cursor: pointer; font-size: 0.9rem; transition: 0.3s; letter-spacing: 1px; }
.ctrl-btn:hover { box-shadow: 0 0 20px rgba(217,122,92,0.4); transform: translateY(-1px); }
.ctrl-btn.secondary { background: rgba(192,92,62,0.2); border: 1px solid rgba(217,122,92,0.4); }
.ctrl-btn.secondary:hover { background: rgba(192,92,62,0.35); }
.ctrl-select { padding: 10px 18px; border-radius: 30px; background: rgba(20,8,8,0.8); border: 1px solid rgba(217,122,92,0.3); color: #ffe0d0; font-size: 0.9rem; outline: none; }
.status-bar { display: flex; gap: 20px; margin-top: 15px; font-size: 0.85rem; opacity: 0.7; }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

@media (max-width: 768px) {
  .container { padding: 15px 12px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-icon { width: 38px; height: 38px; font-size: 18px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .tabs { gap: 6px; flex-wrap: wrap; }
  .tab { padding: 8px 14px; font-size: 0.85rem; }
  .stage { padding: 18px; }
  .stage-title { font-size: 1.1rem; }
  .theater-canvas { height: 280px; }
  .controls { gap: 8px; }
  .ctrl-btn { padding: 8px 16px; font-size: 0.85rem; }
  .ctrl-select { padding: 8px 12px; font-size: 0.85rem; }
  .status-bar { font-size: 0.8rem; }
}
</style>
