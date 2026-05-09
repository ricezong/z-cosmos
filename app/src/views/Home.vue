<template>
  <div ref="sceneContainer" class="scene-container"></div>
  <div class="glow"></div>

  <!-- 顶部装饰星轨 -->
  <div class="orbit-decoration orbit-1"></div>
  <div class="orbit-decoration orbit-2"></div>

  <div class="site-title">
    <span class="site-title-text">Z的探索小站</span>
    <span class="site-title-sub">COSMOS 星域</span>
    <div class="title-particles"></div>
  </div>

  <!-- 已移除登录/注册入口（备案合规：无用户系统） -->

  <div id="function-panel">
    <div class="panel-decoration top-left"></div>
    <div class="panel-decoration top-right"></div>
    <div class="panel-decoration bottom-left"></div>
    <div class="panel-decoration bottom-right"></div>
    <div id="function-title">{{ panelTitle }}</div>
    <div id="function-desc">{{ panelDesc }}</div>
    <div id="function-action" @click="goToLink"><i class="ri-rocket-line"></i> {{ panelAction }}</div>
  </div>

  <!-- ========== 宇宙主题备案栏 ========== -->
  <footer class="cosmic-footer">
    <div class="footer-glow"></div>
    <div class="footer-content">
      <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer" class="footer-link icp-link" title="工业和信息化部备案查询">
        <i class="ri-shield-keyhole-line"></i>
        <span>粤ICP备2026052099号</span>
      </a>

      <span class="footer-divider"></span>


      <a href="https://beian.mps.gov.cn" target="_blank" rel="noopener noreferrer" class="footer-link police-link" title="公安机关互联网站安全管理服务平台">
        <i class="ri-police-badge-line"></i>
        <span>粤公网安备 44010002000000号</span>
      </a>
      <span class="footer-divider"></span>

      <span class="footer-copyright">
        <i class="ri-copyright-line"></i>
        <span>{{ new Date().getFullYear() }} COSMOS 星域</span>
      </span>
    </div>

    <div class="footer-stars">
      <span v-for="i in 8" :key="i" class="star-dot" :style="starStyle(i)"></span>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
const router = useRouter()
const sceneContainer = ref(null)

const panelTitle = ref('Z的探索小站')
const panelDesc = ref('点击任意星球，探索技术领域')
const panelAction = ref('等待探索')

let currentLink = null
function goToLink() {
  if (currentLink) router.push(currentLink)
}

let renderer, cssRenderer, scene, camera, animationId
let draggedPlanet = null, hasMoved = false, dragStartPos
let planets = []
const raycaster = new THREE.Raycaster()
const mouse = new THREE.Vector2()
let dragPlane = new THREE.Plane()
const dragOffset = new THREE.Vector3()
const intersectionPoint = new THREE.Vector3()
const boundary = { x: 9, y: 5.5, z: 7 }

function rand(min, max) { return min + Math.random() * (max - min) }
function randInt(min, max) { return Math.floor(rand(min, max + 1)) }

// 备案栏星光随机位置生成
function starStyle(index) {
  const positions = [
    { top: '-8px', left: '15%' },
    { top: '-6px', right: '20%' },
    { bottom: '-10px', left: '30%' },
    { bottom: '-8px', right: '10%' },
    { top: '50%', left: '-12px' },
    { top: '40%', right: '-10px' },
    { bottom: '20%', left: '-8px' },
    { top: '10%', right: '-14px' },
  ]
  const pos = positions[(index - 1) % positions.length]
  return {
    ...pos,
    animationDelay: `${index * 0.3}s`,
    opacity: 0.3 + Math.random() * 0.5
  }
}

