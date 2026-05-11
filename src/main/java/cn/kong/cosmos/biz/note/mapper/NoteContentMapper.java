package cn.kong.cosmos.biz.note.mapper;

import cn.kong.cosmos.biz.note.entity.NoteContent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记内容 Mapper
 */
@Mapper
public interface NoteContentMapper extends BaseMapper<NoteContent> {
}
