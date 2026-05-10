-- =============================================================
-- Z-Cosmos 测试数据初始化脚本
-- 更新日期：2026-05-10
-- =============================================================

USE `z-cosmos`;

-- ============================================================================
-- 1. 笔记类别表测试数据
-- ============================================================================
INSERT INTO z_note_categories (category_code, category_name, description, sort_order, is_enabled, note_count) VALUES
('java', 'Java', 'Java相关技术笔记，包括Spring Boot、JVM、并发编程等', 1, 1, 0),
('frontend', '前端开发', '前端技术笔记，包括Vue、React、TypeScript等', 2, 1, 0),
('database', '数据库', '数据库相关技术笔记，包括MySQL、Redis、MongoDB等', 3, 1, 0),
('devops', 'DevOps', '运维与部署相关笔记，包括Docker、K8s、CI/CD等', 4, 1, 0),
('algorithm', '算法', '算法学习与实践笔记', 5, 1, 0);

-- ============================================================================
-- 2. 技术笔记表测试数据
-- ============================================================================
INSERT INTO z_notes (note_id, title, content, preview_ratio, short_summary, cover_image, category, tags, view_count, is_locked) VALUES
-- Java 相关笔记
('NOTE001', 
 'Spring Boot 3.0 核心特性解析', 
 '<h2>Spring Boot 3.0 核心特性</h2><p>Spring Boot 3.0 是一个重大版本更新，带来了许多令人兴奋的新特性。</p><h3>1. 基于 Java 17</h3><p>Spring Boot 3.0 要求最低 Java 17 版本，充分利用了 Java 17 的新特性，包括记录类、密封类、模式匹配等。</p><h3>2. 原生支持 GraalVM</h3><p>这是 Spring Boot 3.0 最重要的特性之一。通过 GraalVM 原生镜像，可以实现：</p><ul><li>启动时间缩短至毫秒级</li><li>内存占用降低 10 倍</li><li>更快的峰值性能</li></ul><h3>3. HTTP 接口改进</h3><p>引入了新的 HTTP 接口注解，使得 REST API 开发更加简洁：</p><pre><code>@RestController<br/>@RequestMapping(&quot;/api/users&quot;)<br/>public class UserController {<br/>    @GetMapping<br/>    public List&lt;User&gt; list() {<br/>        return userService.findAll();<br/>    }<br/>}</code></pre><h3>4. 问题追踪增强</h3><p>新增了 ProblemDetail 支持，符合 RFC 7807 规范，统一了错误响应格式。</p><h3>5. 观测性支持</h3><p>内置 Micrometer Tracing，替代了 Spring Cloud Sleuth，提供统一的观测性解决方案。</p>',
 0.30, 
 'Spring Boot 3.0 核心特性全面解析，包括 Java 17 基础、GraalVM 原生支持、HTTP 接口改进等',
 NULL,
 'java',
 '["Spring Boot", "Java 17", "GraalVM", "微服务"]',
 1250, 1),

('NOTE002', 
 'JVM 性能调优实战指南', 
 '<h2>JVM 性能调优实战指南</h2><p>JVM 性能调优是 Java 开发者必须掌握的技能。本文将从实战角度介绍 JVM 调优的核心方法。</p><h3>一、JVM 内存结构</h3><p>JVM 内存分为以下几个区域：</p><ul><li><strong>堆内存（Heap）</strong>：存放对象实例，分为新生代和老年代</li><li><strong>方法区（Metaspace）</strong>：存储类信息、常量、静态变量</li><li><strong>虚拟机栈</strong>：存储局部变量表、操作数栈等</li><li><strong>本地方法栈</strong>：为 Native 方法服务</li></ul><h3>二、垃圾回收器选择</h3><p>常用的垃圾回收器有：</p><ol><li><strong>G1 GC</strong>：适合大内存应用，可预测停顿时间</li><li><strong>ZGC</strong>：超低停顿，适合低延迟场景</li><li><strong>Shenandoah</strong>：与 ZGC 类似的低延迟回收器</li></ol><h3>三、常用 JVM 参数</h3><pre><code># 设置堆内存<br/>-Xms2g -Xmx2g<br/><br/># 使用 G1 回收器<br/>-XX:+UseG1GC<br/><br/># 设置最大停顿时间<br/>-XX:MaxGCPauseMillis=200<br/><br/># 开启 GC 日志<br/>-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10M</code></pre><h3>四、性能监控工具</h3><p>推荐使用以下工具进行性能监控：</p><ul><li>jvisualvm：可视化监控工具</li><li>Arthas：阿里开源的 Java 诊断工具</li><li>Prometheus + Grafana：生产环境监控方案</li></ul>',
 0.30,
 'JVM 性能调优实战指南，涵盖内存结构、垃圾回收器选择、常用参数和监控工具',
 NULL,
 'java',
 '["JVM", "性能调优", "GC", "G1"]',
 890, 1),