/* ========== 精美地球 ========== */
function createEarthTexture() {
  const c = document.createElement('canvas')
  c.width = 1024
  c.height = 512
  const ctx = c.getContext('2d')
  const oceanGrad = ctx.createRadialGradient(512, 256, 0, 512, 256, 600)
  oceanGrad.addColorStop(0, '#1a6a8a')
  oceanGrad.addColorStop(0.4, '#0d5a7a')
  oceanGrad.addColorStop(0.7, '#0a4060')
  oceanGrad.addColorStop(1, '#062840')
  ctx.fillStyle = oceanGrad
  ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 8; i++) {
    const y = 80 + i * 50
    ctx.beginPath()
    ctx.moveTo(0, y)
    for (let x = 0; x <= 1024; x += 20) {
      const wave = Math.sin(x * 0.008 + i * 0.5) * 3 + Math.sin(x * 0.015) * 2
      ctx.lineTo(x, y + wave)
    }
    ctx.strokeStyle = `rgba(30, 100, 140, ${0.15 - i * 0.01})`
    ctx.lineWidth = 1
    ctx.stroke()
  }
  const continents = [
    { cx: 180, cy: 140, points: [[90,60],[130,40],[170,50],[200,70],[220,100],[230,140],[220,180],[200,210],[170,230],[140,220],[110,190],[90,150],[80,100]], land: '#3a7a3a', highlight: '#4a9a4a', shadow: '#2a5a2a' },
    { cx: 260, cy: 350, points: [[220,250],[250,230],[280,240],[310,260],[320,300],[310,350],[290,400],[260,420],[230,410],[210,370],[200,310]], land: '#4a8a4a', highlight: '#5aaa5a', shadow: '#3a6a3a' },
    { cx: 500, cy: 120, points: [[450,60],[480,50],[520,60],[550,80],[560,110],[540,130],[510,140],[480,130],[460,110]], land: '#5a9a5a', highlight: '#6aba6a', shadow: '#4a7a4a' },
    { cx: 530, cy: 310, points: [[470,200],[510,180],[550,190],[580,220],[600,280],[590,340],[560,390],[520,410],[480,390],[450,340],[440,280],[450,230]], land: '#6a8a5a', highlight: '#7a9a6a', shadow: '#5a7a4a' },
    { cx: 750, cy: 180, points: [[620,60],[680,50],[750,60],[820,80],[880,110],[920,150],[940,200],[930,260],[900,300],[850,320],[800,300],[750,270],[720,220],[680,170],[640,120]], land: '#5a9a5a', highlight: '#6aba6a', shadow: '#4a7a4a' },
    { cx: 860, cy: 400, points: [[820,350],[850,340],[880,350],[900,380],[890,420],[860,440],[830,430],[810,400]], land: '#6aaa6a', highlight: '#7aba7a', shadow: '#5a8a5a' }
  ]
  continents.forEach(continent => {
    ctx.beginPath()
    ctx.moveTo(continent.points[0][0], continent.points[0][1])
    for (let i = 1; i < continent.points.length; i++) ctx.lineTo(continent.points[i][0], continent.points[i][1])
    ctx.closePath()
    const landGrad = ctx.createRadialGradient(continent.cx - 30, continent.cy - 20, 0, continent.cx, continent.cy, 150)
    landGrad.addColorStop(0, continent.highlight)
    landGrad.addColorStop(0.5, continent.land)
    landGrad.addColorStop(1, continent.shadow)
    ctx.fillStyle = landGrad
    ctx.fill()
    ctx.strokeStyle = 'rgba(20, 60, 20, 0.6)'
    ctx.lineWidth = 2
    ctx.stroke()
  })
  const highlands = [
    { x: 750, y: 150, rx: 80, ry: 50 }, { x: 600, y: 200, rx: 40, ry: 30 },
    { x: 180, y: 180, rx: 30, ry: 20 }, { x: 500, y: 100, rx: 25, ry: 15 }
  ]
  highlands.forEach(h => {
    const hGrad = ctx.createRadialGradient(h.x, h.y, 0, h.x, h.y, Math.max(h.rx, h.ry))
    hGrad.addColorStop(0, 'rgba(160, 140, 100, 0.5)')
    hGrad.addColorStop(1, 'rgba(100, 80, 60, 0)')
    ctx.fillStyle = hGrad
    ctx.ellipse(h.x, h.y, h.rx, h.ry, 0, 0, Math.PI * 2)
    ctx.fill()
  })
  ctx.fillStyle = 'rgba(210, 180, 120, 0.35)'
  ctx.beginPath()
  ctx.ellipse(520, 240, 70, 40, 0, 0, Math.PI * 2)
  ctx.fill()
  const cityLights = [
    [160, 150], [180, 160], [170, 170], [200, 140], [150, 180], [190, 155], [210, 200], [140, 190],
    [480, 110], [500, 120], [490, 130], [510, 105], [520, 115],
    [820, 140], [840, 150], [800, 130], [760, 160], [880, 180], [850, 170], [900, 200], [780, 190], [920, 160],
    [260, 300], [550, 280], [860, 380]
  ]
  cityLights.forEach(([x, y], i) => {
    const size = rand(1.5, 3)
    const brightness = rand(0.7, 1)
    const glow = ctx.createRadialGradient(x, y, 0, x, y, 12)
    glow.addColorStop(0, `rgba(255, 240, 180, ${brightness * 0.5})`)
    glow.addColorStop(0.5, `rgba(255, 220, 120, ${brightness * 0.2})`)
    glow.addColorStop(1, 'rgba(255, 220, 120, 0)')
    ctx.fillStyle = glow
    ctx.beginPath()
    ctx.arc(x, y, 12, 0, Math.PI * 2)
    ctx.fill()
    ctx.beginPath()
    ctx.arc(x, y, size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255, 255, 220, ${brightness})`
    ctx.fill()
  })
  ctx.globalAlpha = 0.3
  const cloudPatterns = [
    { x: 200, y: 100, w: 180, h: 50, opacity: 0.6 }, { x: 600, y: 80, w: 200, h: 45, opacity: 0.5 },
    { x: 850, y: 120, w: 150, h: 40, opacity: 0.55 }, { x: 400, y: 200, w: 120, h: 35, opacity: 0.4 },
    { x: 700, y: 300, w: 160, h: 50, opacity: 0.5 }, { x: 150, y: 250, w: 80, h: 25, opacity: 0.4 },
    { x: 450, y: 350, w: 100, h: 30, opacity: 0.45 }, { x: 900, y: 280, w: 90, h: 28, opacity: 0.4 }
  ]
  cloudPatterns.forEach(cloud => {
    for (let i = 0; i < 5; i++) {
      const cx = cloud.x + rand(-cloud.w * 0.3, cloud.w * 0.3)
      const cy = cloud.y + rand(-cloud.h * 0.2, cloud.h * 0.2)
      const cw = cloud.w * rand(0.4, 0.8)
      const ch = cloud.h * rand(0.5, 1)
      const cloudGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, cw / 2)
      cloudGrad.addColorStop(0, `rgba(255, 255, 255, ${cloud.opacity * rand(0.6, 1)})`)
      cloudGrad.addColorStop(0.5, `rgba(240, 245, 250, ${cloud.opacity * 0.5 * rand(0.6, 1)})`)
      cloudGrad.addColorStop(1, 'rgba(240, 245, 250, 0)')
      ctx.fillStyle = cloudGrad
      ctx.save()
      ctx.translate(cx, cy)
      ctx.scale(1, ch / cw)
      ctx.beginPath()
      ctx.arc(0, 0, cw / 2, 0, Math.PI * 2)
      ctx.fill()
      ctx.restore()
    }
  })
  ctx.globalAlpha = 1
  const northCap = ctx.createLinearGradient(0, 0, 0, 80)
  northCap.addColorStop(0, 'rgba(255, 255, 255, 0.95)')
  northCap.addColorStop(0.3, 'rgba(240, 250, 255, 0.8)')
  northCap.addColorStop(1, 'rgba(220, 240, 255, 0)')
  ctx.fillStyle = northCap
  ctx.fillRect(0, 0, 1024, 80)
  ctx.globalAlpha = 0.3
  for (let i = 0; i < 5; i++) {
    const x = rand(100, 900)
    ctx.beginPath()
    ctx.arc(x, rand(20, 60), rand(30, 80), 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255, 255, 255, 0.4)'
    ctx.fill()
  }
  ctx.globalAlpha = 1
  const southCap = ctx.createLinearGradient(0, 512, 0, 432)
  southCap.addColorStop(0, 'rgba(255, 255, 255, 0.95)')
  southCap.addColorStop(0.3, 'rgba(240, 250, 255, 0.8)')
  southCap.addColorStop(1, 'rgba(220, 240, 255, 0)')
  ctx.fillStyle = southCap
  ctx.fillRect(0, 432, 1024, 80)
  ctx.globalAlpha = 0.3
  for (let i = 0; i < 5; i++) {
    const x = rand(100, 900)
    ctx.beginPath()
    ctx.arc(x, rand(450, 490), rand(30, 80), 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255, 255, 255, 0.4)'
    ctx.fill()
  }
  ctx.globalAlpha = 1
  return new THREE.CanvasTexture(c)
}

/* ========== 精美火星 ========== */
function createMarsTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512; const ctx = c.getContext('2d')
  const baseGrad = ctx.createLinearGradient(0, 0, 0, 512)
  baseGrad.addColorStop(0, '#9c5235'); baseGrad.addColorStop(0.3, '#c96d4a'); baseGrad.addColorStop(0.5, '#b86040'); baseGrad.addColorStop(0.7, '#c96d4a'); baseGrad.addColorStop(1, '#9c5235')
  ctx.fillStyle = baseGrad; ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 60; i++) {
    const x = rand(0, 1024), y = rand(0, 512), r = rand(30, 100)
    const col = ctx.createRadialGradient(x, y, 0, x, y, r)
    col.addColorStop(0, Math.random() > 0.5 ? 'rgba(140, 60, 30, 0.3)' : 'rgba(220, 160, 120, 0.25)')
    col.addColorStop(1, 'rgba(0, 0, 0, 0)')
    ctx.fillStyle = col; ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2); ctx.fill()
  }
  ctx.strokeStyle = 'rgba(80, 35, 15, 0.5)'; ctx.lineWidth = 4; ctx.beginPath()
  ctx.moveTo(100, 280); ctx.bezierCurveTo(350, 260, 600, 300, 900, 270); ctx.stroke()
  for (let i = 0; i < 35; i++) {
    const x = rand(0, 1024), y = rand(0, 512), r = rand(5, 18)
    ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2)
    const craterGrad = ctx.createRadialGradient(x - r * 0.2, y - r * 0.2, 0, x, y, r)
    craterGrad.addColorStop(0, 'rgba(160, 90, 60, 0.6)'); craterGrad.addColorStop(0.7, 'rgba(140, 70, 45, 0.3)'); craterGrad.addColorStop(1, 'rgba(100, 50, 30, 0)')
    ctx.fillStyle = craterGrad; ctx.fill()
  }
  const northCap = ctx.createRadialGradient(512, 20, 0, 512, 20, 120)
  northCap.addColorStop(0, 'rgba(255, 248, 240, 0.9)'); northCap.addColorStop(1, 'rgba(240, 235, 230, 0)')
  ctx.fillStyle = northCap; ctx.fillRect(0, 0, 1024, 140)
  const southCap = ctx.createRadialGradient(512, 492, 0, 512, 492, 120)
  southCap.addColorStop(0, 'rgba(255, 248, 240, 0.9)'); southCap.addColorStop(1, 'rgba(240, 235, 230, 0)')
  ctx.fillStyle = southCap; ctx.fillRect(0, 372, 1024, 140)
  return new THREE.CanvasTexture(c)
}

/* ========== 精美木星 ========== */
function createJupiterTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512; const ctx = c.getContext('2d')
  const bands = [
    { y: 0, h: 50, col: '#f0dcc4' }, { y: 50, h: 60, col: '#c89060' }, { y: 110, h: 55, col: '#d8b884' },
    { y: 165, h: 70, col: '#a87848' }, { y: 235, h: 65, col: '#e8cca8' }, { y: 300, h: 60, col: '#b08858' },
    { y: 360, h: 55, col: '#d0a870' }, { y: 415, h: 50, col: '#a07040' }, { y: 465, h: 47, col: '#c8a070' }
  ]
  bands.forEach(b => {
    const grad = ctx.createLinearGradient(0, b.y, 0, b.y + b.h)
    grad.addColorStop(0, b.col); grad.addColorStop(0.5, b.col); grad.addColorStop(1, b.col)
    ctx.fillStyle = grad; ctx.fillRect(0, b.y, 1024, b.h)
  })
  for (let y = 0; y < 512; y += 4) {
    ctx.beginPath(); ctx.moveTo(0, y)
    for (let x = 0; x < 1024; x += 16) { const wave = Math.sin(y * 0.06 + x * 0.02) * 2; ctx.lineTo(x, y + wave) }
    ctx.strokeStyle = 'rgba(80, 50, 30, 0.1)'; ctx.lineWidth = 1; ctx.stroke()
  }
  const sx = 700, sy = 320
  const spotGrad = ctx.createRadialGradient(sx, sy, 0, sx, sy, 60)
  spotGrad.addColorStop(0, '#e05030'); spotGrad.addColorStop(0.4, '#c04020'); spotGrad.addColorStop(0.8, 'rgba(100, 40, 20, 0.4)'); spotGrad.addColorStop(1, 'rgba(80, 30, 15, 0)')
  ctx.beginPath(); ctx.ellipse(sx, sy, 60, 35, 0, 0, Math.PI * 2); ctx.fillStyle = spotGrad; ctx.fill()
  ctx.beginPath(); ctx.ellipse(sx - 10, sy - 8, 35, 20, 0.2, 0, Math.PI * 2)
  ctx.strokeStyle = 'rgba(200, 100, 60, 0.4)'; ctx.lineWidth = 2; ctx.stroke()
  for (let i = 0; i < 8; i++) {
    const x = rand(50, 950), y = rand(50, 450), r = rand(8, 20)
    ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2)
    const stormGrad = ctx.createRadialGradient(x, y, 0, x, y, r)
    stormGrad.addColorStop(0, 'rgba(180, 140, 100, 0.4)'); stormGrad.addColorStop(1, 'rgba(150, 110, 80, 0)')
    ctx.fillStyle = stormGrad; ctx.fill()
  }
  return new THREE.CanvasTexture(c)
}

/* ========== 精美冰巨星 ========== */
function createIceGiantTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512; const ctx = c.getContext('2d')
  const baseGrad = ctx.createLinearGradient(0, 0, 0, 512)
  baseGrad.addColorStop(0, '#1a6080'); baseGrad.addColorStop(0.5, '#40a0c8'); baseGrad.addColorStop(1, '#1a6080')
  ctx.fillStyle = baseGrad; ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 20; i++) {
    const y = i * 26 + 8; ctx.beginPath(); ctx.moveTo(0, y)
    for (let x = 0; x <= 1024; x += 24) { const wave = Math.sin(x * 0.01 + i * 0.5) * 3; ctx.lineTo(x, y + wave) }
    ctx.strokeStyle = 'rgba(200, 240, 255, 0.12)'; ctx.lineWidth = 2; ctx.stroke()
  }
  for (let i = 0; i < 15; i++) {
    const x = rand(0, 1024), y = rand(30, 480), w = rand(50, 150), h = rand(15, 40)
    const cloudGrad = ctx.createRadialGradient(x, y, 0, x, y, w / 2)
    cloudGrad.addColorStop(0, 'rgba(220, 250, 255, 0.3)'); cloudGrad.addColorStop(1, 'rgba(200, 240, 255, 0)')
    ctx.fillStyle = cloudGrad; ctx.save(); ctx.translate(x, y); ctx.scale(1, h / w)
    ctx.beginPath(); ctx.arc(0, 0, w / 2, 0, Math.PI * 2); ctx.fill(); ctx.restore()
  }
  const northCap = ctx.createLinearGradient(0, 0, 0, 50)
  northCap.addColorStop(0, 'rgba(200, 240, 255, 0.5)'); northCap.addColorStop(1, 'rgba(180, 230, 255, 0)')
  ctx.fillStyle = northCap; ctx.fillRect(0, 0, 1024, 50)
  return new THREE.CanvasTexture(c)
}

/* ========== 精美紫微星 ========== */
function createNebulaTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512; const ctx = c.getContext('2d')
  const bgGrad = ctx.createRadialGradient(512, 256, 0, 512, 256, 400)
  bgGrad.addColorStop(0, '#1a0848'); bgGrad.addColorStop(0.5, '#100530'); bgGrad.addColorStop(1, '#05010d')
  ctx.fillStyle = bgGrad; ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 20; i++) {
    const x = rand(0, 1024), y = rand(0, 512), r = rand(80, 200)
    const nebulaGrad = ctx.createRadialGradient(x, y, 0, x, y, r)
    const hue = Math.random() > 0.5 ? `rgba(200, 150, 255, ${rand(0.1, 0.2)})` : `rgba(150, 200, 255, ${rand(0.08, 0.15)})`
    nebulaGrad.addColorStop(0, hue); nebulaGrad.addColorStop(1, 'transparent')
    ctx.fillStyle = nebulaGrad; ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2); ctx.fill()
  }
  for (let i = 0; i < 250; i++) {
    const x = rand(0, 1024), y = rand(0, 512), size = rand(0.5, 2.5), brightness = rand(0.4, 1)
    const glow = ctx.createRadialGradient(x, y, 0, x, y, size * 4)
    glow.addColorStop(0, `rgba(255, 250, 230, ${brightness})`); glow.addColorStop(1, 'transparent')
    ctx.fillStyle = glow; ctx.fillRect(x - size * 4, y - size * 4, size * 8, size * 8)
    ctx.beginPath(); ctx.arc(x, y, size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255, ${randInt(245, 255)}, ${randInt(225, 245)}, ${brightness})`; ctx.fill()
  }
  for (let i = 0; i < 10; i++) {
    const x = rand(50, 975), y = rand(50, 460), size = rand(3, 6)
    const outerGlow = ctx.createRadialGradient(x, y, 0, x, y, size * 6)
    outerGlow.addColorStop(0, 'rgba(255, 240, 200, 0.4)'); outerGlow.addColorStop(1, 'transparent')
    ctx.fillStyle = outerGlow; ctx.fillRect(x - size * 6, y - size * 6, size * 12, size * 12)
    ctx.beginPath(); ctx.arc(x, y, size * 0.4, 0, Math.PI * 2); ctx.fillStyle = '#ffffff'; ctx.fill()
  }
  return new THREE.CanvasTexture(c)
}

