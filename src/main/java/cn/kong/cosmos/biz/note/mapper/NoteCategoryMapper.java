package cn.kong.cosmos.biz.note.mapper;

import cn.kong.cosmos.biz.note.entity.NoteCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记类别 Mapper
 */
@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {
}