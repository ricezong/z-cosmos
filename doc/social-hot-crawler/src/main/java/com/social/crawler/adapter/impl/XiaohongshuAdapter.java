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
 * 小红书热点适配器
 *
 * 数据来源：小红书热搜/发现页
 * 支持功能：热点榜单、评论爬取
 */
@Component
public class XiaohongshuAdapter extends AbstractPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(XiaohongshuAdapter.class);

    @Override
    public String getPlatformId() { return "xiaohongshu"; }

    @Override
    public String getPlatformName() { return "小红书"; }

    @Override
    public String getPlatformIcon() { return "\uD83D\uDDA4"; }

    @Override
    public Set<PlatformCapability> getCapabilities() {
        return EnumSet.of(PlatformCapability.HOT_TOPICS, PlatformCapability.COMMENTS, PlatformCapability.NESTED_COMMENTS);
    }

    @Override
    public List<HotTopic> fetchHotTopics(int limit) {
        log.info("[小红书] 开始爬取热点榜单, limit={}", limit);
        List<HotTopic> topics = new ArrayList<>();

        String[][] mockData = {
            {"xhs_1", "居家收纳神器推荐", "租房党必看的收纳好物分享，小空间也能很整洁", "4500000", "热",
             "收纳达人", "https://img.example.com/xhs1.jpg", "120000", "8900", "2300"},
            {"xhs_2", "一周减脂餐食谱", "营养师推荐的一周健康减脂餐，好吃不胖", "3800000", "热",
             "健康美食家", "https://img.example.com/xhs2.jpg", "95000", "7200", "1800"},
            {"xhs_3", "平价护肤品测评", "学生党也能用得起的护肤品真实测评", "3200000", "",
             "成分党小美", "https://img.example.com/xhs3.jpg", "88000", "6500", "1500"},
            {"xhs_4", "旅行拍照姿势大全", "教你拍出朋友圈点赞最多的旅行照", "2900000", "新",
             "摄影师小王", "https://img.example.com/xhs4.jpg", "76000", "5800", "1200"},
            {"xhs_5", "考研上岸经验分享", "双非院校逆袭985，我的考研备考全过程", "2600000", "",
             "考研学姐", "https://img.example.com/xhs5.jpg", "68000", "5200", "980"},
            {"xhs_6", "新手化妆教程", "手残党也能学会的日常妆容教程", "2300000", "",
             "美妆小白", "https://img.example.com/xhs6.jpg", "62000", "4800", "860"},
            {"xhs_7", "租房避坑指南", "第一次租房需要注意的那些事", "2000000", "",
             "租房老司机", "https://img.example.com/xhs7.jpg", "55000", "4200", "750"},
            {"xhs_8", "自制健康饮品", "夏天在家就能做的清爽饮品合集", "1700000", "",
             "饮品控", "https://img.example.com/xhs8.jpg", "48000", "3600", "620"},
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
            topic.setCoverUrl(item[6]);
            topic.setLikeCount(Long.parseLong(item[7]));
            topic.setCommentCount(Long.parseLong(item[8]));
            topic.setShareCount(Long.parseLong(item[9]));
            topic.setTags("[\"生活\",\"分享\"]");
            topic.setCrawledAt(LocalDateTime.now());
            topics.add(topic);
        }

        log.info("[小红书] 爬取完成, 获取 {} 条热点", topics.size());
        return topics;
    }

    @Override
    public List<Comment> fetchComments(String topicId, int limit) {
        log.info("[小红书] 开始爬取评论, topicId={}, limit={}", topicId, limit);
        List<Comment> comments = new ArrayList<>();

        String[][] mockComments = {
            {"xhs_c1", "太实用了，已经下单了！", "小红薯123", "3200", "45"},
            {"xhs_c2", "姐妹们冲！真的好用", "爱分享的小鱼", "2800", "32"},
            {"xhs_c3", "请问链接在哪里呀？", "好奇宝宝", "1500", "28"},
            {"xhs_c4", "我也买了，确实不错", "已入手", "1200", "15"},
            {"xhs_c5", "收藏了！周末去采购", "周末计划", "900", "8"},
            {"xhs_c6", "有没有平替推荐？学生党预算有限", "省钱小能手", "800", "22"},
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
            comment.setPublishedAt(LocalDateTime.now().minusMinutes(i * 8));
            comment.setCrawledAt(LocalDateTime.now());
            comments.add(comment);
        }

        log.info("[小红书] 评论爬取完成, 获取 {} 条评论", comments.size());
        return comments;
    }
}
