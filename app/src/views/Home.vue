<template>
  <div ref="sceneContainer" class="scene-container"></div>
  <div class="glow"></div>

  <div class="site-title">
    <span class="site-title-text">Z的探索小站</span>
    <span class="site-title-sub">COSMOS 星域</span>
  </div>

  <div v-if="!isLoggedIn" class="star-login" @click="router.push('/login')" title="登录">
    <div class="portal-ring outer"></div>
    <div class="portal-ring inner"></div>
    <div class="portal-core"></div>
    <div class="portal-arrow"><i class="ri-arrow-right-up-line"></i></div>
  </div>
  <button v-if="isLoggedIn" class="profile-btn" @click="router.push('/profile')" title="个人主页">
    <div class="sun-halo"></div>
    <div class="sun-corona"></div>
    <div class="sun-sphere">
      <span class="sun-flare flare-1"></span>
      <span class="sun-flare flare-2"></span>
      <span class="sun-flare flare-3"></span>
    </div>
  </button>


  <div id="function-panel">
    <div id="function-title">{{ panelTitle }}</div>
    <div id="function-desc">{{ panelDesc }}</div>
    <div id="function-action" @click="goToLink"><i class="ri-rocket-line"></i> {{ panelAction }}</div>
  </div>
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

const isLoggedIn = ref(false)

function checkLogin() {
  isLoggedIn.value = localStorage.getItem('cosmos_logged_in') === 'true'
}


// 退出登录已移至Profile页面

let renderer, cssRenderer, scene, camera, animationId
let draggedPlanet = null, hasMoved = false, dragStartPos
let planets = []
const raycaster = new THREE.Raycaster()
const mouse = new THREE.Vector2()
let dragPlane = new THREE.Plane()
const dragOffset = new THREE.Vector3()
const intersectionPoint = new THREE.Vector3()
const boundary = { x: 9, y: 5.5, z: 7 }

function shadeColor(col, percent) {
  let R = parseInt(col.substring(1,3),16)
  let G = parseInt(col.substring(3,5),16)
  let B = parseInt(col.substring(5,7),16)
  R = Math.min(255, Math.max(0, R + percent))
  G = Math.min(255, Math.max(0, G + percent))
  B = Math.min(255, Math.max(0, B + percent))
  return `#${((1 << 24) + (R << 16) + (G << 8) + B).toString(16).slice(1)}`
}

function rand(min, max) { return min + Math.random() * (max - min) }

/* 地球：海洋＋不规则大陆＋极地冰盖＋云层 */
function createEarthTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512
  const ctx = c.getContext('2d')
  const ocean = ctx.createLinearGradient(0, 0, 0, 512)
  ocean.addColorStop(0, '#1a4a8a'); ocean.addColorStop(0.5, '#2a6bb5'); ocean.addColorStop(1, '#1a4a8a')
  ctx.fillStyle = ocean; ctx.fillRect(0, 0, 1024, 512)
  const landColors = ['#4a7d3a', '#5a8a4a', '#7a9a5a', '#3a6d2a', '#6a8a4a']
  for (let i = 0; i < 9; i++) {
    ctx.beginPath()
    const cx = rand(50, 974), cy = rand(90, 422)
    ctx.moveTo(cx, cy)
    for (let a = 0; a < Math.PI * 2; a += 0.25) {
      const r = rand(40, 130) * (0.6 + 0.8 * Math.random())
      ctx.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r * 0.6)
    }
    ctx.closePath()
    ctx.fillStyle = landColors[i % landColors.length]
    ctx.globalAlpha = 0.82; ctx.fill(); ctx.globalAlpha = 1
  }
  const north = ctx.createLinearGradient(0, 0, 0, 65)
  north.addColorStop(0, 'rgba(255,255,255,0.92)'); north.addColorStop(1, 'rgba(255,255,255,0)')
  ctx.fillStyle = north; ctx.fillRect(0, 0, 1024, 65)
  const south = ctx.createLinearGradient(0, 512, 0, 447)
  south.addColorStop(0, 'rgba(255,255,255,0.92)'); south.addColorStop(1, 'rgba(255,255,255,0)')
  ctx.fillStyle = south; ctx.fillRect(0, 447, 1024, 65)
  for (let i = 0; i < 45; i++) {
    ctx.beginPath()
    ctx.ellipse(rand(0,1024), rand(0,512), rand(20,80), rand(10,30), rand(0,Math.PI), 0, Math.PI*2)
    ctx.fillStyle = `rgba(255,255,255,${0.14 + Math.random()*0.26})`; ctx.fill()
  }
  return new THREE.CanvasTexture(c)
}

