# Z-Cosmos 工信部备案合规重构方案

## 文档版本
- **版本**: v1.0
- **更新日期**: 2026-05-09
- **适用项目**: z-cosmos

---

## 一、项目概述

本文档详细描述了 Z-Cosmos 项目为符合工信部备案要求而进行的重构设计方案。重构遵循**最小改造原则**，移除所有可能导致备案问题的用户生成内容（UGC）功能和用户系统，保留核心的展示型功能模块。

### 1.1 备案合规原则

1. **无用户系统**：个人备案不允许提供用户注册、登录功能
2. **无UGC内容**：不允许用户发布内容（帖子、评论等）
3. **站长个人内容**：仅展示站长个人发布的技术内容
4. **明确备案信息**：页脚展示ICP备案号、公安备案标识

---

## 二、功能模块调整

### 2.1 移除的功能（备案风险项）

| 模块 | 功能 | 移除原因 | 影响范围 |
|-----|------|---------|---------|
| 用户系统 | 注册/登录/个人资料 | 个人备案不允许用户系统 | 后端auth模块、前端Login/Profile页面 |
| 社区模块 | 发帖/评论/点赞/收藏 | UGC内容需要前置审批 | 后端community模块、前端Community/PostEditor页面 |
| OAuth登录 | GitHub/Gitee/QQ登录 | 第三方登录需要额外备案 | OAuth控制器、第三方登录配置 |
| 用户积分 | 积分/等级/信用体系 | 游戏化功能需特殊审批 | 用户表相关字段、等级逻辑 |

### 2.2 保留的功能（合规项）

| 模块 | 功能 | 合规说明 | 改造方式 |
|-----|------|---------|---------|
| 3D首页 | Three.js行星模型 | 纯展示型，无交互风险 | 保持现状 |
| 技术笔记 | 站长发布的技术文章 | 站长个人内容，非UGC | 重构为只读展示 |
| 热点摘要 | AI生成的热点内容 | 自动生成，注明来源 | 保留并优化 |
| 动画展区 | 前端展示（无后端） | 纯前端展示 | 保持现状 |
| 技能展示 | 前端展示（无后端） | 纯前端展示 | 保持现状 |
| 内容检索 | 站内内容搜索 | 仅搜索站长发布内容 | 简化搜索范围 |
| 站长主页 | 静态个人简介 | 符合个人备案要求 | 新增静态页面 |
| 全局页脚 | ICP/公安备案信息 | 合规必需 | 新增页脚组件 |

---

## 三、数据库表结构设计

### 3.1 完整DDL脚本

```sql
-- =============================================================
-- z-cosmos 合规版数据库建表 DDL
-- 适用数据库：MySQL 8.0+
-- 设计原则：最小化功能，符合个人站点备案要求
-- =============================================================

CREATE DATABASE IF NOT EXISTS `z-cosmos` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `z-cosmos`;

-- ============================================================================
-- 1. 全局认证解锁表（一次解锁，全局有效，可扩展支持多模块）
-- ============================================================================
DROP TABLE IF EXISTS z_auth_unlock;

CREATE TABLE z_auth_unlock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备唯一标识（UUID v4）',
    module_type VARCHAR(32) NOT NULL COMMENT '模块类型（NOTE/OTHER）',
    unlock_code VARCHAR(6) NOT NULL COMMENT '6位动态口令',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未解锁 1-已解锁',
    expires_at DATETIME NOT NULL COMMENT '过期时间（12小时后）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_device_module (device_id, module_type),
    INDEX idx_unlock_code (unlock_code),
    INDEX idx_status (status),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局认证解锁表';

-- ============================================================================
-- 2. 技术笔记表（原帖子表简化版，移除用户系统相关字段）
-- ============================================================================
DROP TABLE IF EXISTS z_notes;

CREATE TABLE z_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    note_id VARCHAR(32) NOT NULL COMMENT '笔记业务ID（雪花算法）',
    title VARCHAR(128) NOT NULL COMMENT '笔记标题',
    content MEDIUMTEXT NOT NULL COMMENT '笔记完整内容（HTML格式）',
    preview_ratio DECIMAL(3,2) NOT NULL DEFAULT 0.30 COMMENT '预览比例（0.30表示前30%）',
    short_summary VARCHAR(256) DEFAULT NULL COMMENT '简短摘要（SEO用，100字以内）',
    cover_image VARCHAR(512) DEFAULT NULL COMMENT '封面图URL',
    category VARCHAR(32) NOT NULL COMMENT '分类标签',
    tags JSON DEFAULT NULL COMMENT '标签数组',
    view_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已读数',
    is_locked TINYINT NOT NULL DEFAULT 1 COMMENT '是否锁定：0-公开 1-需解锁',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_note_id (note_id),
    INDEX idx_category (category),
    INDEX idx_created (created_at),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术笔记表';

-- ============================================================================
-- 3. 热点话题表（AI生成内容）
-- ============================================================================
DROP TABLE IF EXISTS z_hot_topics;

CREATE TABLE z_hot_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    topic_id VARCHAR(32) NOT NULL COMMENT '热点业务ID',
    title VARCHAR(256) NOT NULL COMMENT 'AI提炼标题',
    summary TEXT NOT NULL COMMENT 'AI完整总结',
    source_url VARCHAR(512) DEFAULT NULL COMMENT '原始链接',
    source_name VARCHAR(64) DEFAULT NULL COMMENT '来源平台名称',
    publish_time DATETIME DEFAULT NULL COMMENT '原始发布时间',
    category VARCHAR(32) DEFAULT NULL COMMENT '分类标签',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效：0-下架 1-展示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_topic_id (topic_id),
    INDEX idx_category (category),
    INDEX idx_publish_time (publish_time),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热点话题表';

-- ============================================================================
-- 4. 搜索索引表（简化版，支持标题/标签/关键词匹配）
-- ============================================================================
DROP TABLE IF EXISTS z_search_index;

CREATE TABLE z_search_index (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型（NOTE/HOT）',
    content_id VARCHAR(32) NOT NULL COMMENT '内容业务ID',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    keywords JSON DEFAULT NULL COMMENT '关键词数组',
    tags JSON DEFAULT NULL COMMENT '标签数组',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_content (content_type, content_id),
    FULLTEXT INDEX ft_search (title, keywords, tags) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索索引表';
```

### 3.2 表结构说明

#### 3.2.1 z_auth_unlock（全局认证解锁表）

| 字段 | 类型 | 说明 | 约束 |
|-----|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY |
| device_id | VARCHAR(64) | 设备唯一标识（UUID v4） | NOT NULL |
| module_type | VARCHAR(32) | 模块类型（NOTE/OTHER） | NOT NULL |
| unlock_code | VARCHAR(6) | 6位动态口令 | NOT NULL |
| status | TINYINT | 状态：0-未解锁 1-已解锁 | DEFAULT 0 |
| expires_at | DATETIME | 过期时间（12小时后） | NOT NULL |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | ON UPDATE CURRENT_TIMESTAMP |

**索引设计**：
- 唯一索引：`uk_device_module` - 同一设备同一模块只保留一条记录
- 普通索引：`idx_unlock_code` - 快速验证口令
- 普通索引：`idx_status` - 查询已解锁记录
- 普通索引：`idx_expires` - 清理过期记录

**设计说明**：
- 一次解锁，全局有效：用户完成一次扫码认证后，12小时内可访问所有需要认证的内容
- 认证状态与设备ID关联，不与特定内容ID关联

#### 3.2.2 z_notes（技术笔记表）

