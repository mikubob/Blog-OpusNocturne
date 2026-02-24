package com.xuan.entity.po.blog;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章-标签关联表实体类
 * 对应数据库表：article_tag
 * 用于存储文章与标签的关联关系
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@TableName("article_tag")
@Schema(description = "文章-标签关联表实体类")
@Builder
public class ArticleTag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Schema(description = "主键id", example = "1")
    private Long id;

    /**
     * 文章id
     */
    @Schema(description = "文章id", example = "100")
    private Long articleId;

    /**
     * 标签id
     */
    @Schema(description = "标签id", example = "1")
    private Long tagId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-17T10:00:00")
    private LocalDateTime createTime;
}
