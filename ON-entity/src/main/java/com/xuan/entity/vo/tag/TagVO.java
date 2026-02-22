package com.xuan.entity.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签响应数据类
 * 对应接口：5.3.1 获取所有标签
 * 用于返回标签及其文章数量信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "标签响应数据类")
public class TagVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 标签ID
     */
    @Schema(description = "标签ID", example = "1")
    private Long id;
    
    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "Spring Boot")
    private String name;
    
    /**
     * 标签颜色
     */
    @Schema(description = "标签颜色", example = "#1890ff")
    private String color;
    
    /**
     * 文章数量
     */
    @Schema(description = "文章数量", example = "10")
    private Integer articleCount;
}