| 字段 | 类型 | 说明 | 约束 |
|-----|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY |
| note_id | VARCHAR(32) | 笔记业务ID（雪花算法） | NOT NULL, UNIQUE |
| title | VARCHAR(128) | 笔记标题 | NOT NULL |
| content | MEDIUMTEXT | 笔记完整内容（HTML格式） | NOT NULL |
| preview_ratio | DECIMAL(3,2) | 预览比例（0.30表示前30%） | DEFAULT 0.30 |
| short_summary | VARCHAR(256) | 简短摘要（SEO用，100字以内） | NULL |
| cover_image | VARCHAR(512) | 封面图URL | NULL |
| category | VARCHAR(32) | 分类标签 | NOT NULL |
| tags | JSON | 标签数组 | NULL |
| view_count | BIGINT UNSIGNED | 已读数 | DEFAULT 0 |
| is_locked | TINYINT | 是否锁定：0-公开 1-需解锁 | DEFAULT 1 |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | ON UPDATE CURRENT_TIMESTAMP |

**索引设计**：
- 唯一索引：`uk_note_id` - 业务ID唯一
- 普通索引：`idx_category` - 按分类查询
- 普通索引：`idx_created` - 按时间排序
- 全文索引：`ft_title_content` - 全文搜索

**设计说明**：
- preview_ratio：动态控制预览比例，避免存储大字段
- short_summary：简短摘要用于SEO和列表展示，不超过100字

#### 3.2.3 z_hot_topics（热点话题表）

| 字段 | 类型 | 说明 | 约束 |
|-----|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY |
| topic_id | VARCHAR(32) | 热点业务ID | NOT NULL, UNIQUE |
| title | VARCHAR(256) | AI提炼标题 | NOT NULL |
| summary | TEXT | AI完整总结 | NOT NULL |
| source_url | VARCHAR(512) | 原始链接 | NULL |
| source_name | VARCHAR(64) | 来源平台名称 | NULL |
| publish_time | DATETIME | 原始发布时间 | NULL |
| category | VARCHAR(32) | 分类标签 | NULL |
| is_active | TINYINT | 是否有效：0-下架 1-展示 | DEFAULT 1 |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | ON UPDATE CURRENT_TIMESTAMP |

#### 3.2.4 z_search_index（搜索索引表）

| 字段 | 类型 | 说明 | 约束 |
|-----|------|------|------|
| id | BIGINT | 自增主键 | PRIMARY KEY |
| content_type | VARCHAR(32) | 内容类型（NOTE/HOT） | NOT NULL |
| content_id | VARCHAR(32) | 内容业务ID | NOT NULL |
| title | VARCHAR(256) | 标题 | NOT NULL |
| keywords | JSON | 关键词数组 | NULL |
| tags | JSON | 标签数组 | NULL |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

---

## 四、前端架构设计

### 4.1 页面路由结构

```javascript
// app/src/router/index.js
import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Notes from '../views/Notes.vue'
import NoteDetail from '../views/NoteDetail.vue'
import Hot from '../views/Hot.vue'
import Theater from '../views/Theater.vue'
import Skills from '../views/Skills.vue'
import Search from '../views/Search.vue'
import About from '../views/About.vue'

const routes = [
  { path: '/', component: Home },                    // 3D首页与导航
  { path: '/notes', component: Notes },              // 地球-技术笔记模块
  { path: '/notes/:id', component: NoteDetail, props: true }, // 笔记详情
  { path: '/hot', component: Hot },                  // 木星-热点摘要模块
  { path: '/theater', component: Theater },          // 火星-动画展区（前端维持现状）
  { path: '/skills', component: Skills },            // 冰巨星-技能展示（前端维持现状）
  { path: '/search', component: Search },            // 紫薇星-内容检索模块
  { path: '/about', component: About },              // 站长主页（静态页面）
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return { top: 0 }
  }
})

export default router
```

### 4.2 目录结构

```
app/
├── public/
│   ├── favicon.svg
│   ├── icons.svg
│   └── wechat-qr.png              # 微信公众号二维码（新增）
├── src/
│   ├── api/
│   │   ├── notes.js               # 笔记API（新增）
│   │   ├── hot.js                 # 热点API（改造）
│   │   ├── search.js              # 搜索API（改造）
│   │   ├── auth.js                # 认证API（新增）
│   │   └── http.js                # HTTP客户端（保留）
│   ├── assets/
│   │   ├── hero.png
│   │   └── icons.js
│   ├── components/
│   │   ├── UnlockModal.vue        # 解锁弹窗（新增）
│   │   ├── LoadingSpinner.vue     # 加载动画（保留）
│   │   ├── ToastContainer.vue     # 提示组件（保留）
│   │   └── Footer.vue             # 全局页脚（新增）
│   ├── composables/
│   │   ├── useAuth.js             # 通用认证Hook（新增）
│   │   └── useToast.js            # 提示逻辑（保留）
│   ├── views/
│   │   ├── Home.vue               # 3D首页（保留改造）
│   │   ├── Notes.vue              # 笔记列表（新增）
│   │   ├── NoteDetail.vue         # 笔记详情（新增）
│   │   ├── Hot.vue                # 热点摘要（改造）
│   │   ├── Theater.vue            # 动画展区（保留）
│   │   ├── Skills.vue             # 技能展示（保留）
│   │   ├── Search.vue             # 内容检索（改造）
│   │   └── About.vue              # 站长主页（新增）
│   ├── router/
│   │   └── index.js               # 路由配置（重构）
│   ├── App.vue                    # 根组件（改造）
│   ├── main.js                    # 入口文件（保留）
│   └── style.css                  # 全局样式（保留）
├── index.html
├── package.json
└── vite.config.js
```

### 4.3 核心组件实现

#### 4.3.1 通用认证Hook

```javascript
// src/composables/useAuth.js
import { ref } from 'vue'
import { requestUnlockCode, checkUnlockStatus } from '../api/auth.js'

export function useAuth() {
  const unlockState = ref({})

  /**
   * 生成或获取device_id
   * @returns {string} UUID v4格式的设备ID
   */
  function getDeviceId() {
    let deviceId = localStorage.getItem('device_id')
    if (!deviceId) {
      deviceId = crypto.randomUUID()
      localStorage.setItem('device_id', deviceId)
    }
    return deviceId
  }

  /**
   * 请求解锁
   * @param {string} moduleType - 模块类型（NOTE/OTHER）
   * @param {string} targetId - 目标内容ID
   * @returns {Promise<{code: string, expires_in: number, deviceId: string}>}
   */
  async function requestUnlock(moduleType, targetId) {
    const deviceId = getDeviceId()
    const result = await requestUnlockCode({
      device_id: deviceId,
      module_type: moduleType,
      target_id: targetId
    })
    return { ...result, deviceId }
  }

  /**
   * 轮询检查解锁状态
   * @param {string} moduleType - 模块类型
   * @param {string} targetId - 目标内容ID
   * @param {Function} onSuccess - 解锁成功回调
   * @returns {Function} 停止轮询的函数
   */
  function startPolling(moduleType, targetId, onSuccess) {
    const deviceId = getDeviceId()
    const timer = setInterval(async () => {
      try {
        const status = await checkUnlockStatus({
          device_id: deviceId,
          module_type: moduleType,
          target_id: targetId
        })
        if (status.unlocked) {
          clearInterval(timer)
          // 保存解锁状态到localStorage（12小时有效期）
          const unlockKey = `unlock_${moduleType}_${targetId}`
          localStorage.setItem(unlockKey, JSON.stringify({
            unlocked: true,
            expires: Date.now() + 12 * 60 * 60 * 1000
          }))
          onSuccess()
        }
      } catch (error) {
        console.error('轮询解锁状态失败:', error)
      }
    }, 3000)
    
    // 返回停止轮询的函数
    return () => clearInterval(timer)
  }

  /**
   * 检查本地解锁状态
   * @param {string} moduleType - 模块类型
   * @param {string} targetId - 目标内容ID
   * @returns {boolean} 是否已解锁
   */
  function isLocallyUnlocked(moduleType, targetId) {
    const unlockKey = `unlock_${moduleType}_${targetId}`
    const state = localStorage.getItem(unlockKey)
    if (state) {
      const { unlocked, expires } = JSON.parse(state)
      if (unlocked && Date.now() < expires) {
        return true
      }
      // 已过期，清除本地状态
      localStorage.removeItem(unlockKey)
    }
    return false
  }

  /**
   * 清除本地解锁状态
   * @param {string} moduleType - 模块类型
   * @param {string} targetId - 目标内容ID
   */
  function clearLocalUnlock(moduleType, targetId) {
    const unlockKey = `unlock_${moduleType}_${targetId}`
    localStorage.removeItem(unlockKey)
  }

  return {
    unlockState,
    getDeviceId,
    requestUnlock,
    startPolling,
    isLocallyUnlocked,
    clearLocalUnlock
  }
}
```