/* 大气层效果 */
function createAtmosphere(radius, hexColor, opacity = 0.15) {
  const group = new THREE.Group()
  const mainAtmo = new THREE.Mesh(new THREE.SphereGeometry(radius, 48, 48), new THREE.MeshBasicMaterial({ color: hexColor, transparent: true, opacity, side: THREE.BackSide }))
  group.add(mainAtmo)
  const outerAtmo = new THREE.Mesh(new THREE.SphereGeometry(radius * 1.06, 32, 32), new THREE.MeshBasicMaterial({ color: hexColor, transparent: true, opacity: opacity * 0.35, side: THREE.BackSide }))
  group.add(outerAtmo)
  return group
}

function createLabel(text, color = '#ffffff') {
  const div = document.createElement('div')
  div.textContent = text
  div.style.cssText = `color: ${color}; font-size: 16px; font-weight: 500; letter-spacing: 3px; font-family: 'Cinzel', 'Noto Serif SC', serif; text-shadow: 0 0 20px rgba(0, 0, 0, 0.9), 0 0 40px rgba(80, 120, 200, 0.3); background: linear-gradient(135deg, rgba(15, 25, 45, 0.65), rgba(10, 15, 30, 0.5)); padding: 6px 18px; border-radius: 20px; backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.12); pointer-events: none; white-space: nowrap;`
  return new CSS2DObject(div)
}

