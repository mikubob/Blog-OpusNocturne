package com.xuan.entity.vo.article;

import com.xuan.entity.vo.tag.TagVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情响应数据类
 * 对应接口：5.10前台文章详情 (Portal)
 * 用于返回文章详细信息
 * 
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "文章详情响应数据类")
public class ArticleDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 文章ID
     */
    @Schema(description = "文章ID", example = "1")
    private Long id;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题", example = "Spring Boot 3实战")
    private String title;

    /**
     * 文章内容
     */
    @Schema(description = "文章内容")
    private String content;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "Java")
    private String categoryName;

    /**
     * 作者昵称
     */
    @Schema(description = "作者昵称", example = "玄〤")
    private String authorNickname;

    /**
     * 摘要
     */
    @Schema(description = "摘要", example = "本文介绍Spring Boot 3的核心特性")
    private String summary;

    /**
     * 封面图片
     */
    @Schema(description = "封面图片", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/2026-02-16/cover.png")
    private String coverImg;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数", example = "121")
    private Long viewCount;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2023-10-01T10:00:00")
    private LocalDateTime publishTime;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<TagVO> tags;

    /**
     * 上一篇文章
     */
    @Schema(description = "上一篇文章")
    private ArticleNavVO prevArticle;

    /**
     * 下一篇文章
     */
    @Schema(description = "下一篇文章")
    private ArticleNavVO nextArticle;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数", example = "50")
    private Long likeCount;

    /**
     * 文章前后篇响应数据类
     * 用于在文章详情中展示上一篇和下一篇文章信息
     */
    @Data
    @Schema(description = "文章前后篇响应数据类")
    public static class ArticleNavVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 文章ID
         */
        @Schema(description = "文章ID", example = "2")
        private Long id;

        /**
         * 文章标题
         */
        @Schema(description = "文章标题", example = "Spring Cloud 实战")
        private String title;
    }
}