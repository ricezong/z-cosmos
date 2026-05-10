-- =============================================================
-- 笔记类别表初始化数据
-- 更新日期：2026-05-10
-- =============================================================

USE `z-cosmos`;

-- 插入初始笔记类别数据
INSERT INTO z_note_categories (category_code, category_name, description, icon_url, sort_order, is_enabled, note_count) VALUES
('java', 'Java', 'Java相关技术笔记，包括Spring Boot、JVM、并发编程等', NULL, 1, 1, 0),
('frontend', '前端开发', '前端技术笔记，包括Vue、React、TypeScript、CSS等', NULL, 2, 1, 0),
('database', '数据库', '数据库相关技术笔记，包括MySQL、Redis、MongoDB等', NULL, 3, 1, 0),
('devops', 'DevOps', '运维与部署相关笔记，包括Docker、K8s、CI/CD等', NULL, 4, 1, 0),
('algorithm', '算法与数据结构', '算法学习与实践笔记', NULL, 5, 1, 0),
('ai', '人工智能', 'AI/ML相关技术笔记，包括深度学习、自然语言处理等', NULL, 6, 1, 0),
('architecture', '架构设计', '系统架构设计相关笔记', NULL, 7, 1, 0),
('other', '其他', '其他技术类笔记', NULL, 99, 1, 0);

-- 查看插入的数据
SELECT * FROM z_note_categories ORDER BY sort_order;