function handleBoundary(p) {
  const vel = p.userData.velocity, r = p.userData.radius
  ;['x', 'y', 'z'].forEach(axis => {
    if (p.position[axis] > boundary[axis] - r) { p.position[axis] = boundary[axis] - r; vel[axis] *= -0.5 }
    else if (p.position[axis] < -boundary[axis] + r) { p.position[axis] = -boundary[axis] + r; vel[axis] *= -0.5 }
  })
}

function handleCollisions() {
  for (let i = 0; i < planets.length; i++) {
    for (let j = i + 1; j < planets.length; j++) {
      const a = planets[i], b = planets[j]
      const delta = new THREE.Vector3().subVectors(a.position, b.position)
      const dist = delta.length(), minDist = a.userData.radius + b.userData.radius
      if (dist < minDist) {
        const correction = (minDist - dist) / 2, norm = delta.clone().normalize()
        a.position.add(norm.clone().multiplyScalar(correction))
        b.position.add(norm.clone().multiplyScalar(-correction))
        const va = a.userData.velocity, vb = b.userData.velocity
        const impulse = norm.clone().multiplyScalar((1 + 0.3) * va.dot(norm) - vb.dot(norm))
        va.sub(impulse.clone().multiplyScalar(0.5))
        vb.add(impulse.clone().multiplyScalar(0.5))
      }
    }
  }
}

function onMouseDown(event) {
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(planets, true)
  if (intersects.length > 0) {
    let target = intersects[0].object
    while (target.parent && !target.userData.radius) target = target.parent
    if (target.userData.radius) {
      draggedPlanet = target
      dragPlane.setFromNormalAndCoplanarPoint(camera.getWorldDirection(dragPlane.normal).negate(), draggedPlanet.position)
      if (raycaster.ray.intersectPlane(dragPlane, intersectionPoint)) dragOffset.copy(draggedPlanet.position).sub(intersectionPoint)
      dragStartPos = draggedPlanet.position.clone()
      hasMoved = false
      event.preventDefault()
      renderer.domElement.style.cursor = 'grabbing'
    }
  }
}

function onMouseMove(event) {
  if (!draggedPlanet) return
  if (event.buttons !== 1) { draggedPlanet = null; renderer.domElement.style.cursor = 'grab'; return }
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(mouse, camera)
  if (raycaster.ray.intersectPlane(dragPlane, intersectionPoint)) {
    const newPos = intersectionPoint.clone().add(dragOffset)
    const r = draggedPlanet.userData.radius
    newPos.x = Math.min(boundary.x - r, Math.max(-boundary.x + r, newPos.x))
    newPos.y = Math.min(boundary.y - r, Math.max(-boundary.y + r, newPos.y))
    newPos.z = Math.min(boundary.z - r, Math.max(-boundary.z + r, newPos.z))
    if (newPos.distanceTo(dragStartPos) > 0.3) hasMoved = true
    for (let other of planets) {
      if (other === draggedPlanet) continue
      const dist = newPos.distanceTo(other.position), minDist = draggedPlanet.userData.radius + other.userData.radius
      if (dist < minDist) {
        const norm = new THREE.Vector3().subVectors(newPos, other.position).normalize()
        newPos.copy(other.position.clone().add(norm.multiplyScalar(minDist + 0.1)))
        other.userData.velocity.add(norm.clone().multiplyScalar(0.15))
        draggedPlanet.userData.velocity.add(norm.clone().multiplyScalar(-0.1))
        break
      }
    }
    draggedPlanet.position.copy(newPos)
  }
}

function onMouseUp() {
  if (draggedPlanet) {
    if (!hasMoved) {
      const data = draggedPlanet.userData
      currentLink = data.link
      panelTitle.value = data.func
      panelDesc.value = `${data.name} · ${data.desc}`
      panelAction.value = `前往 ${data.func}`
    }
    draggedPlanet = null
    renderer.domElement.style.cursor = 'grab'
  }
  hasMoved = false
}

const onWindowBlur = () => { if (renderer) { draggedPlanet = null; renderer.domElement.style.cursor = 'grab' } }

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
  cssRenderer.setSize(window.innerWidth, window.innerHeight)
}

