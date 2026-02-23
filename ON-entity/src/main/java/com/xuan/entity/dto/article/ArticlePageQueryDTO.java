package com.xuan.entity.dto.article;

import com.xuan.common.domain.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文章列表分页查询参数类
 * 对应接口：5.8 前台文章列表 (Portal)
 * 用于接收前端分页查询文章列表的参数
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文章列表分页查询参数类")
public class ArticlePageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 按分类筛选
     */
    @Schema(description = "按分类筛选", example = "1")
    private Long categoryId;
    
    /**
     * 按标签筛选
     */
    @Schema(description = "按标签筛选", example = "5")
    private Long tagId;
}