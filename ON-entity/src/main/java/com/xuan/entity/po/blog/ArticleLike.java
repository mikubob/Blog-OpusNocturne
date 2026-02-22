
package com.xuan.entity.po.blog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章点赞实体，用于记录用户对文章的点赞行为
 * 对应数据库表：article_like
 *
 * @author 玄〤
 * @since 2026-02-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_like")
@Schema(description = "文章点赞实体类")
public class ArticleLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long articleId;

    /** 点赞人IP */
    private String ipAddress;

    /** 点赞人ID (登录用户) */
    private Long userId;

    /** 点赞时间 */
    private LocalDateTime createTime;
}