function init() {
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x030812)
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.1
  renderer.domElement.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;display:block;'
  sceneContainer.value.appendChild(renderer.domElement)
  cssRenderer = new CSS2DRenderer()
  cssRenderer.setSize(window.innerWidth, window.innerHeight)
  cssRenderer.domElement.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;pointer-events:none;'
  sceneContainer.value.appendChild(cssRenderer.domElement)
  camera = new THREE.PerspectiveCamera(42, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(0, 2, 22)
  camera.lookAt(0, 0, 0)
  scene.add(new THREE.AmbientLight(0x1a2040, 0.6))
  const dirLight = new THREE.DirectionalLight(0xfff0dd, 1.8)
  dirLight.position.set(8, 10, 6)
  dirLight.castShadow = true
  dirLight.shadow.mapSize.set(1024, 1024)
  const d = 15
  dirLight.shadow.camera.left = -d; dirLight.shadow.camera.right = d; dirLight.shadow.camera.top = d; dirLight.shadow.camera.bottom = -d
  dirLight.shadow.camera.near = 1; dirLight.shadow.camera.far = 30; dirLight.shadow.bias = -0.001
  scene.add(dirLight)
  const fillLight = new THREE.DirectionalLight(0x4488ff, 0.6)
  fillLight.position.set(-6, 3, -8)
  scene.add(fillLight)
  const warmPoint = new THREE.PointLight(0xff7744, 0.4)
  warmPoint.position.set(-4, -3, 5)
  scene.add(warmPoint)
  const coolPoint = new THREE.PointLight(0x6644cc, 0.5)
  coolPoint.position.set(5, -2, -12)
  scene.add(coolPoint)
  for (let L = 0; L < 3; L++) {
    const sg = new THREE.BufferGeometry()
    const c = [4000, 2000, 800][L], sz = [0.12, 0.08, 0.05][L], cl = [0xffffff, 0xddeeff, 0xaabbff][L]
    const sp = new Float32Array(c * 3)
    for (let i = 0; i < c * 3; i += 3) {
      const r = 60 + L * 80 + Math.random() * 120, th = Math.random() * Math.PI * 2, ph = Math.acos(Math.random() * 2 - 1)
      sp[i] = Math.sin(ph) * Math.cos(th) * r; sp[i + 1] = Math.sin(ph) * Math.sin(th) * r; sp[i + 2] = Math.cos(ph) * r
    }
    sg.setAttribute('position', new THREE.BufferAttribute(sp, 3))
    scene.add(new THREE.Points(sg, new THREE.PointsMaterial({ color: cl, size: sz, transparent: true, opacity: 0.7 - L * 0.15 })))
  }
  scene.fog = new THREE.FogExp2(0x030812, 0.003)
  const tex1 = createEarthTexture()
  const planet1 = new THREE.Mesh(new THREE.SphereGeometry(1.6, 64, 64), new THREE.MeshStandardMaterial({ map: tex1, roughness: 0.7, metalness: 0.05 }))
  planet1.castShadow = planet1.receiveShadow = true
  planet1.add(createAtmosphere(1.72, 0x66a3ff, 0.2))
  planet1.userData = { name: '地球', func: '技术笔记', desc: '站长技术笔记，知识共享', link: '/notes', spin: 0.004 }
  const tex2 = createMarsTexture()
  const planet2 = new THREE.Mesh(new THREE.SphereGeometry(1.4, 64, 64), new THREE.MeshStandardMaterial({ map: tex2, roughness: 0.88, metalness: 0 }))
  planet2.castShadow = planet2.receiveShadow = true
  planet2.add(createAtmosphere(1.48, 0xffaa88, 0.1))
  planet2.userData = { name: '火星', func: '剧场区', desc: '火柴人与像素剧场', link: '/theater', spin: 0.006 }
  const tex3 = createJupiterTexture()
  const planet3 = new THREE.Mesh(new THREE.SphereGeometry(2.0, 64, 64), new THREE.MeshStandardMaterial({ map: tex3, roughness: 0.6, metalness: 0 }))
  planet3.castShadow = planet3.receiveShadow = true
  planet3.add(createAtmosphere(2.12, 0xf0d5a0, 0.14))
  planet3.userData = { name: '木星', func: '热点区', desc: '每日热点新闻汇聚', link: '/hot', spin: 0.009 }
  const tex4 = createIceGiantTexture()
  const planet4 = new THREE.Mesh(new THREE.SphereGeometry(1.5, 64, 64), new THREE.MeshStandardMaterial({ map: tex4, roughness: 0.35, metalness: 0.12 }))
  planet4.castShadow = planet4.receiveShadow = true
  planet4.add(createAtmosphere(1.6, 0x88ccee, 0.22))
  planet4.userData = { name: '冰巨星', func: '技能展示', desc: '个人技术栈展示', link: '/skills', spin: 0.003 }
  const ringInner = new THREE.Mesh(new THREE.TorusGeometry(2.1, 0.08, 16, 200), new THREE.MeshStandardMaterial({ color: 0xd8e8f5, transparent: true, opacity: 0.8, side: THREE.DoubleSide }))
  const ringOuter = new THREE.Mesh(new THREE.TorusGeometry(2.45, 0.12, 16, 200), new THREE.MeshStandardMaterial({ color: 0xc0d5ea, transparent: true, opacity: 0.55, side: THREE.DoubleSide }))
  ringInner.rotation.x = ringOuter.rotation.x = Math.PI / 2.8
  ringInner.rotation.z = ringOuter.rotation.z = 0.3
  planet4.add(ringInner)
  planet4.add(ringOuter)
  const tex5 = createNebulaTexture()
  const planet5 = new THREE.Mesh(new THREE.SphereGeometry(1.3, 64, 64), new THREE.MeshStandardMaterial({ map: tex5, roughness: 0.45, metalness: 0.15, emissive: 0x3a1e5a, emissiveIntensity: 0.35 }))
  planet5.castShadow = planet5.receiveShadow = true
  planet5.add(createAtmosphere(1.4, 0xc89aff, 0.25))
  planet5.userData = { name: '紫微星', func: '搜索区', desc: '全站信息聚合搜索', link: '/search', spin: 0.005 }
  const planetMeshes = [planet1, planet2, planet3, planet4, planet5]
  const GOLDEN = Math.PI * (3 - Math.sqrt(5))
  planetMeshes.forEach((p, i) => {
    const angle = i * GOLDEN
    const dist = 3.2 + i * 0.55
    const y = (i - 2) * 0.9 + Math.sin(i * 1.2) * 0.3
    p.position.set(Math.cos(angle) * dist, y, Math.sin(angle) * dist * 0.6)
    scene.add(p)
    const label = createLabel(p.userData.name, '#e0f0ff')
    const r = p.geometry.parameters.radius
    label.position.set(0, r + 0.75, 0)
    p.add(label)
    p.userData.velocity = new THREE.Vector3((Math.random() - 0.5) * 0.012, (Math.random() - 0.5) * 0.012, (Math.random() - 0.5) * 0.012)
    p.userData.radius = p.geometry.parameters.radius
    p.userData.mass = p.userData.radius * 1.5
    planets.push(p)
  })
  ;[0x6688cc, 0xcc88ff, 0xffaa66].forEach(col => {
    const g = new THREE.BufferGeometry()
    const p = new Float32Array(750)
    for (let i = 0; i < 750; i += 3) { p[i] = (Math.random() - 0.5) * 35; p[i + 1] = (Math.random() - 0.5) * 20; p[i + 2] = (Math.random() - 0.5) * 30 }
    g.setAttribute('position', new THREE.BufferAttribute(p, 3))
    scene.add(new THREE.Points(g, new THREE.PointsMaterial({ color: col, size: 0.04, transparent: true, opacity: 0.4 })))
  })
  renderer.domElement.style.cursor = 'grab'
  renderer.domElement.addEventListener('mousedown', onMouseDown)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
  window.addEventListener('blur', onWindowBlur)
  window.addEventListener('resize', onWindowResize)
}

