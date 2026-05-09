package cn.kong.cosmos.biz.note.mapper;

import cn.kong.cosmos.biz.note.entity.Note;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 技术笔记 Mapper
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
