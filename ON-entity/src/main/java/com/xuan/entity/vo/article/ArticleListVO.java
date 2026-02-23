package com.xuan.entity.vo.article;

import com.xuan.entity.vo.tag.TagVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表响应数据类
 * 对应接口：5.9前台文章列表 (Portal)
 * 用于返回文章列表数据
 * 
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "文章列表响应数据类")
public class ArticleListVO implements Serializable {

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
     * 摘要
     */
    @Schema(description = "摘要", example = "基于JDK21的实战总结")
    private String summary;

    /**
     * 封面图片
     */
    @Schema(description = "封面图片", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/2026-02-16/cover.png")
    private String coverImg;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数", example = "100")
    private Long viewCount;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2026-02-16T10:00:00")
    private LocalDateTime publishTime;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "Java")
    private String categoryName;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<TagVO> tags;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数", example = "50")
    private Long likeCount;
}