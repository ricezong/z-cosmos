package com.search.engine.util;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 分词器 - 基于Trie树的中文分词工具
 * 
 * 对应博客中的"分词并创建临时索引"模块
 * 
 * 分词策略：
 * 1. 中文文本：使用Trie树进行前向最大匹配分词
 * 2. 英文文本：按空格分词，统一转小写
 * 3. 混合文本：先按中英文边界切分，分别处理
 * 4. 过滤停用词和标点符号
 * 5. 过滤过短的词（单字符英文）
 */
public class WordSegmenter {

    /** Trie树词典 */
    private final TrieTree trie;

    /** 最大词长 */
    private final int maxWordLength;

    /** 停用词集合 */
    private final Set<String> stopWords;

    /** 中文字符正则 */
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fff]+");
    
    /** 英文单词正则 */
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");

    /** 默认中文停用词 */
    private static final String[] DEFAULT_STOP_WORDS = {
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
        "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
        "自己", "这", "他", "她", "它", "们", "那", "些", "什么", "怎么", "如何", "为什么",
        "可以", "因为", "所以", "但是", "如果", "虽然", "或者", "而且", "以及", "the", "a",
        "an", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
        "do", "does", "did", "will", "would", "could", "should", "may", "might", "shall",
        "can", "need", "dare", "ought", "used", "to", "of", "in", "for", "on", "with",
        "at", "by", "from", "as", "into", "through", "during", "before", "after", "above",
        "below", "between", "out", "off", "over", "under", "again", "further", "then",
        "once", "and", "but", "or", "nor", "not", "so", "yet", "both", "either", "neither",
        "each", "every", "all", "any", "few", "more", "most", "other", "some", "such",
        "no", "only", "own", "same", "than", "too", "very", "just", "because", "if",
        "when", "where", "how", "what", "which", "who", "whom", "this", "that", "these",
        "those", "it", "its", "he", "she", "they", "them", "his", "her", "their", "our",
        "your", "my", "me", "him", "us", "we"
    };

    public WordSegmenter() {
        this.trie = new TrieTree();
        this.maxWordLength = 8;
        this.stopWords = new HashSet<>(Arrays.asList(DEFAULT_STOP_WORDS));
    }

    /**
     * 添加自定义词典
     * @param words 词语列表
     */
    public void addDictionary(Collection<String> words) {
        trie.insertAll(words);
    }

    /**
     * 添加自定义停用词
     * @param words 停用词列表
     */
    public void addStopWords(Collection<String> words) {
        stopWords.addAll(words);
    }

    /**
     * 对文本进行分词
     * @param text 输入文本
     * @return 分词结果列表（已过滤停用词和短词）
     */
    public List<String> segment(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();

        List<String> allTokens = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        boolean inChinese = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isChinese = isChineseChar(c);
            boolean isEnglish = isEnglishChar(c);

            if (isChinese) {
                if (!inChinese && currentSegment.length() > 0) {
                    // 从英文切换到中文，先处理英文部分
                    processEnglishSegment(currentSegment.toString(), allTokens);
                    currentSegment.setLength(0);
                }
                currentSegment.append(c);
                inChinese = true;
            } else if (isEnglish) {
                if (inChinese && currentSegment.length() > 0) {
                    // 从中文切换到英文，先处理中文部分
                    processChineseSegment(currentSegment.toString(), allTokens);
                    currentSegment.setLength(0);
                }
                currentSegment.append(c);
                inChinese = false;
            } else {
                // 其他字符（标点、数字等），作为分隔符
                if (currentSegment.length() > 0) {
                    if (inChinese) {
                        processChineseSegment(currentSegment.toString(), allTokens);
                    } else {
                        processEnglishSegment(currentSegment.toString(), allTokens);
                    }
                    currentSegment.setLength(0);
                }
                inChinese = false;
            }
        }

        // 处理最后一段
        if (currentSegment.length() > 0) {
            if (inChinese) {
                processChineseSegment(currentSegment.toString(), allTokens);
            } else {
                processEnglishSegment(currentSegment.toString(), allTokens);
            }
        }

        return allTokens;
    }

    /**
     * 处理中文片段 - 使用Trie树分词
     */
    private void processChineseSegment(String text, List<String> result) {
        List<String> words = trie.segment(text, maxWordLength);
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() >= 2) {
                result.add(word);
            }
        }
    }

    /**
     * 处理英文片段 - 按空格分词
     */
    private void processEnglishSegment(String text, List<String> result) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            word = word.replaceAll("[^a-z]", "");
            if (word.length() >= 2 && !stopWords.contains(word)) {
                result.add(word);
            }
        }
    }

    /**
     * 判断是否为中文字符
     */
    private boolean isChineseChar(char c) {
        return c >= '\u4e00' && c <= '\u9fff';
    }

    /**
     * 判断是否为英文字符
     */
    private boolean isEnglishChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * 获取Trie树中的词数
     */
    public int getDictionarySize() {
        return trie.getWordCount();
    }
}