#### 4.3.2 解锁弹窗组件

```vue
<!-- src/components/UnlockModal.vue -->
<template>
  <div v-if="visible" class="unlock-modal-overlay" @click.self="close">
    <div class="unlock-modal">
      <div class="modal-header">
        <h3>解锁全文</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <div class="step-tips">
          <p class="step">步骤 1：复制下方口令</p>
          <div class="code-display">{{ unlockCode }}</div>
          <button class="copy-btn" @click="copyCode">复制口令</button>
        </div>
        
        <div class="step-tips">
          <p class="step">步骤 2：扫码关注公众号</p>
          <img :src="qrCodeUrl" alt="微信公众号二维码" class="qr-code" />
        </div>
        
        <div class="step-tips">
          <p class="step">步骤 3：发送口令到公众号</p>
          <p class="hint">发送后等待3-5秒，内容将自动解锁</p>
        </div>
        
        <div class="loading-indicator" v-if="polling">
          <LoadingSpinner size="small" />
          <span>等待解锁中...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  qrCodeUrl: {
    type: String,
    required: true
  },
  unlockCode: {
    type: String,
    required: true
  },
  polling: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'close'])

function close() {
  emit('update:visible', false)
  emit('close')
}

function copyCode() {
  navigator.clipboard.writeText(props.unlockCode).then(() => {
    // 使用toast提示
    console.log('口令已复制')
  }).catch(err => {
    console.error('复制失败:', err)
  })
}
</script>

<style scoped>
.unlock-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.unlock-modal {
  background: white;
  border-radius: 12px;
  max-width: 400px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.step-tips {
  margin-bottom: 20px;
}

.step {
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  font-size: 14px;
}

.code-display {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  letter-spacing: 8px;
  color: #1890ff;
  margin-bottom: 10px;
  font-family: monospace;
}

.copy-btn {
  width: 100%;
  padding: 10px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.copy-btn:hover {
  background: #40a9ff;
}

.qr-code {
  width: 200px;
  height: 200px;
  display: block;
  margin: 10px auto;
  border-radius: 8px;
}

.hint {
  color: #999;
  font-size: 12px;
  text-align: center;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px;
  color: #1890ff;
  font-size: 14px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
```

#### 4.3.3 笔记详情页（含解锁流程）

```vue
<!-- src/views/NoteDetail.vue -->
<template>
  <div class="note-detail">
    <LoadingSpinner v-if="loading" />
    
    <template v-else-if="note">
      <!-- 笔记头部 -->
      <div class="note-header">
        <h1>{{ note.title }}</h1>
        <div class="note-meta">
          <span class="category">{{ note.category }}</span>
          <span class="views">{{ note.view_count }} 人已读</span>
          <span class="time">{{ formatTime(note.updated_at) }}</span>
        </div>
      </div>
      
      <!-- 封面图 -->
      <img v-if="note.cover_image" :src="note.cover_image" class="cover-image" />
      
      <!-- 预览内容（未解锁） -->
      <div v-if="!isUnlocked && note.is_locked" class="preview-content">
        <div v-html="previewContent"></div>
        <div class="blur-overlay">
          <div class="blur-hint">
            <p>🔒 剩余内容需要解锁后阅读</p>
            <button class="unlock-btn" @click="handleUnlock">
              阅读全文
            </button>
          </div>
        </div>
      </div>
      
      <!-- 完整内容（已解锁或无需解锁） -->
      <div v-else class="full-content" v-html="fullContent"></div>
      
      <!-- 标签 -->
      <div v-if="note.tags && note.tags.length" class="note-tags">
        <span v-for="tag in note.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </template>
    
    <!-- 解锁弹窗 -->
    <UnlockModal
      v-model:visible="showUnlockModal"
      :qr-code-url="qrCodeUrl"
      :unlock-code="unlockCode"
      :polling="isPolling"
      @close="stopPolling"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'
import { getNotePreview, getNoteDetail } from '../api/notes.js'
import UnlockModal from '../components/UnlockModal.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const route = useRoute()
const { requestUnlock, startPolling, isLocallyUnlocked } = useAuth()

const loading = ref(true)
const note = ref(null)
const isUnlocked = ref(false)
const previewContent = ref('')
const fullContent = ref('')
const showUnlockModal = ref(false)
const unlockCode = ref('')
const qrCodeUrl = ref('/wechat-qr.png')
const isPolling = ref(false)
let stopPollingFn = null

onMounted(async () => {
  const noteId = route.params.id
  await loadNote(noteId)
})

onUnmounted(() => {
  if (stopPollingFn) {
    stopPollingFn()
  }
})

async function loadNote(noteId) {
  loading.value = true
  try {
    // 检查本地解锁状态
    if (isLocallyUnlocked('NOTE', noteId)) {
      isUnlocked.value = true
      const { data } = await getNoteDetail(noteId)
      note.value = data.note
      fullContent.value = data.note.content
    } else {
      const { data } = await getNotePreview(noteId)
      note.value = data.note
      previewContent.value = data.preview_content
      
      // 如果笔记不需要解锁
      if (!data.note.is_locked) {
        isUnlocked.value = true
        fullContent.value = data.note.content
      }
    }
  } catch (error) {
    console.error('加载笔记失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleUnlock() {
  try {
    const result = await requestUnlock('NOTE', route.params.id)
    unlockCode.value = result.code
    showUnlockModal.value = true
    isPolling.value = true
    
    // 启动轮询
    stopPollingFn = startPolling('NOTE', route.params.id, () => {
      isUnlocked.value = true
      isPolling.value = false
      showUnlockModal.value = false
      // 加载完整内容
      loadFullContent()
    })
  } catch (error) {
    console.error('请求解锁失败:', error)
  }
}

function stopPolling() {
  if (stopPollingFn) {
    stopPollingFn()
    isPolling.value = false
  }
}

async function loadFullContent() {
  try {
    const { data } = await getNoteDetail(route.params.id)
    fullContent.value = data.note.content
  } catch (error) {
    console.error('加载完整内容失败:', error)
  }
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>

<style scoped>
.note-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.note-header {
  margin-bottom: 30px;
}

.note-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 15px;
  line-height: 1.4;
}

.note-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
}

.category {
  background: #e6f7ff;
  color: #1890ff;
  padding: 4px 12px;
  border-radius: 4px;
}

.cover-image {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 30px;
}

.preview-content {
  position: relative;
  margin-bottom: 30px;
}

.full-content,
.preview-content {
  line-height: 1.8;
  color: #333;
  font-size: 16px;
}

.full-content :deep(h1),
.full-content :deep(h2),
.full-content :deep(h3) {
  margin: 30px 0 15px;
  color: #333;
}

.full-content :deep(p) {
  margin-bottom: 15px;
}

.full-content :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.full-content :deep(pre) {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
}

.blur-overlay {
  position: relative;
  margin-top: -100px;
  padding-top: 100px;
  background: linear-gradient(to bottom, transparent, white 50%);
}

.blur-hint {
  text-align: center;
  padding: 40px 20px;
}

.blur-hint p {
  color: #999;
  margin-bottom: 20px;
  font-size: 14px;
}

.unlock-btn {
  padding: 12px 40px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.unlock-btn:hover {
  background: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.tag {
  background: #f5f5f5;
  color: #666;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
```