-- 前端开发笔记
('NOTE003', 
 'Vue 3 Composition API 最佳实践', 
 '<h2>Vue 3 Composition API 最佳实践</h2><p>Composition API 是 Vue 3 最重要的特性之一，它提供了更灵活的代码组织方式。</p><h3>为什么使用 Composition API？</h3><ul><li>更好的代码组织：按功能逻辑组织代码，而非选项</li><li>更好的类型推导：对 TypeScript 支持更友好</li><li>更好的代码复用：通过 composables 实现逻辑复用</li></ul><h3>核心 API 介绍</h3><h4>1. ref 和 reactive</h4><pre><code>import { ref, reactive } from &apos;vue&apos;<br/><br/>// ref 用于基本类型<br/>const count = ref(0)<br/><br/>// reactive 用于对象<br/>const state = reactive({<br/>  name: &apos;Vue 3&apos;,<br/>  version: &apos;3.0&apos;<br/>})</code></pre><h4>2. computed 和 watch</h4><pre><code>import { computed, watch } from &apos;vue&apos;<br/><br/>const doubleCount = computed(() =&gt; count.value * 2)<br/><br/>watch(count, (newVal, oldVal) =&gt; {<br/>  console.log(`count 从 ${oldVal} 变为 ${newVal}`)<br/>})</code></pre><h3>Composables 示例</h3><pre><code>// useCounter.js<br/>export function useCounter() {<br/>  const count = ref(0)<br/>  const increment = () =&gt; count.value++<br/>  const decrement = () =&gt; count.value--<br/>  <br/>  return { count, increment, decrement }<br/>}</code></pre><h3>最佳实践建议</h3><ol><li>优先使用 Composition API</li><li>将可复用逻辑提取为 composables</li><li>合理使用 TypeScript 获得更好的类型支持</li><li>避免在 setup 中执行副作用操作</li></ol>',
 0.30,
 'Vue 3 Composition API 最佳实践，包括核心 API 使用和 composables 封装技巧',
 NULL,
 'frontend',
 '["Vue 3", "Composition API", "TypeScript", "前端框架"]',
 1560, 0),

('NOTE004', 
 'TypeScript 高级类型编程技巧', 
 '<h2>TypeScript 高级类型编程技巧</h2><p>TypeScript 的类型系统非常强大，掌握高级类型编程可以大幅提升代码质量。</p><h3>1. 条件类型</h3><pre><code>type IsString&lt;T&gt; = T extends string ? &apos;yes&apos; : &apos;no&apos;<br/><br/>type A = IsString&lt;string&gt;  // &apos;yes&apos;<br/>type B = IsString&lt;number&gt;  // &apos;no&apos;</code></pre><h3>2. 映射类型</h3><pre><code>type Readonly&lt;T&gt; = {<br/>  readonly [P in keyof T]: T[P]<br/>}<br/><br/>type Optional&lt;T&gt; = {<br/>  [P in keyof T]?: T[P]<br/>}</code></pre><h3>3. 模板字面量类型</h3><pre><code>type EventName = &apos;click&apos; | &apos;hover&apos; | &apos;focus&apos;<br/>type EventHandler = `on${Capitalize&lt;EventName&gt;}`<br/>// &quot;onClick&quot; | &quot;onHover&quot; | &quot;onFocus&quot;</code></pre><h3>4. 工具类型实战</h3><pre><code>// 提取函数的返回类型<br/>type ReturnType&lt;T extends (...args: any) =&gt; any&gt; = <br/>  T extends (...args: any) =&gt; infer R ? R : any<br/><br/>// 深度只读<br/>type DeepReadonly&lt;T&gt; = {<br/>  readonly [P in keyof T]: T[P] extends object ? DeepReadonly&lt;T[P]&gt; : T[P]<br/>}</code></pre>',
 0.30,
 'TypeScript 高级类型编程技巧，包括条件类型、映射类型和工具类型实战',
 NULL,
 'frontend',
 '["TypeScript", "类型系统", "高级类型", "编程技巧"]',
 720, 1),

