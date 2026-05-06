package com.search.engine.repository;

import com.search.engine.model.InvertedIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvertedIndexRepository extends JpaRepository<InvertedIndex, Long> {

    /** 根据词语ID查找所有倒排索引记录 */
    List<InvertedIndex> findByTermId(Long termId);

    /** 根据网页ID查找所有倒排索引记录 */
    List<InvertedIndex> findByPageId(Long pageId);

    /** 根据词语ID和网页ID查找 */
    List<InvertedIndex> findByTermIdAndPageId(Long termId, Long pageId);

    /** 根据多个词语ID查找（用于多关键词查询的交集操作） */
    @Query("SELECT DISTINCT ii.pageId FROM InvertedIndex ii WHERE ii.termId IN :termIds")
    List<Long> findPageIdsByTermIds(List<Long> termIds);

    /** 查找同时包含多个词语的网页ID（交集） */
    @Query("SELECT ii.pageId FROM InvertedIndex ii WHERE ii.termId = :termId")
    List<Long> findPageIdsByTermId(Long termId);

    /** 删除某个网页的所有索引 */
    void deleteByPageId(Long pageId);

    /** 统计索引记录总数 */
    long countByTermId(Long termId);
}
