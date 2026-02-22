package com.xuan.entity.po.blog;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文章分类表实体类
 * 对应数据库表：category
 * 用于存储文章分类信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
@Schema(description = "文章分类表实体类")
public class Category extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称，唯一
     */
    @Schema(description = "分类名称，唯一", example = "Java")
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "Java相关技术")
    private String description;

    /**
     * 排序（升序）
     */
    @Schema(description = "排序（升序）", example = "0")
    private Integer sort;

    /**
     * 状态：1-启用；0-禁用
     */
    @Schema(description = "状态：1-启用；0-禁用", example = "1")
    private Integer status;
}
