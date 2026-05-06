-- =============================================================
-- z-cosmos 数据库建表 DDL
-- 适用数据库：MySQL 8.0+
-- =============================================================

-- ============================================================================
-- 建库
-- ============================================================================
CREATE DATABASE IF NOT EXISTS `z-cosmos` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `z-cosmos`;


-- ----------------------------------------
-- 1.1 用户表 (z_users)
-- 存储用户核心资料、社交统计、状态控制
-- ----------------------------------------
DROP TABLE IF EXISTS z_users;

CREATE TABLE z_users
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    user_id         VARCHAR(64)  NOT NULL COMMENT '用户唯一业务ID',

    -- 登录凭证
    phone           VARCHAR(20)           DEFAULT NULL COMMENT '手机号(账密登录唯一标识)',
    password_hash   VARCHAR(255)          DEFAULT NULL COMMENT '密码哈希(BCrypt,自带盐值)',
    last_login_time TIMESTAMP             DEFAULT NULL COMMENT '最后登录时间',

    -- 第三方登录标识
    wechat_openid   VARCHAR(128)          DEFAULT NULL COMMENT '微信OpenID',
    wechat_unionid  VARCHAR(128)          DEFAULT NULL COMMENT '微信UnionID',
    github_openid   VARCHAR(128)          DEFAULT NULL COMMENT 'GitHub OpenID',
    gitee_openid    VARCHAR(128)          DEFAULT NULL COMMENT 'Gitee OpenID',
    qq_openid       VARCHAR(128)          DEFAULT NULL COMMENT 'QQ OpenID',

    -- 基础资料
    nickname        VARCHAR(128) NOT NULL COMMENT '昵称',
    avatar_url      VARCHAR(512)          DEFAULT NULL COMMENT '头像URL',
    gender          SMALLINT              DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    bio             VARCHAR(500)          DEFAULT NULL COMMENT '个人简介/签名',
    location        VARCHAR(100)          DEFAULT NULL COMMENT '所在地区',
    birthday        DATE                  DEFAULT NULL COMMENT '生日',

    -- 身份认证
    user_type       SMALLINT              DEFAULT 1 COMMENT '类型: 1-普通, 2-创作者, 3-品牌, 4-官方',
    user_level      SMALLINT              DEFAULT 1 COMMENT '等级: 1-10级',
    verified_status SMALLINT              DEFAULT 0 COMMENT '认证: 0-未认证, 1-已认证',
    verified_reason VARCHAR(200)          DEFAULT NULL COMMENT '认证说明',

    -- 社交统计 (计数器, 可异步更新)
    follower_count  BIGINT                DEFAULT 0 COMMENT '粉丝数',
    following_count BIGINT                DEFAULT 0 COMMENT '关注数',
    post_count      BIGINT                DEFAULT 0 COMMENT '发帖数',
    like_count      BIGINT                DEFAULT 0 COMMENT '获赞数',
    collect_count   BIGINT                DEFAULT 0 COMMENT '被收藏数',

    -- 状态风控
    user_status     SMALLINT              DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常, 2-封禁',
    credit_score    INT                   DEFAULT 100 COMMENT '信用分: 0-100',
    ban_reason      TEXT                  DEFAULT NULL COMMENT '封禁原因',
    ban_expire_time TIMESTAMP             DEFAULT NULL COMMENT '封禁到期时间',

    -- 逻辑删除
    is_deleted      SMALLINT              DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删',

    -- 审计字段
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表 - 存储用户核心资料与社交统计';

-- 用户表索引
CREATE UNIQUE INDEX uk_z_users_user_id ON z_users (user_id);
CREATE UNIQUE INDEX uk_z_users_phone ON z_users (phone);
CREATE UNIQUE INDEX uk_z_users_wechat ON z_users (wechat_openid);
CREATE UNIQUE INDEX uk_z_users_github ON z_users (github_openid);
CREATE UNIQUE INDEX uk_z_users_gitee ON z_users (gitee_openid);
CREATE UNIQUE INDEX uk_z_users_qq ON z_users (qq_openid);
CREATE INDEX idx_z_users_status ON z_users (user_status);
CREATE INDEX idx_z_users_credit ON z_users (credit_score);
CREATE INDEX idx_z_users_level ON z_users (user_level, verified_status);


-- ---------- Community · 帖子表 ----------
DROP TABLE IF EXISTS `z_posts`;
CREATE TABLE `z_posts`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`       VARCHAR(32)  NOT NULL COMMENT '业务 ID（雪花）',
    `user_id`       VARCHAR(32)  NOT NULL COMMENT '作者业务 ID',
    `category_code` VARCHAR(32)  NOT NULL COMMENT '分类代码',
    `title`         VARCHAR(128) NOT NULL COMMENT '标题',
    `content`       MEDIUMTEXT   NOT NULL COMMENT 'HTML 富文本（已 Jsoup 清洗）',
    `summary`       VARCHAR(256)          DEFAULT NULL COMMENT '摘要（前端展示用）',
    `cover_image`   VARCHAR(512)          DEFAULT NULL COMMENT '封面图',
    `images`        JSON                  DEFAULT NULL COMMENT '图片数组',
    `view_count`    BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `like_count`    BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `comment_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `collect_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `is_top`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0-否 1-置顶',
    `is_essence`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0-否 1-精华',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0-草稿 1-已发布 2-审核中 3-已隐藏',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_id` (`post_id`),
    KEY             `idx_user_id` (`user_id`),
    KEY             `idx_category` (`category_code`, `is_deleted`, `status`),
    KEY             `idx_created_at` (`created_at`),
    KEY             `idx_hot` (`is_deleted`, `status`, `like_count`, `comment_count`),
    FULLTEXT KEY `ft_title_content` (`title`, `summary`) /*!50100 WITH PARSER ngram */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子';


