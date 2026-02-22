package com.xuan.entity.po.blog;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 文章主表实体类
 * 对应数据库表：article
 * 用于存储文章的基本信息和内容
 * 
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
@Schema(description = "文章主表实体类")
public class Article extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作者id
     */
    @Schema(description = "作者id", example = "1")
    private Long authorId;

    /**
     * 分类id
     */
    @Schema(description = "分类id", example = "1")
    private Long categoryId;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题", example = "Spring Boot 3实战")
    private String title;

    /**
     * URL别名(SEO)，唯一
     */
    @Schema(description = "URL别名(SEO)，唯一", example = "spring-boot-3-practical")
    private String slug;

    /**
     * 文章摘要
     */
    @Schema(description = "文章摘要", example = "基于JDK21的实战总结")
    private String summary;

    /**
     * 文章内容(markdown)
     */
    @Schema(description = "文章内容(markdown)", example = "# Hello World\n...")
    private String content;

    /**
     * 封面图片
     */
    @Schema(description = "封面图片", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/2026-02-16/cover.png")
    private String coverImg;

    /**
     * SEO关键词
     */
    @Schema(description = "SEO关键词", example = "Spring Boot,Java,JDK21")
    private String keywords;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数", example = "100")
    private Long viewCount;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数", example = "50")
    private Long likeCount;

    /**
     * 是否置顶：1-是；0-否
     */
    @Schema(description = "是否置顶：1-是；0-否", example = "0")
    private Integer isTop;

    /**
     * 状态：0-草稿，1-发布，2-下架
     */
    @Schema(description = "状态：0-草稿，1-发布，2-下架", example = "1")
    private Integer status;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2023-10-01T10:00:00")
    private LocalDateTime publishTime;
}