/* 火星：红棕地表＋峽谷＋撞击坑＋极冠 */
function createMarsTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512
  const ctx = c.getContext('2d')
  const base = ctx.createLinearGradient(0, 0, 0, 512)
  base.addColorStop(0, '#8a3a24'); base.addColorStop(0.5, '#c05c3e'); base.addColorStop(1, '#7a3020')
  ctx.fillStyle = base; ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 160; i++) {
    ctx.beginPath()
    ctx.ellipse(rand(0,1024), rand(0,512), rand(10,60), rand(8,40), rand(0,Math.PI), 0, Math.PI*2)
    const dark = Math.random() > 0.5
    ctx.fillStyle = dark ? `rgba(55,22,12,${0.12+Math.random()*0.3})` : `rgba(225,165,115,${0.1+Math.random()*0.28})`
    ctx.fill()
  }
  ctx.strokeStyle = 'rgba(45,18,10,0.55)'; ctx.lineWidth = 6
  ctx.beginPath(); ctx.moveTo(80, 260)
  ctx.bezierCurveTo(320, 240, 620, 290, 940, 250); ctx.stroke()
  for (let i = 0; i < 70; i++) {
    const x = rand(0,1024), y = rand(0,512), r = rand(4,14)
    ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI*2)
    ctx.fillStyle = `rgba(38,16,8,${0.32+Math.random()*0.28})`; ctx.fill()
    ctx.beginPath(); ctx.arc(x-1, y-1, r*0.55, 0, Math.PI*2)
    ctx.fillStyle = 'rgba(235,185,135,0.25)'; ctx.fill()
  }
  ctx.fillStyle = 'rgba(255,240,230,0.85)'
  ctx.beginPath(); ctx.ellipse(512, 24, 190, 22, 0, 0, Math.PI*2); ctx.fill()
  ctx.beginPath(); ctx.ellipse(512, 488, 210, 25, 0, 0, Math.PI*2); ctx.fill()
  return new THREE.CanvasTexture(c)
}

/* 木星：横向气带＋湍流＋大红斑 */
function createJupiterTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512
  const ctx = c.getContext('2d')
  const bands = [
    { y: 0,   h: 40, col: '#e8cfa4' }, { y: 40,  h: 50, col: '#b88c5c' },
    { y: 90,  h: 45, col: '#d8b784' }, { y: 135, h: 55, col: '#9c6c3c' },
    { y: 190, h: 60, col: '#e8d4a0' }, { y: 250, h: 50, col: '#ac7848' },
    { y: 300, h: 55, col: '#d8b884' }, { y: 355, h: 50, col: '#8c5c2c' },
    { y: 405, h: 60, col: '#d0ac78' }, { y: 465, h: 47, col: '#ac7c48' }
  ]
  bands.forEach(b => {
    const g = ctx.createLinearGradient(0, b.y, 0, b.y + b.h)
    g.addColorStop(0, b.col); g.addColorStop(1, shadeColor(b.col, -22))
    ctx.fillStyle = g; ctx.fillRect(0, b.y, 1024, b.h)
  })
  for (let y = 0; y < 512; y += 4) {
    for (let x = 0; x < 1024; x += 18) {
      const offset = Math.sin(y * 0.05 + x * 0.02) * 6
      ctx.beginPath(); ctx.ellipse(x, y + offset, 14, 2.5, 0, 0, Math.PI*2)
      ctx.fillStyle = `rgba(100,60,30,${0.06 + Math.random()*0.1})`; ctx.fill()
    }
  }
  const sx = 680, sy = 320
  const gs = ctx.createRadialGradient(sx, sy, 0, sx, sy, 72)
  gs.addColorStop(0, '#d05834'); gs.addColorStop(0.5, '#a03a1c')
  gs.addColorStop(0.85, 'rgba(120,40,20,0.55)'); gs.addColorStop(1, 'rgba(120,40,20,0)')
  ctx.fillStyle = gs
  ctx.beginPath(); ctx.ellipse(sx, sy, 72, 42, 0, 0, Math.PI*2); ctx.fill()
  return new THREE.CanvasTexture(c)
}