function animate() {
  animationId = requestAnimationFrame(animate)
  planets.forEach(p => {
    if (p !== draggedPlanet) { p.position.add(p.userData.velocity); p.userData.velocity.multiplyScalar(0.995); handleBoundary(p) }
  })
  handleCollisions()
  planets.forEach(p => { p.rotation.y += p.userData.spin || 0.005 })
  renderer.render(scene, camera)
  cssRenderer.render(scene, camera)
}

onMounted(() => { init(); animate() })

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', onWindowResize)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
  window.removeEventListener('blur', onWindowBlur)
  renderer?.domElement?.removeEventListener('mousedown', onMouseDown)
  if (renderer?.domElement?.parentNode) renderer.domElement.parentNode.removeChild(renderer.domElement)
  if (cssRenderer?.domElement?.parentNode) cssRenderer.domElement.parentNode.removeChild(cssRenderer.domElement)
  renderer?.dispose()
})
</script>

<style scoped>
.orbit-decoration { position: absolute; border: 1px dashed rgba(100, 180, 255, 0.08); border-radius: 50%; pointer-events: none; z-index: 2; animation: orbitRotate 60s linear infinite; }
.orbit-1 { width: 300px; height: 300px; top: -80px; right: -60px; }
.orbit-2 { width: 450px; height: 450px; top: -150px; right: -130px; border-color: rgba(200, 150, 255, 0.06); animation-duration: 90s; animation-direction: reverse; }
@keyframes orbitRotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.scene-container { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 0; overflow: hidden; }