### 4.4 API接口封装

```javascript
// src/api/auth.js
import http from './http.js'

/**
 * 请求动态口令
 */
export function requestUnlockCode(data) {
  return http.post('/api/auth/unlock/request', data)
}

/**
 * 检查解锁状态
 */
export function checkUnlockStatus(params) {
  return http.get('/api/auth/unlock/status', { params })
}
```

```javascript
// src/api/notes.js
import http from './http.js'

/**
 * 获取笔记列表
 */
export function getNotes(params) {
  return http.get('/api/notes', { params })
}

/**
 * 获取笔记详情
 */
export function getNoteDetail(id) {
  return http.get(`/api/notes/${id}`)
}

/**
 * 获取笔记预览（前30%）
 */
export function getNotePreview(id) {
  return http.get(`/api/notes/${id}/preview`)
}
```

---

## 五、后端架构设计

### 5.1 目录结构

```
src/main/java/cn/kong/cosmos/
├── biz/
│   ├── note/                         # 技术笔记模块
│   │   ├── controller/
│   │   │   └── NoteController.java
│   │   ├── dto/
│   │   │   ├── req/
│   │   │   └── resp/
│   │   │       ├── NoteListDTO.java
│   │   │       ├── NoteDetailDTO.java
│   │   │       └── NotePreviewDTO.java
│   │   ├── entity/
│   │   │   └── Note.java
│   │   ├── mapper/
│   │   │   └── NoteMapper.java
│   │   └── service/
│   │       ├── NoteService.java
│   │       └── impl/
│   │           └── NoteServiceImpl.java
│   ├── hot/                          # 热点摘要模块（保留改造）
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── mapper/
│   │   └── service/
│   ├── search/                       # 搜索模块（保留改造）
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── mapper/
│   │   └── service/
│   └── auth/                         # 通用认证模块（新增）
│       ├── controller/
│       │   └── AuthUnlockController.java
│       ├── dto/
│       │   ├── req/
│       │   │   └── UnlockRequestDTO.java
│       │   └── resp/
│       │       ├── UnlockCodeDTO.java
│       │       └── UnlockStatusDTO.java
│       ├── entity/
│       │   └── AuthUnlock.java
│       ├── mapper/
│       │   └── AuthUnlockMapper.java
│       └── service/
│           ├── AuthUnlockService.java
│           └── impl/
│               └── AuthUnlockServiceImpl.java
├── auth/                             # 原用户认证模块（移除）
├── common/                           # 通用组件（保留）
│   ├── config/
│   ├── core/
│   └── exception/
└── ZCosmosApplication.java
```

### 5.2 核心接口实现

#### 5.2.1 通用认证控制器

```java
// controller/AuthUnlockController.java
package cn.kong.cosmos.biz.auth.controller;

import cn.kong.cosmos.biz.auth.dto.req.UnlockRequestDTO;
import cn.kong.cosmos.biz.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.biz.auth.dto.resp.UnlockStatusDTO;
import cn.kong.cosmos.biz.auth.service.AuthUnlockService;
import cn.kong.cosmos.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/unlock")
@RequiredArgsConstructor
public class AuthUnlockController {
    
    private final AuthUnlockService authUnlockService;
    
    /**
     * 请求动态口令
     */
    @PostMapping("/request")
    public Result<UnlockCodeDTO> requestUnlockCode(@RequestBody UnlockRequestDTO request) {
        UnlockCodeDTO code = authUnlockService.requestUnlockCode(
            request.getDeviceId(),
            request.getModuleType(),
            request.getTargetId()
        );
        return Result.success(code);
    }
    
    /**
     * 检查解锁状态
     */
    @GetMapping("/status")
    public Result<UnlockStatusDTO> checkUnlockStatus(
            @RequestParam String deviceId,
            @RequestParam String moduleType,
            @RequestParam String targetId) {
        UnlockStatusDTO status = authUnlockService.checkUnlockStatus(
            deviceId, moduleType, targetId
        );
        return Result.success(status);
    }
    
    /**
     * 微信公众号回调验证
     * 注意：此接口需要放行，不经过认证拦截器
     */
    @PostMapping("/wechat/callback")
    public String handleWechatCallback(@RequestBody String xmlMessage) {
        return authUnlockService.handleWechatCallback(xmlMessage);
    }
}
```

#### 5.2.2 认证服务实现

```java
// service/AuthUnlockService.java
package cn.kong.cosmos.biz.auth.service;

import cn.kong.cosmos.biz.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.biz.auth.dto.resp.UnlockStatusDTO;

public interface AuthUnlockService {
    /**
     * 生成动态口令
     * @param deviceId 设备ID
     * @param moduleType 模块类型
     * @param targetId 目标内容ID
     * @return 动态口令和过期时间
     */
    UnlockCodeDTO requestUnlockCode(String deviceId, String moduleType, String targetId);
    
    /**
     * 检查解锁状态
     * @param deviceId 设备ID
     * @param moduleType 模块类型
     * @param targetId 目标内容ID
     * @return 解锁状态
     */
    UnlockStatusDTO checkUnlockStatus(String deviceId, String moduleType, String targetId);
    
    /**
     * 微信公众号回调验证
     * @param xmlMessage 微信XML消息
     * @return 微信响应XML
     */
    String handleWechatCallback(String xmlMessage);
}
```

