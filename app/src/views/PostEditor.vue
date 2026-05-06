<template>
  <div class="star-bg"></div>
  <div class="page-layout">
    <div class="page-fixed">
    <header class="header">
      <div class="header-left">
        <div class="planet-icon"><div class="planet-sphere earth"></div></div>
        <div class="header-title">
          <h1>发布帖子</h1>
          <p>地球 · 技术交流中心</p>
        </div>
      </div>
      <router-link to="/" class="back-btn"><i class="ri-arrow-left-line"></i> 返回星域</router-link>
    </header>
    </div>

    <div class="page-scroll" ref="contentRef" @scroll="onContentScroll">
    <div class="editor-container">

    <div class="editor-breadcrumb">
      <button class="breadcrumb-link" @click="router.push('/community')">
        <i class="ri-arrow-left-s-line"></i>
        <span>社区</span>
      </button>
      <div class="breadcrumb-separator"></div>
      <div class="breadcrumb-current">
        <span>发布帖子</span>
      </div>
    </div>

    <div class="editor-layout">
      <!-- 左侧编辑区 -->
      <div class="editor-main">
        <div class="editor-card">
          <!-- 分类选择 -->
          <div class="field-row">
            <div class="field-label">
              <i class="ri-price-tag-3-line"></i>
              <span>分类</span>
            </div>
            <div class="category-pills">
              <button
                v-for="cat in categories" :key="cat.code"
                class="cat-pill"
                :class="{ active: newPost.categoryCode === cat.code }"
                @click="newPost.categoryCode = cat.code"
              >{{ cat.name }}</button>
            </div>
          </div>

          <!-- 标题输入 -->
          <div class="title-area">
            <input
              v-model="newPost.title"
              maxlength="100"
              class="title-input"
              placeholder="请输入帖子标题..."
            >
            <div class="title-counter">{{ newPost.title.length }}/100</div>
          </div>

          <!-- 工具栏 -->
          <div class="editor-toolbar">
            <div class="toolbar-left">
              <button class="tool-btn" :class="{ active: markdownEnabled }" @click="markdownEnabled = !markdownEnabled" :title="markdownEnabled ? '切换为纯文本' : '开启 Markdown'">
                <i class="ri-markdown-line"></i>
                <span>Markdown</span>
              </button>
            </div>
            <div class="toolbar-right">
              <button class="tool-btn" :class="{ active: showPreview }" @click="showPreview = !showPreview" v-if="markdownEnabled" :title="showPreview ? '隐藏预览' : '显示预览'">
                <i :class="showPreview ? 'ri-eye-off-line' : 'ri-eye-line'"></i>
                <span>{{ showPreview ? '隐藏预览' : '预览' }}</span>
              </button>
            </div>
          </div>

          <!-- 内容编辑区 -->
          <div class="content-area" :class="{ 'with-preview': showPreview && markdownEnabled }">
            <div class="editor-pane">
              <div class="pane-label" v-if="showPreview && markdownEnabled">
                <i class="ri-edit-line"></i> 编辑
              </div>
              <textarea
                v-model="newPost.content"
                class="content-textarea"
                :class="{ 'has-pane-label': showPreview && markdownEnabled }"
                placeholder="分享你的技术见解、经验心得...&#10;&#10;支持 Markdown 语法：&#10;# 标题&#10;**加粗** *斜体*&#10;- 列表项&#10;> 引用&#10;```代码块```"
                ref="textareaRef"
                @scroll="onEditorScroll"
              ></textarea>
            </div>
            <div class="preview-pane" v-if="showPreview && markdownEnabled">
              <div class="pane-label">
                <i class="ri-eye-line"></i> 预览
              </div>
              <div class="preview-body" v-if="newPost.content" ref="previewBodyRef" v-html="renderedPreview" @scroll="onPreviewScroll"></div>
              <div class="preview-empty" v-else>
                <i class="ri-file-text-line"></i>
                <span>在左侧输入内容，这里将实时显示预览效果</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息栏 -->
      <div class="editor-sidebar">
        <div class="sidebar-card">
          <div class="sidebar-title">
            <i class="ri-information-line"></i>
            <span>发布须知</span>
          </div>
          <ul class="sidebar-tips">
            <li>选择合适的分类有助于帖子被发现</li>
            <li>标题简洁明了，概括帖子核心内容</li>
            <li>支持 Markdown 排版</li>
            <li>发布后可在「我的帖子」中管理</li>
          </ul>
        </div>

        <div class="sidebar-card" v-if="newPost.title.trim() && newPost.content.trim()">
          <div class="sidebar-title">
            <i class="ri-draft-line"></i>
            <span>帖子预览</span>
          </div>
          <div class="mini-preview">
            <div class="mini-title">{{ newPost.title }}</div>
            <div class="mini-category">{{ selectedCategoryName || '未分类' }}</div>
            <div v-if="markdownEnabled" class="mini-body" v-html="renderedPreview"></div>
            <div v-else class="mini-snippet">{{ newPost.content.slice(0, 200) }}{{ newPost.content.length > 200 ? '...' : '' }}</div>
          </div>
        </div>

        <!-- 发布按钮区 -->
        <div class="publish-area">
          <button class="publish-btn" :disabled="submitting || !canPublish" @click="submitPost">
            <i class="ri-send-plane-fill"></i>
            <span>{{ submitting ? '发布中...' : '发布帖子' }}</span>
          </button>
          <div class="publish-hint" v-if="publishHint">
            <i class="ri-error-warning-line"></i>
            <span>{{ publishHint }}</span>
          </div>
        </div>
      </div>
    </div>

    </div>
    </div>
  <button class="back-to-top" v-show="showBackTop" @click="scrollToTop"><i class="ri-arrow-up-line"></i></button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import * as communityApi from '../api/community'