.site-title { position: absolute; top: 24px; left: 30px; z-index: 15; display: flex; align-items: baseline; gap: 10px; }
.site-title-text { font-size: 1.7rem; font-weight: 600; letter-spacing: 6px; font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif); background: linear-gradient(135deg, #fff 0%, #e0d4ff 25%, #c8b8ff 50%, #e0d4ff 75%, #fff 100%); background-size: 200% 200%; -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; filter: drop-shadow(0 0 20px rgba(180, 160, 255, 0.6)) drop-shadow(0 0 40px rgba(180, 160, 255, 0.3)); animation: titleShimmer 4s ease-in-out infinite; }
.site-title-sub { font-size: 0.85rem; letter-spacing: 5px; color: rgba(180, 170, 220, 0.75); text-transform: uppercase; font-family: var(--font-display, 'Cinzel', serif); position: relative; }
.site-title-sub::before { content: ''; position: absolute; left: -12px; top: 50%; transform: translateY(-50%); width: 6px; height: 6px; background: radial-gradient(circle, rgba(200, 180, 255, 0.9), transparent); border-radius: 50%; animation: dotPulse 2s ease-in-out infinite; }
@keyframes titleShimmer { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
@keyframes dotPulse { 0%, 100% { opacity: 0.5; transform: translateY(-50%) scale(1); } 50% { opacity: 1; transform: translateY(-50%) scale(1.3); } }

.title-particles { position: absolute; top: -10px; left: -20px; right: -20px; bottom: -10px; pointer-events: none; background: radial-gradient(circle at 20% 50%, rgba(180, 160, 255, 0.08) 0%, transparent 30%), radial-gradient(circle at 80% 30%, rgba(200, 180, 255, 0.06) 0%, transparent 25%); animation: particleFloat 8s ease-in-out infinite; }
@keyframes particleFloat { 0%, 100% { opacity: 0.6; transform: translateY(0); } 50% { opacity: 1; transform: translateY(-5px); } }

.glow { position: absolute; width: 100%; height: 100%; pointer-events: none; background: radial-gradient(circle at 20% 30%, rgba(30, 60, 120, 0.12) 0%, transparent 45%), radial-gradient(circle at 80% 70%, rgba(60, 30, 80, 0.1) 0%, transparent 40%); z-index: 1; }

.star-login { position: absolute; top: 18px; right: 26px; z-index: 15; width: 70px; height: 70px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.star-login:hover { transform: scale(1.1); }
.portal-ring { position: absolute; border-radius: 50%; pointer-events: none; transition: all 0.4s ease; }
.portal-ring.outer { inset: 0; border: 1px dashed rgba(197, 213, 234, 0.6); animation: portalSpin 16s linear infinite; box-shadow: 0 0 16px rgba(168, 188, 212, 0.35), inset 0 0 12px rgba(168, 188, 212, 0.15); }
.portal-ring.middle { inset: 8px; border: 1px solid rgba(168, 188, 212, 0.25); animation: portalSpin 24s linear infinite reverse; background: radial-gradient(circle, rgba(168, 188, 212, 0.1) 0%, transparent 70%); }
.portal-ring.inner { inset: 16px; border: 1px solid rgba(197, 213, 234, 0.45); background: radial-gradient(circle, rgba(197, 213, 234, 0.25) 0%, rgba(150, 180, 220, 0.12) 50%, transparent 100%); animation: portalBreath 3.5s ease-in-out infinite; }
.portal-core { position: relative; width: 16px; height: 16px; border-radius: 50%; background: radial-gradient(circle at 35% 35%, #ffffff 0%, #e0ecff 30%, #a0c0e0 70%, #6080b0 100%); box-shadow: 0 0 8px #ffffff, 0 0 16px rgba(197, 213, 234, 0.95), 0 0 32px rgba(168, 188, 212, 0.6), 0 0 50px rgba(140, 170, 210, 0.3); z-index: 2; animation: coreTwinkle 2.6s ease-in-out infinite; }
.portal-glow { position: absolute; inset: -15px; border-radius: 50%; background: radial-gradient(circle, rgba(168, 188, 212, 0.15) 0%, rgba(140, 170, 210, 0.08) 40%, transparent 70%); animation: glowPulse 3s ease-in-out infinite; z-index: 1; }
.portal-arrow { position: absolute; bottom: 14px; right: 14px; font-size: 12px; color: #e8eef7; display: flex; align-items: center; justify-content: center; opacity: 0.8; transition: all 0.3s ease; z-index: 3; text-shadow: 0 0 8px rgba(168, 188, 212, 0.9); }
.portal-particles { position: absolute; inset: 0; pointer-events: none; }
.portal-particles::before, .portal-particles::after { content: ''; position: absolute; width: 4px; height: 4px; background: rgba(197, 213, 234, 0.7); border-radius: 50%; animation: particleOrbit 4s linear infinite; }
.portal-particles::before { top: 50%; left: -2px; animation-delay: 0s; }
.portal-particles::after { bottom: 50%; right: -2px; animation-delay: -2s; }
@keyframes particleOrbit { 0% { transform: rotate(0deg) translateX(35px) rotate(0deg); opacity: 0; } 10% { opacity: 1; } 90% { opacity: 1; } 100% { transform: rotate(360deg) translateX(35px) rotate(-360deg); opacity: 0; } }
.star-login:hover .portal-arrow { transform: translate(3px, -3px); opacity: 1; }
.star-login:hover .portal-ring.outer { animation-duration: 4s; border-color: rgba(197, 213, 234, 0.95); box-shadow: 0 0 28px rgba(168, 188, 212, 0.7), inset 0 0 18px rgba(168, 188, 212, 0.3); }
.star-login:hover .portal-core { box-shadow: 0 0 12px #ffffff, 0 0 28px rgba(197, 213, 234, 1), 0 0 50px rgba(168, 188, 212, 0.8), 0 0 70px rgba(140, 170, 210, 0.4); }
.star-login:hover .portal-glow { inset: -25px; background: radial-gradient(circle, rgba(168, 188, 212, 0.25) 0%, rgba(140, 170, 210, 0.15) 40%, transparent 70%); }
@keyframes portalSpin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes portalBreath { 0%, 100% { opacity: 0.8; transform: scale(1); } 50% { opacity: 1; transform: scale(1.08); } }
@keyframes coreTwinkle { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.85; transform: scale(0.92); } }
@keyframes glowPulse { 0%, 100% { opacity: 0.6; transform: scale(1); } 50% { opacity: 1; transform: scale(1.1); } }

.profile-btn { position: absolute; top: 18px; right: 26px; z-index: 15; width: 72px; height: 72px; border-radius: 50%; border: none; background: transparent; display: flex; align-items: center; justify-content: center; cursor: pointer; padding: 0; transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.profile-btn:hover { transform: scale(1.12); }
.cosmic-aura { position: absolute; inset: -18px; border-radius: 50%; background: radial-gradient(circle, rgba(180, 160, 255, 0.18) 0%, rgba(160, 140, 220, 0.08) 35%, transparent 65%); animation: auraBreath 4s ease-in-out infinite; pointer-events: none; z-index: 0; transition: all 0.4s ease; }
@keyframes auraBreath { 0%, 100% { opacity: 0.7; transform: scale(1); } 50% { opacity: 1; transform: scale(1.08); } }
.orbit-ring { position: absolute; border-radius: 50%; pointer-events: none; transition: all 0.4s ease; }
.orbit-ring.outer { inset: 0; border: 1px dashed rgba(200, 180, 255, 0.45); animation: orbitSpin 18s linear infinite; box-shadow: 0 0 12px rgba(180, 160, 240, 0.2), inset 0 0 10px rgba(180, 160, 240, 0.08); }
.orbit-ring.middle { inset: 6px; border: 1px solid rgba(220, 200, 255, 0.25); animation: orbitSpinReverse 26s linear infinite; background: radial-gradient(circle, rgba(180, 160, 240, 0.08) 0%, transparent 65%); }
.orbit-ring.inner { inset: 14px; border: 1px solid rgba(230, 210, 255, 0.35); animation: orbitSpin 34s linear infinite; }
@keyframes orbitSpin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes orbitSpinReverse { from { transform: rotate(360deg); } to { transform: rotate(0deg); } }
.cosmic-core { position: relative; width: 40px; height: 40px; border-radius: 50%; background: radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.9) 0%, rgba(240, 230, 255, 0.7) 20%, rgba(200, 180, 255, 0.5) 45%, rgba(160, 140, 220, 0.35) 70%, rgba(120, 100, 180, 0.2) 100%); box-shadow: 0 0 12px rgba(220, 200, 255, 0.7), 0 0 28px rgba(200, 180, 255, 0.5), 0 0 50px rgba(180, 160, 240, 0.25), inset 0 2px 6px rgba(255, 255, 255, 0.6), inset 0 -3px 8px rgba(140, 120, 200, 0.3); animation: corePulse 3.5s ease-in-out infinite; z-index: 2; display: flex; align-items: center; justify-content: center; transition: all 0.4s ease; }
.cosmic-core::before { content: ''; position: absolute; width: 14px; height: 14px; border-radius: 50%; background: radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 1) 0%, rgba(255, 255, 255, 0.8) 30%, rgba(240, 230, 255, 0.6) 60%, transparent 100%); box-shadow: 0 0 6px rgba(255, 255, 255, 0.8); }
@keyframes corePulse { 0%, 100% { filter: brightness(1); transform: scale(1); } 50% { filter: brightness(1.1); transform: scale(1.04); } }
.floating-star { position: absolute; width: 3px; height: 3px; background: radial-gradient(circle, rgba(255, 255, 255, 0.95) 0%, rgba(220, 200, 255, 0.6) 50%, transparent 100%); border-radius: 50%; pointer-events: none; animation: starTwinkle 2.5s ease-in-out infinite; }
.floating-star:nth-child(1) { top: 8px; left: 18px; animation-delay: 0s; }
.floating-star:nth-child(2) { top: 20px; right: 10px; animation-delay: -0.8s; }
.floating-star:nth-child(3) { bottom: 14px; left: 10px; animation-delay: -1.6s; }
.floating-star:nth-child(4) { bottom: 18px; right: 16px; animation-delay: -0.4s; }
@keyframes starTwinkle { 0%, 100% { opacity: 0.3; transform: scale(0.8); } 50% { opacity: 1; transform: scale(1.2); } }
.profile-btn:hover .cosmic-aura { inset: -28px; background: radial-gradient(circle, rgba(200, 180, 255, 0.28) 0%, rgba(180, 160, 240, 0.12) 35%, transparent 65%); }
.profile-btn:hover .orbit-ring.outer { border-color: rgba(220, 200, 255, 0.8); box-shadow: 0 0 22px rgba(180, 160, 240, 0.4), inset 0 0 15px rgba(180, 160, 240, 0.15); animation-duration: 6s; }
.profile-btn:hover .orbit-ring.middle { border-color: rgba(230, 210, 255, 0.5); animation-duration: 8s; }
.profile-btn:hover .orbit-ring.inner { border-color: rgba(240, 220, 255, 0.6); animation-duration: 12s; }
.profile-btn:hover .cosmic-core { box-shadow: 0 0 18px rgba(220, 200, 255, 0.9), 0 0 40px rgba(200, 180, 255, 0.65), 0 0 70px rgba(180, 160, 240, 0.35), inset 0 2px 6px rgba(255, 255, 255, 0.7), inset 0 -3px 8px rgba(140, 120, 200, 0.4); }
.profile-btn:hover .floating-star { opacity: 1; transform: scale(1.4); }
.user-icon { position: absolute; bottom: 8px; right: 8px; font-size: 11px; color: rgba(200, 185, 255, 0.9); display: flex; align-items: center; justify-content: center; z-index: 3; transition: all 0.3s ease; text-shadow: 0 0 6px rgba(200, 180, 255, 0.8); }
.profile-btn:hover .user-icon { transform: translate(2px, -2px) scale(1.15); color: rgba(230, 215, 255, 1); }

#function-panel { position: absolute; bottom: 30px; right: 30px; z-index: 20; width: 300px; background: linear-gradient(135deg, rgba(15, 28, 52, 0.75) 0%, rgba(12, 20, 38, 0.85) 100%); backdrop-filter: blur(20px) saturate(1.2); -webkit-backdrop-filter: blur(20px) saturate(1.2); border-radius: 24px; padding: 24px 28px; border: 1px solid rgba(255, 255, 255, 0.12); box-shadow: 0 25px 50px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(100, 180, 255, 0.08) inset, 0 0 60px rgba(60, 130, 220, 0.08) inset; color: #e0f0ff; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); pointer-events: none; overflow: hidden; }
#function-panel::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 1px; background: linear-gradient(90deg, transparent 0%, rgba(100, 180, 255, 0.4) 50%, transparent 100%); }
.panel-decoration { position: absolute; width: 40px; height: 40px; pointer-events: none; opacity: 0.3; }
.panel-decoration.top-left { top: 8px; left: 8px; border-top: 1px solid rgba(100, 180, 255, 0.3); border-left: 1px solid rgba(100, 180, 255, 0.3); border-radius: 8px 0 0 0; }
.panel-decoration.top-right { top: 8px; right: 8px; border-top: 1px solid rgba(100, 180, 255, 0.3); border-right: 1px solid rgba(100, 180, 255, 0.3); border-radius: 0 8px 0 0; }
.panel-decoration.bottom-left { bottom: 8px; left: 8px; border-bottom: 1px solid rgba(100, 180, 255, 0.3); border-left: 1px solid rgba(100, 180, 255, 0.3); border-radius: 0 0 0 8px; }
.panel-decoration.bottom-right { bottom: 8px; right: 8px; border-bottom: 1px solid rgba(100, 180, 255, 0.3); border-right: 1px solid rgba(100, 180, 255, 0.3); border-radius: 0 0 8px 0; }
#function-title { margin: 0 0 10px; font-size: 1.9rem; font-weight: 500; letter-spacing: 3px; color: #fff; background: linear-gradient(135deg, #fff 0%, #c8e0ff 50%, #fff 100%); -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 0 20px rgba(80, 160, 255, 0.5); border-bottom: 1px solid rgba(255, 255, 255, 0.15); padding-bottom: 12px; position: relative; }
#function-title::after { content: ''; position: absolute; bottom: -1px; left: 0; width: 60px; height: 2px; background: linear-gradient(90deg, rgba(80, 160, 255, 0.8), transparent); border-radius: 1px; }
#function-desc { margin: 14px 0 8px; font-size: 1rem; line-height: 1.6; color: rgba(200, 220, 255, 0.85); letter-spacing: 0.5px; }
#function-action { margin-top: 18px; display: inline-flex; align-items: center; gap: 8px; background: linear-gradient(135deg, rgba(70, 150, 255, 0.35) 0%, rgba(100, 130, 220, 0.25) 100%); padding: 10px 22px; border-radius: 30px; font-size: 0.95rem; font-weight: 500; border: 1px solid rgba(255, 255, 255, 0.2); color: #d0e8ff; pointer-events: auto; cursor: pointer; transition: all 0.3s ease; backdrop-filter: blur(8px); box-shadow: 0 4px 15px rgba(60, 130, 220, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1); }
#function-action i { font-size: 1.1rem; transition: transform 0.3s ease; }
#function-action:hover { background: linear-gradient(135deg, rgba(80, 170, 255, 0.6) 0%, rgba(120, 150, 240, 0.5) 100%); color: #fff; box-shadow: 0 6px 25px rgba(60, 140, 255, 0.4), 0 0 30px rgba(80, 160, 255, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.2); transform: translateY(-2px); }
#function-action:hover i { transform: translateX(3px) translateY(-2px); }

/* ========== 宇宙备案栏 ========== */
.cosmic-footer {
  position: fixed; bottom: 8px; left: 50%; transform: translateX(-50%); z-index: 25;
  padding: 6px 20px 8px; display: flex; align-items: center; justify-content: center; pointer-events: none;
}
.footer-glow {
  position: absolute; inset: -8px -20px;
  background: radial-gradient(ellipse at center, rgba(80, 140, 220, 0.12) 0%, rgba(40, 70, 120, 0.06) 35%, transparent 70%);
  border-radius: 16px; pointer-events: none; animation: footerGlow 4s ease-in-out infinite;
}
@keyframes footerGlow { 0%, 100% { opacity: 0.6; transform: scale(1); } 50% { opacity: 1; transform: scale(1.03); } }
.footer-content {
  position: relative; display: flex; align-items: center; gap: 12px; padding: 4px 16px;
  background: linear-gradient(135deg, rgba(15, 28, 52, 0.65) 0%, rgba(12, 20, 38, 0.75) 100%);
  backdrop-filter: blur(12px) saturate(1.1); -webkit-backdrop-filter: blur(12px) saturate(1.1);
  border: 1px solid rgba(100, 180, 255, 0.15); border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  pointer-events: auto; font-size: 0.75rem; letter-spacing: 0.5px; color: rgba(180, 200, 230, 0.85);
  transition: all 0.3s ease;
}
.cosmic-footer:hover .footer-content { border-color: rgba(120, 190, 255, 0.35); box-shadow: 0 6px 28px rgba(40, 100, 180, 0.25), inset 0 1px 0 rgba(255, 255, 255, 0.12); }
.footer-link { display: inline-flex; align-items: center; gap: 4px; color: rgba(200, 220, 255, 0.9); text-decoration: none; transition: all 0.25s ease; padding: 2px 6px; border-radius: 6px; }
.footer-link i { font-size: 0.85rem; opacity: 0.9; transition: transform 0.3s ease; }
.footer-link:hover { color: #ffffff; background: rgba(100, 160, 240, 0.15); text-shadow: 0 0 12px rgba(120, 180, 255, 0.6); }
.footer-link:hover i { transform: scale(1.15); }
.icp-link:hover { box-shadow: 0 0 16px rgba(80, 160, 255, 0.35); }
.police-link:hover { box-shadow: 0 0 16px rgba(100, 200, 160, 0.35); }
.footer-divider { width: 1px; height: 14px; background: linear-gradient(to bottom, transparent, rgba(100, 180, 255, 0.4), transparent); opacity: 0.6; }
.footer-copyright { display: inline-flex; align-items: center; gap: 4px; opacity: 0.75; }
.footer-copyright i { font-size: 0.8rem; }
.footer-stars { position: absolute; inset: 0; pointer-events: none; overflow: visible; }
.star-dot { position: absolute; width: 2px; height: 2px; background: radial-gradient(circle, rgba(255, 255, 255, 0.95) 0%, rgba(200, 220, 255, 0.6) 50%, transparent 100%); border-radius: 50%; animation: starTwinkle 2.5s ease-in-out infinite; opacity: 0; }
@keyframes starTwinkle { 0%, 100% { opacity: 0.2; transform: scale(0.8); } 50% { opacity: 1; transform: scale(1.3); } }

</style>