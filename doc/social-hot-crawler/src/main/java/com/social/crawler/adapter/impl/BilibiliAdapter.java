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
 * 哔哩哔哩热点适配器
 *
 * 数据来源：B站热搜/热门视频
 * 支持功能：热点榜单、评论爬取
 */
@Component
public class BilibiliAdapter extends AbstractPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(BilibiliAdapter.class);

    @Override
    public String getPlatformId() { return "bilibili"; }

    @Override
    public String getPlatformName() { return "哔哩哔哩"; }

    @Override
    public String getPlatformIcon() { return "\uD83D\uDCFA"; }

    @Override
    public Set<PlatformCapability> getCapabilities() {
        return EnumSet.of(PlatformCapability.HOT_TOPICS, PlatformCapability.COMMENTS, PlatformCapability.NESTED_COMMENTS);
    }

    @Override
    public List<HotTopic> fetchHotTopics(int limit) {
        log.info("[B站] 开始爬取热搜, limit={}", limit);
        List<HotTopic> topics = new ArrayList<>();

        String[][] mockData = {
            {"bili_1", "AI绘画到底有多强？实测10款AI绘画工具", "全面对比当前最流行的AI绘画工具，从出图质量、操作难度、价格等方面进行详细测评。", "8500000", "热", "科技UP主", "https://img.example.com/bili1.jpg", "320000", "15000", "28000", "1200000"},
            {"bili_2", "挑战24小时只吃便利店食物", "记录一天三餐全部在便利店解决的体验，看看能不能吃得健康又省钱。", "6200000", "", "生活UP主", "https://img.example.com/bili2.jpg", "280000", "12000", "22000", "980000"},
            {"bili_3", "用代码实现一个操作系统", "从零开始用C语言编写一个简易操作系统，讲解操作系统的核心原理。", "5800000", "热", "编程UP主", "https://img.example.com/bili3.jpg", "250000", "8500", "18000", "850000"},
            {"bili_4", "全国各地早餐大合集", "走遍中国各地，品尝最具特色的早餐美食，感受不同地域的饮食文化。", "4900000", "", "美食UP主", "https://img.example.com/bili4.jpg", "210000", "9800", "16000", "720000"},
            {"bili_5", "2024年最值得买的数码产品", "从手机、笔记本到外设配件，盘点今年最值得入手的数码好物。", "4300000", "", "数码UP主", "https://img.example.com/bili5.jpg", "180000", "7200", "14000", "650000"},
            {"bili_6", "大学四年到底学到了什么", "毕业季回顾，分享大学四年的真实收获和感悟。", "3800000", "新", "校园UP主", "https://img.example.com/bili6.jpg", "160000", "11000", "20000", "580000"},
            {"bili_7", "自制机械键盘全过程", "从设计电路到3D打印外壳，完整记录一把机械键盘的诞生过程。", "3400000", "", "DIY UP主", "https://img.example.com/bili7.jpg", "140000", "6800", "12000", "520000"},
            {"bili_8", "一口气看完《三体》全集解说", "用最通俗的语言为你解读刘慈欣的科幻巨著《三体》三部曲。", "3000000", "", "影视UP主", "https://img.example.com/bili8.jpg", "120000", "5500", "9800", "480000"},
            {"bili_9", "日本街头美食探店Vlog", "深入日本大街小巷，寻找最地道的日本美食。", "2600000", "", "旅行UP主", "https://img.example.com/bili9.jpg", "100000", "4800", "8500", "420000"},
            {"bili_10", "新手养猫完全指南", "从选猫到日常护理，新手铲屎官必看的养猫知识大全。", "2200000", "", "宠物UP主", "https://img.example.com/bili10.jpg", "85000", "4200", "7200", "380000"},
        };

        for (String[] item : mockData) {
            if (topics.size() >= limit) break;
            HotTopic topic = new HotTopic();
            topic.setPlatform(getPlatformId());
            topic.setTopicId(item[0]);
            topic.setTitle(item[1]);
            topic.setSummary(item[2]);
            topic.setUrl("https://www.bilibili.com/video/" + item[0].replace("bili_", "BV"));
            topic.setCoverUrl(item[6]);
            topic.setHeatValue(Long.parseLong(item[3]));
            topic.setHeatLabel(item[4].isEmpty() ? null : item[4]);
            topic.setRank(topics.size() + 1);
            topic.setAuthor(item[5]);
            topic.setLikeCount(Long.parseLong(item[7]));
            topic.setCommentCount(Long.parseLong(item[8]));
            topic.setShareCount(Long.parseLong(item[9]));
            topic.setViewCount(Long.parseLong(item[10]));
            topic.setTags("[\"视频\",\"热门\"]");
            topic.setCrawledAt(LocalDateTime.now());
            topics.add(topic);
        }

        log.info("[B站] 爬取完成, 获取 {} 条热搜", topics.size());
        return topics;
    }

    @Override
    public List<Comment> fetchComments(String topicId, int limit) {
        log.info("[B站] 开始爬取评论, topicId={}, limit={}", topicId, limit);
        List<Comment> comments = new ArrayList<>();

        String[][] mockComments = {
            {"bili_c1", "太强了UP主！这期视频质量超高，已三连", "B站老用户", "15000", "85"},
            {"bili_c2", "终于有人做这个对比了，收藏了慢慢看", "收藏夹满了", "12000", "62"},
            {"bili_c3", "第二个工具的效果真的惊艳到我了", "设计爱好者", "9800", "45"},
            {"bili_c4", "建议UP主出一期进阶教程，讲讲参数调优", "进阶玩家", "8500", "38"},
            {"bili_c5", "有一说一，第一个工具的出图速度确实快", "客观评价", "7200", "28"},
            {"bili_c6", "作为美术生，我觉得AI绘画在创意方面还有很长的路要走", "美术生小张", "6800", "92"},
            {"bili_c7", "催更催更！下一期能不能讲讲AI视频生成？", "催更大军", "5500", "35"},
            {"bili_c8", "笑死，我用第一个工具生成的图把我妈吓到了", "搞笑担当", "4800", "120"},
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
            comment.setPublishedAt(LocalDateTime.now().minusHours(i));
            comment.setCrawledAt(LocalDateTime.now());
            comments.add(comment);
        }

        // 嵌套回复
        if (comments.size() >= 2) {
            Comment reply = new Comment();
            reply.setPlatform(getPlatformId());
            reply.setCommentId("bili_r1");
            reply.setParentId(comments.get(1).getId());
            reply.setReplyToId(comments.get(1).getId());
            reply.setContent("同收藏！UP主的视频一直都很用心");
            reply.setUsername("路人乙");
            reply.setLikeCount(800L);
            reply.setPublishedAt(LocalDateTime.now().minusMinutes(30));
            reply.setCrawledAt(LocalDateTime.now());
            comments.add(reply);
        }

        log.info("[B站] 评论爬取完成, 获取 {} 条评论", comments.size());
        return comments;
    }
}
