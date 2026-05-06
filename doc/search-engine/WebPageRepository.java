package com.search.engine.repository;

import com.search.engine.model.WebPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebPageRepository extends JpaRepository<WebPage, Long> {

    /** 根据URL查找网页 */
    Optional<WebPage> findByUrl(String url);

    /** 检查URL是否已存在（布隆过滤器的数据库后备） */
    boolean existsByUrl(String url);

    /** 根据状态查询网页 */
    List<WebPage> findByStatus(String status);

    /** 统计各状态的网页数量 */
    long countByStatus(String status);

    /** 全文搜索：在文本内容中搜索关键词 */
    @Query("SELECT w FROM WebPage w WHERE w.textContent LIKE %:keyword%")
    List<WebPage> searchByTextContent(String keyword);

    /** 全文搜索：在标题中搜索关键词 */
    @Query("SELECT w FROM WebPage w WHERE w.title LIKE %:keyword%")
    List<WebPage> searchByTitle(String keyword);
}
