<template>
  <div class="star-bg"></div>
  <div class="container">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon">❄️</div>
        <div class="header-title">
          <h1>自助区</h1>
          <p>冰巨星 · 工具集合中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn">← 返回星域</router-link>
    </header>

    <div class="tool-menu">
      <div class="tool-card" v-for="t in tools" :key="t.id" :class="{ active: activeTool === t.id }" @click="activeTool = t.id">
        <div class="icon">{{ t.icon }}</div>
        <div class="name">{{ t.name }}</div>
      </div>
    </div>

    <!-- Notepad -->
    <div class="tool-panel" v-show="activeTool === 'notepad'">
      <div class="tool-box">
        <h3>📝 便签板</h3>
        <div class="note-grid">
          <div class="note-item" v-for="(note, i) in notes" :key="i">
            <span class="note-del" @click="deleteNote(i)">×</span>
            <textarea v-model="note.text" @change="updateNote(i)" placeholder="写点什么..."></textarea>
            <div class="note-time">{{ new Date(note.time).toLocaleString() }}</div>
          </div>
        </div>
        <div class="empty-state" v-if="notes.length === 0" style="padding:20px;opacity:0.5"><p>暂无便签，点击添加</p></div>
        <button class="add-note-btn" @click="addNote">+ 添加便签</button>
      </div>
    </div>

    <!-- Draw -->
    <div class="tool-panel" v-show="activeTool === 'draw'">
      <div class="tool-box">
        <h3>🎨 涂鸦画板</h3>
        <div class="draw-tools">
          <label>颜色:</label>
          <input type="color" v-model="drawColor">
          <label>粗细:</label>
          <input type="range" v-model="drawSize" min="1" max="20">
          <span>{{ drawSize }}</span>
          <button @click="clearCanvas">清空</button>
          <button @click="saveCanvas">保存</button>
        </div>
        <div style="overflow:auto">
          <canvas ref="drawCanvas" class="draw-canvas" width="900" height="400" @mousedown="startDraw" @mousemove="drawMove" @mouseup="stopDraw" @mouseleave="stopDraw"></canvas>
        </div>
      </div>
    </div>

    <!-- Calculator -->
    <div class="tool-panel" v-show="activeTool === 'calc'">
      <div class="tool-box">
        <h3>🧮 计算器</h3>
        <div class="calc-display">
          <div class="calc-history">{{ calcHistory }}</div>
          <div>{{ calcExpr || '0' }}</div>
        </div>
        <div class="calc-btns">
          <button class="calc-btn clear" @click="calcClear">C</button>
          <button class="calc-btn op" @click="calcOp('/')">÷</button>
          <button class="calc-btn op" @click="calcOp('*')">×</button>
          <button class="calc-btn op" @click="calcBack">⌫</button>
          <button class="calc-btn" v-for="n in ['7','8','9']" :key="n" @click="calcNum(n)">{{ n }}</button>
          <button class="calc-btn op" @click="calcOp('-')">−</button>
          <button class="calc-btn" v-for="n in ['4','5','6']" :key="n" @click="calcNum(n)">{{ n }}</button>
          <button class="calc-btn op" @click="calcOp('+')">+</button>
          <button class="calc-btn" v-for="n in ['1','2','3']" :key="n" @click="calcNum(n)">{{ n }}</button>
          <button class="calc-btn eq" @click="calcEq">=</button>
          <button class="calc-btn" @click="calcNum('0')" style="grid-column:span 2">0</button>
          <button class="calc-btn" @click="calcNum('.')">.</button>
        </div>
      </div>
    </div>

    <!-- Timer -->
    <div class="tool-panel" v-show="activeTool === 'timer'">
      <div class="tool-box">
        <h3>⏱️ 计时器</h3>
        <div class="timer-inputs">
          <input type="number" v-model.number="timerMin" placeholder="分" min="0" max="99">
          <input type="number" v-model.number="timerSec" placeholder="秒" min="0" max="59">
        </div>
        <div class="timer-display">{{ timerDisplay }}</div>
        <div class="timer-btns">
          <button class="add-note-btn" @click="startTimer">▶ 开始</button>
          <button class="add-note-btn" style="background:rgba(106,176,214,0.2)" @click="pauseTimer">⏸ 暂停</button>
          <button class="add-note-btn" style="background:rgba(192,80,80,0.3)" @click="resetTimer">↺ 重置</button>
        </div>
      </div>
    </div>

    <!-- Color -->
    <div class="tool-panel" v-show="activeTool === 'color'">
      <div class="tool-box">
        <h3>🎨 颜色工具</h3>
        <div class="color-preview" :style="{ background: colorHex, color: colorPreviewText }">{{ colorHex }}</div>
        <div class="color-sliders">
          <div class="color-slider-row" v-for="c in ['r','g','b']" :key="c">
            <label>{{ c.toUpperCase() }}</label>
            <input type="range" v-model.number="color[c]" min="0" max="255" @input="updateColor">
            <span>{{ color[c] }}</span>
          </div>
        </div>
        <div class="color-output">
          HEX: {{ colorHex }}<br>
          RGB: rgb({{ color.r }}, {{ color.g }}, {{ color.b }})<br>
          RGBA: rgba({{ color.r }}, {{ color.g }}, {{ color.b }}, 1.0)<br>
          HSL: {{ colorHsl }}
        </div>
      </div>
    </div>

    <!-- Text -->
    <div class="tool-panel" v-show="activeTool === 'text'">
      <div class="tool-box">
        <h3>📄 文本工具</h3>
        <textarea class="text-area-box" v-model="textInput" placeholder="在这里输入或粘贴文本..."></textarea>
        <div class="text-stats">
          <div class="text-stat-item">字符: {{ textStats.chars }}</div>
          <div class="text-stat-item">字数: {{ textStats.words }}</div>
          <div class="text-stat-item">行数: {{ textStats.lines }}</div>
          <div class="text-stat-item">英文: {{ textStats.english }}</div>
          <div class="text-stat-item">数字: {{ textStats.digits }}</div>
        </div>
        <div class="text-actions">
          <button @click="textInput = textInput.toUpperCase()">转大写</button>
          <button @click="textInput = textInput.toLowerCase()">转小写</button>
          <button @click="textInput = textInput.replace(/\s/g, '')">去空格</button>
          <button @click="textInput = textInput.replace(/\n/g, '')">去换行</button>
          <button @click="textInput = textInput.split('').reverse().join('')">反转</button>
          <button @click="copyText">复制</button>
          <button @click="textInput = ''">清空</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const tools = [
  { id: 'notepad', icon: '📝', name: '便签' },
  { id: 'draw', icon: '🎨', name: '画板' },
  { id: 'calc', icon: '🧮', name: '计算器' },
  { id: 'timer', icon: '⏱️', name: '计时器' },
  { id: 'color', icon: '🎨', name: '颜色工具' },
  { id: 'text', icon: '📄', name: '文本工具' }
]
const activeTool = ref('notepad')

