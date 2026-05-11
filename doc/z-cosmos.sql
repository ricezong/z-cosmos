CREATE
DATABASE IF NOT EXISTS `z-cosmos` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
`z-cosmos`;

CREATE TABLE z_auth_unlock
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id   VARCHAR(64) NOT NULL COMMENT '设备指纹或SessionID',
    module_type VARCHAR(32) NOT NULL DEFAULT 'NOTE' COMMENT '业务模块标识',
    unlock_code VARCHAR(6)  NOT NULL COMMENT '6位动态口令',
    status      TINYINT     NOT NULL DEFAULT 0 COMMENT '解锁状态:0-未解锁 1-已解锁',
    expires_at  DATETIME    NOT NULL COMMENT '授权过期时间',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_device_module (device_id, module_type),
    INDEX       idx_validate (device_id, module_type, status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局认证解锁表';

CREATE TABLE z_notes
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    note_id       VARCHAR(32)  NOT NULL COMMENT '笔记业务ID(雪花算法)',
    title         VARCHAR(128) NOT NULL COMMENT '笔记标题',
    category_code VARCHAR(32)  NOT NULL COMMENT '分类编码',
    tags          JSON                  DEFAULT NULL COMMENT '标签数组',
    content_type  TINYINT      NOT NULL DEFAULT 0 COMMENT '内容类型:0-原创 1-转载',
    source_url    VARCHAR(512)          DEFAULT NULL COMMENT '转载来源链接',
    view_count    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计阅读数',
    read_minutes  INT UNSIGNED NOT NULL DEFAULT 5 COMMENT '预估阅读耗时(分钟)',
    is_locked     TINYINT      NOT NULL DEFAULT 1 COMMENT '访问控制:0-公开 1-需解锁',
    short_summary VARCHAR(256)          DEFAULT NULL COMMENT 'SEO摘要/列表副标题',
    preview_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '预览策略:1-按字数 2-按分隔符',
    preview_limit INT          NOT NULL DEFAULT 600 COMMENT '预览截取字数阈值',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_note_id (note_id),
    INDEX         idx_category_list (category_code, is_locked, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记元数据表';

CREATE TABLE z_note_contents
(
    note_id    VARCHAR(32) PRIMARY KEY COMMENT '关联z_notes.note_id',
    content    MEDIUMTEXT NOT NULL COMMENT '完整Markdown源码',
    updated_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '内容更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记内容表';

CREATE TABLE z_note_categories
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_code VARCHAR(32) NOT NULL COMMENT '分类编码(唯一)',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    description   VARCHAR(256)         DEFAULT NULL COMMENT '分类描述',
    icon_url      VARCHAR(512)         DEFAULT NULL COMMENT '分类图标URL',
    sort_order    INT         NOT NULL DEFAULT 0 COMMENT '排序权重',
    is_enabled    TINYINT     NOT NULL DEFAULT 1 COMMENT '启用状态:0-禁用 1-启用',
    note_count    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联笔记总数(冗余)',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_category_code (category_code),
    INDEX         idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记分类表';

CREATE TABLE z_hot_topics
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    topic_id     VARCHAR(32)  NOT NULL COMMENT '热点业务ID',
    title        VARCHAR(256) NOT NULL COMMENT 'AI提炼标题',
    summary      TEXT         NOT NULL COMMENT 'AI完整总结',
    source_url   VARCHAR(512)          DEFAULT NULL COMMENT '原始链接',
    source_name  VARCHAR(64)           DEFAULT NULL COMMENT '来源平台',
    publish_time DATETIME              DEFAULT NULL COMMENT '原始发布时间',
    category     VARCHAR(32)           DEFAULT NULL COMMENT '所属分类',
    is_active    TINYINT      NOT NULL DEFAULT 1 COMMENT '展示状态:0-下架 1-展示',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_topic_id (topic_id),
    INDEX        idx_category (category),
    INDEX        idx_publish_time (publish_time),
    INDEX        idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热点话题表';

CREATE TABLE z_search_index
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    content_type VARCHAR(32)  NOT NULL COMMENT '内容类型:NOTE/HOT',
    content_id   VARCHAR(32)  NOT NULL COMMENT '内容业务ID',
    title        VARCHAR(256) NOT NULL COMMENT '标题',
    keywords     TEXT                  DEFAULT NULL COMMENT '关键词(逗号分隔)',
    tags         TEXT                  DEFAULT NULL COMMENT '标签(逗号分隔)',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_content (content_type, content_id),
    FULLTEXT     INDEX ft_search (title, keywords, tags) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索索引表';