/* 冰巨星：青蓝渐变＋细纬带 */
function createIceGiantTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512
  const ctx = c.getContext('2d')
  const base = ctx.createLinearGradient(0, 0, 0, 512)
  base.addColorStop(0, '#5a9cc5'); base.addColorStop(0.5, '#8bc0dc'); base.addColorStop(1, '#4a8cb5')
  ctx.fillStyle = base; ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 24; i++) {
    const y = i * 22 + Math.sin(i) * 6
    ctx.strokeStyle = `rgba(255,255,255,${0.08 + Math.random()*0.08})`; ctx.lineWidth = 2
    ctx.beginPath(); ctx.moveTo(0, y)
    for (let x = 0; x <= 1024; x += 40) ctx.lineTo(x, y + Math.sin(x * 0.008 + i) * 2)
    ctx.stroke()
  }
  const nc = ctx.createLinearGradient(0, 0, 0, 80)
  nc.addColorStop(0, 'rgba(215,245,255,0.45)'); nc.addColorStop(1, 'rgba(215,245,255,0)')
  ctx.fillStyle = nc; ctx.fillRect(0, 0, 1024, 80)
  const sc = ctx.createLinearGradient(0, 512, 0, 432)
  sc.addColorStop(0, 'rgba(215,245,255,0.45)'); sc.addColorStop(1, 'rgba(215,245,255,0)')
  ctx.fillStyle = sc; ctx.fillRect(0, 432, 1024, 80)
  return new THREE.CanvasTexture(c)
}

/* 紫微星：紫色星云质感＋金光星点 */
function createNebulaTexture() {
  const c = document.createElement('canvas')
  c.width = 1024; c.height = 512
  const ctx = c.getContext('2d')
  const base = ctx.createLinearGradient(0, 0, 0, 512)
  base.addColorStop(0, '#4a2a70'); base.addColorStop(0.5, '#8a5abc'); base.addColorStop(1, '#3a1e5a')
  ctx.fillStyle = base; ctx.fillRect(0, 0, 1024, 512)
  for (let i = 0; i < 30; i++) {
    const cx = rand(0, 1024), cy = rand(0, 512), r = rand(60, 180)
    const g = ctx.createRadialGradient(cx, cy, 0, cx, cy, r)
    const hue = Math.random() > 0.5 ? '255,210,130' : '200,150,230'
    g.addColorStop(0, `rgba(${hue},${0.22 + Math.random()*0.25})`)
    g.addColorStop(0.5, `rgba(${hue},${0.06 + Math.random()*0.1})`)
    g.addColorStop(1, `rgba(${hue},0)`)
    ctx.fillStyle = g
    ctx.beginPath(); ctx.arc(cx, cy, r, 0, Math.PI*2); ctx.fill()
  }
  for (let i = 0; i < 220; i++) {
    ctx.beginPath()
    ctx.arc(rand(0,1024), rand(0,512), rand(0.5, 2), 0, Math.PI*2)
    ctx.fillStyle = `rgba(255,240,200,${0.4 + Math.random()*0.5})`
    ctx.fill()
  }
  return new THREE.CanvasTexture(c)
}

/* 统一大气层：背面透明辛光包裹 */
function createAtmosphere(radius, hexColor, opacity = 0.15) {
  return new THREE.Mesh(
    new THREE.SphereGeometry(radius, 48, 48),
    new THREE.MeshBasicMaterial({ color: hexColor, transparent: true, opacity, side: THREE.BackSide })
  )
}

function createLabel(text, color='#ffffff') {
  const div = document.createElement('div')
  div.textContent = text
  div.style.cssText = `color:${color};font-size:18px;font-weight:bold;text-shadow:0 0 15px rgba(0,0,0,0.9);background:rgba(20,30,50,0.6);padding:6px 16px;border-radius:30px;backdrop-filter:blur(8px);border:1px solid rgba(255,255,255,0.25);pointer-events:none;letter-spacing:1px;`
  return new CSS2DObject(div)
}

function handleBoundary(p) {
  const vel=p.userData.velocity, r=p.userData.radius
  ;['x','y','z'].forEach(axis=>{
    if (p.position[axis]>boundary[axis]-r) { p.position[axis]=boundary[axis]-r; vel[axis]*=-0.5 }
    else if (p.position[axis]<-boundary[axis]+r) { p.position[axis]=-boundary[axis]+r; vel[axis]*=-0.5 }
  })
}

function handleCollisions() {
  for (let i=0; i<planets.length; i++) {
    for (let j=i+1; j<planets.length; j++) {
      const a=planets[i], b=planets[j]
      const delta=new THREE.Vector3().subVectors(a.position,b.position)
      const dist=delta.length(), minDist=a.userData.radius+b.userData.radius
      if (dist<minDist) {
        const correction=(minDist-dist)/2, norm=delta.clone().normalize()
        a.position.add(norm.clone().multiplyScalar(correction))
        b.position.add(norm.clone().multiplyScalar(-correction))
        const va=a.userData.velocity, vb=b.userData.velocity
        const impulse=norm.clone().multiplyScalar((1+0.3)*va.dot(norm)-vb.dot(norm))
        va.sub(impulse.multiplyScalar(0.5)); vb.add(impulse.multiplyScalar(0.5))
      }
    }
  }
}

