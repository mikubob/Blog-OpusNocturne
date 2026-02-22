package com.xuan.entity.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文章创建响应数据类
 * 对应接口文档5.1
 * 用于返回创建文章的标题和id
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Data
@Schema(description = "文章创建响应数据类")
public class ArticleCreatVO implements Serializable {
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
