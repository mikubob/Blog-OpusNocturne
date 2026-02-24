package com.xuan.entity.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分类响应数据类
 * 对应接口：6.1 获取全部分类
 * 用于返回分类及其文章数量信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "分类响应数据类")
public class CategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Long id;
    
    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "Java")
    private String name;
    
    /**
     * 文章数量
     */
    @Schema(description = "文章数量", example = "10")
    private Integer articleCount;
}