```java
// service/impl/AuthUnlockServiceImpl.java
package cn.kong.cosmos.biz.auth.service.impl;

import cn.kong.cosmos.biz.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.biz.auth.dto.resp.UnlockStatusDTO;
import cn.kong.cosmos.biz.auth.entity.AuthUnlock;
import cn.kong.cosmos.biz.auth.mapper.AuthUnlockMapper;
import cn.kong.cosmos.biz.auth.service.AuthUnlockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUnlockServiceImpl implements AuthUnlockService {
    
    private final AuthUnlockMapper authUnlockMapper;
    private final StringRedisTemplate redisTemplate;
    
    @Override
    @Transactional
    public UnlockCodeDTO requestUnlockCode(String deviceId, String moduleType, String targetId) {
        // 生成6位随机口令（数字）
        String unlockCode = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 删除旧的未解锁记录
        authUnlockMapper.delete(
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, moduleType)
                .eq(AuthUnlock::getTargetId, targetId)
                .eq(AuthUnlock::getStatus, 0)
        );
        
        // 保存到数据库（12小时过期）
        AuthUnlock authUnlock = new AuthUnlock();
        authUnlock.setDeviceId(deviceId);
        authUnlock.setModuleType(moduleType);
        authUnlock.setTargetId(targetId);
        authUnlock.setUnlockCode(unlockCode);
        authUnlock.setStatus(0);
        authUnlock.setExpiresAt(LocalDateTime.now().plusHours(12));
        authUnlockMapper.insert(authUnlock);
        
        // 缓存到Redis（快速查询）
        String redisKey = "unlock:code:" + unlockCode;
        String redisValue = deviceId + ":" + moduleType + ":" + targetId;
        redisTemplate.opsForValue().set(redisKey, redisValue, 12, TimeUnit.HOURS);
        
        log.info("生成解锁口令: deviceId={}, moduleType={}, targetId={}, code={}", 
            deviceId, moduleType, targetId, unlockCode);
        
        return new UnlockCodeDTO(unlockCode, 43200); // 43200秒 = 12小时
    }
    
    @Override
    public UnlockStatusDTO checkUnlockStatus(String deviceId, String moduleType, String targetId) {
        AuthUnlock authUnlock = authUnlockMapper.selectOne(
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, moduleType)
                .eq(AuthUnlock::getTargetId, targetId)
                .eq(AuthUnlock::getStatus, 1)
                .gt(AuthUnlock::getExpiresAt, LocalDateTime.now())
        );
        
        return new UnlockStatusDTO(authUnlock != null);
    }
    
    @Override
    @Transactional
    public String handleWechatCallback(String xmlMessage) {
        try {
            // 1. 解析微信XML消息
            // 使用 WxJava 库解析
            // WxMpXmlMessage wxMessage = WxMpXmlMessage.fromXml(xmlMessage);
            
            // 2. 提取用户发送的口令
            // String content = wxMessage.getContent();
            
            // 3. 从Redis中查找口令
            // String redisKey = "unlock:code:" + content;
            // String redisValue = redisTemplate.opsForValue().get(redisKey);
            
            // 4. 验证口令并更新状态
            // if (redisValue != null) {
            //     String[] parts = redisValue.split(":");
            //     String deviceId = parts[0];
            //     String moduleType = parts[1];
            //     String targetId = parts[2];
            //     
            //     authUnlockMapper.update(
            //         new AuthUnlock().setStatus(1),
            //         new LambdaQueryWrapper<AuthUnlock>()
            //             .eq(AuthUnlock::getDeviceId, deviceId)
            //             .eq(AuthUnlock::getModuleType, moduleType)
            //             .eq(AuthUnlock::getTargetId, targetId)
            //             .eq(AuthUnlock::getUnlockCode, content)
            //     );
            //     
            //     // 返回成功消息
            //     return buildWechatReply(wxMessage, "解锁成功！请返回页面查看完整内容。");
            // }
            
            // 临时实现：返回空响应
            return "";
            
        } catch (Exception e) {
            log.error("处理微信回调失败", e);
            return "";
        }
    }
    
    /**
     * 构建微信回复消息
     */
    private String buildWechatReply(Object wxMessage, String content) {
        // 使用 WxJava 库构建回复消息
        // WxMpXmlOutTextMessage outMessage = WxMpXmlOutMessage.TEXT()
        //     .content(content)
        //     .fromUser(wxMessage.getToUser())
        //     .toUser(wxMessage.getFromUser())
        //     .build();
        // return outMessage.toXml();
        return "";
    }
}
```

#### 5.2.3 笔记控制器

```java
// controller/NoteController.java
package cn.kong.cosmos.biz.note.controller;

import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.dto.resp.NotePreviewDTO;
import cn.kong.cosmos.biz.note.service.NoteService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;
    
    /**
     * 获取笔记列表
     */
    @GetMapping
    public Result<IPage<NoteListDTO>> listNotes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        return Result.success(noteService.listNotes(page, size, category, tag));
    }
    
    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    public Result<NoteDetailDTO> getNoteDetail(@PathVariable String id) {
        return Result.success(noteService.getNoteDetail(id));
    }
    
    /**
     * 获取笔记预览（前30%）
     */
    @GetMapping("/{id}/preview")
    public Result<NotePreviewDTO> getNotePreview(@PathVariable String id) {
        return Result.success(noteService.getNotePreview(id));
    }
}
```

#### 5.2.4 笔记服务实现

```java
// service/impl/NoteServiceImpl.java
package cn.kong.cosmos.biz.note.service.impl;

import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.dto.resp.NotePreviewDTO;
import cn.kong.cosmos.biz.note.entity.Note;
import cn.kong.cosmos.biz.note.mapper.NoteMapper;
import cn.kong.cosmos.biz.note.service.NoteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    
    private final NoteMapper noteMapper;
    
    @Override
    public IPage<NoteListDTO> listNotes(Integer page, Integer size, String category, String tag) {
        Page<Note> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Note::getUpdatedAt);
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(Note::getCategory, category);
        }
        
        // TODO: 标签查询（JSON字段查询）
        
        IPage<Note> notePage = noteMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为DTO
        return notePage.convert(this::toListDTO);
    }
    
    @Override
    public NoteDetailDTO getNoteDetail(String noteId) {
        Note note = noteMapper.selectOne(
            new LambdaQueryWrapper<Note>().eq(Note::getNoteId, noteId)
        );
        
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        
        // 增加阅读量
        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);
        
        return toDetailDTO(note);
    }
    
    @Override
    public NotePreviewDTO getNotePreview(String noteId) {
        Note note = noteMapper.selectOne(
            new LambdaQueryWrapper<Note>().eq(Note::getNoteId, noteId)
        );
        
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        
        // 根据preview_ratio动态提取预览内容
        String previewContent = extractPreview(note.getContent(), note.getPreviewRatio());
        
        return toPreviewDTO(note, previewContent);
    }
    
    /**
     * 根据比例提取预览内容
     * @param content 完整内容
     * @param ratio 预览比例（如0.30表示30%）
     */
    private String extractPreview(String content, BigDecimal ratio) {
        if (content == null) return "";
        
        double ratioValue = ratio != null ? ratio.doubleValue() : 0.30;
        int previewLength = (int) (content.length() * ratioValue);
        
        // 确保不超过内容长度
        int endPos = Math.min(previewLength, content.length());
        
        // 尝试在HTML标签边界处截断（避免破坏HTML结构）
        String preview = content.substring(0, endPos);
        
        // 简单处理：如果截断位置在HTML标签内，找到最近的标签结束位置
        int lastTagEnd = preview.lastIndexOf('>');
        if (lastTagEnd > preview.length() * 0.8) {
            // 如果最近的标签结束位置在80%之后，使用该位置
            preview = preview.substring(0, lastTagEnd + 1);
        }
        
        return preview;
    }
    
    /**
     * 转换为列表DTO
     */
    private NoteListDTO toListDTO(Note note) {
        NoteListDTO dto = new NoteListDTO();
        dto.setNoteId(note.getNoteId());
        dto.setTitle(note.getTitle());
        dto.setShortSummary(note.getShortSummary());  // 使用简短摘要
        dto.setCategory(note.getCategory());
        dto.setTags(note.getTags());
        dto.setCoverImage(note.getCoverImage());
        dto.setViewCount(note.getViewCount());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
    
    /**
     * 转换为详情DTO
     */
    private NoteDetailDTO toDetailDTO(Note note) {
        NoteDetailDTO dto = new NoteDetailDTO();
        dto.setNoteId(note.getNoteId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCategory(note.getCategory());
        dto.setTags(note.getTags());
        dto.setCoverImage(note.getCoverImage());
        dto.setViewCount(note.getViewCount());
        dto.setIsLocked(note.getIsLocked());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
    
    /**
     * 转换为预览DTO
     */
    private NotePreviewDTO toPreviewDTO(Note note, String previewContent) {
        NotePreviewDTO dto = new NotePreviewDTO();
        dto.setNote(note);
        dto.setPreviewContent(previewContent);
        return dto;
    }
}
```

### 5.3 API接口文档

#### 5.3.1 通用认证模块

##### 1. 请求动态口令

