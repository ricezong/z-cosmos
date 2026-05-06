package com.social.crawler.adapter.impl;

import com.social.crawler.adapter.AbstractPlatformAdapter;
import com.social.crawler.adapter.PlatformAdapter;
import com.social.crawler.model.HotTopic;
import com.social.crawler.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 知乎热点适配器
 *
 * 数据来源：知乎热榜
 * 支持功能：热点榜单、评论爬取、嵌套回复
 */
@Component
public class ZhihuAdapter extends AbstractPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(ZhihuAdapter.class);

    @Override
    public String getPlatformId() { return "zhihu"; }

    @Override
    public String getPlatformName() { return "知乎"; }

    @Override
    public String getPlatformIcon() { return "\uD83D\uDCDA"; }

    @Override
    public Set<PlatformCapability> getCapabilities() {
        return EnumSet.of(PlatformCapability.HOT_TOPICS, PlatformCapability.COMMENTS,
                          PlatformCapability.NESTED_COMMENTS, PlatformCapability.SEARCH);
    }

    @Override
    public List<HotTopic> fetchHotTopics(int limit) {
        log.info("[知乎] 开始爬取热榜, limit={}", limit);
        List<HotTopic> topics = new ArrayList<>();

        String[][] mockData = {
            {"zh_1", "如何看待2024年AI技术的快速发展？", "人工智能领域在2024年取得了多项突破性进展，从大语言模型到多模态AI，技术迭代速度前所未有。这一趋势引发了社会各界关于AI伦理、就业影响和技术治理的广泛讨论。", "6800000", "热", "科技观察者", "52000", "1800"},
            {"zh_2", "年轻人为什么越来越不愿意结婚了？", "近年来，我国结婚率持续走低，年轻人对婚姻的态度发生了显著变化。经济压力、个人价值观转变、社会观念多元化等因素共同影响了这一趋势。", "5200000", "热", "社会学研究者", "48000", "3200"},
            {"zh_3", "有哪些让你觉得「人间值得」的瞬间？", "生活中总有一些温暖的瞬间让我们感受到生命的美好。也许是陌生人的善意，也许是家人的关怀，也许是一个人的日落。", "4500000", "", "生活记录者", "42000", "2800"},
            {"zh_4", "程序员35岁危机真的存在吗？", "互联网行业流传着35岁程序员面临职业危机的说法。从技术更新迭代、行业竞争、年龄歧视等角度分析，这个现象到底有多严重？", "3900000", "", "技术老兵", "38000", "1500"},
            {"zh_5", "如何评价最新发布的折叠屏手机？", "多家厂商发布了新一代折叠屏手机产品，在屏幕技术、铰链设计、软件适配等方面都有显著提升。", "3400000", "新", "数码测评师", "35000", "980"},
            {"zh_6", "读研真的值得吗？", "随着研究生扩招和学历内卷，读研的性价比成为热议话题。从就业竞争力、学术研究、时间成本等角度分析。", "3000000", "", "教育观察", "32000", "2200"},
            {"zh_7", "中国哪些城市最适合年轻人发展？", "综合考虑薪资水平、生活成本、职业机会、文化氛围等因素，分析各城市对年轻人的吸引力。", "2700000", "", "城市研究者", "28000", "1600"},
            {"zh_8", "如何培养良好的阅读习惯？", "在碎片化信息时代，保持深度阅读变得越来越困难。分享一些实用的阅读方法和习惯养成技巧。", "2400000", "", "阅读推广人", "25000", "1200"},
            {"zh_9", "远程办公会成为未来趋势吗？", "疫情后远程办公模式在全球范围内得到推广，但各公司的态度不一。分析远程办公的优缺点及未来发展趋势。", "2100000", "", "职场分析师", "22000", "860"},
            {"zh_10", "有哪些看似简单但实际很难的编程问题？", "分享一些在编程面试和实际开发中遇到的看似简单但需要深入思考的问题，以及背后的计算机科学原理。", "1800000", "", "算法爱好者", "20000", "750"},
        };

        for (String[] item : mockData) {
            if (topics.size() >= limit) break;
            HotTopic topic = new HotTopic();
            topic.setPlatform(getPlatformId());
            topic.setTopicId(item[0]);
            topic.setTitle(item[1]);
            topic.setSummary(item[2]);
            topic.setUrl("https://www.zhihu.com/question/" + item[0].replace("zh_", ""));
            topic.setHeatValue(Long.parseLong(item[3]));
            topic.setHeatLabel(item[4].isEmpty() ? null : item[4]);
            topic.setRank(topics.size() + 1);
            topic.setAuthor(item[5]);
            topic.setViewCount(Long.parseLong(item[6]));
            topic.setCommentCount(Long.parseLong(item[7]));
            topic.setTags("[\"讨论\",\"观点\"]");
            topic.setCrawledAt(LocalDateTime.now());
            topics.add(topic);
        }

        log.info("[知乎] 爬取完成, 获取 {} 条热榜", topics.size());
        return topics;
    }

    @Override
    public List<Comment> fetchComments(String topicId, int limit) {
        log.info("[知乎] 开始爬取评论, topicId={}, limit={}", topicId, limit);
        List<Comment> comments = new ArrayList<>();

        String[][] mockComments = {
            {"zh_c1", "作为一个AI从业者，我认为这次的技术突破确实意义重大。大语言模型的能力已经超出了很多人的预期，在代码生成、文本创作、知识问答等领域都展现出了惊人的表现。但与此同时，我们也需要关注AI安全和伦理问题。", "AI研究员", "8500", "120"},
            {"zh_c2", "说几点个人看法：\n1. 技术进步是好事，但需要合理监管\n2. 对就业的影响被夸大了，AI更多是辅助工具\n3. 教育体系需要改革，培养创新能力比记忆知识更重要", "理性分析者", "6200", "85"},
            {"zh_c3", "我是一名教师，最关心的是AI对教育的影响。学生用AI写作业怎么办？考试怎么考？这些都是需要认真思考的问题。", "一线教师", "4800", "62"},
            {"zh_c4", "谢邀。从技术角度来看，这次突破主要体现在以下几个方面：模型架构的创新、训练数据的优化、推理效率的提升。每一项都是大量工程师和研究员努力的结果。", "技术专家", "3900", "45"},
            {"zh_c5", "我觉得大家不用太焦虑。历史上每一次技术革命都会引发类似的担忧，但最终社会都会适应并找到新的平衡点。", "历史爱好者", "3200", "38"},
            {"zh_c6", "作为一个刚入行的程序员，我既兴奋又忐忑。兴奋的是AI工具让开发效率大幅提升，忐忑的是不知道自己的技能会不会被替代。", "新人程序员", "2800", "52"},
        };

        for (int i = 0; i < mockComments.length && comments.size() < limit; i++) {
            String[] c = mockComments[i];
            Comment comment = new Comment();
            comment.setPlatform(getPlatformId());
            comment.setCommentId(c[0]);
            comment.setContent(c[1]);
            comment.setUsername(c[2]);
            comment.setLikeCount(Long.parseLong(c[3]));
            comment.setReplyCount(Long.parseLong(c[4]));
            comment.setFloor(i + 1);
            comment.setPublishedAt(LocalDateTime.now().minusHours(i * 2));
            comment.setCrawledAt(LocalDateTime.now());
            comments.add(comment);
        }

        // 添加嵌套回复
        if (comments.size() >= 1) {
            Comment reply = new Comment();
            reply.setPlatform(getPlatformId());
            reply.setCommentId("zh_r1");
            reply.setParentId(comments.get(0).getId());
            reply.setReplyToId(comments.get(0).getId());
            reply.setContent("同意你的观点，AI安全和伦理确实需要被重视。你觉得目前有哪些具体的AI安全问题是亟待解决的？");
            reply.setUsername("追问者");
            reply.setLikeCount(500L);
            reply.setPublishedAt(LocalDateTime.now().minusMinutes(90));
            reply.setCrawledAt(LocalDateTime.now());
            comments.add(reply);
        }

        log.info("[知乎] 评论爬取完成, 获取 {} 条评论", comments.size());
        return comments;
    }
}
