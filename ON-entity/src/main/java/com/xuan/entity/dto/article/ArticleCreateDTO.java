package com.xuan.entity.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建文章请求参数类
 * 对应接口文档5.1
 * 用于接收前端创建文章的参数
 * @author 玄〤
 * @since 2026-02-20
 */
@Data
@Schema(description = "创建文章请求参数类")
public class ArticleCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章标题，必填
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200,message = "文章标题长度不能超过200字符")
    @Schema(description = "文章标题",example = "Spring Boot 3实战",requiredMode = RequiredMode.REQUIRED)
    private String title;
    
    /**
     * 文章内容，必填
     */
    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "文章内容(markdown)",requiredMode = RequiredMode.REQUIRED)
    private String content;
    
    /**
     * 摘要
     */
    @Size(max = 500,message = "文章摘要长度不能超过500字符")
    @Schema(description = "文章摘要",example = "基于JDK21的实战总结")
    private String summary;
    
    /**
     * 分类ID，必填
     */
    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID",example = "1",requiredMode = RequiredMode.REQUIRED)
    private Long categoryId;
    
    /**
     * 标签ID列表
     */
    @Schema(description = "标签ID列表",example = "[1,2,3]")
    private List<Long> tagIds;
    
    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL",example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/2026-02-16/cover.png")
    private String coverImg;
    
    /**
     * 是否置顶：1-是，0-否
     */
    @Schema(description = "是否置顶：1-是，0-否",example = "0")
    private Integer isTop;
    
    /**
     * 状态：0-草稿，1-发布，2-下架，必填
     */
    @Schema(description = "状态：0-草稿，1-发布",example = "1",requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}