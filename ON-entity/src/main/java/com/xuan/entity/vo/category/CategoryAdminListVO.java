package com.xuan.entity.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类后台管理响应数据类
 * 对应接口:6.2.1 分页获取分类列表
 * 用于返回分类的详细信息
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "分类后台管理响应数据类")
public class CategoryAdminListVO implements Serializable {

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    private LocalDateTime updateTime;
}
