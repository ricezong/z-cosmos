package com.search.engine.util;

import java.util.BitSet;

/**
 * 布隆过滤器（Bloom Filter）
 * 
 * 对应博客中的 bloom_filter.bin 文件
 * 用于高效判断一个URL是否已经被爬取过
 * 
 * 原理：
 * - 使用一个位数组（BitSet）和多个哈希函数
 * - 添加元素时，用多个哈希函数计算位置并置1
 * - 查询时，检查所有哈希位置是否都为1
 * - 可能存在假阳性（误判为存在），但不会假阴性（不会漏判）
 * 
 * 空间效率：存储100万个URL大约只需要1.2MB
 */
public class BloomFilter {

    /** 位数组 */
    private final BitSet bitSet;

    /** 位数组大小 */
    private final int bitSize;

    /** 哈希函数数量 */
    private final int hashCount;

    /**
     * 创建布隆过滤器
     * @param expectedElements 预期元素数量
     * @param falsePositiveRate 期望的假阳性率
     */
    public BloomFilter(int expectedElements, double falsePositiveRate) {
        // 计算最优位数组大小：m = -(n * ln(p)) / (ln(2)^2)
        this.bitSize = (int) Math.ceil(
            -(expectedElements * Math.log(falsePositiveRate)) / (Math.log(2) * Math.log(2))
        );
        // 计算最优哈希函数数量：k = (m/n) * ln(2)
        this.hashCount = Math.max(1, (int) Math.round(
            ((double) bitSize / expectedElements) * Math.log(2)
        ));
        this.bitSet = new BitSet(bitSize);
    }

    /**
     * 创建默认布隆过滤器（100万元素，1%假阳性率）
     */
    public BloomFilter() {
        this(1_000_000, 0.01);
    }

    /**
     * 添加元素到布隆过滤器
     * @param value 要添加的值（通常是URL）
     */
    public void add(String value) {
        byte[] bytes = value.getBytes();
        for (int i = 0; i < hashCount; i++) {
            int hash = murmurHash(bytes, i);
            int pos = Math.abs(hash) % bitSize;
            bitSet.set(pos, true);
        }
    }

    /**
     * 判断元素是否可能存在于布隆过滤器中
     * @param value 要查询的值
     * @return true表示可能存在（可能误判），false表示一定不存在
     */
    public boolean mightContain(String value) {
        byte[] bytes = value.getBytes();
        for (int i = 0; i < hashCount; i++) {
            int hash = murmurHash(bytes, i);
            int pos = Math.abs(hash) % bitSize;
            if (!bitSet.get(pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * MurmurHash变体 - 使用不同的种子值模拟多个哈希函数
     * @param data 输入数据
     * @param seed 种子值（第i个哈希函数使用i作为种子）
     * @return 哈希值
     */
    private int murmurHash(byte[] data, int seed) {
        int h = seed;
        for (byte b : data) {
            h = h * 31 + b;
        }
        // 二次混合以减少碰撞
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    /**
     * 获取当前已设置的位数（近似元素数量）
     */
    public int getBitCount() {
        return bitSet.cardinality();
    }

    /**
     * 获取位数组大小
     */
    public int getBitSize() {
        return bitSize;
    }

    /**
     * 获取哈希函数数量
     */
    public int getHashCount() {
        return hashCount;
    }

    /**
     * 计算当前的假阳性率
     */
    public double getCurrentFalsePositiveRate() {
        int setBits = bitSet.cardinality();
        return Math.pow((double) setBits / bitSize, hashCount);
    }

    /**
     * 清空布隆过滤器
     */
    public void clear() {
        bitSet.clear();
    }
}
