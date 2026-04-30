package cn.kong.cosmos.biz.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞实体 - 对应 z_likes 表
 * 帖子和评论共用此表（polymorphic）
 */
@Data
@TableName("z_likes")
public class Like {

    /** POST / COMMENT */
    public static final String TYPE_POST = "POST";
    public static final String TYPE_COMMENT = "COMMENT";

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    /** POST / COMMENT */
    private String targetType;

    private String targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