- **URL**: `/api/auth/unlock/request`
- **Method**: `POST`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "device_id": "550e8400-e29b-41d4-a716-446655440000",
  "module_type": "NOTE"
}
```

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "code": "123456",
    "expires_in": 43200
  }
}
```

##### 2. 检查解锁状态

- **URL**: `/api/auth/unlock/status`
- **Method**: `GET`

**请求参数**:
```
?device_id=550e8400-e29b-41d4-a716-446655440000&module_type=NOTE
```

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "unlocked": true
  }
}
```

##### 3. 微信公众号回调

- **URL**: `/api/auth/unlock/wechat/callback`
- **Method**: `POST`
- **Content-Type**: `application/xml`

**说明**: 此接口由微信公众号服务器调用，用于验证用户发送的动态口令。

#### 5.3.2 技术笔记模块

##### 1. 获取笔记列表

- **URL**: `/api/notes`
- **Method**: `GET`

**请求参数**:
```
?page=1&size=10&category=Java&tag=Spring
```

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "note_id": "note_123456",
        "title": "Spring Boot实战笔记",
        "category": "Java",
        "tags": ["Spring", "Boot"],
        "cover_image": "https://example.com/cover.jpg",
        "view_count": 1234,
        "updated_at": "2026-05-09 10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

##### 2. 获取笔记详情

- **URL**: `/api/notes/{id}`
- **Method**: `GET`

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "note_id": "note_123456",
    "title": "Spring Boot实战笔记",
    "content": "<p>完整HTML内容</p>",
    "category": "Java",
    "tags": ["Spring", "Boot"],
    "cover_image": "https://example.com/cover.jpg",
    "view_count": 1235,
    "is_locked": 1,
    "updated_at": "2026-05-09 10:00:00"
  }
}
```

##### 2. 获取笔记预览

- **URL**: `/api/notes/{id}/preview`
- **Method**: `GET`

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "note": {
      "note_id": "note_123456",
      "title": "Spring Boot实战笔记",
      "is_locked": 1,
      "preview_ratio": 0.30
    },
    "preview_content": "<p>根据preview_ratio动态生成的前30%HTML内容</p>"
  }
}
```

#### 5.3.3 热点话题模块

##### 1. 获取热点列表

- **URL**: `/api/hot/topics`
- **Method**: `GET`

**请求参数**:
```
?page=1&size=10&category=科技
```

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "topic_id": "hot_123456",
        "title": "AI技术突破：GPT-5发布",
        "summary": "AI完整总结内容...",
        "source_url": "https://example.com/news",
        "source_name": "科技日报",
        "publish_time": "2026-05-09 08:00:00",
        "category": "科技",
        "created_at": "2026-05-09 09:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

#### 5.3.4 搜索模块

##### 1. 全局搜索

- **URL**: `/api/search`
- **Method**: `GET`

**请求参数**:
```
?keyword=Spring&type=NOTE&page=1&size=10
```

**响应参数**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "content_type": "NOTE",
        "content_id": "note_123456",
        "title": "Spring Boot实战笔记",
        "highlight": "...<em>Spring</em> Boot..."
      }
    ],
    "total": 20,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

---

## 六、通用认证机制设计

### 6.1 架构设计

认证机制设计为**可扩展的通用模块**，通过 `module_type` 字段支持未来新增其他需要认证的功能模块。

```
┌─────────────────────────────────────────┐
│          前端（Vue 3）                    │
│  ┌───────────────────────────────────┐  │
│  │  useAuth Hook                     │  │
│  │  - getDeviceId()                  │  │
│  │  - requestUnlock()                │  │
│  │  - startPolling()                 │  │
│  │  - isLocallyUnlocked()            │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
              ↕ HTTP
┌─────────────────────────────────────────┐
│          后端（Spring Boot）              │
│  ┌───────────────────────────────────┐  │
│  │  AuthUnlockController             │  │
│  │  - /request                       │  │
│  │  - /status                        │  │
│  │  - /wechat/callback               │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  AuthUnlockService                │  │
│  │  - requestUnlockCode()            │  │
│  │  - checkUnlockStatus()            │  │
│  │  - handleWechatCallback()         │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
              ↕
┌─────────────────────────────────────────┐
│          微信公众号平台                   │
│  - 用户发送口令                          │
│  - 回调后端接口                          │
│  - 验证并返回结果                        │
└─────────────────────────────────────────┘
```

### 6.2 模块类型扩展

```java
// 枚举定义
public enum ModuleType {
    NOTE("技术笔记"),
    VIDEO("视频教程"),
    RESOURCE("资源下载"),
    FUTURE_MODULE("未来扩展模块");
    
    private final String description;
    
    ModuleType(String description) {
        this.description = description;
    }
}
```

### 6.3 解锁流程时序图

```
用户浏览器              前端                 后端API             数据库/Redis        微信公众号
    │                  │                     │                    │                    │
    │  1.访问受保护内容  │                     │                    │                    │
    │─────────────────>│                     │                    │                    │
    │                  │  2.检查本地解锁状态   │                    │                    │
    │<─────────────────│                     │                    │                    │
    │                  │                     │                    │                    │
    │  3.点击阅读全文   │                     │                    │                    │
    │─────────────────>│                     │                    │                    │
    │                  │  4.POST /request    │                    │                    │
    │                  │────────────────────>│                    │                    │
    │                  │                     │  5.生成口令并保存   │                    │
    │                  │                     │───────────────────>│                    │
    │                  │                     │<───────────────────│                    │
    │                  │  6.返回口令          │                    │                    │
    │                  │<────────────────────│                    │                    │
    │  7.展示二维码+口令 │                     │                    │                    │
    │<─────────────────│                     │                    │                    │
    │                  │                     │                    │                    │
    │                  │  8.启动轮询(3s)      │                    │                    │
    │                  │──────┐              │                    │                    │
    │                  │      │ 9.GET /status│                    │                    │
    │                  │      │─────────────>│                    │                    │
    │                  │      │              │  10.查询状态        │                    │
    │                  │      │              │───────────────────>│                    │
    │                  │      │<─────────────│                    │                    │
    │                  │      │ 11.返回未解锁  │                    │                    │
    │                  │<─────│              │                    │                    │
    │                  │<─────┘              │                    │                    │
    │                  │                     │                    │                    │
    │  12.扫码关注公众号 │                     │                    │                    │
    │─────────────────────────────────────────────────────────────────────────────────>│
    │  13.发送口令      │                     │                    │                    │
    │─────────────────────────────────────────────────────────────────────────────────>│
    │                  │                     │                    │                    │
    │                  │                     │  14.微信回调        │                    │
    │                  │                     │<────────────────────────────────────────│
    │                  │                     │  15.验证口令        │                    │
    │                  │                     │───────────────────>│                    │
    │                  │                     │  16.更新状态为已解锁 │                    │
    │                  │                     │<───────────────────│                    │
    │                  │                     │                    │                    │
    │                  │  17.GET /status     │                    │                    │
    │                  │────────────────────>│                    │                    │
    │                  │                     │  18.查询状态        │                    │
    │                  │                     │───────────────────>│                    │
    │                  │                     │  19.返回已解锁       │                    │
    │                  │<────────────────────│                    │                    │
    │  20.停止轮询，加载完整内容              │                    │                    │
    │<─────────────────│                     │                    │                    │
```

### 6.4 安全设计

1. **动态口令安全**
   - 每次请求生成不同的6位随机数字口令
   - 口令12小时过期
   - 口令验证后立即失效

2. **设备标识安全**
   - 使用UUID v4格式生成device_id
   - 存储在localStorage，清除缓存后失效
   - 不收集用户隐私信息

3. **防暴力破解**
   - 同一device_id+module_type+target_id组合只保留最新口令
   - 可通过Redis限流（后续优化）

4. **数据过期清理**
   - 定时任务清理过期解锁记录
   - Redis自动过期删除

---

## 七、部署配置

### 7.1 环境变量配置

```bash
# .env.production

# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/z-cosmos?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=zcosmos_user
DB_PASSWORD=your_secure_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# 微信公众号配置
WECHAT_MP_APP_ID=wx_your_app_id
WECHAT_MP_SECRET=your_app_secret
WECHAT_MP_TOKEN=your_token
WECHAT_MP_AES_KEY=your_aes_key

# JWT配置（如果保留其他认证）
JWT_SECRET=your_jwt_secret_key_at_least_256_bits

# 备案信息
ICP_NUMBER=京ICP备XXXXXXXX号
POLICE_NUMBER=京公网安备 XXXXXXXXX号
COPYRIGHT_TEXT=© 2026 Z-Cosmos. All rights reserved.
CONTACT_EMAIL=contact@example.com
```

### 7.2 Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 强制HTTPS（生产环境必须）
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL证书配置
    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 前端静态文件
    location / {
        root /var/www/z-cosmos/app/dist;
        try_files $uri $uri/ /index.html;
        
        # 缓存策略
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 后端API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 微信公众号回调（必须公网可访问）
    location /api/auth/unlock/wechat/callback {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # 文件上传目录（如果需要）
    location /uploads/ {
        alias /var/www/z-cosmos/uploads/;
        expires 7d;
        add_header Cache-Control "public";
    }
    
    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
}
```

### 7.3 Spring Boot配置

```yaml
# application-prod.yml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: z-cosmos
  
  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 600000
      connection-timeout: 30000
      max-lifetime: 1800000
  
  # Redis配置
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms
  
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 100MB

# MyBatis Plus配置
mybatis-plus:
  type-aliases-package: cn.kong.cosmos.biz
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl  # 生产环境使用slf4j

# 微信公众号配置
wechat:
  mp:
    app-id: ${WECHAT_MP_APP_ID}
    secret: ${WECHAT_MP_SECRET}
    token: ${WECHAT_MP_TOKEN}
    aes-key: ${WECHAT_MP_AES_KEY}

# 备案信息
site:
  icp-number: ${ICP_NUMBER}
  police-number: ${POLICE_NUMBER}
  copyright: ${COPYRIGHT_TEXT}
  contact-email: ${CONTACT_EMAIL}

# 日志配置
logging:
  level:
    root: WARN
    cn.kong.cosmos: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/z-cosmos/app.log
    max-size: 100MB
    max-history: 30
```

### 7.4 部署脚本

```bash
#!/bin/bash
# deploy.sh

echo "=== 开始部署 Z-Cosmos ==="

# 1. 前端构建
echo "1. 构建前端..."
cd app
npm install
npm run build
cd ..

# 2. 后端打包
echo "2. 打包后端..."
mvn clean package -DskipTests

# 3. 停止旧服务
echo "3. 停止旧服务..."
pkill -f 'z-cosmos' || true

# 4. 启动新服务
echo "4. 启动新服务..."
nohup java -jar \
  -Dspring.profiles.active=prod \
  target/z-cosmos-1.0.0.jar \
  > /var/log/z-cosmos/startup.log 2>&1 &

# 5. 等待启动
echo "5. 等待服务启动..."
sleep 10

# 6. 检查服务状态
if curl -s http://localhost:8080/api/notes > /dev/null; then
  echo "✅ 服务启动成功！"
else
  echo "❌ 服务启动失败，请检查日志"
  tail -n 50 /var/log/z-cosmos/startup.log
fi

echo "=== 部署完成 ==="
```

---

## 八、备案材料清单

### 8.1 必需材料

1. **网站负责人身份证**
   - 正反面扫描件（JPG格式）
   - 清晰度要求：文字可辨认

2. **域名证书**
   - 域名注册商提供的证书
   - 域名持有者需与备案主体一致

3. **网站建设方案书**
   - 网站用途说明（个人技术博客）
   - 内容范围（技术笔记、热点摘要）
   - 不涉及UGC内容声明

4. **ICP备案信息真实性核验单**
   - 接入服务商提供
   - 需手写签字

5. **网站内容承诺书**
   - 承诺不提供用户注册功能
   - 承诺不提供用户发布内容功能
   - 承诺内容符合法律法规

6. **域名备案接入协议**
   - 云服务提供商提供
   - 需盖章签字

### 8.2 网站建设方案书模板

```
网站建设方案书

一、网站基本信息
网站名称：Z-Cosmos
网站域名：your-domain.com
网站类型：个人技术博客
网站负责人：XXX
身份证号：XXX

二、网站用途
本站为站长个人技术博客，主要用于：
1. 分享个人技术学习笔记
2. 展示AI生成的热点技术资讯
3. 展示个人项目作品

三、内容范围
网站内容包括：
1. 技术笔记：站长个人编写的技术文章
2. 热点摘要：AI自动生成的技术资讯摘要
3. 动画展示：前端技术演示
4. 技能展示：个人技术栈展示

四、功能说明
1. 内容展示：仅支持浏览功能，不提供用户交互
2. 内容检索：站内搜索功能，仅搜索站长发布内容
3. 扫码解锁：部分技术笔记需通过微信公众号验证后阅读

五、合规承诺
1. 本站为个人备案网站，不提供用户注册功能
2. 不提供用户发布内容功能（无UGC）
3. 所有内容由站长个人发布或AI自动生成
4. 严格遵守《互联网信息服务管理办法》
5. 不发布违法违规内容

承诺人：XXX
日 期：2026-05-09
```

---

## 九、实施步骤

### 阶段一：数据库迁移（预计1天）

#### 9.1.1 备份现有数据库

```bash
# 备份完整数据库
mysqldump -u user -p z-cosmos > z-cosmos-backup-$(date +%Y%m%d).sql

# 验证备份文件
ls -lh z-cosmos-backup-*.sql
```

#### 9.1.2 执行新DDL

```bash
# 登录MySQL
mysql -u user -p

# 执行新DDL
source /path/to/new-schema.sql
```

#### 9.1.3 数据迁移脚本

```sql
-- 迁移笔记数据（从z_posts到z_notes）
INSERT INTO z_notes (note_id, title, content, summary, cover_image, category, tags, view_count, is_locked, created_at, updated_at)
SELECT 
    post_id as note_id,
    title,
    content,
    summary,
    cover_image,
    category_code as category,
    '[]' as tags,
    view_count,
    1 as is_locked,
    created_at,
    updated_at
FROM z_posts
WHERE status = 1;  -- 仅迁移已发布的帖子

-- 迁移热点数据（如有）
INSERT INTO z_hot_summaries (summary_id, title, content, source_url, source_name, publish_time, category, is_active, created_at, updated_at)
SELECT 
    hot_id as summary_id,
    title,
    ai_summary as content,
    source_url,
    source_name,
    publish_time,
    category,
    is_active,
    created_at,
    updated_at
FROM z_hot_topics;  -- 假设原表名

-- 构建搜索索引
INSERT INTO z_search_index (content_type, content_id, title, keywords, tags, created_at)
SELECT 
    'NOTE' as content_type,
    note_id as content_id,
    title,
    CONCAT('["', category, '"]') as keywords,
    tags,
    created_at
FROM z_notes;

INSERT INTO z_search_index (content_type, content_id, title, keywords, tags, created_at)
SELECT 
    'HOT' as content_type,
    summary_id as content_id,
    title,
    CONCAT('["', category, '"]') as keywords,
    '[]' as tags,
    created_at
FROM z_hot_summaries;
```

