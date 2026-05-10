-- =============================================================
-- z-cosmos 合规版数据库建表 DDL
-- 适用数据库：MySQL 8.0+
-- 设计原则：最小化功能，符合个人站点备案要求
-- 更新日期：2026-05-09
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
-- 3. 笔记类别表
-- ============================================================================
DROP TABLE IF EXISTS z_note_categories;

CREATE TABLE z_note_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    category_code VARCHAR(32) NOT NULL COMMENT '类别编码（唯一标识）',
    category_name VARCHAR(64) NOT NULL COMMENT '类别名称',
    description VARCHAR(256) DEFAULT NULL COMMENT '类别描述',
    icon_url VARCHAR(512) DEFAULT NULL COMMENT '图标URL',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    note_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '笔记数量（冗余字段，便于统计）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_category_code (category_code),
    INDEX idx_sort_order (sort_order),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记类别表';

-- ============================================================================
-- 4. 热点话题表（AI生成内容）
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
-- 5. 搜索索引表（简化版，支持标题/标签/关键词匹配）
-- ============================================================================
DROP TABLE IF EXISTS z_search_index;

CREATE TABLE z_search_index (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型（NOTE/HOT）',
    content_id VARCHAR(32) NOT NULL COMMENT '内容业务ID',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    keywords TEXT DEFAULT NULL COMMENT '关键词（逗号分隔）',
    tags TEXT DEFAULT NULL COMMENT '标签（逗号分隔）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_content (content_type, content_id),
    FULLTEXT INDEX ft_search (title, keywords, tags) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索索引表';

