package cn.kong.cosmos.biz.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体 - 对应 z_collects 表
 */
@Data
@TableName("z_collects")
public class Collect {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String postId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