function onMouseDown(event) {
  const rect=renderer.domElement.getBoundingClientRect()
  mouse.x=((event.clientX-rect.left)/rect.width)*2-1
  mouse.y=-((event.clientY-rect.top)/rect.height)*2+1
  raycaster.setFromCamera(mouse,camera)
  const intersects=raycaster.intersectObjects(planets)
  if (intersects.length>0) {
    draggedPlanet=intersects[0].object
    dragPlane.setFromNormalAndCoplanarPoint(camera.getWorldDirection(dragPlane.normal).negate(),draggedPlanet.position)
    if (raycaster.ray.intersectPlane(dragPlane,intersectionPoint)) dragOffset.copy(draggedPlanet.position).sub(intersectionPoint)
    dragStartPos=draggedPlanet.position.clone(); hasMoved=false
    event.preventDefault(); renderer.domElement.style.cursor='grabbing'
  }
}

function onMouseMove(event) {
  if (!draggedPlanet) return
  if (event.buttons!==1) { draggedPlanet=null; renderer.domElement.style.cursor='grab'; return }
  const rect=renderer.domElement.getBoundingClientRect()
  mouse.x=((event.clientX-rect.left)/rect.width)*2-1
  mouse.y=-((event.clientY-rect.top)/rect.height)*2+1
  raycaster.setFromCamera(mouse,camera)
  if (raycaster.ray.intersectPlane(dragPlane,intersectionPoint)) {
    const newPos=intersectionPoint.clone().add(dragOffset)
    const r=draggedPlanet.userData.radius
    newPos.x=Math.min(boundary.x-r,Math.max(-boundary.x+r,newPos.x))
    newPos.y=Math.min(boundary.y-r,Math.max(-boundary.y+r,newPos.y))
    newPos.z=Math.min(boundary.z-r,Math.max(-boundary.z+r,newPos.z))
    if (newPos.distanceTo(dragStartPos)>0.3) hasMoved=true
    for (let other of planets) {
      if (other===draggedPlanet) continue
      const dist=newPos.distanceTo(other.position), minDist=draggedPlanet.userData.radius+other.userData.radius
      if (dist<minDist) {
        const norm=new THREE.Vector3().subVectors(newPos,other.position).normalize()
        newPos.copy(other.position.clone().add(norm.multiplyScalar(minDist+0.1)))
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
      const data=draggedPlanet.userData
      currentLink=data.link
      panelTitle.value=data.func
      panelDesc.value=`${data.name} · ${data.desc}`
      panelAction.value=`前往 ${data.func}`
    }
    draggedPlanet=null; renderer.domElement.style.cursor='grab'
  }
  hasMoved=false
}

const onWindowBlur = ()=>{if(renderer){draggedPlanet=null;renderer.domElement.style.cursor='grab'}}

function onWindowResize() {
  camera.aspect=window.innerWidth/window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth,window.innerHeight)
  cssRenderer.setSize(window.innerWidth,window.innerHeight)
}

