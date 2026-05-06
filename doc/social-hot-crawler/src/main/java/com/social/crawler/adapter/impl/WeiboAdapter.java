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
 * 微博热点适配器
 *
 * 数据来源：微博热搜榜
 * 支持功能：热点榜单、评论爬取
 */
@Component
public class WeiboAdapter extends AbstractPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(WeiboAdapter.class);

    @Override
    public String getPlatformId() { return "weibo"; }

    @Override
    public String getPlatformName() { return "微博"; }

    @Override
    public String getPlatformIcon() { return "\uD83D\uDCAC"; }

    @Override
    public Set<PlatformCapability> getCapabilities() {
        return EnumSet.of(PlatformCapability.HOT_TOPICS, PlatformCapability.COMMENTS);
    }

    @Override
    public List<HotTopic> fetchHotTopics(int limit) {
        log.info("[微博] 开始爬取热搜榜, limit={}", limit);
        List<HotTopic> topics = new ArrayList<>();

        String[][] mockData = {
            {"wb_1", "神舟十八号返回地球", "神舟十八号载人飞船返回舱成功着陆，三名航天员安全返回。", "9500000", "爆", "央视新闻", "180000", "52000"},
            {"wb_2", "高考倒计时30天", "2024年全国高考进入最后30天冲刺阶段，各地考生积极备考。", "7200000", "热", "人民日报", "120000", "38000"},
            {"wb_3", "某明星官宣结婚", "娱乐圈重磅消息，知名演员今日官宣婚讯，引发网友热议。", "6800000", "沸", "娱乐头条", "95000", "85000"},
            {"wb_4", "全国多地高温预警", "中央气象台发布高温黄色预警，多地气温突破40度。", "5500000", "热", "中国天气", "88000", "28000"},
            {"wb_5", "新iPhone发布时间确认", "苹果公司正式宣布新一代iPhone发布会时间。", "4800000", "新", "科技圈", "76000", "42000"},
            {"wb_6", "油价迎来年内最大降幅", "国家发改委宣布下调成品油价格，加满一箱油节省XX元。", "4200000", "", "财经快讯", "65000", "22000"},
            {"wb_7", "某热门综艺官宣嘉宾阵容", "年度最受期待的综艺节目公布完整嘉宾名单。", "3800000", "", "综艺频道", "58000", "35000"},
            {"wb_8", "国产大飞机C919新航线", "C919执飞的新航线正式开通，标志着国产大飞机商业化运营再进一步。", "3500000", "", "航空资讯", "52000", "18000"},
            {"wb_9", "端午节放假安排", "国务院办公厅发布端午节放假安排通知。", "3200000", "", "人民日报", "48000", "15000"},
            {"wb_10", "某地出台购房新政策", "多地出台房地产调控新政策，涉及首付比例、贷款利率等。", "2900000", "", "财经观察", "42000", "25000"},
        };

        for (String[] item : mockData) {
            if (topics.size() >= limit) break;
            HotTopic topic = new HotTopic();
            topic.setPlatform(getPlatformId());
            topic.setTopicId(item[0]);
            topic.setTitle(item[1]);
            topic.setSummary(item[2]);
            topic.setHeatValue(Long.parseLong(item[3]));
            topic.setHeatLabel(item[4].isEmpty() ? null : item[4]);
            topic.setRank(topics.size() + 1);
            topic.setAuthor(item[5]);
            topic.setCommentCount(Long.parseLong(item[6]));
            topic.setShareCount(Long.parseLong(item[7]));
            topic.setTags("[\"热搜\",\"实时\"]");
            topic.setCrawledAt(LocalDateTime.now());
            topics.add(topic);
        }

        log.info("[微博] 爬取完成, 获取 {} 条热搜", topics.size());
        return topics;
    }

    @Override
    public List<Comment> fetchComments(String topicId, int limit) {
        log.info("[微博] 开始爬取评论, topicId={}, limit={}", topicId, limit);
        List<Comment> comments = new ArrayList<>();

        String[][] mockComments = {
            {"wb_c1", "太棒了！为中国航天点赞！", "爱国网友", "28000", "120"},
            {"wb_c2", "航天员辛苦了，欢迎回家！", "航天迷", "22000", "85"},
            {"wb_c3", "每次看到这种新闻都很感动，祖国强大了", "正能量", "18000", "62"},
            {"wb_c4", "从发射到返回，整个过程太震撼了", "科技爱好者", "15000", "45"},
            {"wb_c5", "致敬所有为中国航天事业默默奉献的人", "感恩的心", "12000", "38"},
            {"wb_c6", "期待中国航天创造更多奇迹！", "未来可期", "9800", "28"},
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
            comment.setPublishedAt(LocalDateTime.now().minusMinutes(i * 15));
            comment.setCrawledAt(LocalDateTime.now());
            comments.add(comment);
        }

        log.info("[微博] 评论爬取完成, 获取 {} 条评论", comments.size());
        return comments;
    }
}
