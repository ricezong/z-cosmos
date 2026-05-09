package cn.kong.cosmos.biz.search.mapper;

import cn.kong.cosmos.biz.search.entity.SearchIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索索引 Mapper
 */
@Mapper
public interface SearchIndexMapper extends BaseMapper<SearchIndex> {

    /**
     * 全文搜索（利用 ngram 全文索引）
     * @param keyword 搜索关键词
     * @param contentType 内容类型过滤（可选）
     * @return 匹配结果
     */
    @Select("<script>" +
        "SELECT id, content_type, content_id, title, keywords, tags, created_at, " +
        "MATCH(title, keywords, tags) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS relevance " +
        "FROM z_search_index " +
        "WHERE MATCH(title, keywords, tags) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
        "<if test='contentType != null and contentType != \"\"'>" +
        "AND content_type = #{contentType} " +
        "</if>" +
        "ORDER BY relevance DESC" +
        "</script>")
    List<SearchIndex> fullTextSearch(@Param("keyword") String keyword, @Param("contentType") String contentType);
}