function init() {
  scene=new THREE.Scene()
  scene.background=new THREE.Color(0x050b1a)

  renderer=new THREE.WebGLRenderer({antialias:true})
  renderer.setSize(window.innerWidth,window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio,2))
  renderer.shadowMap.enabled=true; renderer.shadowMap.type=THREE.PCFSoftShadowMap
  renderer.domElement.style.cssText='position:absolute;top:0;left:0;width:100%;height:100%;display:block;'
  sceneContainer.value.appendChild(renderer.domElement)

  cssRenderer=new CSS2DRenderer()
  cssRenderer.setSize(window.innerWidth,window.innerHeight)
  cssRenderer.domElement.style.cssText='position:absolute;top:0;left:0;width:100%;height:100%;pointer-events:none;'
  sceneContainer.value.appendChild(cssRenderer.domElement)

  camera=new THREE.PerspectiveCamera(45,window.innerWidth/window.innerHeight,0.1,1000)
  camera.position.set(8,5,18); camera.lookAt(0,0,0)

  scene.add(new THREE.AmbientLight(0x404060,0.5))
  const dirLight=new THREE.DirectionalLight(0xfff5e6,1.2)
  dirLight.position.set(5,12,8); dirLight.castShadow=true
  dirLight.shadow.mapSize.set(1024,1024)
  const d=15; dirLight.shadow.camera.left=-d; dirLight.shadow.camera.right=d
  dirLight.shadow.camera.top=d; dirLight.shadow.camera.bottom=-d
  dirLight.shadow.camera.near=1; dirLight.shadow.camera.far=30; dirLight.shadow.bias=-0.001
  scene.add(dirLight)

  const fillLight1=new THREE.PointLight(0x4466ff,0.8); fillLight1.position.set(-3,4,6); scene.add(fillLight1)
  const fillLight2=new THREE.PointLight(0xff9966,0.5); fillLight2.position.set(6,3,-4); scene.add(fillLight2)
  const backLight=new THREE.PointLight(0x3355aa,0.6); backLight.position.set(-5,0,-10); scene.add(backLight)

  const starsGeo=new THREE.BufferGeometry()
  const starsCount=3000
  const starPos=new Float32Array(starsCount*3)
  for (let i=0; i<starsCount*3; i+=3) {
    const r=50+Math.random()*150, theta=Math.random()*Math.PI*2, phi=Math.acos((Math.random()*2)-1)
    starPos[i]=Math.sin(phi)*Math.cos(theta)*r
    starPos[i+1]=Math.sin(phi)*Math.sin(theta)*r
    starPos[i+2]=Math.cos(phi)*r
  }
  starsGeo.setAttribute('position',new THREE.BufferAttribute(starPos,3))
  scene.add(new THREE.Points(starsGeo,new THREE.PointsMaterial({color:0xffffff,size:0.15,transparent:true})))

  scene.fog=new THREE.FogExp2(0x050b1a,0.0035)

  const gridHelper=new THREE.GridHelper(30,20,0x3366aa,0x224466)
  gridHelper.position.y=-2.5; scene.add(gridHelper)

  const tex1 = createEarthTexture()
  const planet1 = new THREE.Mesh(new THREE.SphereGeometry(1.6,64,64), new THREE.MeshStandardMaterial({ map: tex1, roughness: 0.7, metalness: 0.05 }))
  planet1.castShadow = planet1.receiveShadow = true
  planet1.add(createAtmosphere(1.72, 0x66a3ff, 0.18))
  planet1.userData = { name:'地球', func:'技术社区', desc:'技术交流，知识共享', link:'/community', spin: 0.004 }

  const tex2 = createMarsTexture()
  const planet2 = new THREE.Mesh(new THREE.SphereGeometry(1.4,64,64), new THREE.MeshStandardMaterial({ map: tex2, roughness: 0.88, metalness: 0 }))
  planet2.castShadow = planet2.receiveShadow = true
  planet2.add(createAtmosphere(1.48, 0xffaa88, 0.09))
  planet2.userData = { name:'火星', func:'剧场区', desc:'火柴人与像素剧场', link:'/theater', spin: 0.006 }

  const tex3 = createJupiterTexture()
  const planet3 = new THREE.Mesh(new THREE.SphereGeometry(2.0,64,64), new THREE.MeshStandardMaterial({ map: tex3, roughness: 0.6, metalness: 0 }))
  planet3.castShadow = planet3.receiveShadow = true
  planet3.add(createAtmosphere(2.12, 0xf0d5a0, 0.12))
  planet3.userData = { name:'木星', func:'热点区', desc:'每日热点新闻汇聚', link:'/hot', spin: 0.009 }

  const tex4 = createIceGiantTexture()
  const planet4 = new THREE.Mesh(new THREE.SphereGeometry(1.5,64,64), new THREE.MeshStandardMaterial({ map: tex4, roughness: 0.35, metalness: 0.12 }))
  planet4.castShadow = planet4.receiveShadow = true
  planet4.add(createAtmosphere(1.6, 0x88ccee, 0.2))
  planet4.userData = { name:'冰巨星', func:'自助区', desc:'自定义工具集合', link:'/tools', spin: 0.003 }

  const tex5 = createNebulaTexture()
  const planet5 = new THREE.Mesh(new THREE.SphereGeometry(1.3,64,64), new THREE.MeshStandardMaterial({ map: tex5, roughness: 0.45, metalness: 0.15, emissive: 0x3a1e5a, emissiveIntensity: 0.35 }))
  planet5.castShadow = planet5.receiveShadow = true
  planet5.add(createAtmosphere(1.4, 0xc89aff, 0.22))
  planet5.userData = { name:'紫微星', func:'搜索区', desc:'全站信息聚合搜索', link:'/search', spin: 0.005 }

  /* 冰巨星双层环 */
  const ringInner = new THREE.Mesh(
    new THREE.TorusGeometry(2.1, 0.08, 16, 200),
    new THREE.MeshStandardMaterial({ color: 0xd8e8f5, transparent: true, opacity: 0.78, side: THREE.DoubleSide })
  )
  const ringOuter = new THREE.Mesh(
    new THREE.TorusGeometry(2.45, 0.12, 16, 200),
    new THREE.MeshStandardMaterial({ color: 0xc0d5ea, transparent: true, opacity: 0.55, side: THREE.DoubleSide })
  )
  ringInner.rotation.x = ringOuter.rotation.x = Math.PI / 2.8
  ringInner.rotation.z = ringOuter.rotation.z = 0.3
  planet4.add(ringInner); planet4.add(ringOuter)

  const planetMeshes=[planet1,planet2,planet3,planet4,planet5]
  planetMeshes.forEach((p,i)=>{
    const angle=(i/planetMeshes.length)*Math.PI*2
    p.position.set(3.5*Math.cos(angle)+(Math.random()-0.5)*2,1.5*Math.sin(angle*1.5)+(Math.random()-0.5)*2,(Math.random()-0.5)*4)
    scene.add(p)
    const label=createLabel(p.userData.name,'#e0f0ff')
    const r = p.geometry.parameters.radius
    label.position.set(0, r + 0.75, 0); p.add(label)
    p.userData.velocity=new THREE.Vector3((Math.random()-0.5)*0.012,(Math.random()-0.5)*0.012,(Math.random()-0.5)*0.012)
    p.userData.radius=p.geometry.parameters.radius
    p.userData.mass=p.userData.radius*1.5
    planets.push(p)
  })

  const dustGeo=new THREE.BufferGeometry()
  const dustCount=400
  const dustPos=new Float32Array(dustCount*3)
  for (let i=0; i<dustCount*3; i+=3) {
    dustPos[i]=(Math.random()-0.5)*30; dustPos[i+1]=(Math.random()-0.5)*20; dustPos[i+2]=(Math.random()-0.5)*30
  }
  dustGeo.setAttribute('position',new THREE.BufferAttribute(dustPos,3))
  scene.add(new THREE.Points(dustGeo,new THREE.PointsMaterial({color:0xaaccff,size:0.03,transparent:true})))

  renderer.domElement.style.cursor='grab'
  renderer.domElement.addEventListener('mousedown',onMouseDown)
  window.addEventListener('mousemove',onMouseMove)
  window.addEventListener('mouseup',onMouseUp)
  window.addEventListener('blur',onWindowBlur)
  window.addEventListener('resize',onWindowResize)
}

