-- =============================================================
-- z-cosmos 数据库建表 DDL
-- Database: z-community  (charset utf8mb4, collate utf8mb4_unicode_ci)
-- =============================================================

-- ---------- Community · 帖子表 ----------
DROP TABLE IF EXISTS `z_posts`;
CREATE TABLE `z_posts` (
  `id`             BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id`        VARCHAR(32)         NOT NULL COMMENT '业务 ID（雪花）',
  `user_id`        VARCHAR(32)         NOT NULL COMMENT '作者业务 ID',
  `category_code`  VARCHAR(32)         NOT NULL COMMENT '分类代码',
  `title`          VARCHAR(128)        NOT NULL COMMENT '标题',
  `content`        MEDIUMTEXT          NOT NULL COMMENT 'HTML 富文本（已 Jsoup 清洗）',
  `summary`        VARCHAR(256)        DEFAULT NULL COMMENT '摘要（前端展示用）',
  `cover_image`    VARCHAR(512)        DEFAULT NULL COMMENT '封面图',
  `images`         JSON                DEFAULT NULL COMMENT '图片数组',
  `view_count`     BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `like_count`     BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `comment_count`  BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `collect_count`  BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `is_top`         TINYINT             NOT NULL DEFAULT 0 COMMENT '0-否 1-置顶',
  `is_essence`     TINYINT             NOT NULL DEFAULT 0 COMMENT '0-否 1-精华',
  `status`         TINYINT             NOT NULL DEFAULT 1 COMMENT '0-草稿 1-已发布 2-审核中 3-已隐藏',
  `created_at`     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT             NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_id` (`post_id`),
  KEY `idx_user_id`      (`user_id`),
  KEY `idx_category`     (`category_code`, `is_deleted`, `status`),
  KEY `idx_created_at`   (`created_at`),
  KEY `idx_hot`          (`is_deleted`, `status`, `like_count`, `comment_count`),
  FULLTEXT KEY `ft_title_content` (`title`, `summary`) /*!50100 WITH PARSER ngram */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子';


-- ---------- Community · 评论表 ----------
DROP TABLE IF EXISTS `z_comments`;
CREATE TABLE `z_comments` (
  `id`                 BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `comment_id`         VARCHAR(32)      NOT NULL,
  `post_id`            VARCHAR(32)      NOT NULL,
  `user_id`            VARCHAR(32)      NOT NULL,
  `parent_id`          VARCHAR(32)      DEFAULT NULL COMMENT '顶层评论为 NULL，楼中楼指向顶层 commentId',
  `reply_to_user_id`   VARCHAR(32)      DEFAULT NULL COMMENT '被回复者 userId',
  `reply_to_nickname`  VARCHAR(32)      DEFAULT NULL COMMENT '被回复者昵称冗余',
  `content`            TEXT             NOT NULL,
  `like_count`         BIGINT UNSIGNED  NOT NULL DEFAULT 0,
  `status`             TINYINT          NOT NULL DEFAULT 1 COMMENT '0-删除 1-正常 2-审核中',
  `created_at`         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`         TINYINT          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_id` (`comment_id`),
  KEY `idx_post_id`   (`post_id`, `is_deleted`, `status`, `created_at`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_user_id`   (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论/楼中楼';


-- ---------- Community · 点赞表（帖子+评论通用） ----------
DROP TABLE IF EXISTS `z_likes`;
CREATE TABLE `z_likes` (
  `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`      VARCHAR(32)      NOT NULL,
  `target_type`  VARCHAR(16)      NOT NULL COMMENT 'POST / COMMENT',
  `target_id`    VARCHAR(32)      NOT NULL,
  `created_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞关系';


-- ---------- Community · 收藏表 ----------
DROP TABLE IF EXISTS `z_collects`;
CREATE TABLE `z_collects` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`     VARCHAR(32)      NOT NULL,
  `post_id`     VARCHAR(32)      NOT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏';


-- ---------- Community · 分类表 ----------
DROP TABLE IF EXISTS `z_categories`;
CREATE TABLE `z_categories` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_code`  VARCHAR(32)     NOT NULL,
  `name`           VARCHAR(32)     NOT NULL,
  `description`    VARCHAR(128)    DEFAULT NULL,
  `icon`           VARCHAR(64)     DEFAULT NULL COMMENT 'RemixIcon 类名',
  `color`          VARCHAR(16)     DEFAULT NULL COMMENT '主色 HEX',
  `sort`           INT             NOT NULL DEFAULT 0,
  `post_count`     BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区分类';


-- ---------- 初始化分类种子数据 ----------
INSERT INTO `z_categories` (`category_code`, `name`, `description`, `icon`, `color`, `sort`) VALUES
  ('tech',     '技术',   '技术交流、编程分享',       'ri-code-line',      '#7890b5', 1),
  ('life',     '生活',   '日常随笔、生活感悟',       'ri-cup-line',       '#a8bcd4', 2),
  ('film',     '影视',   '影评、剧评、观影记录',     'ri-film-line',      '#c5d5ea', 3),
  ('travel',   '旅行',   '山川湖海、远方故事',       'ri-compass-3-line', '#7890b5', 4),
  ('gaming',   '游戏',   '单机、联机、游戏文化',     'ri-gamepad-line',   '#a8bcd4', 5),
  ('art',      '艺术',   '绘画、音乐、设计',         'ri-palette-line',   '#c5d5ea', 6);


-- =============================================================
-- 预留模块（Hot / Theater）
-- =============================================================

-- ---------- Hot · 新闻表 ----------
DROP TABLE IF EXISTS `z_news`;
CREATE TABLE `z_news` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `news_id`        VARCHAR(32)     NOT NULL,
  `category`       VARCHAR(32)     NOT NULL DEFAULT 'general',
  `title`          VARCHAR(200)    NOT NULL,
  `summary`        VARCHAR(512)    DEFAULT NULL,
  `content`        MEDIUMTEXT      DEFAULT NULL,
  `cover_image`    VARCHAR(512)    DEFAULT NULL,
  `source`         VARCHAR(64)     DEFAULT NULL COMMENT '来源名称',
  `source_url`     VARCHAR(512)    DEFAULT NULL,
  `view_count`     BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `hot_score`      DOUBLE          NOT NULL DEFAULT 0.0 COMMENT '热度分',
  `published_at`   DATETIME        NOT NULL,
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_id` (`news_id`),
  KEY `idx_category_published` (`category`, `published_at`),
  KEY `idx_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻/热点';


-- ---------- Theater · 视频表 ----------
DROP TABLE IF EXISTS `z_videos`;
CREATE TABLE `z_videos` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `video_id`         VARCHAR(32)     NOT NULL,
  `category`         VARCHAR(32)     NOT NULL COMMENT 'movie / drama / anime / variety',
  `title`            VARCHAR(128)    NOT NULL,
  `description`      TEXT            DEFAULT NULL,
  `cover_image`      VARCHAR(512)    DEFAULT NULL,
  `total_episodes`   INT             NOT NULL DEFAULT 1,
  `rating`           DECIMAL(3,1)    NOT NULL DEFAULT 0.0,
  `director`         VARCHAR(128)    DEFAULT NULL,
  `actors`           VARCHAR(512)    DEFAULT NULL,
  `view_count`       BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `status`           TINYINT         NOT NULL DEFAULT 1 COMMENT '0-下架 1-正常 2-即将上线',
  `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_id` (`video_id`),
  KEY `idx_category` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧场视频元信息';


-- ---------- Theater · 剧集表 ----------
DROP TABLE IF EXISTS `z_video_episodes`;
CREATE TABLE `z_video_episodes` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `video_id`       VARCHAR(32)     NOT NULL,
  `episode_number` INT             NOT NULL,
  `title`          VARCHAR(128)    DEFAULT NULL,
  `duration`       INT             DEFAULT NULL COMMENT '秒',
  `play_url`       VARCHAR(1024)   NOT NULL COMMENT '播放地址（第三方源链）',
  `source_type`    VARCHAR(32)     NOT NULL DEFAULT 'MP4' COMMENT 'MP4 / YOUTUBE / BILIBILI / HLS',
  `cover_image`    VARCHAR(512)    DEFAULT NULL,
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_episode` (`video_id`, `episode_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧场剧集';