-- 数据库笔记
('NOTE005', 
 'MySQL 索引优化完全指南', 
 '<h2>MySQL 索引优化完全指南</h2><p>索引是数据库性能优化的核心。本文将深入讲解 MySQL 索引的原理和优化策略。</p><h3>一、索引数据结构</h3><p>MySQL 主要使用 B+ 树作为索引的数据结构：</p><ul><li>所有数据都存储在叶子节点</li><li>叶子节点之间有双向指针</li><li>树的高度通常为 2-4 层</li></ul><h3>二、索引类型</h3><ol><li><strong>主键索引</strong>：聚集索引，叶子节点存储完整数据</li><li><strong>普通索引</strong>：二级索引，叶子节点存储主键值</li><li><strong>唯一索引</strong>：索引列值必须唯一</li><li><strong>联合索引</strong>：多个列组成的索引</li><li><strong>全文索引</strong>：用于全文搜索</li></ol><h3>三、最左前缀原则</h3><p>联合索引遵循最左前缀原则：</p><pre><code>-- 创建联合索引<br/>CREATE INDEX idx_name_age ON users(name, age, city)<br/><br/>-- 可以使用索引的查询<br/>SELECT * FROM users WHERE name = &apos;张三&apos;<br/>SELECT * FROM users WHERE name = &apos;张三&apos; AND age = 25<br/><br/>-- 无法使用索引的查询<br/>SELECT * FROM users WHERE age = 25</code></pre><h3>四、索引优化建议</h3><ul><li>为高频查询字段创建索引</li><li>避免在低区分度字段建索引</li><li>定期分析和优化慢查询</li><li>使用 EXPLAIN 分析执行计划</li></ul>',
 0.30,
 'MySQL 索引优化完全指南，涵盖索引原理、类型、最左前缀原则和优化策略',
 NULL,
 'database',
 '["MySQL", "索引优化", "性能优化", "B+树"]',
 1100, 1),

('NOTE006', 
 'Redis 缓存最佳实践与陷阱', 
 '<h2>Redis 缓存最佳实践与陷阱</h2><p>Redis 是最常用的缓存解决方案，但使用不当会带来很多问题。</p><h3>一、缓存穿透</h3><p><strong>问题</strong>：查询不存在的数据，缓存不生效，请求直达数据库。</p><p><strong>解决方案</strong>：</p><ul><li>缓存空值（设置较短过期时间）</li><li>使用布隆过滤器</li></ul><h3>二、缓存击穿</h3><p><strong>问题</strong>：热点 key 过期瞬间，大量请求打到数据库。</p><p><strong>解决方案</strong>：</p><ul><li>设置热点数据永不过期</li><li>使用互斥锁（SETNX）</li><li>逻辑过期方案</li></ul><h3>三、缓存雪崩</h3><p><strong>问题</strong>：大量 key 同时过期，数据库压力激增。</p><p><strong>解决方案</strong>：</p><ul><li>过期时间加随机值</li><li>集群部署，避免单点故障</li><li>服务降级和熔断</li></ul><h3>四、数据结构选择</h3><pre><code># String：简单键值对<br/>SET user:1001 &quot;{&quot;name&quot;:&quot;张三&quot;}&quot;<br/><br/># Hash：对象存储<br/>HSET user:1001 name &quot;张三&quot; age 25<br/><br/># ZSet：排行榜<br/>ZADD leaderboard 100 &quot;user1&quot;<br/><br/># Set：标签系统<br/>SADD article:1:tags &quot;Java&quot; &quot;Spring&quot;</code></pre>',
 0.30,
 'Redis 缓存最佳实践，包括缓存穿透、击穿、雪崩的解决方案和数据结构选择',
 NULL,
 'database',
 '["Redis", "缓存", "性能优化", "最佳实践"]',
 950, 0);

-- ============================================================================
-- 3. 热点话题表测试数据
-- ============================================================================
INSERT INTO z_hot_topics (topic_id, title, summary, source_url, source_name, publish_time, category, is_active) VALUES
('HOT001',
 'AI 大模型迎来重大突破，多模态能力大幅提升',
 '最新研究表明，新一代 AI 大模型在多模态理解方面取得重大突破。通过融合视觉、语言、音频等多种模态，模型能够更准确地理解复杂场景。这项技术将广泛应用于智能助手、内容创作、医疗诊断等领域。专家预测，未来 1-2 年内，多模态 AI 将成为主流应用场景的核心技术。',
 'https://example.com/ai-breakthrough',
 'AI科技前沿',
 '2026-05-10 10:30:00',
 'technology',
 1),

('HOT002',
 'Spring Boot 4.0 路线图公布，将全面拥抱云原生',
 'Spring 团队正式公布了 Spring Boot 4.0 的发展路线图。新版本将全面拥抱云原生架构，包括更好的 Kubernetes 集成、Serverless 支持、服务网格适配等。此外，还将引入更智能的自动配置机制和更强大的观测性工具。预计 2026 年底发布首个预览版本。',
 'https://example.com/spring-boot-4',
 'Spring 官方博客',
 '2026-05-09 15:20:00',
 'technology',
 1),

('HOT003',
 '2026 年开发者技术栈趋势报告发布',
 '最新调查显示，2026 年最受开发者欢迎的技术栈包括：TypeScript 占比 78%、React 占比 65%、Vue 占比 45%、Rust 连续三年成为最受期待的语言。云原生技术普及率达到 82%，AI 辅助编程工具使用率超过 60%。报告还指出，全栈开发者需求量持续增长，平均薪资同比上涨 15%。',
 'https://example.com/dev-trends-2026',
 '开发者生态报告',
 '2026-05-08 09:00:00',
 'industry',
 1),