import { useToast } from '../composables/useToast'

const router = useRouter()
const toast = useToast()

const categories = ref([])
const newPost = ref({ categoryCode: '', title: '', content: '' })
const submitting = ref(false)
const triedSubmit = ref(false)
const markdownEnabled = ref(true)
const showPreview = ref(false)
const textareaRef = ref(null)
const previewBodyRef = ref(null)
const contentRef = ref(null)
let isSyncingScroll = false

const selectedCategoryName = computed(() => {
  const cat = categories.value.find(c => c.code === newPost.value.categoryCode)
  return cat ? cat.name : ''
})

const renderedPreview = computed(() => {
  return renderMarkdown(newPost.value.content)
})

const canPublish = computed(() => {
  return newPost.value.categoryCode && newPost.value.title.trim() && newPost.value.content.trim() && !submitting.value
})

const publishHint = computed(() => {
  if (!triedSubmit.value) return ''
  if (!newPost.value.categoryCode) return '请选择分类'
  if (!newPost.value.title.trim()) return '请填写标题'
  if (!newPost.value.content.trim()) return '请填写内容'
  return ''
})

const mdOpts = { breaks: true, gfm: true }
function renderMarkdown(text) {
  if (!text) return ''
  try { return marked.parse(text, mdOpts) }
  catch (e) { return text.replace(/\n/g, '<br>') }
}

function autoResizeContent() {
  const el = textareaRef.value
  if (el) {
    el.style.height = 'auto'
    el.style.height = Math.max(el.scrollHeight, 400) + 'px'
  }
}

function onEditorScroll() {
  if (isSyncingScroll) return
  isSyncingScroll = true
  const editor = textareaRef.value
  const preview = previewBodyRef.value
  if (editor && preview) {
    const scrollRatio = editor.scrollTop / (editor.scrollHeight - editor.clientHeight)
    preview.scrollTop = scrollRatio * (preview.scrollHeight - preview.clientHeight)
  }
  requestAnimationFrame(() => { isSyncingScroll = false })
}

function onPreviewScroll() {
  if (isSyncingScroll) return
  isSyncingScroll = true
  const editor = textareaRef.value
  const preview = previewBodyRef.value
  if (editor && preview) {
    const scrollRatio = preview.scrollTop / (preview.scrollHeight - preview.clientHeight)
    editor.scrollTop = scrollRatio * (editor.scrollHeight - editor.clientHeight)
  }
  requestAnimationFrame(() => { isSyncingScroll = false })
}

