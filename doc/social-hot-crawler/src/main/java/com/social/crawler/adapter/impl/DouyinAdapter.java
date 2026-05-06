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
 * 抖音热点适配器
 *
 * 数据来源：抖音热搜榜
 * 支持功能：热点榜单、评论爬取、嵌套回复
 *
 * 注意：实际生产环境中需要处理反爬机制（签名验证、X-Bogus等），
 * 此处提供完整的架构实现和模拟数据用于演示。
 * 接入真实API时只需替换 fetchHotTopics/fetchComments 中的请求逻辑。
 */
@Component
public class DouyinAdapter extends AbstractPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(DouyinAdapter.class);

    @Override
    public String getPlatformId() { return "douyin"; }

    @Override
    public String getPlatformName() { return "抖音"; }

    @Override
    public String getPlatformIcon() { return "\uD83C\uDFB5"; }

    @Override
    public Set<PlatformCapability> getCapabilities() {
        return EnumSet.of(PlatformCapability.HOT_TOPICS, PlatformCapability.COMMENTS, PlatformCapability.NESTED_COMMENTS);
    }

    @Override
    public List<HotTopic> fetchHotTopics(int limit) {
        log.info("[抖音] 开始爬取热点榜单, limit={}", limit);
        List<HotTopic> topics = new ArrayList<>();

        String[][] mockData = {
            {"dy_1", "AI换脸技术引发热议", "最近AI换脸技术在短视频平台爆火，引发了关于隐私和伦理的广泛讨论。专家呼吁加强监管。", "9200000", "爆", "科技前沿", "https://img.example.com/dy1.jpg", "350000", "28000", "15000", "5600000"},
            {"dy_2", "国货美妆品牌崛起", "多个国货美妆品牌在抖音销量暴增，年轻消费者对国产品牌的认可度持续提升。", "7800000", "热", "消费观察", "https://img.example.com/dy2.jpg", "280000", "22000", "12000", "4200000"},
            {"dy_3", "大学生毕业季求职攻略", "2024届毕业生求职季来临，HR分享面试技巧和简历优化建议，播放量破千万。", "6500000", "热", "职场达人", "https://img.example.com/dy3.jpg", "220000", "18000", "9500", "3800000"},
            {"dy_4", "新能源汽车充电桩普及", "全国充电桩建设加速，新能源车主出行更加便利，充电焦虑大幅缓解。", "5200000", "新", "汽车频道", "https://img.example.com/dy4.jpg", "180000", "15000", "8000", "3100000"},
            {"dy_5", "居家健身新方式", "居家健身视频持续走红，15分钟高效燃脂训练受到上班族追捧。", "4800000", "", "健身达人", "https://img.example.com/dy5.jpg", "150000", "12000", "6500", "2800000"},
            {"dy_6", "宠物经济持续升温", "宠物相关消费持续增长，宠物食品、医疗、保险等细分赛道蓬勃发展。", "4200000", "", "萌宠日记", "https://img.example.com/dy6.jpg", "130000", "10000", "5200", "2400000"},
            {"dy_7", "露营装备选购指南", "户外露营热度不减，专业博主分享装备选购心得，从帐篷到炊具一应俱全。", "3800000", "", "户外探险", "https://img.example.com/dy7.jpg", "110000", "8500", "4300", "2100000"},
            {"dy_8", "程序员转行做自媒体", "多位程序员分享转行做自媒体的经历，技术类内容创作成为新趋势。", "3500000", "新", "创作者说", "https://img.example.com/dy8.jpg", "95000", "7200", "3800", "1900000"},
        };

        for (int i = 0; i < mockData.length && topics.size() < limit; i++) {
            String[] d = mockData[i];
            HotTopic topic = new HotTopic();
            topic.setPlatform(getPlatformId());
            topic.setTopicId(d[0]);
            topic.setTitle(d[1]);
            topic.setSummary(d[2]);
            topic.setHeatValue(Long.parseLong(d[3]));
            topic.setHeatLabel(d[4].isEmpty() ? null : d[4]);
            topic.setAuthor(d[5]);
            topic.setCoverUrl(d[6]);
            topic.setCommentCount(Long.parseLong(d[7]));
            topic.setLikeCount(Long.parseLong(d[8]));
            topic.setShareCount(Long.parseLong(d[9]));
            topic.setViewCount(Long.parseLong(d[10]));
            topic.setRank(i + 1);
            topic.setUrl("https://www.douyin.com/hot/" + d[0]);
            topic.setTags("[\"" + (i < 3 ? "热门" : "推荐") + "\"]");
            topic.setPublishedAt(LocalDateTime.now().minusHours(i * 2));
            topic.setCrawledAt(LocalDateTime.now());
            topics.add(topic);
        }

        log.info("[抖音] 爬取完成, 获取 {} 条热点", topics.size());
        return topics;
    }

    @Override
    public List<Comment> fetchComments(String topicId, int limit) {
        log.info("[抖音] 开始爬取评论, topicId={}, limit={}", topicId, limit);
        List<Comment> comments = new ArrayList<>();

        String[][] mockComments = {
            {"dy_c1", "这个技术真的太厉害了，但也要注意保护个人隐私", "科技爱好者小王", "8500", "120"},
            {"dy_c2", "支持国货！现在的国产品牌真的越做越好了", "国货支持者", "6200", "85"},
            {"dy_c3", "作为应届毕业生，这些求职建议太实用了", "求职中的小李", "5800", "72"},
            {"dy_c4", "已经买了新能源车，充电确实比以前方便多了", "新能源车主", "4500", "58"},
            {"dy_c5", "跟着练了一周，效果真的不错，推荐给大家", "健身打卡中", "3800", "45"},
            {"dy_c6", "我家猫看到这个视频都愣住了哈哈哈", "铲屎官", "3200", "95"},
            {"dy_c7", "露营装备确实需要好好选，之前买过一次踩坑了", "户外老手", "2800", "38"},
            {"dy_c8", "程序员做自媒体确实有优势，逻辑清晰表达能力强", "同行", "2500", "42"},
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
            comment.setPublishedAt(LocalDateTime.now().minusMinutes(i * 12));
            comment.setCrawledAt(LocalDateTime.now());
            comments.add(comment);
        }

        // 添加嵌套回复
        if (comments.size() >= 2) {
            Comment reply1 = new Comment();
            reply1.setPlatform(getPlatformId());
            reply1.setCommentId("dy_r1");
            reply1.setParentId(comments.get(0).getId());
            reply1.setReplyToId(comments.get(0).getId());
            reply1.setContent("确实，我也这么觉得！隐私保护很重要");
            reply1.setUsername("路人甲");
            reply1.setLikeCount(200L);
            reply1.setPublishedAt(LocalDateTime.now().minusMinutes(3));
            reply1.setCrawledAt(LocalDateTime.now());
            comments.add(reply1);

            Comment reply2 = new Comment();
            reply2.setPlatform(getPlatformId());
            reply2.setCommentId("dy_r2");
            reply2.setParentId(comments.get(1).getId());
            reply2.setReplyToId(comments.get(1).getId());
            reply2.setContent("同感！国货当自强");
            reply2.setUsername("爱国青年");
            reply2.setLikeCount(150L);
            reply2.setPublishedAt(LocalDateTime.now().minusMinutes(2));
            reply2.setCrawledAt(LocalDateTime.now());
            comments.add(reply2);
        }

        log.info("[抖音] 评论爬取完成, 获取 {} 条评论", comments.size());
        return comments;
    }

    @Override
    public boolean testConnection() {
        return true;
    }
}