('HOT004',
 'MySQL 9.0 正式发布，性能提升 40%',
 'Oracle 正式发布 MySQL 9.0 版本，带来重大性能改进。新版本的查询优化器采用机器学习算法，能够自动优化查询计划。InnoDB 存储引擎经过深度优化，读写性能提升 40%。此外，还新增了原生 JSON 函数增强、更好的窗口函数支持等特性。建议生产环境在充分测试后进行升级。',
 'https://example.com/mysql-9',
 '数据库技术周刊',
 '2026-05-07 14:45:00',
 'technology',
 1),

('HOT005',
 'Vue 4.0 RFC 提案引发社区热议',
 'Vue 核心团队发布了 Vue 4.0 的首个 RFC 提案，主要改进包括：更轻量的运行时（减少 30% 体积）、改进的响应式系统、更好的 SSR 性能、原生 Web Components 支持等。社区对此反响热烈，同时也对部分 breaking changes 表示担忧。尤雨溪表示会认真考虑社区反馈，计划 2027 年初发布正式版。',
 'https://example.com/vue-4-rfc',
 '前端技术观察',
 '2026-05-06 11:30:00',
 'technology',
 1);

-- ============================================================================
-- 4. 搜索索引表测试数据
-- ============================================================================
INSERT INTO z_search_index (content_type, content_id, title, keywords, tags) VALUES
-- 笔记搜索索引
('NOTE', 'NOTE001', 'Spring Boot 3.0 核心特性解析', 'Spring Boot,Java 17,GraalVM,微服务,原生镜像', 'Java,Spring Boot,后端框架'),
('NOTE', 'NOTE002', 'JVM 性能调优实战指南', 'JVM,性能调优,垃圾回收,G1,ZGC,内存管理', 'Java,JVM,性能优化'),
('NOTE', 'NOTE003', 'Vue 3 Composition API 最佳实践', 'Vue 3,Composition API,TypeScript,前端框架,响应式', '前端,Vue,JavaScript'),
('NOTE', 'NOTE004', 'TypeScript 高级类型编程技巧', 'TypeScript,类型系统,条件类型,映射类型,工具类型', '前端,TypeScript,编程技巧'),
('NOTE', 'NOTE005', 'MySQL 索引优化完全指南', 'MySQL,索引优化,B+树,查询优化,性能优化', '数据库,MySQL,性能优化'),
('NOTE', 'NOTE006', 'Redis 缓存最佳实践与陷阱', 'Redis,缓存,缓存穿透,缓存击穿,缓存雪崩,数据结构', '数据库,Redis,缓存'),

-- 热点话题搜索索引
('HOT', 'HOT001', 'AI 大模型迎来重大突破，多模态能力大幅提升', 'AI,大模型,多模态,机器学习,深度学习', '人工智能,科技前沿'),
('HOT', 'HOT002', 'Spring Boot 4.0 路线图公布，将全面拥抱云原生', 'Spring Boot,云原生,Kubernetes,Serverless,微服务', 'Java,云原生,后端'),
('HOT', 'HOT003', '2026 年开发者技术栈趋势报告发布', '技术栈,开发者趋势,TypeScript,Rust,全栈开发', '行业报告,开发者生态'),
('HOT', 'HOT004', 'MySQL 9.0 正式发布，性能提升 40%', 'MySQL,数据库,性能优化,InnoDB,查询优化', '数据库,后端'),
('HOT', 'HOT005', 'Vue 4.0 RFC 提案引发社区热议', 'Vue,前端框架,RFC,Web Components,SSR', '前端,JavaScript');

-- ============================================================================
-- 5. 更新笔记类别的笔记数量统计
-- ============================================================================
UPDATE z_note_categories SET note_count = 2 WHERE category_code = 'java';
UPDATE z_note_categories SET note_count = 2 WHERE category_code = 'frontend';
UPDATE z_note_categories SET note_count = 2 WHERE category_code = 'database';

-- ============================================================================
-- 验证数据
-- ============================================================================
SELECT '========== 笔记类别 ==========' AS info;
SELECT * FROM z_note_categories ORDER BY sort_order;

SELECT '========== 技术笔记 ==========' AS info;
SELECT note_id, title, category, view_count, is_locked FROM z_notes ORDER BY created_at;

SELECT '========== 热点话题 ==========' AS info;
SELECT topic_id, title, source_name, publish_time FROM z_hot_topics ORDER BY publish_time DESC;

SELECT '========== 搜索索引 ==========' AS info;
SELECT content_type, content_id, title FROM z_search_index;