async function loadCategories() {
  try {
    const data = await communityApi.listCategories()
    categories.value = data || []
    if (categories.value.length > 0 && !newPost.value.categoryCode) {
      newPost.value.categoryCode = categories.value[0].code
    }
  } catch (e) {
    console.error('加载分类失败:', e)
    categories.value = []
  }
}

async function submitPost() {
  triedSubmit.value = true
  const { categoryCode, title, content } = newPost.value
  if (!title.trim()) { toast.warning('请填写标题'); return }
  if (!content.trim()) { toast.warning('请填写内容'); return }
  if (!categoryCode) { toast.warning('请选择分类'); return }

  submitting.value = true
  try {
    const finalContent = markdownEnabled.value
      ? renderMarkdown(content.trim())
      : content.trim()
    await communityApi.createPost({ categoryCode, title: title.trim(), content: finalContent })
    toast.success('发布成功！')
    router.push('/community')
  } catch (e) {
    toast.error('发布失败: ' + (e.message || '请重试'))
  } finally {
    submitting.value = false
  }
}

const showBackTop = ref(false)
function onContentScroll() {
  const el = contentRef.value
  if (el) showBackTop.value = el.scrollTop > 300
}
function scrollToTop() {
  contentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid rgba(144,166,196,0.3); }
.header-left { display: flex; align-items: center; gap: 15px; }
.header-title h1 { font-size: 1.8rem; font-weight: 300; letter-spacing: 4px; background: linear-gradient(135deg, #ffffff, #c5d5ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.header-title p { font-size: 0.85rem; opacity: 0.7; margin-top: 2px; }
.back-btn { padding: 8px 20px; border-radius: 30px; background: rgba(144,166,196,0.2); border: 1px solid rgba(144,166,196,0.4); color: #c5d5ea; cursor: pointer; text-decoration: none; transition: 0.3s; font-size: 0.9rem; }
.back-btn:hover { background: rgba(144,166,196,0.35); box-shadow: 0 0 15px rgba(144,166,196,0.15); }

.editor-container { position: relative; z-index: 1; max-width: 1100px; margin: 0 auto; padding: 24px 20px; }

/* Breadcrumb */
.editor-breadcrumb { display: flex; align-items: center; gap: 0; margin-bottom: 20px; }
.breadcrumb-link {
  display: inline-flex; align-items: center; gap: 2px;
  padding: 6px 12px; border-radius: 8px;
  background: transparent; border: none;
  color: #a8bcd4; cursor: pointer;
  font-size: 0.88rem; transition: 0.2s; letter-spacing: 0.3px;
  font-family: inherit;
}
.breadcrumb-link i { font-size: 1.2rem; transition: transform 0.2s; }
.breadcrumb-link:hover { background: rgba(144,166,196,0.1); color: #e8eef7; }
.breadcrumb-link:hover i { transform: translateX(-2px); }
.breadcrumb-separator {
  width: 1px; height: 16px;
  background: rgba(144,166,196,0.25);
  margin: 0 8px; flex-shrink: 0;
}
.breadcrumb-current {
  display: flex; align-items: center; gap: 6px;
  font-size: 0.88rem;
}
.breadcrumb-current span { color: #e8eef7; font-weight: 500; letter-spacing: 0.3px; }

/* Layout */
.editor-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.editor-main { flex: 1; min-width: 0; }
.editor-sidebar { width: 280px; flex-shrink: 0; position: sticky; top: 24px; }

/* Editor Card */
.editor-card {
  border: 1px solid rgba(144,166,196,0.15);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(12px);
}

/* Category Field */
.field-row {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(144,166,196,0.08);
}
.field-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 0.85rem; color: #a8bcd4;
  flex-shrink: 0; white-space: nowrap;
}
.field-label i { font-size: 1rem; }
.category-pills {
  display: flex; gap: 8px; flex-wrap: wrap;
}
.cat-pill {
  padding: 5px 14px; border-radius: 16px;
  border: 1px solid rgba(144,166,196,0.18);
  background: rgba(144,166,196,0.06);
  color: #a8bcd4; font-size: 0.82rem;
  cursor: pointer; transition: 0.2s;
  white-space: nowrap;
}
.cat-pill:hover {
  background: rgba(144,166,196,0.12);
  border-color: rgba(144,166,196,0.3);
  color: #e8eef7;
}
.cat-pill.active {
  background: rgba(144,166,196,0.2);
  border-color: rgba(144,166,196,0.5);
  color: #fff;
  font-weight: 500;
}

/* Title Area */
.title-area {
  position: relative;
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(144,166,196,0.08);
}
.title-input {
  width: 100%;
  padding: 0 0 8px 0;
  border: none;
  border-bottom: 2px solid rgba(144,166,196,0.15);
  background: transparent;
  color: #e8eef7;
  font-size: 1.35rem;
  font-weight: 500;
  font-family: inherit;
  letter-spacing: 0.5px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}
.title-input:focus {
  border-bottom-color: rgba(144,166,196,0.5);
}
.title-input::placeholder {
  color: rgba(168,188,212,0.3);
  font-weight: 400;
}
.title-counter {
  position: absolute;
  right: 24px;
  bottom: 20px;
  font-size: 0.72rem;
  color: rgba(168,188,212,0.3);
}

/* Toolbar */
.editor-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px;
  border-bottom: 1px solid rgba(144,166,196,0.08);
  background: rgba(144,166,196,0.03);
}
.toolbar-left, .toolbar-right { display: flex; gap: 4px; }
.tool-btn {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 12px; border-radius: 8px;
  border: none; background: transparent;
  color: rgba(168,188,212,0.6); font-size: 0.8rem;
  cursor: pointer; transition: 0.2s;
  font-family: inherit;
}
.tool-btn:hover {
  background: rgba(144,166,196,0.1);
  color: #e8eef7;
}
.tool-btn.active {
  background: rgba(144,166,196,0.15);
  color: #e8eef7;
}
.tool-btn i { font-size: 1rem; }

/* Content Area */
.content-area {
  display: flex;
  min-height: 400px;
}
.content-area.with-preview {
  display: flex;
}
.editor-pane {
  flex: 1;
  min-width: 0;
  display: flex; flex-direction: column;
}
.pane-label {
  display: flex; align-items: center; gap: 5px;
  padding: 8px 24px;
  font-size: 0.72rem; color: rgba(168,188,212,0.4);
  letter-spacing: 1px; text-transform: uppercase;
  border-bottom: 1px solid rgba(144,166,196,0.06);
  flex-shrink: 0;
}
.pane-label i { font-size: 0.85rem; }
.content-textarea {
  width: 100%;
  flex: 1;
  min-height: 400px;
  max-height: 70vh;
  padding: 20px 24px;
  border: none;
  background: transparent;
  color: #e8eef7;
  font-size: 0.95rem;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  line-height: 1.8;
  outline: none;
  resize: none;
  box-sizing: border-box;
  overflow-y: auto;
}
.content-textarea.has-pane-label {
  min-height: 370px;
}
.content-textarea::placeholder {
  color: rgba(168,188,212,0.25);
}

/* Preview Pane */
.preview-pane {
  flex: 1;
  min-width: 0;
  border-left: 1px solid rgba(144,166,196,0.1);
  display: flex; flex-direction: column;
}
.preview-body {
  flex: 1; overflow-y: auto;
  max-height: 70vh;
  padding: 20px 24px;
  font-size: 0.92rem; line-height: 1.8;
  color: #e8eef7;
}
.preview-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12px;
  color: rgba(168,188,212,0.2);
  font-size: 0.85rem;
  padding: 40px 20px;
}
.preview-empty i { font-size: 2.5rem; opacity: 0.4; }
.preview-body :deep(h1), .preview-body :deep(h2), .preview-body :deep(h3) { color: #c5d5ea; margin: 16px 0 8px; font-weight: 500; }
.preview-body :deep(code) { background: rgba(144,166,196,0.12); padding: 2px 6px; border-radius: 4px; font-size: 0.85em; font-family: 'JetBrains Mono', 'Consolas', monospace; }
.preview-body :deep(pre) { background: rgba(5,10,20,0.7); padding: 14px; border-radius: 8px; overflow-x: auto; border: 1px solid rgba(144,166,196,0.15); margin: 12px 0; }
.preview-body :deep(pre code) { background: none; padding: 0; }
.preview-body :deep(blockquote) { border-left: 3px solid #c5d5ea; padding-left: 12px; color: #c5d5ea; margin: 10px 0; }
.preview-body :deep(ul), .preview-body :deep(ol) { padding-left: 22px; }
.preview-body :deep(p) { margin-bottom: 10px; }
.preview-body :deep(a) { color: #c5d5ea; }

/* Sidebar */
.sidebar-card {
  border: 1px solid rgba(144,166,196,0.12);
  border-radius: 14px;
  padding: 18px 20px;
  margin-bottom: 16px;
  backdrop-filter: blur(10px);
}
.sidebar-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 0.88rem; color: #c5d5ea;
  margin-bottom: 14px; font-weight: 500;
  letter-spacing: 0.5px;
}
.sidebar-title i { font-size: 1.05rem; opacity: 0.7; }
.sidebar-tips {
  list-style: none; padding: 0; margin: 0;
}
.sidebar-tips li {
  position: relative;
  padding: 4px 0 4px 14px;
  font-size: 0.78rem;
  color: rgba(168,188,212,0.55);
  line-height: 1.6;
}
.sidebar-tips li::before {
  content: '';
  position: absolute;
  left: 0; top: 10px;
  width: 4px; height: 4px;
  border-radius: 50%;
  background: rgba(144,166,196,0.3);
}

/* Mini Preview */
.mini-preview {
  padding: 4px 0;
}
.mini-title {
  font-size: 0.92rem;
  color: #e8eef7;
  font-weight: 500;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.mini-category {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 0.7rem;
  background: rgba(144,166,196,0.12);
  color: #a8bcd4;
  border: 1px solid rgba(144,166,196,0.18);
  margin-bottom: 10px;
}
.mini-snippet {
  font-size: 0.78rem;
  color: rgba(168,188,212,0.5);
  line-height: 1.6;
  word-break: break-all;
}
.mini-body {
  font-size: 0.78rem;
  color: rgba(168,188,212,0.65);
  line-height: 1.65;
  max-height: 200px;
  overflow-y: auto;
}
.mini-body :deep(h1), .mini-body :deep(h2), .mini-body :deep(h3) { color: #c5d5ea; margin: 8px 0 4px; font-size: 0.85rem; font-weight: 500; }
.mini-body :deep(p) { margin-bottom: 6px; }
.mini-body :deep(code) { background: rgba(144,166,196,0.1); padding: 1px 4px; border-radius: 3px; font-size: 0.75em; font-family: 'JetBrains Mono', 'Consolas', monospace; }
.mini-body :deep(pre) { background: rgba(5,10,20,0.6); padding: 8px; border-radius: 6px; overflow-x: auto; border: 1px solid rgba(144,166,196,0.1); margin: 6px 0; font-size: 0.72rem; }
.mini-body :deep(pre code) { background: none; padding: 0; }
.mini-body :deep(blockquote) { border-left: 2px solid #c5d5ea; padding-left: 8px; color: #c5d5ea; margin: 6px 0; font-size: 0.75rem; }
.mini-body :deep(ul), .mini-body :deep(ol) { padding-left: 16px; margin: 4px 0; }
.mini-body :deep(a) { color: #c5d5ea; }

/* Publish Area */
.publish-area {
  padding: 0;
}
.publish-btn {
  width: 100%;
  padding: 14px 24px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, #7890b5 0%, #a8bcd4 100%);
  color: #fff;
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.25s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: inherit;
}
.publish-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.publish-btn:not(:disabled):hover {
  box-shadow: 0 6px 24px rgba(144,166,196,0.4);
  transform: translateY(-1px);
}
.publish-btn i { font-size: 1.1rem; }
.publish-hint {
  display: flex; align-items: center; gap: 6px;
  margin-top: 10px; padding: 0 4px;
  font-size: 0.75rem; color: rgba(255,180,120,0.7);
}
.publish-hint i { font-size: 0.9rem; }
</style>