// Notepad
const NOTES_KEY = 'cosmos_tools_notes'
const notes = ref([])
function getNotes() { try { return JSON.parse(localStorage.getItem(NOTES_KEY)) || [] } catch { return [] } }
function saveNotes() { localStorage.setItem(NOTES_KEY, JSON.stringify(notes.value)) }
function addNote() { notes.value.unshift({ text: '', time: Date.now() }); saveNotes() }
function updateNote(i) { notes.value[i].time = Date.now(); saveNotes() }
function deleteNote(i) { notes.value.splice(i, 1); saveNotes() }

// Draw
const drawCanvas = ref(null)
const drawColor = ref('#6ab0d6')
const drawSize = ref(3)
let drawing = false, lastX = 0, lastY = 0
function startDraw(e) { drawing = true; [lastX, lastY] = [e.offsetX, e.offsetY] }
function drawMove(e) {
  if (!drawing) return
  const ctx = drawCanvas.value.getContext('2d')
  ctx.strokeStyle = drawColor.value; ctx.lineWidth = drawSize.value; ctx.lineCap = 'round'
  ctx.beginPath(); ctx.moveTo(lastX, lastY); ctx.lineTo(e.offsetX, e.offsetY); ctx.stroke()
  ;[lastX, lastY] = [e.offsetX, e.offsetY]
}
function stopDraw() { drawing = false }
function clearCanvas() { const ctx = drawCanvas.value.getContext('2d'); ctx.fillStyle = '#0a1520'; ctx.fillRect(0, 0, drawCanvas.value.width, drawCanvas.value.height) }
function saveCanvas() { const link = document.createElement('a'); link.download = 'drawing.png'; link.href = drawCanvas.value.toDataURL(); link.click() }

