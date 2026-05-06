package com.social.crawler.adapter;

import com.social.crawler.model.HotTopic;
import com.social.crawler.model.Comment;

import java.util.List;

/**
 * 平台适配器接口 - 策略模式的核心
 *
 * 设计原则：
 * 1. 每个社交平台实现此接口
 * 2. 新增平台只需实现此接口并注册为Spring Bean
 * 3. 统一的数据模型，屏蔽各平台差异
 *
 * 未来扩展新平台只需三步：
 *   ① 创建 XxxAdapter implements PlatformAdapter
 *   ② 添加 @Component 和 @ConditionalOnProperty 注解
 *   ③ 实现 fetchHotTopics() 和 fetchComments() 方法
 */
public interface PlatformAdapter {

    /** 获取平台唯一标识 */
    String getPlatformId();

    /** 获取平台显示名称 */
    String getPlatformName();

    /** 获取平台图标（emoji或图标名） */
    String getPlatformIcon();

    /** 获取平台支持的功能 */
    PlatformCapability getCapabilities();

    /**
     * 爬取热点列表
     * @param limit 最大返回数量
     * @return 热点列表
     */
    List<HotTopic> fetchHotTopics(int limit);

    /**
     * 爬取指定话题的评论
     * @param topicId 话题在平台上的原始ID
     * @param limit 最大返回数量
     * @return 评论列表（支持嵌套回复）
     */
    List<Comment> fetchComments(String topicId, int limit);

    /**
     * 爬取指定话题的更多评论（分页）
     * @param topicId 话题ID
     * @param cursor 分页游标（平台相关）
     * @param limit 最大返回数量
     * @return 评论列表
     */
    default List<Comment> fetchMoreComments(String topicId, String cursor, int limit) {
        return List.of(); // 默认不支持分页
    }

    /**
     * 测试平台连接是否正常
     * @return true表示连接正常
     */
    default boolean testConnection() {
        return true;
    }

    /**
     * 获取平台状态信息
     */
    default PlatformStatus getStatus() {
        return new PlatformStatus(true, "正常运行");
    }

    /**
     * 平台能力枚举
     */
    enum PlatformCapability {
        HOT_TOPICS,       // 热点榜单
        COMMENTS,         // 评论区
        NESTED_COMMENTS,  // 嵌套回复
        SEARCH,           // 站内搜索
        USER_PROFILE      // 用户主页
    }

    /**
     * 平台状态
     */
    record PlatformStatus(boolean healthy, String message) {}
}
