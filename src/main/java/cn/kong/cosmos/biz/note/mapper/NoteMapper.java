package cn.kong.cosmos.biz.note.mapper;

import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.entity.Note;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 技术笔记 Mapper
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    /**
     * 原子增加阅读数（避免并发问题）
     */
    @Update("UPDATE z_notes SET view_count = view_count + 1 WHERE note_id = #{noteId}")
    void incrementViewCount(@Param("noteId") String noteId);

    /**
     * 分页查询笔记列表（LEFT JOIN 关联分类表，避免 N+1 查询）
     */
    @Select("""
        <script>
        SELECT
            n.note_id,
            n.title,
            n.category_code,
            n.tags,
            n.content_type,
            n.source_url,
            n.read_minutes,
            n.is_locked,
            n.short_summary,
            n.view_count,
            n.created_at,
            c.category_name
        FROM z_notes n
        LEFT JOIN z_note_categories c ON n.category_code = c.category_code
        <where>
            <if test="categoryCode != null and categoryCode != ''">
                AND n.category_code = #{categoryCode}
            </if>
        </where>
        ORDER BY n.created_at DESC
        </script>
        """)
    Page<NoteListDTO> selectNoteListWithCategory(Page<Note> page, @Param("categoryCode") String categoryCode);
}