// Calculator
const calcExpr = ref('')
const calcHistory = ref('')
function calcNum(n) { calcExpr.value += n }
function calcOp(op) { if (calcExpr.value && !/[+\-*/.]$/.test(calcExpr.value)) calcExpr.value += op }
function calcClear() { calcExpr.value = ''; calcHistory.value = '' }
function calcBack() { calcExpr.value = calcExpr.value.slice(0, -1) }
function calcEq() {
  try { const res = Function('"use strict"; return (' + calcExpr.value + ')')(); calcHistory.value = calcExpr.value + ' = ' + res; calcExpr.value = String(res) }
  catch { calcExpr.value = 'Error'; setTimeout(() => { calcExpr.value = '' }, 1000) }
}

// Timer
const timerMin = ref(5)
const timerSec = ref(0)
const timerSeconds = ref(300)
const timerRunning = ref(false)
let timerId = null
const timerDisplay = computed(() => {
  const m = Math.floor(timerSeconds.value / 60)
  const s = timerSeconds.value % 60
  return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0')
})
function startTimer() {
  if (timerRunning.value) return
  if (!timerId) timerSeconds.value = (timerMin.value || 0) * 60 + (timerSec.value || 0)
  if (timerSeconds.value <= 0) return
  timerRunning.value = true
  timerId = setInterval(() => { timerSeconds.value--; if (timerSeconds.value <= 0) { clearInterval(timerId); timerRunning.value = false; timerId = null; alert('⏰ 时间到！') } }, 1000)
}
function pauseTimer() { if (!timerRunning.value) return; clearInterval(timerId); timerRunning.value = false }
function resetTimer() { clearInterval(timerId); timerRunning.value = false; timerId = null; timerSeconds.value = (timerMin.value || 0) * 60 + (timerSec.value || 0) }

// Color
const color = ref({ r: 106, g: 176, b: 214 })
const colorHex = computed(() => '#' + [color.value.r, color.value.g, color.value.b].map(x => x.toString(16).padStart(2, '0')).join('').toUpperCase())
const colorPreviewText = computed(() => { const b = (color.value.r * 299 + color.value.g * 587 + color.value.b * 114) / 1000; return b > 128 ? 'rgba(0,0,0,0.6)' : 'rgba(255,255,255,0.8)' })
const colorHsl = computed(() => {
  const { r, g, b } = color.value; const max = Math.max(r, g, b), min = Math.min(r, g, b); const l = (max + min) / 2 / 255 * 100
  let h = 0, s = 0
  if (max !== min) { const d = max - min; s = l > 50 ? d / (510 - max - min) * 100 : d / (max + min) * 100; if (max === r) h = (g - b) / d + (g < b ? 6 : 0); else if (max === g) h = (b - r) / d + 2; else h = (r - g) / d + 4; h *= 60 } else { h = 0 }
  return `hsl(${Math.round(h)}, ${Math.round(s)}%, ${Math.round(l)}%)`
})
function updateColor() { /* reactive by v-model */ }

// Text
const textInput = ref('')
const textStats = computed(() => {
  const t = textInput.value
  return { chars: t.length, words: t.trim() ? t.trim().split(/\s+/).length : 0, lines: t ? t.split('\n').length : 0, english: (t.match(/[a-zA-Z]/g) || []).length, digits: (t.match(/[0-9]/g) || []).length }
})
function copyText() { navigator.clipboard.writeText(textInput.value).then(() => alert('已复制到剪贴板')) }

onMounted(() => {
  notes.value = getNotes()
  clearCanvas()
})