function animate() {
  animationId=requestAnimationFrame(animate)
  planets.forEach(p=>{
    if (p!==draggedPlanet) { p.position.add(p.userData.velocity); p.userData.velocity.multiplyScalar(0.995); handleBoundary(p) }
  })
  handleCollisions()
  planets.forEach(p=>{p.rotation.y += p.userData.spin || 0.005})
  renderer.render(scene,camera)
  cssRenderer.render(scene,camera)
}

onMounted(()=>{
  checkLogin()
  init()
  animate()
})

onUnmounted(()=>{
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize',onWindowResize)
  window.removeEventListener('mousemove',onMouseMove)
  window.removeEventListener('mouseup',onMouseUp)
  window.removeEventListener('blur',onWindowBlur)
  renderer?.domElement?.removeEventListener('mousedown',onMouseDown)
  if(renderer?.domElement?.parentNode) renderer.domElement.parentNode.removeChild(renderer.domElement)
  if(cssRenderer?.domElement?.parentNode) cssRenderer.domElement.parentNode.removeChild(cssRenderer.domElement)
  renderer?.dispose()
})
</script>

<style scoped>
.scene-container {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 0;
  overflow: hidden;
}

.site-title {
  position: absolute;
  top: 24px;
  left: 30px;
  z-index: 15;
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.site-title-text {
  font-size: 1.6rem;
  font-weight: 400;
  letter-spacing: 6px;
  color: #fff;
  font-family: var(--font-display, 'Cinzel', 'Noto Serif SC', serif);
  text-shadow: 0 0 24px rgba(180,160,255,0.45), 0 0 2px rgba(255,255,255,0.6);
}
.site-title-sub {
  font-size: 0.8rem;
  letter-spacing: 4px;
  color: rgba(200,190,240,0.62);
  text-transform: uppercase;
  font-family: var(--font-display, 'Cinzel', serif);
}

.glow {
  position: absolute;
  width: 100%; height: 100%;
  pointer-events: none;
  background: radial-gradient(circle at 20% 30%, rgba(30,60,120,0.15) 0%, transparent 50%);
  z-index: 1;
}



.star-login {
  position: absolute;
  top: 18px; right: 26px;
  z-index: 15;
  width: 64px; height: 64px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.star-login:hover {
  transform: scale(1.08);
}
.portal-ring {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.portal-ring.outer {
  inset: 0;
  border: 1px dashed rgba(197,213,234,0.55);
  animation: portalSpin 14s linear infinite;
  box-shadow:
    0 0 14px rgba(168,188,212,0.25),
    inset 0 0 10px rgba(168,188,212,0.12);
}
.portal-ring.inner {
  inset: 12px;
  border: 1px solid rgba(168,188,212,0.35);
  background: radial-gradient(circle,
    rgba(168,188,212,0.22) 0%,
    rgba(120,144,181,0.08) 60%,
    transparent 100%);
  animation: portalBreath 3.2s ease-in-out infinite;
}
.portal-core {
  position: relative;
  width: 14px; height: 14px;
  border-radius: 50%;
  background: radial-gradient(circle, #ffffff 0%, #c5d5ea 40%, #7890b5 85%);
  box-shadow:
    0 0 6px #ffffff,
    0 0 14px rgba(197,213,234,0.9),
    0 0 26px rgba(168,188,212,0.5);
  z-index: 2;
  animation: coreTwinkle 2.4s ease-in-out infinite;
}
.portal-arrow {
  position: absolute;
  bottom: 12px; right: 12px;
  font-size: 11px;
  line-height: 1;
  color: #e8eef7;
  display: flex; align-items: center; justify-content: center;
  opacity: 0.75;
  transition: 0.3s ease;
  z-index: 3;
  text-shadow: 0 0 6px rgba(168,188,212,0.8);
}
.star-login:hover .portal-arrow {
  transform: translate(2px, -2px);
  opacity: 1;
}
.star-login:hover .portal-ring.outer {
  animation-duration: 6s;
  border-color: rgba(197,213,234,0.85);
  box-shadow: 0 0 22px rgba(168,188,212,0.5), inset 0 0 14px rgba(168,188,212,0.25);
}
.star-login:hover .portal-core {
  box-shadow:
    0 0 10px #ffffff,
    0 0 22px rgba(197,213,234,1),
    0 0 40px rgba(168,188,212,0.7);
}
@keyframes portalSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
@keyframes portalBreath {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.06); }
}
@keyframes coreTwinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.85; transform: scale(0.9); }
}
.profile-btn {
  position: absolute;
  top: 18px; right: 26px;
  z-index: 15;
  width: 64px; height: 64px;
  border-radius: 50%;
  border: none;
  background: transparent;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  padding: 0;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.profile-btn:hover {
  transform: scale(1.08);
}
/* 最外层柔白光晕（符合规范：白色光晕） */
.sun-halo {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  background: radial-gradient(circle,
    rgba(255,255,255,0.28) 0%,
    rgba(255,236,180,0.18) 30%,
    rgba(230,175,95,0.08) 55%,
    transparent 75%);
  animation: haloPulse 3.6s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
  transition: inset 0.4s ease, background 0.4s ease;
}
@keyframes haloPulse {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.08); }
}
/* 日冕双环：外虚线＋内细实线，反向旋转 */
.sun-corona {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  pointer-events: none;
  z-index: 1;
}
.sun-corona::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px dashed rgba(247,223,160,0.55);
  animation: coronaSpin 18s linear infinite;
  transition: border-color 0.3s;
}
.sun-corona::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.2);
  animation: coronaSpin 26s linear infinite reverse;
}
/* 主球：多层径向渐变，顶点白高光＋底部暗面 */
.sun-sphere {
  position: relative;
  width: 48px; height: 48px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 32% 28%, rgba(255,255,255,0.7) 0%, transparent 22%),
    radial-gradient(circle at 65% 70%, rgba(120,70,20,0.28) 0%, transparent 48%),
    radial-gradient(circle at 45% 42%, #fff4d0 0%, #f6deab 18%, #e7bc6b 42%, #c89a50 72%, #7a5224 100%);
  box-shadow:
    0 0 12px rgba(255,236,180,0.6),
    0 0 28px rgba(230,175,95,0.4),
    0 0 48px rgba(220,160,80,0.2),
    inset 0 -3px 6px rgba(100,60,20,0.35),
    inset 0 2px 4px rgba(255,255,255,0.4);
  animation: sunBreath 5s ease-in-out infinite;
  z-index: 2;
  transition: box-shadow 0.4s ease;
}
/* 太阳耀斑：三道射线状光尖 */
.sun-flare {
  position: absolute;
  left: 50%; top: 50%;
  width: 2px; height: 58px;
  background: linear-gradient(to top,
    transparent 0%,
    rgba(255,236,180,0.35) 38%,
    rgba(255,244,208,0.9) 50%,
    rgba(255,236,180,0.35) 62%,
    transparent 100%);
  transform-origin: 50% 50%;
  border-radius: 1px;
  filter: blur(0.4px);
  pointer-events: none;
  animation: flareFlicker 2.8s ease-in-out infinite;
  transition: height 0.4s ease, opacity 0.3s;
}
.sun-flare.flare-1 { transform: translate(-50%, -50%) rotate(30deg); animation-delay: 0s; }
.sun-flare.flare-2 { transform: translate(-50%, -50%) rotate(110deg); animation-delay: -1s; }
.sun-flare.flare-3 { transform: translate(-50%, -50%) rotate(205deg); animation-delay: -2s; }
@keyframes flareFlicker {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 0.85; }
}
@keyframes coronaSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.profile-btn:hover .sun-halo {
  inset: -18px;
  background: radial-gradient(circle,
    rgba(255,255,255,0.42) 0%,
    rgba(255,236,180,0.26) 30%,
    rgba(230,175,95,0.14) 55%,
    transparent 78%);
}
.profile-btn:hover .sun-sphere {
  box-shadow:
    0 0 18px rgba(255,236,180,0.85),
    0 0 40px rgba(230,175,95,0.55),
    0 0 70px rgba(220,160,80,0.3),
    inset 0 -3px 6px rgba(100,60,20,0.35),
    inset 0 2px 4px rgba(255,255,255,0.5);
}
.profile-btn:hover .sun-flare {
  height: 72px;
  opacity: 1;
}
.profile-btn:hover .sun-corona::before {
  border-color: rgba(255,244,208,0.9);
}
@keyframes sunBreath {
  0%, 100% { filter: brightness(1); transform: scale(1); }
  50% { filter: brightness(1.1); transform: scale(1.04); }
}