#### 9.1.4 验证迁移结果

```sql
-- 检查数据量
SELECT 
    'z_notes' as table_name, 
    COUNT(*) as record_count 
FROM z_notes
UNION ALL
SELECT 
    'z_hot_summaries', 
    COUNT(*) 
FROM z_hot_summaries
UNION ALL
SELECT 
    'z_search_index', 
    COUNT(*) 
FROM z_search_index;

-- 抽查数据
SELECT note_id, title, category FROM z_notes LIMIT 10;
SELECT summary_id, title, source_name FROM z_hot_summaries LIMIT 10;
```

### 阶段二：后端重构（预计2天）

#### 9.2.1 移除旧模块

```bash
# 删除用户认证模块
rm -rf src/main/java/cn/kong/cosmos/auth/

# 删除社区模块
rm -rf src/main/java/cn/kong/cosmos/biz/community/

# 删除相关依赖（pom.xml中移除）
# - spring-boot-starter-security
# - JWT相关依赖（如果不再需要）
```

#### 9.2.2 创建新模块

```bash
# 创建笔记模块
mkdir -p src/main/java/cn/kong/cosmos/biz/note/{controller,dto/req,dto/resp,entity,mapper,service/impl}

# 创建认证模块
mkdir -p src/main/java/cn/kong/cosmos/biz/auth/{controller,dto/req,dto/resp,entity,mapper,service/impl}
```

#### 9.2.3 实现核心代码

按照第五节的代码示例，依次实现：
1. Entity实体类
2. Mapper接口
3. Service接口和实现
4. Controller控制器
5. DTO数据传输对象

#### 9.2.4 配置调整

```yaml
# application.yml 修改
# 1. 移除OAuth配置
# 2. 移除JWT配置（如果不需要）
# 3. 添加微信公众号配置
# 4. 添加备案信息配置
```

### 阶段三：前端重构（预计2天）

#### 9.3.1 移除旧页面

```bash
# 删除用户相关页面
rm src/views/Login.vue
rm src/views/Profile.vue

# 删除社区相关页面
rm src/views/Community.vue
rm src/views/PostDetail.vue
rm src/views/PostEditor.vue

# 删除相关API
rm src/api/auth.js
rm src/api/community.js
rm src/api/profile.js

# 删除相关composables
rm src/composables/useAuth.js  # 旧的认证逻辑
```

#### 9.3.2 创建新页面

```bash
# 创建笔记页面
touch src/views/Notes.vue
touch src/views/NoteDetail.vue

# 创建站长主页
touch src/views/About.vue

# 创建组件
touch src/components/UnlockModal.vue
touch src/components/Footer.vue

# 创建API
touch src/api/notes.js
touch src/api/auth.js  # 新的认证API

# 创建composables
touch src/composables/useAuth.js  # 新的通用认证Hook
```

#### 9.3.3 实现页面代码

按照第四节的代码示例，依次实现各页面组件。

#### 9.3.4 更新路由配置

按照4.1节的路由配置更新 `router/index.js`。

### 阶段四：测试与部署（预计1天）

#### 9.4.1 功能测试清单

- [ ] 3D首页正常加载，星球可点击跳转
- [ ] 笔记列表正常显示，分页正常
- [ ] 笔记详情页预览正常（前30%）
- [ ] 解锁流程完整测试：
  - [ ] 生成动态口令
  - [ ] 展示二维码
  - [ ] 轮询机制正常
  - [ ] 微信公众号回调验证
  - [ ] 解锁后展示完整内容
  - [ ] 本地缓存12小时有效
- [ ] 热点列表正常显示
- [ ] 热点详情弹窗正常
- [ ] 搜索功能正常
- [ ] 站长主页正常显示
- [ ] 页脚备案信息正常显示

#### 9.4.2 性能测试

```bash
# 使用Apache Bench测试
ab -n 1000 -c 10 http://your-domain.com/api/notes

# 检查响应时间
# 要求：P95 < 500ms
```

#### 9.4.3 部署上线

```bash
# 1. 执行部署脚本
chmod +x deploy.sh
./deploy.sh

# 2. 配置Nginx
sudo cp nginx.conf /etc/nginx/sites-available/z-cosmos
sudo ln -s /etc/nginx/sites-available/z-cosmos /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# 3. 配置SSL证书（使用Let's Encrypt）
sudo certbot --nginx -d your-domain.com

# 4. 验证HTTPS
curl -I https://your-domain.com
```

#### 9.4.4 提交备案申请

1. 登录云服务提供商备案系统
2. 填写备案信息
3. 上传备案材料
4. 等待审核（通常5-20个工作日）
5. 审核通过后，在页脚添加备案号

---

## 十、注意事项

### 10.1 备案合规要点

1. **严禁用户系统**
   - 不提供注册、登录功能
   - 不收集用户个人信息
   - 不使用第三方OAuth登录

2. **严禁UGC内容**
   - 不提供发帖、评论功能
   - 不提供点赞、收藏功能
   - 所有内容仅站长发布

3. **明确备案信息**
   - 页脚必须展示ICP备案号
   - 页脚必须展示公安备案标识
   - 提供联系方式（邮箱）

4. **内容合规**
   - 不发布违法违规内容
   - 注明来源（热点摘要）
   - 添加AI免责声明

### 10.2 技术注意事项

1. **微信公众号配置**
   - 需已认证的服务号或订阅号
   - 配置服务器地址（公网可访问）
   - 配置Token和AES Key

2. **HTTPS必须**
   - 生产环境必须启用HTTPS
   - 使用Let's Encrypt免费证书
   - 配置HTTP强制跳转

3. **数据备份**
   - 定期备份数据库
   - 备份文件加密存储
   - 测试恢复流程

4. **日志监控**
   - 记录关键操作日志
   - 监控异常访问
   - 定期清理日志

### 10.3 后续扩展建议

1. **内容管理系统（CMS）**
   - 开发后台管理界面
   - 支持Markdown编辑
   - 图片上传管理

2. **SEO优化**
   - 添加sitemap.xml
   - 配置robots.txt
   - 添加meta标签

3. **性能优化**
   - CDN加速静态资源
   - Redis缓存热点数据
   - 数据库查询优化

4. **安全加固**
   - 防SQL注入
   - 防XSS攻击
   - 防CSRF攻击
   - 接口限流

---

## 附录

### A. 常见问题解答

**Q1: 个人备案能否提供搜索功能？**
A: 可以。站内搜索仅搜索站长发布的内容，不涉及用户生成内容，符合个人备案要求。

**Q2: 扫码解锁功能是否会影响备案？**
A: 不会。该功能仅用于内容访问控制，不涉及用户注册或个人信息收集。

**Q3: 热点摘要的AI生成内容是否需要标注？**
A: 建议标注。在页面添加AI免责声明，说明内容由AI自动生成，仅供参考。

**Q4: 数据库迁移后旧数据如何处理？**
A: 建议保留备份文件至少3个月，确认新系统运行正常后再删除。

**Q5: 微信公众号未认证是否可以使用？**
A: 未认证的订阅号功能受限，建议使用已认证的服务号以获得完整API权限。

### B. 参考文档

1. [工信部备案管理系统](https://beian.miit.gov.cn/)
2. [微信公众号开发文档](https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html)
3. [MyBatis Plus官方文档](https://baomidou.com/)
4. [Vue 3官方文档](https://cn.vuejs.org/)
5. [Spring Boot官方文档](https://spring.io/projects/spring-boot)

### C. 版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| v1.0 | 2026-05-09 | 初始版本，完整重构方案 |

---

**文档结束**

如有问题，请联系：contact@example.com