package com.search.engine.util;

import java.util.*;

/**
 * Trie树（前缀树）- 用于中文分词
 * 
 * 对应博客中的分词模块
 * 使用基于词典的前向最大匹配算法进行分词
 * 
 * Trie树结构：
 * - 每个节点包含一个字符和子节点映射
 * - 从根节点到某个节点的路径表示一个词的前缀
 * - 标记为词尾的节点表示一个完整的词
 * 
 * 分词算法：前向最大匹配（FMM）
 * - 从文本的当前位置开始，在Trie树中查找最长匹配
 * - 如果找到匹配，则切分出一个词，继续处理剩余文本
 * - 如果没有匹配，则按单字切分
 */
public class TrieTree {

    /** Trie树的根节点 */
    private final TrieNode root;

    /** 词典中的总词数 */
    private int wordCount = 0;

    public TrieTree() {
        this.root = new TrieNode();
    }

    /**
     * Trie树节点
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    /**
     * 向Trie树中添加一个词
     * @param word 要添加的词
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        if (!node.isEndOfWord) {
            node.isEndOfWord = true;
            wordCount++;
        }
    }

    /**
     * 批量添加词语
     * @param words 词语列表
     */
    public void insertAll(Collection<String> words) {
        for (String word : words) {
            insert(word);
        }
    }

    /**
     * 在Trie树中查找最长匹配
     * @param text 输入文本
     * @param start 开始位置
     * @return 最长匹配的词语长度，如果没有匹配返回0
     */
    public int findLongestMatch(String text, int start) {
        TrieNode node = root;
        int maxMatch = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!node.children.containsKey(c)) {
                break;
            }
            node = node.children.get(c);
            if (node.isEndOfWord) {
                maxMatch = i - start + 1;
            }
        }
        return maxMatch;
    }

    /**
     * 前向最大匹配分词
     * @param text 输入文本
     * @param maxWordLength 最大词长
     * @return 分词结果列表
     */
    public List<String> segment(String text, int maxWordLength) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;

        int pos = 0;
        while (pos < text.length()) {
            // 限制最大匹配长度
            int end = Math.min(pos + maxWordLength, text.length());
            String segment = text.substring(pos, end);

            // 在Trie树中查找最长匹配
            int matchLen = findLongestMatch(text, pos);

            if (matchLen > 0) {
                result.add(text.substring(pos, pos + matchLen));
                pos += matchLen;
            } else {
                // 没有匹配，按单字切分
                result.add(String.valueOf(text.charAt(pos)));
                pos++;
            }
        }
        return result;
    }

    /**
     * 检查某个词是否在Trie树中
     */
    public boolean contains(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) return false;
            node = node.children.get(c);
        }
        return node.isEndOfWord;
    }

    /**
     * 获取词典中的总词数
     */
    public int getWordCount() {
        return wordCount;
    }
}
