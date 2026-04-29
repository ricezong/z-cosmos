<template>
  <div ref="sceneContainer" class="scene-container"></div>
  <div class="glow"></div>

  <div class="site-title">
    <span class="site-title-text">Z的探索小站</span>
    <span class="site-title-sub">COSMOS 星域</span>
  </div>

  <div v-if="!isLoggedIn" class="star-login" @click="router.push('/login')" title="登录">
    <div class="star-ray ray-h"></div>
    <div class="star-ray ray-v"></div>
    <div class="star-core"></div>
  </div>
  <button v-if="isLoggedIn" class="profile-btn" @click="router.push('/profile')" title="个人主页">
    <div class="sun-sphere"></div>
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

function createPlanetTexture(baseColor, secondaryColor, hasSpots=true, stripePattern=false) {
  const canvas = document.createElement('canvas')
  canvas.width = 512; canvas.height = 512
  const ctx = canvas.getContext('2d')
  const gradient = ctx.createRadialGradient(256,256,0,256,256,256)
  gradient.addColorStop(0, baseColor)
  gradient.addColorStop(0.5, secondaryColor)
  gradient.addColorStop(1, shadeColor(baseColor, -30))
  ctx.fillStyle = gradient; ctx.fillRect(0,0,512,512)
  if (hasSpots) {
    for (let i=0; i<200; i++) {
      const x=Math.random()*512, y=Math.random()*512, radius=3+Math.random()*25, alpha=0.1+Math.random()*0.3
      ctx.beginPath(); ctx.arc(x,y,radius,0,Math.PI*2)
      ctx.fillStyle = `rgba(20,20,30,${alpha})`; ctx.fill()
      if (i%3===0) { ctx.beginPath(); ctx.arc(x-2,y-2,radius*0.3,0,Math.PI*2); ctx.fillStyle=`rgba(255,255,240,${alpha*0.8})`; ctx.fill() }
    }
  }
  if (stripePattern) {
    ctx.strokeStyle='rgba(180,140,100,0.5)'; ctx.lineWidth=18
    for (let i=0; i<12; i++) {
      const y=50+i*40+Math.sin(i)*20
      ctx.beginPath(); ctx.moveTo(0,y); ctx.bezierCurveTo(200,y-30,300,y+40,512,y-10); ctx.stroke()
    }
  }
  const grad2 = ctx.createRadialGradient(256,256,200,256,256,256)
  grad2.addColorStop(0,'rgba(255,255,255,0)')
  grad2.addColorStop(0.8,'rgba(200,220,255,0.15)')
  grad2.addColorStop(1,'rgba(180,200,255,0.4)')
  ctx.fillStyle=grad2; ctx.fillRect(0,0,512,512)
  return new THREE.CanvasTexture(canvas)
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

  const tex1=createPlanetTexture('#3a7bd5','#5fa3f0',true,false)
  const planet1=new THREE.Mesh(new THREE.SphereGeometry(1.6,64,64),new THREE.MeshStandardMaterial({map:tex1,roughness:0.5,metalness:0}))
  planet1.castShadow=planet1.receiveShadow=true
  planet1.userData={name:'地球',func:'技术社区',desc:'技术交流，知识共享',link:'/community'}

  const tex2=createPlanetTexture('#c05c3e','#d97a5c',true,false)
  const planet2=new THREE.Mesh(new THREE.SphereGeometry(1.4,64,64),new THREE.MeshStandardMaterial({map:tex2,roughness:0.7,metalness:0}))
  planet2.castShadow=planet2.receiveShadow=true
  planet2.userData={name:'火星',func:'剧场区',desc:'火柴人与像素剧场',link:'/theater'}

  const tex3=createPlanetTexture('#d4b78c','#b89a6e',true,true)
  const planet3=new THREE.Mesh(new THREE.SphereGeometry(2.0,64,64),new THREE.MeshStandardMaterial({map:tex3,roughness:0.6,metalness:0}))
  planet3.castShadow=planet3.receiveShadow=true
  planet3.userData={name:'木星',func:'热点区',desc:'每日热点新闻汇聚',link:'/hot'}

  const tex4=createPlanetTexture('#6ab0d6','#9fc5e8',true,false)
  const planet4=new THREE.Mesh(new THREE.SphereGeometry(1.5,64,64),new THREE.MeshStandardMaterial({map:tex4,roughness:0.3,metalness:0}))
  planet4.castShadow=planet4.receiveShadow=true
  planet4.userData={name:'冰巨星',func:'自助区',desc:'自定义工具集合',link:'/tools'}

  const tex5=createPlanetTexture('#8a6db8','#b095e0',true,false)
  const planet5=new THREE.Mesh(new THREE.SphereGeometry(1.3,64,64),new THREE.MeshStandardMaterial({map:tex5,roughness:0.4,metalness:0}))
  planet5.castShadow=planet5.receiveShadow=true
  planet5.userData={name:'紫微星',func:'搜索区',desc:'全站信息聚合搜索',link:'/search'}

  const ringGeo=new THREE.TorusGeometry(2.2,0.15,32,200)
  const ringMat=new THREE.MeshStandardMaterial({color:0xc8d9f0,transparent:true,opacity:0.55,side:THREE.DoubleSide})
  const ring=new THREE.Mesh(ringGeo,ringMat)
  ring.rotation.x=Math.PI/2.8; ring.rotation.z=0.3; planet4.add(ring)

  const planetMeshes=[planet1,planet2,planet3,planet4,planet5]
  planetMeshes.forEach((p,i)=>{
    const angle=(i/planetMeshes.length)*Math.PI*2
    p.position.set(3.5*Math.cos(angle)+(Math.random()-0.5)*2,1.5*Math.sin(angle*1.5)+(Math.random()-0.5)*2,(Math.random()-0.5)*4)
    scene.add(p)
    const label=createLabel(p.userData.name,'#e0f0ff')
    label.position.set(0,2.2,0); p.add(label)
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
  planets.forEach(p=>{p.rotation.y+=0.005})
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

const onWindowBlur = ()=>{draggedPlanet=null;renderer.domElement.style.cursor='grab'}
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
  font-weight: 300;
  letter-spacing: 4px;
  color: #fff;
  text-shadow: 0 0 15px rgba(61,157,255,0.5);
}
.site-title-sub {
  font-size: 0.85rem;
  letter-spacing: 2px;
  color: rgba(160,200,255,0.6);
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
  animation: twinkle 2s ease-in-out infinite;
  transition: transform 0.3s;
}
.star-login:hover {
  transform: scale(1.25);
}
.star-core {
  position: absolute;
  width: 20px; height: 20px;
  border-radius: 50%;
  background: radial-gradient(circle, #ffffff 0%, #ffe8a0 25%, #ffc040 55%, transparent 70%);
  box-shadow: 0 0 8px #ffffff, 0 0 18px rgba(255,220,100,0.9), 0 0 36px rgba(255,180,40,0.5), 0 0 56px rgba(255,160,20,0.2);
  z-index: 2;
}
.star-ray {
  position: absolute;
  z-index: 1;
}
.star-ray.ray-h {
  width: 52px; height: 3px;
  background: linear-gradient(to right, transparent 0%, rgba(255,220,100,0.2) 25%, #ffe8a0 50%, rgba(255,220,100,0.2) 75%, transparent 100%);
  border-radius: 2px;
  box-shadow: 0 0 4px rgba(255,220,100,0.6);
}
.star-ray.ray-v {
  width: 3px; height: 52px;
  background: linear-gradient(to bottom, transparent 0%, rgba(255,220,100,0.2) 25%, #ffe8a0 50%, rgba(255,220,100,0.2) 75%, transparent 100%);
  border-radius: 2px;
  box-shadow: 0 0 4px rgba(255,220,100,0.6);
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
  transition: transform 0.3s;
  animation: twinkle 2s ease-in-out infinite;
}
.profile-btn:hover {
  transform: scale(1.25);
}
.sun-sphere {
  position: relative;
  width: 48px; height: 48px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, #ffe680, #ffc040 35%, #e89820 68%, #a06010 100%);
  box-shadow: 0 0 16px rgba(255,200,60,0.7), 0 0 32px rgba(255,180,40,0.4), 0 0 50px rgba(255,160,20,0.2), inset 0 0 8px rgba(255,255,255,0.15);
}
.sun-sphere::after {
  content: '';
  position: absolute;
  inset: 10% 24% 38% 18%;
  background: rgba(255,255,255,0.15);
  border-radius: 50%;
}
@keyframes twinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.85); }
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
  .star-login .star-core { width: 15px; height: 15px; }
  .star-login .star-ray.ray-h { width: 38px; }
  .star-login .star-ray.ray-v { height: 38px; }
  .profile-btn { top: 14px; right: 14px; width: 48px; height: 48px; }
  .profile-btn .sun-sphere { width: 36px; height: 36px; }
  #function-panel { width: auto; left: 20px; right: 20px; bottom: 20px; padding: 15px 20px; }
  #function-title { font-size: 1.3rem; }
  #function-desc { font-size: 0.9rem; }
  #function-action { font-size: 0.85rem; }
}
</style>