onUnmounted(() => {
  clearInterval(timerId)
})
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; background: #050b1a; color: #d0e8f5; min-height: 100vh; }
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
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid rgba(106,176,214,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.planet-icon { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #6ab0d6, #9fc5e8); box-shadow: 0 0 20px rgba(106,176,214,0.4); display: flex; align-items: center; justify-content: center; font-size: 24px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #6ab0d6, #c8e0f5); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(106,176,214,0.2); border: 1px solid rgba(106,176,214,0.4); color: #c8e0f5; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(106,176,214,0.35); box-shadow: 0 0 15px rgba(106,176,214,0.3); }
.tool-menu { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; margin-bottom: 30px; }
.tool-card { background: rgba(10,30,50,0.5); border: 1px solid rgba(106,176,214,0.2); border-radius: 16px; padding: 20px 15px; text-align: center; cursor: pointer; transition: 0.3s; }
.tool-card:hover, .tool-card.active { border-color: rgba(106,176,214,0.5); box-shadow: 0 5px 20px rgba(106,176,214,0.15); background: rgba(10,30,50,0.7); transform: translateY(-3px); }
.tool-card .icon { font-size: 2rem; margin-bottom: 10px; }
.tool-card .name { font-size: 0.9rem; color: #c8e0f5; }
.tool-panel { display: block; }
.tool-box { background: rgba(10,30,50,0.6); border: 1px solid rgba(106,176,214,0.25); border-radius: 20px; padding: 25px; backdrop-filter: blur(10px); }
.tool-box h3 { font-weight: 400; margin-bottom: 20px; color: #9fc5e8; letter-spacing: 2px; font-size: 1.2rem; }
.note-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; }
.note-item { background: rgba(106,176,214,0.15); border: 1px solid rgba(106,176,214,0.3); border-radius: 14px; padding: 15px; position: relative; }
.note-item textarea { width: 100%; min-height: 100px; background: transparent; border: none; color: #d0e8f5; font-family: inherit; resize: vertical; outline: none; font-size: 0.9rem; line-height: 1.5; }
.note-item .note-time { font-size: 0.75rem; opacity: 0.5; margin-top: 8px; }
.note-item .note-del { position: absolute; top: 8px; right: 12px; cursor: pointer; opacity: 0.5; font-size: 1.1rem; transition: 0.3s; }
.note-item .note-del:hover { opacity: 1; color: #ff9999; }
.add-note-btn { padding: 12px 28px; border-radius: 30px; border: none; margin-top: 20px; background: linear-gradient(135deg, #6ab0d6, #9fc5e8); color: white; cursor: pointer; font-size: 0.9rem; transition: 0.3s; }
.add-note-btn:hover { box-shadow: 0 0 20px rgba(106,176,214,0.4); }
.draw-canvas { background: #0a1520; border-radius: 12px; border: 2px solid rgba(106,176,214,0.3); cursor: crosshair; display: block; margin-bottom: 15px; }
.draw-tools { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin-bottom: 15px; }
.draw-tools label { font-size: 0.85rem; opacity: 0.8; }
.draw-tools input[type="color"] { width: 40px; height: 40px; border: none; border-radius: 8px; background: none; cursor: pointer; }
.draw-tools input[type="range"] { width: 120px; }
.draw-tools button { padding: 8px 18px; border-radius: 20px; border: 1px solid rgba(106,176,214,0.3); background: rgba(106,176,214,0.15); color: #c8e0f5; cursor: pointer; transition: 0.3s; }
.draw-tools button:hover { background: rgba(106,176,214,0.3); }
.calc-display { background: rgba(5,15,25,0.7); border: 1px solid rgba(106,176,214,0.3); border-radius: 16px; padding: 20px; margin-bottom: 15px; text-align: right; font-size: 1.8rem; color: #d0e8f5; min-height: 70px; word-break: break-all; }
.calc-history { font-size: 0.9rem; opacity: 0.5; min-height: 20px; margin-bottom: 5px; }
.calc-btns { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.calc-btn { padding: 18px; border-radius: 14px; border: none; background: rgba(106,176,214,0.15); color: #d0e8f5; font-size: 1.1rem; cursor: pointer; transition: 0.2s; }
.calc-btn:hover { background: rgba(106,176,214,0.3); }
.calc-btn.op { background: rgba(106,176,214,0.25); color: #9fc5e8; }
.calc-btn.op:hover { background: rgba(106,176,214,0.4); }
.calc-btn.eq { background: linear-gradient(135deg, #6ab0d6, #9fc5e8); color: white; grid-column: span 2; }
.calc-btn.eq:hover { box-shadow: 0 0 15px rgba(106,176,214,0.4); }
.calc-btn.clear { background: rgba(192,80,80,0.2); color: #ffaaaa; }
.calc-btn.clear:hover { background: rgba(192,80,80,0.35); }
.timer-display { text-align: center; font-size: 4rem; font-weight: 300; color: #9fc5e8; margin: 30px 0; letter-spacing: 4px; font-family: 'Courier New', monospace; }
.timer-btns { display: flex; gap: 15px; justify-content: center; margin-bottom: 25px; }
.timer-inputs { display: flex; gap: 10px; justify-content: center; margin-bottom: 20px; }
.timer-inputs input { width: 70px; padding: 12px; border-radius: 12px; background: rgba(5,15,25,0.6); border: 1px solid rgba(106,176,214,0.3); color: #d0e8f5; font-size: 1.2rem; text-align: center; outline: none; }
.color-preview { width: 100%; height: 120px; border-radius: 16px; background: #6ab0d6; margin-bottom: 20px; border: 2px solid rgba(106,176,214,0.3); display: flex; align-items: center; justify-content: center; font-size: 1.2rem; font-weight: bold; letter-spacing: 1px; }
.color-sliders { display: flex; flex-direction: column; gap: 15px; margin-bottom: 20px; }
.color-slider-row { display: flex; align-items: center; gap: 15px; }
.color-slider-row label { width: 30px; font-size: 0.9rem; }
.color-slider-row input[type="range"] { flex: 1; }
.color-slider-row span { width: 60px; font-size: 0.9rem; text-align: right; font-family: monospace; }
.color-output { background: rgba(5,15,25,0.6); border-radius: 14px; padding: 15px; font-family: monospace; font-size: 0.95rem; line-height: 1.8; }
.text-area-box { width: 100%; min-height: 180px; padding: 15px; border-radius: 14px; background: rgba(5,15,25,0.6); border: 1px solid rgba(106,176,214,0.3); color: #d0e8f5; font-family: inherit; font-size: 0.95rem; outline: none; resize: vertical; margin-bottom: 15px; }
.text-stats { display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 20px; font-size: 0.9rem; }
.text-stat-item { background: rgba(106,176,214,0.15); padding: 8px 16px; border-radius: 12px; border: 1px solid rgba(106,176,214,0.2); }
.text-actions { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 15px; }
.text-actions button { padding: 10px 20px; border-radius: 30px; border: 1px solid rgba(106,176,214,0.3); background: rgba(106,176,214,0.15); color: #c8e0f5; cursor: pointer; transition: 0.3s; font-size: 0.85rem; }
.text-actions button:hover { background: rgba(106,176,214,0.3); }
.empty-state { text-align: center; padding: 50px 20px; opacity: 0.5; }

@media (max-width: 768px) {
  .container { padding: 15px 12px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-title h1 { font-size: 1.4rem; letter-spacing: 2px; }
  .planet-icon { width: 38px; height: 38px; font-size: 18px; }
  .back-btn { font-size: 0.8rem; padding: 6px 14px; }
  .tool-menu { grid-template-columns: repeat(3, 1fr); gap: 8px; }
  .tool-card { padding: 15px 10px; }
  .tool-card .icon { font-size: 1.6rem; }
  .tool-card .name { font-size: 0.8rem; }
  .tool-box { padding: 18px; }
  .tool-box h3 { font-size: 1.05rem; }
  .note-grid { grid-template-columns: 1fr; }
  .draw-canvas { height: 300px; }
  .draw-tools { gap: 8px; }
  .calc-btn { padding: 14px; font-size: 1rem; }
  .timer-display { font-size: 2.5rem; }
  .timer-inputs input { width: 55px; padding: 10px; font-size: 1rem; }
  .color-preview { height: 90px; font-size: 1rem; }
  .add-note-btn { width: 100%; }
}
</style>