#function-panel {
  position: absolute;
  bottom: 30px; right: 30px;
  z-index: 20;
  width: 280px;
  background: rgba(10, 20, 40, 0.65);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  padding: 20px 24px;
  border: 1px solid rgba(255,255,255,0.2);
  box-shadow: 0 20px 40px rgba(0,0,0,0.5), 0 0 0 1px rgba(100,180,255,0.2) inset;
  color: #e0f0ff;
  transition: 0.3s;
  pointer-events: none;
}
#function-title {
  margin: 0 0 8px;
  font-size: 1.8rem;
  font-weight: 400;
  letter-spacing: 2px;
  color: #fff;
  text-shadow: 0 0 15px #3d9dff;
  border-bottom: 1px solid rgba(255,255,255,0.2);
  padding-bottom: 8px;
}
#function-desc { margin: 10px 0 5px; font-size: 1rem; line-height: 1.5; opacity: 0.9; }
#function-action {
  margin-top: 15px;
  display: inline-block;
  background: rgba(60, 140, 255, 0.3);
  padding: 6px 18px;
  border-radius: 30px;
  font-size: 0.9rem;
  border: 1px solid rgba(255,255,255,0.3);
  color: #bbddff;
  pointer-events: auto;
  cursor: pointer;
  transition: 0.2s;
  backdrop-filter: blur(5px);
}
#function-action:hover {
  background: rgba(60, 160, 255, 0.6);
  color: white;
  box-shadow: 0 0 15px #3d9dff;
}

@media (max-width: 768px) {
  .site-title { top: 16px; left: 16px; }
  .site-title-text { font-size: 1.1rem; letter-spacing: 2px; }
  .site-title-sub { font-size: 0.7rem; }
  .star-login { top: 14px; right: 14px; width: 48px; height: 48px; }
  .star-login .portal-ring.inner { inset: 9px; }
  .star-login .portal-core { width: 10px; height: 10px; }
  .star-login .portal-arrow { bottom: 9px; right: 9px; font-size: 9px; }
  .profile-btn { top: 14px; right: 14px; width: 48px; height: 48px; }
  .profile-btn .sun-sphere { width: 36px; height: 36px; }
  .profile-btn .sun-halo { inset: -8px; }
  .profile-btn .sun-corona { inset: -4px; }
  .profile-btn .sun-flare { height: 44px; }
  #function-panel { width: auto; left: 20px; right: 20px; bottom: 20px; padding: 15px 20px; }
  #function-title { font-size: 1.3rem; }
  #function-desc { font-size: 0.9rem; }
  #function-action { font-size: 0.85rem; }
}
</style>