-- ---------- Community · 评论表 ----------
DROP TABLE IF EXISTS `z_comments`;
CREATE TABLE `z_comments`
(
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `comment_id`        VARCHAR(32) NOT NULL,
    `post_id`           VARCHAR(32) NOT NULL,
    `user_id`           VARCHAR(32) NOT NULL,
    `parent_id`         VARCHAR(32)          DEFAULT NULL COMMENT '顶层评论为 NULL，楼中楼指向顶层 commentId',
    `reply_to_user_id`  VARCHAR(32)          DEFAULT NULL COMMENT '被回复者 userId',
    `reply_to_nickname` VARCHAR(32)          DEFAULT NULL COMMENT '被回复者昵称冗余',
    `content`           TEXT        NOT NULL,
    `like_count`        BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `status`            TINYINT     NOT NULL DEFAULT 1 COMMENT '0-删除 1-正常 2-审核中',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`        TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_id` (`comment_id`),
    KEY                 `idx_post_id` (`post_id`, `is_deleted`, `status`, `created_at`),
    KEY                 `idx_parent_id` (`parent_id`),
    KEY                 `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论/楼中楼';


-- ---------- Community · 点赞表（帖子+评论通用） ----------
DROP TABLE IF EXISTS `z_likes`;
CREATE TABLE `z_likes`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`     VARCHAR(32) NOT NULL,
    `target_type` VARCHAR(16) NOT NULL COMMENT 'POST / COMMENT',
    `target_id`   VARCHAR(32) NOT NULL,
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY           `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞关系';


-- ---------- Community · 收藏表 ----------
DROP TABLE IF EXISTS `z_collects`;
CREATE TABLE `z_collects`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`    VARCHAR(32) NOT NULL,
    `post_id`    VARCHAR(32) NOT NULL,
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY          `idx_user_created` (`user_id`, `created_at`),
    KEY          `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏';


-- ---------- Community · 分类表 ----------
DROP TABLE IF EXISTS `z_categories`;
CREATE TABLE `z_categories`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `category_code` VARCHAR(32) NOT NULL,
    `name`          VARCHAR(32) NOT NULL,
    `description`   VARCHAR(128)         DEFAULT NULL,
    `icon`          VARCHAR(64)          DEFAULT NULL COMMENT 'RemixIcon 类名',
    `color`         VARCHAR(16)          DEFAULT NULL COMMENT '主色 HEX',
    `sort`          INT         NOT NULL DEFAULT 0,
    `post_count`    BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区分类';


-- ---------- 初始化分类种子数据 ----------
INSERT INTO `z_categories` (`category_code`, `name`, `description`, `icon`, `color`, `sort`)
VALUES ('tech', '技术', '技术交流、编程分享', 'ri-code-line', '#7890b5', 1),
       ('life', '生活', '日常随笔、生活感悟', 'ri-cup-line', '#a8bcd4', 2),
       ('film', '影视', '影评、剧评、观影记录', 'ri-film-line', '#c5d5ea', 3),
       ('travel', '旅行', '山川湖海、远方故事', 'ri-compass-3-line', '#7890b5', 4),
       ('gaming', '游戏', '单机、联机、游戏文化', 'ri-gamepad-line', '#a8bcd4', 5),
       ('art', '艺术', '绘画、音乐、设计', 'ri-palette-line', '#c5d5ea', 6);


-- =============================================================
-- 预留模块（Hot / Theater）
-- =============================================================

-- ---------- Hot · 新闻表 ----------
DROP TABLE IF EXISTS `z_news`;
CREATE TABLE `z_news`
(
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `news_id`      VARCHAR(32)  NOT NULL,
    `category`     VARCHAR(32)  NOT NULL DEFAULT 'general',
    `title`        VARCHAR(200) NOT NULL,
    `summary`      VARCHAR(512)          DEFAULT NULL,
    `content`      MEDIUMTEXT            DEFAULT NULL,
    `cover_image`  VARCHAR(512)          DEFAULT NULL,
    `source`       VARCHAR(64)           DEFAULT NULL COMMENT '来源名称',
    `source_url`   VARCHAR(512)          DEFAULT NULL,
    `view_count`   BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `hot_score`    DOUBLE       NOT NULL DEFAULT 0.0 COMMENT '热度分',
    `published_at` DATETIME     NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_news_id` (`news_id`),
    KEY            `idx_category_published` (`category`, `published_at`),
    KEY            `idx_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻/热点';

